# 
# Copyright (c) 2003, Tuomas J. Lukka and Janne Kujala
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
import math

import vob
from org.nongnu.libvob.gl import GL, GLRen
from vob.putil import cg
from vob.putil.misc import *
from vob.putil.demokeys import *
from vob.paper.texcache import getCachedTexture
from vob.fillet import light3d



class Scene:
    "Fillet stretching"
    def __init__(self):
    	self.key = KeyPresses(
            self, 
	    SlideLin("x", 0, 20, "x", "Left", "Right"),
	    SlideLin("y", 0, 20, "y", "Down", "Up"),
            *light3d.commonkeys
	)
    def scene(self, vs):
	vs.put( background((.5,1,.2)))

        N = 10

        a = [vs.orthoBoxCS(0, "A%s" % i, 0, 100, 100+100*i,
                           1, 1, self.size, self.size)
             for i in range(0, N)]
        b = [vs.orthoBoxCS(0, "B%s" % i, self.y, 180+80*i+self.x, 100+100*i,
                           1, 1, self.size, self.size)
             for i in range(0, N)]

	def pc(conns, cs):

            for i in range(0, N):
                vs.put(conns, cs + [a[i],  b[i]]);
                vs.put(conns, cs + [b[i],  a[i]]);


        light3d.drawFillets(self, vs, pc)

