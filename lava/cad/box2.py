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
    def flip(self): return Vec(self.x, -self.y)
    def flop(self): return Vec(-self.x, self.y)
    def __str__(self): return "Vec(%s, %s)" % (self.x, self.y)

class Vec3:
    def __init__(self, x, y, z): self.x, self.y, self.z = x, y, z
    def __add__(self, o): return Vec3(self.x + o.x, self.y + o.y, self.z + o.z)
    def __sub__(self, o): return Vec3(self.x - o.x, self.y - o.y, self.z - o.z)
    def __mul__(self, o): return Vec3(self.x * o, self.y * o, self.z * o)
    def __rmul__(self, o): return Vec3(self.x * o, self.y * o, self.z * o)
    def __div__(self, o): return Vec3(self.x / o, self.y / o, self.z / o)
    def __neg__(self): return Vec3(-self.x, -self.y, -self.z)
    def dot(self, o): return self.x * o.x + self.y * o.y + self.z * o.z
    def length(self): return math.hypot(math.hypot(self.x, self.y), self.z)
    def normalized(self): return self.__mul__(1 / self.length())
    def cw90(self): return Vec3(self.y, -self.x, self.z)
    def ccw90(self): return Vec3(-self.y, self.x, self.z)
    def flip(self): return Vec3(self.x, -self.y, self.z)
    def flop(self): return Vec3(-self.x, self.y, self.z)
    def __str__(self): return "Vec3(%s, %s, %s)" % (self.x, self.y, self.z)



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
    def __init__(self, bmap, title = "", out = "-", **args):
        cmd = "graph --bitmap-size %(bmap)s -L '%(title)s'" % locals()

        for arg in args.keys():
            cmd += " -%s %s" % (arg, args[arg])

        if out != "-":
            fd = os.open(out, os.O_WRONLY + os.O_CREAT) 
            tmp = os.dup(1)
            os.dup2(fd, 1)
            os.close(fd)
            self.f = os.popen(cmd, "w")
            os.dup2(tmp, 1)
            os.close(tmp)
        else:
            self.f = os.popen(cmd, "w")

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


def lerp(u, v, t):
    return (1 - t) * u + t * v

T = "X"
if len(sys.argv) > 1:
    T = sys.argv[1]
    
gr1 = Graph(T = T, out = ",,out.1", bmap = "800x800+0+0", title = "Side view",
            x = "-30 90", y = "-60 60", w = .8, h = .8, r = .1, u = .08)

gr2 = Graph(T = T, out = ",,out.2", bmap = "800x800+800+0", title = "Top view",
            x = "-30 90", y = "-60 60", w = .8, h = .8, r = .1, u = .08)

gr3 = Graph(T = T, out = ",,out.3", bmap = "800x800+0+800", title = "Parts",
            x = "-81.5 81.5", y = "-81.5 81.5", w = .8, h = .8, r = .1, u = .08)

# -----------------------------------------------------------
# Parameters
# -----------------------------------------------------------

depth = 55 # depth of the box
screen_width = 36
screen_height = 27

# x-lengths of the linear sections starting from the front
list = 18, 19, 18
#

# If true, the same profile is used for the side and
#    top/bottom profiles
# If false, the slopes may be different, but the 
#    x-lengths of the sections will still be the same.
same_profile = 1

# Design focii: the slopes will be chosen so that
# the reflection passes behind the focus point:
f1 = Vec(depth, -10)
f1b = Vec(depth, -12)

# Reflection point tolerance in cm for drawing reflections
# (doesn't affect the box shape, only the drawed reflections)
tol = 0

# -----------------------------------------------------------

f0 = Vec(0, 0.5 * screen_height)
f0b = Vec(0, 0.5 * screen_width)
v0 = Vec(0, -0.5 * screen_height)
v0b = Vec(0, -0.5 * screen_width)

gr1.drawLine(f0, -f0, 1, 2)
gr2.drawLine(f0b, -f0b, 1, 2)

gr1.drawPoint(f0)
gr1.drawPoint(f1)
gr2.drawPoint(f0b)
gr2.drawPoint(f1b)

gr1.drawPoint(Vec(depth, 0), 4) # eyes

gr2.drawPoint(Vec(depth, -3.5), 4) # left eye
gr2.drawPoint(Vec(depth, 3.5), 4) # right eye

vert = [Vec3(abs(v0b.y), abs(v0.y), v0.x)]

while list:
    l = list[0]
    list = list[1:]

    gr1.drawEllipse(f0, f1, v0, 2)
    gr2.drawEllipse(f0b, f1b, v0b, 2)

    t = -ellipseTangent(f0, f1, v0)
    tb = -ellipseTangent(f0b, f1b, v0b)

    gr1.drawLine(v0, v0 + 50 * t, 2)
    gr2.drawLine(v0b, v0b + 50 * tb, 2)

    # Don't curve back
    if t.y > 0 or not list: t = Vec(1, 0)
    if tb.y > 0 or not list: tb = Vec(1, 0)

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
    gr1.drawLine(v0.flip(), v.flip(), 1, 3)
    gr2.drawLine(v0b, vb, 1, 3)
    gr2.drawLine(v0b.flip(), vb.flip(), 1, 3)

    print >> sys.stderr, " slope:%14.9f%28.9f" % (-t.y / t.x, -tb.y / tb.x)
    print >> sys.stderr, "length:%14.9f%28.9f" % (lb, l)
    print >> sys.stderr, "vertex:%14.9f%14.9f%14.9f%14.9f" % (v0.x, v0.y, v0b.x, v0b.y)
    print >> sys.stderr

    v0 = v
    v0b = vb

    vert.append(Vec3(abs(v0b.y), abs(v0.y), v0.x))



def flatQuad(v0, v1, v2, v3):

    u0 = .5 * (v0 + v1)
    u1 = .5 * (v2 + v3)

    e0 = (u1 - u0).normalized()
    e1 = (v1 - v0).normalized()

    return [ Vec(e0.dot(u - u0), e1.dot(u - u0)) for u in (v0, v1, v2, v3) ]


#vert.reverse()

sides = (
    lambda v0, v1: (-v0, -v0.flip(), -v1, -v1.flip()),
    lambda v0, v1: (-v0, -v0.flop(), -v1, -v1.flop()),
    lambda v0, v1: (v0.flip(), v0, v1.flip(), v1),
    lambda v0, v1: (v0.flop(), v0, v1.flop(), v1),
    )

basis = (
    (Vec(-1, 0), Vec(0, -1)),
    (Vec(0, -1), Vec(1, 0)),
    (Vec(1, 0), Vec(0, 1)),
    (Vec(0, 1), Vec(-1, 0))
)

print "Part list:"
for side in range(0,4):

    x0 = None

    print
    for i in range(0, len(vert) - 1):
        v0, v1, v2, v3 = sides[side](vert[i], vert[i + 1])

        if not x0:
            t = 0.5 * (v0 + v1)
            x0 = Vec(t.x, t.y).length()
        
        u0, u1, u2, u3 = flatQuad(v0, v1, v2, v3)

        dx = x0 - u0.x
        for u in u0, u1, u2, u3:
            u.x += dx
        x0 = u2.x

        e0, e1 = basis[side]
        for u in u0, u1, u2, u3:
            t = e0 * u.x + e1 * u.y
            u.x, u.y = t.x, t.y

        print "%10.4f%10.4f%10.4f" % ((u1 - u0).length(), (u3 - u1).length(), (u3 - u2).length())

        gr3.drawLine(u0, u1)
        gr3.drawLine(u1, u3)
        gr3.drawLine(u3, u2)
        gr3.drawLine(u2, u0)


