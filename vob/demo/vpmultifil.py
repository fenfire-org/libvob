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


from __future__ import nested_scopes
import math

import vob
from org.nongnu.libvob.gl import GL, GLRen
from vob.putil import cg
from vob.putil.misc import *
from vob.putil.demokeys import *
from vob.paper.texcache import getCachedTexture

vp = [
GL.createProgram(cg.compile("""

float2 r90(float2 v) {
    return float2(v.y, -v.x);
}

float2 rn90(float2 v) {
    return float2(-v.y, v.x);
}

void main(
    float4 pos: POSITION,
    uniform float2 center: C0,
    uniform float3 zooms: C1,
    uniform float3 zs: C2,
    uniform float3 angles: C3,
    uniform float3 dists: C4,
    uniform float4 params: C5,
    out float4 opos: POSITION,
    out float4 col: COLOR
) {
    float thickness = 5 + 1000 / (dists.y * dists.y);

    float pi = 3.14159;

    float2 targetvector = float2(sin(angles.y), cos(angles.y));
    float2 dtargetvector = angles.x * r90(targetvector);

    float rangle = angles.y + angles.x * pi / 4;
    float2 touchvector = float2(sin(rangle), cos(rangle));

    float sinangle = abs(dot(r90(targetvector), touchvector));
    float cosangle = dot(targetvector, touchvector);

    float2 tangentinters =
      dists.x * targetvector / cosangle;
    

    float4 p0 = float4(0,0,0,1);
    p0.xy = targetvector * dists.y + 
		dtargetvector * thickness;

    float4 p2 = float4(0,0,0,1);
    p2.xy = touchvector * dists.x;

    float4 p1 = float4(0,0,0,1);
    p1.xy = lerp(p2.xy, tangentinters,
		    1-(thickness / dists.x) / sinangle);

    // weights - multiply all homog coords
    p0 *= .1;
    p1 *= 1;
    p2 *= 5;

    float4 coeff = float4(
	pos.x * pos.x,
	2 * pos.x * (1-pos.x),
	(1-pos.x) * (1-pos.x),
	0
    );

    // coeff = float4(.5,.3,.2,0);

    float4 bez = coeff.x * p0 + coeff.y * p1 + coeff.z * p2;
    bez /= bez.w;

    float2 foopos = lerp(p0.xy, // targetvector * dists.y, 
			p1.xy, // touchvector * dists.x,
			pos.x) ;

    // bez.xy = lerp(foopos.xy, bez.xy, pos.y);

    float4 rpos4 = float4(0,0,0,1);
    rpos4.xy = center + bez.xy + 5 * pos.y * float2(1,1);

    opos = mul(glstate.matrix.mvp, rpos4);

    col.xyz = coeff.xyz;
    col.w = .8;
}

""", "arbvp1")),
]

foo = [
# GL.createProgram(cg.compile("""
(("""

float2 r90(float2 v) {
    return float2(v.y, -v.x);
}

float2 rn90(float2 v) {
    return float2(-v.y, v.x);
}

void main(
    float4 pos: POSITION,
    uniform float2 center: C0,
    uniform float3 zooms: C1,
    uniform float3 zs: C2,
    uniform float3 angles: C3,
    uniform float3 dists: C4,
    uniform float4 params: C5,
    out float4 opos: POSITION,
    out float4 col: COLOR
) {
    float angle = abs(angles.z - angles.y);
    float thickness = 5 + 50 / dists.y;
    float erad = (1 + .5 * (1-smoothstep(0, 1, angle))) * dists.x;

    float xcoord = lerp(.1 / angle, 1, pos.x * pos.x);
    float ycoord = (1-pos.x)*(1-pos.x);

    float realy = lerp(erad, dists.y, ycoord);


    xcoord /= realy / erad;

    float2 targetvector = float2(sin(angles.y), cos(angles.y));
    float2 dtargetvector = angles.x * r90(targetvector);


    float curang = lerp(angles.y, angles.z, xcoord / 2);


    float2 v = float2(sin(curang), cos(curang));
    float2 dv = angles.x * r90(v);

    float2 rpos = center + 
	(realy) * v +
	// rand offset
	pos.y * 2 * v + pos.y * 2 * dv;

    float4 rpos4 = float4(0,0,0,1);
    rpos4.xy = rpos;
    
    opos = mul(glstate.matrix.mvp, rpos4);

    col = pos;
    col.w = .8;
}

""", "arbvp1"))
]

class Scene:
    def __init__(self):
    	self.key = KeyPresses(
            self, 
	    SlideLin("l1", 200, 20, "l1", "Left", "Right"),
	    SlideLin("l2", 300, 20, "l2", "Up", "Down"),
	    SlideLin("ang", 0, .1, "Rotation", "Prior", "Next"),
	)
    def scene(self, vs):
	vs.put( background((.2,1,1)))

	vs.put(getDListNocoords("""
        PushAttrib ENABLE_BIT CURRENT_BIT COLOR_BUFFER_BIT TEXTURE_BIT 

        BindProgram VERTEX_PROGRAM_ARB %s
        Enable VERTEX_PROGRAM_ARB
	""" % (
	    vp[0].getProgId(),
	    )))

	a = []

	def param(i, v1=0, v2=0, v3=0, v4=0):
	    vs.put(GLRen.createProgramLocalParameterARB(
			"VERTEX_PROGRAM_ARB", i),
			vs.orthoCS(0, str(len(a)), 0, v1, v2, v3, v4))
	    a.append(0)

	param(0, 300, 300)

	q = GLRen.createQuad(500, 2, 1)

	param(3, -1, 3, self.ang)
	param(4, 30, self.l1, self.l2)

	vs.put(q,  0)

	param(3, 1, self.ang, 3)
	param(4, 30, self.l2, self.l1)

	vs.put(q, 0)

	param(3, 1, 3, self.ang + 2*math.pi)
	param(4, 30, self.l1, self.l2)

	vs.put(q,  0)

	param(3, -1, self.ang + 2*math.pi, 3)
	param(4, 30, self.l2, self.l1)

	vs.put(q, 0)


        vs.put( getDListNocoords("PopAttrib"))


