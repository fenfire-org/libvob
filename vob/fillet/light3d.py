# 
# Copyright (c) 2003, Janne V. Kujala and Tuomas J. Lukka
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

from org.nongnu.libvob.gl import GL, GLRen
from org.nongnu.libvob.util import ColorUtil
from vob.putil import cg
from vob.putil.misc import *
from vob.putil.demokeys import *

from vob.paper.texcache import getCachedTexture
tex = getCachedTexture(
    [1024, 512, 0, 4, "RGBA", "RGBA", "noise", 
     [ "freq", "100", "bias", ".5", "scale", ".8" ]]
    )

tex = GL.createTexture()
GL.call("""
BindTexture TEXTURE_2D %s
TexImage2D TEXTURE_2D 0 ALPHA 16 16 0 ALPHA \
1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \
1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \
1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 \
1 1 1 0 0 0 0 0 0 0 0 0 0 1 1 1 \
1 1 0 0 0 0 0 0 0 0 0 0 0 0 1 1 \
1 1 0 0 0 0 0 0 0 0 0 0 0 0 1 1 \
1 1 0 0 0 0 0 0 0 0 0 0 0 0 1 1 \
1 1 0 0 0 0 0 0 0 0 0 0 0 0 1 1 \
1 1 0 0 0 0 0 0 0 0 0 0 0 0 1 1 \
1 1 0 0 0 0 0 0 0 0 0 0 0 0 1 1 \
1 1 0 0 0 0 0 0 0 0 0 0 0 0 1 1 \
1 1 0 0 0 0 0 0 0 0 0 0 0 0 1 1 \
1 1 1 0 0 0 0 0 0 0 0 0 0 1 1 1 \
1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 \
1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \
1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \

TexImage2D TEXTURE_2D 1 ALPHA 8 8 0 ALPHA \
1 1 1 1 1 1 1 1 \
1 1 1 1 1 1 1 1 \
1 1 1 0 0 0 1 1 \
1 1 0 0 0 0 0 1 \
1 1 0 0 0 0 0 1 \
1 1 0 0 0 0 0 1 \
1 1 1 0 0 0 1 1 \
1 1 1 1 1 1 1 1

TexImage2D TEXTURE_2D 2 ALPHA 4 4 0 ALPHA \
1 1 1 1 \
1 1 1 1 \
1 1 0 1 \
1 1 1 1

TexImage2D TEXTURE_2D 3 ALPHA 2 2 0 ALPHA \
1 1 \
1 1

TexParameter TEXTURE_2D TEXTURE_BASE_LEVEL 0
TexParameter TEXTURE_2D TEXTURE_MAX_LEVEL 3
TexParameter TEXTURE_2D TEXTURE_WRAP_S REPEAT
TexParameter TEXTURE_2D TEXTURE_WRAP_T REPEAT
TexParameter TEXTURE_2D TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
TexParameter TEXTURE_2D TEXTURE_LOD_BIAS -1.25
BindTexture TEXTURE_2D 0
""" % tex.getTexId())

vp = [
GL.createProgram(cg.compile("""
void main(
    float4 pos: POSITION,
    float3 norm: NORMAL,
    float4 tex0: TEXCOORD0,
    float4 col: COLOR,
    out float4 opos: POSITION,
    out float4 ocol: COLOR,
    out float4 otex0: TEXCOORD0
) {
    opos = mul(glstate.matrix.mvp, pos);
    float4x4 foo = glstate.matrix.modelview[0];
    float3 normvec = normalize(float3(
                  dot(foo[0].xyz, norm),
                  dot(foo[1].xyz, norm),
                  dot(foo[2].xyz, norm)));

    float3 lightvec = normalize(float3(-1, -1, -1));
    float3 lightcolor = float3(1, .5, 1);
    float3 specularcolor = float3(1, 1, 1);
    
    float3 lightvec2 = normalize(float3(.5, .5, -1));
    float3 light2color = float3(.5, 1, .5);

    float3 eyevec = float3(0,0,-1);

    float3 halfvec = normalize(lightvec + eyevec);
    float diffuse = dot(normvec, lightvec);
    float diffuse2 = dot(normvec, lightvec2);
    float specular = dot(normvec, halfvec);
    float4 lighting = lit(diffuse, specular, 10);

    float t = (1 - tex0.z);
    float3 defaultcolor = float3(.2, .2, 1);
    float3 color = lerp(defaultcolor, col, t * t);

    ocol.rgb = lighting.y * color * lightcolor
             + lighting.z * specularcolor
             + max(diffuse2, 0) * color * light2color
             //+ max(-normvec.z, 0) * col;
             ;
    // Fog
    // float t = 8*mul(glstate.matrix.mvp, pos).z;
    // t = 8*mul(glstate.matrix.mvp, pos).z*40;
    // ocol.rgb = ocol.rgb * (1 - t) + float3(1,.5,.2) * t;
    ocol.a = 1;

    otex0 = tex0 * float4(20, 20, 0, 1);
}

""", "arbvp1")),
GL.createProgram(cg.compile("""
void main(
    float4 pos: POSITION,
    float4 tex0: TEXCOORD0,
    float4 col: COLOR,
    out float4 opos: POSITION,
    out float4 ocol: COLOR,
    out float4 otex0: TEXCOORD0
) {
    opos = mul(glstate.matrix.mvp, pos);
    float3 defaultcolor = float3(.2, .2, 1);
    float t = (1 - tex0.z);
    ocol.rgb = lerp(defaultcolor, col, t * t);
    ocol.a = 1;

    otex0 = tex0 * float4(20, 20, 0, 1);
}

""", "arbvp1")),

]

