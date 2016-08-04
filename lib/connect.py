### Code manipulation ##########################################################

from sys import _getframe

class Dynamic(object):
    """
    Turns a function into a dynamic property.
    The function is evaluated when the attribute is used.

    Example:
    >>> class SomeClass(object):
    ...     @Dynamic
    ...     def someprop(self):
    ...         print 'Calculating value'
    ...         return 13
    ...
    >>> s = SomeClass()
    >>> s.someprop
    Calculating value
    13
    >>> s.someprop
    Calculating value
    13
    >>> 
    """
    def __init__(self, calculate_function):
        self._calculate = calculate_function
    def __repr__(self):
        return "%s(%s)" % (self.__class__.__name__, self._calculate.__name__)
    def __get__(self, obj, _=None):
        return self if obj is None else self._calculate(obj)

class Static(Dynamic):
    """
    Turns a function into a static property.
    The function is evaluated when the attribute is used for the first time.

    Example:
    >>> class SomeClass(object):
    ...     @Static
    ...     def someprop(self):
    ...         print 'Calculating value'
    ...         return 13
    ...
    >>> s = SomeClass()
    >>> s.someprop
    Calculating value
    13
    >>> s.someprop
    13
    >>> 
    """
    def __get__(self, obj, _=None):
        if obj is None:
            return self
        value = self._calculate(obj)
        setattr(obj, self._calculate.func_name, value)
        return value

def Property(ReturnType, Evaluation=Dynamic):
    """
    Turns a function into a typed property.

    Example:
    >>> class SomeClass(object):
    ...     @Property(int)
    ...     def dynamicprop(self):
    ...         print 'Calculating 1'
    ...         return 1
    ...     @Property(int, Static)
    ...     def staticprop(self):
    ...         print 'Calculating 2'
    ...         return 2
    ...
    >>> s = SomeClass()
    >>> s.dynamicprop + s.staticprop
    Calculating 1
    Calculating 2
    3
    >>> s.dynamicprop + s.staticprop
    Calculating 1
    3
    >>> 
    """
    class TypedProperty(ReturnType, Evaluation):
        def __new__(cls, calculate_function):
            attr = Evaluation(calculate_function)
            attr.__dict__.update(cls.__dict__)
            return attr
    return TypedProperty

### Object manipulation ##########################################################

from sys import _getframe
from itertools import ifilter

class Attributes(object):
    """
    The 'with' statement temporarily exposes all attributes as global variables.
    Any new variables or changes to any exposed attribute are stored in the
    object instance on exit of the 'with' statement.

    Example:
    >>> obj = Attributes()
    >>> obj.x = 1
    >>> with obj:
    ...     y = x + 1
    ...     del x
    ...     
    >>> obj
    Attributes(y = 2)
    >>> 
    """
    def __init__(self, ** attr):
        self.__dict__.update(attr)
        self._entries = 0
    def __repr__(self):
        return "%s(%s)" % (self.__class__.__name__, ", ".join([
            "%s = %r" % v for v in ifilter(lambda (name, _): name[0] != '_',
                                           self.__dict__.items())]))
    def __enter__(self):
        """Show all attributes temporarily as global variables."""
        self._entries += 1
        if self._entries == 1:
            callers = []
            depth = 0
            try:
                while 1:
                    callers.append(_getframe(depth + 1).f_locals)
                    depth += 1
                    if _getframe(depth).f_code.co_name == '<module>': raise
            except:
                pass
            callers.append(_getframe(depth).f_globals)
            callers.reverse()
            self._callers = callers
            self._backups = [s.copy() for s in self._callers]
            self._callers[0].update(self.__dict__)
    def __exit__(self, exc_type, exc_value, traceback):
        """Hide all exposed attributes and any new variables"""
        if self._entries == 1:
            callers = {}
            for s in self._callers: callers.update(zip(s.keys(), [s] * len(s)))
            currentNames = set(callers.keys())
            savedNames = set()
            for s in self._backups: savedNames.update(s.keys())
            exposedNames = set(self.__dict__.keys())
            for n in exposedNames - currentNames:
                delattr(self, n)
            for n in currentNames - savedNames:
                s = callers[n]
                setattr(self, n, s[n])
                del s[n]
            for s, b in zip(self._callers, self._backups):
                s.update(b)
            del self._callers, self._backups
        self._entries -= 1

