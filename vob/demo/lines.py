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


from __future__ import nested_scopes
import vob

class Scene:
    def __init__(self):
        self.x = 150
        self.y = 150
    
    def key(self, k):
	pass

    def mouse(self, m):
        self.x = m.getX()
        self.y = m.getY()
        vob.AbstractUpdateManager.chg()

    def scene(self, vs):
	vs.put( background((.9, .8, .6)))
        cs = vs.coords.ortho(0,0, 0,0, self.x, self.y)
        vs.put( vob.vobs.ContinuousLineVob( 12.0, [0,0,0, 0.2,0.8,0,   1,1,0]), cs)
        print 'scene done.'
     
