#!/usr/local/bin/python
# coding: latin-1
"""
Smath tools

First step: Handle a sequence of operands and operators in reverse polish notation

ToDo: def sm2py(s): "Convert s (text or filename) from Smath syntax to python3 syntax. Fails if not Python compatible."
ToDo: def py2sm(s): "Convert s (text or filename) from python3 syntax to Smath syntax. Fails if not Smath compatible."
"""

from lib.SMath.rpn import rpn
from lib.SMath.sm import sm
