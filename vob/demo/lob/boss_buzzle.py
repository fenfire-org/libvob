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


import vob

style = vob.GraphicsAPI.getInstance().getTextStyle("sansserif", 0, 24)


class Block:
    def __init__(self, identity, i,j):
        self.id, self.i, self.j = str(identity), i,j

    def render(self, vs, into):

        translate = vs.coords.translate(into, self.j, self.i)
        vs.matcher.add(0, translate, self.id)
        vs.coords.activate(translate)

        # put vobs
        vs.put(vob.vobs.RectBgVob(java.awt.Color.yellow), translate)
        vs.put(vob.vobs.TextVob(style, self.id), translate)
        
class BossBuzzle:
    def __init__(self, rows, cols):
        self.rows, self.cols = rows, cols
        self.blocks = []
        ind = 1
        for i in range(rows):
            for j in range(cols):
                if ind == rows*cols: break
                self.blocks.append(Block(ind,i,j))
                ind += 1

        self.empty = Block('Empty', 3,3)

    def render(self, vs, into):
        for i in self.blocks:
            i.render(vs, into)

    def changeEmptyToKey(self, key):
        for i in self.blocks:
            if i.id != key: continue

            # swap
            x,y = i.i, i.j
            i.i, i.j = self.empty.i, self.empty.j
            self.empty.i, self.empty.j = x,y
            


boss = BossBuzzle(4,4)

class Scene:
    def key(self, k):
        print 'key: ',k

    def mouse(self, ev):
        if hasattr(self, 'vs'):
            cs = self.vs.getCSAt(self.into, ev.getX(), ev.getY(), None)
            if cs > 1:
                key = self.vs.matcher.getKey(cs)
                print key
                if ev.getType() == ev.MOUSE_CLICKED:
                    boss.changeEmptyToKey(key)
                    self.vs.anim.animate()

    def scene(self, vs):
        self.vs = vs
        vs.put(background((.3,.6,.8)))

        w, h = vs.size.width, vs.size.height
        translateCS = vs.coords.translate(0, w/2.0, h/2.0)
        scaleCS = vs.coords.scale(translateCS, 50,50)
        translateCS2 = vs.coords.translate(scaleCS, -2, -2)
        self.into = translateCS2
        boss.render(vs, self.into)
        
