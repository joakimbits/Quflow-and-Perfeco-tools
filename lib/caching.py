"""
Wrapper for a generator that allows it to be accessed as a list.

Based on recipy http://code.activestate.com/recipes/534114/ (r4)
"""

def generator(cache):
    for i in cache: yield i

class Cache(list):
    """
    Allows a generator to be accessed as a list.

    >>> it = generator(range(5)); type(it)
    <type 'generator'>
    >>> c = Cache(it); isinstance( c, list )
    True
    >>> c
    [0, ...]
    >>> c[:2]
    [0, 1]
    >>> del c[2]
    >>> c
    [0, 1, 3, ...]
    >>> c[2:]
    [3, 4]
    >>> c
    [0, 1, 3, 4]
    """
    generator   = ()
    cached      = 0
    
    def __init__( self, generator ):
        list.__init__(self)
        self.generator = generator

    def next(self):
        x = self.generator.next()
        list.append( self, x )
        self.cached += 1
        return x

    def __getitem__( self, k ):
        if isinstance( k, slice ):
            return [self.__getitem__(i)
                    for i in xrange(k.start, k.stop, k.step)]
        i = self.cached - 1
        while i < k:
            self.next()
            i += 1
        return list.__getitem__( self, k )

    def __getslice__( self, i, j ):
        try:
            self[j-1]
        except:
            pass
        return list.__getslice__( self, i, j )
    
    def __setitem__( self, i, x ):
        self[i]
        return list.__setitem__( self, i, x )
    
    def __setslice__( self, i, j, v ):
        try:
            self[j-1]
        except:
            pass
        return list.__setslice__( self, i, j, v )
    
    def insert( self, i, x ):
        self[i]
        return list.insert( self, i, x )
    
    def pop( self, * I ):
        for i in I: self[i]
        return list.pop( self, * I )

    def remove( self, * I ):
        for i in I: self[i]
        return list.remove( self, * I )

    def __delitem__( self, i ):
        if isinstance( i, slice ):
            for i in xrange(k.start, k.stop, k.step): del self[i]
        else:
            self[i]
            list.__delitem__( self, i )

    def __delslice__( self, i, j ):
        try:
            self[j]
        except:
            pass
        return list.__delslice__( self, i, j )

    def __repr__(self):
        try:
            self.next()
        except:
            return list.__repr__(self)
        return list.__repr__(self)[:-1] + ', ...]'

    def __getattribute__( self, name, default = None ):
        if name in Cache.__dict__:
            return list.__getattribute__( self, name )
        if name in ['__add__', '__contains__', '__eq__', '__ge__', '__gt__',
                    '__iadd__', '__imul__', '__iter__', '__le__', '__len__',
                    '__lt__', '__mul__', '__ne__', '__reduce__',
                    '__reversed__', '__rmul__', '__sizeof__', 'append', 'count',
                    'extend', 'index', 'reverse', 'sort']:
            self[:]
        return list.__getattribute__( self, name, default )
    
## end of http://code.activestate.com/recipes/534114/ }}}

if __name__ == '__main__':
    import doctest
    doctest.testmod()
