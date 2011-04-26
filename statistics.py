#! Calculations with uncertainties
#!---------------------------------
#! Based on uncertainquantities.py from quantities-0.9.0.
#!
#! Changes:
#!
#! 1. UncertainQuantity: fixed code in __add__, __sub__, repeat and flatten.
#!
#! 2. Added ln, sqrt, oo, std, interval, independent, corners, collect, inverf,
#!    confidence.
#!
#! Fixed code
#!---------------
from __future__ import division
import sys
import numpy as np
from quantities import markup
from quantities.quantity import Quantity, scale_other_units
from quantities.registry import unit_registry
from quantities.decorators import with_doc
from quantities.units import dimensionless, pi
from numpy import array, zeros
from math import log as ln

class UncertainQuantity(Quantity):

    __array_priority__ = 22

    def __new__(cls, data, units='', uncertainty=None, dtype='d', copy=True):
        ret = Quantity.__new__(cls, data, units, dtype, copy)
        ret._uncertainty._dimensionality = ret._dimensionality
        if uncertainty is not None:
            ret.uncertainty = uncertainty
        elif isinstance(data, UncertainQuantity):
            if copy or self._dimensionality != uncertainty._dimensionality:
                uncertainty = data.uncertainty.rescale(ret.units)
            ret.uncertainty = uncertainty
        return ret

    def _set_units(self, units):
        super(UncertainQuantity, self)._set_units(units)
        self.uncertainty.units = self._dimensionality
    units = property(Quantity._get_units, _set_units)

    @property
    def _reference(self):
        ret = super(UncertainQuantity, self)._reference.view(UncertainQuantity)
        ret.uncertainty = self.uncertainty._reference
        return ret

    @property
    def simplified(self):
        ret = super(UncertainQuantity, self).simplified.view(UncertainQuantity)
        ret.uncertainty = self.uncertainty.simplified
        return ret

    def get_uncertainty(self):
        return self._uncertainty
    def set_uncertainty(self, uncertainty):
        if not isinstance(uncertainty, Quantity):
            uncertainty = Quantity(
                uncertainty, self._dimensionality, copy=False)
        try:
            assert self.shape == uncertainty.shape
        except AssertionError:
            raise ValueError('data and uncertainty must have identical shape')
        if uncertainty._dimensionality != self._dimensionality:
            uncertainty = uncertainty.rescale(self._dimensionality)
        self._uncertainty = uncertainty
    uncertainty = property(get_uncertainty, set_uncertainty)

    @property
    def relative_uncertainty(self):
        return self.uncertainty.magnitude/self.magnitude

    @with_doc(Quantity.rescale, use_header=False)
    def rescale(self, units):
        cls = UncertainQuantity
        ret = super(cls, self).rescale(units).view(cls)
        ret.uncertainty = self.uncertainty.rescale(units)
        return ret

    def __array_finalize__(self, obj):
        Quantity.__array_finalize__(self, obj)
        self._uncertainty = getattr(
            obj,
            'uncertainty',
            Quantity(
                np.zeros(self.shape, self.dtype),
                self._dimensionality,
                copy=False))

    @with_doc(Quantity.__add__, use_header=False)
    @scale_other_units
    def __add__(self, other):
        res = super(UncertainQuantity, self).__add__(other)
        #! Bugfix
        try:
            u = (self.uncertainty**2+other.uncertainty**2)**0.5
        except:
            u = self.uncertainty
        #!
        return UncertainQuantity(res, uncertainty=u, copy=False)

    @with_doc(Quantity.__radd__, use_header=False)
    @scale_other_units
    def __radd__(self, other):
        return self.__add__(other)

    @with_doc(Quantity.__sub__, use_header=False)
    @scale_other_units
    def __sub__(self, other):
        res = super(UncertainQuantity, self).__sub__(other)
        #! Bugfix
        try:
            u = (self.uncertainty**2+other.uncertainty**2)**0.5
        except:
            u = self.uncertainty
        #!
        return UncertainQuantity(res, uncertainty=u, copy=False)

    @with_doc(Quantity.__rsub__, use_header=False)
    @scale_other_units
    def __rsub__(self, other):
        #! Bugfix
        res = super(UncertainQuantity, self).__rsub__(other)
        try:
            u = (self.uncertainty**2+other.uncertainty**2)**0.5
        except:
            u = self.uncertainty
        return UncertainQuantity(res, uncertainty=u, copy=False)
        #!

    @with_doc(Quantity.__mul__, use_header=False)
    def __mul__(self, other):
        res = super(UncertainQuantity, self).__mul__(other)
        try:
            sru = self.relative_uncertainty
            oru = other.relative_uncertainty
            ru = (sru**2+oru**2)**0.5
            u = res.view(Quantity) * ru
        except AttributeError:
            other = np.array(other, copy=False, subok=True)
            u = (self.uncertainty**2*other**2)**0.5

        res._uncertainty = u
        return res

    @with_doc(Quantity.__rmul__, use_header=False)
    def __rmul__(self, other):
        return self.__mul__(other)

    @with_doc(Quantity.__truediv__, use_header=False)
    def __truediv__(self, other):
        res = super(UncertainQuantity, self).__truediv__(other)
        try:
            sru = self.relative_uncertainty
            oru = other.relative_uncertainty
            ru = (sru**2+oru**2)**0.5
            u = res.view(Quantity) * ru
        except AttributeError:
            other = np.array(other, copy=False, subok=True)
            u = (self.uncertainty**2/other**2)**0.5

        res._uncertainty = u
        return res

    @with_doc(Quantity.__rtruediv__, use_header=False)
    def __rtruediv__(self, other):
        temp = UncertainQuantity(
            1/self.magnitude, self.dimensionality**-1,
            self.relative_uncertainty/self.magnitude, copy=False)
        return other * temp

    if sys.version_info[0] < 3:
        __div__ = __truediv__
        __rdiv__ = __rtruediv__

    @with_doc(Quantity.__pow__, use_header=False)
    def __pow__(self, other):
        res = super(UncertainQuantity, self).__pow__(other)
        res.uncertainty = res.view(Quantity) * other * self.relative_uncertainty
        return res

    @with_doc(Quantity.__getitem__, use_header=False)
    def __getitem__(self, key):
        return UncertainQuantity(
            self.magnitude[key],
            self._dimensionality,
            self.uncertainty[key],
            copy=False)

    @with_doc(Quantity.__repr__, use_header=False)
    def __repr__(self):
        return '%s(%s, %s, %s)'%(
            self.__class__.__name__,
            repr(self.magnitude),
            self.dimensionality.string,
            repr(self.uncertainty.magnitude))

    @with_doc(Quantity.__str__, use_header=False)
    def __str__(self):
        if markup.config.use_unicode:
            dims = self.dimensionality.unicode
        else:
            dims = self.dimensionality.string
        s = '%s %s\n+/-%s (1 sigma)'%(
            str(self.magnitude),
            dims,
            str(self.uncertainty))
        if markup.config.use_unicode:
            return s.replace('+/-', '\xb1').replace(' sigma', '\u03c3')
        return s

    @with_doc(np.ndarray.sum)
    def sum(self, axis=None, dtype=None, out=None):
        return UncertainQuantity(
            self.magnitude.sum(axis, dtype, out),
            self.dimensionality,
            (np.sum(self.uncertainty.magnitude**2, axis))**0.5,
            copy=False)

    @with_doc(np.ndarray.repeat)
    def repeat( self, repeats, axis = None):
        x = super(UncertainQuantity, self).repeat( repeats, axis)
        #! Bugfix
        x.uncertainty.shape = self.shape
        x.uncertainty = x.uncertainty.repeat( repeats, axis)
        #!
        return x

    @with_doc(np.ndarray.flatten)
    def flatten( self ):
        x = super(UncertainQuantity, self).flatten()
        #! Bugfix
        x.uncertainty.shape = x.shape
        #!
        return x

    def __getstate__(self):
        """
        Return the internal state of the quantity, for pickling
        purposes.

        """
        state = list(super(UncertainQuantity, self).__getstate__())
        state.append(self._uncertainty)
        return tuple(state)

    def __setstate__(self, state):
        (ver, shp, typ, isf, raw, units, sigma) = state
        np.ndarray.__setstate__(self, (shp, typ, isf, raw))
        self._dimensionality = units
        self._uncertainty = sigma

