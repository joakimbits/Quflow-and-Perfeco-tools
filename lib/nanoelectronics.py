#https://github.com/joakimbits/Quflow-and-Perfeco-tools/lib/nanoelectronics.py
"""
Single-particle properties for nanometer-sized electronic circuits
"""

from __future__ import division
from quantities import constants, UnitQuantity, Quantity
from sympy import Symbol
from algebra import System, set_default_units
from caching import Cache
from statistics import UncertainQuantity, \
     std, interval, confidence, independent, corners, collect, \
     oo, oo_, sqrt, ln, probability, score
from numpy import array, diff, argsort, searchsorted
from matplotlib import pyplot
from enthought.traits.api import \
     HasTraits, HasStrictTraits, on_trait_change, \
     Trait, Instance, Callable, Bool, Str, Array, Dict, Range, \
     DelegatesTo, PrototypedFrom, adapts
from quantities.units import \
     pi, e, V, eV, cm, nm, s, K, F, T
from quantities.constants import \
     m_e, h, k, g_e, mu_B, epsilon_0

sign = lambda x: x/abs(x)
m_e = UnitQuantity( 'free electron mass', m_e, symbol='m_e' )
set_default_units( length='nm', mass='m_e' )
aF = UnitQuantity( 'attofarad', 1e-18*F, symbol = 'aF')
def degC(x): return (273.15 + x)*K
Real = Instance( Quantity )


