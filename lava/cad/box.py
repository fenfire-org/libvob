#! /usr/bin/python
# 
# Copyright (c) 2003, Janne V. Kujala
# 
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
# Public License along with Fenfire; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
#

import math
import sys
import os

class Vec:
    def __init__(self, x, y): self.x, self.y = x, y
    def __add__(self, other): return Vec(self.x + other.x, self.y + other.y)
    def __sub__(self, other): return Vec(self.x - other.x, self.y - other.y)
    def __mul__(self, other): return Vec(self.x * other, self.y * other)
    def __rmul__(self, other): return Vec(self.x * other, self.y * other)
    def __div__(self, other): return Vec(self.x / other, self.y / other)
    def __neg__(self): return Vec(-self.x, -self.y)
    def dot(self, other): return self.x * other.x + self.y * other.y
    def length(self): return math.hypot(self.x, self.y)
    def normalized(self): return self.__mul__(1 / self.length())
    def cw90(self): return Vec(self.y, -self.x)
    def ccw90(self): return Vec(-self.y, self.x)



def getEllipse(f0, f1, v):
    c = 0.5 * (f1 - f0).length()
    a = 0.5 * ((f0 - v).length() + (f1 - v).length())
    b = math.sqrt(a * a - c * c)
    o = 0.5 * (f0 + f1) 
    e0 = (f1 - f0).normalized()
    e1 = e0.ccw90()

    #print >> sys.stderr, a,b,c
    return a, b, c, o, e0, e1


def ellipseTangent(f0, f1, v):
    a, b, c, o, e0, e1 = getEllipse(f0, f1, v)

    x = e0.dot(v - o)
    y = e1.dot(v - o)

    t = Vec(x / a, y / b).cw90()
    #print >> sys.stderr, t.length()

    return (t.x * a * e0 + t.y * b * e1).normalized()


class Graph:
    def __init__(self, bmap, title = ""):
        self.f = os.popen("graph -TX -x -30 90 -y -60 60 --bitmap-size %(bmap)s -w .8 -h .8 -r .1 -u .08 -L '%(title)s'" % locals(), "w")

    def mode(self, m, s):
        self.f.write("#m=%s,S=%s\n" % (m, s))
    
    def drawEllipse(self, f0, f1, v, m = 1):
        a, b, c, o, e0, e1 = getEllipse(f0, f1, v)

        n = 40
        self.mode(m, 0)
        for i in range(0, n+1):
            ang = i * 2 * math.pi / n
            u = o + math.cos(ang) * a * e0 + math.sin(ang) * b * e1
            self.f.write("%s %s\n" % (u.x, u.y))

    def drawReflection(self, v, u, t, m = 1):
        t = t.normalized()
        n = t.cw90()
        self.drawLine(v, u, m)

        x = 3 * t.dot(v - u)
        y = 3 * n.dot(v - u)
    
        self.drawLine(u, u - t * x + n * y, m)
    

    def drawPoint(self, p, s = 3):
        self.mode(0, s)
        self.f.write("%s %s\n" % (p.x, p.y))

    def drawLine(self, u, v, m = 1, s = 0):
        self.mode(m, s)
        self.f.write("%s %s\n" % (u.x, u.y))
        self.f.write("%s %s\n" % (v.x, v.y))



def flip(v):
    return Vec(v.x, -v.y)

def lerp(u, v, t):
    return (1 - t) * u + t * v

gr1 = Graph(bmap = "800x800+0+0", title = "Side view")
gr2 = Graph(bmap = "800x800+800+0", title = "Top view")

# -----------------------------------------------------------
# Parameters
# -----------------------------------------------------------

depth = 55 # depth of the box
screen_width = 40
screen_height = 30
front_width = 40
front_height = 30

# x-lengths of the linear sections starting from the front
list = 15, 24, 16
#list = 35, 20

# If true, the same profile is used for the side and
#    top/bottom profiles
# If false, the slopes may be different, but the 
#    x-lengths of the sections will still be the same.
same_profile = 1

# Design focii: the slopes will be chosen so that
# the reflection passes behind the focus point:
f1 = Vec(depth - 0, 15) # side view
f1b = Vec(depth - 0, 16.25) # top view

# Reflection point tolerance in cm for drawing reflections
# (doesn't affect the box shape, only the drawed reflections)
tol = 0

# -----------------------------------------------------------

f0 = Vec(0, -0.5 * screen_height)
f0b = Vec(0, -0.5 * screen_width)
v0 = Vec(depth, -0.5 * front_height)
v0b = Vec(depth, -0.5 * front_width)

gr1.drawLine(f0, -f0, 1, 2)
gr2.drawLine(f0b, -f0b, 1, 2)

gr1.drawPoint(f0)
gr1.drawPoint(f1)
gr2.drawPoint(f0b)
gr2.drawPoint(f1b)

gr1.drawPoint(Vec(depth, 0), 4) # eyes

gr2.drawPoint(Vec(depth, -3.5), 4) # left eye
gr2.drawPoint(Vec(depth, 3.5), 4) # right eye

while list:
    l = list[0]
    list = list[1:]

    gr1.drawEllipse(f0, f1, v0, 2)
    gr2.drawEllipse(f0b, f1b, v0b, 2)

    t = ellipseTangent(f0, f1, v0)
    tb = ellipseTangent(f0b, f1b, v0b)

    gr1.drawLine(v0, v0 + 50 * t, 2)
    gr2.drawLine(v0b, v0b + 50 * tb, 2)

    # Don't curve back
    if t.y > 0 or not list: t = Vec(-1, 0)
    if tb.y > 0 or not list: tb = Vec(-1, 0)

    # Choose the larger slope
    if same_profile:
        if tb.y > t.y:
            tb = t
        else:
            t = tb

    
    for y in 0,:
        gr1.drawReflection(lerp(f0, -f0, y), v0 - t * tol, t, 4)
        gr2.drawReflection(lerp(f0b, -f0b, y), v0b - tb * tol, tb, 4)

    lb = Vec(l, l * tb.y / tb.x).length()
    l = Vec(l, l * t.y / t.x).length()

    v = v0 + t * l
    vb = v0b + tb * lb
    gr1.drawLine(v0, v, 1, 3)
    gr1.drawLine(flip(v0), flip(v), 1, 3)
    gr2.drawLine(v0b, vb, 1, 3)
    gr2.drawLine(flip(v0b), flip(vb), 1, 3)

    print " slope:%14.9f%28.9f" % (-t.y / t.x, -tb.y / tb.x)
    print "length:%14.9f%28.9f" % (lb, l)
    print "vertex:%14.9f%14.9f%14.9f%14.9f" % (v0.x, v0.y, v0b.x, v0b.y)
    print


    v0 = v
    v0b = vb