class Scope(Attributes):
    """
    Inheritable attributes.

    Examples:
    >>> s0 = Scope(x=1)                 # s0.x=1
    >>> s1, s2 = s0(y=10), s0(y=100)    # s1.x=s2.x=s0.x; s1.y=10; s2.y=100
    >>> with s1:
    ...     z = x + y + s2.y            # s1.z = s1.x + s1.y + s2.y = 111
    ...
    >>> s3 = s1(s2)                     # s3.x=s2.x; s3.y=s2.y; s3.z=s1.z
    >>> with s3:
    ...     print x, y, z
    ... 
    1 100 111
    """
    def __init__(self, * scopes, ** attributes):
        """Inherit scopes and add attributes"""
        Attributes.__init__(self)
        self._scopes = ()
        self.attach(* scopes, ** attributes)
    def __repr__(self):
        return "%s(%s)" % (self.__class__.__name__, ", ".join(
            [repr(s) if not s is self else "<recursion>" for s in self._scopes]
            + ["%s = %r" % v for v in ifilter(lambda (name, _): name[0] != '_',
                                              self.__dict__.items())]))
    def attach(self, * scopes, ** attributes):
        """Inherit more scopes and add more attributes"""
        self._scopes = scopes + self._scopes
        self.__dict__.update(attributes)
    def __call__(self, * scopes, ** attributes):
        """Create a child scope with more scopes and attributes"""
        return self.__class__(* (scopes + (self,)), ** attributes)
    def __enter__(self):
        for s in reversed(self._scopes): s.__enter__()
        Attributes.__enter__(self)
    def __exit__(self, exc_type, exc_value, traceback):
        Attributes.__exit__(self, exc_type, exc_value, traceback)
        for s in self._scopes:
            s.__exit__(exc_type, exc_value, traceback)

from itertools import ifilter
from spiro.spiroclient import SpiroClient
from spiro.spiroserver import SpiroServer
from urlparse import urlparse
from threading import Thread

class Workspaces(SpiroServer, Thread):
    """
    Publishes named workspaces with some shared attributes.

    Example (here publishing on another port than default 9091):
    
    >>> w = Workspaces('//:9092', readme = "Hello World!")
    Service spiro://:9092/<workspace> started.
    """
    host = ""   # Serves all interfaces
    port = 9091 # Default spiroserver port 9091
    # Verbosity
    Trace = 4
    Warnings = 3
    Errors = 2
    def __init__(self, host=host, verbosity = Warnings, ** shared):
        self._host = host
        u = urlparse(host, scheme='spiro')
        SpiroServer.__init__(self, host = u.hostname or "",
                             port = u.port or self.port,
                             logVerbosity = verbosity)
        Thread.__init__(self)
        self.modules = shared
        self.start()
        print "Service spiro://%s:%d/<workspace> started." % (
            self.host, self.port)
    def __repr__(self):
        return "%s(%s)" % (self.__class__.__name__, ", ".join(
            [repr(self._host)] +
            ["%s = %r" % a for a in self.modules.items()]))

class Workspace(Attributes):
    """
    Uses a named workspace on a server.
    
    >>> ### Workspaces should be available on a device that is always on. ###
    >>> w = Workspaces(readme = "Data is safe here!")
    Service spiro://:9091/<workspace> started.
    >>> a = Workspace('a', x = 1)
    >>> with a:
    ...     print readme
    ...     y = x + 1
    ...     del x
    ... 
    Data is safe here!
    >>> a
    Workspace('a', readme = 'Data is safe here!', y = 2)
    >>> del a
    >>> Workspace('a')
    Workspace('a', readme = 'Data is safe here!', y = 2)
    """
    host = "127.0.0.1" # Connect to localhost in same device.
    # Verbosity
    Trace = 4
    Warnings = 3
    Errors = 2
    _localattributes = ['_name', '_connection', '_callers', '_backups',
                        '_entries']
    def __init__(self, name, verbosity=Warnings, ** attributes):
        self._name = name
        u = urlparse(name, scheme='spiro')
        # TODO: "https://user:password@hostname:port/path" scheme.
        if u.scheme == 'spiro':
            c = SpiroClient(u.path, host = u.hostname or 'localhost',
                            port = u.port or Workspaces.port,
                            verbosity=verbosity)
        else: raise "Only a spiro connection scheme is supported."
        self._connection = c
        for attr, val in attributes.items(): setattr(c, attr, val)
        attributes = dict([(attr, getattr(c, attr)) for attr in c.dir()])
        Attributes.__init__(self, ** attributes)
    def __repr__(self):
        # TODO: Remove this connection usage when server-push is implemented.
        c = self._connection
        return "%s(%s)" % (self.__class__.__name__, ", ".join(
            [repr(self._name)] +
            ["%s = %r" % v for v in [(a, getattr(self, a)) for a in ifilter(
                lambda a: a[0] != '_', c.dir())]]))
    def __getattr__(self, attr):
        val = self.__dict__[attr] = getattr(self._connection, attr)
        return val
    def __delattr__(self, attr):
        del self.__dict__[attr]
        if not attr in self._localattributes:
            self._connection.do("del %s" % attr)
    def __setattr__(self, attr, val):
        if attr in self._localattributes:
            self.__dict__[attr] = val
        else:
            setattr(self._connection, attr, val)
            self.__dict__[attr] = getattr(self._connection, attr)
    def __enter__(self):
        # TODO: Remove this method when server-push is implemented.
        c = self._connection
        self.__dict__.update(dict([(attr, getattr(c, attr)) for attr in c.dir()]))
        Attributes.__enter__(self)

