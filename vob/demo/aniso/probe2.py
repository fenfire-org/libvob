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
from vob.putil.demokeys import *
import jarray

print "LOAD PROBE2"

# NVIDIA's FSAA turns on using that environment variable
if java.lang.System.getProperty("__GL_FSAA_MODE") != None:
    fsaaCompatibility = 1
else:
    fsaaCompatibility = 0
print "FSAA compatibility: ", fsaaCompatibility

# Create the textures with a single lit pixel at (0,0)
# of a single mipmap level

nlevels = 7
maxsize = 2**(nlevels-1)

ebytes = jarray.zeros( 4 * maxsize*maxsize, 'b')

# The texture whose mipmapping we look at
tex = vob.gl.GL.createTexture()

# A scratch texture
tex2 = vob.gl.GL.createTexture()
tex2.setTexParameter("TEXTURE_2D", "TEXTURE_MAG_FILTER", "NEAREST")
tex2.setTexParameter("TEXTURE_2D", "TEXTURE_MIN_FILTER", "NEAREST")


#for i in range(0,300): ebytes[i] = -1
#for i in range(300,600): ebytes[i] = 127


for i in range(0, nlevels ):
    tex.texImage2D(i, "RGBA", 
	maxsize >> i, maxsize >> i, 0,
	"RGBA", "UNSIGNED_BYTE", ebytes)
	    

quadlist = vob.lava.normalizedquads.simpleQuadList(0,0,1,1)
quadlistId = quadlist.getDisplayListID()

lc = -1
uc = 2
triplequadlist = vob.lava.normalizedquads.simpleQuadList(lc,lc,uc,uc)
triplequadlistId = triplequadlist.getDisplayListID()

lc = -2
uc = 3
quintuplequadlist = vob.lava.normalizedquads.simpleQuadList(lc,lc,uc,uc)
quintuplequadlistId = quintuplequadlist.getDisplayListID()

if fsaaCompatibility:
    quadlist = triplequadlist
    quadlistId = triplequadlistId

xoffs = 0
list = []

sqx = 10
sqy = 100

list2 = ["""
    BindTexture TEXTURE_2D %s
""" % tex2.getTexId()]

zeroval = 0

for i in range(0, nlevels ):
    levelSize = maxsize >> i
    for x in range(0, maxsize >> i):
	for y in range(0, maxsize >> i):
	    xm = x + xoffs
	    ym = y
	    if fsaaCompatibility: 
		xm = 2 + xm * 4
		ym = 2 + ym * 4
	    yinv = levelSize - 1 - y
	    list.append("""
		TexSubImage2D TEXTURE_2D  %(i)s %(x)s %(yinv)s 1 1 RGBA 1 1 1 1
		PushMatrix
		Translate %(xm)s %(ym)s 0
		CallList %(quadlistId)s
		PopMatrix
		TexSubImage2D TEXTURE_2D  %(i)s %(x)s %(yinv)s 1 1 RGBA %(zeroval)s %(zeroval)s %(zeroval)s %(zeroval)s
	    """% locals())

    # y coordinate in the OpenGL coordinate system
    yingl = 768 - levelSize


    color = (1. / (levelSize * levelSize));
    if not fsaaCompatibility:
	xoffsflub = xoffs 
	list2.append("""
	    CopyTexImage2D TEXTURE_2D 0 RGBA8 %(xoffsflub)s %(yingl)s %(levelSize)s %(levelSize)s 0
	    """ % locals())
    else:
	# Get the texture properly sized
	list2.append("""
	    CopyTexImage2D TEXTURE_2D 0 RGBA8 0 0 %(levelSize)s %(levelSize)s 0
	    """ % locals())
	# XXX ??? !!! For some reason, the X coordinate is in 
	# ABSOLUTE SCREEN COORDINATES on NV 44.96 when using Gf4Go FSAA.
	# Y coordinate is normal.
	# Strange bug, anyone? 
	flub = -3
	for x in range(0, maxsize >> i):
	    for y in range(0, maxsize >> i):
		
		xm = flub + 2 + (x+xoffs)*4
		ym = 768 - ( 3 + (levelSize -1 - y)*4 )
		list2.append("""
		    CopyTexSubImage2D TEXTURE_2D 0 %(x)s %(y)s %(xm)s %(ym)s 1 1
		""" % locals())

    list2.append("""
	Color %(color)s %(color)s %(color)s 1
	CallList %(quintuplequadlistId)s
    """ % locals())

    xoffs += maxsize >> i
    xoffs += 2

