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
import vob.demo.lava.noise

from vob.color.spaces import YSTtoRGB,linear_to_monitor

def getcol0(st):
    return YSTtoRGB((.5, st[0], st[1]))

def col2rgb(col):
    for i in range(0,3):
        if not (0 <= col[i] <= 1):
            print "WARNING: color not in monitor gamut:", col
            
    rgb = linear_to_monitor(col)
    return rgb

def getcol(st):
    return col2rgb(getcol0(st))

def run():
    demowindow.w.setLocation(0,0,1600,1200)

    outfile = open(",,RES", "a")
    outfile.write("---\n")

    r = java.util.Random(java.lang.System.currentTimeMillis())
    for i in range(0,100): # drive in: problem with java.util.Random
	r.nextDouble()

    offs = .25
    def f():
	return r.nextDouble() * 2 * offs  - offs
	
    def cs(vs, x, y):
	return vs.coords.ortho(0, 0, 100 * x, 100 * y, 100, 100)

    vs_blank = getvs()
    vs_blank.put(background((.2,.2,.2)))
 
    vs = getvs()
    #vs.put(background((0,0,0)))
    #vs.put(background((.2,.2,.2)))
    noise = vob.demo.lava.noise.Scene()
    vs = noise.scene(vs)

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

    frame = getDList("""
    Color .7 .7 .7
    Begin QUADS
    Vertex 0 0
    Vertex 1600 0
    Vertex 1600 200
    Vertex 0 200

    Vertex 0 0
    Vertex 0 1200
    Vertex 200 1200
    Vertex 200 0

    Vertex 0 1200
    Vertex 1600 1200
    Vertex 1600 1000
    Vertex 0 1000

    Vertex 1600 0
    Vertex 1600 1200
    Vertex 1400 1200
    Vertex 1400 0

    End
    """)

    vs.put(frame, 0)
    vs_blank.put(frame, 0)

    while 1:
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

	vs1 = getvs()
	vs2 = getvs()
	vs1.put(background((0,0,0)))
	vs2.put(background((0,0,0)))

        delta2 = delta
        if r.nextDouble() < .1: delta2 = (0,0)
        
	qu1 = coloredQuad(getcol(color1))
	qu2 = coloredQuad(getcol(color2))
	qu1d = coloredQuad(getcol((color1[0] + delta2[0],
                                   color1[1] + delta2[1])))


	for x in range(0, 16):
	    for y in range(0, 12):
		if (x + y) % 2:
                    vs1.put(qu2, cs(vs1, x, y))
                    vs2.put(qu2, cs(vs2, x, y))
		else:
                    vs1.put(qu1, cs(vs1, x, y))
                    vs2.put(qu1d, cs(vs2, x, y))

        vs1.put(frame, 0)
        vs2.put(frame, 0)


        def showNoise():
            global t0
            t = java.lang.System.currentTimeMillis()
            if not globals().has_key("t0") or t - t0 > 250:
                t0 = t
                render(vs)
                noise.scene(vs)
            
            
        wait(2000, showNoise)

	render(vs1)
	timeScrub()
        wait(1500)

        wait(2000, showNoise)

	render(vs2)
	wait(1500)
        
	(key, msec) = waitkey(showNoise)

	render(vs_blank)
        wait(500)
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