class FreeElectron(HasTraits):
    """
    Base class for electronic materials

    >>> vacuum = FreeElectron()
    >>> def view(material, N):
    ...     for Ec in material.capacitor(N):
    ...         print 'M       so        er C      w       A           V     B'
    ...         print Ec.M.rescale(m_e), Ec.so, Ec.er, Ec.C.rescale(aF), Ec.w,
    ...         print Ec.A, Ec.V.rescale(V), Ec.B.rescale(T)
    ...         print 'E (eV) n  m  l  s'
    ...         for E, n, m, l, s in zip(Ec.rescale(eV), Ec.n, Ec.m, Ec.l, Ec.s):
    ...             print '%6.3f %2d %2d %2d %+1.1f'% (E, n, m, l, s)
    >>> view(vacuum, 20)
    Calculating 20 lowest states: eval sort fill
    M       so        er C      w       A           V     B
    1.0 m_e 0.0 nm*eV 1 10.0 aF 10.0 nm 100.0 nm**2 0.0 V 0.0 T
    E (eV) n  m  l  s
     0.017  1  0  0 -0.5
     0.033  2  0 -1 -0.5
     0.050  3  0  1 -0.5
     0.066  4  0 -2 -0.5
     0.082  5  0  0 +0.5
     0.098  6  0 -1 +0.5
     0.118  7  0  2 -0.5
     0.134  8  0 -3 -0.5
     0.150  9  0  1 +0.5
     0.166 10  0 -2 +0.5
     0.185 11  1  0 -0.5
     0.201 12  1 -1 -0.5
     0.218 13  1 -2 -0.5
     0.234 14  1  1 -0.5
     0.250 15  1  0 +0.5
     0.266 16  1 -1 +0.5
     0.284 17  0 -4 -0.5
     0.300 18  0  3 -0.5
     0.316 19  0  2 +0.5
     0.332 20  0 -3 +0.5
    <BLANKLINE>
    >>> vacuum.potential = 0.09*V; view(vacuum, 10)
    Calculating 10 lowest states: eval sort fill
    M       so        er C      w       A           V     B
    1.0 m_e 0.0 nm*eV 1 10.0 aF 10.0 nm 100.0 nm**2 0.09 V 0.0 T
    E (eV) n  m  l  s
    -0.073  1  0  0 -0.5
    -0.057  2  0 -1 -0.5
    -0.040  3  0  1 -0.5
    -0.024  4  0 -2 -0.5
    -0.008  5  0  0 +0.5
     0.008  6  0 -1 +0.5
     0.028  7  0  2 -0.5
     0.044  8  0 -3 -0.5
     0.060  9  0  1 +0.5
     0.076 10  0 -2 +0.5
    <BLANKLINE>
    >>> B0 = h/(e*vacuum.area); B0.rescale(T)
    array(41.35667333632514) * T
    >>> vacuum.magnetic_field = B0; view(vacuum, 10)
    Calculating 10 lowest states: eval sort fill
    M       so        er C      w       A           V     B
    1.0 m_e 0.0 nm*eV 1 10.0 aF 10.0 nm 100.0 nm**2 0.09 V 41.3566733363 T
    E (eV) n  m  l  s
    -0.075  1  0 -1 -0.5
    -0.058  2  0  0 -0.5
    -0.042  3  0 -2 -0.5
    -0.023  4  0  1 -0.5
    -0.007  5  0 -3 -0.5
     0.009  6  0 -2 +0.5
     0.027  7  0 -3 +0.5
     0.043  8  0 -1 +0.5
     0.060  9  1 -1 -0.5
     0.077 10  1  0 -0.5
    <BLANKLINE>    
    """

    # Material properties
    name                    = Str('Vacuum')
    reference               = Str('Joakim.Pettersson@Quflow.com')
    effective_mass          = Real(1*m_e)
    spin_orbit_coupling     = Real(0*eV*nm)
    dielectric              = Real(1)

    # Design parameters
    capacitance             = Real(10*aF)
    width                   = Real(10*nm)
    area                    = Real(100*nm**2)
    potential               = Real(0*V)
    magnetic_field          = Real(0*T)
    
    # Observable properties
    charge      = System('q   == p e')
    conductance = System('g   == q^2/h')
    flux        = System('psi == l h/q')

    # Particle energy contributions.
    #
    # - These are valid at the limit of weak external field, low particle
    # density and infinite walls. C, A, V and B are in general distributed
    # properties. As such they need to be calculated self-consistently for each
    # state within each alternative system configuration. In a many-particle
    # system these approach classical values determined by the channel geometry
    # and charge density.
    # Notes:
    #   v is the (static) group velicity, not the (dynamic) internal motion.
    #
    rest        = System('E == m_e c^2')
    drift       = System('E == M v^2/2')
    charging    = System('E == q (n q/C + V)')
    confinement = System('E == (m + 0.5)^2 h^2/(8 M w^2)')
    circulation = System('E == ((l - A B q/h + 1/2)^2 + 1/4) h^2/(8 pi M A)')
    spin        = System('E == 2 s (pi so^2 (l - A B q/h + 1/2)^2/A'
                                   ' + ((l - A B q/h + 1/2) h^2/(8 pi M A)'
                                   ' - g_e mu_B B/2)^2)^0.5')
    
    # Systems
    
    def cavity(self, N = None):
        """
        For each design corner, yield the resonances due to confinement,
        circulation, spin and polarity. 
        
        If a number of states N is specified, those are calculated numerically
        and sorted by energy.
        """
        if N:
            print "Calculating %d lowest states:"% N,
            mlsp = independent(map( array, (
                self.m[:N], self.l[:N], self.s[:N], self.p[:N]) ))
            states = dict(zip( 'm l s p'.split(), mlsp ))
        Emls = System(self.confinement.E + self.circulation.E + self.spin.E)
        q   = self.charge.q
        g   = self.conductance.g
        psi = self.flux.psi
        for M, so, er, C, w, A, V, B in corners(independent(
            self.effective_mass,
            self.spin_orbit_coupling,
            self.dielectric,
            self.capacitance,
            self.width,
            self.area,
            self.potential,
            self.magnetic_field )):
            Ec = Emls( constants, locals() )
            if N:
                print "eval",
                E = Ec(states)
                print "sort",
                i = E.argsort(kind = 'merge')[:N]
                print "fill",
                E, m, l, s, p = [v if v.size == 1 else v[i]
                                 for v in [E] + mlsp]
                print
                Ec = System( E, Ec, m=m, l=l, s=s, p=p )
            yield Ec
        if N: print
        
    def capacitor(self, N = None):
        """
        For each design corner, yield the resonances due to single-electronic
        charging.
        """
        n = 1 + array(range(N)) if N else Symbol('n')
        for Ec in self.cavity(N):
            En = self.charging.E( Ec, n=n )
            yield System( 'En + Ec', En, En=En, Ec=Ec )

    # Quantum numbers

    def m():
        m = 0
        while 1:
            yield m
            m += 1
    m = Cache(m())

    def l():
        l = 0
        yield 0
        while 1:
            l += 1
            yield l
            yield -l
    l = Cache(l())
            
    s = [0.5, -0.5]

    p = [-1]

    # Views

    def __str__(self): return self.name

    def __repr__(self): return '%s: %r'%( self.name, list(self.cavity()) )


if __name__ == '__main__':
    import doctest
    doctest.testmod()
