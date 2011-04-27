#!/usr/bin/env jython
#@+leo-ver=4
#@+node:@file spiroserver.py
#@@first
"""
Implements the server side of SPIRO

Run with -h or --help to see options

By default, this script executes under jython
"""
#@+others
#@+node:Third-party imports
# --------------------------------------------------------
# if you are building the server into a jar file, you will
# need to add here any third-party java imports you need
# --------------------------------------------------------

# these are just examples from the I2P project
#import net.i2p
#import net.i2p.client
#import net.i2p.crypto
#import net.i2p.data

#@-node:Third-party imports
#@+node:imports
import sys, os, time, Queue, thread, threading, StringIO, traceback, getopt, pickle
import socket
from SocketServer import ThreadingTCPServer, StreamRequestHandler

try:
    import signal
except:
    pass

import spiroclient

import code

from pdb import set_trace
#@-node:imports
#@+node:globals
host = "127.0.0.1"
port = 9091

# default logging parameters
defaultLogfile = sys.stdout
defaultLogVerbosity = 5
defaultLogDelay = 0

#@-node:globals
#@+node:class SpiroServer
class SpiroServer(ThreadingTCPServer):
    """
    Server side of SPIRO
    
    When each client connection arrives, this creates an instance of
    SpiroSession and dispatches it to run the session.
    """
    #@    @+others
    #@+node:attributes
    host = host
    port = port
    
    importslist = []
    #@-node:attributes
    #@+node:__init__
    def __init__(self, **kw):
        """
        Create the SPIRO server object
        
        Arguments:
            - none yet
        Keywords:
            - host - host to listen on for client conns (default self.host ('127.0.0.1')
            - port - port to listen on for client conns (default self.port (7656)
            - imports - imports to pre-load into each new client namespace
            - logVerbosity - logging verbosity, defaults to global defaultLogVerbosity
            - logFile - file to log to, defaults to defaultLogfile
            - logDelay - delay between logging messages, default 0
        """
        # get a logger, and grab method shortcuts
        self.logger = SpiroLogger(**kw)
        self.log = self.logger.log
        self.logException = self.logger.logException
    
        # get optional host/port for client and i2cp
        self.host = kw.get('host', self.host)
        self.port = int(kw.get('port', self.port))
        self.importslist = kw.get('imports', self.importslist)
        self.modules = {}
    
        self.objCache = {}
    
        # sessions by name, allows n clients to share a space
        self.sessions = {}
    
        # do the required imports
        for importname in self.importslist:
            module = myimport(importname)
            self.modules[importname] = module
        if self.modules.keys():
            self.log(4, "Pre-Imported modules: %s" % ", ".join(self.modules.keys()))
    
        # do the base class
        ThreadingTCPServer.__init__(
            self, 
            (self.host, self.port),
            SpiroSession)
    #@-node:__init__
    #@+node:run
    def run(self):
        """
        Run the SPIRO server.
    
        when connections come in, they are automatically
        accepted, and a L{SpiroReqHandler} object created,
        and its L{handle} method invoked.
        """
        self.log(4, "Listening for client requests on %s:%s" % (self.host, self.port))
        self.serve_forever()
    #@-node:run
    #@+node:finish_request
    def finish_request(self, request, client_address):
        """Finish one request by instantiating RequestHandlerClass."""
        try:
            self.RequestHandlerClass(request, client_address, self)
        except:
            pass
        self.log(3, "Client session terminated")
    #@-node:finish_request
    #@-others

