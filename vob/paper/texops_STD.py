# 
# Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
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
# 


# Texture operators for unextended OpenGL.
# Basically, we choose between TEXTURE_2D and
# TEXTURE_3D for each unit.

shaderTypes = [
    [
	("TEXTURE_2D", "RGB2", "TexGen2D"),
	("TEXTURE_2D", "RGB2", "TexGen2D"),
    ],
];

class ShaderPass:
    def __init__(self, shaderType):
	self.st = shaderType
	self.tex = [None for texunit in shaderType]
    def getTextureTypes(self):
	return [texunit[1] for texunit in self.st]
    def getTexgenTypes(self):
	return [texunit[2] for texunit in self.st]
    def setTexture(self, ind, tex):
	self.tex[ind] = tex
    def getRGBoutputs(self):
	return [i for i in range(0,len(self.st))]
    def getRGBoutputscales(self):
        return [self.tex[i].scale for i in range(0,len(self.st))]
    def setupCode(self, rnd):
	c = ""
	for t in range(0,len(self.st)):
	    target = self.st[t][0]
	    texid = self.tex[t].getTexId()
	    if texid == None: raise NoTextureSetForStage()
	    c += """
	    ActiveTexture TEXTURE%(t)s

		Disable TEXTURE_3D
		Disable TEXTURE_2D
		Disable TEXTURE_1D

		Enable %(target)s
		BindTexture %(target)s %(texid)s
	    """ % locals()

	return c

def makeNormalShaderPass():
    return ShaderPass(shaderTypes[0])

scaleFactor = 1.0
