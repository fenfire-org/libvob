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
	    

quadlist = vob.gl.GL.createDisplayList("""
    Begin QUADS
    TexCoord 0 0
    Vertex 0 0
    TexCoord 0 1
    Vertex 0 1
    TexCoord 1 1
    Vertex 1 1
    TexCoord 1 0
    Vertex 1 0
    End
""")
quadlistId = quadlist.getDisplayListID()

lc = -2
uc = 3
triplequadlist = vob.gl.GL.createDisplayList("""
    Begin QUADS
    TexCoord %(lc)s %(lc)s
    Vertex %(lc)s %(lc)s
    TexCoord %(lc)s %(uc)s
    Vertex %(lc)s %(uc)s
    TexCoord %(uc)s %(uc)s
    Vertex %(uc)s %(uc)s
    TexCoord %(uc)s %(lc)s
    Vertex %(uc)s %(lc)s
    End
""" % locals())
triplequadlistId = triplequadlist.getDisplayListID()

if 1:
    # Use the simple distorted surrounding-square mode
    def getSqSize(level):
	"""Get the size of the square to use in the unified visualization.

	level -- mipmap level
	"""
	if level == 0: return 2
	else: return getSqSize(level-1) * 2 + 2

    def getSqCoord(level, x):
	"""Get the relative coordinate of the square in the unified visualization.

	level -- mipmap level
	x -- the coordinate in the texture
	"""
	# print "GetSqCoord",level,x
	if level >= nlevels: return 0
	ret = 1 + (x & 1) * getSqSize(level) + getSqCoord(level+1, x>>1)
	# print "Ret: ",ret
	return ret

    squareLists = []
    for level in range(0, nlevels ):
	sqSize = getSqSize(level)
	sql = []
	for i in range(0, sqSize):
	    sqM = sqSize - 1
	    sql.append("""
	    PushMatrix
		Translate %(i)s 0 0
		CallList %(quadlistId)s
		Translate 0 %(sqM)s 0
		CallList %(quadlistId)s
	    PopMatrix
	    PushMatrix
		Translate 0 %(i)s 0
		CallList %(quadlistId)s
		Translate %(sqM)s 0 0
		CallList %(quadlistId)s
	    PopMatrix
	    """ % locals())
	squareLists.append(GL.createDisplayList("".join(sql)))
else:
    pass

xoffs = 0
list = ["""
    BindTexture TEXTURE_2D %s
""" % tex.getTexId()]

sqx = 10
sqy = 100

list2 = ["""
    BindTexture TEXTURE_2D %s
""" % tex2.getTexId()]

zeroval = 0

for i in range(0, nlevels ):
    listId = squareLists[i].getDisplayListID()
    levelSize = maxsize >> i
    for x in range(0, maxsize >> i):
	xsq = sqx + getSqCoord(i, x)
	for y in range(0, maxsize >> i):
	    ysq = sqy + getSqCoord(i, y)
	    xm = x + xoffs
	    ym = y
	    yinv = levelSize - 1 - y
	    list.append("""
		TexSubImage2D TEXTURE_2D  %(i)s %(x)s %(yinv)s 1 1 RGBA 1 1 1 1
		PushMatrix
		Translate %(xm)s %(ym)s 0
		CallList %(quadlistId)s
		PopMatrix
	    """ % locals())

	    list.append("""
		PushMatrix
		Translate %(xsq)s %(ysq)s 0
		CallList %(listId)s
		PopMatrix
	    """ % locals())

	    list.append("""
		TexSubImage2D TEXTURE_2D  %(i)s %(x)s %(yinv)s 1 1 RGBA %(zeroval)s %(zeroval)s %(zeroval)s %(zeroval)s
	    """% locals())

    # y coordinate in the OpenGL coordinate system
    yingl = 768 - levelSize
    list2.append("""
	CopyTexImage2D TEXTURE_2D 0 RGBA8 %(xoffs)s %(yingl)s %(levelSize)s %(levelSize)s 0
	CallList %(triplequadlistId)s
    """ % locals())

    xoffs += maxsize >> i
    xoffs += 2

print "".join(list2)

