"""
quflow.structures - Algebra for non-uniform quantities.

FUNCTIONS

deeparray - Generate a variable-depth array from an arbitrary structure.
deepvectorize - Vectorize a function to the innermost nd-leaves of an array. 
independent - Creates a separate axis for each element of an array.

UTILITIES

lazy_property - Delay evaluation until first usage of a property.
rip - Extract samples from dependent parameters.
Index - Arbitrary-shaped array that allows being used as index.
Elements - Flattened array view of arbitary structured data.
Parameter - Inwards-broadcasting array that transposes to Sample.
Sample - Normal (outwards-broadcasting) array that transposes to Parameter.

TARGETS

quflow.distributions - Algebra for distributed quantities.
quflow.systems - Algebra for parameterized quantities.

DEPENDENCIES

numpy
quantities

AUTHORS

1. EUPL (c) Joakim.Pettersson@quflow.com September 2011.
https://github.com/joakimbits/Quflow-and-Perfeco-tools/tree/master/lib

Welcome to patch and improve - every bit is free!
"""

from __future__ import division
from numpy import ndarray, array, shape, asanyarray, broadcast_arrays,\
     expand_dims, empty, float64, flatiter
from quantities import Quantity, units, dimensionless
from hashlib import sha1
from copy import deepcopy


def lazy_property(fn):
    """ Turn a method into a lazy-evaluated property
    
    >>> class Foo:
    ...     @lazy_property
    ...     def bar(self):
    ...         print "Calculating"
    ...         return 42
    ... 
    >>> f = Foo()
    >>> f.bar
    Calculating
    42
    >>> f.bar
    42
    """
    
    attr_name = '_lazy_' + fn.__name__
    @property
    def _lazy_property(self):
        if not hasattr(self, attr_name):
            setattr(self, attr_name, fn(self))
        return getattr(self, attr_name)
    return _lazy_property


def depth(a):
    try:
        return 1 + max(map( depth, a ))
    except:
        return 0


class deepvectorize(object):
    """ Convert function f( * x, ** y ) to evaluate non-uniform arrays.
    
    >>> import math
    >>> nan = float('nan')
    >>> isnan = deepvectorize(math.isnan)
    >>> X = deeparray([[1, nan], 3])
    >>> print isnan(X), isnan(nan)
    [[False  True] False] True
    """
    
    def __init__(self, f, dims=0 ):
        self.f, self.dims = f, dims
        self.__doc__ = "Deepvectorized " + (f.__doc__ or f.func_name)
        
    def __call__(self, * x, ** y ):
        i = len(x) # Number of x-parameters.
        keys = y.keys()
        A = map( deeparray, list(x) + y.values() ) # Parameters as arrays.
        D = map( depth, A ) # Parameter depths.
        if max(D) > self.dims: # Traverse deeper?
            n = max([len(a) for a in A if shape(a)]) # Array length.
            A = [a if shape(a) else [a] * n for a in A] # Transposable arrays.
            return deeparray([self( * z[:i], ** dict(zip( y.keys(), z[i:] )))
                              for z in zip( * A )])
        else: # Evaluate.
            return self.f( * A[:i], ** dict(zip( keys, A[i:] )) )


def deeparray( a, transpose=False, level=0 ):
    """ Similar to numpy.array(a) but handles irregular shapes and units also.
    
    >>> deeparray(1)
    array(1)
    >>> deeparray((1, units.nm))
    array([1, 1 nm (nanometer)], dtype=object)
    >>> deeparray((units.nm, units.m))
    array([  1.00000000e+00,   1.00000000e+09]) * nm
    >>> deeparray((1, units.nm, (2, 3)))
    array([1, 1 nm (nanometer), [2 3]], dtype=object)
    """
    
    try:
        if transpose:
            try: a = zip( * a )
            except: pass
        sub = level + 1
        A = [deeparray( x, transpose, sub ) for x in a] # Make subarrays.
        S = map( shape, A ) # Gather subshapes.
        T = map( type, A ) # Gather subtypes.
        T = [float if t is float64 else t for t in T] # Hacked type coercion.
        D = [asanyarray(x).dtype for x in A] # Gather subdtypes.
        s0, t0, d0 = S[0], T[0], D[0]
        if array([s != s0 for s in S[1:]]).any()\
        or array([t != t0 for t in T[1:]]).any()\
        or array([d != d0 for d in D[1:]]).any():
            a = empty( (len(A),), dtype='object' )
            a[:] = A # Varying shape, type or dtype.
        elif issubclass( t0, Quantity ): # Units.
            V, U = zip( * [(ai.magnitude, ai.units) for ai in A] )
            u0 = U[0]
            try: # Compatible units.
                V1 = [v * u.rescale(u0).magnitude for v, u in zip( V, U )]
                if u0 != dimensionless: # Common units.
                    a = V1 * u0
                else: # No units.
                    a = empty( (len(V),), dtype=d0 )
                    a[:] = V
            except: # Different units.
                a = empty( (len(A),), dtype='object' )
                a[:] = A
        else: # Common shape, type and dtype without units.
            a = array( A, dtype=d0 ) 
    except:
        pass
    return asanyarray(a) if level == 0 else a


