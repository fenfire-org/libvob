# 
# Copyright (c) 2003, Janne V. Kujala and Tuomas J. Lukka
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

from vob.putil.usertestutil import *
from vob.putil.misc import *
from vob.putil import demowindow
from java.lang import Thread
import math

from vob.color.spaces import YSTtoRGB

gamma = 2.2

def getcol0(st):
    return YSTtoRGB((.5, st[0], st[1]))

def col2rgb(col):
    for i in range(0,3):
        if not (0 <= col[i] <= 1):
            print "WARNING: color not in monitor gamut:", col
            
    rgb = map(lambda x: x**(1./gamma), col)
    return rgb

def getcol(st):
    return col2rgb(getcol0(st))

def run():
    demowindow.w.setLocation(0,0,1600,1200)

    outfile = open("RES", "a")
    outfile.write("---\n")

    r = java.util.Random(java.lang.System.currentTimeMillis())
    for i in range(0,100): # drive in: problem with java.util.Random
	r.nextDouble()

    offs = .25
    def f():
	return r.nextDouble() * 2 * offs  - offs
	
    def cs(vs, x, y):
	return vs.coords.ortho(0, 0, 100 * x, 100 * y, 100, 100)

    vs = getvs()
    #vs.put(background((0,0,0)))
    vs.put(background((.2,.2,.2)))

    c1 = [ (.5, 0) ]
    c2 = [ (.46, .17),
           (.4, .3),
           (0, .5),
           ]
    delta = .1
    L = .5

    d2 = delta / math.sqrt(2)
    trials = [
	[c1, c2, d]
        for c1 in c1
        for c2 in c2
        for d in ( (delta, 0),
                   (d2, d2), 
                   (0, delta),
                   (-d2, d2),
                   (-delta, 0),
                   (-d2, -d2),
                   (0, -delta),
                   (d2, -d2),
                   )
        ]
    trials2 = []

    while 1:
	render(vs)

        if not trials:
            trials, trials2 = trials2, trials

	i = r.nextInt(len(trials))
	t = trials[i]
        if i == len(trials) - 1:
            trials.pop()
        else:
            trials[i] = trials.pop()
        
	color1 = t[0]
	color2 = t[1]
	delta = t[2]

	timeScrub()

	vs1 = getvs()
	vs1.put(background((0,0,0)))

        delta2 = delta
        if r.nextDouble() < .1: delta2 = (0,0)
        
	qu1 = coloredQuad(getcol(color1))
	qu2 = coloredQuad(getcol(color2))
	qu1d = coloredQuad(getcol((color1[0] + delta2[0],
                                   color1[1] + delta2[1])))


	for x in range(0, 16):
	    for y in range(0, 12):
		if (x + y) % 2:
		    q = qu2
		else:
		    if r.nextDouble() < .5:
			q = qu1
		    else:
			q = qu1d
		vs1.put(q, cs(vs1, x, y))

	timeScrub()
		    
	render(vs1)
	(key, msec) = waitkey()
	render(vs)
	if key == "2": 
	    var = 0
	else:
	    var = 1

	outfile.write("%s %s %s %s %s %s %s %s\n" %
	    (
		color1[0], color1[1],
		color2[0], color2[1],
		delta2[0], delta2[1],
		var,
		msec
		))

	outfile.flush()

	if var == 0:
	    mul = 2**(-.1 + .7 * r.nextDouble())
	else:
	    mul = 2**(+.1 - .7 * r.nextDouble())
            
        if delta2 == (0,0):
            mul = 1

        trials2.append( (color1,
                         color2,
                         (mul * delta[0],
                          mul * delta[1])
                         ) )


	#timeScrub()
	#Thread.sleep(100)
	timeScrub()
