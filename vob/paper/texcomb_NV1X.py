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


# Register combiners for NV1x architecture, where there
# are only two of them.

from vob.putil import nvcode

from vob.color.spaces import RGBtoLAB

from org.nongnu.libvob.gl import GL

from math import exp
    
class TransparentCombinerPass:
    def setupCode(self, texinputs, texscales, colors, rnd, trans = 0):
	# 4 colors
	colorbase = rnd.nextInt()
	c0, c1, c2, c3 = [ colors.getColorStr(colorbase+i)
				for i in range(0,4) ]

        #print [round( RGBtoLAB(map(float, rgb.split()))[0] ) for rgb in [c0,c1,c2] ]

	r0, r1, r2, r3 = [ colors.getNVDP3VecStr(colorbase+i)
				for i in range(0,4) ]

        # map alpha dot product a \in [0,1] into clamp(1 - (1-a) * alphascale)
        if trans > 0:
            alphascale = 1 - 1.0/trans
        else:
            alphascale = 0
            
        alphascale = alphascale * (1. / 16)

	assert len(texinputs) != 0
	while len(texinputs) < 4:
	    texinputs = texinputs + texinputs
	t0, t1, t2, t3 = texinputs[0:4]

        #c0, c1, c2 = [ "1 1 1", "1 0 1", "0 1 0"]
        
	constantcode = """
            Enable BLEND
            BlendFunc SRC_ALPHA ONE_MINUS_SRC_ALPHA 
	    BlendEquation FUNC_ADD
            Disable ALPHA_TEST

	    Enable REGISTER_COMBINERS_NV
	    CombinerParameterNV NUM_GENERAL_COMBINERS_NV 2
	    
	    CombinerParameterNV CONSTANT_COLOR0_NV %(r0)s
	    CombinerParameterNV CONSTANT_COLOR1_NV %(r1)s
	    Color %(c0)s
	    SecondaryColorEXT %(c1)s
	    Fog FOG_COLOR %(c2)s
	""" 

        type = rnd.nextInt(3)
        # types: 0=BAND-LIKE, 1=3-COL-LERP, 2=FRACTION-LINE
        
        # Random scaling of (dot) products
        if trans > 0:
            # Try to keep the textures non-fuzzy
            rndscale = exp(.3*abs(rnd.nextGaussian()))
        else:
            rndscale = exp(.5*rnd.nextGaussian())

        def avg(*args):
            sum = 0
            for arg in args: sum += arg
            return sum / float(len(args))
        
	# Then, select the combiner path type.
	if type == 0:
            scale = nvcode.combinerscale(avg(*texscales) * 8.0 * rndscale)
            bandscale = nvcode.combinerscale(3.0 * exp(.5 * rnd.nextGaussian()))

	    # Band-like texture.
	    #
	    # A little different from what Tjl and Jvk originally
	    # planned, where the EF product would have been used;
	    # Sadly, we forgot that E and F are not signed(!).

            # Make outside of the bands transparent if trans > 0
            if trans > 0:
                finalG = "SPARE1_NV"
            else:
                finalG = "ZERO"

	    c = ("""
                # Band-like texture
                
                # SPARE0 <- (TEX0 . TEX1)
		CI0 RGB A TEXTURE%(t0)s EXPAND_NORMAL_NV RGB
		CI0 RGB B TEXTURE%(t1)s EXPAND_NORMAL_NV RGB
		CO0 RGB SPARE0_NV DISCARD_NV DISCARD_NV %(bandscale)s NONE TRUE FALSE FALSE

                # SPARE1 <- SPARE0 * SPARE0
                # SPARE0 <- (TEX0 . CONST0)
		CI1 RGB A SPARE0_NV SIGNED_IDENTITY_NV RGB
		CI1 RGB B SPARE0_NV SIGNED_IDENTITY_NV RGB
		CI1 RGB C TEXTURE%(t0)s EXPAND_NORMAL_NV RGB
		CI1 RGB D CONSTANT_COLOR0_NV EXPAND_NORMAL_NV RGB
		CO1 RGB SPARE1_NV SPARE0_NV DISCARD_NV %(scale)s NONE FALSE TRUE FALSE

                # EF <- SPARE0 * SPARE1
		FCI E SPARE1_NV UNSIGNED_INVERT_NV RGB
		FCI F SPARE0_NV UNSIGNED_IDENTITY_NV RGB

                # lerp(EF, PRI_COL, SEC_COL)
		FCI A E_TIMES_F_NV UNSIGNED_INVERT_NV RGB
		FCI B PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
		FCI C SECONDARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
		FCI D ZERO UNSIGNED_IDENTITY_NV RGB

		FCI G %(finalG)s UNSIGNED_INVERT_NV BLUE
	    """)
        elif type == 1:
            scale = nvcode.combinerscale(avg(*texscales) * 8.0 * rndscale)
            alphascale = nvcode.combinerscale(exp(.5 * abs(rnd.nextGaussian())))
            
            # Interpolate between three colors:
            # d0 = t0 . r0
            # d1 = t1 . r1
            # lerp(d1, lerp(d0, c0, c1), c2)
            # The alpha value is computed as d0^2 - d1^2

            if trans > 0:
                finalG = "SPARE1_NV UNSIGNED_IDENTITY_NV"
            else:
                finalG = "ZERO UNSIGNED_INVERT_NV"
            
	    c = ("""
                # Interpolate between three colors using two dot products
            
                # SPARE0 <- (TEX0 . CONST0)
                # SPARE1 <- (TEX1 . CONST1)
		CI0 RGB A TEXTURE%(t0)s EXPAND_NORMAL_NV RGB
                CI0 RGB B CONSTANT_COLOR0_NV EXPAND_NORMAL_NV RGB
		CI0 RGB C TEXTURE%(t1)s EXPAND_NORMAL_NV RGB
                CI0 RGB D CONSTANT_COLOR1_NV EXPAND_NORMAL_NV RGB
		CO0 RGB SPARE0_NV SPARE1_NV DISCARD_NV %(scale)s NONE TRUE TRUE FALSE

                # PRI_COL <- lerp(SPARE0, PRI_COL, SEC_COL)
		CI1 RGB A PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
		CI1 RGB B SPARE0_NV UNSIGNED_INVERT_NV RGB
		CI1 RGB C SECONDARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
		CI1 RGB D SPARE0_NV UNSIGNED_IDENTITY_NV RGB
		CO1 RGB DISCARD_NV DISCARD_NV PRIMARY_COLOR_NV NONE NONE FALSE FALSE FALSE

                # SPARE1.alpha <- SPARE0^2 - SPARE1^2
                CI1 ALPHA A SPARE0_NV SIGNED_IDENTITY_NV BLUE
                CI1 ALPHA B SPARE0_NV SIGNED_IDENTITY_NV BLUE
                CI1 ALPHA C SPARE1_NV SIGNED_NEGATE_NV BLUE
                CI1 ALPHA D SPARE1_NV SIGNED_IDENTITY_NV BLUE
                CO1 ALPHA DISCARD_NV DISCARD_NV SPARE1_NV %(alphascale)s NONE FALSE FALSE FALSE

                # lerp(SPARE1, PRI_COL, FOG)
                FCI A SPARE1_NV UNSIGNED_INVERT_NV RGB
                FCI B PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
                FCI C FOG UNSIGNED_IDENTITY_NV RGB
                FCI D ZERO UNSIGNED_IDENTITY_NV RGB

		FCI G %(finalG)s ALPHA
	    """)
        else:
            scale = nvcode.combinerscale(avg(*texscales) * 8.0 * rndscale)
            alphascale = nvcode.combinerscale(avg(*texscales) * 8.0 * rndscale)

            # Interpolate on the fraction line c0,c1,c2:
            # d0 = t0 . t1
            # c(d0) =
            #    -1 -> c0
            #     0 -> c1
            #    +1 -> c2

            # lerp(d1, lerp(d0, c0, c1), c2)
            # The alpha value is computed as d0^2 - d1^2

            if trans > 0:
                finalG = "SPARE1_NV UNSIGNED_IDENTITY_NV"
            else:
                finalG = "ZERO UNSIGNED_INVERT_NV"
            
	    c = ("""
                # Fraction-line color interpolate
                
                # SPARE0 <- (TEX0 . TEX1)  
                # SPARE1 <- -(TEX0 . TEX1) 
		CI0 RGB A TEXTURE%(t0)s EXPAND_NORMAL_NV RGB
		CI0 RGB B TEXTURE%(t1)s EXPAND_NORMAL_NV RGB
		CI0 RGB C TEXTURE%(t0)s EXPAND_NEGATE_NV RGB
		CI0 RGB D TEXTURE%(t1)s EXPAND_NORMAL_NV RGB
		CO0 RGB SPARE0_NV SPARE1_NV DISCARD_NV %(scale)s NONE TRUE TRUE FALSE

                # PRI_COL <- lerp(SPARE1, SEC_COL, PRI_COL)
		CI1 RGB A PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
		CI1 RGB B SPARE1_NV UNSIGNED_INVERT_NV RGB
		CI1 RGB C SECONDARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
		CI1 RGB D SPARE1_NV UNSIGNED_IDENTITY_NV RGB
		CO1 RGB DISCARD_NV DISCARD_NV PRIMARY_COLOR_NV NONE NONE FALSE FALSE FALSE

                # lerp(SPARE0, PRI_COL, FOG)
                FCI A SPARE0_NV UNSIGNED_INVERT_NV RGB 
                FCI B PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB 
                FCI C FOG UNSIGNED_IDENTITY_NV RGB 
                FCI D ZERO UNSIGNED_IDENTITY_NV RGB 

                # SPARE1.alpha <- TEX0.b * CONST0.b + TEX1.b * CONST1.b
                CI1 ALPHA A TEXTURE%(t0)s EXPAND_NORMAL_NV BLUE
                CI1 ALPHA B CONSTANT_COLOR0_NV EXPAND_NORMAL_NV BLUE
                CI1 ALPHA C TEXTURE%(t1)s EXPAND_NORMAL_NV BLUE
                CI1 ALPHA B CONSTANT_COLOR1_NV EXPAND_NORMAL_NV BLUE
                CO1 ALPHA DISCARD_NV DISCARD_NV SPARE1_NV %(alphascale)s NONE FALSE FALSE FALSE
                
		FCI G %(finalG)s ALPHA 
	    """)
            
            
	c = (constantcode + c) % locals()
	c = nvcode.combinercode(c)
	# print "c: ",c

        if not GL.hasExtension("GL_NV_register_combiners"):
            # Kluge: emulate using fragment program
            c = nvcode.convCombiner(c, GL)
	return c


