# 
# Copyright (c) 2003, Tuomas J. Lukka
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



import vob
from vob.paper.texcache import getCachedTexture
from vob.putil.misc import *

bg = getCachedTexture([512,512,0,1,"LUMINANCE", "LUMINANCE", "noise",
	[
	"freq", "20",
	"type", "fBm",
	"bias", ".5",
	"scale", ".5",
	"fbmgain", ".9",
	"fbmoct", "8",
	"fbmlacu", "1.892",
	]])

bg.setTexParameter("TEXTURE_2D", "TEXTURE_MAX_ANISOTROPY_EXT", "6")

class Scene:
    def scene(self, vs):
	vs.put(background((.5,1,1)))
	vs.put(GLCache.getCallList("""
	    PushAttrib CURRENT_BIT ENABLE_BIT
	    Enable TEXTURE_2D
	    Enable COLOR_SUM_EXT
	    Color .3 .8 1
	    SecondaryColorEXT 0 .5 .6

	    MatrixMode TEXTURE
	    PushMatrix
	    Rotate 45 0 0 1 
	    Scale 5 15 1 
	    Rotate 45 0 0 1 
	    MatrixMode MODELVIEW

	"""))
	vs.put(quad(bg.getTexId()), 
	    vs.orthoCS(0, "A", 0, 0, 0, 1024, 1024))
	vs.put(GLCache.getCallList("""
	    MatrixMode TEXTURE
	    PopAttrib
	    PopMatrix
	    MatrixMode MODELVIEW
	"""))
    def key(self, k):
	pass
