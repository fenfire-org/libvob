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
import vob
from vob.putil.misc import *

class Scene:
    """All basis textures used in libpaper.
    """
    def __init__(self):
	self.tex = vob.paper.textures.init(3, "RGB")["RGB2"]
    def key(self, k):
        pass
    def scene(self, vs):
	vs.put(background((.7,.8,.7)))
	print self.tex
	(x,y) = (0,0)
	sp = 10
	size = vs.getSize().height / 4 - 2*sp
	(w,h) = (size,size)
        total = 0
	for t in self.tex:
           vs.put( getDListNocoords("Color 1 1 1")) 
	   vs.put(quad((t.getTexId())),  0, sp + x*(w+sp), sp + y*(h+sp), w, h)
	   x += 1
	   if x>3:
		x = 0
		y += 1
           pw = GL.getGLTexLevelParameterFloat("TEXTURE_2D", t.getTexId(), 0, 
                                               "TEXTURE_WIDTH")[0]
           ph = GL.getGLTexLevelParameterFloat("TEXTURE_2D", t.getTexId(), 0, 
                                               "TEXTURE_HEIGHT")[0]
           print "Size: %sx%s = %s" % (pw, ph, pw * ph)
           total += pw * ph
        print "Total size =", total
