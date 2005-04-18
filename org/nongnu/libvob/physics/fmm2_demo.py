# 
# Copyright (c) 2004, Matti J. Katila
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


from org.nongnu.libvob import physics
from org.nongnu.libvob import vobs, AbstractUpdateManager

from java.lang import Math, Thread
from time import sleep


class Timer(Thread):
    def __init__(self, sim):
        Thread.__init__(self)
        self.simulator = sim
        self.yet = 1
    def run(self):
        while self.yet:
            self.simulator.simulate(0.5)
            AbstractUpdateManager.setNoAnimation()
            AbstractUpdateManager.chg()
            sleep(0.02)

## if 0: #class Simu:
##     def __init__(self, p):
##         self.p = p
##     def simulate(self, t):
##         x0 = p.getParticleStates()
##         x1 = p.deriv(x0,t)
##         p.setParticleStates(

class Scene:
    def __init__(self):
        global p1,p2,p3,p4,p5,p6,p7,p8,p9,p10
        p = self.particles = physics.impl.ParticleSpaceImpl()
        
        p1 = p.createStagnantParticle("a", physics.Particle(400,350))
        p2 = p.createLiveParticle("b",physics.Particle(250,50))
        p3 = p.createLiveParticle("c", physics.Particle(320,200))
        p4 = p.createLiveParticle("d", physics.Particle(400,600))
        p5 = p.createLiveParticle("e", physics.Particle(460,610))
        p6 = p.createLiveParticle("f", physics.Particle(450,620))
        p7 = p.createLiveParticle("g", physics.Particle(440,630))
        p8 = p.createLiveParticle("h", physics.Particle(430,640))
        p9 = p.createLiveParticle("j", physics.Particle(420,650))
        p10 = p.createLiveParticle("k", physics.Particle(401,660))

        for pa in [p1, p2,p3,p4, p5,p6,p7,p8,p9,p10]:
            p.setRepulsionForce(pa, 1000)

        p.init()


        for pa in [p2,p3,p4]:
            p.connectParticlesWithSpringMassModel(p1, pa, 100, 0.7)
        for pa in [p5,p6,p7]:
            p.connectParticlesWithSpringMassModel(p2, pa, 100, 0.7)
        for pa in [p8,p9,p10]:
            p.connectParticlesWithSpringMassModel(p3, pa, 100, 0.7)

        #.connectParticlesWithSpringMassModel(p5, p10, 1000, 0.7)

        self.simul = physics.ParticleSimulator(p)
        
        
        self.t = Timer(self.simul)
        self.t.start()

        self.id = None

    def scene(self, vs):
        global p1,p2,p3,p4
        vs.put(background((.8, .4, .9)))
        self.vs = vs

        for p in [p1,p2,p3,p4,p5,p6,p7,p8,p9,p10]:
            #print p.p
            cs = vs.orthoBoxCS(0, p,0, p.x(), p.y(),
                               1,1,20,20)
            vs.put(vobs.RectBgVob(java.awt.Color.black), cs)
            vs.coords.activate(cs)

    def key(self, key):
        AbstractUpdateManager.chg()

        self.t.yet = 0
        
    def mouse(self, ev):
        if self.id == None:
            cs = self.vs.getCSAt(0, ev.getX(), ev.getY(),None)
            if cs <= 0: return
            id = self.vs.matcher.getKey(cs)
            #print 'id',id
            if id == None: return
            self.id = id

        p = self.id
        p.p.x(ev.getX())
        p.p.y(ev.getY())
        if ev.getType() == ev.MOUSE_RELEASED:
            self.id = None
            AbstractUpdateManager.chg()
