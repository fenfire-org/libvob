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

#
# This demo shows how vob matcher is used to make
# draggable/moveable objects and also how
# they can be put into something with receiver.

import vob, java, jarray

### Mouse system

class Mover(vob.mouse.MousePressListener,
            vob.mouse.MouseDragListener):
    def __init__(self, animation, cs):
        self.animation = animation
        self.cs = cs

    def pressed(self, x,y):
        return self

    def startDrag(self, x,y):
        self.x, self.y = x, y
    def drag(self, x,y):
        vs = self.animation.getVs()
        vs.coords.setTranslateParams(self.cs, x-self.x,y-self.y)
        self.animation.reuseVS = 1
        self.animation.chg()
    def endDrag(self, x,y):
        vs = self.animation.getVs()
        key = vs.matcher.getKey(vs.matcher.getParent(vs.coords.getCSAt(0, x,y, None)))
        if key != None and isinstance(key, Receiver):
            key.swap = (1 - key.swap)
            self.animation.chg()
            print 'something!'
        else:
            self.animation.chg()

### Matchers

class Moveable:
    def __init__(self):
        self.plexer = vob.mouse.MouseMultiplexer()
        self.knownVS = None
    def move(self, animation, ev, cs):
        vs = animation.getVs()
        moveCS = vs.matcher.getParent(cs)

        if self.knownVS == vs:
            if self.plexer.deliverEvent(ev):
                return 1
        else:
            if vs.matcher.getCS(vs.matcher.getParent(moveCS), self) == moveCS:
                self.plexer.setListener(1, 0, 'Move box', Mover(animation, moveCS))
                self.knownVS = vs
                if self.plexer.deliverEvent(ev):
                    return 1
        return 0
        
class Receiver:
    def __init__(self):
        self.swap = 0

moveable = Moveable()
receiver = Receiver()

class Scene:
    class Animation:
        def __init__(self):
            self._vs = None
            self._generate()

        def setVs(self, vs): self._vs = vs
        def getVs(self): return self._vs

        def noAnimation(self): self.animate = 0
        def animation(self): self.animate = 1

        def chg(self):
            if not self.animate:
                vob.AbstractUpdateManager.setNoAnimation()
            if not self.reuseVS:
                self._vs = None
            self._generate()

        def _generate(self):
            self.animate = 1
            self.reuseVS = 0
            vob.AbstractUpdateManager.chg()
                
    def __init__(self):
        self.animation = self.Animation()

    def scene(self, vs):
	if self.animation.getVs() != None:
	    return self.animation.getVs()
        else: self.animation.setVs(vs)
	vs.put( background((0.1,0.9,0.8)))

        size = jarray.zeros(2, 'f')

        w, h = vs.getSize().getWidth(), vs.getSize().getHeight()
        # 1/5 part of sizes
        w /= 3.
        h /= 3.

    
        receiverCS = vs.translateCS(0, receiver,0,0)
        
        # put red and yellow boxes on screen
        redCS = vs.orthoCS(receiverCS, "RED",0, 0,0 , w,h)
        yellowCS = vs.orthoCS(receiverCS, "YELLOW",0, 2*w, 2*h, w,h)

        if receiver.swap:
            tmp = redCS; redCS = yellowCS; yellowCS = tmp;

        vs.put(vob.vobs.RectBgVob(java.awt.Color.red), redCS)
        vs.put(vob.vobs.RectBgVob(java.awt.Color.yellow), yellowCS)
        vs.coords.activate(redCS)
        vs.coords.activate(yellowCS)


        # now, before putting the object, create moveable..
        self.moveCS = vs.translateCS(0, moveable, 0,0)

        # create a small box which is moveable
        self.boxCS = vs.orthoCS(self.moveCS, "BOX",0, 1.3*w,1.3*h, w/3.,h/3.)
        vs.coords.activate(self.boxCS)

        vs.put(vob.vobs.RectBgVob(java.awt.Color.black), self.boxCS)

    def mouse(self, ev):
        vs = self.animation.getVs()
        cs = vs.getCSAt(0, ev.getX(), ev.getY(), None)
        if cs > -1:
            if moveable.move(self.animation, ev, cs):
                print 'moved'
            else:
                print vs.matcher.getKey(cs)
        else:
            print cs
        

    def key(self, k): pass
        
currentScene = Scene()
