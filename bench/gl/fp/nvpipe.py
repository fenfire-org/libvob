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


from types import *
import java
import vob
GL = vob.gl.GL

# inds = [4,5,6,7,8,9]
inds = None

fpp = [
(vob.putil.nvcode.parseCombiner("""
SPARE0 = TEX0

color = SPARE0
"""),0
),
"""!!FP1.0
# COLH = const
MOV o[COLH], {1,0,0,0};
END
""",
"""!!FP1.0
# COLH = f[TEX0]
MOV o[COLH], f[TEX0];
END
""",
"""!!FP1.0
# H = texcoord; H = TEX(H); COLR = H
MOVX H0, f[TEX0].xyzw;
TEX H0, H0, TEX0, 2D;
MOVX o[COLR], H0;
END
""",
"""!!FP1.0
# H = TEX; COLH = H
TEX H0, f[TEX0].xyzw, TEX0, 2D;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H0, h1 = TEX; COLR = (x) H0+h1
TEX H0, f[TEX0].xyzw, TEX0, 2D;
TEX H1, f[TEX1].xyzw, TEX0, 2D;
ADDX  o[COLH], H0, H1;
END
""",
"""!!FP1.0
# H0, h1 = TEX; COLR = (R) H0+h1
TEX H0, f[TEX0].xyzw, TEX0, 2D;
TEX H1, f[TEX1].xyzw, TEX0, 2D;
ADDR  o[COLH], H0, H1;
END
""",
"""!!FP1.0
# H0, h1 = TEX; COLR = (R) H0+h1
TEX H0, f[TEX0].xyzw, TEX0, 2D;
TEX H1, f[TEX1].xyzw, TEX0, 2D;
ADDR  o[COLH], H0, H1;
END
""",
"""!!FP1.0
# H0 = tex, h1 = TEX(h0); COLh = (h) H1
TEX H0, f[TEX0].xyzw, TEX0, 2D;
TEX H1, H0, TEX0, 2D;
MOV  o[COLH], H1;
END
""",
"""!!FP1.0
# H0 = f[TEX0] ; COLH = H0
MOV H0, f[TEX0];
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H0 = f[TEX0] ; ADDX H0; ; COLH = H0
MOV H0, f[TEX0];
ADDX H0, H0, H0;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H0 = f[TEX0] ; 2*ADDX H0; ; COLH = H0
MOV H0, f[TEX0];
ADDX H0, H0, H0;
ADDX H0, H0, H0;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H0 = f[TEX0] ; 3*ADDX H0; ; COLH = H0
MOV H0, f[TEX0];
ADDX H0, H0, H0;
ADDX H0, H0, H0;
ADDX H0, H0, H0;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H0 = f[TEX0] ; 4*ADDX H0; ; COLH = H0
MOV H0, f[TEX0];
ADDX H0, H0, H0;
ADDX H0, H0, H0;
ADDX H0, H0, H0;
ADDX H0, H0, H0;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H0 = f[TEX0] ; 4*ADDX H0, 1H1; ; COLH = H0
MOV H0, f[TEX0];
MOV H1, f[TEX0];
ADDX H0, H0, H0;
ADDX H0, H0, H0;
ADDX H0, H0, H0;
ADDX H0, H0, H1;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H0 = f[TEX0] ; 4*ADDX H0 par H1; ; COLH = H0
MOV H0, f[TEX0];
MOV H1, f[TEX0];
ADDX H0, H0, H0;
ADDX H1, H1, H1;
ADDX H0, H0, H0;
ADDX H1, H1, H1;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H0 = f[TEX0] ; 4*ADD H0 par H1; ; COLH = H0
MOV H0, f[TEX0];
MOV H1, f[TEX0];
ADD H0, H0, H0;
ADD H1, H1, H1;
ADD H0, H0, H0;
ADD H1, H1, H1;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H0 = f[TEX0] ; 4*ADD H0 par H1; ; COLH = H0
MOV H0, f[TEX0];
MOV H1, f[TEX0];
ADD H0, H0, H0;
ADD H1, H1, H1;
ADD H0, H0, H0;
ADD H1, H1, H0;
MOV o[COLH], H1;
END
""",
"""!!FP1.0
# H0 = f[TEX0] ; ADD H0;  COLH = H0
MOV H0, f[TEX0];
ADD H0, H0, H0;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H = TEX; COLR = H
TEX H0, f[TEX0].xyzw, TEX0, 2D;
MOV o[COLR], H0;
END
""",
"""!!FP1.0
# H = TEX; COLR = H
TEX H0, f[TEX0].xyzw, TEX0, 2D;
MOV o[COLR], H0;
END
""",
"""!!FP1.0
# R = TEX; COLR = R
TEX R0, f[TEX0].xyzw, TEX0, 2D;
MOV o[COLR], R0;
END
""",
"""!!FP1.0
# H = TEX; COLH = H
TEX H0, f[TEX0].xyzw, TEX0, 2D;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H = TEX; addx; COLH = H
TEX H0, f[TEX0].xyzw, TEX0, 2D;
ADDX H0, H0, H0;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H = TEX; addh; COLH = H
TEX H0, f[TEX0].xyzw, TEX0, 2D;
ADDH H0, H0, H0;
MOV o[COLH], H0;
END
""",
"""!!FP1.0
# H = TEX; addr; COLH = H
TEX H0, f[TEX0].xyzw, TEX0, 2D;
ADDR H0, H0, H0;
MOV o[COLH], H0;
END
""",
]