edgefp = GL.createProgram(cg.compile("""
float dep(float2 tc, uniform samplerRECT tex0) {
    float4 dotvec = {
	    1./(256*256)
	    ,
	    1./(256)
	    , 
	    1
	    , 
	    0
	    };
    float4 d = texRECT(tex0, tc);
    return dot(d, dotvec);
}

void main(
    float4 incol: COLOR0,
    float4 qpos : TEXCOORD0,
    float4 wpos : WPOS,
    uniform samplerRECT tex0,
    out float4 col : COLOR) {

    float2 tc = wpos.xy;

    float d = dep(tc, tex0);
    float4 neigh = float4(
		dep(tc + float2(0, 1), tex0),
		 dep(tc + float2(0, -1), tex0),
		 dep(tc + float2(1, 0), tex0),
		 dep(tc + float2(-1, 0), tex0));
    float4 del = abs(neigh-d);
    float dmax = max(del.x, max(del.y, max(del.z, del.w)));
    
    float4 color;
//    color.xyz = (de - .52) * 100;
    color.xyz = 0;
    color.w = dmax > 0.002;

    col = color;
}
    
""", "fp30"))

edgefp = GL.createProgram("""!!FP1.0
TEX R0, f[WPOS].xyxx, TEX0, RECT;
DP4R R0.x, R0, {1.5258789e-05, 0.00390625, 1, 0};

ADDR R1.xy, f[WPOS].xyxx, {-1, 0}.xyxx;
TEX R1, R1.xyxx, TEX0, RECT;
DP4R R2.x, R1, {1.5258789e-05, 0.00390625, 1, 0};

ADDR R1.xy, f[WPOS].xyxx, {1, 0}.xyxx;
TEX R1, R1.xyxx, TEX0, RECT;
DP4R R2.y, R1, {1.5258789e-05, 0.00390625, 1, 0};

ADDR R1.xy, f[WPOS].xyxx, {0, -1}.xyxx;
TEX R1, R1.xyxx, TEX0, RECT;
DP4R R2.z, R1, {1.5258789e-05, 0.00390625, 1, 0};

ADDR R1.xy, f[WPOS].xyxx, {0, 1}.xyxx;
TEX R1, R1.xyxx, TEX0, RECT;
DP4R R2.w, R1, {1.5258789e-05, 0.00390625, 1, 0};

#ADDR R3, R2.xyzw, R2.yxwz;
#MADR R3, -0.5, R3, R0.x;
#ADDR R3, |R3.x|, |R3.z|;
#MULR R3, R3, 100;

ADDR R2, R2, -R0.x;
MAXR R1.xy, |R2.xyxx|, |R2.wzxx|;
MAXR R1.x, R1.x, R1.y;
#MAXR R1.x, R1.x, R3.x;#########
SGTR H0.x, R1.x, {0.0020000001}.x;
MOVR o[COLR].xyz, {0, 0, 0}.xyzx;
MOVR o[COLR].w, H0.x;
END
""")


commonkeys = [
	    SlideLin("angle", 1, .05, "tan(meet angle)", "+", "-"),
	    SlideLin("thick", 1, .1, "thickness", "T", "t"),
	    Toggle("drawEdge", 0, "Draw edge", "e"),
	    Toggle("drawInside", 1, "Draw inside", "i"),
	    Toggle("depthColor", 1, "Color from depth", "d"),
	    Toggle("lines", 0, "Toggle showing lines", "l"),
	    Toggle("ellipses", 1, "Toggle ellipses", "s"),
	    Toggle("stretched", 1, "Toggle stretched", "v"),
	    Toggle("curvature", 0, "Show curvature", "c"),
	    Toggle("sectors", 1, "Show sectors", "w"),
	    Toggle("fillets", 1, "Toggle filleting", "f"),
	    SlideLin("size", 100, 10, "Node size", "K", "k"),
	    SlideLin("dice", 20, 1, "Dice factor", "P", "p"),
	    Toggle("fillet3d", 1, "3D fillets", "3"),
	    Toggle("blend3d", 0, "3D fillets blend", "4"),
	    SlideLin("linewidth", 2, 1, "line width", "B", "b"),
	    Toggle("texture", 0, "texture", "x"),
	    SlideLin("dicelen", 100, 5, "Dice length", "G", "g"),
	    SlideLin("tblsize", 20, 1, "Table size", "<", ">"),
	    SlideLin("mode", 0, 1, "Blending mode", "M", "m"),
]

