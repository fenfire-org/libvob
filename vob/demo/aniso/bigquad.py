# (c) Tuomas J. Lukka

import vob
from vob.putil.misc import *
from vob.putil.demokeys import *
from math import ceil

GL = vob.gl.GL
GLRen = vob.gl.GLRen

def getFont(ftfont,height,aniso):
	font = vob.gl.SimpleAlphaFont.convertFont(
		ftfont, 10, 
		1./height, 1./height, [
			    "TEXTURE_MAG_FILTER", "LINEAR",
			    "TEXTURE_MIN_FILTER", "LINEAR_MIPMAP_LINEAR",
			    "TEXTURE_MAX_ANISOTROPY_EXT", str(ceil(aniso))
		    ])
	if aniso > 1:
	    vob.gl.SimpleAlphaFont.postprocessGLFont_reduceReso(
		font.getQuadFont(), aniso, 1)

	vob.gl.SimpleAlphaFont.changeFormat(font.getQuadFont(), "LUMINANCE",
	[
			    "TEXTURE_MAG_FILTER", "LINEAR",
			    "TEXTURE_MIN_FILTER", "LINEAR_MIPMAP_LINEAR",
	 		    "TEXTURE_MAX_ANISOTROPY_EXT", str(ceil(aniso))
	])
	return font

class Quad:
    def place(self, vs, cs, tcs):
	vs.put(GLRen.createTransMatrix("TEXTURE"), tcs)
	vs.put(self.quadCall, cs)
    

class AnisoQuad(Quad):
    def __init__(self, aniso, ftfont, height):
	self.font = getFont(ftfont, height, aniso)

	self.aniso = aniso

	self.quadList = vob.lava.normalizedquads.simpleQuadList(0, 0, 1, 1, 
		self.font.getQuadFont().getTextures()[4])
	self.quadCall = GLRen.createCallListCoorded(self.quadList)

class LodBiasQuad(Quad):
    def __init__(self, ftfont, height):
	self.font = getFont(ftfont, height, 1)

	tex = self.font.getQuadFont().getTextures()[4]
	tex.setTexParameter("TEXTURE_2D", "TEXTURE_LOD_BIAS", "-1")
	self.quadList = vob.lava.normalizedquads.simpleQuadList(0, 0, 1, 1, tex)
	self.quadCall = GLRen.createCallListCoorded(self.quadList)

	

class Super4Quad(Quad):
    def __init__(self, ftfont, height):
	self.font = getFont(ftfont, height, 1)
	self.quadList = vob.lava.normalizedquads.super4QuadList(0, 0, 1, 1, 
		self.font.getQuadFont().getTextures()[4])
	self.quadCall = GLRen.createCallListCoorded(self.quadList)

class Super4ARBQuad(Quad):
    def __init__(self, ftfont, height):
	self.font = getFont(ftfont, height, 1)
	self.quadList = vob.lava.normalizedquads.super4QuadList(0, 0, 1, 1, 
		self.font.getQuadFont().getTextures()[4],
		combinerCode=
		    vob.lava.normalizedquads.super4ARBcombinerCode())
	self.quadCall = GLRen.createCallListCoorded(self.quadList)

class Super4NVQuad(Quad):
    def __init__(self, ftfont, height):
	self.font = getFont(ftfont, height, 1)
	self.quadList = vob.lava.normalizedquads.super4QuadList(0, 0, 1, 1, 
		self.font.getQuadFont().getTextures()[4],
		combinerCode=
		    vob.lava.normalizedquads.super4NVFPcombinerCode())
	self.quadCall = GLRen.createCallListCoorded(self.quadList)

class Super4NVFPOnlyQuad(Quad):
    def __init__(self, index, ftfont, height):
	self.font = getFont(ftfont, height, 1)
	self.quadList = vob.lava.normalizedquads.super4OnlyNVFPQuadList(index, 0, 0, 1, 1, 
		self.font.getQuadFont().getTextures()[4])
	self.quadCall = GLRen.createCallListCoorded(self.quadList)

class Super4NVFPOnlyQuad2(Quad):
    def __init__(self, ftfont, height):
	self.font = getFont(ftfont, height, 1)
	self.quadList = vob.lava.normalizedquads.super4OnlyNVFPQuadList2(0, 0, 1, 1, 
		self.font.getQuadFont().getTextures()[4])
	self.quadCall = GLRen.createCallListCoorded(self.quadList)

class Scene:
    def __init__(self):
	
	self.height = 128
	self.ftfont = vob.gl.GL.createFTFont(
		    "/usr/share/fonts/type1/gsfonts/n019003l.pfb",
		    self.height, self.height)
    


	self.quads = [
	    AnisoQuad(1, self.ftfont, self.height),           # 0
	    AnisoQuad(2, self.ftfont, self.height),           # 1
	    Super4Quad(self.ftfont, self.height), 		# 2
	    Super4ARBQuad(self.ftfont, self.height),		# 3
	    Super4NVQuad(self.ftfont, self.height),		# 4
	    Super4NVFPOnlyQuad(0,self.ftfont, self.height),	# 5
	    LodBiasQuad(self.ftfont, self.height),		# 6
	    Super4NVFPOnlyQuad2(self.ftfont, self.height),	# 7
	    Super4NVFPOnlyQuad(1,self.ftfont, self.height),	# 8
	    Super4NVFPOnlyQuad(2,self.ftfont, self.height),	# 9
	    Super4NVFPOnlyQuad(3,self.ftfont, self.height),	# 10
	]

    	self.key = KeyPresses(
            self, 
	    ListIndex("quad", "quads", 0, "Quad to render", "T", "t"),
	    Toggle("shift", 0, "Shift text", "s"),
	)

	self.nquads = 1
	self.quadsize = 512

    def scene(self, vs):
	vs.put( background((.0,.9,.9)))
	print self.quads[self.quad]


	tcs = vs.scaleCS(0, "B", 10 * self.quadsize / 512., 10 * self.quadsize / 512.)
	tcs = vs.rotateCS(tcs, "C", 28)
	tcs = vs.translateCS(tcs, "D", .1*self.shift, .07*self.shift)

	vs.put(getDListNocoords("""
	    TexEnv TEXTURE_ENV TEXTURE_ENV_MODE REPLACE
	    Color 1 0 1
	    Enable ALPHA_TEST
	    AlphaFunc EQUAL 1
	    Enable BLEND
	    BlendFunc SRC_ALPHA ONE_MINUS_SRC_ALPHA
	    Enable TEXTURE_2D
	"""))

	for i in range(0, self.nquads):
	    ncs = vs.orthoCS(0, str(i), -i, i, i, self.quadsize, self.quadsize)
	    self.quads[self.quad].place(vs, ncs, tcs)


