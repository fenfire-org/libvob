# 
# Copyright (c) 2003, Tuomas J. Lukka
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
from java.lang import Thread
import math

def run():

    outfile = open("RES", "a")
    outfile.write("---\n")

    r = java.util.Random(java.lang.System.currentTimeMillis())
    for i in range(0,100): # drive in: problem with java.util.Random
	r.nextDouble()

    offs = .25
    def f():
	return r.nextDouble() * 2 * offs  - offs
	
    def cs(vs, x, y):
	return vs.coords.ortho(0, 0, 100 + 60 * x, 100 + 60 * y, 60, 60)

    vs = getvs()
    vs.put(background((0,0,0)))

    c1 = (.57, .68, .72)
    c2s = [
	(.60, .64, .65),
	(.10, .94, .95),
	(.0, .0, .0),
	(.1, .1, .1),
    ]
    ds = [
	(.12, 0, 0),
	(.12, .12, .12),
	(.12, -.12, .12)
    ]

    trials = [
	[c1, c2, d] for c2 in c2s for d in ds
    ]


    while 1:
	render(vs)

	tri = r.nextInt(len(trials))
	t = trials[tri]
	color1 = t[0]
	color2 = t[1]
	delta = t[2]

	timeScrub()

	vs1 = getvs()
	vs1.put(background((0,0,0)))
	qu1 = coloredQuad(color1)
	qu2 = coloredQuad(color2)
	qu1d = coloredQuad((color1[0] + delta[0], color1[1] + delta[1], color1[2] + delta[2]))


	for x in range(0, 14):
	    for y in range(0, 10):
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

	outfile.write("%s %s %s %s %s %s %s %s %s %s %s\n" %
	    (
		color1[0],
		color1[1],
		color1[2],
		color2[0],
		color2[1],
		color2[2],
		delta[0],
		delta[1],
		delta[2],
		var,
		msec
		))

	outfile.flush()

	if var == 0:
	    mul = 1 + .6 * r.nextDouble()
	else:
	    mul = 1 - .6 * r.nextDouble()
	trials[tri][2] = (
	    mul * delta[0],
	    mul * delta[1],
	    mul * delta[2],
	)
		

	timeScrub()
	Thread.sleep(1200)
	timeScrub()


