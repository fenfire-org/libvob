# 
# Copyright (c) 2003, Tuomas J. Lukka and Janne Kujala
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
from org.nongnu.libvob.gl import GL
from vob.putil.misc import *
from vob.putil.demokeys import *

from vob.paper.texcache import getCachedTexture

vob.AbstractUpdateManager.defaultAnimationTime = 10000

from math import sin
import math
import jarray


def init():
    font = GL.createFont(None, 64)
    font2 = GL.createFont(None, 64)
    vob.putil.demowindow.font = font
    vob.putil.demowindow.font2 = font2

    vob.putil.demowindow.mouser = vob.util.PS2Reader("/dev/input/mouse0")

    fonttexs = []

    fn = jarray.zeros(10*10, 'b')
    fn2 = jarray.zeros(10*10, 'h')

    fn[15] = 1
    fn[14] = 1
    fn[13] = 1
    fn[12] = 1
    fn[25] = 1
    fn[24] = 1
    fn[23] = 1
    fn[22] = 1
    fn[35] = 1
    fn[34] = 1
    fn[33] = 1
    fn[32] = 1
    for i in range(0,20): # exercise JVM
	print "Exercise ",i
	vob.util.TexManip.minDist(fn, fn2, 10, 10, 10)

	for y in range(0,10):
	    print " ".join([str(fn2[10*y + i]) for i in range(0,10)])


    print "Tex: ", font.getNTextures();
    for i in range(0, font.getNTextures()):
	t = font.getTexture(i)
	vob.putil.texture.printTex(t.getTexId())
	w = int(t.getLevelParameter(0, "TEXTURE_WIDTH")[0])
	h = int(t.getLevelParameter(0, "TEXTURE_HEIGHT")[0])
	print "T",i,t,w,h

	fn = jarray.zeros(2*w*h, 'b')
	fn2 = jarray.zeros(w*h, 'h')
	fn3 = jarray.zeros(4*w*h, 'b')

	t.getTexImage(0, "ALPHA", "UNSIGNED_BYTE", fn)
    #    for i in range(0, w*h, 5):
    #	fn[i] += int(20 * sin(i))

    #    for x in range(0,w,20):
    #	for y in range(0,w,20):
    #	    print fn[x + w*y]
	
	# Distance * 16
	vob.util.TexManip.minDist(fn, fn2, w, h, 16)
	print "Maniped1"
	vob.util.TexManip.b2s(fn2, fn3, 4)

	print "Maniped"

    #    for x in range(0,w,20):
    #	for y in range(0,w,20):
    #	    print fn[x + w*y]
       
	t.setTexParameter("TEXTURE_2D", "GENERATE_MIPMAP_SGIS", "TRUE");

	for y in range(0,50):
	    print " ".join([str(fn3[1000*y + i]) for i in range(0,30)])
	t.texImage2D(0, "SIGNED_HILO16_NV", w, h, 0, "HILO_NV", "SHORT", fn3)

	fonttexs.append(t)

if not hasattr(vob.putil.demowindow, "font"):
    init()

font = vob.putil.demowindow.font
font2 = vob.putil.demowindow.font2
for i in range(0, font.getNTextures()):
    t = font.getTexture(i)
    t.setTexParameter("TEXTURE_2D", "TEXTURE_MIN_FILTER", "NEAREST");


args = [ [256, 256, 0, 4, "RGBA", "RGBA", "noise",
          ["bias", ".5", "scale", "1.0", "type", "faBm", "freq", "10"]],
         [64, 64, 0, 4, "RGBA", "RGBA", "geometric", ["type", "0"]],
         [64, 64, 0, 4, "RGBA", "RGBA", "geometric", ["type", "1"]],
         [64, 64, 0, 4, "RGBA", "RGBA", "geometric", ["type", "2"]],
         [2, 2, 0, 4, "RGBA", "RGBA", "geometric", ["type", "3"]],
         [64, 64, 0, 4, "RGBA", "RGBA", "geometric", ["type", "4"]],
         [64, 64, 0, 4, "RGBA", "RGBA", "geometric", ["type", "5"]],
         [2, 2, 0, 4, "RGBA", "RGBA", "geometric", ["type", "6"]],
         ]

tex = getCachedTexture(args[7])

# 16 * texel [1/512]

from vob.putil import cg

