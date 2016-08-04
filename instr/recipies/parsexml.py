# Copyright 2004-2005 Elemental Security, Inc. All Rights Reserved.
# Licensed to PSF under a Contributor Agreement.

"""Simple XML parsing and rendering framework.

This uses someting almost, but not quite, entirely unlike a DTD.

"""

# Python imports
import os
import sys
import cgi
import copy
import datetime
from StringIO import StringIO
from xml.parsers import expat

class ParseError(Exception):
    """Exception raised for problems parsing the rules XML."""
    def __init__(self, msg, *args):
        if args:
            msg = msg % args
        Exception.__init__(self, msg)

class GenericParser(object):

    """Generic parser for XML.

    Typical usage:

    Initialization:

    >>> p = GenericParser([Root1, Root2, ...])

    Then one of the following:

    >>> obj = p.parseText('<foo>...</foo>')
    >>> obj = p.parseFile('demo1.xml')
    >>> obj = p.parseStream(open('demo1.xml'), 'demo1.xml')

    Or, for a lower-level API:

    >>> p.reset('demo1.xml')
    >>> p.feedText('<foo>')
    >>> p.feedText('...')
    >>> p.feedText('</foo>', True)
    >>> obj = p.root
    >>> 

    """

    def __init__(self, rootClasses, __filler=0, strict=True):
        self.rootClasses = rootClasses
        self.strict = strict

    def parseText(self, text, filename=None):
        self.reset(filename)
        self.feedText(text, True)
        return self.root

    def parseFile(self, file):
        if hasattr(file, "read"):
            return self.parseStream(file)
        if file == "-" or not file:
            return self.parseStream(sys.stdin, "<stdin>")
        f = open(file, "r")
        try:
            self.reset(file)
            return self.parseStream(f, file)
        finally:
            f.close()

    def parseStream(self, f, filename=None):
        if filename is None:
            try:
                filename = f.name
            except AttributeError:
                filename = None
        self.reset(filename)
        self.feedStream(f)
        return self.root

    def reset(self, filename=None):
        self.filename = filename
        self.parser = parser = expat.ParserCreate(None, None)
        parser.StartElementHandler = self.start
        parser.EndElementHandler = self.end
        parser.CharacterDataHandler = self.characters
        self.stack = []
        self.rules = []
        self.root = None

    def feedText(self, text, isFinal=False):
        try:
            self.parser.Parse(text, isFinal)
        except expat.ExpatError, err:
            self.raiseError(err)

    def feedStream(self, stream):
        try:
            self.parser.ParseFile(stream)
        except (expat.ExpatError, ParseError), err:
            self.raiseError(err)

    def raiseError(self, err):
        msg = "line %d: %s" % (self.parser.ErrorLineNumber, err)
        if self.filename:
            msg = "%s, %s" % (self.filename, msg)
        raise ParseError(msg)

    def start(self, tag, attrs):
        try:
            # Attempt to convert Unicode to 8-bit strings
            tag = str(tag)
        except:
            pass
        for key, value in attrs.items():
            try:
                # Attempt to convert Unicode to 8-bit strings
                attrs[key] = str(value)
            except:
                pass
        if not self.stack:
            # Pick a root element
            for cls in self.rootClasses:
                if tag == cls.__element__:
                    if self.strict:
                        self.root = cls(attrs)
                    else:
                        self.root = cls(attrs, False)
                    self.stack.append(self.root)
                    break
            else:
                raise ParseError("<%s> is not a root element", tag)
        else:
            if self.strict:
                obj = self.stack[-1].__add_child__(tag, attrs)
            else:
                parent = self.stack[-1]
                if parent is None:
                    parent = self.getparent()
                if parent is None:
                    obj = None
                else:
                    obj = parent.__add_child__(tag, attrs, strict=False)
            self.stack.append(obj)

    def characters(self, data):
        try:
            # Attempt to convert Unicode to 8-bit strings
            data = str(data)
        except:
            pass
        if self.stack:
            if self.strict:
                self.stack[-1].__add_characters__(data)
            else:
                parent = self.stack[-1]
                if parent is None:
                    parent = self.getparent()
                if parent is not None:
                    parent .__add_characters__(data, strict=False)

    def end(self, tag):
        obj = self.stack.pop()
        if self.strict:
            assert tag == obj.__element__
        else:
            assert obj is None or tag == obj.__element__

    def getparent(self):
        i = len(self.stack)
        parent = None
        while parent is None and i > 0:
            i -= 1
            parent = self.stack[i]
        return parent

