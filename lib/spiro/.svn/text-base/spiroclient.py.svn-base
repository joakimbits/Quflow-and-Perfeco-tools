#!/usr/bin/env python
#@+leo-ver=4
#@+node:@file spiroclient.py
#@@first
"""
Client side of Spiro - manages connections to Spiro server
"""
#@+others
#@+node:imports
import sys, os, time, Queue, socket, thread, threading, StringIO, traceback, getopt, pickle
from pdb import set_trace

#@-node:imports
#@+node:globals
host = "127.0.0.1"
port = 9091

# set this to higher values to see what's going on
verbosity = 2

loglock = threading.Lock()
logfile = sys.stdout
logDelay = 0
#@-node:globals
#@+node:class SpiroClient
class SpiroClient:
    """
    Abstracts a connection to a spiro server
    """
    #@    @+others
    #@+node:attributes
    _host = host
    _port = port
    
    _localattributes = [
        '_client',
        '_name',
        '_objPath',
        '_cacheId',
        '__getinitargs__',
        '__getstate__',
        '__slots__',
    
        '_host', '_port', '_threaded', '_isRunning', '_replyQueues', '_replyQueuesLock',
        '_nextId', '_nextIdLock', '_sendLock', '_sock', '_sendLock', '_verbosity',
        '_callbacks',
        ]
    
    _badAttributes = [
        '__members__',
        '__methods__',
        '__getstate__',
        '__getinitargs__',
        '__slots__',
        ]
    
    _threaded = 1
    
    _verbosity = verbosity
    #@-node:attributes
    #@+node:__init__
    def __init__(self, sessName, **kw):
        """
        Creates the spiro client
    
        Arguments:
            - sessName - name of session - allows multiple clients to share a common
              namespace on server.
        
        Keywords:
            - host - hostname of spiro server
            - port - port of spiro server
        """
        # save name
        self._name = sessName
    
        # get optional host/port for client and i2cp
        self._host = kw.get('host', self._host)
        self._port = int(kw.get('port', self._port))
        self._threaded = int(kw.get('threaded', self._threaded))
        self._verbosity = int(kw.get('verbosity', self._verbosity))
    
        # set up session data and locks
        self._isRunning = 1
        self._replyQueues = {}
        self._replyQueuesLock = threading.Lock()
        self._nextId = 0
        self._nextIdLock = threading.Lock()
        self._sendLock = threading.Lock()
    
        # session-global registry of callbacks, keyed by unique number
        self._callbacks = {}
        
        # create a server connection
        self._sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._sock.connect((self._host, self._port))
    
        # and register
        self._sock.send(sessName+"\n")
    
        # launch a thread for asynchronous message handling
        if self._threaded:
            thread.start_new_thread(self._rxThread, ())
    #@-node:__init__
    #@+node:_rxThread
    def _rxThread(self):
        """
        Thread which receives incoming replies and callbacks.
        In the case of replies, files them away in self._replyQueues
        """
        try:
            while self._isRunning:
    
                reply = self._getReply()
    
                self._log(4, "reply=%s" % reply)
    
                # not a callback - treat as regular reply message
                id = reply['cmdId']
                self._replyQueuesLock.acquire()
        
                self._log(5, "writing reply back to queue")
        
                self._replyQueues[id].put(reply)
                self._replyQueuesLock.release()
    
        except:
            self._logException(4, "receiver thread crashed")
    #@-node:_rxThread
    #@+node:_callbackThread
    def _callbackThread(self, msg):
        """
        Executes a callback, and sends back a reply, within separate thread
        """
    
        #    type='callback',
        #    msgId=msgId,
        #    callbackId=callbackId,
        #    args=args,
        #    kw=kw,
    
        log(4, "got callback msg: %s" % msg)
        
        msgId = msg['msgId']
        callbackId = msg['callbackId']
        args = msg['args']
        kw = msg['kw']
        
        func = self._callbacks.get(callbackId, None)
    
        if func:
            try:
                log(4, "Invoking local callback function")
                ret = func(*args, **kw)
                status = 'ok'
            except:
                ret = None
                status = 'exception'
    
            self._sendCommand(msgId, None, 'callbackreply', '', ret)
    #@-node:_callbackThread
    #@+node:_getReply
    def _getReply(self):
        """
        Fetch the next reply thing from server
        """
        while 1:
            # get reply
            self._log(5, "listening for reply from server")
        
            size = int(self._readline())
            raw = self._readbytes(size)
        
            self._log(5, "got raw reply, unpacking")
        
            try:
                reply = pickle.loads(raw)
            except:
                self._logException(4, "Failed to unpickle server reply")
                raise
        
            val = reply.get('val', None)
            if val is not None:
                try:
                    dec = pickle.loads(val)
                    reply['val'] = dec
                    self._log(4, "successfully decoded value")
        
                    # tell server to ditch the object because we managed to decode locally
                    self._sendCommand(-1, reply['cacheId'], "goodbye", None)
        
                except:
                    del reply['val']
                    self._log(4, "failed to decode value")
            else:
                if reply.has_key('val'):
                    del reply['val']
                self._log(4, "no val field in reply")
    
            msgType = reply['type']
    
            # delegate to a thread if this is a callback
            if msgType == 'callback':
                self._log(4, "got callback, starting thread")
                thread.start_new_thread(self._callbackThread, (reply,))
                continue
        
            break
        
        return reply
    
    #@-node:_getReply
    #@+node:_readline
    def _readline(self):
        """
        Reads a full line from server socket
        and returns it (without trailing newline)
        """
        chars = []
        while 1:
            try:
                char = self._sock.recv(1)
            except:
                char = ''
            if not char:
                self._logException(4, "failed to read response line")
                raise ServerError("Failed to read server reply header")
            if char == '\n':
                break
            chars.append(char)
        return "".join(chars)
    
    #@-node:_readline
    #@+node:_readbytes
    def _readbytes(self, n):
        """
        Guaranteed to read n bytes from server, or raise exception
        """
        chunks = []
    
        while n > 0:
            try:
                chunk = self._sock.recv(n)
            except:
                chunk = None
            if not chunk:
                raise ServerError("Failed to read response from server")
            chunks.append(chunk)
            n -= len(chunk)
        return "".join(chunks)
    
    #@-node:_readbytes
    #@+node:_sendbytes
    def _sendbytes(self, buf):
        """
        Guaranteed send of all buffer, or exception
        """
        self._sendLock.acquire()
        while buf:
            n = self._sock.send(buf)
            if n <= 0:
                raise ServerError("Failed to send server cmd")
            buf = buf[n:]
        self._sendLock.release()
    #@-node:_sendbytes
    #@+node:callback
    def callback(self, func):
        """
        Registers a callable as a Spiro callback and wraps it
        in a callback wrapper
        """
        id = self._allocId()
        cb = SpiroCallback(id)
        self._callbacks[id] = func
        return cb
    #@-node:callback
    #@+node:_allocId
    def _allocId(self):
        """
        Allocates a unique id
        """
        self._nextIdLock.acquire()
        n = self._nextId
        self._nextId += 1
        self._nextIdLock.release()
        return n
    
    
    #@-node:_allocId
    #@+node:__getattr__
    def __getattr__(self, attr):
        """
        performs a getattr on some remote thing
        """
        # spit on bad attributes
        if attr in self._badAttributes:
            raise AttributeError
    
        self._log(5, "SpiroClient: remotely fetching attribute %s" % attr)
    
        resp = self._serverCommand(None, 'get', attr, 0)
        self._log(5, "got reply from server for attribute fetch '%s'" % attr)
    
        status = resp['status']
    
        if resp['status'] == 'notfound':
            raise AttributeError("remote name or attribute '%s' does not exist" % attr)
    
        if status == 'exception':
            excName = resp['excName']
            excArgs = resp['excArgs'] + (resp['excTrace'],)
            exc = SpiroExceptionFactory(excName)
            raise exc(*excArgs)
    
        if status == 'ok':
    
            if resp.has_key('val'):
                self._log(4, "caching and returning literal value")
                #self.__dict__[attr] = resp['val']
                return resp['val']
    
            self._log(4, "wrapping returned thing as a SpiroObject")
    
            cacheId = resp['cacheId']
            obj = SpiroObject(self, cacheId, attr)
    
            self._log(5, "SpiroClient: adding new cached local attrib %s" % attr)
            self.__dict__[attr] = obj
            
            self._log(5, "SpiroClient: added new cached local attrib %s" % attr)
            return obj
    
        else:
            raise AttributeError("No such name %s" % attr)
    #@-node:__getattr__
    #@+node:_serverCommand
    def _serverCommand(self, cacheId, cmd, attr, *args, **kw):
        """
        Executes on the server
        """
        cmdId = self._allocId()
        self._replyQueuesLock.acquire()
        q = self._replyQueues[cmdId] = Queue.Queue()
        self._replyQueuesLock.release()
    
        self._log(5, "cmdId=%s cacheId=%s cmd=%s args=%s kw=%s" % (cmdId, cacheId, cmd, args, kw))
    
        # pickle up the call
        self._log(5, "creating remote call object")
    
        self._sendCommand(cmdId, cacheId, cmd, attr, *args, **kw)
    
        self._log(5, "awaiting reply")
    
        if self._threaded:
            # now wait for the reply
            reply = q.get()
    
            # and toss the queue
            self._replyQueuesLock.acquire()
            del self._replyQueues[cmdId]
            self._replyQueuesLock.release()
        else:
            reply = self._getReply()
        
        self._log(5, "got reply %s" % reply)
    
        # and hand back reply
        # (processing needed)
        return reply
    #@-node:_serverCommand
    #@+node:__setattr__
    def __setattr__(self, attr, val):
        """
        """
        self._log(4, "setting attribute %s to %s" % (attr, val))
    
        if attr in self._localattributes:
            self.__dict__[attr] = val
        else:
            # job for the server
            self._log(4, "trying to set attribute %s on server-side" % attr)
            resp = self._serverCommand(None, 'set', attr, val)
    
            status = resp['status']
            
            if status == 'exception':
                excName = resp['excName']
                excArgs = resp['excArgs'] + (resp['excTrace'],)
                exc = SpiroExceptionFactory(excName)
                raise exc(*excArgs)
            elif status == 'ok':
                return # fine
    #@-node:__setattr__
    #@+node:_sendCommand
    def _sendCommand(self, cmdId, cacheId, cmd, attr, *args, **kw):
        """
        """
        req = {
            'cmdId': cmdId,
            'cacheId': cacheId,
            'cmd': cmd,
            'attr':attr,
            'args': args,
            'kw': kw,
            }
    
        self._log(4, "sending remote call:\n%s" % req)
    
        try:
            raw = pickle.dumps(req)
        except:
            self._logException(2, "Failed to pickle up for server cmd %s" % req)
            raise BadAction("Failed to pickle")
        
        
        # ok to send to server
        self._sendbytes("%s\n%s" % (len(raw), raw))
    #@-node:_sendCommand
    #@+node:__str__
    def __str__(self):
        return "<Spiro client object connected to %s:%s>" % (self._host, self._port)
    
    #@-node:__str__
    #@+node:__repr__
    def __repr__(self):
        return repr(str(self))
    #@-node:__repr__
    #@+node:_log
    def _log(self, level, msg, nprev=1):
        
        if level > self._verbosity:
            return
        log(level, msg, nprev)
    #@-node:_log
    #@+node:_logException
    def _logException(self, level, msg=''):
    
        if level > self._verbosity:
            return
    
        s = StringIO.StringIO()
        traceback.print_exc(file=s)
        self._log(level, "%s\n%s" % (s.getvalue(), msg), 1)
    
    #@-node:_logException
    #@-others