UncertainQuantity = UncertainQuantity

#! Added functions and constants
#! ------------------------------
sqrt = lambda x: x**0.5
oo = float('inf')
oo_ = 1e308

def std( x0 = 0, sigma = 0, unit = dimensionless):
    return UncertainQuantity( x0, unit, sigma)

def interval( min = -oo, max = oo, unit = dimensionless):
    return Quantity( [min, max], unit)

def independent(* quantities):
    """
    Treat all samples as independent.

    >>> x = Quantity([10, 20])
    >>> y = UncertainQuantity([1, 2], dimensionless, [1, 2])
    >>> z = Quantity(100)
    >>> print x/y + z
    [ 110.  110.] dimensionless
    +/-[ 10.  10.] dimensionless (1 sigma)
    >>> x, y, z = independent(x, y, z)
    >>> print x/y + z
    [ 110.  120.  105.  110.] dimensionless
    +/-[ 10.  20.   5.  10.] dimensionless (1 sigma)
    >>> 
    """
    N = 1
    quantities = list(quantities)
    for i, x in enumerate(quantities):
        n = x.size
        quantities[i] = x.repeat(N)
        N *= n
    for i, x in enumerate(quantities):
        n = x.size
        x.shape = (1, n)
        x = x.repeat( int(N/n), 0)
        quantities[i] = x.flatten()
    return quantities

