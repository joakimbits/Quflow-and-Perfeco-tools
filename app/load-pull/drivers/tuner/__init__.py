# -*- coding: cp1252 -*-

import drivers
_dialog = dict (
    module  = drivers,
    path    = r'tuner\position.vi',
    inputs  = ['position (mm)'],
    outputs = ['destination (mm)'])

from visa import instrument
from pyvisa.vpp43 import read_stb
from time import time, sleep
from drivers.LabVIEW import Dialog, NaN

class Load:
    """
    destination=<RESET>(mm) -> location (mm)
    
    Phase Shifter controlled with GPIB commands
        by Jörgen Lindell & Torbjörn Samuelsson June 1995
        (VIs and page numbers refer to the thesis)
    Ported to Python and extended with calibrated breaking
        by Joakim Pettersson September 2005
    """
    # Breaking calibration
    _Resolution      = 0.2 # mm
    _ShortStop       = 0.6
    _LongStop        = 0.8
    _ShortDistance   = 10
    _Time            = 0.1 # s Recommended breaking time
    # Page 9
    _Config = "C2X" # Ports 1-2 In / 3-4 Out
    _Format = "F0X" # ASCII Hex
    _ReadIn = "G1X" # Read inputs
    _ReadOut = "G2X" # Read outputs
    _SRQEnable = "M1X" # SRQ when move is complete
    _Output = "D%XZX" # %x Write output value x
    _Set = "A%dX" # %n Set output bit n
    _Clear = "B%dX" # %n Clear output bit n
    # Page 13 and reverse engineering from appendix F
    def _map (mode=000, up=0, arm=1, position=0.0):
        x = up << 14 | arm << 13 | int (5 * position)
        for bit, mask in [(10, 100),  # Data3
                          (11, 010),  # Data2
                          (12, 001)]: # Data1
            x |= ((mode & mask) / mask) << bit
        return x
    _Initialize  = _map (000, 1)
    _SweepFwd    = _map (001, 1)
    _SweepBwd    = _map (010, 0)
    _PositionFwd = _map (011, 1)
    _PositionBwd = _map (100, 0)
    _Trig = _Clear % 14
    def __init__(self, address=17):
        # Initialize.vi
        self._instr = instrument ("GPIB::%d" % address)
        self._instr.write (Rotator._Config +
                           Rotator._ReadIn +
                           Rotator._Format +
                           Rotator._SRQEnable +
                           Rotator._Trig)
        print "Rotator is at", self (), 'mm'
    def _read(self):
        # Read Ports.vi
        sleep (Rotator._Time)
        status = int (self._instr.read (), 16)
        self._position = (status & 1023) / 5.
        self._switchB = (status & 2048) == 0
        self._switchN = (status & 1024) == 0
        self._direction = (status & 4096) != 0
        self._pin13 = (status & 8192) / 8192
        self._pin14 = (status & 16384) / 16384
        self._pin15 = (status & 32768) / 32768
        return self._position
    def _debug(self):
        self._read ()
        output = self._instr.ask (Rotator._ReadOut)
        self._instr.write (Rotator._ReadIn)
        status = read_stb (self._instr.vi)
        return """
            Position:    %(_position).1f mm
            SwitchB:     %(_switchB)d
            SwitchN:     %(_switchN)d
            Direction:   %(_direction)d
            """ % self.__dict__ + """
            Output:      0x""" + output + """
            Status byte: 0x%2X""" % status
    def __call__(self, destination=None):
        """
        Set the control to a new destination, move the rotator there, and update the indicator.
        """
        # Mode.vi, without sweep functionality
        self._destination = destination
        x0 = self._read ()
        if destination == None:
            return x0
        elif destination != destination: # NaN -> reset to position 0
            if self._switchN:
                self._output = Rotator._Initialize
            else:
                self._output = None
            distance = self._position
            stop = 0
            breakAt = 0
        else:
            self.Update (destination)
            distance = destination - x0
            if abs (distance) > Rotator._Resolution:
                direction = distance / abs (distance)
                if Rotator._Resolution <= abs (distance) <= Rotator._LongStop:
                    startAt = x0 - direction * (Rotator._LongStop + Rotator._Resolution)
                    if startAt < 0: startAt = destination + Rotator._LongStop + Rotator._Resolution
                    x0 = self (startAt)
                    self.Update (destination)
                    distance = destination - x0
                    direction = distance / abs (distance)
                if abs (distance) <= Rotator._ShortDistance:
                    stop = Rotator._ShortStop
                else:
                    stop = Rotator._LongStop
                breakAt = x0 + distance - direction * stop
                if distance > 0:
                    self._output = Rotator._PositionFwd | int (breakAt / Rotator._Resolution)
                elif distance < 0:
                    self._output = Rotator._PositionBwd | int (breakAt / Rotator._Resolution)
                else:
                    self._output = None
            else:
                self._output = None
        # Send Info To Digital488.vi
        if self._output != None:
            read_stb (self._instr.vi)
            self._instr.write (Rotator._Output % self._output + Rotator._Trig)
            #t0 = time ()
            self._instr.wait_for_srq (10)
            #t1 = time ()
            x1 = self._read ()
            self.Set (x1)
            return x1
        else:
            return x0
    def Update(self, destination):
        "Hook for Dialog"
    def Set(self, position):
        "Hook for Dialog"

class Rotator (Dialog, Load):
    def __init__(self, address=17):
        Dialog.__init__(self, ** _dialog)
        Load.__init__(self, address)
    __call__ = Load.__call__
    def run(self):
        "Handover control to the LabVIEW dialog until it is closed"
        while 1:
            next = Dialog.__call__(self, self._read ()) # get destination
            if next != self._destination:
                self (next) # go to destination
            else:
                return

if __name__ == "__main__":
    print "Rotator r:"
    r = Rotator ()
    print " Moving r to its reset position"
    r (NaN)
    print " Moving r to its highest position"
    r (190)
    print " Handing over control of r to the LabVIEW dialog"
    r.run ()