#@-node:class SpiroClient
#@+node:class SpiroObject
class SpiroObject:
    """
    Manages a local ref to a remote Spiro object
    """
    #@    @+others
    #@+node:attributes
    # list of attributes which may be set locally
    _localattributes = [
        '_client',
        '_objPath',
        '_cacheId',
        '_unpickled',
        '__getinitargs__',
        '__getstate__',
        '__slots__',
        ]
    
    _badAttributes = [
        #'__members__',
        #'__methods__',
        '__getinitargs__',
        '__getstate__',
        '__setstate__',
        '__slots__',
        #'_client',
        ]
    
    
    #@-node:attributes
    #@+node:__init__
    def __init__(self, client, cacheId, objPath, **kw):
        """
        Don't instantiate this directly - instances are created
        by SpiroClient when remote refs are passed back
        """
        log(5, "SpiroObject.__init__: client=%s (%s)" % (repr(client), client.__class__))
    
        self._client = client
        self._cacheId = cacheId
        self._objPath = objPath
    
        # flag that indicates whether this object has been unpickled
        self._unpickled = 0
    #@-node:__init__
    #@+node:Pickling Frig-o-matic
    def __getstate__(self):
        """
        for pickling, pass only the attribs which are piclkable
        """
        #log(4, "called")
    
        return {
            '_cacheId': self._cacheId,
            '_objPath': self._objPath,
            '_unpickled': 1,
            }
    
    
    
    #@-node:Pickling Frig-o-matic
    #@+node:__setattr__
    def __setattr__(self, attr, val):
        """
        Intercepts attribute sets, handling internal attributes
        locally, but forwarding all other attribute set actions
        over to the server
        """
        if attr in self._localattributes:
            if attr != '_client':
                self._log(4, "trying to set attribute %s locally" % attr)
            self.__dict__[attr] = val
        else:
            # job for the server
            self._log(4, "trying to set attribute %s on server-side" % attr)
            resp = self._client._serverCommand(self._cacheId, 'set', attr, val)
    
            status = resp['status']
            
            if status == 'exception':
                excName = resp['excName']
                excArgs = resp['excArgs'] + (resp['excTrace'],)
                exc = SpiroExceptionFactory(excName)
                raise exc(*excArgs)
    
            elif status == 'ok':
                return # fine
    #@-node:__setattr__
    #@+node:__getattr__
    def __getattr__(self, attr):
        """
        This will pick up all manner of object accesses
        """
        # spit on bad attributes
        if attr in self._badAttributes:
            log(4, "Bad attribute %s" % attr)
            raise AttributeError
    
        log(5, "SpiroObject: remotely fetching attribute %s" % attr)
    
        resp = self._client._serverCommand(self._cacheId, 'get', attr, 0)
    
        # return literal value if we managed to unpickle it
        if resp.has_key('val'):
            #self.__dict__[attr] = resp['val']
            return resp['val']
    
        status = resp['status']
    
        if status == 'exception':
            excName = resp['excName']
            excArgs = resp['excArgs'] + (resp['excTrace'],)
            exc = SpiroExceptionFactory(excName)
            raise exc(*excArgs)
    
        if status == 'ok':
            cacheId = resp['cacheId']
            obj = SpiroObject(self._client, cacheId, None)
            self._log(5, "SpiroObject: adding new cached local attrib %s" % attr)
            self.__dict__[attr] = obj
            self._log(5, "SpiroObject: added new cached local attrib %s" % attr)
            return obj
    
        else:
            raise AttributeError("Remote Spiro object has no attribute '%s'" % attr)
    #@-node:__getattr__
    #@+node:__call__
    def __call__(self, *args, **kw):
        """
        Sends a call cmd to server obj
        """
        res = self._client._serverCommand(self._cacheId, 'call', None, *args, **kw)
    
        status = res['status']
    
        if status == 'exception':
            excName = res['excName']
            excArgs = res['excArgs'] + (res['excTrace'],)
            exc = SpiroExceptionFactory(excName)
            raise exc(*excArgs)
    
        # return literal value if we managed to unpickle it
        if res.has_key('val'):
            #self.__dict__[attr] = resp['val']
            return res['val']
    
        return SpiroObject(self._client, res['cacheId'], None)
    #@-node:__call__
    #@+node:__del__
    def __del__(self):
        """
        Tells the server we're going away and to please stop caching us
        """
        try:
            if self._unpickled:
                self._client._sendCommand(-1, self._cacheId, "goodbye", None, 0)
        except:
            logException(4, "dammit")
    
    #@-node:__del__
    #@+node:__str__
    def nn__str__(self):
        """
        """
        # get remote representation
    
        if self._objPath:
            return "<Spiro remote object '%s' at %s:%s>" % (
                self._objPath, self._client._host, self._client._port)
        else:
            return "<Spiro remote object at %s:%s>" % (
                self._client._host, self._client._port)
    #@-node:__str__
    #@+node:__repr__
    def x__repr__(self):
        return repr(str(self))
    
    #@-node:__repr__
    #@+node:_log
    def _log(self, level, msg):
        
        try:
            self._client._log(level, msg)
        except:
            print "SpiroObject._log screwup: _client=%s (%s)" % (
                repr(self._client),
                self._client.__class__,
                )
            log(4, msg)
    
    #@-node:_log
    #@+node:_logException
    def _logException(self, level, msg):
        
        self._client._logException(level, msg)
    #@-node:_logException
    #@-others
#@-node:class SpiroObject
#@+node:class SpiroCallback
class SpiroCallback:
    """
    An abstract wrapper for callback objects
    """
    def __init__(self, id):
        self.id = id
#@-node:class SpiroCallback
#@+node:SpiroExceptionFactory
def SpiroExceptionFactory(excName):
    """
    Creates an exception class with a given name, which
    also subclasses from SpiroObject
    """
    class mySpiroException(Exception):
        pass
    mySpiroException.__name__ = excName
    return mySpiroException
#@-node:SpiroExceptionFactory
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
#@+node:test
def test():

    print "connecting to Spiro server..."
    c = SpiroClient("fred", threaded=0, verbosity=5)

    print "Importing java package"
    c.do("import java")
    
    print "Grabbing java's sqrt() function..."
    sqrt = c.java.lang.Math.sqrt

    print "Representation of this function:"
    print sqrt

    print "Executing sqrt(16)"
    print "got %s" % sqrt(16)
    
#@-node:test
#@+node:Mainline
if __name__ == '__main__':
    test()
#@-node:Mainline
#@+node:Exceptions
class ServerError(Exception): pass

class BadAction(Exception): pass
#@-node:Exceptions
#@-others
#@-node:@file spiroclient.py
#@-leo
