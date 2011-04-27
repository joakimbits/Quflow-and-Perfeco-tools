"""
Glue to LabVIEW
"""
from labview import Application
from UserDict import UserDict 

class VI (UserDict, dict):
    """
    Access a LabVIEW VI
    """
    def __init__(self, path, inputs=[], outputs=[], session=None):
        "path, inputs=[], outputs=[]"
        self.path = path
        self.inputs = inputs
        self.outputs = outputs
        self.session = session or Application ()
        self.vi = self.session.GetVIReference (path, "", True)
        if self.__doc__ == VI.__doc__:
            # Make these parameters pop-up in Python GUIs
            self.__doc__ = ", ".join (inputs) + ' -> ' + \
                           ", ".join (outputs)
    def __str__(self):
        return self.path
    def __repr__(self):
        return self.__class__.__name__ + repr (
            (self.path, self.inputs, self.outputs))
    def Show(self):
        "Show panel"
        self.vi.OpenFrontPanel (True, True)
    def Hide(self):
        "Hide panel"
        self.vi.CloseFrontPanel ()
    def __setitem__(self, key, value):
        "self[key] = value"
        self.vi.SetControlValue (key, value)
    def __getitem__(self, key):
        "self[key]"
        return self.vi.GetControlValue (key)
    def copy(self):
        copy = {}
        for name, value in self.iteritems ():
            copy[name] = value
        return copy
    def __setattr__(self, name, value):
        "self.name = value"
        for key in self.__dict__.get ('inputs', []):
            if key[:len (name)] == name:
                self.Show ()        # Open panel to keep values in memory
                self[key] = value   # Modify the input value
                self (* self.Get ())# Run the VI to update all output values
                return
        if name == 'data': # UserDict data
            for name, value in value.iteritems ():
                self[name] = value
        else:
            self.__dict__[name] = value
    def __getattr__(self, name):
        "self.name"
        for key in self.__dict__.get ('outputs', []):
            if key[:len (name)] == name:
                return self.__dict__['vi'].GetControlValue (key)
        if name == 'data': # UserDict data
            return self.copy ()
        return self.__dict__[name]
    def Set(self, * values):
        "Set all input values"
        for i, v in zip (self.inputs, values): self [i] = v
        self.Show ()
    def Get(self):
        "Get all input values"
        if len (self.inputs) == 1:
            return self [self.inputs[0]]
        else:
            return [self [i] for i in self.inputs]
    def Fetch(self):
        "Fetch all output values"
        if len (self.outputs) == 1:
            return self [self.outputs[0]]
        else:
            return [self [o] for o in self.outputs]
    def Update(self, * values):
        "Update all output values"
        for o, v in zip (self.outputs, values): self [o] = v
        self.Show ()
    def __call__(self, * values, ** options):
        """
        * inputs, ** options -> * outputs
        
        Call as a subVI (i.e., using wire-able controls and indicators).
        
        Default options:
            result = outputs
            show = hide = suspend = interact = False
        """
        names = options.pop ('result', self.outputs)[:]
        if type (names) is str:
            names = [names]
        n = len (names)
        names = names + self.inputs[:len (values)]
        values = [None] * n + list (values)
        for name in self.inputs:
            for nickName in options.keys ():
                if name[:len (nickName)] == nickName:
                    print names, values, options
                    names.append (name)
                    values.append (options.pop (nickName))
        if options:
            for option, key in {
                'show': 'openFP',
                'hide': 'CloseFPAfterCall',
                'suspend': 'SuspendOnCall',
                'interact': 'bringAppToFront'}.iteritems ():
                options.setdefault (key, options.pop (option, False))
            result = self.vi.Call2 (names, values, ** options)
        else:
            result = self.vi.Call (names, values)
        if n == 1:
            return result[1][0]
        else:
            return tuple (result[1][:n])

class Dialog (VI):
    """
    Set all inputs, run the VI and fetch all outputs afterwards.
    """
    def __call__(self, * values, ** options):
        "Run the dialog"
        self.Show ()
        self.Set (* values)
        for option, key in {
            'show': 'openFP',
            'hide': 'CloseFPAfterCall',
            'suspend': 'SuspendOnCall',
            'interact': 'bringAppToFront'}.iteritems ():
            options.setdefault (key, options.pop (option, False))
        self.vi.Call2 (** options)
        return self.Fetch ()

if __name__ == '__main__':
    #soundCheck = VI (r'H:\Test\LabVIEW\sound check.vi',
    soundCheck = VI (r'C:\Documents and Settings\ebtjope\Desktop\New Briefcase\Test\LabVIEW\sound check.vi',
                     ['volume',
                      'frequency',
                      'phase',
                      'duration'],
                     ['sound'])
    soundCheck.Show ()
    f0 = 440
    for i in range (-3, 5):
        f = f0 * 2 ** i
        print "soundCheck.f =", f
        soundCheck.f = f
    print "phase was:", soundCheck (0.1, f = 440, hide = True, result = 'volume')
