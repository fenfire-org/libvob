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


"""Render normalized (i.e. texture coords == object coords)
quads with a single texture, with various algorithms.
For testing supersampling.

Assumptions: pixel-based projection matrix.
"""

import vob
GL = vob.gl.GL
GLRen = vob.gl.GLRen

def simpleQuadList(x0,y0,x1,y1, tex = None):
    """Get a GL.DisplayList for a simple Normalized Quad.

    x0,y0,x1,y1 -- the coordinates of the corners of the quad.
    tex -- The texture to bind (optional)
    """
    quadList = vob.gl.GL.createDisplayList(simpleQuadCode(x0,y0,x1,y1,tex))
    if tex != None:
	quadList.addDepend(tex)
    return quadList

def simpleQuadCode(x0,y0,x1,y1, tex = None):
    if tex == None:
	texCodeStart = ""
	texCodeEnd = ""
    else:
	texCodeStart = """
	    Enable TEXTURE_2D
	    BindTexture TEXTURE_2D %s
	""" % tex.getTexId()
	texCodeEnd = """
	    BindTexture TEXTURE_2D 0
	    Disable TEXTURE_2D
	"""
    str = """
	%(texCodeStart)s
	Begin QUADS
	    TexCoord %(x0)s %(y0)s
	    Vertex %(x0)s %(y0)s
	    TexCoord %(x0)s %(y1)s
	    Vertex %(x0)s %(y1)s
	    TexCoord %(x1)s %(y1)s
	    Vertex %(x1)s %(y1)s
	    TexCoord %(x1)s %(y0)s
	    Vertex %(x1)s %(y0)s
	End
	%(texCodeEnd)s
    """ % locals()
    return str

# A vertex program to set four texture coordinates
# shifted 1/4 of a pixel to each direction.
_super4vp = GL.createProgram("""!!ARBvp1.0 OPTION ARB_position_invariant;
ATTRIB tex0 = vertex.texcoord;
ATTRIB col = vertex.color;
PARAM mv[4] = { state.matrix.modelview };
PARAM imv[4] = { state.matrix.modelview.inverse };
PARAM texmat[4] = { state.matrix.texture[0] };

TEMP mu;
TEMP sh1;
TEMP sh2;
TEMP t;

PARAM foo = {1, .5, .25, 0};

# Homogeneous factor:
# The .25 needs to be scaled by this
# to get the correct offset
DP4 mu, imv[3], tex0;

DP4 sh1.x, imv[0], {.25, .25, 0, 0};
DP4 sh1.y, imv[1], {.25, .25, 0, 0};
DP4 sh1.z, imv[2], {.25, .25, 0, 0};
DP4 sh1.w, imv[3], {.25, .25, 0, 0};

DP4 sh2.x, imv[0], {.25, -.25, 0, 0};
DP4 sh2.y, imv[1], {.25, -.25, 0, 0};
DP4 sh2.z, imv[2], {.25, -.25, 0, 0};
DP4 sh2.w, imv[3], {.25, -.25, 0, 0};

MUL sh1, sh1, mu;
MUL sh2, sh2, mu;

ADD t, tex0, sh1;
DP4 result.texcoord[0].x, texmat[0], t;
DP4 result.texcoord[0].y, texmat[1], t;
DP4 result.texcoord[0].z, texmat[2], t;
DP4 result.texcoord[0].w, texmat[3], t;

ADD t, tex0, -sh1;
DP4 result.texcoord[1].x, texmat[0], t;
DP4 result.texcoord[1].y, texmat[1], t;
DP4 result.texcoord[1].z, texmat[2], t;
DP4 result.texcoord[1].w, texmat[3], t;

ADD t, tex0, sh2;
DP4 result.texcoord[2].x, texmat[0], t;
DP4 result.texcoord[2].y, texmat[1], t;
DP4 result.texcoord[2].z, texmat[2], t;
DP4 result.texcoord[2].w, texmat[3], t;

ADD t, tex0, -sh2;
DP4 result.texcoord[3].x, texmat[0], t;
DP4 result.texcoord[3].y, texmat[1], t;
DP4 result.texcoord[3].z, texmat[2], t;
DP4 result.texcoord[3].w, texmat[3], t;

MOV result.color, col;

END
""")

