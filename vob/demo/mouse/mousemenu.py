# 
# Copyright (c) 2003, Matti J. Katila
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
        self.vs = None
        items = [
            vob.vobs.SelectItemVob('Poromies', None),
            vob.vobs.SelectItemVob('a', None),
            vob.vobs.SelectItemVob('fiksu palomies', None),
            vob.vobs.SelectItemVob('aasialainen', None),
            vob.vobs.SelectItemVob('b \n asdfafdsadfas \n c', None),
            ]
        self.list = vob.vobs.SelectListVob(items)
        self.x, self.y = 250, 270
    
    def mouse(self, ev):
        if ev.getButton() == 3:
            if ev.getType() == ev.MOUSE_CLICKED:
                self.x, self.y = ev.getX(), ev.getY()
                self.vs = None
        else:
            pts = jarray.zeros(3, 'f')
            self.vs.coords.inverseTransformPoints3(self.cs, [ev.getX(), ev.getY(), 0.], pts)

            #print 'points',pts, [ev.getX()-150, ev.getY()-150]
            
            if ev.getType() in [ev.MOUSE_PRESSED, ev.MOUSE_DRAGGED]:
                self.list.preSelect(self.vs, pts[0], pts[1])
            if ev.getType() in [ev.MOUSE_RELEASED, ev.MOUSE_CLICKED]:
                self.list.postSelect(self.vs, pts[0], pts[1])
                
                hit = self.vs.coords.getCSAt(0, ev.getX(), ev.getY(), None)
                if hit >= 0:
                    obj = self.vs.matcher.getKey(hit)
                    print 'obj: ', obj #.getKey()
                else:
                    print 'nothing', hit
                
        vob.AbstractUpdateManager.chg()

    def key(self, key):
        self.x = 150 + self.list.getWidth()
        self.y = 150 + self.list.getHeight()
        vob.AbstractUpdateManager.chg()

    def scene(self, vs):
        if self.vs != None:
            return self.vs
        self.vs = vs
        vs.put(background((.1, .1, .9)))
        
        x, y = 150, 150
        self.cs = vs.orthoBoxCS(0,'asdf', 0, x,y, 1,1, self.x-x, self.y-y)
        vs.put(self.list, self.cs)


currentScene = Scene()