print "".join(list2)

list = getDListNocoords("".join(list))
list2 = getDList("".join(list2))
# XXX SHould adddepend to dlist but not vital

# Program to adjust logarithmic color scale appropriately

colorProg = GL.createProgram("""!!ARBvp1.0

DP4 result.texcoord.x, state.matrix.texture.row[0], vertex.texcoord;
DP4 result.texcoord.y, state.matrix.texture.row[1], vertex.texcoord;
DP4 result.texcoord.z, state.matrix.texture.row[2], vertex.texcoord;
DP4 result.texcoord.w, state.matrix.texture.row[3], vertex.texcoord;

TEMP R0,R1;

MUL R0, vertex.color, vertex.texcoord[1].x;

SGE R1, R0, 1.1;
MAD R0.xyz, R1, -R0, R0;
ADD R0.x, R0, R1;

MOV result.color, R0;


DP4 result.position.x, state.matrix.mvp.row[0], vertex.position;
DP4 result.position.y, state.matrix.mvp.row[1], vertex.position;
DP4 result.position.z, state.matrix.mvp.row[2], vertex.position;
DP4 result.position.w, state.matrix.mvp.row[3], vertex.position;

END""")

colorprogid = colorProg.getProgId()

class Scene:
    def __init__(self):
	self.minfilters = [
	    "LINEAR_MIPMAP_LINEAR",
	    "LINEAR_MIPMAP_NEAREST",
	    "NEAREST_MIPMAP_LINEAR",
	    "NEAREST_MIPMAP_NEAREST",
	    "NEAREST",
	    ]
    	self.key = KeyPresses(
            self, 
	    SlideLin("faniso", 1, 1, "Filter Anisotropy", "a", "A"),
	    SlideLin("ctrx", .5, .01, "x coord", "Left", "Right"),
	    SlideLin("ctry", .5, .01, "y coord", "Up", "Down"),
	    SlideLin("rot", 0, 1, "angle", ">", "<"),
	    SlideLin("maxlevel", 3, 1, "Brightness at level", "t", "g"),
	    SlideLog("scale", .09, "isotropic scale", "k", "j"),
	    SlideLog("aniso", 1, "anisotropy", "l", "h"),
	    ListIndex("minfilter", "minfilters", 0, "Filter type", "F", "f"),
	    Toggle("super4", 0, "4xSS", "s"),
	)
    def scene(self, vs):
	self.vs = vs
	vs.put( background((.0,.2,.2)))

	#
	# Create the texture transformation matrix
	#

	texturemat = 0

	# Translated to the given texel
	texturemat = vs.translateCS(texturemat, "A", self.ctrx, self.ctry)

	# Scaled anisotropically
	texturemat = vs.scaleCS(texturemat, "C", self.scale * self.aniso, self.scale)

	# Rotated
	texturemat = vs.rotateCS(texturemat, "B", self.rot)

	# Set rotation and scale center to halfway of the texel
	texturemat = vs.translateCS(texturemat, "D", -.5, -.5)

	tex.setTexParameter("TEXTURE_2D", "TEXTURE_MAG_FILTER", "NEAREST")
	tex.setTexParameter("TEXTURE_2D", "TEXTURE_MIN_FILTER", self.minfilters[self.minfilter])
	tex.setTexParameter("TEXTURE_2D", "TEXTURE_MAX_ANISOTROPY_EXT", 
		    self.faniso)

	

	# invtexturemat = vs.invertCS(texturemat, "D")
	print "Min: ",self.minfilters[self.minfilter]

	vs.put(getDListNocoords(vob.putil.nvcode.parseCombiner("""
	    PushAttrib ENABLE_BIT TEXTURE_BIT
	    Enable TEXTURE_2D
	    Disable BLEND
	    Disable ALPHA_TEST
	    Color 1 1 1
	    TexEnv TEXTURE_ENV TEXTURE_ENV_MODE REPLACE

	    """)))
	if zeroval == 0:
	    if self.super4:
		vs.put(getDListNocoords(vob.putil.nvcode.parseCombiner("""

		    Enable REGISTER_COMBINERS_NV

		    SPARE0 = (TEX0 + TEX1) * 0.5

		    SPARE1 = (TEX2 + TEX3) * 0.5

		    color = SPARE0 + SPARE1
		    alpha = 1

		""")))
	    else:
		vs.put(getDListNocoords(vob.putil.nvcode.parseCombiner("""

		    Enable REGISTER_COMBINERS_NV

		    # SPARE0 = ( (+TEX0) ) * 2

		    SPARE0 = ( (+TEX0) )* 2

		    color = SPARE0
		    alpha = 1

		""")))
	else:
	    vs.put(getDListNocoords(vob.putil.nvcode.parseCombiner("""

		Enable REGISTER_COMBINERS_NV

		SPARE0 = ( (+TEX0) + (-.5) ) * 4

		SPARE0 =  (+SPARE0) + (.5) 

		color = SPARE0
		alpha = 1

	    """)))
	vs.put(GLRen.createTransMatrix("TEXTURE"),
		texturemat)

	if self.super4:
	    vs.put(getDListNocoords(
		vob.lava.normalizedquads.super4SetupCode(tex)))
	    vs.put(list)
	    vs.put(getDListNocoords(
		vob.lava.normalizedquads.super4TeardownCode()))
	else:
	    vs.put(getDListNocoords("""
		BindTexture TEXTURE_2D %s
	    """ % tex.getTexId()))
	    vs.put(list)
	    vs.put(getDListNocoords("""
		BindTexture TEXTURE_2D 0
	    """))

	vs.put(getDListNocoords("""
	    PopAttrib
	    """))

	if 0:

	    footprintFrame = vs.orthoCS(0, "F", 0, 500, 100, 256, 256)
	    footprintCS = vs.coords.concat(footprintFrame, texturemat)
	    vs.matcher.add(footprintCS, "FP")
	    footprintCS = vs.translateCS(footprintCS, "TRA", 0, 0, -10)

	    vs.put(getDListNocoords("""
		PopAttrib
		Disable TEXTURE_2D
		Disable BLEND
		PolygonMode FRONT_AND_BACK LINE
	    """))

	    vs.put(coloredQuad((0,0,0)), footprintFrame)
	    vs.put(coloredQuad((1,0,0)), footprintCS)

	    vs.put(getDListNocoords("""
		PolygonMode FRONT_AND_BACK FILL
	    """))

	pixzoomcs = vs.orthoCS(0, "PFP", 0, 700, 450, 100, 100)

	global bright
	bright = 1 << ((nlevels-1 - self.maxlevel)*2)
	print self.maxlevel, bright

	vs.put(getDList("""
	    Enable TEXTURE_2D
	    Color 0 0 0

	    CallList %(quintuplequadlistId)s

	    LineWidth 5

	    Color 1 1 1

	    Disable REGISTER_COMBINERS_NV
	    Enable BLEND
	    BlendFunc 1 1

	    Enable VERTEX_PROGRAM_ARB
	    BindProgramARB VERTEX_PROGRAM_ARB %(colorprogid)s

	    MultiTexCoord TEXTURE1 %(bright)s 0 0 0

	""" % globals()), pixzoomcs)




	# Then, render the texels in coordinate space...
	vs.put(GLRen.createTransMatrix("TEXTURE"),
		texturemat)

	vs.put(list2, pixzoomcs)

	vs.put(getDListNocoords("""
	    Disable TEXTURE_2D
	    Disable VERTEX_PROGRAM_ARB
	    Disable BLEND
	    PolygonMode FRONT_AND_BACK LINE
	"""))
	vs.put(coloredQuad((.2,0,0)), pixzoomcs)
	vs.put(getDListNocoords("""
	    PolygonMode FRONT_AND_BACK FILL
	"""))

	# print vs.coords.transformPoints3(footprintCS, [0,0,0, 0,1,0, 1,0,0, 1,1,0], None)
	

