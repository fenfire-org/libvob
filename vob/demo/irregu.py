# 
# Copyright (c) 2003, Janne Kujala
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
import vob
from org.nongnu.libvob.gl import GL, GLRen
from math import sin,cos,pi

from vob.putil.misc import *
from vob.putil.effects import *

class Scene:
    def __init__(self):

        self.mode = 255
        self.combiners = "Enable"

        self.type = "square"
        self.period = 1.0
        self.border = 0.2
        self.w, self.h = 2, 1.5
        self.initirregu()

        #self.x1, self.y1 = 600, 450
        #self.xs, self.ys = 200, 100
        self.x1, self.y1 = 0, 0
        self.xs, self.ys = 1, 1
        self.a1 = 0
        self.scale = 1
        self.distort = 1

    def initirregu(self):

        w = self.w
        h = self.h
        self.mask = getDList("""
            Begin LINE_LOOP
            Vertex -%(w)s -%(h)s
            Vertex -%(w)s +%(h)s
            Vertex +%(w)s +%(h)s
            Vertex +%(w)s -%(h)s
            End
        """ % locals())

	self.irreguframe = IrreguFrame(-w, -h, w, h, self.border, self.period,
                                       type = self.type)

    def key(self, k):
        if 0: pass
        elif k == "Up":
            self.x1 += .2 * cos(self.a1)
            self.y1 += .2 * sin(self.a1)
        elif k == "Down":
            self.x1 -= .2 * cos(self.a1)
            self.y1 -= .2 * sin(self.a1)
        elif k == "Left": self.a1 -= .1
        elif k == "Right": self.a1 += .1
        elif k == "x": self.xs += .1
        elif k == "X": self.xs -= .1
        elif k == "y": self.ys += .1
        elif k == "Y": self.ys -= .1
        elif k == "+": self.scale += .1
        elif k == "-": self.scale -= .1
        elif k == "c":
            if self.combiners == "Enable":
                self.combiners = "Disable"
            else:
                self.combiners = "Enable"
            self.initirregu()
        elif k == "w": self.w += .1; self.initirregu()
        elif k == "W": self.w -= .1; self.initirregu()
        elif k == "h": self.h += .1; self.initirregu()
        elif k == "H": self.h -= .1; self.initirregu()
        elif k == "b": self.border += .01; self.initirregu()
        elif k == "B": self.border -= .01; self.initirregu()
        elif k == "p": self.period += .1; self.initirregu()
        elif k == "P": self.period -= .1; self.initirregu()
        elif "1" <= k <= "9":
            self.mode ^= 1 << (int(k)-1)
            print "mode=", [ (".","X")[(self.mode >> i) & 1] for i in range(0, 9) ]
        elif k == "t":
            if self.type == "square":
                self.type = "ellipse"
            else:
                self.type = "square"
            self.initirregu()
        elif k == "d":
            self.distort = not self.distort
        elif k == "l":
            GL.call("""
            PolygonMode FRONT_AND_BACK LINE
            """)
        elif k == "L":
            GL.call("""
            PolygonMode FRONT_AND_BACK FILL
            """)
        
	pass

    def scene(self, vs):
	vs.put( background((0.1,0.4,0.5)))

        cs2 = vs.coords.affine(
            0, 0, self.x1, self.y1,
            self.xs * cos(self.a1), self.ys * -sin(self.a1),
            self.xs * sin(self.a1), self.ys * cos(self.a1) )

	vs.matcher.add(cs2, "2")

        cs2 = vs.coords.affine(cs2, 0, -1, -1, 2, 0, 0, 2 )
        vs.matcher.add(cs2, "2box")
            
        cs1 = vs.coords.affine(0, 10, 600, 450,
                               self.scale * 150,
                               self.scale * 32.3,
                               self.scale * -14.2,
                               self.scale * 150)
	vs.matcher.add(cs1, "1")

        cs1_d = vs.coords.distort(cs1, 0, 0, 0.1, 0.1, 10, 0.5)
	vs.matcher.add(cs1_d, "1_d")

	if self.distort:
	    cs1 = cs1_d

        if self.mode & 4:
            vs.map.put(self.mask, cs1)

        if self.mode & 1:
            vs.map.put(self.irreguframe.frame, cs1, cs2)

        if self.mode & 2:
            vs.map.put(self.irreguframe.content, cs1, cs2)
