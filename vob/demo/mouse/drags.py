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


from __future__ import nested_scopes
import vob
from org.nongnu.libvob.gl import GL, GLRen
from org.nongnu.libvob import VobMouseEvent as e
from vob.putil.misc import *

class MoveDrag(vob.mouse.MouseDragListener):
    def __init__(self, scene, list, index):
	self.list = list
	self.index = index
	self.scene = scene
    def startDrag(self, x, y):
	(self.cx, self.cy) = (x, y)
    def drag(self, x, y):
	tup = self.list[self.index]
	self.list[self.index] = (
	    tup[0] + x - self.cx,
	    tup[1] + y - self.cy,
	    tup[2], tup[3])
	(self.cx, self.cy) = (x, y)
	self.scene.update()
    def endDrag(self, x, y):
	pass

class MoveSize(vob.mouse.MouseDragListener):
    def __init__(self, scene, list, index):
	self.list = list
	self.index = index
	self.scene = scene
    def startDrag(self, x, y):
	(self.cx, self.cy) = (x, y)
    def drag(self, x, y):
	tup = self.list[self.index]
	self.list[self.index] = (
	    tup[0], 
	    tup[1],
	    tup[2] + x - self.cx,
	    tup[3] + y - self.cy,
	    )
	(self.cx, self.cy) = (x, y)
	self.scene.update()
    def endDrag(self, x, y):
	pass

class MoveBlueTo(vob.mouse.MouseClickListener):
    def __init__(self, scene):
	self.scene = scene
    def clicked(self, x, y):
	self.scene.places[0] = (
	    x, y, self.scene.places[0][2], self.scene.places[0][3])
	# Note how we don't call update() here: we *don't* want
	# to set the new coordinates.
	self.scene.updateSlow()
	vob.AbstractUpdateManager.chg()

class QuadSelect(vob.mouse.MousePressListener):
    def __init__(self, scene, drags):
	self.scene = scene
	self.drags = drags
    def pressed(self, x, y):
	if self.scene.vs == None: return
	key = self.scene.vs.getKeyAt(0, x, y, None)
	if key == None: return None
	return self.drags[key]

class Scene:
    def __init__(self):
	self.multiplexer = vob.mouse.MouseMultiplexer()
	self.vs = None
	self.quads = [coloredQuad((0,0,.5)), coloredQuad((.5,0,0))]
	self.places = [(100,100,100,100), (300,100,100,100)]

	self.multiplexer.setListener(
	    1, 0, "Drag things", QuadSelect(self, 
	    {
	     "0": MoveDrag(self, self.places, 0),
	     "1": MoveDrag(self, self.places, 1),
	     }))

	self.multiplexer.setListener(
	    1, e.SHIFT_MASK, "Resize things", QuadSelect(self, 
	    {
	     "0": MoveSize(self, self.places, 0),
	     "1": MoveSize(self, self.places, 1),
	     }))

	self.multiplexer.setListener(
	    1, 0, "Click and move box", MoveBlueTo(self)
	    )

    def mouse(self, ev):
	self.multiplexer.deliverEvent(ev)

    def scene(self, vs):
	if self.vs != None:
	    return self.vs
	self.vs = vs
	vs.put( background((0.1,0.9,0.8)))
	self.cs0 = vs.orthoCS(0, "0", 0, 0,0,1,1)
	self.cs1 = vs.orthoCS(0, "1", 10, 0,0,1,1)
	self.updateCoords()
	vs.put(self.quads[0], self.cs0)
	vs.put(self.quads[1], self.cs1)

	vs.activate(self.cs0)
	vs.activate(self.cs1)

    def updateCoords(self):
	self.vs.coords.setOrthoParams(self.cs0, 0, *self.places[0])
	self.vs.coords.setOrthoParams(self.cs1, 10, *self.places[1])

    def updateSlow(self):
	self.vs = None
    def update(self):
	if self.vs == None: return
	self.updateCoords()
	vob.AbstractUpdateManager.chg()

