#!/usr/local/bin/python
# coding: latin-1
"""
Smath tools

ToDo: def py2sm(s): "Convert s (text or filename) from python3 syntax to Smath syntax. Fails if not Smath compatible."
"""

from lib.SMath.rpn import execute, use, _
from lib.SMath.sm import sm


def sm2py(file="test.sm"):
    """
    Convert SMath math to python3 syntax

    >>> sm2py()
    """
    for region in sm(file):
        lines = len(_.trace)
        math = list(region)
        print("# " + " ".join(math))
        execute(*math)
        for r in _.trace[lines:]:
            yield r


for line in sm2py(): print(line)