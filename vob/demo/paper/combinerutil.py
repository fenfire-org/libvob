# 
# Copyright (c) 2003, Tuomas J. Lukka and Janne Kujala
# 
# This file is part of Gzz.
# 
# Gzz is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Gzz is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Gzz; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 

from __future__ import nested_scopes
from vob.putil import nvcode
from vob.putil.misc import *
from vob.putil.demokeys import *

def shootImages():
    pt = PaperTemplate()
    def frame(fileprefix, t0, c, t1):
	vs = w.createVobScene()
	vs.map.put(background((0.7, 0.8, 0.6)))
	pt.place(vs, vs.orthoCS(0, "A", 0, 10, 10, 650, 650), t0, c, t1)
	w.renderStill(vs, 0)
	saveanim.saveframe("shots/%s.png" % fileprefix, w)

    frame("texcomb_a", "RGB0", "RGB0", "NONE")
    frame("texcomb_b", "NONE", "RGB1", "RGB1")
    frame("texcomb_c", "RGB0", "BAND0", "RGB1")
    frame("texcomb_d", "RGB0", "BAND1", "RGB1")



from org.nongnu.libvob.gl import GL, GLRen, Paper, PaperMill

import java
from java.lang import Math

from vob.color.spaces import *

import vob.paper.textures
vob.paper.textures.init(4, "RGBA")

#from vob.paper.textures import *
from vob.paper.texcoords import *
from vob.paper.colors import *

passmask = [ 1, 0, 0, 0 ]

texcodesList = [
(    "NONE" ,"""
    alpha = 0
    """),
(    "RGB0" ,"""
    color = TEX0
    alpha = 1
    """),
(    "RGB1" ,"""
    color = TEX1
    alpha = 1
    """),
(    "DOT" ,"""
    # Dot product between TEX0 and a constant
    SPARE0 = (2*TEX0-1).(2*CONST0-1)
    color = SPARE0

    """),
(    "BAND0" ,"""
	
	# Dot product between the two texture values
	SPARE0 = ((2*TEX0-1) . (2*TEX1-1)) * 2

	SPARE0 = (+SPARE0) + (.5)

	color = SPARE0
	alpha = 1
    """),
(    "BAND1" ,"""
	# Dot product between the two texture values
	SPARE0 = ((2*TEX0-1) . (2*TEX1-1)) * 4

	# Square to select a narrow band
	SPARE0 = ((+SPARE0)*(+SPARE0)) * 2

	color = SPARE0
	alpha = 1
    """),
(    "BAND1_X" ,"""
	# Dot product between the two texture values
	SPARE0 = ((2*TEX0-1) . (2*TEX1-1)) * 4

	# Square to select a narrow band
	SPARE0 = ((+SPARE0)*(+SPARE0)) * 2

	# Interpolate between the color values
	color = SPARE0*COL0 + (1-SPARE0)*COL1
	alpha = 1
    """),

(    "BAND3" ,"""
	# Same as before, now modulate the
	# dot product with some values 
	# depending on a dot product of TEX0.
	SPARE0 = ((2*TEX0-1) . (2*TEX1-1)) * 4
	SPARE1 = ((2*TEX0-1).(2*CONST0-1)) * 4
	SPARE1.a = ((TEX1.blue)*(TEX0.blue)) * 4

	SPARE0 = ((+SPARE0)*(+SPARE0)) * 2
	SPARE1 = ((SPARE1) * (SPARE1.a)) * 2

	EF = SPARE0 * SPARE1
	color = EF*COL0 + (1-EF)*COL1
	alpha = 1
    """),
]

texcodes = { }
parsedTexcodes = { }
for e in texcodesList: 
    code = "# "+e[0]+"\n"+e[1]
    texcodes[e[0]] = code
    parsedTexcodes[e[0]] = nvcode.parseCombiner(code)



def getpaper(vecs, cols, x0, y0, x1, y1, t0, t1, tex0comb, isectcomb, tex1comb):
    pap = Paper()
    pap.setNPasses(1)
    ppass = pap.getPass(0)
    ppass.setSetupcode("""
    	    PushAttrib ENABLE_BIT TEXTURE_BIT DEPTH_BUFFER_BIT
    """)
    ppass.setTeardowncode("""
	    PopAttrib
            ActiveTexture TEXTURE0
    """)

    # texid = vob.paper.textures.ptextures["RGB2"][1].getTexId();
    texid0 = vob.paper.textures.getNamed("RGB2", t0).getTexId()
    texid1 = vob.paper.textures.getNamed("RGB2", t1).getTexId()
    
    constcode = """
        ActiveTexture TEXTURE0
        Enable TEXTURE_2D
        BindTexture TEXTURE_2D %(texid0)s
        TexParameter TEXTURE_2D TEXTURE_WRAP_S REPEAT
        TexParameter TEXTURE_2D TEXTURE_WRAP_T REPEAT
        TexParameter TEXTURE_2D TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
        TexParameter TEXTURE_2D TEXTURE_MAG_FILTER LINEAR
        
        ActiveTexture TEXTURE1
        Enable TEXTURE_2D
        BindTexture TEXTURE_2D %(texid1)s
        TexParameter TEXTURE_2D TEXTURE_WRAP_S REPEAT
        TexParameter TEXTURE_2D TEXTURE_WRAP_T REPEAT
        TexParameter TEXTURE_2D TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
        TexParameter TEXTURE_2D TEXTURE_MAG_FILTER LINEAR
        
        Enable BLEND
        BlendFunc SRC_ALPHA ONE_MINUS_SRC_ALPHA 
        BlendEquation FUNC_ADD
        Disable ALPHA_TEST

        Enable REGISTER_COMBINERS_NV
    """ % locals()

    r0,r1 = vecs
    c0,c1,c2,c3 = cols
    constcode += """
        Color %(c0)s
        SecondaryColorEXT %(c1)s
        Fog FOG_COLOR %(c2)s
        CombinerParameterNV CONSTANT_COLOR0_NV %(r0)s
        CombinerParameterNV CONSTANT_COLOR1_NV %(r1)s
    """ % locals()

    return GLRen.createBasisPaperQuad(pap, 0, 0, x0, y0,
				0, 0, x1, y1,
                                      GL.createDisplayList(constcode + parsedTexcodes[tex0comb]),
                                      GL.createDisplayList(constcode + parsedTexcodes[tex1comb]),
                                      GL.createDisplayList(constcode + parsedTexcodes[isectcomb]))

