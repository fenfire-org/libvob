# (c): Matti J. Katila

import vob, jarray


class Scene:
    def __init__(self):
        self.mousePt = [300, 300]
        self.dir = -1

    def mouse(self, ev):
        #print self.mousePt, self
        self.mousePt = [ev.getX(), ev.getY()]
        if self.vs != None:
            self.vs.anim.switchVS()

        if ev.getType() == vob.VobMouseEvent.MOUSE_CLICKED:
            self.dir *= -1

    def scene(self, vs):
        self.vs = vs
        vs.put(background((.1, .1, .9)))

        anchorAreaCS = vs.orthoBoxCS(0, "area", 0, 150,150, 1,1, 640, 480)
        #anchorAreaCS = vs.orthoCS(0, "area", 0,0.1,0.1, .9, .9)
        vs.put(vob.vobs.RectBgVob(), anchorAreaCS)
        #print 'anchor', anchorAreaCS
        
        buoyCS = vs.coords.buoyOnCircle2(anchorAreaCS,
                                         vs.orthoBoxCS(0, "b",0,
                                                       self.mousePt[0],
                                                       self.mousePt[1],
                                                       1,1, 1,1),
                                         self.dir, 0)
        #print 'buoy', buoyCS
        vs.matcher.add(buoyCS, "Buoy")
        cs = vs.orthoBoxCS(buoyCS, "buoy", 0, -100,-100, 1,1, 200,200)
        #print 'sun', cs
        vs.put(vob.vobs.OvalBgVob(java.awt.Color.yellow),
               cs)
        #     vs.translateCS(buoyCS, "tr", 0,0, -100))
        
        
