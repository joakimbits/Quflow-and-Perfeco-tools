#https://github.com/joakimbits/Quflow-and-Perfeco-tools/lib/algebra.py
"""
Algebra with symbols, physical constants, arrays and uncertainties
"""

from re import compile
from sympy import Basic, Symbol, Matrix, sympify, lambdify, solve, \
     Sum, integrate
from sympy import sum as summation
from collections import Iterable
from quantities import Quantity, UnitQuantity, units, constants
from numpy import ndarray, array
from quantities.unitquantity import \
     UnitCurrency, UnitCurrent, UnitInformation, UnitLength, \
     UnitLuminousIntensity, UnitMass, UnitSubstance, UnitTemperature, \
     UnitTime
from statistics import UncertainQuantity


class SymUnits(dict):
    """
    Conversion table from symbolic to numeric units, with support for default
    units and formats.

    >>> from quantities import nm, m, F, UnitQuantity
    >>> aF = UnitQuantity( 'attofarad', 1e-18*F, symbol = 'aF' )
    >>> default.set_units_and_formats(nm, aF, aF/nm, '%.2f')
    >>> (10*aF).simplified
    array(1.0000000000000003e-35) * s**4*A**2/(kg*nm**2)
    >>> default.rescale(_)
    array(10.0) * aF
    >>> print default.format(_), default.symquantity(_.units)
    10.00 aF
    """

    _basis = dict([     # basis for dimensions
        (d._default_unit._reference.dimensionality, d)
        for d in (UnitCurrent, UnitInformation, UnitLength,
                  UnitLuminousIntensity, UnitMass, UnitSubstance,
                  UnitTemperature, UnitTime)])
    _units = dict()     # units for dimensions
    _patterns = dict()    # formats for units

    def __init__( self, * units_and_patterns ):
        B = self._basis
        U = self._units
        P = self._patterns
        units_to_format = list()
        for u_or_p in units_and_patterns:
            if isinstance( u_or_p, str ) and '%' in u_or_p:
                p = u_or_p
                for u in units_to_format: P[u] = p
                units_to_format = list()
            else:
                u = u_or_p
                dims = u._reference.dimensionality
                if dims in B: B[dims].set_default_unit(u)
                U[dims] = u
                units_to_format.append(u)
        for dim, d in B.iteritems():
            u = d._default_unit
            self['_'+ u.symbol] = U[dim] = u
            
    set_units = set_units_and_formats = __init__

    def units( self, quantity ):
        if not isinstance( quantity, Quantity ):
            quantity = Quantity(quantity)
        u = quantity.units
        return self._units.get( u._reference.dimensionality, u.simplified )

    def rescale( self, quantity ):
        if isinstance( quantity, Quantity ):
            return quantity.rescale(self.units(quantity))
        return quantity

    def symquantity( self, quantity ):
        return SymQuantity( self.rescale(quantity),
                            simplified = False, _ = False )

    def pattern( self, quantity ):
        return self._patterns.get( self.units(quantity), '%g' )

    def format( self, quantity ):
        q = self.rescale(quantity)
        f = self.pattern(q)
        try: return f% q
        except: return [f% qi for qi in q]

default = SymUnits()
default.set_units(UnitQuantity( 'scalar', 1, symbol = '#' ))

_Mul = compile(r'([A-Za-z0-9_)])(\s+)(?=[A-Za-z0-9_(])').sub
_Eq = compile(r'^([^=]*)==(.*)').sub
pythonify = lambda expr: _Eq( r'\1-(\2)', _Mul( r'\1*', expr ))
dictmap = lambda l, f: dict(zip( l, map( f, l )))
params = lambda dict: ', '.join(['%s=%r' %p for p in dict.iteritems()])


class SymQuantity( Basic ):
    """
    Converts a physical quantity to a symbolic (sympy) expression.

    Symbolic units have the same name as in the current base units in the
    quantities package, but starting with '_'. The reason for this convention
    is to avoid conflicts with properties in expressions.
    
    Note: Expression are managed in sympy which has poor interoperability with
    arrays. Any such data is therefore converted to implicit symbols and stored
    in a special attribute 'implicit'. The numeric version of the applicable
    base units are stored there as well.
    
    >>> from quantities.constants import e
    >>> q = SymQuantity(-e); C = Symbol('C'); E = q**2/(2*C); E
    1.28348474774783e-38*_A**2*_s**2/C
    >>> q.rescale(e)
    -1.0*e
    """

    # Object data
    symbol      = None # Used for naming of data arrays.
    result      = None # Symbolic expression in base units.
    implicit    = None # Applicable base units and data arrays.
    
    def __new__( cls, quantity, symbol = None, simplified = True, _ = True ):
        self = Basic.__new__( cls, quantity )
        try:
            self.symbol = symbol or quantity.symbol
        except:
            self.symbol = symbol
        I = self.implicit = dict()
        units = set()
        q = quantity.simplified if simplified else quantity
        k = 1
        for u, d in q.dimensionality.iteritems():
            unit = '_'+ u.symbol if _ else u.symbol
            k *= Symbol(unit)**d
            units.add(unit)
            default[unit] = u
        if q.shape or isinstance(q, UncertainQuantity):
            n_ = self.symbol +'_'
            I[n_] = q/q.units
            self.result = Symbol(n_) * k
        else:
            self.result = q.magnitude * k
        I.update(dictmap( units, default.get ))
        return self
    
    def rescale( self, unit ):
        try:
            s = Symbol(unit.symbol)
        except:
            k = 1
            for u, d in unit.dimensionality.iteritems():
                k *= Symbol('_'+ u.symbol)**d
            s = unit.magnitude * k
        return self.result/SymQuantity(unit) * s

    def __getattribute__( self, name, default=None ):
        if name in SymQuantity.__dict__:
            return object.__getattribute__( self, name )
        if name in self.implicit: return self.implicit[name]
        result = self.result
        if default==None:
            try:
                return getattr( result, name )
            except AttributeError:
                raise AttributeError("%r has no attribute '%s'."%( self, name ))
        return getattr( result, name, default )
    
    def __repr__(self):
        if self.implicit:
            return '%r /. %s' % (self.result, params(self.implicit))
        return repr(self.result)

    def __str__(self):
        return str(self.result)


