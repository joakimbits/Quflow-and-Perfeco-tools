print "Calibration for 10 dB damping from generator to detector"
Damping = 10 #dB
NoiseAccuracy = 4 #dB
FrequencyStep = 10e6 #Hz
BW = 30e3 #Hz
Limit = -90 #dB
NoGain = [(0., 20.), (0., 10e9)]

# Open a calibration file next to setups\stability[\__init__].py
from setups import stability
import shelve
calibration = shelve.open (stability.__file__ + r'\..\calibration.shelve')

def calibrate (testCases, insertionLoss=None, gain=[], noises={}, ** setup):
    """
    testCases, insertionLoss=None, gain=[],noises={}, ** setup -> insertionLoss, gain, noises
    testCases:  requirements.implicit.testCases (...)
    noises:     noise model (dBm/Hz-Hz) vs. resolution bandwidth (Hz)
    gain:       gain model (dBm-Hz)
    """
    print "Calibration needed!"
    ## Figure out the calibration needs by looking at the testCases
    # intervals = frequency range (Hz) vs. resolution bandwidth (Hz)
    # fmin, fmax = overall frequency range (Hz)
    from requirements.implicit import Inf
    intervals, fmin, fmax = {}, Inf, -Inf
    for testCase, setups in testCases.items ():
        for setup2, shape in setups.items ():
            bw = setup2['bw']
            if bw:
                lo, hi = intervals.get (bw, (Inf, -Inf))
                for (p0, f0), (p1, f1) in zip (shape[::2], shape[1::2]):
                    if p0 == p1 < Inf:
                        lo = min (lo, f0)
                        hi = max (hi, f1)
                fmin = min (fmin, lo)
                fmax = max (fmax, hi)
                intervals[bw] = (lo, hi)

    ## Prepare the test setup
    s = stability.Stability (** setup)
    
    recover = ""
    
    if not insertionLoss:
        insertionLoss = input ("""
        Measure insertion loss:
        1. Disconnect GSM_IN.
        2. Tune the generator power so that GSM_IN is +-0.1 dBm, using a power meter.
        3. Enter the generator power in dBm: """)
        recover = "Reconnect carrier to GSM_IN."
    
    if not gain:
        s.signal (849e6)
        assert raw_input ("""
        Measure gain:
        1. Disconnect the BP filter and DUT.
        2. Tune the generator power to +-0.1 dBm, using a power meter.
        3. Connect the generator to the detector.
        4. Replace the load-pull tuner (phase rotator) with 50 Ohms.
        5. Press <Enter> to start calibration: """) == "", "Calibration aborted"
        from pylab import arange
        for f in arange (fmin, fmax+1, FrequencyStep):
            print f, 'Hz ->',
            s.signal (f)
            power = s.analyzer (
                band = [f - 1e6, f + 1e6], ref = Limit, bw = BW, shape = True,
                gain = NoGain)[2][0][0]
            print power + Limit, 'dB'
            gain.append ((power + Limit, f))
        recover = "Reconnect BP filter, DUT and load-pull tuner."

    if not noises:
        s.signal (None) # Switch off RF
        assert raw_input ("""
        Measure noise at different resolution bandwidths:
        Switch off the DUT and press <Enter> to start.
        """) == "", "Noise calibration aborted"
        from pylab import log10
        from drivers.LabVIEW import VI
        import drivers
        model = VI (module  = drivers,
                    path    = r'detector\model signal (spline).vi',
                    inputs  = ['signal (dB vs Hz)',
                               'max error'],
                    outputs = ['points'])
        model.Show ()
        s.signal (None)
        for bw, fRange in intervals.items ():
            spectrum = s.analyzer (fRange, bw=bw, gain=NoGain)[0]
            noises[bw] = [(p - 10*log10 (bw) + NoiseAccuracy, f) \
                          for p, f in model (spectrum, NoiseAccuracy)]
        calibration['noises'] = noises
    
    return (insertionLoss, gain, noises)

try:
    insertionLoss = calibration['insertionLoss']
    gain = calibration['gain']
    noises = calibration['noises']
except:
    try:
        tests
    except:
        from test849MHz import tests
    insertionLoss, gain, noises = calibrate (
        tests,
        calibration.get ('insertionLoss', None),
        calibration.get ('gain', []),
        calibration.get ('noises', {}))
    calibration['insertionLoss'] = insertionLoss
    calibration['gain'] = gain
    calibration['noises'] = noises

calibration.close ()
