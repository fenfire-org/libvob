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
from vob.color.spaces import *

#set_temp(6500)

def getcol(Y, x, y):
    col = YSTtoRGB( (Y, x, y) )
    #col = LABtoRGB( (YtoL(Y), 60*x, 60*y) )
    col = linear_to_monitor(col)

    return col


def rotate(pt, ang):
    a = ang * math.pi / 180
    return (pt[0] * math.cos(a) - pt[1] * math.sin(a),
            pt[0] * math.sin(a) + pt[1] * math.cos(a))

def rotatepair(pair, ang):
    p0 = rotate(pair[0][1:], ang)
    p1 = rotate(pair[1][1:], ang)
    
    return ((pair[0][0], p0[0], p0[1]),
            (pair[1][0], p1[0], p1[1]))
    

class Scene:
    """Color pairs to be used in the experiment"""
    def scene(self, vs):
        vs.put( background( getcol(.4, 0, 0) ) )


        pairs0 = [
            ((.5, .5, 0), (.5, .25, 0)),
            ((.5, .5, 0), (.5, 0, 0)),
            ((.5, .5, 0), (.4, .5, 0)),
            ((.5, .5, 0), (.6, .5, 0)),
            ((.5, .5, 0), (.5, -.5, 0)),
            ((.5, .5, 0), (.5, 0, .5)),
            ((.5, .5, 0), (.5, 0, -.5)),
            ((.5, .5, 0), (.5, .433, .25)),
            ]
        
        pairs = [ rotatepair(pair, ang)
                  #for ang in (0, 60, 120, 180, 240, 300)
                  for ang in (0, 72, 144, 216, 288, )
                  #for ang in (0, 90, 180, 240,  )
                  #for ang in (0, 120, 240,  )
                  for pair in pairs0
                  ]

        i = 0
        for pair in pairs:
            x = i % 8
            y = i / 8
            
            csA = vs.orthoCS(0, "csA%s" % (i,), 0,
                             x * 128 + 4, y * 128,
                             60, 120)

            csB = vs.orthoCS(0, "csB%s" % (i,), 0, x * 128 + 64, y * 128,
                             60, 120)

            vs.map.put(coloredQuad(apply(getcol, pair[0])), csA)
            vs.map.put(coloredQuad(apply(getcol, pair[1])), csB)

            i += 1

