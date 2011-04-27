from visa import instrument
from time import sleep

class Signal:
    """
    Controller for HP ESG-D4000A by ebtjope 2005-11-01
    """
    def __init__(self, frequency=4e9, power=-135, gpib=11):
        self._instr = instrument ("GPIB::%d" % gpib)
        self._frequency = frequency
        self._power = power
        self (frequency, power)
        self.__doc__ = self.__call__.__doc__
    def __call__(self, frequency, power=None):
        """
        frequency(Hz), power=(dBm) -> frequency(Hz)
        frequency=None switches off RF.
        """
        if power != None: self._power = power
        if frequency != None: self._frequency = frequency
        self._instr.write ("FREQ %.0f Hz\nPOW %s dBm\nOUTPUT %d" % (
            self._frequency, self._power, frequency != None))
        sleep (.1)
        return frequency
    def __del__(self):
        """
        Switch off RF power
        """
        self (None)

class Waveform:
    """
    Controller for Agilent 33120A by ebtjope 2005-11-07
    """
    def __init__(self, high=50e-3, low=0, frequency=1e3,
                 shape='SQUare', dutyCycle=50, unit='VPP', load='50', gpib=19):
        self._instr = instrument ("GPIB::%d" % gpib)
        self._instr.write ("DISPlay:TEXT '%s OHM'" % load)
        self._instr.write ("OUTPut:LOAD %s" % load)
        self._instr.write ("VOLTage:UNIT %s" % unit)
        self._instr.write ("FUNCtion:SHAPe %s" % shape)
        if shape == 'SQUare':
            self._instr.write ("PULSe:DCYCle %g" % dutyCycle)
        self._format = "APPL:%(_shape)s %(_frequency)g, %(_amplitude)g, %(_offset)g"
        self._shape = shape
        self._high = high
        self._low = low
        self._frequency = frequency
        self (high, low, frequency)
        self.__doc__ = self.__call__.__doc__
    def __call__(self, high=None, low=None, frequency=None):
        """
        high=(V), low=(V), frequency=(Hz) -> high (Hz)
        """
        if high != None: self._high = high
        if low != None: self._low = low
        if frequency != None: self._frequency = frequency
        self._amplitude = self._high - self._low
        self._offset = (self._high + self._low) / 2
        self._command = self._format % self.__dict__
        self._instr.write (self._command)
        self._instr.write ("DISPlay:TEXT '%g-%gV %gHz'" % (self._low, self._high, self._frequency))
        return self._high
    def __del__(self):
        """
        Switch off waveform (i.e., set to minimum amplitude)
        """
        self (50e-3, 0)
        self._instr.write ("DISPlay:TEXT:CLEar")

if __name__ == "__main__":
    """
    from pylab import arange
    print "Signal generator s:"
    s = Signal ()
    print " Power rampup with frequency shirps in LB+HB"
    Giga = 1e9
    RAMP = arange (-50, 10, 10) # dBm
    LB = arange (0.8, 0.9, .01)*Giga # Hz
    HB = arange (1.8, 1.9, .01)*Giga # Hz
    for p in RAMP:
        for f in list (LB) + list (HB):
            s (f, p)
    print
    print "Waveform generator w with 216 Hz square wave:"
    w = Waveform (frequency=216)
    print " Rampump to 1.8 V"
    for high in arange (0.1, 1.81, .1): w (high)
    #del w, s
    """
    s = Signal (816e6, power=10)
    w = Waveform (1.8, frequency=216)