width = 0
height = 0
directDepthCopy = 0

def initBuffers(w, h):
    print "initBuffers(%s,%s)" % (w,h)
    global width, height, depthTexture, tmpTexture, directDepthCopy
    if width == w and height == h: return

    depthTexture = GL.createTexture()
    tmpTexture = GL.createTexture()
	
    if not GL.hasExtension("GL_NV_texture_rectangle"): return

    width,height = w,h

    rect = 1
    targ = "TEXTURE_RECTANGLE_NV"
    if directDepthCopy:
        depthTexture.loadNull2D(targ, 0, "DEPTH_COMPONENT24",
				width, height, 0, "DEPTH_COMPONENT", "INT")
    else:
        depthTexture.loadNull2D(targ, 0, "RGBA8",
				width, height, 0, "BGRA", "INT")

	tmpTexture.loadNull2D(targ, 0, "RGB8",
			      width, height, 0, "RGB",  "INT")
	tmpTexture.setTexParameter(targ, "TEXTURE_MIN_FILTER", "LINEAR")
	tmpTexture.setTexParameter(targ, "TEXTURE_MAG_FILTER", "LINEAR")
	
    depthTexture.setTexParameter(targ, "TEXTURE_MIN_FILTER", "LINEAR")
    depthTexture.setTexParameter(targ, "TEXTURE_MAG_FILTER", "LINEAR")



