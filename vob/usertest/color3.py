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

from vob.demo.lava.color import getcol as getcol0

def getcol(a):
    a = a * math.pi / 180
    return getcol0(.4, .41 * math.cos(a), .41 * math.sin(a))

def normalize(a):
    while a < 0: a += 360
    while a >= 360: a -= 360
    return a

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

    c1 = (0, 36, 72, 108, 144, 180, 216, 252, 288, 324, )

    trials = [
	[c, normalize(c + d2), 15]
        for c in c1
        for d2 in [30, 60, 90]
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
        if r.nextDouble() < .1: delta2 = 0
        
	qu1 = coloredQuad(getcol(color1))
	qu2 = coloredQuad(getcol(color2))
	qu1d = coloredQuad(getcol(color1 + delta2))


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

	outfile.write("%s %s %s %s %s\n" %
	    (
		color1,
		color2,
		delta2,
		var,
		msec
		))

	outfile.flush()

	if var == 0:
	    mul = 2**(-.1 + .7 * r.nextDouble())
	else:
	    mul = 2**(+.1 - .7 * r.nextDouble())
            
        if delta2 == 0:
            mul = 1

        if r.nextDouble() < .5: mul *= -1

        trials2.append( (color1, color2, mul * delta ) )


	#timeScrub()
	#Thread.sleep(100)
	timeScrub()