#@-node:class SpiroServer
#@+node:class SpiroSession
class SpiroSession(StreamRequestHandler):
    r"""
    Manages a single client session, over a TCP socket connection from the client.
    
    When a client connects to the SPIRO server, the SpiroServer
    object creates an instance of this class, and invokes its
    handle method. See L{handle}.

    Note that if a client terminates its connection to the server, the server
    will destroy all current connections initiated by that client
    
    Connection is persistent
    """
    #@	@+others
    #@+node:handle
    def handle(self):
        """
        Runs the whole client session, receiving commands from the client,
        executing them locally, and sending back results as appropriate
        """
        # get the server's logger
        server = self.server
        self.log = server.log
        self.logException = server.logException
    
        # need a local sending lock
        self.sendLock = threading.Lock()
    
        # cache of call results (FIXME: MEMORY LEAK!!!)
        self.nextResultId = 0
        self.cache = {} # cache for results of past cmds
    
        # get session name
        self.sessName = self.rfile.readline().strip()
    
        # create a session-local namespace and interpreter object
        #self.interpDict = {}
        #self.interp = SpiroInterpreter(self.interpDict)
        env = self.server.sessions.get(self.sessName, None)
        if env:
            self.interpDict, self.interp = env
        else:
            self.interpDict = {}
            self.interp = SpiroInterpreter(self.interpDict)
            self.server.sessions[self.sessName] = (self.interpDict, self.interp)
    
        # populate the namespace with preloaded modules
        self.interpDict.update(self.server.modules)
    
        # table of callback reply queues
        self.callbackReplies = {}
    
        self.log(4, "Got req from %s" % repr(self.client_address))
    
        while 1:
            try:
                req = self.getRequest()
            except:
                self.logException(2, "Failed to get request")
                break
            try:
                thread.start_new_thread(self.executeRequest, (req,))
                #self.executeRequest(req)
            except:
                self.logException(4, "Failed to process request")
    
    #@-node:handle
    #@+node:allocId
    def allocId(self):
        """
        Allocates a unique id
        """
        n = self.nextResultId
        self.nextResultId += 1
        #self.log(4, "self=%s allocated id %s" % (self, n))
        
        return n
    
    
    #@-node:allocId
    #@+node:getRequest
    def getRequest(self):
        """
        Reads a client req - size\ndata - a pickled dict
        """
        try:
            size = int(self.rfile.readline().strip())
        except:
            self.logException(2, "Failed to read size header, killing client")
            thread.exit()
    
        raw = self.readbytes(size)
        try:
            #req = pickle.loads(raw)
            #file("/tmp/shit", "wb").write(raw)
            unpickler = SpiroUnpickler(self, raw)
            req = unpickler.load()
        except:
            self.logException(1, "Failed to unpickle client command")
            raise
        return req
    
    
    
    #@-node:getRequest
    #@+node:executeRequest
    def executeRequest(self, req):
        """
        Given a request (a dict), does what's needed
        """
    
        # cmdId, cmd, args, kw
    
        self.log(4, repr(req))
    
        cmdId = req['cmdId']
        cacheId = req['cacheId']
        cmd = req['cmd']
        args = req['args']
        kw = req['kw']
        attr = req.get('attr', None)
    
        if cmd == 'goodbye':
            # sent by a dying client object
            del self.cache[cacheId]
            return # no reply needed
    
        if cmd == 'callbackreply':
            cbId = cmdId
            ret = args[0]
            self.log(4, "reply from callback #%s -> %s" % (cbId, ret))
            cbq = self.callbackReplies.get(cbId, None)
            if cbq != None:
                cbq.put(ret)
            return
    
        if cmd == 'get':
    
            if cacheId is not None:
                try:
                    self.log(4, "found item %d in cache, fetching attrib %s" % (cacheId, attr))
                    parentobj = self.cache[cacheId]
                    if attr == '__str__':
                        obj = strfuncFactory(parentobj)
                    elif attr == '__repr__':
                        obj = reprfuncFactory(parentobj)
                    elif attr == '__dict':
                        obj = parentobj.__dict__
                    elif attr == '__class':
                        obj = parentobj.__class__
                    elif attr == '__dir':
                        obj = tuple(dir(parentobj))
                    else:
                        obj = getattr(parentobj, attr)
                    cacheId = self.allocId()
                    self.cache[cacheId] = obj
    
                    self.log(4, "about to reply")
    
                    encoded = self.pickleIfPossible(obj)
    
                    self.sendReply(
                        cmdId=cmdId,
                        cacheId=cacheId,
                        status='ok',
                        val=encoded,
                        )
                except Exception, e:
                    self.logException(4, "No such attribute '%s'" % attr)
                    self.sendReply(
                        cmdId=cmdId,
                        attr=attr,
                        status='notfound',
                        )
                    return
                    self.sendException(
                        e,
                        cmdId=cmdId,
                        cacheId=None,
                        status='exception',
                        attr=attr,
                        excName=e.__class__.__name__,
                        excArgs=e.args
                        )
                return
    
            # not cached, try to fetch
            try:
                self.log(4, "get: seeking root attrib %s" % attr)
                if attr in ['do', 'dir', 'doimport']:
                    obj = getattr(self, attr)
                    encoded = None
                else:
                    self.log(4, "get: about to eval %s" % repr(attr))
                    #obj = eval(attr)
                    obj = self.interpDict[attr]
                    self.log(4, "get: got %s" % repr(obj))
    
                    encoded = self.pickleIfPossible(obj)
    
                cacheId = self.allocId()
                self.cache[cacheId] = obj
    
                self.sendReply(
                    cmdId=cmdId,
                    cacheId=cacheId,
                    status='ok',
                    val=encoded,
                    )
    
            except Exception, e:
                self.logException(4, "No such object '%s'" % attr)
                self.sendReply(
                    cmdId=cmdId,
                    attr=attr,
                    status='notfound',
                    )
                return
    
                self.sendException(
                    e,
                    cmdId=cmdId,
                    cacheId=None,
                    status='exception',
                    attr=attr,
                    excName=e.__class__.__name__,
                    excArgs=e.args
                    )
    
        elif cmd == 'call':
    
            obj = self.cache[cacheId]
    
            try:
                self.log(4, "about to call object '%s' with args=%s kw=%s" % (
                    str(obj), repr(args), repr(kw)
                    ))
                res = obj(*args, **kw)
    
                # pass back encoded return value, if we can encode it
                encoded = self.pickleIfPossible(res)
    
                # pass back as object ref
                cacheId = self.allocId()
                self.log(4, "back from call, allocated cacheId %s" % cacheId)
                self.cache[cacheId] = res
                self.sendReply(
                    cmdId=cmdId,
                    cacheId=cacheId,
                    val=encoded,
                    status='ok',
                    )
    
            except Exception, e:
                self.logException(4, "call failure")
                self.sendException(
                    e,
                    cmdId=cmdId,
                    cacheId=None,
                    status='exception',
                    excName=e.__class__.__name__,
                    excArgs=e.args
                    )
    
        elif cmd == 'set':
            try:
                val = args[0]
                if cacheId is None:
                    self.interpDict[attr] = val
                else:
                    obj = self.cache[cacheId]
                    self.log(4, "trying to set attribute %s of %s to %s" % (
                                repr(attr), repr(obj), repr(val)))
                    setattr(obj, attr, val)
                self.sendReply(
                    cmdId=cmdId,
                    status='ok',
                    )
                self.log(4, "setattr successful")
            except Exception, e:
                self.logException(4, "setattr failure")
                self.sendException(
                    e,
                    cmdId=cmdId,
                    cacheId=None,
                    status='exception',
                    excName=e.__class__.__name__,
                    excArgs=e.args
                    )
        
    
    #@-node:executeRequest
    #@+node:pickleIfPossible
    def pickleIfPossible(self, obj):
        """
        if object is non-mutable (hashable), try to pickle it
        and return the pickle.
        
        Otherwise, return None
        """
        try:
            hash(obj)
            raw = pickle.dumps(obj)
            return raw
        except:
            return None
    #@-node:pickleIfPossible
    #@+node:do
    def do(self, stmts):
        """
        Executes one or more arbitarary statements.
        
        Arguments:
            - stmts - a string containing one or more newline-separated statements
        """
        self.log(4, "Executing '%s'" % stmts)
    
        self.interp.runsource(stmts)
    
    #@-node:do
    #@+node:dir
    def dir(self):
        """
        List of symbols in interp dict
        """
        return tuple(self.interpDict.keys())
    
    #@-node:dir
    #@+node:doimport
    def doimport(self, modname, attr=None):
        """
        Performs an import
        """
        mod = __import__(modname)
        components = modname.split('.')
        for comp in components[1:]:
            mod = getattr(mod, comp)
        
        #self.interpDict[modname] = mod
        return mod
    
    #@-node:doimport
    #@+node:sendCallback
    def sendCallback(self, callbackId, args, kw):
        """
        Send a callback req to the client, and await result
        """
        # allocate unique message id
        msgId = self.allocId()
    
        # create reply queue
        q = self.callbackReplies[msgId] = Queue.Queue()
    
        # send callback message
        self.sendReply(
            type='callback',
            msgId=msgId,
            callbackId=callbackId,
            args=args,
            kw=kw,
            )
    
        # get response and ditch queue
        ret = q.get()
    
        self.log(4, "got callback response: %s" % repr(ret))
    
        del q
        del self.callbackReplies[msgId]
        
        # and return the return val
        return ret
    
    #@-node:sendCallback
    #@+node:sendException
    def sendException(self, exc, reply=None, **kw):
        """
        Sends an exception back to client
        
        Reply can be passed as a dict argument, or as keywords
        """
        if reply is None:
            reply = {}
        reply.update(kw)
    
        # get a text traceback
        s = StringIO.StringIO()
        traceback.print_exc(file=s)
        s = s.getvalue()
    
        reply['type'] = 'exception'
        reply['status'] = 'exception'
        reply['excName'] = exc.__class__.__name__
        reply['excArgs'] = exc.args
        reply['excTrace'] = s
        print "************* added trace"
        
        self.sendReply(**reply)
    #@-node:sendException
    #@+node:sendReply
    def sendReply(self, reply=None, **kw):
        """
        Sends reply back to client.
        
        Reply can be passed as a dict argument, or as keywords
        """
        if reply is None:
            reply = {}
        reply.update(kw)
    
        # default message type to 'reply'
        reply['type'] = reply.get('type', 'reply')
    
        try:
            raw = pickle.dumps(reply)
        except:
            self.logException(4, "Failed to pickle client reply")
            raise ClientError("failed to pickle reply to client")
    
        self.log(4, "sending reply %s" % repr(reply))
        
        # ok to send
        self.sendLock.acquire()
        self.wfile.write("%s\n%s" % (len(raw), raw))
        self.wfile.flush()
        self.sendLock.release()
    #@-node:sendReply
    #@+node:readbytes
    def readbytes(self, n):
        """
        Guaranteed read of n bytes from client
        """
        chunks = []
        while n > 0:
            try:
                chunk = self.rfile.read(n)
            except:
                chunk = None
            if not chunk:
                raise ClientError("Failed to read command from client")
            chunks.append(chunk)
            n -= len(chunk)
        return "".join(chunks)
    
    #@-node:readbytes
    #@-others