fp = [
GL.createProgram(cg.compile("""
void main(
	float2 t : TEXCOORD0,
	uniform float2 meas: register(c0),
	uniform sampler2D t0: TEXUNIT0,
	out half4 color: COLOR) {
    float2 tx = ddx(t);
    float2 ty = ddy(t);

    float le = sqrt(length(tx) * length(ty));

    t -= .5*(tx + ty);

    float c = 1;

    float2 dx = tx, dy = ty;
    half4 c0 = tex2D(t0, t + c*(dx+dy));
    half4 c1 = tex2D(t0, t + c*(dx));
    half4 c2 = tex2D(t0, t + c*(dy));
    half4 c3 = tex2D(t0, t );

    half4 dists = 256 * 16 * half4(
	c0.x,
	c1.x,
	c2.x,
	c3.x);

    float tdx = (dists.x + dists.y - dists.z - dists.w);
    float tdy = (dists.x + dists.z - dists.y - dists.w);

    float ax = abs(tdx);
    float ay = abs(tdy);

    float maxx = max(ax, ay);
    float minx = min(ax, ay);

    float angle = (maxx < .001 ? 0 : minx / maxx);

    float grayval = .25 * dot(clamp((dists + le) / le, 0,1), 
			half(1).xxxx).x;

    color.x = (angle * angle);
    color.y = .25 * dot((dists>float(0.).xxxx), half(1).xxxx).x;
    color.z = grayval;

    float tres = (1 - angle*angle);

    float tt = .25;

    color.xyz = smoothstep(
	    tt * tres, tt + (1-tt) * (1-tres),
	grayval
	    );
//    color.x = grayval;
//    color.x = !isfinite(angle);
//    color.y = !isfinite(tdx);
//    color.z = !isfinite(tdy);

    color.w = (grayval < 1);
    color.w = 1;

}


""", "fp30")),

GL.createProgram(cg.compile("""
void main(
	float2 t : TEXCOORD0,
	uniform float2 meas: register(c0),
	uniform sampler2D t0: TEXUNIT0,
	out half4 color: COLOR) {
//    if(tex2D(t0, t).x > 0) discard;
    half4 ders;
    ders.xy = ddx(t);
    ders.zw = ddy(t);

    half2 dx = ders.xy;
    half2 dy = ders.zw;
    
    half4 dersq = ders * ders;

    half2 dersums = dersq.xz + dersq.yw;
    half l = max(dersums.x, dersums.y);
    l = sqrt(l);
    // l = max(length(ddx(t)), length(ddy(t)))

    // * 512 = texture width
    // / 2 = half, for radius
    // / 2 = half, for x-sampling pattern
    half4 maxrad = l * 512 / 2 / 2 * meas.y;

    half4 c0 = tex2D(t0, t + meas.x*(dx+dy));


    half4 c1 = tex2D(t0, t + meas.x*(dx-dy));
    half4 c2 = tex2D(t0, t + meas.x*(-dx+dy));
    half4 c3 = tex2D(t0, t + meas.x*(-dx-dy));

    half4 dists = 256 * 16 * half4(
	c0.x,
	c1.x,
	c2.x,
	c3.x);




    fixed c = dot(1-smoothstep(-maxrad, maxrad, dists), fixed4(1,1,1,1)) / 4;

    fixed rgb = (1 - c);

    color.xyz = rgb;
//    color.y = c0.x - maxrad * 1000;
 //   color.z = c0.w + maxrad * 1000;
    color.w = 1;

}


""", "fp30")),
GL.createProgram(cg.compile("""
void main(
	float2 t : TEXCOORD0,
	uniform float2 meas: register(c0),
	uniform sampler2D t0: TEXUNIT0,
	out half4 color: COLOR) {
    float4 tx = tex2D(t0,t);
    float x = tx.x * 256 * 16;
    float tex = 5;
    if(x > tex)
	color = float4(1,0,1,0);
    else if(x > 0)
	color = float4(0,1,0,1);
    else if(x > -tex)
	color = float4(0,0,1,1);
    else
	color = float4(1,0,0,1);
}


""", "fp30")),
GL.createProgram("""!!FP1.0
DECLARE meas;
MOV R10, f[TEX0].xyzw;
DDX R8, R10;
DDY R9, R10;

DP4 R6, R8.xyxy, R8.xyxy;
DP4 R7, R9.xyxy, R9.xyxy;
MAX R6, R6, R7;
MUL R6, R6, .5;
RSQ R6, R6.x;
RCP R6, R6.x;
MUL R6, R6, 512;

# R6 = texels per pixel


#TEX R0, R10, TEX0, 2D;
#MUL R0, R0, 16;
# MAD R11, 
# MAD R0, R0.w, 256, R0.x;
# MAD R0, R0.x, 256, R0.w;

# Sample in an X pattern.

MOV R15, p[0].x;

MAD R11, R15, R8, R10;
MAD R11, R15, R9, R11;
TEX R12, R11, TEX0, 2D;
MAD R1.x, R12.w, 256, R12.x;

MAD R11, -R15, R8, R10;
MAD R11, -R15, R9, R11;
TEX R12, R11, TEX0, 2D;
MAD R1.y, R12.w, 256, R12.x;

MAD R11, R15, R8, R10;
MAD R11, -R15, R9, R11;
TEX R12, R11, TEX0, 2D;
MAD R1.z, R12.w, 256, R12.x;

MAD R11, -R15, R8, R10;
MAD R11, R15, R9, R11;
TEX R12, R11, TEX0, 2D;
MAD R1.w, R12.w, 256, R12.x;

# Scale up by 256 and down by 16
MUL R1, R1, 16;

# Now, R1 contains the 4 texel lengths
# of the edges from the 4 points.

# Scale

# Calculate half the width - i.e. the radius
MUL R6, R6, .5;

# Further scale down by half for the X sampling pattern
MUL R6, R6, .5;

# Arbitrary scale
MUL R6, R6, p[0].y;

SLT R0, R1, R6;

# Then, what to do with this?
DP4 R0, R0, 1;
MUL R0, R0, .25;

# SLE R0, R1, 0;

# MUL R6, R6, .5;
# 
# SLT R0.x, R1, R6;
# 
# MUL R6, R6, .5;
# 
# SLT R0.y, R1, R6;
# 
# MUL R6, R6, .5;
# 
# SLT R0.z, R1, R6;

SUB R0.xyz, 1, R0.w;

MOV o[COLR], R0;

END"""),
GL.createProgram("""!!FP1.0
MOV R10, f[TEX0].xyzw;
DDX R8, R10;
DDY R9, R10;
MUL R18, R8, 1;
MUL R19, R9, 1;

TXD R0, R10, R18, R19, TEX0, 2D;
ADD R11, R10, R18;
TXD R1, R11, R18, R19, TEX0, 2D;
# ADD R11, R11, R18;
# TXD R3, R11, R18, R19, TEX0, 2D;
SUB R11, R10, R18;
TXD R2, R11, R18, R19, TEX0, 2D;
# SUB R11, R11, R18;
# TXD R4, R11, R18, R19, TEX0, 2D;

ADD R11, R10, R19;
TXD R5, R11, R18, R19, TEX0, 2D;
SUB R11, R10, R19;
TXD R6, R11, R18, R19, TEX0, 2D;

# Now we have 3 horizontal samples
# in R2, R0, R1
# and 3 vertical in R6, R0, R5.
# Set the pixel if it's maximum in one
# direction

MAX R7.x, R2.w, R1.w;
MAX R7.y, R5.w, R6.w;

SUB R7, R0.w, R7;

RCP R1.w, R0.w;
MUL R7.xy, R7, R1.w;

# MAX R7.w, R7.x, R7.y;
MAX R7.w, R7.x, R7.x;

# ADDC_SAT R7.w, R7.w, .1;
MOVC_SAT R7.w, R7.w;
MOV R7(GT), 1;

# SGEC R7.x, R0.w, R2.w;
# SGE R7.x(NE), R0.w, R1.w;
# SGEC R7.y, R0.w, R5.w;
# SGE R7.y(NE), R0.w, R6.w;
# ADD R7.w, R7.x, R7.y;

MUL R0, R7.w, R0;
SUB R0.xyz, 1, R0.w;
# MOV R0.xy, R7;
MOV R0.w, 1;
MOV o[COLR], R0;

END
"""),
GL.createProgram("""!!FP1.0

MOV R10, f[TEX0].xyzw;
DDX R8, R10;
DDY R9, R10;

MUL R18, R8, .5;
MUL R19, R9, .5;

TXD R0, R10, R18, R19, TEX0, 2D;
ADD R11, R10, R8;
TXD R1, R11, R18, R19, TEX0, 2D;
ADD R11, R10, -R8;
TXD R2, R11, R18, R19, TEX0, 2D;
ADD R11, R10, R9;
TXD R3, R11, R18, R19, TEX0, 2D;
ADD R11, R10, -R9;
TXD R4, R11, R18, R19, TEX0, 2D;

ADD R5, R0, R1;
ADD R5, R5, R2;
ADD R5, R5, R3; 
ADD R5, R5, R4; 
MUL R5, R5, .2;

MUL R6, R0, R0;
MAD R6, R1, R1, R6;
MAD R6, R2, R2, R6;
MAD R6, R3, R3, R6;
MAD R6, R4, R4, R6;
MUL R6, R6, .2;

MOV R5.x, R5.w;
MOV R5.y, R0.w;

TEX R0, R5, TEX3, 2D;

#SGT R0.w, R0.w, R5.w;

#SUB R0.xyz, 1, R0.w;
MOV R0.w, 1;
MOV o[COLR], R0;

END
"""),
GL.createProgram("""!!FP1.0

MOV R10, f[TEX0].xyzw;
DDX R8, R10;
MUL R8, .3333333333, R8;
DDY R9, R10;

MAD R10, -3, R8, R10;

TXD R0, R10, R8, R9, TEX0, 2D;
ADD R10, R8, R10;
TXD R1, R10, R8, R9, TEX0, 2D;
ADD R10, R8, R10;
TXD R2, R10, R8, R9, TEX0, 2D;
ADD R10, R8, R10;
TXD R3, R10, R8, R9, TEX0, 2D;
ADD R10, R8, R10;
TXD R4, R10, R8, R9, TEX0, 2D;
ADD R10, R8, R10;
TXD R5, R10, R8, R9, TEX0, 2D;
ADD R10, R8, R10;
TXD R6, R10, R8, R9, TEX0, 2D;

MUL R0.x, 0.1111, R0.w;
MAD R0.x, 0.2222, R1.w, R0.x;
MAD R0.x, 0.3333, R2.w, R0.x;
MAD R0.x, 0.2222, R3.w, R0.x;
MAD R0.x, 0.1111, R4.w, R0.x;

MUL R0.y, 0.1111, R1.w;
MAD R0.y, 0.2222, R2.w, R0.y;
MAD R0.y, 0.3333, R3.w, R0.y;
MAD R0.y, 0.2222, R4.w, R0.y;
MAD R0.y, 0.1111, R5.w, R0.y;

MUL R0.z, 0.1111, R2.w;
MAD R0.z, 0.2222, R3.w, R0.z;
MAD R0.z, 0.3333, R4.w, R0.z;
MAD R0.z, 0.2222, R5.w, R0.z;
MAD R0.z, 0.1111, R6.w, R0.z;

DP3 R0.w, R0, 1;
SUB R0.xyz, 1, R0;

MOV o[COLR], R0;
END
"""),
GL.createProgram("""!!FP1.0

MOV R10, f[TEX0].xyzw;
TEX R0, f[TEX0].xyzw, TEX0, 2D;
#SUB R0, R0, {.2,0,.2,0};
SUB R0.xyz, 1, R0.w;
MOV R0.w, 1;
MOV o[COLR], R0;
END
"""),
]

