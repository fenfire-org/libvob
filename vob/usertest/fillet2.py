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

from vob.putil.usertestutil import *
from vob.putil.misc import *
from java.lang import Thread,System
import math
from random import shuffle

from vob.demo.multifil import randgraph

from vob.putil import demowindow
from vob.putil.saveanim import saveframe

def run():
    demowindow.w.setLocation(0,0,1600,1200)

    
    seed = java.lang.System.currentTimeMillis()

    outfile = open("fillet.dat", "a")
    outfile.write("--- %s\n" % (seed,))

    r = java.util.Random(seed)
    for i in range(0,100): # drive in: problem with java.util.Random
	r.nextDouble()

    sc = randgraph.Scene()
    sc.size = 30
    sc.box = 800 #1100
    sc.blend3d = 1
    sc.drawInside = 0
    #sc.depthColor = 0

    vs0 = getvs()
    vs0.put(background((.5,1,.2)))

    graphs = []
    for i in range(0,3):
        for path2 in (0,1):
            for N in [ 3 * n for n in range(2,10) ]:
                graphs.append( (r.nextInt(), r.nextInt(), path2, N) )

    trials = []
    for g in graphs:
        for fillets in (0,1):
            trials.append( (g[0], g[1], g[2], g[3], fillets) )

    shuffle(trials, r.nextDouble)


    t0 = java.lang.System.currentTimeMillis()
    while trials:
	render(vs0)

        sc.seed, sc.seed2, sc.path2, sc.N, fillets = trials.pop()
        if fillets:
            sc.rot = (0,0,1,0)
        else:
            sc.rot = (0,0,0,1)

        sc.hl_only = 1
        sc.fillets = 0
        sc.thick = 0
        vs1 = getvs()
        sc.scene(vs1)

        sc.hl_only = 0
        sc.fillets = fillets
        sc.thick = 1
        vs2 = getvs()
        sc.scene(vs2)

	timeScrub()

	#renderOnly(vs1)

        warn = ""
        #t1 = java.lang.System.currentTimeMillis()
        #t = 1500 - (t1 - t0)
        #if t > 0:
        #    Thread.sleep(t)
        #else:
        #    warn += "A%d" % (-t,)
        #    print "Warning: did not get any sleep"

        #swapBuffers()

        t0 = java.lang.System.currentTimeMillis()
        renderOnly(vs2)
        t1 = java.lang.System.currentTimeMillis()
        t = 1000 - (t1 - t0)
        if t > 0:
            Thread.sleep(t)
        else:
            warn += "B%d" % (-t,)
            print "Warning: did not get any sleep"

        swapBuffers()
        Thread.sleep(200)
        
	render(vs0)
	(key, msec) = waitkey()
        t0 = java.lang.System.currentTimeMillis()

        if key == "Print":
            globals()["frame"] = globals().get("frame", 0) + 1;
            saveframe("frame%d.png" % (frame, ), demowindow.w)

	outfile.write("%s %s %s %s  %s %s %s %s\n" % (
            key, msec, sc.fillets, sc.N, sc.path2,
            sc.seed, sc.seed2, warn,
            ))

	outfile.flush()

	timeScrub()


    System.exit(0)