#@-node:class SpiroSession
#@+node:class SpiroUnpickler
class SpiroUnpickler(pickle.Unpickler):
    """
    A butchered unpickler which facilitates the passing of
    object refs between server and client.
    
    It substitutes the locally cached objects in place of
    refs to these objects.
    
    So we intercept the unpickling and, when client sends a req
    containing a SpiroObject instance, we look up our locally
    cached object and substitute that instaed.
    """
    #@    @+others
    #@+node:__init__
    def __init__(self, spirosession, pickled):
        """
        Altered front-end, because this unpickler needs to look
        up locally cached objects
        
        Arguments:
            - spirosession - a SpiroSession object
            - pickled - a string which was picked on the client side
        """
        spirosession.log(4, "creating SpiroUnpickler")
    
        self.spirosession = spirosession
        s = StringIO.StringIO(pickled)
    
        SpiroUnpickler.dispatch[pickle.BUILD] = SpiroUnpickler.load_build
    
        pickle.Unpickler.__init__(self, s)
    
    #@-node:__init__
    #@+node:load_build
    def load_build(self):
        """
        Detect SpiroObject instance refs, and replace these with
        refs to the corresponding locally cached objects.
        
        During unpickling, load_build gets called just as an object is
        reconstituted and its state restored, so this is the perfect time
        to remove SpiroObject refs from the stack and replace them
        with the real local objects
        """
        pickle.Unpickler.load_build(self)
    
        # replace SpiroObject refs with our own cached ref to the object
        if isinstance(self.stack[-1], spiroclient.SpiroObject):
            so = self.stack[-1]
            print "just built a SpiroObject, with %s" % so.__dict__
            print "currently cached objs: %s" % self.spirosession.cache
            self.stack[-1] = self.spirosession.cache[so._cacheId]
            print "switched it with our cached object: %s" % self.stack[-1]
    
        # replace SpiroCallback refs with funcs that send required callback
        elif isinstance(self.stack[-1], spiroclient.SpiroCallback):
            so = self.stack[-1]
            print "Intercepted callback object, creating callback func"
            self.stack[-1] = SpiroCallbackFactory(self.spirosession, so.id)
            print "callback func created"
        pass
    #@-node:load_build
    #@-others

    