list = getDListNocoords("".join(list))
list2 = getDList("".join(list2))
# XXX SHould adddepend to dlist but not vital

supervp = GL.createProgram("""!!ARBvp1.0 OPTION ARB_position_invariant;
ATTRIB tex0 = vertex.texcoord;
ATTRIB col = vertex.color;
PARAM mat[4] = { state.matrix.modelview };
PARAM texmat[4] = { state.matrix.texture[0] };

TEMP t;

PARAM foo = {1, .5, .25, 0};

MOV t, tex0;
SUB t, tex0, foo.zzww;
DP4 result.texcoord[0].x, texmat[0], t;
DP4 result.texcoord[0].y, texmat[1], t;
DP4 result.texcoord[0].z, texmat[2], t;
DP4 result.texcoord[0].w, texmat[3], t;

ADD t, t, foo.ywww;
DP4 result.texcoord[1].x, texmat[0], t;
DP4 result.texcoord[1].y, texmat[1], t;
DP4 result.texcoord[1].z, texmat[2], t;
DP4 result.texcoord[1].w, texmat[3], t;

ADD t, t, foo.wyww;
DP4 result.texcoord[2].x, texmat[0], t;
DP4 result.texcoord[2].y, texmat[1], t;
DP4 result.texcoord[2].z, texmat[2], t;
DP4 result.texcoord[2].w, texmat[3], t;

SUB t, t, foo.ywww;
DP4 result.texcoord[3].x, texmat[0], t;
DP4 result.texcoord[3].y, texmat[1], t;
DP4 result.texcoord[3].z, texmat[2], t;
DP4 result.texcoord[3].w, texmat[3], t;

MOV result.color, col;

END
""")

