#https://github.com/joakimbits/Quflow-and-Perfeco-tools/blob/master/statistics.py
"""
Statistics for uncertainties in physical quantities

Based on uncertainquantities.py from quantities-0.9.0.

Changes:

 1. UncertainQuantity: fixed bugs in __add__, __sub__, repeat and flatten.

 2. Added sqrt, ln, oo, oo_, norm, invnorm, erf, inverf, probability, score,
    confidence, std, interval, independent, corners, collect
"""

from __future__ import division
from quantities import Quantity, UncertainQuantity
from quantities.units import dimensionless, pi
from numpy import array, ndarray, zeros
from collections import Iterable
from math import erf, log

'''
import sys
import numpy as np
from quantities import markup
from quantities.quantity import scale_other_units
from quantities.registry import unit_registry
from quantities.decorators import with_doc

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
'''


#! Added functions and constants
#! ------------------------------
sqrt = lambda x: x**0.5
ln = log
oo = float('inf')
oo_ = 1e308

norm = lambda x: 0.5*(1 + erf(x/sqrt(2)))

def invnorm(p):
    """"
    http://home.online.no/~pjacklam/notes/invnorm/#Python
    >>> norm(3)
    0.9986501019683699
    >>> invnorm(_)
    2.9999999995780815
    """
    assert 0 < p < 1, p
    a = (-3.969683028665376e+01,  2.209460984245205e+02, \
         -2.759285104469687e+02,  1.383577518672690e+02, \
         -3.066479806614716e+01,  2.506628277459239e+00)
    b = (-5.447609879822406e+01,  1.615858368580409e+02, \
         -1.556989798598866e+02,  6.680131188771972e+01, \
         -1.328068155288572e+01 )
    c = (-7.784894002430293e-03, -3.223964580411365e-01, \
         -2.400758277161838e+00, -2.549732539343734e+00, \
          4.374664141464968e+00,  2.938163982698783e+00)
    d = ( 7.784695709041462e-03,  3.224671290700398e-01, \
          2.445134137142996e+00,  3.754408661907416e+00)
    plow  = 0.02425
    phigh = 1 - plow
    if p < plow:
       q  = sqrt(-2*ln(p))
       return (((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5]) / \
               ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1)
    if phigh < p:
       q  = sqrt(-2*ln(1-p))
       return -(((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5]) / \
                ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1)
    q = p - 0.5
    r = q*q
    return (((((a[0]*r+a[1])*r+a[2])*r+a[3])*r+a[4])*r+a[5])*q / \
           (((((b[0]*r+b[1])*r+b[2])*r+b[3])*r+b[4])*r+1)

def inverf(p):
    """
    >>> 3/sqrt(2)
    2.1213203435596424
    >>> erf(_)
    0.9973002039367398
    >>> inverf(_)
    2.121320343261301
    """
    return invnorm((p+1)/2)/sqrt(2) 
    
def probability(z):
    """
    Calculates the probability p for a standard score z. This is the probability
    to find a random variable x with normal distribution
    
        dp/dx = exp(-x^2/2)/sqrt(2 pi)
    
    within the interval [-z, z].

    >>> probability(3)
    0.9973002039367398
    """
    return erf(z/sqrt(2))

def score(p):
    """
    Calculates the standard score z for a probability p. This defines the
    confidence interval [-z, z] where the probability is p to find a random
    variable x with normal distribution
    
        dp/dx = exp(-x^2/2)/sqrt(2 pi).

    >>> score(0.9973)
    2.9999769922817587
    """
    return sqrt(2)*inverf(p)

def confidence( x, p ):
    """
    Calculates the forecast interval [x0, x1] where the probability is exactly
    p to find x and the span x1-x0 is minimized.

    Assumption: x in an UncertainQuantity representing a random variable with
    normal distribution
    
        dp/dx = exp(-((x-mu)/sigma)^2/2)/(sigma sqrt(2 pi)),
    
    where mu = x.magnitude and sigma = x.uncertainty.

    On forecasting - http://www.jstor.org/stable/pdfplus/1391361.pdf

    >>> confidence( std( 0, 1 ), 0.9973 )
    array([-2.99997699,  2.99997699]) * dimensionless
    """
    x0 = x.magnitude*x.units
    dx = score(p)*x.uncertainty
    return collect(* [x0-dx, x0+dx])

def std( x0 = 0, sigma = 1, unit = dimensionless):
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
    if len(quantities) == 1: quantities = quantities[0]
    N = 1
    quantities = list(quantities)
    for i, x in enumerate(quantities):
        try: 
            n = x.size
            if n > 1:
                quantities[i] = x.repeat(N)
                N *= n
        except AttributeError:
            pass
    for i, x in enumerate(quantities):
        try:
            n = x.size
            if n > 1:
                x.shape = (1, n)
                x = x.repeat( int(N/n), 0)
                quantities[i] = x.flatten()
        except AttributeError:
            pass
    return quantities

def corners( * quantities ):
    """
    Iterate over all quantity corners.

    Simplifies to Quantity, array, float and int when possible.

    >>> x = interval( 10, 20 )
    >>> y = interval( 1, 2 )
    >>> array(list(corners( x, y )))
    array([[10,  1],
           [20,  2]])
    >>> array(list(corners(* independent( x, y ))))
    array([[10,  1],
           [20,  1],
           [10,  2],
           [20,  2]])
    >>> for x, y, z in corners( 1.0, 2, [3.14,  4] ): print x, y, z
    1 2 3.14
    1 2 4
    """
    if len(quantities) == 1: quantities = quantities[0]
    N = 1
    for q in quantities:
        try:
            N = len(q)
            if N > 1: break
        except: pass
    x = array([(q if (isinstance( q, Iterable )
                      and (q.size if isinstance( q, ndarray )
                           else len(q)) != 1)
                else array([q.magnitude if isinstance( q, Quantity )
                            else q]*N))
               for q in quantities])
    u = [q.units if isinstance( q, Quantity ) else dimensionless
         for q in quantities]
    z = zeros(N)
    d = array([q.uncertainty if isinstance( q, UncertainQuantity ) else z
               for q in quantities])
    for xi, di in zip(x.T, d.T):
        yield [UncertainQuantity( xij, uj, dij ) if dij != 0
               else Quantity( xij, uj ) if not uj == dimensionless
               else array( xij ) if xij.size != 1
               else int(xij) if xij == int(xij)
               else float(xij) if xij == float(xij)
               else xij.astype(xij.dtype) for xij, uj, dij in zip( xi, u, di )]
    
def collect( * quantity ):
    """
    >>> x = [std(i, i/10) for i in range(2)]; x
    [UncertainQuantity(array(0.0), dimensionless, array(0.0)), UncertainQuantity(array(1.0), dimensionless, array(0.1))]
    >>> x = collect(* x); x
    UncertainQuantity(array([ 0.,  1.]), dimensionless, array([ 0. ,  0.1]))
    >>> collect(* [x+i for i in range(2)])
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


if __name__ == '__main__':
    import doctest
    doctest.testmod()
