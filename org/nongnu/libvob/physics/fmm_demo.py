# (c): Matti J. Katila

from org.nongnu.libvob import physics
from org.nongnu.libvob.physics import fmm
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
            self.simulator.solve(2)
            AbstractUpdateManager.setNoAnimation()
            AbstractUpdateManager.chg()
            sleep(0.02)

class Scene:
    def __init__(self):
        global p1,p2,p3,p4
        p = self.particles = physics.impl.ParticleSpaceImpl()
        self.struct = s = fmm.FMMHashTree(0,1500,0,1000, 16, 0.0001)

        p = s.add(physics.Particle(50,50))
        p.setq(0, 1000)
        p = s.add(physics.Particle(150,100))
        p.setq(0, 1000)
        p = s.add(physics.Particle(200,300))
        p.setq(0, 1000)
        p = s.add(physics.Particle(250,200))
        p.setq(0, 1000)
        p = s.add(physics.Particle(300,100))
        p.setq(0, 1000)
        p = s.add(physics.Particle(350,180))
        p.setq(0, 1000)
        
        self.fmm = fmm.FMM(self.struct)

        self.t = Timer(self.fmm)
        self.t.start()

    def scene(self, vs):
        vs.put(background((.8, .4, .9)))

        for p in self.struct.getAllParticles():
            print p.near, p.far, p.F
            print p, p.x(),p.y()
            cs = vs.orthoBoxCS(0, p,0, p.x(), p.y(),
                               1,1,20,20)
            vs.put(vobs.RectBgVob(java.awt.Color.black), cs)

        #self.fmm.proceed(0)
        for p in self.struct.getAllParticles():
            p.x(p.x() + p.F.r()) 
            p.y(p.y() + p.F.i()) 


    def key(self, key):
        AbstractUpdateManager.chg()

        self.t.yet = 0
