# 
# Copyright (c) 2003, Tuomas J. Lukka
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


import vob
from vob.putil.misc import *
from vob.putil.demokeys import *

vob.AbstractUpdateManager.defaultAnimationTime = 1500
vob.AbstractUpdateManager.fractCalc = vob.AbstractUpdateManager.LinearCalculator(0)

class Scene:
    def __init__(self):
	self.fisheye = vob.view.FisheyeState(1, .1, 100, 1, 10000)
	self.color = GLCache.getCallList("""
	     Color 0 0 0
	""")
	self.mode = [GLCache.getCallList("""
	     PolygonMode FRONT_AND_BACK LINE
	    """), 
	    GLCache.getCallList("""
	     PolygonMode FRONT_AND_BACK FILL
	    """)]



	self.pap = vob.gl.PaperMill.getInstance().getPaper(42)

	self.key = KeyPresses( self,
	    ListIndex("curmode", "mode", 0, "Toggle polymode", "l", "L"),
	    Toggle("usepaper", 1, "use paper", "p"),
	    SlideLog("dicelen", 5,  "Dice length", 'd', 'D'),
	    SlideLog("dicelen2", 5,  "Dice length", 'f', 'F'),
	    SlideLin("x", 0, 100, "x coord", "Left", "Right"),
	    SlideLin("y", 0, 100, "y coord", "Up", "Down"),
	)

	self.repl = 0
    def key(self, k):
        pass
    def scene(self, vs):
	if self.repl:
	    self.repl = 0
	    return self.vs
	self.vs = vs
	vs.put( background((.0,.7,.6)))

	size = vs.getSize()

	cs = vs.translateCS(0, "A", size.width / 2, size.height / 2)

	cs2 = self.fisheye.getCoordsys(vs, cs, "B")
	vs.matcher.add(cs, cs2, "X")
	cs = cs2

	vs.map.put(self.mode[self.curmode])
	
	if self.usepaper:
	    s = 1000
	    cs = vs.orthoCS(cs, "C", 0, self.x + -s/2, self.y + -s/1.5, s, s)
	    paper = GLRen.createFixedPaperQuad(self.pap, 0, 0, 10, 10, 2,
		self.dicelen, self.dicelen2, 20)
	    vs.map.put(paper, cs)
	else:
	    cs = vs.orthoCS(cs,"D", 0, 2, -2, 10, 10)

	    vs.map.put(self.color)

	    dice = GLRen.createDiceTester(self.dicelen, self.dicelen2, 1, 20)
	    vs.map.put(dice, cs)
	vs.map.put(self.mode[1])
    def mouse(self, ev):
	print "Ev: ",ev
	if self.fisheye.event(ev):
	    print "Did fisheye"
	    self.fisheye.setCoordsysParams()
	    self.repl = 1
	    vob.AbstractUpdateManager.setNoAnimation()
	    vob.AbstractUpdateManager.chg()
	    return 1

