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


# Texture operators for NV2X chips, i.e. chips that
# support GL_NV_texture_shader and GL_NV_texture_shader2.


# Syntax: one tuple per texture unit,
# ( operation, texture type[, previous texture input] )
# where
# operation
#	T1/2/3: TEXTURE_1D / 2D / 3D
#	D: DOT_PRODUCT 
#	D2: DOT_PRODUCT_2D
#	O: OFFSET_TEXTURE_2D
# texture type
#	DOT2: 2D dot-product -suitable
#	RGB2: 2D RGB
#       DSDT: 2D DSDT offset texture

shaderTypes = [
    [
	("T2", "DOT2"),
	("D", None, 0),
	("D2", "RGB2", 0),
	("T2", "RGB2")
    ],
    [
	("T2", "DOT2"),
	("T2", "DOT2"),
	("D", None, 0),
	("D2", "RGB2", 1),
    ],
    [
	("T3", "R3"),
	("T3", "R3"),
	("T3", "R3"),
	("T3", "R3"),
    ],
    [
	("T2", "RGB2"),
	("T2", "RGB2"),
	("T2", "RGB2"),
	("T2", "RGB2"),
    ],
    [
        ("T2", "DSDT"),
        ("O", "RGB2"),
        ("T2", "DSDT"),
        ("O", "RGB2")
    ],
    [
        ("T2", "DSDT_HILO"),
        ("OH", "RGB2", 0),
        ("T2", "DSDT_HILO"),
        ("OH", "RGB2", 2),
    ]
]

# XXX: kluge: odd and even units are for "-eps" and "+eps"
embossShaderTypes = [
    [
	("T2", "RGB2"),
	("T2", "RGB2"),
    ]
]

shaderOps = {
    "T2" : "TEXTURE_2D",
    "T3" : "TEXTURE_3D",
    "D" : "DOT_PRODUCT_NV",
    "D2" : "DOT_PRODUCT_TEXTURE_2D_NV",
    "O" : "OFFSET_TEXTURE_2D_NV",
    "OH" : "OFFSET_HILO_TEXTURE_2D_NV",
}

shaderTargets = {
    "T2" : "TEXTURE_2D",
    "T3" : "TEXTURE_3D",
    "D": None,
    "D2": "TEXTURE_2D",
    "O": "TEXTURE_2D",
    "OH": "TEXTURE_2D",
}

shaderTexgenTypes = {
    "T2" : "TexGen2D",
    "T3" : "TexGen2D3",
    "D" : "TexGenDotVector",
    "D2" : "TexGenDotVector",
    "O" : "TexGen2D",
    "OH" : "TexGen2D",
}


class NoTextureSetForStage:
    pass

class ShaderPass:
    def __init__(self, shaderType):
	self.st = shaderType
	self.tex = [None for texunit in shaderType]
    def getTextureTypes(self):
	"Get the texture type names (D2, R2 ...) for the texture units."
	return [texunit[1] for texunit in self.st]
    def getTexgenTypes(self):
	"Get the texgen letter codes for the texture units."
	return [shaderTexgenTypes[texunit[0]] for texunit in self.st]
    def setTexture(self, ind, tex):
	self.tex[ind] = tex
    def setupCode(self, rnd):
	c = """
	    Enable TEXTURE_SHADER_NV
	"""
	for t in range(0,len(self.st)):
	    shortOp = self.st[t][0]
	    op = shaderOps[shortOp]
	    target = shaderTargets[shortOp]
	    c += """
	    ActiveTexture TEXTURE%(t)s

		TexEnv TEXTURE_SHADER_NV SHADER_OPERATION_NV %(op)s

	    """ % locals()

	    if target != None:
                texid = self.tex[t].getTexId()
		if texid == None: raise NoTextureSetForStage()
		c += """
		    Enable %(target)s
		    BindTexture %(target)s %(texid)s
		""" % locals()
	    if len(self.st[t]) > 2: # Previous texture input
		c += """
		    TexEnv TEXTURE_SHADER_NV PREVIOUS_TEXTURE_INPUT_NV TEXTURE%s
		""" % (self.st[t][2])

            if shortOp in [ "O", "OH" ]:
                hyper = rnd.nextGaussian() * self.tex[t].featurescale * 2
                mat = ( rnd.nextGaussian()*hyper,
                        rnd.nextGaussian()*hyper,
                        rnd.nextGaussian()*hyper,
                        rnd.nextGaussian()*hyper )
                c += """
                    TexEnv TEXTURE_SHADER_NV OFFSET_TEXTURE_MATRIX_NV %s %s %s %s
                """ % mat
            
	return c
    def getRGBoutputs(self):
	"""A list of the texture units whose outputs are useful in
	the register combiners.
	"""
	return [i for i in range(0,len(self.st))
		if self.st[i][1] in ("RGB2", "RGB3", "FOOBAR")]

    def getRGBoutputscales(self):
        return [self.tex[i].scale for i in self.getRGBoutputs()]

def makeNormalShaderPass():
    return ShaderPass(shaderTypes[5])

scaleFactor = 1.5
