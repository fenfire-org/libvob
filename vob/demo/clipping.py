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

import vob, java

# (AWT only) Create a few rectangles and clip them

class Scene:
    def key(self, k):
        vob.AbstractUpdateManager.chg()
        
    def mouse(self, m): pass

    def scene(self, scene):
        scene.put(background((1,1,.8)))

        cs1 = scene.orthoBoxCS(0, 'cs1', 0, 100, 100, 1, 1, 100, 100)
        cs2 = scene.orthoBoxCS(0, 'cs2', 0, 150, 110, 1, 1, 100, 100)
        cs3 = scene.orthoBoxCS(0, 'cs3', 0, 120, 120, 1, 2, 100, 100)
        cs4 = scene.orthoBoxCS(0, 'cs4', 0, 180, 180, 2, 2, 100, 100)

        v = vob.vobs.RectVob(java.awt.Color.black, 2)
        scene.put(v, cs1)
        
        scene.map.clip(cs1)
        scene.map.clip(cs3)
        v = vob.vobs.RectVob(java.awt.Color.blue, 2)
        scene.put(v, cs2)
        scene.map.unclip()
        
        v = vob.vobs.RectVob(java.awt.Color.red, 2)
        scene.put(v, cs3)
        scene.map.unclip()
        
        v = vob.vobs.RectVob(java.awt.Color.green, 2)
        scene.put(v, cs4)

        
