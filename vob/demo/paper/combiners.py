# 
# Copyright (c) 2003, Tuomas J. Lukka and Janne Kujala
# 
# This file is part of Gzz.
# 
# Gzz is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Gzz is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Gzz; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 

from __future__ import nested_scopes
from combinerutil import *

from vob.putil import saveanim
from vob.putil.demokeys import *
from vob.putil import misc


class Scene:
    """Show some basis textures and what the NV10 register combiners can do with them.

    The small squares show the randomly selected colors used in the real outputs.
    The text shows the combiner code currently used for the intersection.
    """
    def __init__(self):
	self.bgcolor = (0.7, 0.8, 0.6)
	self.pt = PaperTemplate()

	self.isectcomb = "BAND0"

	self.combinercodes = [e[0] for e in texcodesList if e[0] != "NONE"]
	self.key = KeyPresses(self,
ListIndex("ccind", "combinercodes", 0, "combiner code for intersection region", "<", ">", 
		    noAnimation = 1),
*self.pt.keys
	)
	# else:
	#     self.pt.key(k)
	    
    def scene(self, vs):
	vs.put( background(self.bgcolor))

	cs = vs.orthoCS(0, "S1", 0, 50, 50, 400, 400)
	self.pt.place(vs, cs, "RGB0", self.combinercodes[self.ccind], "RGB1")

        for i in range(0,3):
            cs = vs.coords.affine(0, 10, 700 + (i-1)*80, 80, 50, 0, 0, 50)
	    vs.matcher.add(cs, "col" + str(i))
            col = self.pt.cols[i].split()
            cq = coloredQuad((col[0], col[1], col[2]))
            vs.map.put(cq, cs)
            vs.map.put(self.pt.frame, cs)

	tcs = vs.orthoCS(0, "TXT", 0, 650, 300, 1, 1)
	vs.map.put(getDListNocoords("""
	    Disable REGISTER_COMBINERS_NV
	    Color 0 0 0 1
	    Disable ALPHA_TEST
	    Enable TEXTURE_2D
	"""))
	misc.putMultilineText (vs, tcs, texcodes[self.combinercodes[self.ccind]], 15)