# Classes defining the "DTD"

class ElementClass(object):
    # Override these special attributes in subclasses
    __element__ = ""     # string giving the tag name
    __attributes__ = {}  # dict of attrname -> function (e.g. String, Boolean)
    __children__ = {}    # dict of ElementClass -> attrname
                         # if attrname ends in [], it is a list
    __characters__ = ""  # string giving name where to store character data

    #classmethod
    def __parser__(cls, __filler=0, strict=True):
        return GenericParser([cls], strict=strict)
    __parser__ = classmethod(__parser__)

    #classmethod
    def __parseStream__(cls, stream, filename=None, __filler=0, strict=True):
        return GenericParser([cls], strict=strict).parseStream(stream,
                                                               filename)
    __parseStream__ = classmethod(__parseStream__)

    #classmethod
    def __parseText__(cls, text, filename=None, __filler=0, strict=True):
        return GenericParser([cls], strict=strict).parseText(text, filename)
    __parseText__ = classmethod(__parseText__)

    #classmethod
    def __parseFile__(cls, file, __filler=0, strict=True):
        return GenericParser([cls], strict=strict).parseFile(file)
    __parseFile__ = classmethod(__parseFile__)

    def __init__(self, __attrs=None, __strict=True, **kwds):
        # You can't pass both __attrs and kwds simultaneously
        assert __attrs is None or kwds == {}
        # We don't support __characters__ and __children__ simultaneously
        assert not (self.__children__ and self.__characters__), \
               self.__class__.__name__
        # First set all undefined attributes to None
        for key in self.__attributes__:
            name = self.__fixname__(key)
            if not hasattr(self, name):
                setattr(self, name, None)
        # And set all child attributes to None or []
        for childname in self.__children__.itervalues():
            if childname.endswith("[]"):
                childname = childname[:-2]
                setattr(self, childname, [])
            else:
                setattr(self, childname, None)
        # Set __characters__ attribute to ""
        if self.__characters__:
            self.__init_characters__()
        # Then set all actual attributes, either from __attrs or from kwds
        if __attrs is None:
            # Use kwds; these should be appropriate Python values.
            # You can use this to set *arbitrary* instance attributes.
            for key, value in kwds.items():
                # key is already the *output* of __fixname__!
                setattr(self, key, value)
        else:
            # Use __attrs; these should be strings
            if __strict:
                self.__set_attributes__(__attrs)
            else:
                self.__set_attributes__(__attrs, strict=False)

    def __init_characters__(self):
            setattr(self, self.__characters__, "")

    def __fixname__(self, name):
        return name.replace("-", "_")

    def __str__(self):
        f = StringIO()
        self.__render__(f)
        return f.getvalue().rstrip("\n")

    def __eq__(self, other):
        if other is None:
            return False
        cls = self.__class__
        if other.__class__ is not cls:
            return False
        for name in cls.__attributes__:
            name = self.__fixname__(name)
            if getattr(self, name) != getattr(other, name):
                return False
        for c, name in cls.__children__.items():
            if name.endswith("[]"):
                name = name[:-2]
            if getattr(self, name) != getattr(other, name):
                return False
        name = cls.__characters__
        if name:
            if getattr(self, name) != getattr(other, name):
                return False
        return True

    def __ne__(self, other):
        if other is None:
            return True
        return not self.__eq__(other)

    def __render__(self, f=None, level="", indent="  "):
        if f is None:
            f = sys.stdout
        f.write("%s<%s" % (level, self.__element__))
        if len(self.__attributes__) > 1:
            sep = "\n%s  " % (level+indent)
        else:
            sep = " "
        attrnames = self.__attributes__.keys()
        attrnames.sort()
        for key in attrnames:
            value = getattr(self, self.__fixname__(key), None)
            if value is not None:
                function = self.__attributes__[key]
                value = function.__render__(value)
                f.write('%s%s="%s"' % (sep, key, value))
        if self.__characters__:
            assert not self.__children__ # Again!
            f.write(">")
            self.__render_characters__(f, level, indent)
            f.write("</%s>\n" % self.__element__)
        elif not self.__children__:
            f.write(" />\n")
        else:
            f.write(">\n")
            entries = [(childname, key)
                       for key, childname in self.__children__.items()]
            entries.sort()
            for childname, key in entries:
                if childname.endswith("[]"):
                    childname = childname[:-2]
                    children = getattr(self, childname, [])
                    for child in children:
                        child.__render__(f, level+indent, indent)
                else:
                    child = getattr(self, childname, None)
                    if child is not None:
                        child.__render__(f, level+indent, indent)
            f.write("%s</%s>\n" % (level, self.__element__))

    def __render_characters__(self, f, level, indent):
        text = getattr(self, self.__characters__, None)
        if text is not None:
            # Reproduce the text exactly (except for quoting)
            f.write(cgi.escape(text))

    def __set_attributes__(self, attrs, __filler=0, strict=True):
        attrdefs = self.__attributes__
        for key, value in attrs.items():
            if key not in attrdefs:
                if not strict:
                    continue
                raise ParseError("<%s> tag has no %r attribute",
                                 self.__element__, key)
            setattr(self, self.__fixname__(key), attrdefs[key](value))

    def __add_child__(self, tag, attrs, __filler=0, strict=True):
        for cls in self.__children__:
            if tag == cls.__element__:
                if strict:
                    obj = cls(attrs)
                else:
                    obj = cls(attrs, False)
                childname = self.__children__[cls]
                if childname.endswith("[]"):
                    childname = childname[:-2]
                    children = getattr(self, childname, None)
                    if children is None:
                        children = []
                        setattr(self, childname, children)
                    children.append(obj)
                else:
                    child = getattr(self, childname, None)
                    if child is None:
                        setattr(self, childname, obj)
                    elif strict:
                        raise ParseError("duplicate <%s> in <%s>",
                                         tag, self.__element__)
                return obj
        if not strict:
            return None
        raise ParseError("<%s> not allowed inside <%s>", tag, self.__element__)

    def __add_characters__(self, data, __filler=0, strict=True):
        if self.__characters__:
            text = getattr(self, self.__characters__, None)
            if text is None:
                text = data
            else:
                text += data
            setattr(self, self.__characters__, text)
        else:
            if strict and not data.isspace():
                raise ParseError("unexpected characters inside <%s> element",
                                 self.__element__)

    def __deepcopy__(self, memo=None):
        # Assume the class can be invoked without arguments
        new = self.__class__()
        # Assume attributes are immutable values
        for key in self.__attributes__:
            name = self.__fixname__(key)
            setattr(new, name, getattr(self, name))
        # Use proper deep copying for sub-elements
        for name in self.__children__.itervalues():
            if name.endswith("[]"):
                name = name[:-2]
            setattr(new, name, copy.deepcopy(getattr(self, name), memo))
        name = self.__characters__
        if name:
            setattr(new, name, copy.deepcopy(getattr(self, name), memo))
        return new

    def __clone__(self):
        """Public API (!) to create a clone."""
        return copy.deepcopy(self)

