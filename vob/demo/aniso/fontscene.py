# 
# Copyright (c) 2004, Tuomas J. Lukka
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
from vob.putil.demokeys import *
from math import ceil

class Scene:
    def __init__(self):

	# self.anisos = [1, 1.5, 2.0, 3.0]
	# self.anisos = [1, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0]

	self.filters = [None, (-.25,-.125)]

	self.anisos = [1, 2]

	height = self.height = 128

	# string = "AIOSs"
	string = "SP"

	self.ftfont = vob.gl.GL.createFTFont(
		    "/usr/share/fonts/type1/gsfonts/n019003l.pfb",
		    height, height)

	self.fonts = [[[
	    vob.gl.SimpleAlphaFont.convertFont(
		self.ftfont, 30,
		1./self.height, 1./self.height,
		    [
			    "TEXTURE_MAG_FILTER", "LINEAR",
			    "TEXTURE_MIN_FILTER", "LINEAR_MIPMAP_LINEAR",
			    "TEXTURE_MAX_ANISOTROPY_EXT", str(ceil(self.anisos[i]))
		    ])
		    for i in range(0, len(self.anisos))]
			for k in range(0, len(self.filters))]
			for j in range(0,2)]

	self.superfont = \
	    vob.gl.SimpleAlphaFont.convertFont(
		self.ftfont, 30,
		1./self.height, 1./self.height,
		    [
			    "TEXTURE_MAG_FILTER", "LINEAR",
			    "TEXTURE_MIN_FILTER", "LINEAR_MIPMAP_LINEAR",
			    "TEXTURE_MAX_ANISOTROPY_EXT", "2"
		    ], 4)

	# 0: horizontal, 1: vertical

	for k in range(0, len(self.filters)):
	    for i in range(0, len(self.fonts[0][k])):
		if self.anisos[i] > 1:
		    vob.gl.SimpleAlphaFont.postprocessGLFont_reduceReso(
			self.fonts[0][k][i].getQuadFont(), self.anisos[i], 1)
		if self.filters[k]:
		    vob.gl.SimpleAlphaFont.filterDownsampled(
			self.fonts[0][k][i].getQuadFont(), self.filters[k][0], self.filters[k][1],
			[
			    "TEXTURE_MAG_FILTER", "LINEAR",
			    "TEXTURE_MIN_FILTER", "LINEAR_MIPMAP_LINEAR",
			    "TEXTURE_MAX_ANISOTROPY_EXT", str(ceil(self.anisos[i]))
		    ])
	    for i in range(0, len(self.fonts[1][k])):
		if self.anisos[i] > 1:
		    vob.gl.SimpleAlphaFont.postprocessGLFont_reduceReso(
			self.fonts[1][k][i].getQuadFont(), 1, self.anisos[i])
		if self.filters[k]:
		    vob.gl.SimpleAlphaFont.filterDownsampled(
			self.fonts[1][k][i].getQuadFont(), self.filters[k][0], self.filters[k][1],
			[
			    "TEXTURE_MAG_FILTER", "LINEAR",
			    "TEXTURE_MIN_FILTER", "LINEAR_MIPMAP_LINEAR",
			    "TEXTURE_MAX_ANISOTROPY_EXT", str(ceil(self.anisos[i]))
		    ])

	self.texts = [[[
	    vob.gl.GLRen.createText1(f.getQuadFont(),
		string, 0, 0)
	    for f in fonts2] for fonts2 in fonts] for fonts in self.fonts]

	self.supertext =  \
	    vob.gl.GLRen.createTextSuper4(self.superfont.getQuadFont(),
		string, 0, 0)

	self.levels = [[
	    [	[
		    vob.gl.SimpleAlphaFont.getLevelDrawPixels(f.getQuadFont(), ord(string[0]), level)
		    for level in range(0,9)]
		for f in fonts2]
		for fonts2 in fonts
	    ]
	    for fonts in self.fonts]

    	self.key = KeyPresses(
            self, 
	    ListIndex("aniso", "anisos", 0, "Anisotropy (index of list)", "A", "a"),
	    ListIndex("filter", "filters", 0, "Mipmap filter", "F", "f"),
	    Toggle("shift", 0, "Shift text", "m"),
	    Toggle("vert", 0, "Aniso vertically", "v"),
	    Toggle("super4", 0, "Supersample", "s"),
	    SlideLin("lodbias", 0, .1, "LOD bias", "b", "B"),
	    SlideLin("rotation", 0, 1, "Rotation", ">", "<"),
	    )
    def scene(self, vs):
	vs.put( background((1,1,1)))

	print "Aniso:",self.anisos[self.aniso],self.vert,self.filter

	if self.super4:
	    text = self.supertext
	else:
	    text = self.texts[self.vert][self.filter][self.aniso]

	shifted = vs.translateCS(0, "SH", 5 * self.shift, 3 * self.shift)
	shifted = vs.rotateCS(shifted, "R", self.rotation)

	lb = self.lodbias
	vs.put(getDListNocoords("""
	    Enable TEXTURE_2D
	    TexEnv TEXTURE_ENV TEXTURE_ENV_MODE REPLACE
	    TexEnv TEXTURE_FILTER_CONTROL_EXT TEXTURE_LOD_BIAS_EXT %(lb)s
	    Enable BLEND
	    Disable ALPHA_TEST
	""" % locals()))

	if 1:
	    ni = 5
	    y0 = 20
	    if self.super4:
		vs.put(getDListNocoords(
		    vob.lava.normalizedquads.super4TexSetupCode() +
		    vob.lava.normalizedquads.super4CombinerCode()))
	    for size in (8, 9.3, 10.4,12,16):
		for x in range(0,ni):
		    for y in range(0, ni):
			vs.put(text, 
			    vs.orthoCS(shifted, str((size,x,y,"A")), 0, 
				20 + (1*int(size*1.42)+1./ni) * x,
				y0 + 20 + (int(size*1.12)+1./ni) * y,
				size, size))
		y0 += int(ni * size * 1.2 )
	    if self.super4:
		vs.put(getDListNocoords(
		    vob.lava.normalizedquads.super4TeardownCode() + """
		    Disable REGISTER_COMBINERS_NV
		    """
		    ))
	    if not self.super4:
		for level in range(0,8):
		    vs.put(self.levels[self.vert][self.filter][self.aniso][level],
			vs.orthoCS(0, str(("I",level)), 0, 
				500, 150 + 70 * level, 1, 1))
	if 0:
	    y = 10
	    for j in range(1,40):
		i = .33 * j
		vs.put(text,
		    vs.orthoCS(shifted, str((i,"A")), 0, 20, y, i, i))
		vs.put(text,
		    vs.orthoCS(shifted, str((i,"B")), 0, 40.5, y+.5, i, i))
		vs.put(text,
		    vs.rotateCS(
			vs.orthoCS(shifted, str((i,"C")), 0, 60.5, y+.5, i, i),
			"R", -15))
		vs.put(text,
		    vs.rotateCS(
			vs.orthoCS(shifted, str((i,"D")), 0, 80.5, y+.5, i, i),
			"R", -30))

		y += 1.5*i

	    if 0:
		vs.put(text,
		    vs.rotateCS(
		    vs.orthoCS(shifted, "3", 0, 100, 60, 8, 8),
		    "4", -25
		    ))
