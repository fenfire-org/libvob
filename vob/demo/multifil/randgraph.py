# 
# Copyright (c) 2003, Tuomas J. Lukka and Janne Kujala
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
import math

import vob
from org.nongnu.libvob.gl import GL, GLRen
from vob.putil import cg
from vob.putil.misc import *
from vob.putil.demokeys import *

from vob.fillet import light3d
from vob.putil.demowindow import w
from org.nongnu.libvob.input import RelativeAxisListener
from random import Random
import math

def quatmul(a, b):
    return (
        a[3] * b[0] + a[0] * b[3] + a[1] * b[2] - a[2] * b[1],
        a[3] * b[1] - a[0] * b[2] + a[1] * b[3] + a[2] * b[0],
        a[3] * b[2] + a[0] * b[1] - a[1] * b[0] + a[2] * b[3],
        a[3] * b[3] - a[0] * b[0] - a[1] * b[1] - a[2] * b[2],
        )

    
class Scene:
    "Random graph"
    def __init__(self):
        self.rot = (0, 0, 0, 1)
        def rotX(a): self.rot = quatmul((0,-math.sin(.005*a),0,math.cos(.005*a)), self.rot)
        def rotY(a): self.rot = quatmul((-math.sin(.005*a), 0, 0, math.cos(.005*a)), self.rot)

    	self.key = KeyPresses(
            self, 
	    SlideLin("x", 200, 20, "x", "Left", "Right"),
	    SlideLin("y", 300, 20, "y", "Up", "Down"),
            Action("Rotate Left", "Left", lambda *x: rotX(10)),
            Action("Rotate Right", "Right", lambda *x: rotX(-10)),
            Action("Rotate Up", "Up", lambda *x: rotY(-10)),
            Action("Rotate Down", "Down", lambda *x: rotY(10)),
            Action("Use display list", "D", self.compile, noAnimation=1),
	    SlideLin("N", 21, 3, "N", "N", "n"),
	    SlideLin("seed", 42, 1, "seed number", "A", "a"),
	    SlideLin("seed2", 42, 1, "highlihght seed number", "Z", "z"),
	    Toggle("path2", 0, "length-2 path", "2"),
	    Toggle("perspective", 0, "perspective", "F"),
            *light3d.commonkeys
	)
        self.list = None
        self.box = 800
        self.hl_only = 0

        class Listener(RelativeAxisListener):
            def __init__(self, changed):
                self.changed = changed

            def changedRelative(self, x):
                self.changed(x)
                vob.AbstractUpdateManager.setNoAnimation()
                vob.AbstractUpdateManager.chg()

        if 0:
            self.ps2 = vob.input.impl.PS2MouseDevice("/dev/input/mouse1", "main",
                                                     vob.input.impl.PS2MouseDevice.IMPS_PROTO)
            self.naxes = len(self.ps2.getAxes())
            self.ps2.getAxes()[0].setMainListener(Listener(rotX))
            self.ps2.getAxes()[1].setMainListener(Listener(rotY))

    def compile(self, *args):
        if self.list:
            self.list = None
            return

        list = GL.createDisplayList();
        vs = w.createVobScene()
        self.putGraph(vs, 0, 800);

        list.startCompile(w.window)

        #w.renderStill(vs, 0)
        vs.coords.renderInterp(w.getRenderingSurface(),
                               vs.map, None, None, 0, 0, 1);

        list.endCompile(w.window)

        self.list = GLRen.createCallListCoorded(list)

    def putGraph(self, vs, cs, box):
        #self.N = 24
        #self.seed = 344239589
        #self.seed2 = -40716569 
        
        rng = Random(self.seed)
        N2 = 2*self.N/3

        nodes = [ {
            "cs" : vs.orthoBoxCS(cs, "node%s" % i,
                                 (rng.random() - .5) * box,
                                 (rng.random() - .5) * box - 0.5*self.size,
                                 (rng.random() - .5) * box - 0.5*self.size,
                                 1, 1, self.size, self.size),
            "conns" : [ ],
            } for i in range(0,self.N) ]

        for node in nodes[:N2]:
            while 1:
                a = rng.choice(nodes[N2:])
                b = rng.choice(nodes[N2:])
                if a != node and b != node and a != b: break
            
            node["conns"].append(a["cs"])
            node["conns"].append(b["cs"])
            a["conns"].append(node["cs"])
            b["conns"].append(node["cs"])

        if self.seed2 != None:
            rng = Random(self.seed2)
            nodes0 = nodes
            while nodes0:
                x = rng.choice(nodes0)

                def p2(x, y):
                    return filter(lambda z: z in y["conns"], x["conns"])

                nodes1 = filter(lambda y: y != x and y["cs"] not in x["conns"],
                                nodes)

                if 1:
                    # path2 = direct neighbor
                    if self.path2:
                        nodes2 = filter(lambda y: y["cs"] in x["conns"], nodes)
                    else:
                        nodes2 = nodes1
                else:
                    if self.path2:
                        nodes2 = filter(lambda y: p2(x, y), nodes1)
                    else:
                        nodes2 = filter(lambda y: not p2(x, y), nodes1)
                        
                if nodes2: break

                nodes0 = filter(lambda y: x != y, nodes0)

            if nodes2:
                y = rng.choice(nodes2)
            else:
                print "Cannot find a pair of nodes of the requested type!!"
                y = None
        else:
            x, y = None, None

	def pc(conns, cs):
            if self.hl_only:
                vs.put(getDListNocoords("Color 1 0 0"))
                if x != None: vs.put(conns, cs + [x["cs"]])
                if y != None: vs.put(conns, cs + [y["cs"]])
                return
                
            for node in nodes:
                if node in [x, y]:
                    vs.put(getDListNocoords("Color 1 0 0"))
                else:
                    vs.put(getDListNocoords("Color .2 .2 1"))
                vs.put(conns, cs + [node["cs"]] + node["conns"])

            if 0:
                for node in [x,y]:
                    vs.put(getDList("""
                    PushAttrib ENABLE_BIT CURRENT_BIT
                    Disable DEPTH_TEST
                    Disable VERTEX_PROGRAM_ARB
                    Color 1 .25 .25
                    LineWidth 3
                    Begin LINES
                    Vertex 0 %(k2)s  
                    Vertex %(k)s %(k2)s 
                    Vertex %(k2)s 0
                    Vertex %(k2)s %(k)s
                    End
                    PopAttrib
                    Color .2 .2 1
                    """ % { "k" : self.size, "k2" : self.size * .5}), node["cs"])

        light3d.drawFillets(self, vs, pc)
        
    def scene(self, vs):
	vs.put( background((.5,1,.2)))

        size = vs.getSize()

        cs = 0
        if self.perspective:
            fov = 45
            zmin = 1
            zmax = 2500
            dist = 1500

            y = zmin * math.tan(fov * math.pi / 360)
            aspect = float(size.width) / size.height
            
            vs.put(getDListNocoords("""
            MatrixMode PROJECTION
            LoadIdentity
            Frustum %s %s %s %s %s %s
            Scale 1 -1 -1
            Translate -%s -%s %s
            MatrixMode MODELVIEW
            """ % (-aspect * y, aspect * y, -y, y, zmin, zmax,
                   0.5 * size.width, 0.5 * size.height, dist)))

        cs = vs.translateCS(cs, "Trans",
                            0.5 * size.width,
                            0.5 * size.height,
                            0.5 * self.box);
        
        cs = vs.coords.rotateQuaternion(cs,  *self.rot)
        vs.matcher.add(cs, "Rot")

        if self.list:
            vs.put(self.list, cs)
        else:
            self.putGraph(vs, cs, self.box)
