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

noise = getCachedTexture(
    [1024, 512, 0, 4, "RGBA", "RGBA", "noise", 
	[ "freq", "100", "bias", ".5", "scale", ".8" ]]
    )

lnoise = getCachedTexture(
    [256, 256, 0, 4, "RGBA", "RGBA", "noise", 
	[ "freq", "10", "bias", ".5", "scale", ".8" ]]
    )

turb = getCachedTexture(
    [512, 512, 0, 4, "RGBA", "RGBA", "fnoise", 
	[ "turb", "1",  "freq", "1", "scale", ".3", "bias", ".3",
	    "freq2", "100", "df", "1",
	     ]]
    )



vp = [
GL.createProgram(cg.compile("""

float edge[5][4] = {
    {0, 0, 0, 1},
    {0, 1, 0, 1},
    {1, 1, 0, 1},
    {1, 0, 0, 1},
    {0, 0, 0, 1},
};

float2 inters(float2 what) {
    float2 ctr = float2(.5,.5);
    float2 vec = what-ctr;
    float2 a = abs(vec);
    // if(a.x + a.y < .001) return float4(0,0,0,1);
    float mul;
    if(a.x > a.y) { 
	mul = .5 / a.x;
    } else {
	mul = .5 / a.y;
    }
    return ctr + vec * mul;
}

void main(
	float4 t: TEXCOORD0,
	float4 pos: POSITION,
	out float4  opos: POSITION,
	out float4 ocol : TEXCOORD0
) {
    float4 ctr = float4(.5,.5,0,1);
    float4 ctr1 = mul(glstate.matrix.program[0], ctr);
    float4 ctr2 = mul(glstate.matrix.program[1], ctr);

    float4 ctr1_in2 = mul(glstate.matrix.inverse.program[1], ctr1);
    float4 ctr2_in1 = mul(glstate.matrix.inverse.program[0], ctr2);

    // Solve eq: find intersections of unit squares
    float2 inters1 = inters(ctr2_in1.xy);
    float2 inters2 = inters(ctr1_in2.xy);
    // float2 inters1 = ctr.xy;
    // float2 inters2 = ctr.xy;

    float inter = frac(4*pos.x);
    float edgeind = fmod(floor(4*pos.x), 4) ;


    float2 xa = float2(0,0);
    float2 xb = float2(0,0);

    xa.x = (edgeind >= 2 && edgeind < 4);
    xa.y = (edgeind >= 1 && edgeind < 3);

    xb.x = (edgeind >= 1 && edgeind < 3);
    xb.y = (edgeind >= 0 && edgeind < 2);

/* DOESN'T WORK

    float4 xa = edge[edgeind];
    float4 xb = edge[edgeind+1];
*/


    float2 x = lerp(xa, xb, inter);
    // x = float4(pos.x, 0, 0, 1);

    float shri = 3.7*(pos.y - pos.y*pos.y);

    float4 sx1;
    float4 sx2;
    sx1.xy = lerp(x, inters1, shri);
    sx2.xy = lerp(x, inters2, shri);

    sx1.z = 0;
    sx1.w = 1;
    sx2.z = 0;
    sx2.w = 1;

    float4 x1 = mul(glstate.matrix.program[0], sx1);
    float4 x2 = mul(glstate.matrix.program[1], sx2);

    float4 p = lerp(x1, x2, pos.y);

    float4 pin1 = mul(glstate.matrix.inverse.program[0], p);
    float4 pin2 = mul(glstate.matrix.inverse.program[1], p);

    pin1 /= pin1.w;
    pin2 /= pin2.w;

    pin1 -= .5; 
    pin2 -= .5;

    pin1 = abs(pin1) * 2;
    pin2 = abs(pin2) * 2;

    pin1 = max(pin1.x, pin1.y);
    pin2 = max(pin2.x, pin2.y);

    opos = mul(glstate.matrix.projection, p);
    ocol.xy = pos.xy; 
    ocol.z = shri;

    ocol.w = min(pin1, pin2).x;

//    ocol.w = .5;
 //   oc.z = 1;
}
""", "arbvp1")),
]

fp = [
GL.createProgram(cg.compile("""
void main(
	float4 p: TEXCOORD0,
	out float4 ocol: COLOR,
	uniform sampler2D t0: TEXUNIT0,
	uniform sampler2D t1: TEXUNIT1
) {
    // Don't render inside the rectangles
    // if(p.w < 1) discard;

    float3 dark = float3(186, 71, 18) / 255.0;
    float3 light = float3(249, 233, 50) / 255.0;

    float4 dtex = tex2D(t0, p.xy * float2(7, .1));
    float4 atex = tex2D(t1, p.xy * float2(2, 2));

    ocol.xyz = lerp(dark, light, dtex.x);
    ocol.w = .1 + .5*smoothstep(.5, 1.3, p.w) + 
	(.5 * smoothstep(.5, .6, atex.x));

    // ocol.xyz = 0;

}
""", "arbfp1")),
]


class Scene:
    def __init__(self):
    	self.key = KeyPresses(
            self, 
	    SlideLin("x", 400, 50, "x coord", "Left", "Right"),
	    SlideLin("y", 400, 50, "y coord", "Up", "Down"),
	    SlideLin("a", 0, 15, "Rotation", "Prior", "Next"),
	)
    def scene(self, vs):
	vs.put(background((.5,1,1)))

	vs.put(GLRen.createTransMatrix("MATRIX0_ARB"),
		vs.orthoCS(0, "A", 0, 200, 200, 200, 100))
	vs.put(GLRen.createTransMatrix("MATRIX1_ARB"),
		vs.rotateCS(
		    vs.orthoCS(0, "B", 0, self.x, self.y, 150, 150),
		    "R", self.a
		    ))

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
		noise.getTexId(),
		lnoise.getTexId(),
		    )))

	vs.put(GLRen.createQuad(40,40,1), 
	    vs.orthoCS(0, "C", 0, 100, 500, 20, 20))

        vs.put( getDListNocoords("PopAttrib"))

