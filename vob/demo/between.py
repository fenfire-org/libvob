# (c): Matti J. Katila and Benja Fallenstain

import vob


class Scene:
    def __init__(self):
        self.mousePt = [300, 300]

    def mouse(self, ev):
        self.mousePt = [ev.getX(), ev.getY()]
        if self.vs != None:
            self.vs.anim.switchVS()

    def scene(self, vs):
        self.vs = vs
        vs.put(background((.1, .1, .9)))


        a = vs.orthoBoxCS(0, "a", 0, 100,100, 1,1, 0,0);
        b = vs.orthoBoxCS(0, "a", 0, self.mousePt[0],
                          self.mousePt[1],1,1, 20,20);

        a = vs.orthoBoxCS(0, "a", 0, 100,100, 1,1, 0,0);
        b = vs.orthoBoxCS(0, "a", 0, self.mousePt[0],
                          self.mousePt[1],1,1, 20,20);

        c = vs.coords.between(a,b);
        vs.put(vob.vobs.RectBgVob(), vs.orthoBoxCS(a, "", 0, -10, -10, 1, 1, 20, 20))
        vs.put(vob.vobs.RectBgVob(), vs.orthoBoxCS(b, "", 0, -10, -10, 1, 1, 20, 20))
        cs = vs.orthoBoxCS(c, "buoy", 0, -10,-10, 1,1, 20,20)
        vs.put(vob.vobs.OvalBgVob(java.awt.Color.yellow),
               cs)
        
        
