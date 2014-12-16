"""Mix-in classes for automatic attribute setting and representation
https://code.activestate.com/recipes/578973-easy-attribute-setting-and-pretty-representation/

>>> class T(HasInitableAttributes, HasTypedAttributes, IsCallable):
...     spam = str
...     def __init__(self, eggs, ham = 'ham'): pass
...
>>> t = T(eggs = 'bacon'); t(ham = 'eggs'); t.spam += 'sausage'; t
__main__.T('bacon', ham = 'eggs', spam = 'sausage')
>>> 
"""


from inspect import getargspec
from itertools import chain
from sys import platform
if platform == 'pyboard':
    def setattr(object, name, value): pass  # Not yet in MicroPython

class HasInitableAttributes(object):
    """Initializes attributes automatically

    >>> class T(HasInitableAttributes):
    ...     z = 0
    ...     def __init__(self, x, y, **opts): pass
    ...
    >>> t = T(1, 2, a = 3); t
    __main__.T(1, 2, a = 3)
    >>> t.z = 3; t
    __main__.T(1, 2, a = 3, z = 3)
    >>> T(x = 1, y = 2)
    __main__.T(1, 2)
    """

    _argspec = ((), None, None, None)
    _printed = []
    
    def __new__(cls, *pars, **opts):
        "Initialize all attributes in the signature and any other options supplied"
        self = super(HasInitableAttributes, cls).__new__(cls)
        self._argspec = names, parsname, optsname, defaults = getargspec(self.__init__)
        name = type(self).__name__
        if not name in HasInitableAttributes._printed:
            print("%s = %r," % (type(self).__name__, tuple(self._argspec)))
            HasInitableAttributes._printed.append(name)
        if not defaults: defaults = []
        n = len(names) - len(defaults) - 1
        for i, name in enumerate(names[1:]):
            if name in opts:
                if len(pars) == i:
                    pars = pars + (opts.pop(name),)
                elif len(pars) > i:
                    raise TypeError("__init__() got multiple values for argument %r" % name)
        if len(pars) < n:
            pos = len(pars)
            raise TypeError("Required argument %r (pos %d) not found" % (names[1+pos], pos))
        for n, v in chain(zip(names[-len(defaults):], defaults), zip(names[1:], pars), opts.items()):
            setattr(self, n, v)
        return self
    
    def __repr__(self):
        "Show all attributes in the signature and any other public attributes that are changed"
        names, parsname, optsname, defaults = self._argspec
        if not defaults: defaults = []
        optnames = names[-len(defaults):] if defaults else []
        optvalues = (getattr(self, name) for name in optnames)
        othernames = sorted(set((n for n in self.__dict__ if n[0] != '_')) - set(names))
        othervalues = list((getattr(self, name, None) for name in othernames))
        otherdefaults = list((getattr(self.__class__, name, None) for name in othernames))
        return "%s.%s(%s)" % (self.__module__, self.__class__.__name__, ", ".join(chain(
            (repr(getattr(self, name)) for name in names[1:len(names)-len(defaults)]),
            ("%s = %r" % (name, value) for name, value, default in zip(optnames, optvalues, defaults) if value != default),
            ("%s = %r" % (name, value) for name, value, default in zip(othernames, othervalues, otherdefaults) if value != default))))


class HasTypedAttributes(object):
    """Objectifies class attributes automatically

    >>> class T(HasTypedAttributes):
    ...     spam = str
    ...     class C(HasTypedAttributes):
    ...         eggs = list
    ...
    >>> a, b = T(), T(); a.spam += 'ham'; a.C.eggs.append('bacon'); a.spam, b.spam, a.C.eggs, b.C.eggs
    ('ham', '', ['bacon'], [])
    >>> 
    """

    def __new__(cls, *pars, **opts):
        self = super(HasTypedAttributes, cls).__new__(cls)
        for name in dir(self):
            if name[0] != '_':
                value = getattr(self, name)
                if isinstance(value, type):
                    try:
                        setattr(self, name, value(opts.pop(name)) if name in opts else value())
                    except:
                        pass
        if opts:
            raise TypeError("__init__() got%s unexpected keyword argument%s %r" % 
                (" an", "", opts.keys()[0]) if len(opts) == 1 else ("", "s", opts.keys()))
        return self


class IsCallable(object):
    """Update attributes by calling

    >>> class T(IsCallable):
    ...     x = 0
    ...
    >>> t = T(); t(x = 1, y = 2); t.x, t.y
    (1, 2)
    """

    def __call__(self, *pars, **opts):
        self.__dict__.update(*pars, **opts)


if __name__ == '__main__':
    from doctest import testmod
    testmod()
