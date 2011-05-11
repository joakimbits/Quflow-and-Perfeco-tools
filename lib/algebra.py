#https://github.com/joakimbits/Quflow-and-Perfeco-tools/lib/algebra.py
"""
Algebra with symbols, physical constants, arrays and uncertainties
"""

from re import compile
from sympy import Basic, Symbol, Matrix, sympify, lambdify, solve
from collections import Iterable
from quantities import Quantity
from numpy import ndarray, array
from quantities.unitquantity import \
     UnitCurrency, UnitCurrent, UnitInformation, UnitLength, \
     UnitLuminousIntensity, UnitMass, UnitSubstance, UnitTemperature, \
     UnitTime
from statistics import UncertainQuantity

dimensions = (UnitCurrent, UnitInformation, UnitLength,
              UnitLuminousIntensity, UnitMass, UnitSubstance,
              UnitTemperature, UnitTime)
_Mul = compile(r'([A-Za-z0-9_)])(\s+)(?=[A-Za-z0-9_(])').sub
_Eq = compile(r'^([^=]*)==(.*)').sub
pythonify = lambda expr: _Eq( r'\1-(\2)', _Mul( r'\1*', expr ))
dictmap = lambda l, f: dict(zip( l, map( f, l )))
params = lambda dict: ', '.join(['%s=%r' %p for p in dict.iteritems()])

class System( Basic ):
    """
    Associates attributes with any object.

    Attributes can be supplied as dictionaries, modules, objects or keyword
    arguments during the association or when called.

    If the object is a string, it is assumed to contain a mathematical
    expression. It is then pythonified before evaluation: inserts * in
    implicit multiplications, replaces ^ with ** and replaces ==<rhs> with
    -(<rhs>).

    An expression is evaluated as far as possible using the supplied attributes
    as substitutions. Remaining symbols can be solved for by using the symbol as
    an attribute. 

    Note: Expression are managed in sympy which has poor interoperability with
    quantities and arrays. Any such data is terefore temporarily converted to
    implicit symbols until all parameters of the expression is defined. These
    are stored in a special attribute 'implicit'.
    
    Example 1: Solving for parameters in an expression.
    >>> charging = System('E == (n - C V/e)^2 e^2/(2 C)'); charging.E
    (-2*C*V*e*n + C**2*V**2 + e**2*n**2)/(2*C)
    >>> E = charging.E(V=0); E
    0.5*e**2*n**2/C
    >>> from quantities import constants, F, eV
    >>> En = E( constants, aF=1E-18*F, C = System('n*0.1 aF')); En.rescale(eV)
    0.801088222000001*eV*n
    >>> En(n = 1).rescale(eV)
    array(0.8010882220000002) * eV
    """
    
    # Object data - defined here to make __getattribute__ easier.
    model       = None # Input sympy equation or numpy array.
    environment = None # Dictionary of parameter values.
    applied     = None # Substitutions made.
    result      = None # Resulting sympy equation or numpy array.
    implicit    = None # Hidden arrays and units in resulting equation.
    remaining   = None # Remaining symbols in resulting equation.
    
    _skip = dictmap("COSINEQ", Symbol) # Override these in sympy

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
        if A:
            # Evaluate all sub-expressions.
            for n, v in A.copy().iteritems():
                if isinstance( v, Basic ) and not v == Symbol(n) \
                   and v.atoms(Symbol):
                    v = v(E) if isinstance( v, System ) else System( v, E )
                    A.update(v.applied)
                    A[n] = v.result
                    I.update(v.implicit)
                    R.update(v.remaining)
            # Workaround objects that sympy can't handle. Can be fixed
            # by improving the interoperability between sympy and numpy.
            Q = dict([(n, v) for n, v in A.iteritems()
                      if isinstance( v, Quantity ) and not n[0]=='_'])
            I.update([(n, v) for n, v in A.iteritems()
                      if isinstance( v, Iterable ) and not n in Q])
            OK = dictmap( set(A).difference(Q).difference(I), A.get )
            # A = OK + I + Q now.
            if Q:
                # Collect all base units
                base_units = dict([('_'+ u.symbol, u)
                                   for u in [d._default_unit
                                             for d in dimensions]])
                # Expand quantities to base units and replace those with symbols
                units = set()
                for n, q in Q.iteritems():
                    q = q.simplified
                    k = 1
                    for u, d in q.dimensionality.iteritems():
                        unit = '_'+ u.symbol
                        k *= Symbol(unit)**d
                        units.add(unit)
                    if q.shape or isinstance(q, UncertainQuantity):
                        n_ = n +'_'
                        I[n_] = q/q.units
                        OK[n] = Symbol(n_) * k
                    else:
                        OK[n] = q.magnitude * k
                I.update(dictmap( units, base_units.get ))
            # End of workaround. A = OK + I now.
            try:
                result = M.subs(OK).evalf()
            except Exception, err:
                raise Exception('%s /. %s: %s'% (M, params(OK), err))
            if not R and isinstance( result, Basic ):
                # Only implicit parameters left - ok to evaluate numerically. 
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
        if isinstance( self.result, Quantity ):
            return self.result.rescale(unit)
        else:
            symunits = System('x*u', u=unit)/Symbol('x')
            return self.result/symunits * Symbol(unit.symbol)       
        
    def __getattribute__( self, name, default=None ):
        if name in System.__dict__:
            return object.__getattribute__( self, name )
        if name in self.environment: return self.environment[name]
        if name in self.implicit: return self.implicit[name]
        if name in self.remaining: 
            return System( solve( self.result, Symbol(name) ), self.environment,
                           self.implicit )
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

    __str__ = __repr__

if __name__ == '__main__':
    import doctest
    doctest.testmod()