# Functions defining attribute types

def String(arg):
    # XXX Unicode?
    return str(arg)
def String_render(s):
    return cgi.escape(s, True)
String.__render__ = String_render

def Integer(arg):
    try:
        return int(arg)
    except ValueError, err:
        raise ParseError("attribute value %r is not an Integer", arg)
Integer.__render__ = str

_trues = dict.fromkeys(["yes", "on", "true", "1"])
_falses = dict.fromkeys(["no", "off", "false", "0"])

def Boolean(arg):
    s = arg.strip().lower()
    if s in _trues:
        return True
    if s in _falses:
        return False
    raise ParseError("attribute value %r is not a Boolean", arg)
Boolean.__render__ = lambda b: b and "true" or "false"

def _test():
    class Inner(ElementClass):
        __element__ = "inner"
        __attributes__ = {"special": Boolean}
        __characters__ = "text"
    class Outer(ElementClass):
        __element__ = "outer"
        __attributes__ = {"id": Integer, "name": String}
        __children__ = {Inner: "inner[]"}
    sample = '''<outer id="1" name="foo">
        <inner special="false">blah, blah</inner>
    </outer>'''
    outie = Outer.__parseText__(sample)
    print (outie.id, outie.name)
    for innie in outie.inner:
        print (innie.special, innie.text, str(innie))
    print outie

if __name__ == "__main__":
    _test()

 	  	 

