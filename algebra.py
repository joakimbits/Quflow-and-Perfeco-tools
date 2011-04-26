# Requires bugfix for issue 1889 in sympy.core.ast_parser.py:64
#         return eval(e, global_dict, local_dict)
from re import compile
from sympy import Basic, Symbol, Matrix, sympify, lambdify, solve, Mul
from collections import Iterable
from quantities import Quantity
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
pythonified = lambda expr: _Eq( r'\1-(\2)', _Mul( r'\1*', expr ))
dictmap = lambda l, f: dict(zip( l, map( f, l )))
params = lambda dict: ', '.join(['%s=%r' %p for p in dict.iteritems()])


_skip = dictmap("COSINEQ", Symbol) # Skip one-letter definitions in sympy

def _systemize(s):
    if isinstance( s, Basic ): yield s
    elif isinstance( s, str ): yield sympify( pythonified(s), _skip.copy() )
    elif isinstance( s, (Iterable, Matrix) ):
        for ss in s:
            for e in _systemize(ss): yield e
    else: raise TypeError('No system: %r'%s)

def systemize(s):
    "Basic equations of a source system"
    return tuple(_systemize(s))


def _system_parameters(s):
    if isinstance( s, (Iterable, Matrix) ):
        for ss in s:
            for item in _system_parameters(ss): yield item
    else:
        for a in s.atoms(Symbol): yield (a.name, a)

def system_parameters(s):
    "Parameters of a system"
    return dict(_system_parameters(s))

