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
# A gl demo framework that can reload the given file at will.
#
# The target size for demos is 1024x768, since that is
# a reasonable lowest common denominator. 
#
# Our demo animations will also be in that size.
#
# The current screen size can be queried from the vobscene
# and the user is free to resize the window; demos shouldn't
# automatically resize the window.
#
# The demos should make an effort to adapt to any window size.

__do_not_delete__ = 1

import sys
import os
from java.lang import System,Runnable,Throwable
if System.getProperty("vob.api") == None:
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
from vob.putil.commander import AwtCommander



print "ARGV:",sys.argv
print "DBG:",dbg.short,dbg.long,dbg.all
opts, args = getopt.getopt(sys.argv[1:], 
	dbg.short+'C', 
	dbg.long+['commander'])
for o,a in opts:
    print "Opt: ",o,a
    if o in dbg.all:
	dbg.option(o,a)
    if o in ("-C", "--commander"):
        AwtCommander(globals())

currentScene = None

def loadSubmodules(theModule):
    """Given a package, load all its submodules.
    Return all touched modules.
    """
    list = [theModule]
    f = getattr(theModule,"__file__", None)
    if not f: return list
    if f[-11:] != "__init__.py": return list
    f = f[:-11]
    files = os.listdir(f)
    print files
    for n in files:
	if n[-3:] == ".py":
	    print "Loading",n
	    name = n[:-3]
	    a = getattr(theModule, name)
	    list.extend(loadSubmodules(a))
    return list

def firstDocLine(obj):
    if hasattr(obj, "__doc__") and obj.__doc__ != None:
	return obj.__doc__.splitlines()[0]
    else:
	return "XXX UNDOC "

globalSceneMap = { }

def loadScenes():
    print "RELOAD"
    global globalScenes
    global globalHelp, globalSceneMap, globalSceneHelp
    global currentScene
    currentScene = None

    if 1:
	print "VOB: ",vob
	vob.putil.reloader.reloadModules()
    try:
	vob.putil.demowindow.w = w
	if "." not in scenefile or "/" in scenefile:
	    exec open(scenefile) in globals(), globals()
	    if currentScene == None:
		currentScene = Scene()
                currentScene.anim = anim
	else:
	    print "No file found, trying module.",scenefile
	    exec """
import sys
print sys.modules
import %(scenefile)s
theModule = %(scenefile)s
		"""%globals() in globals(), globals()
	    print theModule
	    # Now, due to lazy loading, we can't find out
	    # the names of the submodules directly.
	    # Have to use this trickery.
	    mods = loadSubmodules(theModule)
	    globalScenes = []
	    for mod in mods:
		if getattr(mod, "Scene", None):
                    sc = mod.Scene()
                    sc.anim = anim
		    globalScenes.append(sc)
	    print globalScenes

            scenes = [ (firstDocLine(scene),scene) for scene in globalScenes ]
            scenes.sort()
            globalScenes = [ scene[1] for scene in scenes ]

	    if len(globalScenes) == 1:
		currentScene = globalScenes[0]
		globalSceneMap = { }
		globalSceneHelp = "(no global keys)\n"
		return

	    globalHelp = theModule.__doc__
	    globalSceneMap = {
		"F1": GlobalScene()
	    }
	    globalSceneHelp = "\nGlobal keys:\n?: help (press in each F* scene separately)\nq: quit\nr: reload\nF1: Overall help\n"
	    for i in range(0,len(globalScenes)):
		key = "F%s"%(i+2)
		globalSceneMap[key] = globalScenes[i]
		globalSceneHelp += "%s: %s\n"%(key,
			firstDocLine(globalScenes[i]))

	    print globalScenes, globalSceneHelp, globalSceneMap

	    currentScene = globalSceneMap["F1"]
	    GlobalScene.__doc__ = globalHelp

	    print "Global help:",globalHelp,"CUR",currentScene.__doc__
	    
    except java.lang.Throwable, t :
	print "ERROR WHILE LOADING JAVA"
	t.printStackTrace()
    except:
	print "ERROR WHILE LOADING"
	traceback.print_exc()

class GlobalScene:
    def key(self, k):
	pass
    def scene(self, vs):
	vs.map.put(vob.putil.misc.background((.3, .7, .6)))
	global showHelp
	showHelp = 1

def globalkey(k):
    pass

usingNormalBindings = 1
chgAfterKeyEvent = 1

