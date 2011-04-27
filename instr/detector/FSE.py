PATH = (r'C:\Test\spectrum\rs\rsfsex\rsfsex.llb' +
        r'\RSFSEx Read Spectrum App.vi')
VISA = 'VISA session'
F0 = 'Start Freq (20.0 Hz)'
F1 = 'Stop Freq (3.5e9 Hz)'
P = 'Measured Spectrum'
MODE = 'Data Mode (F:Configure)'
CONFIGURE = False
MEASURE = True
LB = [.8, .9] # GHz
HB = [1.8, 1.9] # GHz
SPURIOSES = [.03, 4] # at 3MHz resolution BW

from LabVIEW import VI

class Spectrum (VI):
    """
    Measure spectrum
    """
    def __init__(self,
                 band = LB + HB, # GHz
                 gpib=20): # GPIB address
        VI.__init__(self, PATH, [VISA, MODE, F0, F1], [P])
        self.visa = ('GPIB0::%d::INSTR' % gpib, 0)
        self (band)
    def __call__(self, band = None):
        if band:
            self.f0 = 1e9 * min (band)
            self.f1 = 1e9 * max (band)
            VI.__call__(self, self.visa, CONFIGURE, self.f0, self.f1)
        VI.__call__(self, self.visa, MEASURE, self.f0, self.f1)
        return self.Fetch ()

if __name__ == "__main__":
    print "Spectrum s:"
    s = Spectrum ()
    print s ()
