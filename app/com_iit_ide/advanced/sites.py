"""
Web sites with more advanced functionality
"""

from com.iit.api import Author, Connectable
from com_iit_ide.core.web import \
     page, htmlStr, Site, Server#, serve
from types import ClassType

def path2module (path):
    m = ""
    for i in path:
        if m: m += '.'
        m += i
    return m or "__main__"

class Editable (Site):
    """
    Website logic for browsing and editing content
    """
    def __call__(self, path, py="", refresh=None, update=None):
        Site.__call__(self, path) # updates self._path/_route
        this = self._route[-1]
        name = self._path[-1]
        if update:
            this = self._route[-1] = eval (py)
            setattr (self._route[-2], name, this)
        yield "<h3>%s %s of %s:</h3>" % (
            type(this).__name__, name, path2module (self._path[:-1]))
        yield htmlStr (this)
        if type(this) is ClassType \
        or this.__class__.__doc__ != this.__doc__: 
            yield "<div align=left><pre>%s</pre></div>" % (this.__doc__ or "")
        yield "<form action=%s method=post>" % name
        yield "<p>Python representation:</p>"
        yield "<input type=text name=py value=%s></p>" % repr (repr (this))
        yield "<p><input type=submit name=refresh value=Refresh>"
        yield "<input type=submit name=update value=Update></p>"
        yield "</form>"

class Remotable (Editable):
    """
    Website logic for browsing, editing and using content
    """
    def __call__(self, path, py="", refresh=None, update=None, call=None):
        yield htmlStr (Editable.__call__(self, path, py, refresh, update))
        target = self._route[-1]
        name = self._path[-1]
        if callable (target):
            yield "<form action=%s method=post>" % repr (name)
            yield "<p>Python invocation:</p>"
            yield "<input type=text name=py value=%s></p>" % repr (py)
            yield "<p><input type=submit name=call value=Call></p>"
            yield "</form>"
            if call:
                result = eval ("target (%s)" % py)
                yield "<h4>The %s result is:</h4>" % result.__class__.__name__
                yield htmlStr (result)

class Module:
    "General storage"

class Portal (Remotable):
    """
    address = <local Connectable>, content = <login page>, verbosity = Connectable.Warnings, ** parameters
    Website logic for browsing, authoring, using and sharing content.

    Demonstrates the usage of multiple Authors on a single Connectable 
    runtime environment, providing each with a unique workspace.
    
    Limitation: publish and withdraw only affects the site object, not the
    workspaces where they should be shared.
    """
    loginDialog = """
        <form action=login method=post>
        <p>Please enter a relevant Java/Python namespace for your module:</p>
        <input type=text name=module value=com.iit.guests></p>
        <p><input type=submit name=login value=Enter></p>
        </form>
        """
    def __init__(self, address = "", content = loginDialog, verbosity = Connectable.Warnings, ** parameters):
        """
        Supply the "host:port" address of a running com.iit.api.Connectable,
        plus any initial content or parameters for the site.
        
        Creates a local Connectable if no address is specified.
        """
        Remotable.__init__(self, content, ** parameters)
        self.publisher = address or Connectable (verbosity = verbosity)
    def __call__(self, path, ** options):
        """
        /, /index:
            Ask for module to author (unless other content has been 
        /login?module=com.iit.guests:
            Prepare route to module and add __main__ author below it with a
            unique workspace on the publisher. Return link to this __main__.
        /.../__main__?py=:
            Execute py script within the __main__ author's workspace on the
            publisher. Ask for new script to execute.
        /.../__main__/.../item?py=&update=False&call=False&publish=:
            Update or call item using py expression.
            Yield info about item and ask for update or call.
            Copy item to parent of __main__ using name in publish, or ask for
            name (default is same name) to publish it with on the module.
        /.../module/item?withdraw=False:
            Yield the item.
            If module has a __main__, delete item from it if withdraw, or ask
            to withdraw it. 
        """
        print self.__call__, path, options
        if path == ['index']:
            self._path = path
            self._route = [self, self.index]
            yield self.index
        elif path == ['login']:
            self._path = path
            self._route = [self, None]
            module = options.get ('module', "com.iit.guests")
            target = self
            for i, m in enumerate (module.split ('.')):
                if not hasattr (target, m):
                    setattr (target, m, Module ())
                target = getattr (target, m)
            print "Now connecting to workspace %s on" % module, self.publisher
            target.__main__ = Author (module, str (self.publisher))
            route = module.replace ('.', '/')
            yield "Click to author module <a href=%s/__main__ >%s</a>" % (route, module)
        elif path[-1] == '__main__':
            author = Site.__call__(self, path)
            py = options.get ('py', "").replace ("\r\n", "\n")
            print "Executing", repr (py), "in", path2module (self._path)
            if py: author.do (py)
            yield """
                <form action=__main__ method=get>
                <p>You may enter python code to modify your module here.</p>
                <i>One statement at a time only, due to a limitation in spiroserver.</i>
                <textarea rows=28 cols=80 name=py>%s</textarea></p>
                <p><input type=submit value=Execute></p>
                </form>
                """ % py
        elif "__main__" in path:
            publish = options.pop ('publish', "")
            yield htmlStr (Remotable.__call__(self, path, ** options))
            for p in range (3, len (path) + 1):
                if path[1-p] == "__main__": break
            this = self._route[-1]
            target = self._route[-p]
            name = path2module (path[2-p:])
            module = path2module (path[:1-p])
            if publish:
                setattr (target, publish, this)
            else: 
                yield "<form action=%s method=post>" % name
                yield "<input type=submit value=Publish> as"
                yield "<input type=text name=publish value=%s>" % repr (name)
                yield "in module %s" % module
                yield "</form>"
        else:
            print options
            yield Site.__call__(self, path)
            if hasattr (self._route[-2], "__main__"):
                if options.get ('withdraw', False):
                    delattr (self._route[-2], path[-1])
                    yield "<H1>DELETED!</H1>"
                else:
                    yield "<form action=%s method=post>" % path[-1]
                    yield "<input type=submit name=withdraw value=Withdraw>"
                    yield "</form>"
    upload = """
            <form action="upload" method="post" enctype="multipart/form-data">
            filename: <input type="file" name="myFile"/><br/>
            <input type="submit"/>
            </form>
            """ # Lets the user select a file and upload it.
    def download(self, path):
        """
        Download a file to the user.
        """
        fStat = stat (path)
        cpg.response.headerMap["Content-Type"] = "application/x-download"
        cpg.response.headerMap['Content-Length'] = int(fStat.st_size)
        f = open (path, 'rb')
        return fileGenerator (f)
    download.exposed = True

if __name__ == '__main__':
    s = Server (Portal ())
    print "Login at http//localhost:8080 now!"
