# 
# Copyright (c) 2003, Tuomas J. Lukka and Janne Kujala
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
import math

import vob
from org.nongnu.libvob.gl import GL, GLRen
from vob.putil import cg
from vob.putil.misc import *
from vob.putil.demokeys import *
from vob.paper.texcache import getCachedTexture
from vob.fillet import light3d


class Scene:
    "Multifilleting"
    def __init__(self):
    	self.key = KeyPresses(
            self, 
	    SlideLin("x", 200, 20, "x", "Left", "Right"),
	    SlideLin("y", 300, 20, "y", "Up", "Down"),
            *light3d.commonkeys
	)
    def scene(self, vs):
	vs.put( background((.5,1,.2)))

	a = vs.orthoBoxCS(0, "A", 0, 100, 100, 1, 1, self.size, self.size);
	b = vs.orthoBoxCS(0, "B", 200, self.x, self.y, 1, 1, self.size, self.size);
	c = vs.orthoBoxCS(0, "C", 400, 200, 500, 1, 1, self.size, self.size);
	d = vs.orthoBoxCS(0, "D", 600, 100, 300, 1, 1, self.size, self.size);
	e = vs.orthoBoxCS(0, "E", 800, 500, 500, 1, 1, self.size, self.size);

	def pc(conns, cs):
	    # vs.put(conns, [a,  b, c, d])
	    # vs.put(conns, [b,  a, d, e]);
            # vs.put(conns, [c,  a]);
	    # vs.put(conns, [d,  a, b]);

	    vs.put(conns, cs + [b,  e]);

            vs.put(getDListNocoords("Color .7 .5 .3"))

	    vs.put(conns, cs + [c,  e]);

#	    vs.put(GLRen.createDebugSwitch("Fillets", 1));
#	    vs.put(GLRen.createDebugSwitch("VFillets", 1));
	    #vs.put(GLRen.createDebugSwitch("Quadrics", 1));

            vs.put(getDListNocoords("Color 1 0 0"))

	    vs.put(conns, cs + [e, b, c]);

#	    vs.put(GLRen.createDebugSwitch("Fillets", 0));
#	    vs.put(GLRen.createDebugSwitch("VFillets", 0));
	    #vs.put(GLRen.createDebugSwitch("Quadrics", 0));



        light3d.drawFillets(self, vs, pc)