def rip(a):
    """ Extracts samples from dependent parameters.
    
    ToDo: Auto-identify sample type to speed up algebra.
    
    >>> rip([[[1,2,3], [4,5,6]], [7,8,9]])
    array([[[1 4], 7],
           [[2 5], 8],
           [[3 6], 9]], dtype=object)
    >>> deeparray( _, transpose=True )
    array([[[1 2 3]
     [4 5 6]], [7 8 9]], dtype=object)
    """
    try: return deeparray(zip( * [rip(x) for x in a] ))
    except: return a


def independent(X):
    """ Put each x in X on its own axis.
    
    >>> x, y, z = independent(deeparray([units.nm, [10, 20], [1, [2, 3]]]))
    >>> print '%s * (%s + %s) == %s'% (x, y, z, x * (y + z))
    1 nm (nanometer) * ([[ 10.]
     [ 20.]] + [1 [ 2.  3.]]) == [[11.0 [ 12.  13.]]
     [21.0 [ 22.  23.]]] nm
    """
    
    if not isinstance( X, ndarray ) or not X.shape: return X
    n = array([array(x).size > 1 for x in X]).sum()
    if not n: return X
    D = reversed(range(n))
    i = 0
    ret = list()
    for x in X:
        if isinstance( x, ndarray) and x.shape:
            x0 = x
            try:
                x = array( x, dtype=float )
            except:
                pass
            x = independent(x)
            for d in D:
                if i != d: x = expand_dims( x, d )
            if isinstance( x0, Quantity ) and not isinstance( x, Quantity ):
                x = x * x0.units
            i += 1
        ret.append(x)
    return array( ret, dtype=ndarray )


def deepiter( a, index=0, coords=(), ndim=0 ):
    """ 
    >>> for element, index, coords in deepiter([0, 1, [2, [3, 4], 5]]):
    ...     print element, index, coords
    0 0 (0,)
    1 1 (1,)
    2 2 (2, 0)
    3 3 (2, 1, 0)
    4 4 (2, 1, 1)
    5 5 (2, 2)
    >>> a = deeparray([(0, 1), [(2, 3), (4, 5)]]); a
    array([[0 1], [[2 3]
     [4 5]]], dtype=object)
    >>> for element, index, coords in deepiter(a): print element, index, coords
    0 0 (0, 0)
    1 1 (0, 1)
    2 2 (1, 0, 0)
    3 3 (1, 0, 1)
    4 4 (1, 1, 0)
    5 5 (1, 1, 1)
    >>> for element, index, coords in deepiter( a, ndim=1 ):
    ...     print element, index, coords
    [0 1] 0 (0,)
    [2 3] 1 (1, 0)
    [4 5] 2 (1, 1)
    >>> elements, indices, coords = zip( * deepiter( a, ndim=1 ) )
    >>> for element in elements:
    ...     element[:] = element[1], element[0]
    >>> a
    array([[1 0], [[3 2]
     [5 4]]], dtype=object)
    >>> a[1][0][:] = 30, 20
    >>> elements
    (array([1, 0]), array([30, 20]), array([5, 4]))
    """
    try:
        assert not ndim or depth(a) > ndim
        for i, x in enumerate(a):
            for e, index, c in deepiter( x, index, coords + (i,), ndim ):
                yield e, index, c
            index += 1
    except:
        yield a, index, coords


