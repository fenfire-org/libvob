# 
# Copyright (c) 2005, Benja Fallenstein
#
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

from org.nongnu.libvob.fn import *
from org.nongnu.libvob.lob import *
from org.nongnu.libvob.lob.lobs import *
from javolution.lang import *

from java.awt import Color

import vob, java, org

vob.putil.demo.usingNormalBindings = 0
vob.putil.demo.chgAfterKeyEvent = 0

class Table(TableLob.Table):
    def getRowCount(self): return 10
    def getColumnCount(self): return 10

    def getLob(self, row, col):
        return Lobs.filledRect(Color(50 + row*10, 0, 50 + col*10))
        #return Lobs.rect(Color.red, 2)

def render(scene, layout, key, x, y):
    cs = scene.coords.translate(0, x, y)
    scene.matcher.add(0, cs, key)
    layout.render(scene, cs, cs, 1, 1)

def renderLob(scene, lob, key, x, y):
    size = lob.getSizeRequest()
    render(scene, lob.layout(size.natW, size.natH), key, x, y)

class Scene:
    def key(self, k):
        print 'key', k

        if self.textlob().key(k):
            self.anim.animate()
        
    def mouse(self, m): pass

    def textlob(self):
        t = Text.valueOf(text.get())
        loblist = TextLobList.newInstance(Lobs.font(), t)
        loblist = KeyLobList.newInstance(loblist, "text")
        loblist = Linebreaker.newInstance(Axis.X, loblist, 300)
        lob = BoxLob.newInstance(Axis.Y, loblist)
        lob = Lobs.frame(lob, None, Color.black, 1, 5, 0)

        cursor_lob = Lobs.line(java.awt.Color.black, 0, 0, 0, 1)
        cursor_lob = Lobs.key(cursor_lob, "textcursor")
        lob = Lobs.decorate(lob, cursor_lob, "text", textcursor.getInt())

        lob = TextKeyController.newInstance(lob, text, textcursor)
        
        return lob

    #foo = 0
    def scene(self, scene):
        _ = scene
        matcher = org.nongnu.libvob.layout.IndexedVobMatcher()

        scene = org.nongnu.libvob.VobScene(_.map, _.coords, matcher,
                                           _.gfxapi, _.window, _.size)
            
        scene.put(background((1,1,.8)))

        #if self.foo:
        #    cs1 = scene.coords.box(0, 50, 50, 1, 1)
        #    cs2 = scene.coords.box(0, 80, 80, 1, 1)
        #else:
        #    cs1 = scene.coords.box(0, 80, 50, 1, 1)
        #    cs2 = scene.coords.box(0, 50, 80, 1, 1)
        #
        #self.foo = not self.foo
        #
        #scene.matcher.add(0, cs1, "foo")
        #scene.matcher.add(0, cs2, "bar")
        #
        #scene.put(org.nongnu.libvob.vobs.SimpleConnection(0,0,0,0,java.awt.Color.black), cs1, cs2)

        lob = TableLob.newInstance(Table())
        layout = lob.layout(400, 300)
        render(scene, layout, "table", 100, 100)

        lob = Lobs.text(Lobs.font(Color.blue), "Hello world!")
        lob = Lobs.frame3d(lob, None, Color.red, 1, 5, 0, 1)
        size = lob.getSizeRequest()
        render(scene, lob.layout(size.natW, size.natH), "hello world",
               300-size.natW/2, 50)

        renderLob(scene, self.textlob(), "textbox", 600, 100)

        print 'scene rendered'

        return scene
        

textcursor = SimpleModel.newInstance(-1)

text = SimpleModel.newInstance("""Copyright (c) 2005, Benja Fallenstein

This file is part of Libvob.

Libvob is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

Libvob is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Libvob; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.""")
