# 
# Copyright (c) 2003, Janne V. Kujala
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
from vob.putil.misc import *
from vob.putil.demokeys import *
from vob.paper.texcache import getCachedTexture

import vob.color.spaces
from vob.color.spaces import YSTtoRGB,gamma,set_temp,linear_to_monitor

#set_temp(6500)

def getcol(l, x, y):
    col = YSTtoRGB( (l,  x, y) )
    col = linear_to_monitor(col)

    return col
    

class Scene:
    """Color circle"""
    def __init__(self):
    	self.key = KeyPresses(
            self, 
	    SlideLin("c", .5, .1, "chroma", "Left", "Right"),
	    SlideLin("Y", .5, .05, "luminance", "Down", "Up"),
	    SlideLin("ang", 0, .1, "Rotation", "Prior", "Next"),
	)
    def scene(self, vs):
        vs.put( background( getcol(self.Y, 0, 0) ) )

        code = """
        Begin QUAD_STRIP
        """

        min = [1,1,1]
        max = [0,0,0]

        for ang in range(0,365,1):
            a = ang / 180. * math.pi

            x = math.cos(a)
            y = math.sin(a)

            col = getcol(self.Y, self.c * x, self.c * y)

            for i in range(0,3):
                if col[i] < min[i]: min[i] = col[i]
                if col[i] > max[i]: max[i] = col[i]

            code += """
            Color %s %s %s
            Vertex %s %s
            Vertex %s %s
            """ % (col[0], col[1], col[2], x, y, .5 * x, .5 * y)
        
        code += """
        End
        """

        print "MIN: %5.3f %5.3f %5.3f" % tuple(min)
        print "MAX: %5.3f %5.3f %5.3f" % tuple(max)

        cs = vs.orthoCS(0, "cs1", 0, 512, 384, 300, -300);
        
        vs.map.put(getDList(code), cs)
