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
from javolution.realtime import PoolContext

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

class Scene:
    def key(self, k):
        print 'key', k

        PoolContext.enter()
        try:
            if self.lob().key(k):
                self.anim.animate()
        finally:
            PoolContext.exit()
        
    def mouse(self, m): pass

    def lob(self):
        lobs = SimpleLobList.newInstance()

        lob = TableLob.newInstance(Table())
        lob = Lobs.request(lob, 400, 400, 400, 300, 300, 300)
        lob = Lobs.translate(lob, 100, 100)
        lob = Lobs.key(lob, "table")
        lobs.add(lob)

        lob = Lobs.hbox(Lobs.text(Lobs.font(Color.blue), "Hello world!"))
        lob = Lobs.frame3d(lob, None, Color.red, 1, 5, 0, 1)
        lob = Lobs.align(lob, .5, .5)
        lob = Lobs.request(lob, 400, 400, 400, 100, 100, 100)
        lob = Lobs.translate(lob, 100, 0)
        lob = Lobs.key(lob, "hello world")
        lobs.add(lob)

        lob = Components.textArea(text, textcursor, Lobs.font())
        lob = lob.layoutOneAxis(300)
        lob = Lobs.translate(lob, 600, 100)
        lob = Lobs.key(lob, "textbox")
        lobs.add(lob)

        return Tray.newInstance(lobs, 0)


    def scene(self, scene):
        _ = scene
        matcher = org.nongnu.libvob.layout.IndexedVobMatcher()

        scene = org.nongnu.libvob.VobScene(_.map, _.coords, matcher,
                                           _.gfxapi, _.window, _.size)
            
        scene.put(background((1,1,.8)))

        PoolContext.enter()
        try:
            lob = self.lob()
            lob = lob.layout(scene.size.width, scene.size.height)
            lob.render(scene, 0, 0, 1, 1)
        finally:
            PoolContext.exit()

        print 'scene rendered'

        return scene
        

textcursor = SimpleModel.newInstance(-1)

text = SimpleModel.newInstance("""Copyright (c) 2005, Benja Fallenstein

This file is part of Libvob.

Libvob is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

Libvob is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Libvob; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.""")