def corners( * quantities ):
    """
    Iterate over all corners.

    >>> x = interval( 10, 20 )
    >>> y = interval( 1, 2 )
    >>> array( list( corners( x, y )))
    array([[10,  1],
           [20,  2]])
    >>> for xi, yi in corners( * independent( x, y )): print (xi, yi)
    (array(10) * dimensionless, array(1) * dimensionless)
    (array(20) * dimensionless, array(1) * dimensionless)
    (array(10) * dimensionless, array(2) * dimensionless)
    (array(20) * dimensionless, array(2) * dimensionless)
    """
    x = array([q.magnitude for q in quantities])
    u = [q.units for q in quantities]
    z = zeros(x[0].size)
    d = array([q.uncertainty if isinstance( q, UncertainQuantity ) else z
               for q in quantities])
    for xi, di in zip(x.T, d.T):
        yield [UncertainQuantity( xij, uj, dij ) if dij != 0
               else Quantity( xij, uj )
               for xij, uj, dij in zip( xi, u, di )]
    
def collect( * quantity ):
    """
    >>> x = [std(i, i/10) for i in range(2)]; x
    [UncertainQuantity(array(0.0), dimensionless, array(0.0)), UncertainQuantity(array(1.0), dimensionless, array(0.1))]
    >>> x = collect(* x); x
    UncertainQuantity(array([ 0.,  1.]), dimensionless, array([ 0. ,  0.1]))
    >>> collect(*[x+i for i in range(2)])
    UncertainQuantity(array([[ 0.,  1.],
           [ 1.,  2.]]), dimensionless, array([[ 0. ,  0.1],
           [ 0. ,  0.1]]))
    """
    u = quantity[0].units
    z = zeros(quantity[0].shape)
    q = [qi.rescale(u) for qi in quantity]
    x = array([qi.magnitude for qi in q])
    try:
        d = array([qi.uncertainty if isinstance( qi, UncertainQuantity ) else z
                   for qi in q])
    except ValueError:
        raise ValueError(q, z)
    if (d != 0).any(): return UncertainQuantity( x, u, d )
    return Quantity( x, u )

def inverf(x):
    "http://homepages.physik.uni-muenchen.de/~Winitzki/erf-approx.pdf"
    a = 8*(pi-3)/(3*pi*(4-pi))
    return sqrt(sqrt((2/(pi*a)+ln(1-x**2)/2)**2-ln(1-x**2)/a) \
                        -(2/(pi*a)+ln(1-x**2)/2))

def confidence( x, c):
    try:
        x0 = x.magnitude*x.units
        dx = inverf(c)*x.uncertainty
        return collect(* [x0-dx, x0+dx])
    except: return x

if __name__ == '__main__':
    import doctest
    doctest.testmod()