def super4SetupCode(tex):
    """4x supersampling setup code (text): bind textures and set lod biases.

    Doesn't work with NV25_EMULATE (it looks as if
    there was some additional lod bias),
    """
    assert _super4vp != None, "Super4 not possible on your hardware."

    progid = _super4vp.getProgId()
    texid = tex.getTexId()

    return """
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
    """ % locals()

def super4TexSetupCode():
    return """
    ActiveTexture TEXTURE3_ARB
    TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS -1
    Enable TEXTURE_2D

    ActiveTexture TEXTURE2_ARB
    TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS -1
    Enable TEXTURE_2D

    ActiveTexture TEXTURE1_ARB
    TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS -1
    Enable TEXTURE_2D

    ActiveTexture TEXTURE0_ARB
    TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS -1
    Enable TEXTURE_2D
    """ % locals()
    

def super4TeardownCode():
    """4x supersampling teardown code (text)
    """

    return """
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
    """

def super4CombinerCode():
    """4x supersampling register combiner code that
    just averages the 4 inputs, both color and alpha.
    """
    return vob.putil.nvcode.parseCombiner("""
    # Strengthen by multiplying by 4

    Enable REGISTER_COMBINERS_NV

    SPARE0 = (TEX0 + TEX1) * 0.5
    SPARE0.a = (TEX0.a + TEX1.a) * 0.5

    SPARE1 = (TEX2 + TEX3) * 0.5
    SPARE1.a = (TEX2.a + TEX3.a) * 0.5

    SPARE0 = (SPARE0 + SPARE1) * 0.5
    SPARE0.a = (SPARE0.a + SPARE1.a) * 0.5

    color = SPARE0 
    alpha = SPARE0.a
    """)

# ARB Fragment program as a replacement to the combiner code.
_super4vp_arbfp = GL.createProgram("""!!ARBfp1.0
TEMP c0,c1,c2;
TEX c0, fragment.texcoord[0], texture[0], 2D;
TEX c1, fragment.texcoord[1], texture[1], 2D;
ADD c2, c0, c1;
TEX c0, fragment.texcoord[2], texture[2], 2D;
TEX c1, fragment.texcoord[3], texture[3], 2D;
ADD c0, c0, c1;
ADD c0, c0, c2;
MUL result.color, c0, .25;
END
""")

# NV Fragment program (optimized) as a replacement to the combiner code.
_super4vp_nvfp = GL.createProgram("""!!FP1.0
TEX H0, f[TEX0], TEX0, 2D;
TEX H1, f[TEX1], TEX1, 2D;
ADDX H2, H0, H1;
MULX H2, H2, .25;
TEX H0, f[TEX0], TEX0, 2D;
TEX H1, f[TEX1], TEX1, 2D;
ADDX H0, H0, H1;
MADX o[COLH], H0, .25, H2;
END
""")