### Attribute management #######################################################
from enthought.traits.api import HasTraits, Trait, Float, Str, BaseStr, \
     Python, PythonValue, Instance, adapts
from enthought.traits.ui.api import View, Group, Item, OKButton, CancelButton

from quantities import \
     UnitQuantity as unit, \
     CompoundUnit as units, \
     Quantity as exact, \
     UncertainQuantity as real
     
class Unit(BaseStr):
    info_text = 'unit string'
    dimension = None
    def validate(self, object, name, value):
        try:
            self.dimension = exact(1.0, value)
            return value
        except:
            self.error(object, name, value)

class UnitWithFixedDimension(Unit):
    def info(self):
        return "unit string" if self.dimension == None \
               else "unit string with fixed dimension ('%s')" % (
                   str(self.dimension.simplified).split(' ')[1])
    def validate(self, object, name, value):
        if value == None:
            self.dimension = None
            return ""
        else:
            try:
                if self.dimension == None:
                    self.dimension = exact(1.0, value)
                self.dimension.rescale(value)
                return value
            except:
                self.error(object, name, value)

class Real(HasTraits):
    """
    Real data with fixed dimension

    >>> x = Real(name = 'x', dimension = 'm')
    >>> x.data = real(1, 'mm', 0.1)
    >>> x.configure_traits() # Opens a dialog for editing the data.
    True
    """
    data = Trait(real(None))
    name = Str
    value = Float(None)
    uncertainty = Float
    unit = UnitWithFixedDimension
    dimension = Unit
    traits_view = View(
        Item(name = 'name'),
        Item(name = 'value'),
        Item(name = 'uncertainty'),
        Item(name = 'unit'),
        Item(name = 'dimension'))
    def _dimension_changed(self, old, new):
        try:
            self.data.rescale(new)
        except:
            self.unit = None
            self.data = real(None, new)
        self._data_changed(old, self.data)
    def _unit_changed(self, old, new):
        self.data = self.data.rescale(units(new.encode('ascii')))
        self._data_changed(old, self.data)
    def _uncertainty_changed(self, old, new):
        self.data.set_uncertainty(new)
    def _value_changed(self, old, new):
        self.data = real(new, self.data.units, self.data.get_uncertainty())
    def _data_changed(self, old, new):
        if str(old) != str(new):
            self.unit = str(new.units).split(' ')[1].strip('(').strip(')')
            self.value = float(new)
            self.uncertainty = float(new.get_uncertainty())

### Resource manipulation ######################################################

try:
    from visa import instrument, get_instruments_list as ports
except:
    print "VISA libraries missing. Recommended source: ni.com"
from new import instancemethod
import sys
from time import sleep

def enter(* objects):
    for o in objects: o.__enter__()
def exit(* objects):
    if objects:
        for o in reversed(objects): o.__exit__(None, None, None)
    else: sys.exit()

