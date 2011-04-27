ANALYZER = r'H:\Test\detector\SpectrumAnalyzer.vi'
SPURIOSES = [30e6, 4e9] # Hz, to me measured at 3MHz resolution BW

Default = 'default value'
def _default (value, default):
    if value == Default:
        return None
    elif value == None:
        return default
    else:
        return value

from LabVIEW import VI

class Analyzer (VI):
    """
    band=[Hz], ref=(dBm), bw=(Hz), shape=(?), noise=[dBm/Hz,Hz], gain=[dB,Hz]
    -> signal [dB-Hz], noise [dB-Hz], peaks [dB,Hz], significance [dB])

    Measure spectrum & detect peaks
    """
    def __init__(self,
                 band=None, #[Hz]
                 ref=None,  #(dBm)
                 bw=None,   #(Hz)
                 shape=None,#(?)
                 noise=None,#[dBm/Hz,Hz]
                 gain=None, #[dB,Hz]
                 gpib=20):  #GPIB address
        VI.__init__(self, ANALYZER,
                    ['band (Hz)',
                     'reference (dBm)',
                     'resolution (Hz)',
                     'variable shape (F)',
                     'noise (dBm/Hz,Hz)',
                     'gain (dB,Hz)',
                     'VISA session',
                     'error in (no error)'],
                    ['signal & noise (dB vs Hz)',
                     'peaks (dB,Hz)',
                     'significance (dB)',
                     'resolution out (Hz)',
                     'VISA session out',
                     'error out'])
        self.visa = ('GPIB0::%d::INSTR' % gpib, 0)
        self.error = (False, 0, "")
        self.band=self.ref=self.bw=self.shape=None
        self.noise=self['noise (dBm/Hz,Hz)']
        self.gain=self['gain (dB,Hz)']
        self (band, ref, bw, noise, gain, shape) # configure the instrument
    def __call__(self, band=None, ref=None, bw=None, shape=None, noise=None, gain=None):
        band = self.band = _default (band, self.band)
        ref = self.ref = _default (ref, self.ref)
        bw = self.bw = _default (bw, self.bw)
        noise = self.noise = _default (noise, self.noise)
        gain = self.gain = _default (gain, self.gain)
        shape = self.shape = _default (shape, self.shape)
        (signal, noise), peaks, margins, bw, self.visa, self.error = VI.__call__(self,
            band, ref, bw, shape, noise, gain, self.visa, self.error)
        return (signal, noise, peaks, margins, bw)

if __name__ == "__main__":
    print "Analyzer a:"
    a = Analyzer (band=SPURIOSES, ref=-70)
    print " Peaks detected:", len (a ()[2])
