"""
Glue to LabVIEW
"""
from labview import Application
from UserDict import UserDict
Inf = 1e300 * 1e300 # http://mail.python.org/pipermail/python-bugs-list/2004-December/026846.html
NaN = -(Inf - Inf)

class VI (UserDict):
    """
    Access a LabVIEW VI.
    """
    def __init__(self, path=None, inputs=[], outputs=[],
                 module=None, description="", session=None):
        "Connect to a VI through lists of input and output names"
        if module:
            self._path = module.__file__ + "\\..\\" + (
                path or module.__name__ + '.vi')
        else:
            self._path = path
        self._inputs = inputs
        self._outputs = outputs
        self._session = session or Application ()
        self._vi = self._session.GetVIReference (self._path, "", True)
        self._name = self._path[-1:-1 - self._path[::-1].find ('\\'):-1][::-1]
        self._description = description or self._name
        if self.__doc__ == VI.__doc__:
            # Make these parameters pop-up in Python GUIs
            self.__doc__ = ", ".join (self._inputs) + ' -> ' + \
                           ", ".join (self._outputs)
    def __str__(self):
        return self._path
    def __repr__(self):
        return self.__class__.__name__ + repr (
            (self._path, self._inputs, self._outputs))
    def __getitem__(self, key):
        "self[key]"
        try: return self._vi.GetControlValue (key)
        except: return self.__dict__[key]
    def __setitem__(self, key, value):
        "self[key] = value"
        try: self._vi.SetControlValue (key, value)
        except: self.__dict__[key] = value
    def copy(self):
        "dict (self)"
        copy = {}
        for key in self.__dict__.keys ():
            if key[0] != '_': copy[key] = self.__dict__[key]
        for name in self._outputs + self._inputs:
            copy[name] = self[name]
        return copy
    def __getattr__(self, nickName):
        "self.nickName"
        for key in list (self.__dict__.get ('_outputs', [])) + list (
            self.__dict__.get ('_inputs', [])):
            if key[:len (nickName)] == nickName:
                return self.__dict__['_vi'].GetControlValue (key)
        if nickName == 'data':  # Used by UserDict methods
            return self.copy ()
        return self.__dict__[nickName]
    def __setattr__(self, nickName, value):
        "self.nickName = value"
        for key in list (self.__dict__.get ('_inputs', [])) + list (
            self.__dict__.get ('_outputs', [])):
            if key[:len (nickName)] == nickName:
                self[key] = value       # Modify the input value
                self (* self.Get ())    # Run the VI (self._vi.Run doesn't work)
                return
        if nickName == 'data': # Used by UserDict methods
            for name, value in value.iteritems ():
                self[name] = value
        else:
            self.__dict__[nickName] = value
    def Show(self):
        "Show panel"
        self._vi.OpenFrontPanel (True, True)
    def Hide(self):
        "Hide panel"
        self._vi.CloseFrontPanel ()
    def Set(self, * values):
        "Set input values"
        for i, v in zip (self._inputs, values): self [i] = v
        self.Show ()
    def Fetch(self):
        "Fetch output values"
        if len (self._outputs) == 1:
            return self [self._outputs[0]]
        else:
            return [self [o] for o in self._outputs]
    def Get(self):
        "Get input values"
        if len (self._inputs) == 1:
            return self [self._inputs[0]]
        else:
            return [self [i] for i in self._inputs]
    def Update(self, * values):
        "Update output values"
        for o, v in zip (self._outputs, values): self [o] = v
        self.Show ()
    def __call__(self, * values, ** options):
        """
        * inputs, ** options -> * outputs
        
        Call as a subVI (i.e., using wire-able controls and indicators).
        
        Default options:
            result = outputs
            show = hide = suspend = interact = False
        """
        get = list (options.pop ('result', self._outputs)[:])
        if type (get) is str: get = [get]
        params = dict (zip (get, [self.__getattr__ (g) for g in get]))
        params.update (zip (self._inputs, values))
        for nickName in options.keys ():
            for key in self._inputs:
                if key[:len (nickName)] == nickName:
                    params[key] = options.pop (nickName)
                    break
        for i in set (params.keys ()) - set (get):
            if params[i] == None: del params[i] # None -> use default
        names = get + list (set (params.keys ()) - set (get))
        values = [params[n] for n in names]
        if options:
            for option, key in {
                'show': 'openFP',
                'hide': 'CloseFPAfterCall',
                'suspend': 'SuspendOnCall',
                'interact': 'bringAppToFront'}.iteritems ():
                options.setdefault (key, options.pop (option, False))
            result = self._vi.Call2 (names, values, ** options)
        else:
            result = self._vi.Call (names, values)
        if len (get) == 1:
            return result[1][0]
        else:
            return tuple (result[1][:len (get)])

class Dialog (VI):
    """
    Set all inputs, run the VI and fetch all outputs afterwards.
    
    Warning - Use the VI class instead of Dialog if controls are wired to the
    connector pane! The reason is that Dialog use Call instead of Run, so such
    controls will reset to their default value during the call. 
    """
    def __call__(self, * values, ** options):
        "Run the dialog"
        self.Show ()
        self.Set (* values)
        for option, key, default in [
            ('show', 'openFP', True),
            ('hide', 'CloseFPAfterCall', False),
            ('suspend', 'SuspendOnCall', False),
            ('interact', 'bringAppToFront', True)]:
            options.setdefault (key, options.pop (option, default))
        self._vi.Call2 (** options) # Warning - unwired controls resets to default
        return self.Fetch ()

if __name__ == '__main__':
    import drivers
    soundCheck = VI (r'LabVIEW\sound check.vi',
                     ['volume', 'frequency', 'phase', 'duration'], ['sound'],
                     drivers)
    print "soundCheck VI:", soundCheck
    print "soundCheck function:", soundCheck.__doc__
    print "soundCheck table:", soundCheck.keys ()
    print "soundCheck attributes:", ", ".join (
        ["%s/%s/" % (k[0], k[1:]) for k in soundCheck.keys ()])
    print
    print "soundCheck.Show () opens the panel"
    soundCheck.Show ()
    f0 = 440
    for i in range (-3, 5):
        f = f0 * 2 ** i
        p = 30 * i
        print "soundCheck (freq=%g, phase=%g)" % (f, p), '->',
        print len (soundCheck (freq=f, phase=p)[2][0]), "wave samples"
    print "soundCheck.v == soundCheck.vol == soundCheck['volume'] ==", soundCheck.v
    print "soundCheck.duration = 1 runs the VI with 1 s duration"
    soundCheck.d = 1
    print
    print 'You must execute ">>> del soundCheck" or close Python to edit the VI'
