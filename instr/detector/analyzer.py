from LabVIEW import Dialog
PATH = r'...\Analysis\8numeric.llb\Peak Detector.vi'

class PeakDetector (Dialog):
    """
    Peak Detector.vi
    """
    def __init__(self):
        Dialog.__init__(self, r'H:\Test\spectrum\PeakDetector.vi',
                    ['X', 'threshold', 'width', 'peaks/valleys',
                     'initialize (T)', 'end of data (T)'],
                    ['# found', 'Locations', 'Amplitudes', '2nd Derivatives'])
    def __call__(self, trace, threshold=0, width=3, valleys=False,
                 initialize=True, endOfData=True):
        return Dialog.__call__(self, trace, threshold, width, not valleys,
                               initialize, endOfData)

if __name__=='__main__':
    print "PeakDetector p:"
    p = PeakDetector ()
    print " Detected",
    w = [0,1,2,3,4,5,6,5,4,3,2,1,0] * 3
    print p (w)[0], 'peaks'
