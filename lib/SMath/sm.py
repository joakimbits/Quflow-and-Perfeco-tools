"""
SMath file import
"""
from lxml.etree import parse


def sm(file="lib/SMath/test.sm"):
    r"""
    Extract math from an SMath file - region by region

    >>> print('\n'.join((' '.join(region) for region in sm('test.sm'))))#doctest: +ELLIPSIS
    f t inlet_onoff 2 ) tr f t * 1 mod 2 ) 4 * : tr 1 < 1 tr 3 < 0 1 if 3 ) if 3 ) 2 1 line 4 ) :
    f t outlet_onoff 2 ) ...
    """
    tree = parse(file)
    worksheet = tree.getroot()
    assert worksheet.tag.endswith('}worksheet'), worksheet.tag

    def math_iter(math):
        for inp in math:
            if inp.tag.endswith('}input'):
                for e in inp:
                    if e.tag.endswith('}e'):
                        t = e.attrib['type']
                        if t in ('operand', 'operator', 'bracket'):
                            yield e.text
                        elif t == 'function':
                            yield e.text
                            yield e.attrib['args']
                            yield ')'
                        else:
                            yield e.attrib
                            yield e.text

    for regions in worksheet:
        if regions.tag.endswith('}regions'):
            for region in regions:
                if region.tag.endswith('}region'):
                    for math in region:
                        if math.tag.endswith('}math'):
                            yield math_iter(math)