class Elements(ndarray):
    """ Flattened array view of arbitary structured data.
    
    >>> a = [0, 1, [2, [3, 4], 5]]
    >>> a_ = Elements(a); a_
    Elements([0, 1, 2, 3, 4, 5])
    >>> a_[2:4]
    Elements([2, 3])
    >>> a_[2:4] = 20, 30; a_
    Elements([ 0,  1, 20, 30,  4,  5])
    >>> a
    [0, 1, [20, [30, 4], 5]]
    >>> a_.base is a
    True
    """
    flat = None # The flattened array
    base = None # A reference to the array that is iterated over.
    allcoords = None # All indices into the base array.
    coords = NotImplemented # Current index into the base array.
    index = NotImplemented # Current index into the flattened array.
    def __new__( cls, a, units=dimensionless, depth=0 ):
        E, I, C = zip( * deepiter( a, ndim=depth ) )
        a_ = Index( E, units, depth ).view(cls)
        a_.flat, a_.base, a_.allcoords = a_, a, C
        return a_
    def __delitem__( a_, i ):
        "del a_[i]"
        super( Index, a_ ).__delitem__(i)
        del a_.allcoords[i]
    def __setitem__( a_, i, x ):
        "a_[i] = x"
        try:
            for j, xj in zip( range( * i.indices(len(a_)) ), x ):
                a_[j] = xj
            return
        except:
            if isinstance( i, slice ): raise
        try:
            for j in range( * i.indices(len(a_)) ):
                a_[j] = x
            return
        except: pass
        try:
            for j, xj in zip( i, x ):
                a_[j] = xj
            return
        except: pass
        try:
            for j in i:
                a_[i] = x
            return
        except:
            pass
        ndarray.__setitem__( a_, i, x )
        coords = a_.allcoords[i]
        e = a_.base
        for j in coords[:-1]: e = e[j]
        e[coords[-1]] = x
    def __setslice__( a_, i, j, X ):
        a_.__setitem__( slice( i, j ), X )
    def copy(x):
        c = super( Index, x ).copy()
        c.flat, c.base, c.allcoords = c, x.base, x.allcoords
        return c


class Index(Quantity):
    """ Arbitrary-structured array with units. Can be used as key for indexing. 
    
    Also adds prettier printing and bugfixes for quantities.
    
    Limitation 1: The responsibility of keeping the data intact is left to the programmer.
    ToDo: Copy instead of reference mutable data, and disable mutations.
    
    Limitation 2: The key (hash) is based on a checksum that has many sparse bransches. 
    ToDo: Change to FastBIT indexing using http://code.google.com/p/pyfastbit.
    That index has only one branch and condenses real quantities well.

    >>> a = Index([(1,2), 3]); {a: 4}
    {Index([[1 2], 3], dtype=object): 4}
    >>> a.flatten()
    Elements([1, 2, 3], dtype=object)
    """
    depth = 0
    
    def __new__( cls, a, units=dimensionless, depth=0, T=None ):
        a = ((a*units if units!=dimensionless else a)
             if isinstance(a, Quantity ) else
             Quantity( deeparray(a), units )).view(cls)
        a.depth = depth
        if T != None: a.__dict__['T'] = T
        return a
    
    def __hash__(a): return hash(sha1(a.data).hexdigest())
    
    @lazy_property
    def flat(a):
        return a.flatten()

    def flatten(a):
        return Elements( a, a.units, a.depth )
    
    def copy(a):
        b = deepcopy(a)
        b.__dict__.update(a.__dict__)
        return b
    
    @lazy_property
    def T(a):
        return Index( deeparray( a, True ), depth=a.depth, T=a )

    @lazy_property
    def T(a):
        return Index( deeparray( a, True ), depth=a.depth, T=a )

    def __repr__(a):
        s = ndarray.__repr__(a).replace(
            '\n', '\n' + ' ' * (len(a.__class__.__name__) - len('array')) )
        if a.units != dimensionless: s += ' * ' + a.dimensionality.string
        return s.replace( 'nan', '...' )
    
    def __str__(a):
        s = ndarray.__str__(a)
        if a.units != dimensionless: s += ' ' + a.dimensionality.string
        return s.replace( 'nan', '...' )
    
    # Bugfixes.
    def __rmul__( x, y ): return x * y 
    def __radd__( x, y ): return x + y
    
    def __getitem__( x, key ):
        if isinstance( key, int ) and x.units != dimensionless:
            # This might be resolved by issue # 826
            return type(x)(x.magnitude[key], x._dimensionality)
        else:
            return super(Quantity, x).__getitem__(key)


