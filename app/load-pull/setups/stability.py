## Encapsulated classes
from drivers.generator import Signal, Waveform
from drivers.detector import Analyzer 
from drivers.tuner import Rotator

## Local VIs
from drivers.LabVIEW import VI, Inf, NaN
import drivers
model = VI (module  = drivers,
            path    = r'detector\model signal (spline).vi',
            inputs  = ['signal (dB vs Hz)',
                       'max error'],
            outputs = ['points'])

## Local utilities
from pylab import arange, log10
c0 = 3e8 # m/s
Giga = 1e9
milli = 1e-3

import shelve
import setups
archive = shelve.open (setups.__file__ + r"\..\%s.shelve" % __name__)
archive['example tests'] = {}

from requirements.implicit import Absolute, testCases
ExampleTests = testCases (
    Absolute ({30: [(-50, 800), (-50, 950)]}),
    carrier = [849e6, 915e6],
    band = (800e6, 950e6))

class Stability:
    """
    
    Step rotator, step carrier and resolution, find spurioses above power limits
    outside a margin from each generated frequency.
    """
    def __init__(self, archive=archive):
        """
        Initialize the setup
        """
        try:
            carrier, phase, bw, reference, band = self.archive['Stability.__init__']
        except:
            carrier=850e6
            phase=0
            bw=3e6
            reference=0
            band=(800e6, 950e6)
        self.archive = archive
        self.signal = Signal (frequency=850e6, power=11.32)
        # 10.17 dB = 10dB damper (increased gain by 0.17dB)
        # 11.32 dB = 849MHz BP + 10dB
        self.shape = Waveform (1.8, frequency=216)
        self.load = Rotator ()
        self.analyzer = Analyzer (gain = archive.get ('gain', None))
        self.analyzer.Show ()
        self.noises = archive.get ('noises', {
            self.analyzer.resolution: self.analyzer.noise})
        self.archive['noises'] = self.noises
        self.archive['gain'] = self.analyzer.gain
        self (carrier, phase, bw, reference, band)
        self.__doc__ = self.__call__.__doc__
    def __call__(self, carrier=NaN, phase=None, bw=None, reference=None, band=None):
        """
        carrier=(Hz), phase=(deg), bw=(Hz), reference=(dBm), band=(Hz,Hz) \
        -> spectrum, peaks, significance
        Analyze stability
        """
        if carrier == carrier:
            self.carrier = carrier
        if phase != None:
            x = c0 * phase / (2 * self.carrier * 360)
        else:
            x = self.delay / c0 / 2
        ## preparation
        self.carrier = self.signal (carrier)
        self.delay = 2 * self.load (x / milli) * milli / c0
        self.phase = 360 * self.delay * self.carrier
        if bw != None: self.bw = bw
        if reference != None: self.reference = reference
        if band != None: self.band = band
        try:
            noise = self.noises[self.bw]
        except:
            best = Inf
            for b in self.noises.keys ():
                if abs (b / bw - 1) < abs (best / bw - 1): best = b
            print "(extrapolating %g-kHz noise)" % (best / 1e3),
            noise = self.noises[best]
        ## execution
        return self.analyzer (self.band, self.reference, self.bw, True, noise)
    def Test(self, case='example tests', tests=ExampleTests, margin=1.8e6, phaseResolution=4):
        """
        case='example tests', tests=ExampleTests, margin=1.8e6(Hz), phaseResolution=4(deg) ->  {(dBm,Hz): (Hz,deg)}
        Execute tests
        """
        ## preparation
        archive = self.archive
        section = archive.get (case, {})
        deviations = section.get ('deviations', {})
        meta_info = {('deviation (dB)', 'at (Hz)', 'requirement'): 'setup'}
        if deviations:
            assert section['deviations_meta'] == meta_info, 'archived deviations'
            print "Keeping", len (deviations), "existing deviations"
            worst = max (deviations)[0]
        else:
            section['deviations_meta'] = meta_info
            worst = 0
        ## execution
        carrier = self.signal
        load = self.load
        analyzer = self.analyzer
        noises = self.noises
        f = [t['carrier'] for t in tests.keys ()]
        x0 = 0
        x1 = min ([0.190, c0/min(f)/2]) # TODO: range-checking in Generator
        dx = phaseResolution/360. * c0/max(f)/2
        for x in arange (x0, x1, dx):
            x = load (x/milli)*milli # TODO: SI unit in Generator
            print "rotator", x, 'mm:'
            for transmitter, setups in tests.iteritems ():
                transmitter = transmitter.copy ()
                f0 = carrier (transmitter.pop ('carrier', None))
                del transmitter['band']
                if transmitter: print " Ignoring", transmitter
                v = 360 * 2*x*f0/c0
                print " carrier", f0/1e6, "MHz, load", v, "deg:"
                for setup, shape in setups.iteritems ():
                    bw = setup.get ('bw', None)
                    if bw:
                        print "  dector", bw/1e3, 'kHz:',
                        try:
                            analyzer['noise (dBm/Hz,Hz)'] = noises[bw]
                        except:
                            try:
                                print "(extrapolating %g-kHz noise)" % (analyzer.resolution / 1e3),
                            except:
                                print "(no noise)",
                                analyzer['noise (dBm/Hz,Hz)'] = [(-Inf, 20), (-Inf, 10e9)]
                        for (p1, f1), (p2, f2) in zip (shape[::2], shape[1::2]):
                            if p1 == p2 < Inf and f1 < f2:
                                if False:#bw < 30e3:
                                    print "(skipped due to detector problem)",
                                else:
                                    requirement = "<%g dBm in %g-%g MHz" % (p1, f1/1e6, f2/1e6)
                                    print requirement,
                                    signal, noise, peaks, significance, bw = analyzer ((f1, f2), p1, bw)
                                    for (p, f), s in zip (peaks, significance):
                                        if s > 0 and abs (f - f0 * int (f/f0+.5)) > margin:
                                            print
                                            print p, "dBm at", f/Giga, "GHz is", f/f0, 'of', f0/Giga, "GHz at", v, "degrees",
                                            deviations[(p, f, requirement)] = dict (carrier=f0, phase=v, bw=bw)
                                            section['deviations'] = deviations
                                            archive[case] = section
                                            if p > worst: # Remember what to use in next analysis (__call__)
                                                worst = p
                                                self.carrier = f0
                                                self.phase, self.delay = v, 2 * x / c0
                                                self.bw = bw
                                                self.reference = p1
                                                self.band = (f1, f2)
                                        else:
                                            print ' ',
                        print
        return deviations
    def __del__(self):
        self.archive['Stability.__init__'] = (self.carrier, self.phase, self.bw, self.reference, self.band)


## The stuff below is for development of a script in stability.vi.

if __name__ == "__main__":
    try:
        assert i == i
        i += 1
    except:
        i = 0
else:
    i = NaN

"""
carrier, phase, bw, reference, band 
-> signal, noise, peaks, significance
by ebtjope 2005-11-17
requires setups.stability on PYTHONPATH.
"""

if i == 0: # Setup
    from setups.stability import Stability, shelve
    try: assert path
    except: from applications.test849MHz import path
    archive = shelve.open (path)
    stability = Stability (archive)
    carrier = stability.carrier
    phase = stability.phase
    bw = stability.bw
    reference = stability.reference
    band = stability.band
if i >= 0: # Execute
    signal, noise, peaks, significance, bw = stability (
        carrier, phase, bw, reference, band)
    f0, df, signal = signal
    f0, df, noise = noise
    peaks = [list (p) for p in peaks] # convert array to matrix
if i < 0: # Cleanup
    del stability
    archive.close ()