rng = java.util.Random()

class PaperTemplate:
    def __init__(self):
        self.vecseed0 = 0
        self.vecseed1 = 1
        self.colseed = 2

        self.initvecs()
        self.initcols()

        self.x0, self.y0 = .12, .02
        self.x1, self.y1 = 0, 0
        self.texgen0 = TexGenXYRepeatUnit(vecs = [[.5,0], [0,.5]])
        self.texgen1 = TexGenXYRepeatUnit(vecs = [[.25,.5], [.25,0]])

        self.frame = getDList("""
	Color 0 0 0
	Disable TEXTURE_2D
        Disable DEPTH_TEST
	LineWidth 2
	Begin LINE_LOOP
	Vertex 0 0
	Vertex 0 1
	Vertex 1 1
	Vertex 1 0
	End
        """)

	self.keys = [
Action("Select random new colors", "c", lambda *args: self.randColors()),
Action("Select random dot-product vector 1", "1", lambda *args: self.randvec(0)),
Action("Select random dot-product vector 2", "2",  lambda *args: self.randvec(1)),
Action("Move", "Left",  lambda *args: self.move(-1,0)),
Action("Move", "Right",  lambda *args: self.move(1,0)),
Action("Move", "Up",  lambda *args: self.move(0,-1)),
Action("Move", "Down",  lambda *args: self.move(0,1)),
	]

    def move(self, dx, dy):
	self.x0 += dx * .01
	self.y0 += dy * .01
    def initvecs(self):
        colors = Colors(self.vecseed0)
        r0 = colors.getNVDP3VecStr(0)
        colors = Colors(self.vecseed1)
        r1 = colors.getNVDP3VecStr(1)
        self.vecs = [ r0, r1 ]

    def initcols(self):
        rnd = java.util.Random(self.colseed)
        colors = Colors(rnd.nextInt())
        colorbase = rnd.nextInt()
	self.cols = [ colors.getColorStr(colorbase+i)
                      for i in range(0,4) ]

    def randColors(self):
	self.colseed = rng.nextInt(2000000000)
	self.initcols()
	
    def randVec(self, ind):
	if ind == 0:
            self.vecseed0 = rng.nextInt(2000000000)
	else:
            self.vecseed1 = rng.nextInt(2000000000)
	self.initvecs()
	return

	# XXX
        if k == "F4":
            self.texgen0 = TexGenXYRepeatUnit(rnd=rng)
        elif k == "F5":
            self.texgen1 = TexGenXYRepeatUnit(rnd=rng)

    def place(self, vs, into, t1, c, t2):
	print self.x0,self.y0,self.x1,self.y1
	sca = 1/3.0
	x0s = 1 / sca
	y0s = 1 / sca
	x1s = 1 / sca
	y1s = 2 / sca
        pq = getpaper(self.vecs, self.cols, 
	    x0s, y0s, x1s, y1s,
	    "cone", "triangle",
	    t1, c, t2)


        # cs1 and cs2 specify the mapping from the texture coords
        # [0,1] x [0,1] to vertex coords, cs1 is for the first texture unit
        # and cs2 for the second

	sca = 2 * sca

	cs1 = vs.coords.affine(into, 10, self.x0, self.y0,
                                       self.texgen0.vecs[0][0]*sca,
                                       self.texgen0.vecs[1][0]*sca,
                                       self.texgen0.vecs[0][1]*sca,
                                       self.texgen0.vecs[1][1]*sca)
	vs.matcher.add(into, cs1, "1")
	cs2 = vs.coords.affine(into, 10, self.x1, self.y1,
                                       self.texgen1.vecs[0][0]*sca,
                                       self.texgen1.vecs[1][0]*sca,
                                       self.texgen1.vecs[0][1]*sca,
                                       self.texgen1.vecs[1][1]*sca)
	vs.matcher.add(into, cs2, "2")

	vs.map.put(pq, cs1, cs2)
	vs.map.put(self.frame, vs.scaleCS(cs1, "s", x0s, y0s))
	vs.map.put(self.frame, vs.scaleCS(cs2, "s", x1s, y1s))