class System( SymQuantity ):
    """
    Associates attributes with a symbolic expression or numeric object.

    Attributes can be supplied as dictionaries, modules, objects or keyword
    arguments during the association or when called.

    If the object is a string, it is assumed to contain a mathematical
    expression. It is then pythonified before symbolic evaluation: inserts * in
    implicit multiplications, replaces ^ with ** and replaces ==<rhs> with
    -(<rhs>).

    An expression is evaluated as far as possible using the supplied attributes
    as substitutions. Remaining symbols can be solved for by using the symbol as
    an attribute. 

    >>> eq = System('E == q (V - n q/C)'); eq.E
    (C*V*q - n*q**2)/C
    >>> integrate( _, Symbol('n') )
    V*n*q - n**2*q**2/(2*C)
    >>> _.diff(Symbol('n'))
    V*q - n*q**2/C
    >>> charging = System('E == (n - C V/e)^2 e^2/(2 C)'); charging.E
    (-2*C*V*e*n + C**2*V**2 + e**2*n**2)/(2*C)
    >>> E = charging.E(V=0); E
    0.5*e**2*n**2/C
    >>> from quantities import constants, F, eV
    >>> En = E( constants, C = System('n*0.1 aF'), aF=1E-18*F); En.rescale(eV)
    0.801088222000001*eV*n
    >>> En(n = 1).rescale(eV)
    array(0.8010882220000003) * eV
    """
    
    # Object data
    model       = None # Input sympy equation or numeric object.
    environment = None # Dictionary of attribute values.
    applied     = None # Substitutions made in input equation.
    result      = None # Resulting sympy equation or numeric object.
    implicit    = None # Hidden arrays and units in resulting equation.
    remaining   = None # Remaining symbols in resulting equation.
    
    # Class data
    _skip = dictmap("COSINEQ", Symbol) # skip these when evaluating in sympy

    def __new__( cls, model, * setup, ** operation ):
        return Basic.__new__( cls )

    def __init__( self, model, * setup, ** operation ):
        # Collect parameter values
        environment = dict()
        for s in setup + (operation,):
            if isinstance( s, dict ): environment.update(s)
            elif isinstance( s, System ): environment.update(s.environment)
            else: environment.update(s.__dict__)
        E = self.environment = environment
        M = self.model = (sympify( pythonify(model), self._skip.copy() )
                          if isinstance( model, str ) else
                          (model[0] if len(model) == 1 else Matrix(model))
                          if isinstance( model, list ) else model)
        S = symbols = (dict([(a.name, a) for a in M.atoms(Symbol)])
                       if isinstance( M, Basic ) else dict())
        # Find applicable substitutions and remaning undefined parameters.
        A = self.applied = dictmap( set(S).intersection(E), E.get )
        I = self.implicit = dict()
        R = self.remaining = dictmap( set(S).difference(A), S.get )
        # S = A + R now.
        # Use I for objects in A that sympy can't handle.
        if A:
            # Evaluate all sub-expressions. May expand A + I + R.
            for n, v in A.copy().iteritems():
                if isinstance( v, Basic ) and not isinstance( v, Symbol ):
                    try:
                        assert len(v.atoms(Symbol)) > 1
                    except:
                        continue
                    v = v(E) if isinstance( v, System ) else System( v, E )
                    A.update(v.applied)
                    A[n] = v.result
                    I.update(v.implicit)
                    R.update(v.remaining)
            # M /. E == M /. A now. A may contain sympy-incompatible objects.
            # Categorize A.
            Q = dict([(n, v) for n, v in A.iteritems()
                      if isinstance( v, Quantity ) and not n[0]=='_'])
            I.update([(n, v) for n, v in A.iteritems()
                      if isinstance( v, Iterable ) and not n in Q])
            OK = dictmap( set(A).difference(Q).difference(I), A.get )
            # M /. E == M /. (OK + I + Q) now. Only OK is sympy-compatible.
            # Eliminate Q by expanding OK and I.
            for n, q in Q.iteritems():
                q = SymQuantity( q, n )
                I.update(q.implicit)
                OK[n] = q.result
            # M /. E == M /. (OK + I) == (M /. OK) /. I now.
            # Evalute M /. OK symbolically.
            try:
                result = M.subs(OK).evalf()
            except Exception, err:
                raise Exception('%s /. %s: %s'% (M, params(OK), err))
            # M /. E == result /. I now.
            # If possible, evaluate result /. I numerically.
            if not R and isinstance( result, Basic ):
                try:
                    result = lambdify( I, result )(* I.values())
                except Exception, err:
                    raise Exception('%s /. %s: %s' % (result, params(I), err))
                self.implicit = dict()
            self.result = result
        else:
            self.result = M
            self.implicit = dict()

    def __call__( self, * setup, ** operation ):
        return type(self)( self.result, self.environment, self.implicit,
                               * setup, ** operation )
    
    def __instancecheck__( self, instance ):
        return object.__instancecheck__( self, instance ) or isinstance(
            self.result, instance )
    def __subclasscheck__( self, subclass ):
        return object.__subclasscheck__( self, subclass ) or issubclass(
            self.result, subclass )    
    def __getitem__( self, k ): return self.result[k]
    def __len__( self, k ): return len(self.result)
    def __float__(self): return float(self.result)
    
    def rescale( self, unit ):
        if isinstance( self.result, Quantity ): return self.result.rescale(unit)
        else: return SymQuantity.rescale( self, unit )

    def __getattribute__( self, name, default=None ):
        if name in System.__dict__:
            return object.__getattribute__( self, name )
        E, A, I, R, r = [object.__getattribute__( self, n ) for n in (
            'environment', 'applied', 'implicit', 'remaining', 'result')]
        if name in A: return System( A[name], E, A, I )
        if name in E: return System( E[name], E, A )
        if name in R: return System( solve( r, Symbol(name) ), E, A, I )
        return SymQuantity.__getattribute__( self, name, default )

    def __repr__(self):
        r = self.result
        if isinstance( r, ndarray ) and r.size > 1:
            N = len(r)
            V = dict([(n, v) for n, v in self.applied.iteritems()
                      if isinstance( v, ndarray ) and v.size > 1])
            head = ['', ''] + V.keys()
            data = map( default.rescale, [range(N), r] + V.values() )
            unit = [str(default.units(c)).split()[1] for c in data]
            data = array([map( default.format, c ) for c in data])
            table = array([head, unit] + list(data.T))
            W = map( max, [map( len, c ) for c in table.T])
            f = ' '.join(['%'+ str(w) +'s' for w in W])
            return '\n'.join([f% tuple(row) for row in table])
        else: return SymQuantity.__repr__(self)