class Scene:
    def __init__(self):
	self.minfilters = [
	    "LINEAR_MIPMAP_LINEAR",
	    "LINEAR_MIPMAP_NEAREST",
	    "NEAREST_MIPMAP_LINEAR",
	    "NEAREST_MIPMAP_NEAREST",
	    ]
    	self.key = KeyPresses(
            self, 
	    SlideLin("ctrx", .5, .01, "x coord", "Left", "Right"),
	    SlideLin("ctry", .5, .01, "y coord", "Up", "Down"),
	    SlideLin("rot", 0, 1, "angle", ">", "<"),
	    SlideLog("sx", .09, "x scale", "j", "k"),
	    SlideLog("sy", .09, "y scale", "h", "l"),
	    ListIndex("minfilter", "minfilters", 0, "Filter type", "F", "f"),
	)
    def scene(self, vs):
	self.vs = vs
	vs.put( background((.0,.2,.2)))

	texturetrans = vs.translateCS(0, "A", self.ctrx, self.ctry)
	texturerot = vs.rotateCS(texturetrans, "B", self.rot)
	texturemat = vs.scaleCS(texturerot, "C", self.sx, self.sy)

	tex.setTexParameter("TEXTURE_2D", "TEXTURE_MAG_FILTER", "LINEAR")
	tex.setTexParameter("TEXTURE_2D", "TEXTURE_MIN_FILTER", self.minfilters[self.minfilter])
	tex.setTexParameter("TEXTURE_2D", "TEXTURE_MAX_ANISOTROPY_EXT", "20")

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

        if 0:
            # 2x2 super-sampling:
            # Doesn't work with NV25_EMULATE (it looks as if
            # there was some additional lod bias),
            # haven't tried with real hardware yet..
            progid = supervp.getProgId()
            texid = tex.getTexId()
            vs.put(getDListNocoords(vob.putil.nvcode.parseCombiner("""
            BindProgram VERTEX_PROGRAM_ARB %(progid)s
            Enable VERTEX_PROGRAM_ARB

            ActiveTexture TEXTURE3_ARB
            TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS -1
            BindTexture TEXTURE_2D %(texid)s
            Enable TEXTURE_2D

            ActiveTexture TEXTURE2_ARB
            TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS -1
            BindTexture TEXTURE_2D %(texid)s
            Enable TEXTURE_2D

            ActiveTexture TEXTURE1_ARB
            TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS -1
            BindTexture TEXTURE_2D %(texid)s
            Enable TEXTURE_2D

            ActiveTexture TEXTURE0_ARB
            TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS -1
            BindTexture TEXTURE_2D %(texid)s
            Enable TEXTURE_2D
            """ % locals())))
            if zeroval == 0:
                vs.put(getDListNocoords(vob.putil.nvcode.parseCombiner("""
		# Strengthen by multiplying by 4

		Enable REGISTER_COMBINERS_NV

                SPARE0 = (TEX0 + TEX1) * 0.5

                SPARE1 = (TEX2 + TEX3) * 0.5

                color = SPARE0 + SPARE1
                alpha = 1
                """)))
            else:
                vs.put(getDListNocoords(vob.putil.nvcode.parseCombiner("""
		# Strengthen by multiplying by 4

		Enable REGISTER_COMBINERS_NV

                SPARE0 = TEX0 + TEX1 - 0.5

                SPARE1 = TEX2 + TEX3 - 0.5

                color = SPARE0 + SPARE1
                alpha = 1
                """)))        
        else:
        
            if zeroval == 0:
                vs.put(getDListNocoords(vob.putil.nvcode.parseCombiner("""
		# Strengthen by multiplying by 4

		Enable REGISTER_COMBINERS_NV

		SPARE0 = ( (+TEX0) ) * 2

		color = SPARE0
		alpha = 1
                """)))
            else:
                vs.put(getDListNocoords(vob.putil.nvcode.parseCombiner("""
		# Strengthen by multiplying by 4

		Enable REGISTER_COMBINERS_NV

		SPARE0 = ( (+TEX0) + (-.5) ) * 4

		SPARE0 =  (+SPARE0) + (.5) 

		color = SPARE0
		alpha = 1
                """)))
            
	vs.put(GLRen.createTransMatrix("TEXTURE"),
		texturemat)
	vs.put(list)

	footprintFrame = vs.orthoCS(0, "F", 0, 500, 100, 256, 256)
	footprintCS = vs.coords.concat(footprintFrame, texturemat)
	vs.matcher.add(footprintCS, "FP")
	footprintCS = vs.translateCS(footprintCS, "TRA", 0, 0, -10)

	vs.put(getDListNocoords("""
	    PopAttrib

            ActiveTexture TEXTURE3_ARB
            TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS 0
            Disable TEXTURE_2D
            ActiveTexture TEXTURE2_ARB
            TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS 0
            Disable TEXTURE_2D
            ActiveTexture TEXTURE1_ARB
            TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS 0
            Disable TEXTURE_2D
            ActiveTexture TEXTURE0_ARB
            TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS 0
            Disable TEXTURE_2D

	    Disable TEXTURE_2D
	    Disable BLEND
	    PolygonMode FRONT_AND_BACK LINE
	"""))

	vs.put(coloredQuad((0,0,0)), footprintFrame)
	vs.put(coloredQuad((1,0,0)), footprintCS)

	vs.put(getDListNocoords("""
	    PolygonMode FRONT_AND_BACK FILL
	"""))

	pixzoomcs = vs.orthoCS(0, "PFP", 0, 100, 550, 50, 50)

	vs.put(getDList("""
	    Enable TEXTURE_2D
	    Color 0 0 0

	    CallList %(triplequadlistId)s

	    Color 1 1 1

	    Disable REGISTER_COMBINERS_NV
	    Enable BLEND
	    BlendFunc 1 1
	""" % globals()), pixzoomcs)




	# Then, render the texels in coordinate space...
	vs.put(GLRen.createTransMatrix("TEXTURE"),
		texturemat)
	vs.put(list2, pixzoomcs)

	vs.put(getDListNocoords("""
	    Disable TEXTURE_2D
	    Disable BLEND
	    PolygonMode FRONT_AND_BACK LINE
	"""))
	vs.put(coloredQuad((1,0,0)), pixzoomcs)
	vs.put(getDListNocoords("""
	    PolygonMode FRONT_AND_BACK FILL
	"""))

	# print vs.coords.transformPoints3(footprintCS, [0,0,0, 0,1,0, 1,0,0, 1,1,0], None)
	

