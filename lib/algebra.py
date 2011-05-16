#https://github.com/joakimbits/Quflow-and-Perfeco-tools/lib/algebra.py
"""
Algebra with symbols, physical constants, arrays and uncertainties
"""

from re import compile
from sympy import Basic, Symbol, Matrix, sympify, lambdify, solve, \
     Sum, integrate
from sympy import sum as summation
from collections import Iterable
from quantities import Quantity, units, constants
from numpy import ndarray, array
from quantities.unitquantity import \
     UnitCurrency, UnitCurrent, UnitInformation, UnitLength, \
     UnitLuminousIntensity, UnitMass, UnitSubstance, UnitTemperature, \
     UnitTime
from statistics import UncertainQuantity


class BaseUnits(dict):
    dimensions = (UnitCurrent, UnitInformation, UnitLength,
                  UnitLuminousIntensity, UnitMass, UnitSubstance,
                  UnitTemperature, UnitTime)
    def __call__( self, ** unit ):
        units.set_default_units( ** unit )
        self.update([('_'+ u.symbol, u)
                     for u in [d._default_unit for d in self.dimensions]])

set_default_units = base_units = BaseUnits()
set_default_units()

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
    
    def __new__( cls, quantity, symbol = None ):
        self = Basic.__new__( cls, quantity )
        try:
            self.symbol = symbol or quantity.symbol
        except:
            self.symbol = symbol
        I = self.implicit = dict()
        units = set()
        q = quantity.simplified
        k = 1
        for u, d in q.dimensionality.iteritems():
            unit = '_'+ u.symbol
            k *= Symbol(unit)**d
            units.add(unit)
        if q.shape or isinstance(q, UncertainQuantity):
            n_ = self.symbol +'_'
            I[n_] = q/q.units
            self.result = Symbol(n_) * k
        else:
            self.result = q.magnitude * k
        I.update(dictmap( units, base_units.get ))
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
    array(0.8010882220000002) * eV
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
        parameters = setup + (operation,)
        self = Basic.__new__( cls, model, * parameters )
        # Collect parameter values
        environment = dict()
        for s in parameters:
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
                        v = v(E) if isinstance( v, System ) else System( v, E )
                        A.update(v.applied)
                        A[n] = v.result
                        I.update(v.implicit)
                        R.update(v.remaining)
                    except:
                        pass
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
        return self

    def __call__( self, * setup, ** operation ):
        return System( self.model, self.environment, self.implicit,
                       * setup, ** operation )
    
    def __getitem__( self, k ):
        return self.result.__getitem__(k)

    def rescale( self, unit ):
        if isinstance( self.result, Quantity ): return self.result.rescale(unit)
        else: return SymQuantity.rescale( self, unit )

    def __getattribute__( self, name, default=None ):
        if name in System.__dict__:
            return object.__getattribute__( self, name )
        if name in self.environment: return self.environment[name]
        if name in self.remaining: 
            return System( solve( self.result, Symbol(name) ),
                           self.environment, self.implicit )
        return SymQuantity.__getattribute__( self, name, default )
        
if __name__ == '__main__':
    import doctest
    doctest.testmod()