if inds == None:
    inds = range(0, len(fpp))

fpp = [fpp[i] for i in range(0,len(fpp)) if i in inds]

def mkprog(p):
    if type(p) == TupleType:
	return p
    return GL.createProgram(p)

fp = [mkprog(p) for p in fpp]

def benchScene(vs,
	prog = 0,
	nquads = 10,
	):
    size = vs.getSize();
    vs.map.put(vob.vobs.SolidBackdropVob(java.awt.Color.green))

    cs = vs.orthoCS(0, "a", 0, 0, 0, size.width, size.height)
    print "WH: ",size

    qua = vob.putil.misc.quad()

    vs.put(vob.putil.misc.getDListNocoords("""
        PushAttrib ENABLE_BIT CURRENT_BIT COLOR_BUFFER_BIT TEXTURE_BIT 
	Disable ALPHA_TEST
	Disable BLEND
	Disable DEPTH_TEST
        TexParameter TEXTURE_2D TEXTURE_MIN_FILTER NEAREST
	TexParameter TEXTURE_2D  GENERATE_MIPMAP_SGIS TRUE

        TexImage2D TEXTURE_2D 0 RGBA 4 4 0 RGBA \
        .1 .2 .3 0  0 0 1 0  0 1 0 0  0 1 1 0 \
        1 0 0 0  1 0 1 0  1 1 0 0  1 1 1 0 \
        0 0 0 0  0 0 .5 0  0 .5 0 0  0 .5 .5 0 \
        .5 0 0 0  .5 0 .5 0  .5 .5 0 0  .5 .5 .5 0 
        TexParameter TEXTURE_2D TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
        TexParameter TEXTURE_2D TEXTURE_MAG_FILTER LINEAR

	ActiveTextureARB TEXTURE1
	TexParameter TEXTURE_2D  GENERATE_MIPMAP_SGIS TRUE
        TexImage2D TEXTURE_2D 0 RGBA 4 4 0 RGBA \
        .1 .2 .3 0  0 0 1 0  0 1 0 0  0 1 1 0 \
        1 0 0 0  1 0 1 0  1 1 0 0  1 1 1 0 \
        0 0 0 0  0 0 .5 0  0 .5 0 0  0 .5 .5 0 \
        .5 0 0 0  .5 0 .5 0  .5 .5 0 0  .5 .5 .5 0 
        TexParameter TEXTURE_2D TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
        TexParameter TEXTURE_2D TEXTURE_MAG_FILTER LINEAR
	ActiveTextureARB TEXTURE0
    """))


    if type(fp[prog]) == TupleType:
	print fp[prog]
	vs.put(vob.putil.misc.getDListNocoords("""
	    Disable FRAGMENT_PROGRAM_ARB
	    Enable REGISTER_COMBINERS_NV
	    %s
	""" % fp[prog][0]))
	ret = fp[prog]
    else:
	vs.put(vob.putil.misc.getDListNocoords("""
	    BindProgram FRAGMENT_PROGRAM_ARB %s
	    Enable FRAGMENT_PROGRAM_ARB
	""" % fp[prog].getProgId()))
	ret = fpp[prog].splitlines()[1]

    for i in range(0,nquads):
	tcs = vs.translateCS(cs, str(i), 0, 0, -1)
	vs.map.put(qua, tcs)

    vs.put(vob.putil.misc.getDListNocoords("""PopAttrib"""))

    return ret
	

args = { 
    "nquads" : (100),
    "prog" : tuple(range(0, len(fp))),
}