# NV Fragment program to do supersampling without a vertex program
_super4fp = [
    GL.createProgram("""!!FP1.0
	DDX H0, f[TEX0];
	DDY H1, f[TEX0];
	    MAD H0, H1.xyxy, {1,1,-1,-1}, H0.xyxy;
	    MUL H0, .25, H0;
	    ADD R0, H0, f[TEX0].xyxy;
	TEX H1, R0.xyxy, TEX0, 2D;
	TEX H2, R0.zwzw, TEX0, 2D;
	ADDX H2, H1, H2;
	MULX H2, H2, .25;
	    ADD R0, -H0, f[TEX0].xyxy;
	TEX H0, R0.xyxy, TEX0, 2D;
	TEX H1, R0.zwzw, TEX0, 2D;
	ADDX H0, H0, H1;
	MADX o[COLH], H0, .25, H2;
	END
	"""),
    GL.createProgram("""!!FP1.0
	# Speeded up through advice of beyond3d member xmas/samx
	# Should be functionally equivalent, except for minuscule rounding
	# errors from repeated add to same value

	DEFINE hc={.5,.25,-.25,0};

	DDX H0, f[TEX0];
	DDY H1, f[TEX0];
	    # MAD R0, H1.xyxy, {-0.25, -0.25, 0.25, 0.25},  f[TEX0].xyxy;
	    # MAD R0, H0.xyxy, {-0.25, -0.25, -0.25, -0.25}, R0;
	    MAD R0, H1.xyxy, hc.zzyy, f[TEX0].xyxy;
	    MAD R0, H0.xyxy, hc.zzzz, R0;
	TEX H1, R0.xyxy, TEX0, 2D;
	TEX H2, R0.zwzw, TEX0, 2D;
	ADDX H2, H1, H2;
	MULX H2, H2, .25;
	    MAD R0, H0.xyxy, hc.xxxx, R0; 
	TEX H0, R0.xyxy, TEX0, 2D;
	TEX H1, R0.zwzw, TEX0, 2D;
	ADDX H0, H0, H1;
	MADX o[COLH], H0, .25, H2;
	END
	"""),
    GL.createProgram("""!!FP1.0
	# Speeded up through advice of beyond3d member xmas/samx
	# Not functionally equivalent: supersampling grid displaced 1/4 pixel
	# in y direction! This allows one less instruction.
	DDX H0, f[TEX0];
	DDY H1, f[TEX0];
	    MAD R0, H1.xyxy, {-0.25, -0.25, 0.25, 0.25}, f[TEX0].xyxy;
	TEX H1, R0.xyxy, TEX0, 2D;
	TEX H2, R0.zwzw, TEX0, 2D;
	ADDX H2, H1, H2;
	MULX H2, H2, .25;
	    MAD R0, H0.xyxy, { 0.5, 0.5, 0.5, 0.5}, R0; 
	TEX H0, R0.xyxy, TEX0, 2D;
	TEX H1, R0.zwzw, TEX0, 2D;
	ADDX H0, H0, H1;
	MADX o[COLH], H0, .25, H2;
	END
	"""),
    GL.createProgram("""!!FP1.0
	# Speeded up through advice of beyond3d member xmas/samx
	# Same as previous but similarly displaced in both coordinates.
	DDX H0, f[TEX0];
	DDY H1, f[TEX0];
	    MADH H3, H1.xyxy, {0, 0, 0.5, 0.5}, f[TEX0].xyxy;
	TEX H1, H3.xyxy, TEX0, 2D;
	TEX H2, H3.zwzw, TEX0, 2D;
	ADDX H2, H1, H2;
	MULX H2, H2, .25;
	    MADH H3, H0.xyxy, { 0.5, 0.5, 0.5, 0.5}, H3; 
	TEX H0, H3.xyxy, TEX0, 2D;
	TEX H1, H3.zwzw, TEX0, 2D;
	ADDX H0, H0, H1;
	MADX o[COLH], H0, .25, H2;
	END
	"""),
    GL.createProgram("""!!FP1.0
	# Speeded up through advice of beyond3d member xmas/samx
	# Same as previous but similarly displaced in both coordinates.
	DDX H0, f[TEX0];
	DDY H1, f[TEX0];
	    MAD R0, H1.xyxy, {0, 0, 0.5, 0.5}, f[TEX0].xyxy;
	TEX H1, R0.xyxy, TEX0, 2D;
	TEX H2, R0.zwzw, TEX0, 2D;
	ADDX H2, H1, H2;
	MULX H2, H2, .25;
	    MAD R0, H0.xyxy, { 0.5, 0.5, 0.5, 0.5}, R0; 
	TEX H0, R0.xyxy, TEX0, 2D;
	TEX H1, R0.zwzw, TEX0, 2D;
	ADDX H0, H0, H1;
	MADX o[COLH], H0, .25, H2;
	END
	"""),
]