class Driver(Attributes):
    """
    Simplifies interactive prototyping and usage of text-based commands.
    Replacement for, or add-on to, existing AT/ITP/ATP software.

    The following attributes needs to be defined in the class or when a driver
    is instantiated:
    
    commands:   String patteclass Phone(Driver):
    ...     commands=dict(attention = "AT", call = "ATD%s", hangup = "H0")
    ...     events=dict(ok = "OK")
    ...     procedures=dict(enter = "attention(), ok()")
    ...     expectEcho = Truerns used when writing commands.
    events:     String patterns used when reading events.
    procedures: String patterns used when running procedures.
    """
    """
    Examples:
    >>> class Phone(Driver):
    ...     commands=dict(attention = "AT", call = "ATD%s", hangup = "H0")
    ...     events=dict(ok = "OK")
    ...     procedures=dict(enter = "attention(), ok()")
    ...     expectEcho = True
    ...
    >>> phonePort = ports()[2] # Assuming the third port controls the phone.
    >>> phone = Phone(phonePort, timeout = 25)
    >>> me = "+4646149234" # A global variable.
    >>> enter(phone) # Beginning command-oriented usage.
    Entering Phone('ASRL5::INSTR')
    Executing 'attention(), ok()' % ()
    ASRL5::INSTR> AT
    ASRL5::INSTR< AT
    ASRL5::INSTR< OK
    >>> # Items defined now affect only the phone object.
    >>> procedure(trick = "call('%s'), sleep(15), hangup()")
    >>> trick(me)
    Executing "call('%s'), no_carrier(), hangup()" % ('+4646149234',)
    ASRL5::INSTR> ATD+4646149234
    ASRL5::INSTR< ATD+4646149234
    ASRL5::INSTR> H0
    ASRL5::INSTR< H0
    >>> exit(phone) # Ending command-oriented usage.
    Exiting Phone('ASRL5::INSTR')
    >>> 
    """
    def __init__(self, port, ** attributes):
        timeout = attributes.pop('timeout', 5)
        attributes.update(dict(
            command = self.command,
            event = self.event,
            procedure=self.procedure))
        Attributes.__init__(self, ** attributes)
        self._device = instrument(port, timeout = timeout)
        self.command(** self.commands)
        self.event(** self.events)
        self.procedure(** self.procedures)
    def __repr__(self):
        return "%s(%s)" % (self.__class__.__name__, "".join(
            [repr(self._device.resource_name)] +
            [", %s = %r" % (n, v) if n[0] != "_" and False else "" \
             for n, v in self.__dict__.items()]))
    def __enter__(self):
        print "Entering", repr(self)
        Attributes.__enter__(self)
        if 'enter' in self.procedures: self.enter()
    def __exit__(self, exc_type, exc_value, traceback):
        print "Exiting", repr(self)
        if 'exit' in self.procedures: self.exit()
        Attributes.__exit__(self, exc_type, exc_value, traceback)
    class Command:
        def __init__(self, driver, pattern):
            self._driver = driver
            self._pattern = pattern
        def __call__(self, * values):
            self._driver.write(self._pattern % values)
    class Event(Command):
        def __call__(self, * values):
            assert self._driver.read() == self._pattern % values, \
                "expected " + self._pattern % values
    class Procedure(Command):
        def __call__(self, * values):
            device = self._driver._device
            timeout = device.timeout
            device.timeout = 0
            try:
                dump = self._driver._device.read_raw()
                print "Warning: Un-read characters before procedure"
                print repr(dump)
            except:
                pass
            device.timeout = timeout
            glob = {}
            if hasattr(self._driver, '_scopes'):
                for s in self._driver._scopes: glob.update(s)
            print "Executing", repr(self._pattern), '%', repr(values)
            eval(self._pattern % values, glob, self._driver.__dict__)
    def command(self, ** commands):
        "defines simple name=pattern commands(s)"
        for name, pattern in commands.items():
            self.commands[name] = pattern
            setattr(self, name, Driver.Command(self, pattern))
        if hasattr(self, '_scopes'):
            self._scopes[0].update(zip(commands.keys(), [
                getattr(self, n) for n in commands.keys()]))
    def event(self, ** events):
        "defines simple name=pattern events(s)"
        for name, pattern in events.items():
            self.events[name] = pattern
            setattr(self, name, Driver.Event(self, pattern))
        if hasattr(self, '_scopes'):
            self._scopes[0].update(zip(events.keys(), [
                getattr(self, n) for n in events.keys()]))
    def procedure(self, ** procedures):
        "defines simple name=pattern procedure(s)"
        for name, pattern in procedures.items():
            self.procedures[name] = pattern
            setattr(self, name, Driver.Procedure(self, pattern))
        if hasattr(self, '_scopes'):
            self._scopes[0].update(zip(procedures.keys(), [
                getattr(self, n) for n in procedures.keys()]))
    def write(self, message):
        print("%s> %s" % (self._device.resource_name, message))
        self._device.write(message)
        if getattr(self, 'expectEcho', False):
            echo = self.read()
            assert echo == message, \
                "echo " + message + " expected, but got " + echo
    def read(self):
        message = ""
        while message == "": message = self._device.read()
        message = message.strip()
        print("%s< %s" % (self._device.resource_name, message))
        return message

if __name__ == "__main__":
    import doctest
    try:
        doctest.testmod()
    except:
        print "This test suite needs"
        print "* VISA libraries and"
        print "* two phones."
        print "support@quflow.com/Joakim Pettersson"
