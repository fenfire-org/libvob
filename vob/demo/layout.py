# 
# Copyright (c) 2004, Benja Fallenstein
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

from org.nongnu.libvob.layout import *
from org.nongnu.libvob.layout.Lob import X,Y
from org.nongnu.libvob.vobs import *

from java.awt import Color

import vob, java

inf = java.lang.Float.POSITIVE_INFINITY
style = vob.GraphicsAPI.getInstance().getTextStyle('Serif', 0, 18)
textHeight = style.getHeight(1)

spaceGlue = TemplateLob(RequestChangeLob(Y, Glue(X, 5, 5, 20), 0, textHeight, inf))
strut = TemplateLob(RequestChangeLob(Y, Glue(X, 0, 0, 0), 0, textHeight, inf))
space = BreakPoint(X, spaceGlue, 0, strut, None);

class Scene:
    def __init__(self):
        import sys
        text = """Copyright (c) 2004, Benja Fallenstein. This file is part of Libvob. Libvob is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version. Libvob is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of the GNU Lesser General Public License along with Libvob; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA."""

        box = Breaker(X)
        i = 0

        for char in text:
            if char==' ':
                box.add(space)
            else:
                box.add(TextVob(style, char, 0, i))

            i += 1


        #for word in text.split():
        #    box.add(TextVob(style, word, 0, i))
        #    box.add(glue(X, 5, 5, 20))
        #    i += 1

        box = Between(FilledRectVob(Color.white),
                      ClipLob(Margin(box, 5)),
                      RectVob(Color.black, 2))

        lob = self.lob = Box(X)
        lob.add(Glue(X, 20, 20, inf))
        lob.add(RequestChangeLob(X, box, 100, 500, 500))
        lob.add(Glue(X, 20, 20, inf))
        
    
    def key(self, k):
        vob.AbstractUpdateManager.chg()
        
    def mouse(self, m): pass

    def scene(self, scene):
        scene.put(background((1,1,.8)))

        size = scene.getSize()
        self.lob.setSize(size.width, size.height)
        self.lob.render(scene, 0, 0, 0, size.width, size.height)

        print 'scene rendered'
        