# NV Fragment program to do supersampling without a vertex program,
# by using aniso filtering cleverly: halve derivative in y dir, shift 1/4 
# up / down, only TEX twice
_super4fp2 = GL.createProgram("""!!FP1.0
DDX H0.xy, f[TEX0];
DDY H0.zw, f[TEX0].zwxy;
MAD R0, H0, {-.25,-.25,.25,.25}, f[TEX0].xyxy; 
MUL H0.zw, .5, H0;
TXD H1, R0.xyxy, H0.xyxy, H0.zwzw, TEX0, 2D;
TXD H0, R0.zwzw, H0.xyxy, H0.zwzw, TEX0, 2D;
ADDX H0, H1, H0;
MULX o[COLH], H0, .5;
END
""")


def super4ARBcombinerCode():
    return """
	Enable FRAGMENT_PROGRAM_ARB
	BindProgramARB FRAGMENT_PROGRAM_ARB %s
    """ % _super4vp_arbfp.getProgId()

def super4NVFPcombinerCode():
    return """
	Enable FRAGMENT_PROGRAM_NV
	BindProgramARB FRAGMENT_PROGRAM_NV %s
    """ % _super4vp_nvfp.getProgId()

def super4QuadList(x0,y0,x1,y1, tex, combinerCode=None):
    quadList = vob.gl.GL.createDisplayList(super4QuadCode(x0,y0,x1,y1,tex))
    quadList.addDepend(tex)
    return quadList


def super4QuadCode(x0,y0,x1,y1, tex, combinerCode=None):
    sq = simpleQuadCode(x0,y0,x1,y1)
    if combinerCode == None:
	combinerCode = super4CombinerCode()

    code = """
	PushAttrib TEXTURE_BIT ENABLE_BIT
    """ + super4SetupCode(tex) + combinerCode + sq + super4TeardownCode() + """
	PopAttrib
    """

    return code

def super4OnlyNVFPQuadList(index, x0,y0,x1,y1, tex):
    quadList = vob.gl.GL.createDisplayList(super4OnlyNVFPQuadCode(index, x0,y0,x1,y1,tex))
    quadList.addDepend(tex)
    return quadList

def super4OnlyNVFPQuadCode(index, x0,y0,x1,y1, tex):
    sq = simpleQuadCode(x0,y0,x1,y1,tex)
    return """
	PushAttrib TEXTURE_BIT ENABLE_BIT
	Enable FRAGMENT_PROGRAM_NV
	BindProgramARB FRAGMENT_PROGRAM_NV %s
	TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS -1
    """ % _super4fp[index].getProgId() + sq +  """
	TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS 0
	PopAttrib
    """


def super4OnlyNVFPQuadList2(x0,y0,x1,y1, tex):
    quadList = vob.gl.GL.createDisplayList(super4OnlyNVFPQuadCode2(x0,y0,x1,y1,tex))
    quadList.addDepend(tex)
    return quadList

def super4OnlyNVFPQuadCode2(x0,y0,x1,y1, tex):
    tex.setTexParameter("TEXTURE_2D", "TEXTURE_MAX_ANISOTROPY_EXT", "2")
    sq = simpleQuadCode(x0,y0,x1,y1,tex)
    return """
	PushAttrib TEXTURE_BIT ENABLE_BIT
	Enable FRAGMENT_PROGRAM_NV
	BindProgramARB FRAGMENT_PROGRAM_NV %s
	TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS -1
    """ % _super4fp2.getProgId() + sq +  """
	TexEnv TEXTURE_FILTER_CONTROL TEXTURE_LOD_BIAS 0
	PopAttrib
    """
	