showHelp = 0


class Bindings(vob.AbstractBinder):
    def keystroke(self, s):
	global showHelp, currentScene
	# print "KEY: '%s'"%s
	if usingNormalBindings:
	    if s == "q" or s == "Q" or s == "Ctrl-Q":
		System.exit(43)
	    elif s == "r" or s == "R" or s == "Ctrl-R":
		loadScenes()
	    elif s == "?":
		showHelp = 1-showHelp
		vob.AbstractUpdateManager.setNoAnimation()
	    elif globalSceneMap.has_key(s):
		currentScene = globalSceneMap[s]
		vob.AbstractUpdateManager.setNoAnimation() 
		showHelp = 0
	    elif not globalkey(s):
		logger.key(currentScene, s)
                #currentScene.key(s)
	else:
	    if s == "Ctrl-R":
		loadScenes()
            elif currentScene:
		logger.key(currentScene, s)
                #currentScene.key(s)
	    elif s == "Ctrl-Q":
		System.exit(43)

        if not chgAfterKeyEvent: return
        vob.AbstractUpdateManager.chg()
        
    def mouse(self, e):
	# print "MOUSE: '%s'"%e
	if hasattr(currentScene, "mouse"):
            logger.mouse(currentScene, e)
	    #currentScene.mouse(e)
    def timeout(self, o):
	print str(System.currentTimeMillis()) + " TIMEOUT ",o
	currentScene.timeout(o)

def addHelp(vs):
    def d(obj): 
        if hasattr(obj, "getdoc"):
            return obj.getdoc(currentScene)
	o = getattr(obj, "__doc__", "")
	if not o: return ""
	return o
    help = d(currentScene.__class__) + d(currentScene.key) + globalSceneHelp
    print "Doc: ",help
    lines = help.split("\n")
    ys = min(vs.size.height / len(lines), 40)
    cury = 0
    style = vs.gfxapi.getTextStyle("sans", 0, 25)
    vobs = []
    for l in lines:
	vobs.append((
	    vob.vobs.TextVob(style, l), str(("HL",cury)), cury))
	cury += ys
    # Draw white frame
    vs.map.put(getDListNocoords("""
	Color 1 1 1 1
	Disable BLEND
    """))
    s = 1.0
    for v in vobs:
	vs.put(v[0], "1"+v[1], -45, 0 + s, v[2] + s, ys, ys)
	vs.put(v[0], "2"+v[1], -45, 0 - s, v[2] + s, ys, ys)
	vs.put(v[0], "3"+v[1], -45, 0 + s, v[2] - s, ys, ys)
	vs.put(v[0], "4"+v[1], -45, 0 - s, v[2] - s, ys, ys)
    vs.map.put(getDListNocoords("""
	Color 0 0 0 1
	Enable BLEND
    """))
    for v in vobs:
	vs.put(v[0], v[1], -45, 0, v[2], ys, ys)
    vs.map.put(getDListNocoords("""
	Color 0 0 0 1
	Disable BLEND
    """))



class Show(vob.AbstractShower):
    def generate(self, vs):
	# print "GENERATE"
	if not currentScene:
	    print "No scene\n"
	    return vs
        otherVS = currentScene.scene(vs)

        # it's possible to replace the filled scene by other,
        # loom vobmatcher etc..
        if otherVS != None and otherVS != vs: vs = otherVS 
        if showHelp:
            addHelp(vs)
        return vs


# Disabled - there's a problem so
# We'll define return value of scene()
# to mean replacing
#def replaceNewScene(vs):
#    global replacingScene
#    vob.AbstractUpdateManager.setNoAnimation()
#    replacingScene = vs

class Main(Runnable):
    def run(self):
	b,s = (Bindings(), Show())
	global w, anim
	w = gfxapi.createWindow()

	geometry = java.lang.System.getProperty("vob.windowsize", "1024x768")
	(width, height) = geometry.split("x")

	w.setLocation(0,0,int(width),int(height))
        anim = vob.impl.WindowAnimationImpl(w,b,s)
	loadScenes()
	vob.AbstractUpdateManager.addWindow(anim)
	vob.AbstractUpdateManager.chg()

def run():
    global logger
    logger = vob.putil.logger.Logger()

    global gfxapi
    gfxapi = vob.GraphicsAPI.getInstance()
    gfxapi.startUpdateManager(Main())

scenefile = args[0]

if __name__ == "__main__":
    run()
