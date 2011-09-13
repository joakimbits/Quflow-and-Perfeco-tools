"""
quflow.structures - Algebra for non-uniform quantities.

FUNCTIONS

deeparray - Generates a variable-depth array from an arbitrary structure.
deepvectorize - Vectorizes a function to the innermost nd-leaves of an array. 
independent - Creates a separate axis for each element of an array.

UTILITIES

lazy_property - Delays evaluation until first usage of a property.
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
     expand_dims, empty
from quantities import Quantity, units, dimensionless


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


def independent(X):
    """ Put each x in X on its own axis.
    
    >>> x, y, z = independent(deeparray([units.nm, [10, 20], [1, [2, 3]]]))
    >>> print '%s * (%s + %s) == %s'% (x, y, z, x * (y + z))
    1 nm (nanometer) * ([[ 10.]
     [ 20.]] + [1 [ 2.  3.]]) == [[11.0 [ 12.  13.]]
     [21.0 [ 22.  23.]]] nm
    """
    if not X.shape: return X
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


class Structure(Quantity):
    """ Base class for Parameter and Sample. Adds prettier printing and bugfixes
    to Quantity.
    """
    
    def __repr__(a):
        s = ndarray.__repr__(a).replace(
            '\n', '\n' + ' ' * (len(a.__class__.__name__) - len('array')) )
        if a.units != dimensionless: s += ' * ' + a.dimensionality.string
        return s
    
    def __str__(a):
        s = ndarray.__str__(a)
        if a.units != dimensionless: s += ' ' + a.dimensionality.string
        return s
    
    # Bugfixes.
    def __rmul__( x, y ): return x * y 
    def __radd__( x, y ): return x + y 
    __getitem__ = ndarray.__getitem__

    
class Parameter(Structure):
    """ Alegbra aligned on outermost axis (axis 0).

    Transposes to Sample.
    
    >>> x, y, X, Y = 100, 200, array([10,11,12]), array([20,21,22,23])
    >>> z, Z1 = array((x, y)), array( (X, Y), dtype=ndarray )
    >>> z + Z1 # Ok because numpy interprets Z1 as 1D.
    array([[110 111 112], [220 221 222 223]], dtype=object)
    >>> Z2 = array( (X, Y[:3]), dtype=ndarray ) # Becomes 2D...
    >>> z + Z2 # Fails because z broadcasts from a new axis 1 to 0.
    Traceback (most recent call last):
      File "<string>", line 1, in <fragment>
    ValueError: shape mismatch: objects cannot be broadcast to a single shape
    >>> Parameter(z) + Z2 # z stays on 0 and broadcasts to 1 instead.
    Parameter([[110, 111, 112],
               [220, 221, 222]], dtype=object)
    >>> z + Parameter(Z2)
    Parameter([[110, 111, 112],
               [220, 221, 222]], dtype=object)
    """
    T = None
    
    def __new__( cls, x, T=None ):
        x = (x if isinstance( x, Quantity ) else
             Quantity(x)).view(cls)
        x.T = T
        return x
    
    def coerce_inner_dims( x, y ):
        x, y = x.view(Structure), asanyarray(y)
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
            Z = ndarray( [o( cls(x), op, y ) for x, y in zip( X, Y )], 
                         dtype=object )
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
    

    
class Sample(Structure):
    """ Algebra aligned on innermost axis.

    Transposes to Parameter.
    
    >>> X = deeparray([[1, units.nm], 3])
    >>> X3 = Sample([1*X, 2*X, 3*X]); X3
    Sample([[[1 1.0 nm], 3],
            [[2 2.0 nm], 6],
            [[3 3.0 nm], 9]], dtype=object)
    >>> X3.T
    Parameter([[[1 2 3] [ 1.  2.  3.] nm], [3 6 9]], dtype=object)
    """

    def __new__( cls, a, T=None, dtype=None ):
        return (a if isinstance( a, Quantity ) else
             Quantity(a)).view(cls)
    
    @lazy_property
    def T(a):
        return deeparray( a, Parameter, True )


class deepvectorize(object):
    """ Convert function f(x) to evaluate non-uniform arrays.
    >>> import math
    >>> nan = float('nan')
    >>> isnan = deepvectorize(math.isnan)
    >>> X = deeparray([[1, nan], 3])
    >>> print isnan(X), isnan(nan)
    [[False True] False] True
    """
    
    def __init__(self, f, dims=0, dtype=float ):
        self.f, self.dims, self.dtype = f, dims, dtype
        self.__doc__ = "Deepvectorized " + f.__doc__
        
    def __call__(self, x):
        if isinstance( x, ndarray ) and len(x.shape) > self.dims: 
            return array( map( self, x ), dtype=ndarray )
        return self.f(x.astype(self.dtype) if self.dims else x)


def deeparray( a, cls=Sample, transpose=False, level=0 ):
    """ Similar to array(a) but creates irregular shapes as well.
    >>> deeparray(1)
    Sample(1)
    >>> deeparray((1, units.nm))
    Sample([1, 1 nm (nanometer)], dtype=object)
    >>> deeparray((units.m, units.nm))
    Sample([  1.00000000e+00,   1.00000000e-09]) * m
    >>> deeparray((1, units.nm, (2, 3)))
    Sample([1, 1 nm (nanometer), [2 3]], dtype=object)
    """
    try:
        if transpose:
            try: a = zip( * a )
            except: pass
        sub = level + 1
        A = [deeparray( x, cls, transpose, sub ) for x in a] # Make subarrays.
        S = map( shape, A ) # Gather subshapes.
        T = map( type, A ) # Gather subtypes.
        D = [asanyarray(a).dtype for a in A] # Gather subdtypes.
        s0, t0, d0 = S[0], T[0], D[0]
        if array([s != s0 for s in S[1:]]).any()\
        or array([t != t0 for t in T[1:]]).any()\
        or array([d != d0 for d in D[1:]]).any():
            a = empty( (len(A),), dtype='object' ) # Irregular shape.
            a[:] = A
        elif issubclass( t0, Quantity ):
            V, U = zip( * [(cls(ai.magnitude), ai.units) for ai in A] )
            u0 = U[0]
            try:
                # Compatible units.
                V1 = [v * u.rescale(u0).magnitude for v, u in zip( V, U )]
                if u0 != dimensionless:
                    a = V1 * u0
                else:
                    a = empty( (len(V),), dtype=d0 )
                    a[:] = V
            except:
                # Incompatible units.
                a = empty( (len(A),), dtype='object' )
                a[:] = A
        else:
            a = array( A, dtype=d0 )
    except:
        pass
    return cls(a) if level == 0 else a
    

if __name__ == '__main__':
    import doctest
    doctest.testmod()