class DebugCombinerPass:
    "A combinerpass that shows one component (r, g, b) from each of the textures"
    def setupCode(self, texinputs, *extra):
	""" Setup code for the pass. 
	
	Texinputs = the indices of the texture units producing usable RGB.
	"""

	print "DBG: ",texinputs
	t0 = t1 = t2 = texinputs[0]
	if len(texinputs) > 1:
	    t1 = texinputs[1]
	    if len(texinputs) > 2:
		t2 = texinputs[2]
	c = """
	    Enable REGISTER_COMBINERS_NV
	    CombinerParameterNV NUM_GENERAL_COMBINERS_NV 1
	    CombinerParameterNV CONSTANT_COLOR0_NV 1 0 0 1
	    CombinerParameterNV CONSTANT_COLOR1_NV 0 1 0 1
	    Color                                  0 0 1 1

	    CombinerInputNV COMBINER0_NV RGB VARIABLE_A_NV CONSTANT_COLOR0_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_B_NV TEXTURE%(t0)s SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_C_NV CONSTANT_COLOR1_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_D_NV TEXTURE%(t1)s SIGNED_IDENTITY_NV RGB

	    CombinerOutputNV COMBINER0_NV RGB DISCARD_NV DISCARD_NV SPARE0_NV NONE NONE FALSE FALSE FALSE

	    FinalCombinerInputNV VARIABLE_A_NV PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_B_NV TEXTURE%(t2)s UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_C_NV ZERO UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_D_NV SPARE0_NV UNSIGNED_IDENTITY_NV RGB

	    FinalCombinerInputNV VARIABLE_G_NV ZERO UNSIGNED_INVERT_NV ALPHA

	""" % locals()

	return c

