# (c): Matti J. Katila and Benja Fallenstain

import vob


class Scene:
    def __init__(self):
        self.mousePt = [300, 300]

    def mouse(self, ev):
        self.mousePt = [ev.getX(), ev.getY()]
        if hasattr(self, 'vs'):
            self.vs.anim.switchVS()

    def scene(self, vs):
        self.vs = vs
        vs.put(background((.1, .1, .9)))

        a = vs.coords.translate(0, 100, 100, 0)
        a = vs.coords.scale(a, 10, 10)
        a = vs.coords.box(a, 30, 5)

        b = vs.coords.translate(0, self.mousePt[0], self.mousePt[1], 0)
        b = vs.coords.scale(b, 10, 10)
        b = vs.coords.box(b, 30, 5)

        ac = vs.coords.center(a)
        bc = vs.coords.center(b)

        ac = vs.coords.box(ac, 0, 0)
        bc = vs.coords.box(bc, 0, 0)

        ac = vs.coords.translate(ac, 0, 0, .5)
        bc = vs.coords.translate(bc, 0, 0, .5)

        vs.put(vob.vobs.SimpleConnection(.5,.5,.5,.5, java.awt.Color.white),
               ac, bc)

        c = vs.coords.between(ac, bc)
        vs.put(vob.vobs.RectBgVob(), a)
        vs.put(vob.vobs.RectBgVob(), b)
        cs = vs.orthoBoxCS(c, "buoy", 0, -10,-10, 1,1, 20,20)
        vs.put(vob.vobs.OvalBgVob(java.awt.Color.yellow),
               cs)
        
        
