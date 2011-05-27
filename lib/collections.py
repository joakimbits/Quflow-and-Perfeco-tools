"""
Collections of unique numbers or positions.

Used within nanoelectronics to explore all possible collections of electrons.
(Stationary electrons have either unique wave-numbers or unique positions.)
"""

from numpy import array
from itertools import combinations
from math import floor

def sum_of_unique_integers( lvec, L, lmin, lmax, k = 0 ):
    """
    Example: All combinations of 3 unique integers from -3 to 3 with sum 0.
    >>> print array(list(sum_of_unique_integers( [0]*3, 0, -3, 3 )))
    [[-3  0  3]
     [-3  1  2]
     [-2 -1  3]
     [-2  0  2]
     [-1  0  1]]
    """
    lvec = array(lvec)
    K = len(lvec)
    if k == K:
        yield lvec.copy()
    else:
        dk = K - k - 1
        l1 = max( lmin, L + dk*(dk-1-2*lmax)/2 )
        l2 = min( lmax, L + dk*(dk-1-2*l1)/2 )
        for l in range( l1, l2+1 ):
            lvec[k] = l
            for lv in sum_of_unique_integers( lvec, L-l, l+1, lmax, k+1 ):
                yield lv

def sum_of_halves( svec, S, k = 0 ):
    """
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

def sum_of_unique_integer_with_half( lsvec, LS, lmin, lmax, k = 0 ):
    """
    Example: All combinations of 2 unique (l, s) values from (-1, -0.5) to
    (1, 0.5) with total sum 0.0.
    >>> print array(list(sum_of_unique_integer_with_half(
    ...     [(0, 0.5)]*2, 0.0, -1, 1 )))
    [[[-1.  -0.5]
      [ 1.   0.5]]
    <BLANKLINE>
     [[-1.   0.5]
      [ 0.   0.5]]
    <BLANKLINE>
     [[-1.   0.5]
      [ 1.  -0.5]]
    <BLANKLINE>
     [[ 0.  -0.5]
      [ 0.   0.5]]
    <BLANKLINE>
     [[ 0.  -0.5]
      [ 1.  -0.5]]]
    """
    lsvec = array(lsvec)
    K = len(lsvec)
    if k == K:
        yield lsvec.copy()
    else:
        dk = K - k - 1
        ls1 = max( lmin-0.5, LS + .5*dk*(dk-2-2*lmax) )
        ls2 = min( lmax+0.5, LS + .5*dk*(dk-2*ls1) )
        l1 = max(int(lmin), int(floor(ls1)))
        l2 = min(lmax, int(floor(ls2+0.5)))
        for l in range( l1, l2+1 ):
            for s in (-0.5, 0.5):
                if ls1 <= l + s <= ls2:
                    lsvec[k] = (l, s)
                    for lsv in sum_of_unique_integer_with_half(
                        lsvec, LS-l-s, l+0.5, lmax, k+1 ):
                        yield lsv

if __name__ == '__main__':
    import doctest
    doctest.testmod()
