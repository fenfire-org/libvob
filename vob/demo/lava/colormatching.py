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
import math

vob.color.spaces.gamma = 1 / 0.45
vob.color.spaces.offset = 0.099

def from_polar(r, a):
    x = r * math.cos(a * 2 * math.pi)
    y = r * math.sin(a * 2 * math.pi)
    return x, y

def to_polar(x, y):
    a = math.atan2(y, x) / (2 * math.pi)
    r = math.hypot(x, y)
    return r, a

col_space = 1
def getcol(Y, H, S):
    if col_space == 1: S *= 2 * Y
    x, y = from_polar(S, H)
    if col_space <= 1:
        col = YSTtoRGB(( Y, x, y) )
    elif col_space == 2:
        col = YRBtoRGB(( Y, x, y) )
    else:
        L,u,v = YtoL(Y), 100 * x, 100 * y
        col = LUVtoRGB(( L, u, v) )
    #print col
    return col

def setcol(col):
    if col_space <= 1:
        Y, x, y = RGBtoYST(col)
    elif col_space == 2:
        Y, x, y = RGBtoYRB(col)
    else:
        L,u,v = RGBtoLUV(col)
        Y,x,y = LtoY(L), .01 * u, .01 * v
    S, H = to_polar(x, y)
    if col_space == 1: S /= 2 * Y
    return Y, H, S

def clamp(col):
    def clamp1(x):
        if x < 0: return 0
        if x > 1: return 1
        return x
    return tuple([clamp1(x) for x in col])


arrow = getDList("""
        Color 0 0 0
        LineWidth 1
        PointSize 1
        Begin POINTS
        Vertex 0 0
        End
        Begin LINES
        Vertex 0 0
        Vertex 1 0

        Vertex 1 0
        Vertex .8 .1

        Vertex 1 0
        Vertex .8 -.1
        End
        """)

def delta(col):
    x = RGBtoYST(col)

    l = []
    
    dx = .1, .1, .1
    mt = [ 1 / math.sqrt(i) for i in range(0,3) ]

    for a in range(-1,0,2):
        for b in range(-1,0,2):
            for c in range(-1,0,2):
                m = mt[ (a != 0) + (b != 0) + (c != 0) ]
                y = list(x)
                y[0] += a * m * dx[0] 
                y[1] += b * m * dx[1] 
                y[2] += c * m * dx[2]
                l.append( YSTtoRGB( y ) )

    return x
                

    

#colors = [ apply(getcol, col) for col in delta( (.5, 0, .5) ) ]
           
    