class Parameter(Index):
    """ Array algebra aligned on outermost axis (axis 0).

    >>> x, y, X, Y = 100, 200, array([10,11,12]), array([20,21,22,23])
    >>> z, Z1 = array((x, y)), array( (X, Y), dtype=ndarray )
    >>> z + Z1 # Ok because numpy interprets Z1 as 1D.
    array([[110 111 112], [220 221 222 223]], dtype=object)
    >>> Z2 = array( (X, Y[:3]), dtype=ndarray ) # Becomes 2D...
    >>> z + Z2 # Fails because z broadcasts from a new axis 1 to 0.
    Traceback (most recent call last):
      File "<string>", line 1, in <fragment>
    ValueError: shape mismatch: objects cannot be broadcast to a single shape
    >>> Parameter(z) + Z2 # Now z stays on 0 and broadcasts to 1 instead.
    Parameter([[110, 111, 112],
               [220, 221, 222]], dtype=object)
    >>> z + Parameter(Z2)
    Parameter([[110, 111, 112],
               [220, 221, 222]])
    """
    
    @lazy_property
    def T(a):
        return Sample( rip(a), depth=a.depth, T=a )

    def coerce_inner_dims( x, y ):
        x, y = x.view(Index), asanyarray(y)
        xs, ys = x.shape, y.shape
        xd, yd = len(xs), len(ys)
        d = max( xd, yd )
        if xd < d: x = x.reshape(xs + (1,)*(d - xd))
        if yd < d: y = y.reshape(ys + (1,)*(d - yd))
        return (x, y)
    
    __coerce__ = coerce_inner_dims

    def outer_operation( x, op, y ):
        cls = type(x)
        X, Y = x.coerce_inner_dims(y)
        Z = getattr( Quantity, op )( X.view(Quantity), Y )
        if Z is NotImplemented:
            o = cls.outer_operation
            X, Y = broadcast_arrays( X, Y )
            Z = empty( (len(X),), object )
            Z[:] = [o( cls(x), op, y ) for x, y in zip( X, Y )]
        return cls(Z)
    
    def outeralgebra( ops ):
        for op in ops.split(','):
            exec "def %s( x, y ): return x.outer_operation( %r, y )"% (
                op, op)
            yield eval(op)
    
    __add__,__sub__,__mul__,__floordiv__,__mod__,__divmod__,__pow__,\
    __lshift__,__rshift__,__and__,__xor__,__or__,__div__,__truediv__,\
    __radd__,__rsub__,__rmul__,__rdiv__,__rtruediv__,__rfloordiv__,\
    __rmod__,__rdivmod__,__rpow__,__rlshift__,__rrshift__,__rand__,\
    __rxor__,__ror__,__ilshift__,__irshift__,__iand__,__ixor__,__ior__\
    = outeralgebra(
        '__add__,__sub__,__mul__,__floordiv__,__mod__,__divmod__,__pow__,'
        '__lshift__,__rshift__,__and__,__xor__,__or__,__div__,__truediv__,'
        '__radd__,__rsub__,__rmul__,__rdiv__,__rtruediv__,__rfloordiv__,'
        '__rmod__,__rdivmod__,__rpow__,__rlshift__,__rrshift__,__rand__,'
        '__rxor__,__ror__,__ilshift__,__irshift__,__iand__,__ixor__,__ior__')

    
class Sample(Index):
    """ Array algebra aligned on innermost axis. Transposes to Parameter.
    Future plan: Auto-identify leaf type as a record to speed up algebra.
    
    >>> X = deeparray([[1, units.nm], 3])
    >>> X3 = Sample([1*X, 2*X, 3*X]); X3
    Sample([[[1 1.0 nm], 3],
            [[2 2.0 nm], 6],
            [[3 3.0 nm], 9]], dtype=object)
    >>> X3 * deeparray([[3,2],1])
    Sample([[[3 2.0 nm], 3],
            [[6 4.0 nm], 6],
            [[9 6.0 nm], 9]], dtype=object)
    >>> X3.T
    Parameter([[[1 2 3] [ 1.  2.  3.] nm], [3 6 9]], dtype=object)
    >>> P = _ * deeparray([[3,2],1]); P
    Parameter([[[3 6 9] [ 2.  4.  6.] nm], [3 6 9]], dtype=object)
    >>> E = Elements( P, depth=1 ); E
    Elements([[3 6 9], [ 2.  4.  6.] nm, [3 6 9]], dtype=object)
    >>> E[1] = 10*E[1]; E
    Elements([[3 6 9], [ 20.  40.  60.] nm, [3 6 9]], dtype=object)
    >>> P
    Parameter([[[3 6 9] [ 20.  40.  60.] nm], [3 6 9]], dtype=object)
    >>> 
    """
    @lazy_property
    def T(a):
        return Parameter( deeparray( a, True ), depth=a.depth, T=a )

if __name__ == '__main__':
    import doctest
    doctest.testmod()
