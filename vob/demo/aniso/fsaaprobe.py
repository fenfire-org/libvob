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
import jarray

SIZE = 50

lc = 0
uc = .1

quadlist = vob.gl.GL.createDisplayList("""
    Begin QUADS
    TexCoord %(lc)s %(lc)s
    Vertex %(lc)s %(lc)s
    TexCoord %(lc)s %(uc)s
    Vertex %(lc)s %(uc)s
    TexCoord %(uc)s %(uc)s
    Vertex %(uc)s %(uc)s
    TexCoord %(uc)s %(lc)s
    Vertex %(uc)s %(lc)s
    End
""" % locals())
quadlistId = quadlist.getDisplayListID()

list = []

rng = range(-SIZE, 2*SIZE)

for x in rng:
    trax = x + x * 1.0 / SIZE
    for y in rng:
	tray = y + y * 1.0 / SIZE
	list.append("""
	    PushMatrix
	    Translate %(trax)s %(tray)s 0
	    CallList %(quadlistId)s
	    PopMatrix
	"""% locals())
	

list = getDList("".join(list))

class Scene:
    def __init__(self):
    	self.key = KeyPresses(
            self, 
	    )
    def scene(self, vs):
	self.vs = vs
	vs.put( background((.0,.2,.2)))
	vs.put(getDListNocoords("""
	    Disable TEXTURE_2D
	    Disable ALPHA_TEST
	    Color 1 1 1
	"""))
	vs.put(list, vs.translateCS(0, "A", 200, 200))
    
