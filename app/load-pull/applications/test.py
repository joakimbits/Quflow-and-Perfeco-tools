case = "spurious emissions"

from requirements.RF_3GPP import Req, testCases, Inf
from setups.stability import Stability
from shelve import open
import applications

tests = testCases (
    Req['3GPP 4.3.3.1']['in-band'],
    Req['3GPP 4.3.3.1']['out-of-band'],
    carrier = 849e6, #[849e6, 915e6],
    band = (800e6, 950e6))

default = open (applications.__file__ + r"\..\default.shelve")
dut = default.get ('DUT', 'SKY77513 003-V2 SN005')
if __name__ == '__main__':
    print "Measuring", case
    from calibration import noises, gain
    default['dut'] = dut = raw_input (
        "Connect a DUT and then enter its ID here (%s): " % dut) or dut
    path = applications.__file__ + r"\..\%s.shelve" % dut
    print "Any", case, "will be recorded in", path
    
    archive = open (path)
    archive['noises'] = noises
    archive['gain'] = gain
    stability = Stability (archive)
    deviations = stability.Test (case, tests)
    if deviations:
        print "FAIL"
        #stability ()
        setups = [s for s in deviations.values ()]
        F0 = [s['carrier'] for s in setups]
        V = [s['phase'] for s in setups]
        P = [p for p, f, req in deviations.keys ()]
        F = [p for p, f, req in deviations.keys ()]
        from disipyl.tkdisipyl import Diddle as Interactive
        from disipyl.plots import ScatterPlot3D
        Interactive (ScatterPlot3D (F0, F, P))
    else:
        print "PASS"
    archive.close ()
else:
    path = applications.__file__ + r"\..\%s.shelve" % dut
    archive = open (path)
    deviations = archive.get (case, {}).get ('deviations', None)
    archive.close ()
default.close ()