#@nonl
#@-node:class SpiroUnpickler
#@+node:class SpiroCallbackFactory
class SpiroCallbackFactory:
    """
    Creates callable proxy objects which send callback messages
    to the client, and await/return the results
    """
    def __init__(self, session, id):
        self.id = id
        self.sendCallback = session.sendCallback
    
    def __call__(self, *args, **kw):
        return self.sendCallback(self.id, args, kw)

#@-node:class SpiroCallbackFactory
#@+node:class SpiroInterpreter
class SpiroInterpreter(code.InteractiveInterpreter):
    """
    For each client connection, we maintain a private namespace within
    an InteractiveInterpreter.
    """    

    def write(self, s):
        """
        Override this to gag the interpreter
        """
        self.spirosession.log(4, "SpiroInterpreter: %s" % s)
        pass
#@-node:class SpiroInterpreter
#@+node:class SpiroLogger
class SpiroLogger:
    """
    Provides normal and exception logging routines
    """
    #@    @+others
    #@+node:__init__
    def __init__(self, **kw):
        """
        Creates a Spiro logger
        
        Keywords:
            - logFile - file to log to (default sys.stdout)
            - logVerbosity - verbosity at which to log
            - logDelay - delay between logging messages - useful if logging
              to stdout and tracking infinite recursion bugs
        """
        self.file = kw.get('logFile', defaultLogfile)
        self.verbosity = kw.get('logVerbosity', defaultLogVerbosity)
        self.delay = kw.get('logDelay', defaultLogDelay)
    
        # create a lock to avoid garbling the log output
        self.lock = threading.Lock()
    #@-node:__init__
    #@+node:log
    def log(self, level, msg, nPrev=0):
        """
        Logs a single message
        """
        # ignore messages that are too trivial for chosen verbosity
        if level > self.verbosity:
            return
    
        # wait if required
        if self.delay:
            time.sleep(self.delay)
    
        self.lock.acquire()
        try:
            # rip the stack
            caller = traceback.extract_stack()[-(2+nPrev)]
            path, line, func = caller[:3]
            path = os.path.split(path)[1]
            full = "%s:%s:%s():\n* %s" % (
                path,
                line,
                func,
                msg.replace("\n", "\n   + "))
            now = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
            msg = "%s %s\n" % (now, full)
        
            if self.file == sys.stdout:
                print msg
            else:
                open(self.file, "a").write(msg+"\n")
        except:
            s = StringIO.StringIO()
            traceback.print_exc(file=s)
            print s.getvalue()
            print "Logger crashed"
        self.lock.release()
    #@-node:log
    #@+node:logException
    def logException(self, level, msg=''):
        """
        Logs an annotated exception dump
        
        Here, the 'msg' arg is stuff to be appended to the logged traceback
        """
        s = StringIO.StringIO()
        traceback.print_exc(file=s)
        self.log(level, "%s\n%s" % (s.getvalue(), msg), 1)
    #@-node:logException
    #@-others
