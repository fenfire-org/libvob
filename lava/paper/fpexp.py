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
from org.nongnu.libvob.gl import GL, GLRen
from vob.putil import cg
from vob.putil.misc import *
from vob.putil.demokeys import *
from vob.paper.texcache import getCachedTexture
import vob.paper.colors

noise = getCachedTexture(
    [512, 512, 0, 4, "RGBA", "RGBA", "noise", 
	[ "freq", "50", "bias", ".5", "scale", ".8" ]]
    )





vp = [
GL.createProgram(cg.compile("""

void main(
	float4 pos: POSITION,
	out float4 ot0 : TEXCOORD0,
	out float4 ot1 : TEXCOORD1,
	out float4 ot2 : TEXCOORD2,
	out float4 ot3 : TEXCOORD3,
	uniform float4 v0 : register(c0),
	uniform float4 v1 : register(c1),
	uniform float4 v2 : register(c2),
	uniform float4 v3 : register(c3),
	uniform float4 v4 : register(c4),
	uniform float4 v5 : register(c5),
	uniform float4 v6 : register(c6),
	uniform float4 v7 : register(c7),
	out float4  opos: POSITION
) {
    opos = mul(glstate.matrix.mvp, pos);
    float4 mpos = pos * 2;
    ot0.x = dot(mpos, v0) ;
    ot0.y = dot(mpos, v1) ;
    ot1.x = dot(mpos, v2) ;
    ot1.y = dot(mpos, v3) ;
    ot2.x = dot(mpos, v4) ;
    ot2.y = dot(mpos, v5) ;
    ot3.x = dot(mpos, v6) ;
    ot3.y = dot(mpos, v7) ;


}
""", "arbvp1"))
]

fp = [
GL.createProgram(cg.compile("""
void main(
	float4 t0 : TEXCOORD0,
	float4 t1 : TEXCOORD1,
	float4 t2 : TEXCOORD2,
	float4 t3 : TEXCOORD3,
	uniform float3 color0: register(c0),
	uniform float3 color1: register(c1),
	uniform float3 color2: register(c2),
	uniform float3 color3: register(c3),
	uniform float3 color4: register(c4),
	uniform float3 color5: register(c5),
	uniform float3 color6: register(c6),
	uniform float3 color7: register(c7),
	uniform sampler2D u0: TEXUNIT0,
	uniform sampler2D u1: TEXUNIT1,
	out float4 ocol: COLOR
) {

    float4 t0val = tex2D(u0, t0.xy * 5);
    fixed4 t1val = tex2D(u1, 1.5 * t1.xy);

    float4 coeff1 = t1val;
    float4 coeff2 = t0val;

    coeff1 = clamp(abs(coeff1), 0, 1);
    coeff2 = clamp(abs(coeff2), 0, 1);

    coeff1 *= coeff1;
    coeff2 *= coeff2;

    float sum = dot(coeff1, 1) + dot(coeff2, 1);

    coeff1 /= sum;
    coeff2 /= sum;
    
    ocol.xyz = 
	    color0 * coeff1.x + 
	    color1 * coeff1.y +
	    color2 * coeff1.z +
	    color3 * coeff1.w +
	    color4 * coeff2.x + 
	    color5 * coeff2.y +
	    color6 * coeff2.z +
	    color7 * coeff2.w;
    ocol.w = 1;
}
""", "arbfp1"))
]

class Scene:
    def __init__(self):
    	self.key = KeyPresses(
            self, 
	    SlideLin("colorseed", 400, 1, "Color seed", "c", "C"),
	    SlideLin("texseed", 800, 1, "Tex seed", "t", "T"),
	    SlideLin("texseed2", 900, 1, "Tex seed 2", "u", "U"),
	    SlideLin("coordseed", 200, 1, "coord seed", "v", "V"),
	)
	
    def scene(self, vs):

	colors = vob.paper.colors.Colors(self.colorseed,
	    minlum = 0)

	textures = vob.paper.textures.Textures(self.texseed)
	texrand = java.util.Random(491728 * self.texseed2)
	# The RNG starts badly
	for i in range(0,6): texrand.nextDouble()

	tex0 = textures.getPaperTexture("DSDT_HILO", texrand)
	tex1 = textures.getPaperTexture("RGB2", texrand)

	tcrand = java.util.Random(491728 * self.coordseed)
	# The RNG starts badly
	for i in range(0,6): tcrand.nextDouble()

	repunit = vob.paper.texcoords.TexGenXYRepeatUnit(
	    rnd = tcrand,
	    )
	
	syses = [repunit,]
	syses.extend(
	    [repunit.getRelated(tcrand) for i in range(0,4)])

	vs.put(background((.5,1,1)))
	vs.put(getDListNocoords("""
	PushAttrib ENABLE_BIT CURRENT_BIT COLOR_BUFFER_BIT TEXTURE_BIT 

	BindProgram VERTEX_PROGRAM_ARB %s
	Enable VERTEX_PROGRAM_ARB

	BindProgram FRAGMENT_PROGRAM_ARB %s
	Enable FRAGMENT_PROGRAM_ARB

	Enable BLEND
	Disable DEPTH_TEST
	AlphaFunc GREATER 0
	BlendFunc SRC_ALPHA ONE_MINUS_SRC_ALPHA

	BindTexture TEXTURE_2D %s
	TexParameter TEXTURE_2D TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
	TexParameter TEXTURE_2D TEXTURE_MAG_FILTER LINEAR
	TexParameter TEXTURE_2D TEXTURE_MAX_ANISOTROPY_EXT 20

	ActiveTextureARB TEXTURE1

	BindTexture TEXTURE_2D %s
	TexParameter TEXTURE_2D TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
	TexParameter TEXTURE_2D TEXTURE_MAG_FILTER LINEAR
	TexParameter TEXTURE_2D TEXTURE_MAX_ANISOTROPY_EXT 20

	ActiveTextureARB TEXTURE0

	""" % (
		vp[0].getProgId(),
		fp[0].getProgId(),
		tex0.getTexId(),
		tex1.getTexId(),
		    )))

	fpid =	fp[0].getProgId()

	for c in range(0,8):
	    col = colors.colorarrs[c]
	    vs.put( 
		GLRen.createProgramLocalParameterARB("FRAGMENT_PROGRAM_ARB", c),
		    vs.orthoCS(0, "aa%s"%c, 0, 
			col[0], col[1], col[2], 0))


	vind = 0
	for c in range(0, len(syses)):
	    v = syses[c]._getSTVectors(tcrand)
	    vs.put(
	      GLRen.createProgramLocalParameterARB("VERTEX_PROGRAM_ARB", 
				    vind),
		vs.orthoCS(0, "bb%s"%c, 0, 
		    v[0][0], v[0][1], v[0][2], v[0][3]))
	    vs.put(
	      GLRen.createProgramLocalParameterARB("VERTEX_PROGRAM_ARB", 
				    vind),
		vs.orthoCS(0, "bc%s"%c, 0, 
		    v[1][0], v[1][1], v[1][2], v[1][3]))
	    vind+=1
	    print v

	vs.put(GLRen.createQuad(2,2,1), 
	    vs.orthoCS(0, "C", 0, 50, 50, 700, 700))

	vs.put( getDListNocoords("PopAttrib"))
