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

from vob.putil.misc import *
from vob.putil.demokeys import *

import vob.color.spaces
from vob.color.spaces import linear_to_monitor

def color(rgb):
    return getDListNocoords("Color %s %s %s" % tuple(linear_to_monitor(rgb)))

N = 48

class Scene:
    """Gamma correction"""
    def __init__(self):
    	self.key = KeyPresses(
            self, 
	    SlideLin("gamma", 2.4, .05, "exponent", "Down", "Up"),
	    SlideLin("offset", .055, .005, "offset", "Left", "Right"),
            )

        code = """
        LineWidth 1.0
        Begin LINES
        """
        for i in range(0,N/2):
            code += """
            Vertex 0 %s
            Vertex .3333 %s
            """ % (2*i, 2*i)

        code += """
        End
        """
        self.stripe = getDList(code)

        code = """
        LineWidth 1.0
        Begin LINES
        """
        for i in range(0,N/3):
            code += """
            Vertex .6666 %s
            Vertex 1 %s
            """ % (3*i+1, 3*i+1)

        code += """
        End
        """
        self.stripe2 = getDList(code)

        code = """
        LineWidth 1.0
        Begin LINES
        """
        for i in range(0,N):
            if i % 3 != 1:
                code += """
                Vertex .6666 %s
                Vertex 1 %s
                """ % (i, i)
                
        code += """
        End
        """
        self.stripe3 = getDList(code)

        code = """
        Begin QUAD_STRIP
        Vertex .3333 0
        Vertex .6666 0
        Vertex .3333 %s
        Vertex .6666 %s
        End
        """ % (2*N, 2*N)
        self.block = getDList(code)

    def scene(self, vs):
        vob.color.spaces.gamma = self.gamma
        vob.color.spaces.offset = self.offset
        
        vs.put( background( (0, 0, 0) ) )
        
        css = [vs.orthoCS(0, "cs%s" % y, 0, 0, y-.5, 1024, 1) for y in range(0,1200,N)]
        i = 0
        for cs in css:
            col = [(1,1,1),(0,1,0),(1,0,0),(0,0,1)][i&3]
            f = .5 * (1 - (i / 4) * .2)**3
            i += 1
            col = [c * f for c in col]
            col1 = [c * 2 for c in col]
            col2 = [c * 3 for c in col]
            col3 = [c * 3 / 2 for c in col]

            if (col1[0] <= 1.0001 and
                col1[1] <= 1.0001 and
                col1[2] <= 1.0001):
                vs.put(color(col1))
                vs.map.put(self.stripe, cs)

            if (col2[0] <= 1.0001 and
                col2[1] <= 1.0001 and
                col2[2] <= 1.0001):
                vs.put(color(col2))
                vs.map.put(self.stripe2, cs)
            elif (col3[0] <= 1.0001 and
                  col3[1] <= 1.0001 and
                  col3[2] <= 1.0001):
                vs.put(color(col3))
                vs.map.put(self.stripe3, cs)

            vs.put(color(col))
            vs.map.put(self.block, cs)

