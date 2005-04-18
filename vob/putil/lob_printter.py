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


"""
Util to print out layout object visual output for documentation.
"""

import sys, org, java
import time, string

file = sys.argv[2]

scale = 2

class Scene:
    print 'foo', sys.argv[2:]

    def lob(self):
        global file
        file = string.replace(file, '/', '.')
        c = java.lang.Class.forName(file)
        constructor = c.getConstructors()[0]
        instance = constructor.newInstance([])
        return instance.getLob()
        
        #return vob.lob.Components.label("foo")

    def scene(self, scene):
        _ = scene
        matcher = vob.impl.IndexedVobMatcher()
        scene = vob.VobScene(_.map, _.coords, matcher,
                             _.gfxapi, _.window, _.size)
        scene.put(background((.9,.9,.9)))

        
        #vob.lob.PoolContext.enter()
        try:
            l = self.lob()
            global scale
            scaled = scene.scaleCS(0, "scaled", scale, scale)
            l = l.layout(scene.size.width/scale, scene.size.height/scale)
            l.render(scene, scaled, scaled, 1, 1)
        finally: pass
        #vob.lob.PoolContext.exit()

        global s
        s = scene.size

        class T(java.lang.Thread):
            def run(self):
                time.sleep(1)
                global file
                file = string.replace(file, '.', '/')
                vob.putil.saveimage.save(file+'.png',
                                         w.readPixels(0, 0, s.width, s.height),
                                         s.width, s.height
                                         )
                sys.exit(0)
        T().start()
        return scene
