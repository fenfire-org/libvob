# 
# Copyright (c) 2003, Tuomas J. Lukka
# 
# This file is part of Libvob.
# 
# Libvob is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Libvob is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Libvob; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 

# 
# A gl usertest framework

__do_not_delete__ = 1

import sys
import os
from java.lang import System,Runnable,Throwable
System.setProperty("vob.api", "gl")

sys.path.insert(0, ".")

import java

import math
import getopt
import traceback

import vob

import vob.putil.reloader

from org.nongnu.libvob.gl import GL, GLRen, GLCache

from vob.putil.misc import *
from vob.putil import dbg


print "ARGV:",sys.argv
print "DBG:",dbg.short,dbg.long,dbg.all
opts, args = getopt.getopt(sys.argv[1:], 
	dbg.short, 
	dbg.long)
for o,a in opts:
    print "Opt: ",o,a
    if o in dbg.all:
	dbg.option(o,a)


scenefile = args[0]

def loadScenes():
    print "RELOAD"
    vob.putil.reloader.reloadModules()
    vob.putil.demowindow.w = w
    try:
	    exec """
import sys
print sys.modules
import %(scenefile)s
theModule = %(scenefile)s
		"""%globals() in globals(), globals()
	    theModule.run()
    except java.lang.Throwable, t :
	print "ERROR WHILE LOAD/EXEC"
	t.printStackTrace()
    except:
	typ, val, tra = sys.exc_info()
	l = traceback.format_list(traceback.extract_tb(tra))

	print "ERROR WHILE LOAD/EXEC! %s\n%s\n"%(str(typ),str(val)), l


class Bindings(vob.AbstractBinder):
    def keystroke(self, s):
	global showHelp, currentScene
	# print "KEY: '%s'"%s
	if s == "Ctrl-Q":
	    System.exit(43)
	elif s == "Ctrl-R":
	    loadScenes()
	else:
	    vob.putil.usertestutil._key(s)
	# vob.AbstractUpdateManager.chg()
    def mouse(self, e):
	# print "MOUSE: '%s'"%e
	if hasattr(currentScene, "mouse"):
	    currentScene.mouse(e)
    def timeout(self, o):
	print str(System.currentTimeMillis()) + " TIMEOUT ",o
	currentScene.timeout(o)

class Show(vob.AbstractShower):
    def generate(self):
	vs = vob.putil.usertestutil._currentvs
	if vs == None:
	    vs = w.createVobScene()
	    vs.put(background((1,1,0)))
	return vs


class Main(Runnable):
    def run(self):
	b,s = (Bindings(), Show())
	global w
	w = gfxapi.createWindow()
	# w.setLocation(0,0,1600,1200)
	w.setLocation(0,0,1024,768)
	scr = vob.Screen(w, b, s)
	loadScenes()
	vob.AbstractUpdateManager.addWindow(scr)
	vob.AbstractUpdateManager.chg()
	

gfxapi = vob.GraphicsAPI.getInstance()
gfxapi.startUpdateManager(Main())

