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
# A gl screenshot script framework
# For reasons of FSAA &c, uses currently only the main window -
# Don't put any window in front ;)

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

from org.nongnu.libvob.gl import GL, GLRen, GLCache

from vob.putil.misc import *
from vob.putil import dbg

class SnapshotLoop:
    pass

# The one image to loop making, waiting for a key to reload and continue
loop = None

print "ARGV:",sys.argv
print "DBG:",dbg.short,dbg.long,dbg.all
opts, args = getopt.getopt(sys.argv[1:], 
	dbg.short, 
	dbg.long+["loop="])
for o,a in opts:
    print "Opt: ",o,a
    if o in dbg.all:
	dbg.option(o,a)
    elif o == "--loop":
	loop = a
	


scenefile = args[0]

def loadScenes():
    vob.putil.reloader.reloadModules()
    vob.putil.demowindow.w = w
    try:
	    exec """
import sys
import %(scenefile)s
theModule = %(scenefile)s
		"""%globals() in globals(), globals()
	    theModule.run()
    except java.lang.Throwable, t :
	print "ERROR WHILE LOAD/EXEC"
	t.printStackTrace()

keyhit = 0

class Bindings(vob.AbstractBinder):
    def keystroke(self, s):
	print "KEY: ",s
	global keyhit
	keyhit = 1
	if s == "Ctrl-Q":
	    System.exit(43)
    def mouse(self, e):
	return
    def timeout(self, o):
	return

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
	try:
	    if loop:
		while 1:
		    try:
			loadScenes()
		    except SnapshotLoop:
			print "Continuing"
			continue
		    break
	    else:
		loadScenes()
	except:
	    typ, val, tra = sys.exc_info()
	    l = traceback.format_list(traceback.extract_tb(tra))

	    print "ERROR WHILE LOAD/EXEC! %s\n%s\n"%(str(typ),str(val)), l
	System.exit(0)
	vob.AbstractUpdateManager.addWindow(scr)
	vob.AbstractUpdateManager.chg()
	

gfxapi = vob.GraphicsAPI.getInstance()
gfxapi.startUpdateManager(Main())


