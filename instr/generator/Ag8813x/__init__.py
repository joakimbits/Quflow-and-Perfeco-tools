from visa import instrument
from pyvisa.vpp43 import read_stb
from time import time, sleep

class Generator:
    """
    frequency(Hz), power=0(dBm) -> frequency(Hz)
    Segment1 is an init pattern, segments 2-4 is a loop pattern.
    """
    def __init__(self, gpib=10, frequency=312e6,
                 channel=1, amplitude=100e-3, offset=300e-3):
        """
        Open a GPIB session with the device
        """
        self.instr = instrument ("GPIB::%d" % gpib)
        self.write = self.instr.write
        self.ask = self.instr.ask
        self.idn = self.ask ("*IDN?")
        assert "8113" in self.idn, "Expected Agilent 8113x Pulse-/Pattern Generator on " \
               + str(self.instr) + ' but received "%s".' % self.idn
        #write ("*RST;*WAI;")
        #write (":DISP OFF")
        #self.write (":ARM:SOUR EXT1;MODE STAR;SENS POS")
        self.write (":ARM:SOUR IMM")
        self.write (":ROSC:SOUR EXT;EXT:FREQ 10MHz")
        self.write (":PULS:DCYC1 50PCT;HOLD1 DCYC")
        self.write (":PULS:DCYC2 50PCT;HOLD2 DCYC")
        self (frequency, channel, amplitude, offset)
        self.write (":DIG:SIGN:FORM NRZ")
        self.write (":DIG:PATT:LOOP 1;LOOP:INF ON;STAR SEGM2")
        self.write (":PULS:TRIG:MODE CONT;POS 2;VOLT TTL")
        self.Init ("01230123")
        self.Header ("01230123")
        self.Body ("01230123")
        self.Footer ("01230123")
        self.write (":DIG:PATT ON;")
        self.write (":OUTP1 ON;:OUTP1:COMP ON")
        self.write (":OUTP2 ON;:OUTP2:COMP ON")
    def __call__(self, frequency=None, channel=1, amplitude=None, offset=None):
        """
        Set frequency (Hz), amplitude (Vpp) and offset (Vdc).
        """
        if frequency != None: self.frequency = frequency
        self.channel = channel
        if amplitude != None: self.amplitude = amplitude
        if offset != None: self.offset = offset
        if self.frequency:
            self.write (":PULS:PER %gs" % (1.0/self.frequency))
        if self.amplitude:
            self.write (":VOLT%d %gV" % (channel, self.amplitude))
        if self.offset:
            self.write (":VOLT%d:OFFS %gV" % (channel, self.offset))
        return frequency
    def Pattern(self, pattern="3210", segment=1, channel=1):
        if pattern[:4]=="PRBS":
            base = int (pattern[4:]) or 7
            self.write (":DIG:PATT:PRBS %d" % base)
            self.write (":DIG:PATT:SEGM%d:LENG %d" % (segment, 2^base - 1))
            self.write (":DIG:PATT:SEGM%d:TYPE%d PRBS" % (segment, channel))
        else:
            size = str(len(pattern))
            self.write (":DIG:PATT:SEGM%d:DATA%d #%d%s%s;LENG %s" % (
                segment, channel, len(size), size, pattern, size))
    Init = Pattern
    def Header(self, pattern="", segment=2, channel=1):
        self.Pattern (pattern, segment, channel)
    def Body(self, pattern="", segment=3, channel=1):
        self.Pattern (pattern, segment, channel)
    def Footer(self, pattern="", segment=4, channel=1):
        self.Pattern (pattern, segment, channel)
    def Errors(self):
        err = []
        while 1:
            err.append (self.ask(":SYST:ERR?"))
            if not int (err[-1].split(',')[0]): break
        return err
    def __del__(self):
        """
        Switch off outputs
        """
        self.instr.write (":DIG:PATT OFF")

if __name__ == "__main__":
    print "Generator g:"
    g = Generator ()
    print g.Errors ()
