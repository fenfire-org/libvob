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
from vob.color.spaces import *


def safe_float(str):
    if str == "\n": return 0
    return float(str)

from vob.color.CIE_standard_observer import *

def index(arr, ind):
    i = int(ind)
    f = ind - i
    if i >= len(arr) - 1: return 0
    return (1 - f) * arr[i] + f * arr[i + 1]

def l_to_XYZ(l, supp = 0):
    f = (l - 380) / 5.0
    if supp:
        return (index(x10, f),
                index(y10, f),
                index(z10, f))
    else:
        return (index(x2, f),
                index(y2, f),
                index(z2, f))

def spectrum(supp = 0):
    code = """
    LineWidth 3.0
    Begin LINE_LOOP
    """

    for l in range(405, 695, 5):
        (X,Y,Z) = l_to_XYZ(l, supp)

        if X+Y+Z == 0: continue

        x = X / (X + Y + Z)
        y = Y / (X + Y + Z)

        col = linear_to_monitor(XYZtoRGB((X,Y,Z)))
        
        #print x,y,col
        x,y=map_xy(x,y)
        
        code += """
        Color %s %s %s
        Vertex %s %s
        """ % (col[0], col[1], col[2], x, y)
        
    code += "End"

    return getDList(code)
    

def getcol(Y, a, fract):

    s = math.cos(a)
    t = math.sin(a)

    sat = maxYSTsat((Y,s,t)) * fract

    col = YSTtoRGB((Y,sat*s,sat*t))

    (X,Y,Z) = RGBtoXYZ(col)

    x = X / (X + Y + Z)
    y = Y / (X + Y + Z)

    col = linear_to_monitor(col)

    x,y=map_xy(x,y)
    return (col[0], col[1], col[2], x, y)


class Scene:
    """Chromaticity diagram"""
    def __init__(self):
        def CIE1931toLUV(x,y):
            L,U,V = RGBtoLUV(XYZtoRGB((x*Y/y,Y,(1-x-y)*Y/y)))
            return .01*U,.01*V
        def CIE1931toLAB(x,y):
            L,A,B = RGBtoLAB(XYZtoRGB((x*Y/y,Y,(1-x-y)*Y/y)))
            return .01*A,.01*B
        def CIE1931toYST(x,y):
            tmp,S,T = RGBtoYST(XYZtoRGB((x*Y/y,Y,(1-x-y)*Y/y)))
            return S,T

        def scale(x,y):
            return 2*x-1,2*y-1
        def UCSscale(x,y):
            u,v = CIE1931toUCS(x,y)
            return scale(u,v)
        
        self.maps = [ scale, UCSscale, CIE1931toLUV, CIE1931toLAB, CIE1931toYST ]
    	self.key = KeyPresses(
            self, 
	    SlideLin("Y", .05, .05, "lightness", "Down", "Up"),
	    SlideLin("T", 6500, 500, "color temperature", "Left", "Right"),
            ListIndex("map_ind", "maps", 0, "diagram type", "T", "t")
	)
    def scene(self, vs):
        global map_xy,Y
        map_xy = self.maps[self.map_ind]
        Y = self.Y

        set_temp(self.T)

        vs.put( background( getcol(self.Y, 0, 0) ) )

        code = []

        N = 8
        for i in range(0, N):
            fracts = (((i + 0.0) / N),
                      ((i + 1.0) / N))

            code.append("""
            #PolygonMode FRONT_AND_BACK FILL
            #PolygonMode FRONT_AND_BACK LINE
            Begin QUAD_STRIP
            """)

            for ang in range(0,365,5):
                a = ang / 180. * math.pi

                for f in fracts:
                    c = getcol(self.Y, a, f)

                    code.append("""
                    Color %s %s %s
                    Vertex %s %s
                    """ % c)
        
            code.append("""
            End
            """)
        #print code

        
        cs = vs.orthoCS(0, "cs1", 0, 512, 384, 384, -384);
        
        vs.map.put(spectrum(), cs)
        #vs.map.put(spectrum(1), cs)
        vs.map.put(getDList("".join(code)), cs)

        code = """
        PointSize 2.0
        Begin POINTS
        """

        for XYZ,col in [ (RGBtoXYZ((1,1,1)), (1,1,1)),
                         
                         (l_to_XYZ(700.0), (0,0,0)),
                         (l_to_XYZ(546.1), (0,0,0)),
                         (l_to_XYZ(435.8), (0,0,0)),

                         #(l_to_XYZ(645.2), (1,1,1)),
                         #(l_to_XYZ(526.3), (1,1,1)),
                         #l_to_XYZ(444.4), (1,1,1)),
                         ]:
            x,y = apply(map_xy,normalize(XYZ)[:2])
            code += """
            Color %s %s %s
            Vertex %s %s
            PointSize 4.0
            """ % (col[0],col[1],col[2],x,y)
        code += """
        End

        Color 0 0 0
        Begin LINES
        Vertex -1 0
        Vertex +1 0
        Vertex 0 -1
        Vertex 0 +1
        End
        """
        
        vs.map.put(getDList(code), cs)