class Scene:
    """Color matching experiment scene"""
    def __init__(self):
        self.cols = [
            (.25, 0, 0),
            (.5, 0, .5),
            (.5, -.25, .5),
            (.5, 0, .5),
            ]
        self.index = 1
        self.oldindex = 0

        if 0:
            logfile.seek(0, 0)
            print "Reading logfile"
            data = logfile.readlines()
            print "Evaluating data"
            for s in data:
                self.cols = eval(s)
            print "Done"

        Y = self.cols[self.index][0]
        H = self.cols[self.index][1]
        S = self.cols[self.index][2]

        def move(dx, dy):
            def do(*args):
                x, y = from_polar(self.S, self.H)
                x += dx
                y += dy
                self.S, self.H = to_polar(x, y)
            return do

        d = .02
        d2 = d / math.sqrt(2)
        self.types = [ "P P", "C P", "P C", "C C", "interpolated" ]
        self.key = KeyPresses(
            self, 
	    SlideLin("Y", Y, .01, "lightness", "Prior", "Next"),
	    SlideLin("H", H, .005, "hue", "Right", "Left"),
	    SlideLin("S", S, .02, "saturation", "Down", "Up"),
	    SlideLog("w", .5, "interpolation width", "/", "*"),
            ListIndex("type", "types", 0, "stimulus type", "T", "t"),
            Toggle("parity", 0, "checkerboard parity", "p"),
            Toggle("bars", 0, "bars", "b"),
            Toggle("blend", 0, "blend", "B"),
            Toggle("line", 0, "separating line", "l"),
            Toggle("frame", 0, "frame", "f"),
            ListIndex("index", "cols", self.index, "color to modify", "C", "c"),
	    SlideLin("size", -2, 1, "square size", "-", "+"),
            Action("Normalize", "n", self.normalize),
            Action("Select color space", "s", self.select_col_space),
            Action("Move N", "KP_Up", move(0, d)),
            Action("Move S", "KP_Down", move(0, -d)),
            Action("Move W", "KP_Left", move(-d, 0)),
            Action("Move E", "KP_Right", move(d, 0)),
            Action("Move NW", "KP_Prior", move(d2, d2)),
            Action("Move SW", "KP_Next", move(d2, -d2)),
            Action("Move NE", "KP_Home", move(-d2, d2)),
            Action("Move SE", "KP_End", move(-d2, -d2)),
            )

    def select_col_space(self, *args):
        global col_space

        cols = [ apply(getcol, col) for col in self.cols ]
        col_space = (col_space + 1) % 4
        print "Using", ("ST", "S/Y T/Y", "RB", "UV")[col_space], "color space"
        self.cols = map(setcol, cols)
        self.oldindex = None
        
    def normalize(self, *args):
        col = getcol(self.Y, self.H, self.S)
        col = clamp(col)
        self.Y, self.H, self.S = setcol(col)
        vob.AbstractUpdateManager.chg()
        print self.Y, self.H, self.S

    def mouse(self, ev):
        d = ev.getWheelDelta()
        if d == 0:
            x = (ev.getX() - self.col_orig[0]) / self.col_scale
            y = (self.col_orig[1] - ev.getY()) / self.col_scale
            
            self.S, self.H = to_polar(x, y)
        else:
            self.Y += d * .01
        vob.AbstractUpdateManager.chg()

    
    def scene(self, vs):
        if self.index == self.oldindex:
            self.cols[self.index] = (self.Y, self.H, self.S)
        else:
            self.Y, self.H, self.S = self.cols[self.index]
            self.oldindex = self.index
            
        #log([apply(getcol, col) for col in self.cols])
        status = " ".join(["(%.4G, %.4G, %.4G)" % col for col in self.cols])
        str(self.cols)
        
        vs.put( background( (0, 0, 0) ) )

        vssize = vs.getSize()
        
        cs = vs.orthoCS(0, "root", 0,
                        0, 0,
                        vssize.width / 4.0, vssize.height / 3.0)

        csA0 = vs.orthoCS(cs, "csA0", 0,
                          0, 0,
                          2, 3)
        
        csB0 = vs.orthoCS(cs, "csB0", 0,
                          4 - 2, 0,
                          2, 3)

        size = 2.0**self.size

        csA1 = vs.orthoCS(cs, "csA1", 0,
                          (2 - size) / 2.0,
                          (3 - size) / 2.0,
                          size, size)

        csB1 = vs.orthoCS(cs, "csB1", 0,
                          4 - (2 - size) / 2.0 - size,
                          (3 - size) / 2.0,
                          size, size)


        colA0, colA1, colB0, colB1 = tuple(
            [linear_to_monitor(apply(getcol, col)) for col in self.cols]
            )

 
        if self.type < 4:
            vs.map.put(coloredQuad(colA0), csA0)
            vs.map.put(coloredQuad(colB0), csB0)

            if (self.type & 1) == 0:
                vs.map.put(coloredQuad(colA1), csA1)
            if (self.type & 2) == 0:
                vs.map.put(coloredQuad(colB1), csB1)

            nx = int(2 / size + .5)
            ny = int(3 / size + .5)
            for y in range(0, ny):
                for x in range(1 - self.parity, nx):
                    if (x+y & 1) == 0 and (self.type & 1):
                        csA = vs.orthoCS(cs, "csA%s-%s" % (x,y), 0,
                                         2 - (x + 1) * size,
                                         y * size,
                                         size,
                                         size)
                        vs.map.put(coloredQuad(colA1), csA)
                    if (x+y & 1) == self.parity and (self.type & 2):
                        csB = vs.orthoCS(cs, "csB%s-%s" % (x,y), 0,
                                         2 + x * size,
                                         y * size,
                                         size,
                                         size)
                        vs.map.put(coloredQuad(colB1), csB)
        else:
            nx = int(2 / size + .5)
            ny = int(3 / size + .5)
            for y in range(0, ny):
                for x in range(-nx, nx):
                    def lerp(c0,c1,t):
                        return [(1-t) * c0[i] + t * c1[i] for i in range(0,3)]
                    t = 0.5 + (x+.5) / (2.0 * nx) / self.w
                    if t < 0: t = 0
                    if t > 1: t = 1
                    
                    cs2 = vs.orthoCS(cs, "cs-%s-%s" % (x,y), 0,
                                     x * size + 2,
                                     y * size,
                                     size,
                                     size)
                    if (x+y & 1) == 0:
                        col = lerp(colA0, colB0, t)
                    else:
                        col = lerp(colA1, colB1, t)
                    vs.map.put(getDListNocoords("Color %s %s %s" % tuple(col)))
                    vs.map.put(quad(), cs2)

        
        if self.frame:
            vs.map.put(getDListNocoords("""
            PushAttrib ENABLE_BIT
            Disable ALPHA_TEST
            %sable BLEND
            """ % ("Dis", "En")[self.blend]))
            
            c = "%s %s %s" % linear_to_monitor(getcol(0, 0, 0))
            vs.map.put(getDList("""
            Begin QUAD_STRIP
            Color %(c)s 1
            Vertex 0 0
            Color %(c)s 0
            Vertex .25 .25
            Color %(c)s 1
            Vertex 4 0
            Color %(c)s 0
            Vertex 3.75 .25
            Color %(c)s 1
            Vertex 4 3
            Color %(c)s 0
            Vertex 3.75 2.75
            Color %(c)s 1
            Vertex 0 3
            Color %(c)s 0
            Vertex .25 2.75
            Color %(c)s 1
            Vertex 0 0
            Color %(c)s 0
            Vertex .25 .25
            End
            PopAttrib
            """ % locals()), cs)

        if self.line:
            vs.map.put(getDList("""
            Color 0 0 0
            LineWidth 8
            Begin LINES
            Vertex 2 0
            Vertex 2 3
            End
            """), cs)


        putText(vs, cs, status,
                color = (0,0,0),
                x = 2, y = 2.98, h = .08, key = "txt")


        x, y = from_polar(self.S, self.H)

        s = .375
        d = .5 + int(s/size) 
        x0 = (d + (self.index & 1)) * size
        y0 = d * size
        if self.index & 2: x0 = 4 - x0 - (self.parity | (self.type == 4)) * size
        cs1 = vs.affineCS(cs, "cs1", 0, x0, y0,
                          s, 0, 0, -s)
        
        cs2 = vs.affineCS(cs, "cs2", 0, x0, y0,
                          x * s, -y * s,
                          -y * s, -x * s)

        self.col_orig = (x0 / 4.0 * vssize.width,
                         y0 / 3.0 * vssize.height)
        self.col_scale = s / 4.0 * vssize.width

        vs.map.put(arrow, cs2)

        if self.bars:
            bars = clamp(getcol(self.Y, self.H, self.S))

            #h = math.fmod(self.H, 1)
            #if h < 0: h += 1
            #bars += self.Y, h, self.S

            cols = ((1,0,0), (0,1,0), (0,0,1),
                    (1,1,1), (1,1,0), (.5,.5,.5),
                    )

            for i in range(0,len(bars)):
                cs_bar = vs.affineCS(cs, "bar" + str(i),
                                     0, 0.1 + 0.1 * i, 3, .05, 0, 0, -.2 * bars[i])
                vs.map.put(coloredQuad(cols[i]), cs_bar)

        if 1:
            if not self.__dict__.has_key("gamut"):
                self.gamut = {}
 
            if self.gamut.has_key(self.Y):
                g = self.gamut[self.Y]
            else:
                code = ["""
                Begin LINE_LOOP
                Color 1 1 1
                """]
                for vert in getRGBslice(self.Y):
                    Y, H, S = setcol(vert)
                    x, y = from_polar(S, H)
                    code.append("Vertex %s %s" % (x, y))
                code.append("End")
                g = getDList("\n".join(code))
            
            vs.map.put(g, cs1)

        else:
            vs.map.put(getDListNocoords("Color 1 1 1"))

            cols = getRGBslice(self.Y, dupes = 1)
            poly = map(lambda col: apply(from_polar, setcol(col)[2:0:-1]), cols)

            cs = []
            for i in range(0, len(poly)):
                cs.append(vs.affineCS(cs1, "cs_gamut" + str(i), 0,
                                      poly[i][0], poly[i][1], 1, 0, 0, 1))

            line = GLRen.createLineConnector(0,0,0,0)
            for i in range(0, len(poly)):
                vs.map.put(line, cs[i-1], cs[i])



vob.AbstractUpdateManager.defaultAnimationTime = 500

logfile = open(",,colormatching.log", "a+")

prev_data = None
def log(data):
    global prev_data
    s = str(data)
    if s == prev_data: return
    prev_data = s

    logfile.write(s + "\n")
    logfile.flush()
    