#@-node:class SpiroLogger
#@+node:Exceptions
class ClientError(Exception): pass
#@-node:Exceptions
#@+node:strfuncFactory
def strfuncFactory(obj):
    """
    Returns a callable which, when invoked, calls 'str' on obj
    """
    def _thefunc(_theobj=obj):
        return str(_theobj)
    
    return _thefunc
#@-node:strfuncFactory
#@+node:reprfuncFactory
def reprfuncFactory(obj):
    """
    Returns a callable which, when invoked, calls 'repr' on obj
    """
    def _thefunc(_theobj=obj):
        return repr(_theobj)

    return _thefunc
#@-node:reprfuncFactory
#@+node:log
def log(level, msg, nPrev=0):

    # ignore messages that are too trivial for chosen verbosity
    if logDelay:
        time.sleep(logDelay)

    if level > verbosity:
        return

    loglock.acquire()
    try:
        # rip the stack
        caller = traceback.extract_stack()[-(2+nPrev)]
        path, line, func = caller[:3]
        path = os.path.split(path)[1]
        full = "%s:%s:%s():\n* %s" % (
            path,
            line,
            func,
            msg.replace("\n", "\n   + "))
        now = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        msg = "%s %s\n" % (now, full)
    
        if logfile == sys.stdout:
            print msg
        else:
            file(logfile, "a").write(msg+"\n")
    except:
        s = StringIO.StringIO()
        traceback.print_exc(file=s)
        print s.getvalue()
        print "Logger crashed"
    loglock.release()
