# 
# Copyright (c) 2003, Tuomas J. Lukka
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
from Numeric import *
import math
import os
import sys
import RandomArray

from Tkinter import *

length= 75
epsilon = 0.0001

# print l

def A(xleft, xright, rleft, rright):
    return (xright-xleft) * .5 * (rleft + rright)

def L(xleft, xright, rleft, rright):
    return sqrt(((xright-xleft)**2) * xweight ** 2 +  (rright-rleft)**2)

def U(xleft, xright, rleft, rright):
    return areaweight * A(xleft, xright, rleft, rright) +3* L(xleft, xright, rleft, rright)


def pot(x, r, U):
    xleft = x[:-1]
    xright = x[1:]
    rleft = r[:-1]
    rright = r[1:]

    u = add.reduce(U(xleft, xright, rleft, rright))

    def der(parind):
	pars1 = [xleft, xright, rleft, rright]
	pars2 = [xleft, xright, rleft, rright]
	pars1[parind] = pars1[parind] + epsilon
	pars2[parind] = pars2[parind] - epsilon

	return (U(*pars1) - U(*pars2)) / epsilon;

    dudxl = der(0)
    dudxr = der(1)
    dudrl = der(2)
    dudrr = der(3)

    dudr = r * 0
    dudr[1:] += dudrr 
    dudr[:-1] += dudrl 

    dudx = r * 0
    #dudx[1:] += dudxr 
    #dudx[:-1] += dudxl 

    return (u, dudx, dudr)

def minfunc(v, U):
    (u, dux, dur) = pot(v[0:v.shape[0]/2], v[v.shape[0]/2:], U)
    g = v * 0
    g[0:v.shape[0]/2] = dux
    g[v.shape[0]/2:] = dur
    g[0] = g[v.shape[0]/2-1] = g[v.shape[0]/2] = g[-1] = 0
    return (u, g)


geomview = os.popen("geomview -c -", "w", 0)
# geomview = sys.stdout

geomview.write("""
(geometry example { LIST { : foo0 } { : foo1 } { : foo2 } { :foo3 } { :foo4 }})
""");
geomview.flush()

def wmesh(ind, s, r):
    nang = 10
    geomview.write("(read geometry { define foo%s\nMESH %s %s\n" % (ind,s.shape[0], 2) )
    for a in (0, 1):
	x = s
	y = r * a
	z = 0 * r  + ind
	for i in range(0, s.shape[0]):
	    geomview.write("%s %s %s\n" % (x[i], y[i], z[i]))
    geomview.write("})\n")

# wmesh(l, (l-0.5)**2)

# geomview.close()

class MomGrad:
    def __init__(self, func, x, constraint, v):
	self.func = func
	self.constraint = constraint
	self.constraintv = v
	self.mom = x * 0
	self.x = x
	self.step = 0.02
	self.brake = 0.990
    def round(self):
	(u, g) = self.func(self.x)

	self.constraintv = areatarget

	(c, cg) = self.constraint(self.x)
	cg = cg / sqrt(add.reduce(cg*cg))

	dot = add.reduce(cg * g)
	print u, c

	self.u = u
	self.mom += g + (.1 * (self.constraintv - c) - dot) * cg

	l = add.reduce(self.mom*self.mom)
	self.x += -self.step / sqrt(l) * self.mom
	# self.x = maximum(self.x, 0)
	self.mom *= self.brake

sur = []
for l in (1,):
    tl = l * length
    print "TL",tl
    x = zeros(2*tl, Float)
    x[0:tl] = arange(tl) * 1.0 / tl * l
    x[tl] = 2
    x[-1] = 2
    mi = MomGrad(lambda v: minfunc(v, L), x, lambda v: minfunc(v, A), 0)
    sur.append(mi)

def slider(obj, name, var, default, from_, to, resolution=0.001):
    setattr(obj, name, var)
    var.set(default)
    Scale(obj, label=name, orient="horizontal", 
	resolution=resolution,
	from_=from_, to=to, variable=var).pack(
	    fill="both", expand=1)

class UI(Frame):
    def __init__(self, master=None):
	Frame.__init__(self, master)
	self.pack(expand=1, fill="both")
	Button(self, text="Start", command = self.idle).pack()
	slider(self, "areatarget", DoubleVar(), 1, 0, 8);
	slider(self, "xweight", DoubleVar(), 1, 0, 8);

    def idle(self):
	print "Start idle"
	global areatarget, xweight
	for i in range(0, len(sur)):
	    areatarget = ui.areatarget.get()
	    xweight = ui.xweight.get()
	    for rou in range(0,100): sur[i].round()
	    print "Pot:",sur[i].u
	    wmesh(i, sur[i].x[0:sur[i].x.shape[0]/2], sur[i].x[sur[i].x.shape[0]/2:])
	self.after_idle(self.idle)
	print "End idle"

ui = UI()
ui.mainloop()




