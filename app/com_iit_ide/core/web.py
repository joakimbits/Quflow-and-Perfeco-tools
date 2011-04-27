"""
Web page logic variants, a web server and a telnet server
"""

import cherrypy                         # cherrypy.server is an HTTP server at default port=8080.
#from telnetlib.backdoor import serve    # serve is a TELNET server at default port=8023.
from threading import Thread            # servers will run in separate threads, for now.
from html import page, HtmlParameters, htmlStr, uri, table, cell
from com.iit.api.core.tools import Terminal

def fileGenerator (f, kB=8):
    """
    Generates chunks of default size 8 kB from file f. Useful to speed up file
    upload and download without using much RAM.
    """
    while 1:
        data = f.read (kB * 1024)
        if not data: break
        yield data

def children (x):
    """
    Returns x.dir (), x.__dixt__.keys (), or [] if neither of those exists
    """
    return (getattr (x, 'dir', None) or getattr (x, '__dict__', {}).keys) ()

class Page:
    """
    Webpagelogic for showing single-page content

    Optional html parameter names are listed in 'HtmlParameters'.
    """
    def __init__(self, content = "Hello World!", ** parameters):
        self.index = content
        for name, value in parameters.iteritems ():
            setattr (self, name, value)
    @page() # generate a web page using html parameters in self
    def default(self, * path, ** options):
        if path != ['index']:
            return "Try going <a hef=..>up one level</a>."
        if options:
            return "Try <a href=.>without parameters</a>."
        return self.index
    def __repr__(self):
        """
        Returns an string which can be evaluated to re-generate self.
        """
        args = [self.index]
        kwds = self.__dict__.copy ()
        for key, val in kwds.copy ().iteritems ():
            if key[0] == '_' or val in args:
                del kwds[key]
        r = "%s.%s (" % (self.__module__, self.__class__.__name__)
        s = ""
        for a in args:
            r += s + repr (a)
            s = ", "
        for k, v in kwds.iteritems ():
            r += s + k + '=' + repr (v)
            s = ", "
        return r + ')'

class Site (Page):
    """
    Website logic for browsing content

    ToDo: This version gives access to all attributes, including the html
    parameters and the attributes of the editor self. These attributes should
    be hidden in order to clean up and safeguard the site.
    """
    @page()
    def default(self, * path, ** options):
        """
        Makes the object visible on a web page. Forwards to the __call__ method,
        flattening the result to a html-safe string.
        
        Override this method if you want to return decorated objects or html
        strings, or need to return other content than html pages, such as files.
        """
        return htmlStr (self (list (path), ** options))
    def __call__(self, path):
        """
        Returns a target object by traversing the website using the path. The
        path and resulting route of objects are stored in self._path and
        self._route, respectively.

        Extend this method if you need to handle options.
        """
        self._path = path
        self._route = [self]
        for i in path:
            self._route.append (getattr (self._route[-1], i))
        return self._route[-1]
    def sidebar(self,
                tab = "&nbsp;", # Indent by single space
                link = "<br>%(indent)s<a href=%(uri)s>%(name)s</a>",
                target = "<br>%(indent)s<b>%(name)s</b>"):
        """
        Hierarchical sidebar for links to information related to content on the
        selected page. Returns an iterator with indented html links according
        to the route taken to reach the target and to any related attributes.
        The target is highlighted rather than linked, of course.

        The intent tab defaults to a single space. The link and target formats
        are configurable and should use the lookup items 'indent', 'uri' and
        'name'.
        """
        n = len (self._path)
        if n>1: 
            parent, this = self._route[-2:]
            up, here = self._path[-2:]
            us = children (parent)
            for i, s in enumerate (self._path[:-1]):
                yield link % {'indent': tab * i,
                              'uri':    ("../" * (n - i - 1)) + s,
                              'name':   s}
        else:
            this = self._route[-1]
            here = self._path[-1]
            us = [here]
        for s in us:
            items = {'indent': tab * n,
                     'uri':    s,
                     'name':   s}
            if s == here:
                yield target % items
                for c in children (this):
                    yield link % {'indent': tab * (n + 1),
                                  'uri':    s + '/' + c,
                                  'name':   c}
            else:
                yield link % items

class Server (Terminal, Thread):
    """
    Web server for a web page or site
    """
    defaultOptions = {
        #[server]
        'socketPort': 80,
        'threadPool': 10,
        #[staticContent]
        'bitmaps':  "bitmaps",
        'css':      "css",
        #[session]
        'storageType': 'ram'}
    def __init__(self, site, options = defaultOptions):
        """
        Start a web server for the site/page

        If another server is already running a root site, add this below
        the root level instead of replacing the existing root.
        """
        self.options = options
        Terminal.__init__(self, "not started")
        Thread.__init__(self)
        self.root = site
        try:
            self.start ()
            cherrypy.root = site
        except:
            print "Another server is running a root (/) site", cherrypy.root
            print "This", site, "is put in /" + site.__class__.__name__
            setattr (cherrypy.root, site.__class__.__name__, site)
    def run(self):
        Terminal.__init__(self,
            "http://localhost:%d" % self.options.get ('socketPort', 8080))
        cherrypy.server.start (configMap = self.options)
    def stop(self):
        cherrypy.server.stop ()
        Terminal.__init__(self, "stopped")

if __name__ == '__main__':
    def exe (s):
        "Execute a Python script"
        exec s + '\n'
    s = Server (Remotable, exe,
        test = "Hello there, wazup?",
        pi = 3.14)
    telnet = Thread (target = serve)        # This is the telnet Python server thread.
    telnet.start ()                         # It should have access to s also, within its thread context.
    s.root.telnet = telnet                  # Exposes telnet below the root of the web server.
    print "Listening on http://localhost:8080"
