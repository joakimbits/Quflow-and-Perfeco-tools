from drivers.LabVIEW import VI, Inf, NaN
import drivers
_vi = dict (
    module  = drivers,
    path    = r'detector\SpectrumAnalyzer.vi',
    inputs  = ['band (Hz)',
               'reference (dBm)',
               'resolution (Hz)',
               'variable shape (F)',
               'noise (dBm/Hz,Hz)',
               'gain (dB,Hz)',
               'VISA session',
               'error in (no error)'],
    outputs = ['signal & noise (dB vs Hz)',
               'peaks (dB,Hz)',
               'significance (dB)',
               'resolution out (Hz)',
               'VISA session out',
               'error out'])

class Analyzer (VI):
    """
    band=[Hz], ref=(dBm), bw=(Hz), shape=(?), noise=[dBm/Hz,Hz], gain=[dB,Hz]
    -> signal [dB-Hz], noise [dB-Hz], peaks [dB,Hz], significance [dB])

    Measure spectrum & detect peaks
    """
    NoGain = ((0., 20.), (0., 10e9)) # dB,Hz
    NoNoise = ((-Inf, 20.), (-Inf, 10e9)) # dBHz,Hz
    def __init__(self,
                 band=None, #[Hz]
                 ref=None,  #(dBm)
                 bw=None,   #(Hz)
                 shape=None,#(?)
                 noise=None,#[dBm/Hz,Hz]
                 gain=None, #[dB,Hz]
                 gpib=20):  #GPIB address
        VI.__init__(self, ** _vi)
        self._visa = ('GPIB0::%d::INSTR' % gpib, 0)
        self._error = (False, 0, "")
        self (band, ref, bw, shape, noise, gain) # configure the instrument
    def __call__(self, * values, ** options):
        """
        band=, ref=, bw=, shape=, noise=, gain= -> signal, noise, peaks, margins, bw
        Remembers previous values of all inputs.
        """
        values = list (values) + [self[i] for i in self._inputs[len (values):]]
        for i, option in enumerate (['band', 'ref', 'bw', 'shape', 'noise', 'gain']):
            if options.has_key (option): values[i] = options.pop (option)
        ((f0, df, signal), (F0, dF, noise)), peaks, margins, bw, self._visa, self._error = VI.__call__(self,
            * (values + [self._visa, self._error]), ** options)
        return (
            (f0, df, list (signal)),
            (F0, dF, list (noise)),
            list (peaks),
            list (margins),
            bw)

if __name__ == "__main__":
    print "Analyzer a:"
    a = Analyzer (band=[30e6, 4e9], ref=-70)
    print " Peaks detected:", len (a ()[2])
