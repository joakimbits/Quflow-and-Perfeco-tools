from com.iit.api.core import Dynamic

## Maintained: Automatically upgrades all class instances
# Author: Michael Hudson
# http://aspn.activestate.com/ASPN/Cookbook/Python/Recipe/160164
# Includes fix by Joakim to also support "class C(C)" patterns.

try: 
  import weakref, inspect
  
  class MetaInstanceTracker(type):
    def __new__(cls, name, bases, ns):
      t = super(MetaInstanceTracker, cls).__new__(cls, name, bases, ns)
      t.__instance_refs__ = []
      return t
    def __instances__(self):
      instances = [(r, r()) for r in self.__instance_refs__]
      instances = filter(lambda (x,y): y is not None, instances)
      self.__instance_refs__ = [r for (r, o) in instances]
      return [o for (r, o) in instances]
    def __call__(self, *args, **kw):
      instance = super(MetaInstanceTracker, self).__call__(*args, **kw)
      self.__instance_refs__.append(weakref.ref(instance))
      return instance
  
  class InstanceTracker:
    __metaclass__ = MetaInstanceTracker
  
  class MetaAutoReloader(MetaInstanceTracker):
    def __new__(cls, name, bases, ns):
      new_class = super(MetaAutoReloader, cls).__new__(
        cls, name, bases, ns)
      f = inspect.currentframe().f_back
      for d in [f.f_locals, f.f_globals]:
        if d.has_key(name):
          old_class = d[name]
          for instance in old_class.__instances__():
            instance.change_class(new_class)
            new_class.__instance_refs__.append(
              weakref.ref(instance))
          # this section only works in 2.3
          for subcls in old_class.__subclasses__():
            if subcls.__name__ != name \
            or subcls.__module__ != ns['__module__']:
              newbases = ()
              for base in subcls.__bases__:
                if base is old_class:
                  newbases += (new_class,)
                else:
                  newbases += (base,)
              subcls.__bases__ = newbases
          break
      return new_class
  
  class AutoReloader:
    __metaclass__ = MetaAutoReloader
    def change_class(self, new_class):
      self.__class__ = new_class
  
  class Maintained (Dynamic, AutoReloader):
      ""
except:
  class Maintained: 
    "missing Python 2.4 feature"
    def __init__(self): print self, "is not Maintained -", Maintained.__doc__

class Shared (Dynamic):
    ""

class Distributed (Dynamic):
    ""