class SortedSystem(System):
    """
    Sorts array result in increasing order, cut to a maximum 'size' if such an
    attribute is specified.

    Example: First spin-degenerate states in a flat nanoelectron loop.
    >>> from quantities.constants import e, m_e
    >>> from quantities import nm, eV, UnitQuantity
    >>> from statistics import independent
    >>> default.set_units_and_formats( eV, '%.4f' )
    >>> loop = System( 'Em + El', constants,
    ...     Em = System('(m + 0.5)^2 h^2/(8 M w^2)'),
    ...     El = System('((l - A B q/h)^2 + 1/4) h^2/(8 pi M A)'),
    ...     q = -e, M = m_e )
    >>> nanoloop = loop( w = 20*nm, A = 1000*nm**2, B = 0 )
    >>> m, l = independent( [0, 1], range( -4, 5 ) )
    >>> SortedSystem( loop.model, nanoloop, m=m, l=l, size = 10 )
                 Em     El m  l
    #     eV     eV     eV #  #
    0 0.0003 0.0002 0.0000 0  0
    1 0.0004 0.0002 0.0001 0 -1
    2 0.0004 0.0002 0.0001 0  1
    3 0.0007 0.0002 0.0005 0 -2
    4 0.0007 0.0002 0.0005 0  2
    5 0.0013 0.0002 0.0011 0 -3
    6 0.0013 0.0002 0.0011 0  3
    7 0.0021 0.0021 0.0000 1  0
    8 0.0022 0.0002 0.0019 0 -4
    9 0.0022 0.0002 0.0019 0  4
    """

    def __init__( self, model, * setup, ** operation ):
        System.__init__( self, model, * setup, ** operation )
        v = self.result
        if isinstance( v, ndarray ) and v.size > 1:
            order = v.argsort(kind = 'merge')
            try:
                order = order[:self.environment['size']]
            except:
                pass
            self.result = v[order]
            A = self.applied
            for n, v in A.copy().iteritems():
                if isinstance( v, ndarray ) and v.size > 1:
                    A[n] = v[order]


if __name__ == '__main__':
    import doctest
    doctest.testmod()
