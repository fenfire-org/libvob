# 
# Copyright (c) 2003, Tuomas J. Lukka and Janne Kujala
# 
# This file is part of Gzz.
# 
# Gzz is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Gzz is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Gzz; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 

from __future__ import nested_scopes

import vob.paper.papermill
import vob.paper.textures
from vob.paper.papermill import ThePaperMill

from types import ClassType
from vob.putil import saveanim

import java

from vob.putil.misc import *
from vob.putil import demowindow

def r(sc, filename):
    vs = demowindow.w.createVobScene()
    sc.scene(vs)
    demowindow.w.renderStill(vs, 0)
    saveanim.saveframe(filename, demowindow.w)

def makeScreenshots():
    demowindow.w.setLocation(0,0,1400,1000)
    ps = MultiPaperScene()
    ps.seed = 199871850
    ps.initpaper()
    r(ps, "shots/multipaper.png")

    ps = BasisScene()
    r(ps, "shots/paperbasistex.png")



class ColorMapScene:
    def __init__(self):
        self.bgcolor = (0,0,0)
        self.angle = 0

        AbstractUpdateManager.defaultAnimationTime = 3000
        AbstractUpdateManager.fractCalc = AbstractUpdateManager.LinearCalculator()

    def key(self, k):
        pass
    def mouse(self, ev):
	if ev.getID() == ev.MOUSE_CLICKED:
	    self.angle = ev.getX()
	    ev.getY()
            
	    AbstractUpdateManager.chg()

    def scene(self, vs):
	vs.put( background(self.bgcolor))

	cs1 = vs.coords.affine(0, "1", 0, -90, 0, 1, 0, 0, 1)

        vs.put( getDListNocoords("""
            PointSize 1
            Disable TEXTURE_2D
            PushMatrix
            Translate 600 450 100
            Scale 8 8 16
        """))
	rotate(vs, "rot", self.angle, -1, 1, 1)
        vs.map.put(cmap, cs1)
	poptrans(vs, "rot")
        vs.put( getDListNocoords("""
            PopMatrix
        """))

def dobenchmark(w, vs):
    global benchmark
    if benchmark:
        benchmark = 0
        iters = 100
        t = w.timeRender(vs, iters)
        print "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Time of", iters, "renders:", t, "s"

zoom = 900.0
passmask = [1,1,1,1]

def getpaper(seed, optimized=0):
    if optimized:
	pap = ThePaperMill().getOptimizedPaper(seed, passmask=passmask)
    else:
	pap = ThePaperMill().getPaper(seed, passmask=passmask)
    print "Pq: ",seed
    return GLRen.createPaperQuad(pap, -1, -1, +1, 1, 0)

# A jython test program for papers
# Load using gldemo.py


# Jython doesn't have boolean. (future?)
False = 0
True = 1

rng = java.util.Random()

def toggleOpengl11(*args):
    org.nongnu.libvob.gl.PaperOptions.use_opengl_1_1 = \
	    not org.nongnu.libvob.gl.PaperOptions.use_opengl_1_1
    print 'Use of OpenGL 1.1:', org.nongnu.libvob.gl.PaperOptions.use_opengl_1_1 

def togglePapermillDebug(*args):
    org.nongnu.libvob.paper.papermill.dbg = not org.nongnu.libvob.paper.papermill.dbg
    print "Papermilldbg ",org.nongnu.libvob.paper.papermill.dbg

from vob.putil.demokeys import *
keys = [
    SlideLog("zoom", 4.5, "zoom factor", "<", ">"),
    Action("Reload textures", "t", lambda *args: retexture()),
    Action("Toggle using OpenGL 1.1 -compatibility mode", "O", toggleOpengl11),
    Action("Toggle papermill debug", "d", togglePapermillDebug),
]

benchmark = 0
def globalkey(k):
    global benchmark
    global currentScene
    global zoom
    if k >= "F5" and k <= "F8":
        i = int(k[1]) - 5
        global passmask
        passmask[i] = not passmask[i]
	print passmask
        currentScene.initpaper()
#    if k == "c":
#        global cmap
#        x,y = 0,0
#        xs,ys = 200,200
#        pixels = GL.createByteVector(xs*ys*3)
#        pixels.readFromBuffer_ubytes(demowindow.w.window, "FRONT", x, y, xs, ys, "RGB");
#
#        print "Read pixels"
#        cmap = GLRen.createLABColorMap(pixels, xs * ys);
#        currentScene = ColorMapScene()
#	AbstractUpdateManager.setNoAnimation()
#    if k == "C":
#        currentScene = ColorMapScene()
#	AbstractUpdateManager.setNoAnimation()
#    if k == "p":
#	makeScreenshots()