debug=False
class System( Basic ):
    """
    A utility for defining and using mathematical expressions.
    
    String expressions are pythonified before evaluation. This inserts * in
    implicit multiplications, replaces ^ with ** and replaces ==<rhs> with
    -(<rhs>).

    Symbols are solved for when used as an attribute.

    Expressions are simplified when dictionaries, modules, objects or keyword
    arguments are supplied, during creation or when called. Since expressions
    are managed in sympy which has poor interoperability with quantities and
    arrays, any such data is temporarily converted to symbols until all
    parameters are defined.
    
    Example 1: Solving for parameters in an expression
    >>> charging = System('E == (n - C V/e)^2 e^2/(2 C)'); charging.E
    -V*e*n + 0.5*C*V**2 + 0.5*e**2*n**2/C
    >>> E = charging.E(V=0); E
    0.5*e**2*n**2/C

    Example 2: Using physical constants, uncertainties and arrays
    >>> from quantities import constants, F, eV
    >>> from statistics import std, confidence
    >>> from numpy import array
    >>> dE = System( E(n=1) - E(n=0), constants); dE #showing temporary symbols
    1.28348474774783e-38*_A**2*_s**2/C /. _s=UnitTime('second', 's'), _A=UnitCurrent('ampere', 'A')
    >>> confidence( dE(C = std(2,1)*1e-18*F).rescale(eV), 0.99)
    array([ 0.00367171,  0.07643711]) * eV
    >>> dE(C = array([1,2,3])*1e-18*F).rescale(eV)
    array([ 0.08010882,  0.04005441,  0.02670294]) * eV
    """
    
    # object data - names are defined here to make __getattribute__ easier.
    source_system       = None # source equations
    source_parameters   = None # source parameter values
    system              = None # compiled equations
    defined             = None # used parameters values
    implicit            = None # stuff that can mess upp sympy
    default             = None # remaning symbols and messy stuff
    subsystem           = None # simplified equations
    
    def __new__( cls, * scopes, ** scope ):
        # Collect source and parameters
        source_system = list() # str, list or Basic
        source_parameters = dict() # anything else
        for s in scopes + (scope,):
            if isinstance( s, (Matrix, Basic, str, list) ):
                source_system.append(s)
            elif isinstance( s, dict ): source_parameters.update(s)
            else: source_parameters.update(s.__dict__)
        self = Basic.__new__( cls, source_system, source_parameters )
        # Build symbolic expressions and parameters
        system = systemize(source_system)
        parameters = system_parameters(system)
        defined   = dictmap( set(parameters).intersection(source_parameters),
                             source_parameters.get )
        if debug:
            print 'System:'
            print source_system
            print '%r /. %s' % (system, params(defined))
        # Work around objects that sympify can't handle. Can be fixed by
        # improving the interoperability between sympy, numpy and quantity. 
        quantities = dict([(n, v) for n, v in defined.iteritems()
                           if isinstance( v, Quantity ) and not n[0]=='_'])
        implicit = dict([
            (n, v) for n, v in defined.iteritems()
            if isinstance( v, Iterable ) and not n in quantities])
        welldefined = dictmap(
            set(defined).difference(quantities).difference(implicit),
            defined.get )
        undefined = dictmap( set(parameters).difference(defined),
                             parameters.get )
        if quantities:
            # Collect all base units
            qitem = lambda q: ('_'+ q.symbol, q)
            base_units = dict([qitem(d._default_unit) for d in dimensions])
            # Expand quantities to base units and replace those with symbols
            units = set()
            for n, q in quantities.iteritems():
                q = q.simplified
                k = 1
                for u, d in q.dimensionality.iteritems():
                    unit = '_'+ u.symbol
                    k *= Symbol(unit)**d
                    units.add(unit)
                if q.shape or isinstance(q, UncertainQuantity):
                    n_ = n +'_'
                    implicit[n_] = q/q.units
                    welldefined[n] = Symbol(n_) * k
                else:
                    welldefined[n] = q.magnitude * k
            implicit.update(dictmap( units, base_units.get ))
        # Simplify system into subsystem using parameters 
        try:
            subsystem = Matrix(system).subs(welldefined).evalf()
        except TypeError, err:
            raise TypeError('%s /. %s: %s' % (system, params(welldefined), err))
        self.source_system = source_system
        self.source_parameters = source_parameters
        self.system = system
        self.defined = defined
        self.implicit = implicit
        if undefined:
            if subsystem.shape == (1,1): subsystem = subsystem[0,0]
            self.default = undefined
            self.default.update(implicit)
            self.subsystem = subsystem
        else:
            if subsystem.shape == (1,1): subsystem = subsystem[0,0]
            else: subsystem = list(subsystem)
            if debug:
                print 'Solution:'
                print '%r /. %s' % (system, params(defined))
                print '%r /. %s' % (params(welldefined), params(implicit))
                print '%r /. %s' % (subsystem, params(implicit))
            self.default = dict()
            try:
                y = lambdify( implicit, subsystem )(* implicit.values())
            except TypeError, err:
                raise TypeError('%s /. %s: %s' % (subsystem, params(implicit), err))
            self.subsystem = y
        return self

    def __call__( self, * scopes, ** scope ):
        return System( self.subsystem, self.source_parameters, self.implicit,
                       * scopes, ** scope )
        
    def __getattribute__( self, name, default=None ):
        if name in System.__dict__:
            return object.__getattribute__( self, name )
        if name in self.source_parameters: return self.source_parameters[name]
        if name in self.default:
            if isinstance(self.default[name], Symbol):
                "Use case: Solving a parameter in the expression"
                return System( solve( self.subsystem, Symbol(name) ),
                               self.source_parameters, self.implicit )
            else: return self.default[name]
        subsystem = self.subsystem
        if default==None:
            try:
                return getattr( subsystem, name )
            except AttributeError:
                raise AttributeError("%r has no attribute '%s'."%( self, name ))
        return getattr( subsystem, name, default )
    
    def __str__(self):
        return str(self.source_system)
    
    def __repr__(self):
        if self.implicit and self.default:
            return '%r /. %s' % (self.subsystem, params(self.implicit))
        return repr(self.subsystem)


if __name__ == '__main__':
    import doctest
    doctest.testmod()
