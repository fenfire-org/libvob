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
import random

def run():

    outfile = open("RES.bw", "a")
    outfile.write("---\n")

    r = java.util.Random(java.lang.System.currentTimeMillis())
    r2 = random.Random()
    for i in range(0,100): # drive in: problem with java.util.Random
	r.nextDouble()

    def cs(vs, x, y):
	return vs.coords.ortho(0, 0, 100 + 60 * x, 100 + 60 * y, 60, 60)

    vs = getvs()
    vs.put(background((0,0,0)))

    trials = []

    for i in range(0,6):
        for j in range(0,19):
            c1 = (0, .2, .45, .55, .8, 1)[i]
            c2 = .5
            delta = (j - 9) / 9.0 / 3.5
            if not (0 <= c1 + delta <= 1): continue
            trials += [(c1, c2, delta)]
            
    r2.shuffle(trials)

    while 1:
	render(vs)

        if 0:
            color1 = r.nextDouble()

            color2 = .5 # r.nextDouble()

            if r.nextDouble() < .10:
                delta = 0
            else:
                delta = -10
                while not (0 <= color1 + delta <= 1):
                    delta = r.nextDouble() * .8 - .4
        else:
            color1, color2, delta = trials.pop()

	timeScrub()

	vs1 = getvs()
	vs2 = getvs()
	vs1.put(background((0,0,0)))
	vs2.put(background((0,0,0)))

	qu1 = coloredQuad((color1,color1,color1))
	qu2 = coloredQuad((color2,color2,color2))
	qu1d = coloredQuad((color1 + delta, color1 + delta, color1 + delta))

	q1 = [qu1, qu2]
	q2 = [qu1d, qu2]
	

        w = 14
        h = 10
	for x in range(0, w):
	    for y in range(0, h):
		if r.nextDouble() < .5:
		    ind = 1
		else:
		    ind = 0
                if ((x == w/2-1 or x == w/2) and 
                    (y == h/2-1 or y == h/2)):
                    ind = (x ^ y) & 1
		vs1.put(q1[ind], cs(vs1, x, y))
		vs2.put(q2[ind], cs(vs2, x, y))
		

	timeScrub()

	render(vs1)

	Thread.sleep(1000)

	render(vs)

	Thread.sleep(1000)

	render(vs2)

	(key, msec) = waitkey()
	# print delta

	render(vs)

	# print "KEY: ", key, msec
	if key == "s": 
	    var = 0
	else:
	    var = 1

	# print var

	outfile.write("%s %s %s %s %s\n" %
	    (
		color1,
		color2,
		delta,
		var,
		msec
		))

	outfile.flush()

	timeScrub()
	Thread.sleep(1200)
	timeScrub()
