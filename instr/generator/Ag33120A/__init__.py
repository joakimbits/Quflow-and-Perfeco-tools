GENERATOR = r"H:\Test\generator\generator.vi"
from pylab import arange
from LabVIEW import VI

Giga = 1.e9
RAMP = arange (-50, 10, 10) # dBm
LB = arange (0.8, 0.9, .01)*Giga # Hz
HB = arange (1.8, 1.9, .01)*Giga # Hz

class Generator (VI):
    """
    frequency(Hz), power=0(dBm) -> frequency(Hz)
    """
    def __init__(self, frequency=None, power=None, gpib=11):
        """
        Open a session with the device (GPIB address)
        """
        VI.__init__(self,
            GENERATOR,
            ['RF on',
             'frequency (Hz)',
             'power (dBm)',
             'VISA session',
             'error in (no error)'],
            ['VISA session out',
             'error out'])
        self.session = (('GPIB0::%d::INSTR' % gpib, 0), None)
        self.power = None
        self (frequency, power)
    def __call__(self, frequency, power=None):
        """
        Set frequency (GHz) and output power (0dBm).
        """
        if power != None: self.power = power
        self.session = VI.__call__(self, frequency != None, frequency, self.power, * self.session)
        return frequency
    def __del__(self):
        """
        Switch off RF power
        """
        self (None)

if __name__ == "__main__":
    print "Generator g:"
    g = Generator ()
    print " Power rampup with frequency shirps in LB"
    for p in RAMP:
        for f in LB:
            g (f, p)
    del g
