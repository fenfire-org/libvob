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
from vob.putil.misc import *

class Scene:
    def __init__(self):
	aniso = [ 1, 1.1, 1.5, 2.0, 3.0,5.0]
	ftfonts = [
	    vob.gl.GL.createFTFont(
		"/usr/share/fonts/type1/gsfonts/n019003l.pfb",
		int(64 * a), 64)
		for a in aniso]
	self.fonts = [
	    vob.gl.SimpleAlphaFont.convertFont(
		ftfonts[i],
		20,
		1/64./aniso[i], 1/64.,
		[
			"TEXTURE_MAG_FILTER", "LINEAR",
			"TEXTURE_MIN_FILTER", "LINEAR_MIPMAP_LINEAR",
			"TEXTURE_MAX_ANISOTROPY_EXT", "20"
		])
		for i in range(0, len(aniso))]
	self.text = [
	    vob.gl.GLRen.createText1(f.getQuadFont(),
		"ABCDEabcde()", 0, 0)
	    for f in self.fonts]
	self.size = 10
    def scene(self, vs):
	self.vs = vs
	vs.put( background((.0,.7,.6)))

	vs.put(getDListNocoords("""
	    Enable TEXTURE_2D
	    Enable BLEND
	"""))


	for i in range(0, len(self.fonts)):
	    vs.put(self.text[i], 
		vs.orthoCS(0, str(i), 
		    0, 50, 3 * self.size + 2*self.size*i, self.size, self.size))
		
