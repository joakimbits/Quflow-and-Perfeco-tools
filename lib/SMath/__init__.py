"""
Smath tools

First step: Handle a sequence of operands and operators in reverse polish notation

ToDo: def sm2py(s): "Convert s (text or filename) from Smath syntax to python3 syntax. Fails if not Python compatible."
ToDo: def py2sm(s): "Convert s (text or filename) from python3 syntax to Smath syntax. Fails if not Smath compatible."
"""

from lib.SMath.rpn import rpn
from lxml.etree import parse


def sm(file="lib/SMath/test.sm"):
    """
    Extract math from an SMath file - region by region

    >>> sm('test.sm')
    f t inlet_onoff 2 () tr f t * 1 mod 2 () 4 * : tr 1 < 1 tr 3 < 0 1 if 3 () if 3 () 2 1 line 4 () :
    f t outlet_onoff 2 () tr f t * 1 mod 2 () 4 * : tr 1 < 0 tr 3 < 1 0 if 3 () if 3 () 2 1 line 4 () :
    n f t onoff 3 () tr f t * 1 mod 2 () 4 * : tud tr 1 < tr tr 3 < 2 tr - tr 4 - if 3 () if 3 () : tud 4 f * / n bar * hr / * 3 1 line 5 () :
    1 (
    f t inlet 2 () 1 2 π * f * t * cos 1 () + 2 / :
    2 (
    f t outlet 2 () 1 2 π * f * t * cos 1 () - 2 / :
    3 (
    n f t boom 3 () 2 π * f * t * sin 1 () 2 π * f * / n bar * hr / * :
    4 (
    dx 0.1 :
    dashx 1.5 :
    dotx 0.75 :
    x isdash 1 () x dashx 1.5 dotx * + mod 2 () dashx < :
    x isdot 1 () x 2 dotx * mod 2 () dx < ( :
    x 0 50 dx range 3 () :
    i 0 : xi x xi isdash 1 () i i 1 + : xdash i el 2 () xi : 2 1 line 4 () 0 if 3 () 1 1 line 3 () for 3 () 2 1 line 4 ()
    i 0 : xi x xi isdot 1 () i i 1 + : xdot i el 2 () xi : 2 1 line 4 () 0 if 3 () 1 1 line 3 () for 3 () 2 1 line 4 ()
    xonoff 12.5 - dx + 12.5 dx - 12.5 dx + 37.5 dx - 37.5 dx + 62.5 dx - 1 6 mat 8 () transpose 1 () :
    """
    tree = parse(file)
    worksheet = tree.getroot()
    assert worksheet.tag.endswith('}worksheet'), worksheet.tag
    for regions in worksheet:
        if regions.tag.endswith('}regions'):
            for region in regions:
                if region.tag.endswith('}region'):
                    for math in region:
                        if math.tag.endswith('}math'):
                            for inp in math:
                                if inp.tag.endswith('}input'):
                                    for e in inp:
                                        if e.tag.endswith('}e'):
                                            t = e.attrib['type']
                                            if t in ('operand', 'operator', 'bracket'):
                                                print(e.text, end=' ')
                                            elif t == 'function':
                                                print(e.text, e.attrib['args'], end=' () ')
                                            else:
                                                print(e.attrib, e.text, end=' ')
                            print()


if __name__ == '__main__':
    import doctest

    doctest.testmod()
