# 
# Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
# 
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
# 

from __future__ import nested_scopes
from org.nongnu import libvob as vob
from org.nongnu.libvob.gl import GL, GLRen, Paper, GLCache, IrregularFrame
import java 
import math
from vob.putil.nvcode import parseCombiner
from vob.paper.texcache import getCachedTexture

dbg = 0

class IrreguFrame(IrregularFrame):
    dicefactor = .4
    # dicefactor = 0.03

    if dbg: print "Start Irregu shading..."

    tex = GL.createTexture()
    tex = getCachedTexture([128, 128, 0, 1, "INTENSITY", "LUMINANCE",
	      "sawnoise", ["bias", "0.5",
			   "scale", "0.15", "freq", "1", "df", "2", 
			   "scale2", "0.25", "freq2", "10", "df2", ".5"]])
    #                       "scale", "0.2", "freq", "1", "df", "2", 
    #                       "scale2", "0.05", "freq2", "12", "df2", "1.5"])

    if dbg: print "part 1 done"
    ripple_scale = 0.25
    tex2 = getCachedTexture([256, 256, 0, 4, "RGBA", "RGBA",
                             "irregu", ["radius", "2",
                                        "ripple_scale", str(ripple_scale),
                                        "angle", "0",
                                        "angles", "3",
                                        "eps", ".250",
                                        "scaling", "const"]],
                            shade_all_levels = 1)
    tex3 = getCachedTexture([256, 256, 0, 1, "INTENSITY", "LUMINANCE",
                             "irregu", ["ripple_scale", str(ripple_scale),
                                        ]],
                            shade_all_levels = 1)
    if dbg: print "done"

    boxtex = GL.createTexture()

    combiners = "Enable"

    def __init__(self, x0, y0, x1, y1, border, ripple,
                 typeInt=0,
                 contentColor=java.awt.Color.white,
                 frameColor=java.awt.Color.black,
                 type = "square"):

        self.dbg = 0
        if typeInt == 1:
            type = 'square'
        elif typeInt == 2:
            type = 'ellipse'

        
        if self.dbg:
            print "Texture id:", self.tex.getTexId()

	def code(color):
	    return """
                PushAttrib ENABLE_BIT TEXTURE_BIT CURRENT_BIT
                Enable ALPHA_TEST
                AlphaFunc GREATER 0.0
		Disable BLEND

		Color %(color)s

                ActiveTexture TEXTURE1
                BindTexture TEXTURE_2D %(boxtex)s
                Enable TEXTURE_2D
                TexImage2D TEXTURE_2D 0 ALPHA 4 4 0 ALPHA 0 0 0 0 0 1 1 0 0 1 1 0 0 0 0 0
                TexParameter TEXTURE_2D TEXTURE_BASE_LEVEL 0
                TexParameter TEXTURE_2D TEXTURE_MAX_LEVEL 0
		TexParameter TEXTURE_2D TEXTURE_WRAP_S CLAMP
		TexParameter TEXTURE_2D TEXTURE_WRAP_T CLAMP
		TexParameter TEXTURE_2D TEXTURE_MIN_FILTER NEAREST
		TexParameter TEXTURE_2D TEXTURE_MAG_FILTER NEAREST

                #TexGen S TEXTURE_GEN_MODE EYE_LINEAR
                #Enable TEXTURE_GEN_S
                #TexGen T TEXTURE_GEN_MODE EYE_LINEAR
                #Enable TEXTURE_GEN_T

                ActiveTexture TEXTURE0

                BindTexture TEXTURE_2D %(tex)s
                Enable TEXTURE_2D
                %(comb)s REGISTER_COMBINERS_NV
                CombinerParameterNV NUM_GENERAL_COMBINERS_NV 1
                CombinerInputNV COMBINER0_NV ALPHA VARIABLE_A_NV TEXTURE1 UNSIGNED_IDENTITY_NV ALPHA
                CombinerInputNV COMBINER0_NV ALPHA VARIABLE_B_NV TEXTURE0 SIGNED_NEGATE_NV ALPHA
                CombinerInputNV COMBINER0_NV ALPHA VARIABLE_C_NV TEXTURE1 UNSIGNED_IDENTITY_NV ALPHA
                CombinerInputNV COMBINER0_NV ALPHA VARIABLE_D_NV SECONDARY_COLOR_NV UNSIGNED_IDENTITY_NV BLUE
                CombinerOutputNV COMBINER0_NV ALPHA DISCARD_NV DISCARD_NV SPARE0_NV NONE NONE FALSE FALSE FALSE

                FinalCombinerInputNV VARIABLE_A_NV ZERO UNSIGNED_IDENTITY_NV RGB
                FinalCombinerInputNV VARIABLE_B_NV ZERO UNSIGNED_IDENTITY_NV RGB
                FinalCombinerInputNV VARIABLE_C_NV ZERO UNSIGNED_IDENTITY_NV RGB
                FinalCombinerInputNV VARIABLE_D_NV PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
                FinalCombinerInputNV VARIABLE_G_NV SPARE0_NV UNSIGNED_IDENTITY_NV ALPHA
        """ % {"boxtex" : self.boxtex.getTexId(), 
		"tex" : self.tex.getTexId(), 
		"comb" : self.combiners,
		"color" : vob.util.ColorUtil.colorGLString(color)}

        def code2(color):
            return parseCombiner("""
                PushAttrib ENABLE_BIT TEXTURE_BIT COLOR_BUFFER_BIT
                CombinerParameterNV CONSTANT_COLOR0_NV %(color)s 1

                Enable REGISTER_COMBINERS_NV

                SPARE0 = TEX0 . COL0
                SPARE0.alpha = TEX0.alpha * COL0.alpha + COL1.blue

                SPARE0.alpha = SPARE0.blue + SPARE0.alpha

                alpha = SPARE0.alpha
                color = CONST0

                BindTexture TEXTURE_2D %(tex)s
                TexParameter TEXTURE_2D TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
                TexParameter TEXTURE_2D TEXTURE_MAG_FILTER LINEAR
                Enable TEXTURE_2D

                Enable ALPHA_TEST
                AlphaFunc GEQUAL 1.0

                Color 0 0 0 1
        """) % {"tex" : self.tex2.getTexId(),
                "color" : vob.util.ColorUtil.colorGLString(color)}

        def code3(color):
            return parseCombiner("""
                PushAttrib ENABLE_BIT TEXTURE_BIT CURRENT_BIT COLOR_BUFFER_BIT

                BindTexture TEXTURE_2D %(tex)s
                TexParameter TEXTURE_2D TEXTURE_MIN_FILTER LINEAR_MIPMAP_LINEAR
                TexParameter TEXTURE_2D TEXTURE_MAG_FILTER LINEAR
                Enable TEXTURE_2D
                TexEnv TEXTURE_ENV TEXTURE_ENV_MODE ADD

                Enable ALPHA_TEST
                AlphaFunc GEQUAL 1.0

        """) % {"tex" : self.tex3.getTexId(),
                "color" : vob.util.ColorUtil.colorGLString(color)}
        
        
        if type == "square":
            self._content = GLRen.createIrregularQuad(
		x0, y0, x1, y1, border, ripple, 0, 
		code(contentColor), self.dicefactor)
            self._frame = GLRen.createIrregularQuad(
		x0, y0, x1, y1, border, ripple, 1, 
		code(frameColor), self.dicefactor)
        elif type == "ellipse":
            texscale = ripple
            ripple_scale = border / ripple

            ratio = float(ripple_scale) / self.ripple_scale

            if ratio < 3./4 or ratio > 4./3:
                if self.dbg:
                    print "WARNING: anisotropy ratio", round(ratio,2), "is far from one"
            
            # Irregu flags
            Y_COLOR       = 1;
            Y_SECCOLOR    = 2;
            DOTVEC_COLOR  = 4;
            INTERP_DOTVEC = 8;
            SLICE_1D      = 16;
            SLICE_2D      = 32;
            SHIFTS        = 64;
            INSIDE        = 128;
            SHIFTS8       = 256;

            if GL.hasExtension("GL_NV_register_combiners"):
                self._content = GLRen.createIrregularEdge(
                    8, texscale, 2.0, 128, 0,
                    -1 * ripple_scale * texscale,
                    0 * ripple_scale * texscale,
                    0, "1 1 1 1 0 0 0 0", "", 3, 0, 
                    SLICE_1D + Y_SECCOLOR + INSIDE,
                    code2(contentColor),
                    1.0)
                
                self._frame = GLRen.createIrregularEdge(
                    8, texscale, 2.0, 128, 0,
                    -1 * ripple_scale * texscale,
                    0 * ripple_scale * texscale,
                    0, "1 1 1 1 0 0 0 0", "", 3, 0, 
                    SLICE_1D + Y_SECCOLOR + DOTVEC_COLOR + INTERP_DOTVEC,
                    code2(frameColor),
                    1.0)
            else:
                self._content = GLRen.createIrregularEdge(
                    8, texscale, 2.0, 128, 0,
                    -1 * ripple_scale * texscale,
                    0 * ripple_scale * texscale,
                    0, "1 1 1 1 0 0 0 0", "", 0, 0, 
                    SLICE_1D + Y_COLOR + INSIDE,
                    code3(contentColor),
                    1.0)
                
                self._frame = GLRen.createIrregularEdge(
                    8, texscale, 2.0, 128, 0,
                    -1 * ripple_scale * texscale,
                    0 * ripple_scale * texscale,
                    0, "1 1 1 1 0 0 0 0", "", 0, 0, 
                    SLICE_1D + Y_COLOR + SHIFTS,
                    code3(frameColor) +
                    """
                    BlendFunc ZERO ZERO
                    Enable BLEND
                    """
                    , 1.0)

    def getContent(self): return self._content
    def getFrame(self): return self._frame
    def getBlank(self): return self._content
