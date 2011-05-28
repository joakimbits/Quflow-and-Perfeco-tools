"""
Collections of unique numbers or positions.

Used within nanoelectronics to explore all possible collections of electrons
generating a certain net magnetic flux. (Stationary electron wave-modes have
either unique wave-numbers or unique positions. Their circulation and spin
directions determine the direction and magnitude of generated magnetic flux.)
"""

from numpy import array
from itertools import combinations
from math import floor

def sum_of_unique_integers( lvec, L, lmin, lmax, k = 0 ):
    """
    Example: All combinations of 3 unique integers from -3 to 3 with sum 0, in
    increasing order of complexity.
    >>> a = array(list(sum_of_unique_integers( [0]*3, 0, -3, 3 ))); print a
    [[-1  0  1]
     [-2  0  2]
     [-2 -1  3]
     [-3  1  2]
     [-3  0  3]]
    >>> for c in a: print complexity(c),
    2 8 14 14 18
    """
    lvec = array(lvec)
    K = len(lvec)
    if k == K:
        yield lvec.copy()
    else:
        dk = K - k - 1
        l1 = max( lmin, L + dk*(dk-1-2*lmax)/2 )
        l2 = min( lmax, L + dk*(dk-1-2*l1)/2 )
        for l in range( l2, l1-1, -1 ):
            lvec[k] = l
            for lv in sum_of_unique_integers( lvec, L-l, l+1, lmax, k+1 ):
                yield lv

def sum_of_halves( svec, S, k = 0 ):
    """
    Yield all possible positions of s=+-0.5 with sum S.

    S corresponds to net number of magnetic flux quanta h/e generated from
    non-circulating spin s electrons.
    
    Example: All positions of 3 halves with sum +0.5. 
    >>> print array(list(sum_of_halves( [0.5]*3, +0.5 )))
    [[-0.5  0.5  0.5]
     [ 0.5 -0.5  0.5]
     [ 0.5  0.5 -0.5]]
    """
    svec = array(svec)
    K = len(svec)
    svec[k:K] = array(0.5).repeat(K-k)
    negs = 0.5*(K-k-2*S)
    negv = array(-0.5).repeat(negs)
    for flips in combinations( range( k, K ), int(negs) ):
        sv = svec.copy()
        sv[array(flips)] = negv
        yield sv

def sum_of_fluxes( lsvec, LS, lmin=-1000, lmax=1000, k = 0 ):
    """
    Yield all possible combinations of l+s with sum LS, in increasing order
    of complexity. l are integers and s are +-0.5.

    LS corresponds to the net number of magnetic flux quanta generated from
    a set of circulating electrons. Each electron makes l loops around the net
    flux before interfering constructively with themselves. The electrons are
    assumed to co-exist in the same space during many such circulations, and
    therfore have unique combinations of l and s.

    Example: 10 least complex combinations of 2 unique (l, s) values with total
    sum 0.0.
    >>> lsg = sum_of_fluxes([(0, 0.5)]*2, 0.0 )
    >>> for i in range(10):
    ...     lsv = lsg.next()
    ...     print complexity(lsv), ' '.join(['%d%+.1f'% (l, s) for l, s in lsv])
    0.5 0+0.5 0-0.5
    0.5 0-0.5 0+0.5
    1.5 0-0.5 1-0.5
    1.5 -1+0.5 0+0.5
    2.5 -1+0.5 1-0.5
    2.5 -1-0.5 1+0.5
    5.5 -1-0.5 2-0.5
    5.5 -2+0.5 1+0.5
    8.5 -2+0.5 2-0.5
    8.5 -2-0.5 2+0.5
    """
    lsvec = array(lsvec)
    K = len(lsvec)
    if k == K:
        yield lsvec.copy()
    else:
        # Calculate l+s interval considering the remaning span.
        dk = K - k - 1
        ls1 = max( lmin-0.5, LS + .5*dk*(dk-2-2*lmax) )
        ls2 = min( lmax+0.5, LS + .5*dk*(dk-2*ls1) )
        # Find corresponding l interval and growth direction.
        l1 = max(int(lmin), int(floor(ls1)))
        l2 = min(lmax, int(floor(ls2+0.5)))
        ll, ss, dl = (((range( l1, l2+1 ), (-0.5, 0.5), 0.5) if l1+l2 > 0
                       else (range( l2, l1-1, -1 ), (0.5, -0.5), -0.5)))
        for l in ll:
            for s in ss:
                if ls1 <= l + s <= ls2:
                    lsvec[k] = (l, s)
                    for lsv in sum_of_fluxes( lsvec, LS-l-s, l+dl, lmax, k+1 ):
                        yield lsv

def complexity( a ): return (array(a)**2).sum()

if __name__ == '__main__':
    import doctest
    doctest.testmod()
