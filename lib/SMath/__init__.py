"""
Smath tools

First step: Handle a sequence of operands and operators in reverse polish notation

ToDo: def sm2py(s): "Convert s (text or filename) from Smath syntax to python3 syntax. Fails if not Python compatible."
ToDo: def py2sm(s): "Convert s (text or filename) from python3 syntax to Smath syntax. Fails if not Smath compatible."
"""

from .rpn import rpn
from xml.etree.ElementTree import parse, iterparse

def sm2rpn(name):
    """
    Extract math from an SMath file in reverse polish notation.

    >>> sm('test.sm')
    """



