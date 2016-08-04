"""
Functions and decorators for creating HTML content
"""

from types import GeneratorType, ListType

def htmlStr (x):
    """
    Return a html-safe string representation of x if not already a string. If x
    is a sequence or iterator, return a string containing lines of such html-safe
    representations.
    """
    if isinstance (x, str):
        return x
    else:
        if type (x) in [ListType, GeneratorType]:
            return "".join ([htmlStr (s) + '\n' for s in x])
        else:
            s = str (x)
            if s and not s is x and s[0] == '<' and s[-1] == '>' and not "/>" in s and not "</" in s:
                return s.replace ('<', "&lt;").replace ('>', "&gt;")
            else:
                return s

def lines (f):
    """
    Decorator for functions that return a sequence or iterator:
    Convert result into a text containing lines of string representations.
    """
    def _wrapper (* items, ** options):
        return htmlStr (f (* items, ** options))
    return _wrapper

HtmlParameters = ['content', 'title', 'description',
                  'header', 'footer', 'sidebar',
                  'static', 'styled', 'parents']
@lines
def html (content="", title="", description="", header="", footer="", sidebar="",
          static=False, styled=False, parents=[], ** ignoreTheRest):
    """
    Wrap the parameters into html content, converting them to strings if
    necessary.

    Uses the parents as lookup tables or attribute containers for unspecified
    parameters. If a parent contain 'grand'-parents, these will be searched
    also, recursively, until a non-empty string is found. Both constants and
    functions can be used to define parameters. 

    Parameters are attempted to be called without arguments and the result used
    if that suceeds. They are then converted to html-safe string representations
    if not already strings. Parameters defined as sequences or iterators are
    converted to rows of html-safe string representations. 
    """
    lookups = [locals ()] + parents
    options = {}.fromkeys (HtmlParameters)
    i = 0
    while i < len (lookups):
        p = lookups[i]
        i += 1
        pn = getattr (p, '__name__', "") or p.__class__.__name__
        for n in HtmlParameters:
            if not options[n] or n == 'parents':
                info = ""
                try:
                    options[n] = p[n]
                    info = "%s['%s']" % (pn, n)
                except:
                    options[n] = getattr (p, n, "")
                    info = "%s.%s" % (pn, n)
                if callable (options[n]):
                    options[n] = options[n] ()
                    info += " ()"
                if options[n] and info and pn != 'dict': print n, '=', info
                if options[n] and n == 'parents':
                    lookups += options[n]
    content = htmlStr (options['content'])
    title = htmlStr (options['title'])
    description = htmlStr (options['description'])
    header = htmlStr (options['header'])
    footer = htmlStr (options['footer'])
    sidebar = htmlStr (options['sidebar'])
    if styled: yield        "<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.0 Transitional//EN'>"
    yield                   "<HTML>"
    yield                   "<HEAD>"
    if title: yield         "<TITLE>%s</TITLE>" % title
    if styled: yield        "<LINK rel='stylesheet' type='text/css' href='StyleSheet.css' />"
    if description: yield   "<META name='Description' content='%s' />" % description
    if not static: yield    "<META http-equiv='pragma' content='no-cache' />"
    yield                   "</HEAD>"
    yield                   "<BODY>"
    if header: yield        "<H3 align='center'>%s</H3>" % header
    if sidebar:
        yield               "<TABLE border='0' width='100%%' cellspacing='0' cellpadding='0'>"
        yield               "<TR valign='top'>"
        yield               "<TD width='150'>%s</TD>" % sidebar
        yield               "<TD width='25'>&nbsp;</TD>"
        yield               "<TD width='0'>"
    if title: yield         "<H3 align='center'>%s</H3>" % title
    if content:
        yield               "<DIV align='center'>"
        if styled: yield    "<!--HEADER-->"
        yield               content
        if styled: yield    "<!--FOOTER-->"
        yield               "</DIV>"
    if sidebar:
        yield               "</TD>"
        yield               "</TR>"
        yield               "</TABLE>"
    if footer: yield        "<H5 align='center'>%s</H5>" % footer
    yield                   "</BODY>"
    yield                   "</HTML>"

def uri (up=[], down=[], protocol="", ** parameters):
    """
    Return the relative uri based on pages left going up (..) and pages reached
    going down (/name), adding "cgi-style" parameters at the end. Pages can
    be any object with a name, or a string name.
    """
    s = "../" * len (up)
    for p in down:
        for i in ['name', '__name', '__name__']:
            if hasattr (p, i):
                s += getattr (p, i)
                break
        else:
            s += str (p)
        s += '/'
    if s: s = s[:-1]    # strip away last '/'
    else: s = '.'
    separator = '?'
    for name, value in parameters.items ():
        s += separator + name + '=' + repr (value)
        separator = '&'
    return protocol + s

@lines
def table (cells, border=0, width='100%', cellspacing=0, cellpadding=0, valign='top'):
    """
    Wrap a 2D array of HTML TD cells into an HTML TABLE
    """
    yield "<TABLE border='%s' width='%s' cellspacing='%s' cellpadding='%s'>" % tuple (
        [str (i) for i in [border, width, cellspacing, cellpadding]])
    for row in cells:
        yield " <TR valign='%s'>%s</TR>" % (valign, "".join (list (row)))
    yield "</TABLE>"

def cell (content = "&nbsp;", width = 0):
    """
    Wrap content into an HTML TD cell
    """
    return "<TD width='%s'>%s</TD>" % (width, htmlStr (content))

def page (title=""):
    """
    Decorator function for methods that return content for web pages:
    Wrap the result into html content with the given title using html parameters
    from the options supplied in the call or defined in the object.

    If any of the parameters listed in 'HtmlParameters' are defined in the call
    or in the object self, they will be used on the web page.

    If the object contain 'parents', these will be searched also, recursively.
    Parameters may be specified as lookup values, lookup functions, field values
    or methods. Any value is valid - its string representation will be used. If
    the resturn value is a sequence or iterator, the string representations of
    those items will be put on separate text rows.

    The decorated function will have the 'exposed' attribute set, so that the
    CherryPy web server will invoke it if found on the cpg.root object tree.
    """
    def decorator (f):
        def wrapper(self, * items, ** options):
            p = options.copy ()
            p.setdefault ('parents', [])
            p['parents'] += [self]
            return html (f (self, * items, ** options), title, ** p)
        wrapper.exposed = True
        return wrapper
    return decorator