loremipsum = """Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.

Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.

Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.

Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.

Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis.

At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  At accusam aliquyam diam diam dolore dolores duo eirmod eos erat, et nonumy sed tempor et et invidunt justo labore Stet clita ea et gubergren, kasd magna no rebum. sanctus sea sed takimata ut vero voluptua. est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat. 

Consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.
"""
words = loremipsum.split(" ")

vob.AbstractUpdateManager.dbg = 0
vob.impl.gl.GLUpdateManager.dbg = 0

t = 0
class Scene(vob.util.PS2Reader.Listener):
    def __init__(self):
	self.prognum_dummylist = [ 0 for x in fp ]
    	self.key = KeyPresses(
            self, 
	    SlideLin("x", 100, 10, "x coord", "Left", "Right"),
	    SlideLin("y", 100, 10, "x coord", "Up", "Down"),
	    SlideLin("a", 0, 15, "Rotation", "Prior", "Next"),
	    ListIndex("prognum", "prognum_dummylist", 0, "prog type", "T", "t"),
	    Action("Create dist texture", "d", self.makeDist),
	    )
	self.mouser = vob.putil.demowindow.mouser
	self.mouser.setListener(self)
	self.ext = 0
	self.mouser.start()

    def makeDist(self, foo, foo2):
	vs = vob.putil.demowindow.w.createVobScene()
	vs.put(background((0,0,0)))

	
	vs.put(getDListNocoords("""
	    BlendFunc ONE ONE
	    BlendEquation MAX
	    Enable ALPHA_TEST
	    AlphaFunc GREATER 0.99
	"""))
	#tx = vob.vobs.TextVob(vob.GraphicsAPI.getInstance().getTextStyle(
	#		"serif", 1, 64),
	#		    "sofesjif@342oFAEOFJ", 0, None)
	for x in range(-10,11):
	    for y in range(-10,11):
		d = 1 - math.hypot(x,y) / 20
		print d
		vs.put(getDListNocoords("""
		    Color %s %s %s 1
		""" % (d, d, d)))
		cs = vs.orthoCS(0,"", 0, x+40, y+40, 20, 20)
		vs.put(tx, cs)
	vob.putil.demowindow.w.renderStill(vs, 0)

    def scene(self, vs):
	if hasattr(self, "vs") and self.ext:
	    self.ext = 0
	    self.setCS()
	    AbstractUpdateManager.setNoAnimation()
	    AbstractUpdateManager.chg()
	    return self.vs

	vs.put( background((1,1,1)))

	fpid =	fp[self.prognum].getProgId()

        print fpid

        vs.put( getDListNocoords("""
        PushAttrib ENABLE_BIT CURRENT_BIT COLOR_BUFFER_BIT TEXTURE_BIT 

        BindProgram FRAGMENT_PROGRAM_ARB %s
        Enable FRAGMENT_PROGRAM_ARB
        Enable ALPHA_TEST
        AlphaFunc GREATER 0
        Enable BLEND
        BlendFunc ONE ONE
        BlendEquation MIN

        ActiveTexture TEXTURE3
        BindTexture TEXTURE_2D %s

        TexImage2D TEXTURE_2D 0 RGBA 4 4 0 RGBA \
        .1 .2 .3 0  0 0 1 0  0 1 0 0  0 1 1 0 \
        1 0 0 0  1 0 1 0  1 1 0 0  1 1 1 0 \
        0 0 0 0  0 0 .5 0  0 .5 0 0  0 .5 .5 0 \
        .5 0 0 0  .5 0 .5 0  .5 .5 0 0  .5 .5 .5 0 
        
        TexParameter TEXTURE_2D TEXTURE_MIN_FILTER NEAREST
        TexParameter TEXTURE_2D TEXTURE_MAG_FILTER NEAREST
        ActiveTexture TEXTURE0
        """ % (fpid, tex.getTexId())))

        cs = vs.orthoCS(0, "a", 0, self.x, self.y, 1, 1)

	print "x,y:", self.mouser.x, self.mouser.y
        parcs = vs.orthoCS(0, "para", 0, 0, 0, 0, 0)


	vs.put( GLRen.createProgramNamedParameterNV(fpid, "meas"),
	    parcs)

	cs = vs.rotateCS(cs, "b", self.a)

	if self.prognum < 2:
	    f = font
	else: 
	    f = font2
	tx = vob.gl.GLRen.createText1(f, "sofesjif@32FEF", 0, 0)

# Good mouse param values e.g. approx 28,40 or 30,43.

	y = 0
	for i in range(7,50,2):
	    y += i*1.5
	    csi = vs.orthoCS(cs, str(i), 0, 0, y, 2*i, 2*i)
            vs.put(tx, csi)
        
        vs.put( getDListNocoords("PopAttrib"))

	self.vs = vs
	self.parcs = parcs
	self.setCS()


    def setCS(self):
	self.vs.coords.setOrthoParams(self.parcs, 0,
	    self.mouser.x / 100.0, self.mouser.y / 100.0, self.mouser.z / 100.0, 1)

    def chg(self, *args):
	print "chg ",args
	self.ext = 1
	AbstractUpdateManager.setNoAnimation()
	AbstractUpdateManager.chg()

