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

from org.nongnu.libvob.lob import *
from org.nongnu.libvob.lob.lobs import *
from javolution.lang import *

from java.awt import Color

import vob, java

class Table(TableLob.Table):
    def getRowCount(self): return 10
    def getColumnCount(self): return 10

    def getLob(self, row, col):
        return Lobs.filledRect(Color(50 + row*10, 0, 50 + col*10))
        #return Lobs.rect(Color.red, 2)

def render(scene, layout, x, y):
        cs = scene.coords.translate(0, x, y)
        layout.render(scene, cs, 0, 1, 1)

class Scene:
    def key(self, k):
        vob.AbstractUpdateManager.chg()
        
    def mouse(self, m): pass

    def scene(self, scene):
        scene.put(background((1,1,.8)))

        lob = TableLob.newInstance(Table())
        layout = lob.layout(400, 300)
        render(scene, layout, 100, 100)

        font = SimpleLobFont.newInstance("serif", 0, 16, Color.blue)
        text = Text.valueOf("Hello, World!")
        lob = BoxLob.newInstance(Axis.X, TextLobList.newInstance(font, text))

        size = lob.getSizeRequest()
        layout = lob.layout(size.natW, size.natH)
        render(scene, layout, 300-size.natW/2, 50)

        print 'scene rendered'
        