def drawFillets(self, vs, pc):
	vs.put(getDListNocoords("""
	    PushAttrib POLYGON_BIT ENABLE_BIT
	    Enable DEPTH_TEST
	    DepthFunc LEQUAL
	    LineWidth %s
	    PolygonOffset -100 -100
	    ShadeModel SMOOTH
	""" % self.linewidth))

	if self.lines:
	    vs.put(getDListNocoords("PolygonMode FRONT_AND_BACK LINE"))

    	border = 2
        spans = GLRen.createFilletSpan2(border, self.dice, 
                                    1 +
                                    4 * self.depthColor +
                                    16 * self.ellipses +
                                    64 * self.stretched +
                                    128 * self.sectors)
	conns = GLRen.createSortedConnections(spans, spans, 2)
        spans = GLRen.createFilletSpan2(border, self.dice, 
                                    2 +
                                    4 * self.depthColor +
                                    16 * self.ellipses +
                                    64 * self.stretched +
                                    128 * self.sectors)
	conns_l = GLRen.createSortedConnections(spans, spans, 2)
        spans = GLRen.createFilletSpan2(1000, self.dice, 
                                    2 +
                                    4 * self.depthColor +
                                    16 * self.ellipses +
                                    32 +
                                    64 * self.stretched +
                                    128 * self.sectors)
	conns_c = GLRen.createSortedConnections(spans, spans, 2)

        f3d = GLRen.createFillet3D(border, self.dice, 1);

        conns3d = GLRen.createIterConnections(f3d, f3d, 2);

        conns3dblend = GLRen.createFillet3DBlend(self.dice, self.dicelen, self.tblsize, self.mode);

        if self.fillets:
            thick = vs.coords.rational1D22(0, .5 * self.thick, 0, 0,  1, 1, 0);
            angle = vs.coords.rational1D22(0, self.angle, 0, 0,  1, 0, 0);

            thick = vs.coords.rational1D22(0,
                                           2*self.thick, self.thick,
                                           .1, 2, 1, 1);
            
            angle = vs.coords.power1D2(0, self.angle, 1/3., 1, -1);
        else:
            thick = vs.coords.power1D2(0, .15 * self.thick, 0, 0, 0)
            angle = vs.coords.power1D2(0, 0, 1/3., 0, -1)

        vs.matcher.add(thick, "Thi")
        vs.matcher.add(angle, "Ang")

        if self.fillet3d:
            vs.put(getDListNocoords("""
	    Color 1 1 1
            BindProgram VERTEX_PROGRAM_ARB %s
            Enable VERTEX_PROGRAM_ARB
            MatrixMode PROJECTION
            PushMatrix
            MatrixMode MODELVIEW
            PushMatrix
            """ % vp[not self.depthColor].getProgId()))

            if self.texture:
                vs.put(getDListNocoords("""
                BindTexture TEXTURE_2D %s
                #TexParameter TEXTURE_2D TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
                #TexParameter TEXTURE_2D TEXTURE_MAG_FILTER LINEAR
                #TexParameter TEXTURE_2D TEXTURE_MAX_ANISOTROPY_EXT 20
                TexEnv TEXTURE_ENV TEXTURE_ENV_MODE MODULATE
                Enable TEXTURE_2D
                """ % tex.getTexId()))

            if self.blend3d and self.fillets:
                vs.put(getDListNocoords("Color .2 .2 1"))
                pc(conns3dblend, [thick, angle])

            if self.drawInside or not self.fillets:
                vs.put(getDListNocoords("Color .2 .2 1"))
                pc(conns3d, [thick, angle])

            vs.put(getDListNocoords("""
            MatrixMode PROJECTION
            PopMatrix
            MatrixMode MODELVIEW
            PopMatrix
            """))
        else:
            if hasattr(self, 'drawInsideColor'):
                vs.put(getDListNocoords("Color "+ColorUtil.colorGLString(self.drawInsideColor)))
            else: vs.put(getDListNocoords("Color 1 1 1"))
            if self.drawInside: pc(conns, [thick, angle])
            vs.put(getDListNocoords("""
	    Color 0 0 0
            """))
            if self.drawEdge: pc(conns_l, [thick, angle])

            if self.curvature : pc(conns_c, [thick, angle])

            
	vs.put(getDListNocoords("""
            BindProgram VERTEX_PROGRAM_ARB 0
            BindTexture TEXTURE_2D 0
	    PopAttrib
	"""))


	if self.fillet3d and self.drawEdge:
	    size = vs.getSize()
	    initBuffers(size.width, size.height)

	    # Now, draw the edges
	    if directDepthCopy:
	        vs.put(getDListNocoords("""
	            BindTexture TEXTURE_RECTANGLE_NV %s
		""" % depthTexture.getTexId()))
		vs.put(GLRen.createCopyTexSubImage2D("TEXTURE_RECTANGLE_NV",
						     0, 0, 0, width, height),
		       0)

	    else:
		vs.put(getDListNocoords("""
		PushAttrib ENABLE_BIT
		Disable ALPHA_TEST
		Disable DEPTH_TEST
		Disable TEXTURE_RECTANGLE_NV
		Disable TEXTURE_2D
		"""))

	        # Copy the color buffer to a texture
		vs.put(getDListNocoords("""
	            BindTexture TEXTURE_RECTANGLE_NV %s
		    """ % tmpTexture.getTexId()))
		vs.put(GLRen.createCopyTexSubImage2D("TEXTURE_RECTANGLE_NV",
						     0, 0, 0, width, height),
		       0)

		# Copy stencil&depth to color
		csdest = vs.translateCS(0,"copydest",0,height)
		vs.put(GLRen.createCopyPixels(width, height,
					      "DEPTH_STENCIL_TO_BGRA_NV"),
		       0,
		       csdest,
		       )

		# Read the depth to a texture
		vs.put(getDListNocoords("""
                    BindTexture TEXTURE_RECTANGLE_NV %s
		    """ % depthTexture.getTexId()))
		vs.put(GLRen.createCopyTexSubImage2D("TEXTURE_RECTANGLE_NV",
						     0, 0, 0, width, height),
		       0)

		# Copy the color back from the backup texture
		vs.put(getDListNocoords("""
	            BindTexture TEXTURE_RECTANGLE_NV %s
		    Enable TEXTURE_RECTANGLE_NV
		    """ % tmpTexture.getTexId()))
		vs.put(getDList("""
                    Color 1 1 1
		    Begin QUAD_STRIP
		    TexCoord 0 0
		    Vertex 0 %(h)s
		    
		    TexCoord 0 %(h)s
		    Vertex 0 0
		    
		    TexCoord %(w)s  0
		    Vertex %(w)s %(h)s
		    
		    TexCoord %(w)s %(h)s
		    Vertex %(w)s 0
		    End
		""" % { "w" : width, "h" : height }), 0)
            
		vs.put(getDListNocoords("""
		    PopAttrib
		"""))

	    vs.put(getDListNocoords("""
	        PushAttrib ENABLE_BIT
	        Color 1 1 1
		BindProgram FRAGMENT_PROGRAM_NV %s
		Enable FRAGMENT_PROGRAM_NV
		Enable BLEND
		BindTexture TEXTURE_RECTANGLE_NV %s
	    """ % (edgefp.getProgId(),
		   depthTexture.getTexId())))

	    vs.put(quad(), vs.orthoCS(0, "nprEdgeQuad", -100, 0, 0,
				      width, height))

	    vs.put(getDListNocoords("""
	        BindTexture TEXTURE_RECTANGLE_NV 0
		PopAttrib
	    """))
