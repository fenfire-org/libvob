# 
# Copyright (c) 2003, Asko Soukka
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
# Public License along with Fenfire; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 

"""
Demo to generated repeatable background textures.
"""

import vob
import vob.paper.papermill
import vob.paper.textures

from vob.paper.papermill import ThePaperMill
from vob.putil import *
from vob.demo.paper.util import *

class Scene:
    """
    Scene to generated repeatable background textures.
    """
    def __init__(self):
	self.bgcolor = (0.0, 0.0, 0.0)
	self.seed = rng.nextInt(2000000000)
	self.key = KeyPresses(self, 
             Action("New random paper", "n", self.randomize),
             Action("Save screenshot", "s", self.savepaper),
             *keys)

    def savepaper(self, *args):
        filename = str(self.seed)+'.png'
        saveanim.saveframe(filename, vob.putil.demowindow.w, x=0, y=0, w=384, h=384)

    def randomize(self, *args):
        self.seed = rng.nextInt(2000000000)

    def scene(self, vs):
        size = vs.getSize()
	vs.put( background(self.bgcolor))

        self.pq = self.getPaper(self.seed)

	cs = vs.coords.affine(0, 1, 0, 0, size.height, 0, 0, size.height)
        vs.matcher.add(cs, "tex")

        vs.map.put(self.pq, cs, 0)

    def getPaper(self, seed):
        self.pap = ThePaperMill().getPaper(seed, passmask=passmask, vecs=[[.5,0],[0, .5]])
        return GLRen.createPaperQuad(self.pap, -0.5, -0.5, 0.5, 0.5, 0)
