"""
Use this as a template for your own SPIRO server program.

Import all the java packages you need, import spiroserver
and instantiate a SpiroServer class, and run it.
"""

import sys
import spiroserver

# config settings

serverHost = "127.0.0.1"
serverPort = 9091
verbosity = 2
logfile = sys.stdout

# -----------------------------------------------

# Put here all the java imports you need. If your clients
# need any third party java packages/classes/functions,
# you'll have to import them here

# remove these - this is just what I'm using on a current project
# stick in your own needed imports instead
import net.i2p
import net.i2p.client
import net.i2p.crypto

# list the roots of these packages here, comma-separated, into a string
myimports = "net.i2p"

# -----------------------------------------------

# now set up your SPIRO server, perhaps subclassing
# it if you need to.

myserver = spiroserver.SpiroServer(
    host=serverHost,
    port=serverPort,
    logFile=logfile,
    logVerbosity=verbosity,
    imports=myimports,
    )

myserver.run()

# another alternative - invoke main() with a list of argv-style args
# which will take care of instantiating/running the SpiroServer

# spiroserver.main([
#   "-H", serverHost,
#   "-P", str(serverPort),
#   "-l", logfile,
#   "-V", str(verbosity),
#   "-i", myimports,
#   ])