#@nonl
#@-node:log
#@+node:logException
def logException(level, msg=''):
    s = StringIO.StringIO()
    traceback.print_exc(file=s)
    log(level, "%s\n%s" % (s.getvalue(), msg), 1)
#@-node:logException
#@+node:myimport
def myimport(name):
    mod = __import__(name)
    components = name.split('.')
    for comp in components[1:]:
        mod = getattr(mod, comp)
    return mod
#@-node:myimport
#@+node:usage
def usage(detailed=0):
    
    print "Usage: %s <options> [<command>]" % sys.argv[0]
    if not detailed:
        print "Run with '-h' to get detailed help"
        sys.exit(0)

    print "spiro - Simple Python Interface for Remote Objects"
    print "Options:"
    print "  -h, -?, --help        - display this help"
    print "  -v, --version         - print program version"
    print "  -l, --logfile=file    - file to log to, default stdout"
    print "  -V, --verbosity=n     - set verbosity to n, default 2, 1==quiet, 4==noisy"
    print "  -y, --logdelay=n      - delay between logging messages (float) (default 0)"
    print "  -H, --listenhost=host - specify host to listen on for client connections"
    print "  -P, --listenport=port - port to listen on for client connections"
    print "  -i, --imports=[i1,..] - comma-separated list of imports"
    print "Commands:"
    print "     (run with no commands to launch SAM server)"
    print "     samserver - runs as a SAM server"
    print "     test - run a suite of self-tests"
    print
    
    sys.exit(0)





#@-node:usage
#@+node:main
def main(argv=None):
    """
    Creates and runs a Spiro server
    
    Arguments:
        - a list of argv-style fields
    """
    # if no args, use sys.argv
    if argv == None:
        argv = sys.argv[1:]

    argc = len(argv)

    try:
        opts, args = getopt.getopt(argv,
                                   "h?vV:H:P:i:l:y:",
                                   ['help', 'version',
                                    'verbosity=', 'logfile=', 'logdelay=',
                                    'host=', 'port=',
                                    'imports=',
                                    ])
    except:
        traceback.print_exc(file=sys.stdout)
        usage("You entered an invalid option")

    serveropts = {}
    serveropts['imports'] = []

    for opt, val in opts:
        if opt in ['-h', '-?', '--help']:
            usage(1)
        elif opt in ['-v', '--version']:
            print "SPIRO server version %s" % version
            sys.exit(0)
        elif opt in ['-V', '--verbosity']:
            serveropts['logVerbosity'] = int(val)
        elif opt in ['-l', '--logfile']:
            serveropts['logFile'] = val
        elif opt in ['-y', '--logdelay']:
            serveropts['logDelay'] = float(val)
        elif opt in ['-H', '--host']:
            serveropts['host'] = val
        elif opt in ['-P', '--port']:
            serveropts['port'] = int(val)
        elif opt in ['-i', '--imports']:
            serveropts['imports'] = val.split(",")
        else:
            usage(0)

    try:
        while 1:
            try:
                server = SpiroServer(**serveropts)
                break
            except socket.error:
                print "waiting 5 secs for socket to clear up"
                time.sleep(5)
        server.run()

        
    except KeyboardInterrupt:
        print "Spiro server terminated by user"
        if sys.platform == 'win32':
            sys.exit(1)
        else:
            try:
                os.kill(os.getpid(), signal.SIGKILL)
            except:
                sys.exit(1)
    except:
        logException(2, "Server Exception")
        sys.exit(1)



#@-node:main
#@+node:Mainline
if __name__ == '__main__':
    main()
#@-node:Mainline
#@-others
#@-node:@file spiroserver.py
#@-leo
