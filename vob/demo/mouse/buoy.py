# 
# Copyright (c) 2005, Matti J. Katila
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


import vob, jarray


class Scene:
    def __init__(self):
        self.mousePt = [400, 400]
        self.mousePt2 = [300, 300]
        self.dir = -1

    def mouse(self, ev):
        print self.mousePt
        self.mousePt = [ev.getX(), ev.getY()]
        if hasattr(self, 'vs'):
            self.vs.anim.switchVS()

        if ev.getType() == vob.VobMouseEvent.MOUSE_CLICKED:
            self.dir *= -1

    def scene(self, vs):
        self.vs = vs
        vs.put(background((.1, .1, .9)))

        anchorAreaCS = vs.orthoBoxCS(0, "area", 0, 150,150, 1,1, 640, 480)
        vs.put(vob.vobs.RectBgVob(), anchorAreaCS)
        
        buoyCS = vs.coords.buoyOnCircle2(anchorAreaCS,
                                         vs.orthoBoxCS(0, "b",0,
                                                       self.mousePt[0],
                                                       self.mousePt[1],
                                                       1,1, 1,1),
                                         self.dir, 0)

        vs.put(vob.vobs.SimpleConnection(.5,.5,.5,.5), anchorAreaCS, buoyCS)

        vs.matcher.add(buoyCS, "Buoy")
        cs = vs.orthoBoxCS(buoyCS, "buoy", 0, -100,-100, 1,1, 200,200)
        vs.put(vob.vobs.OvalBgVob(java.awt.Color.yellow), cs)
        
        
        # child buoy

        circle = vs.coords.orthoBox(buoyCS, 0, -200,-200, 1,1,400,400)
        #anchor = vs.coords.center(buoyCS)
        anchor = vs.coords.translate(0, self.mousePt2[0], self.mousePt2[1])
        anchor = vs.coords.box(anchor, 1, 1);
        childBuoy = vs.coords.buoyOnCircle2(circle, anchor, self.dir, 0)
        box = vs.coords.orthoBox(childBuoy, 0, -100,-100, 1,1,200,200)
        vs.put(vob.vobs.OvalBgVob(java.awt.Color.red), box)

        vs.put(vob.vobs.SimpleConnection(.5,.5,.5,.5), buoyCS, childBuoy)
