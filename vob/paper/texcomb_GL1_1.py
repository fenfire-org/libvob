# 
# Copyright (c) 2003, Janne Kujala, Tuomas J. Lukka and Matti Katila
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


# Register combiners emulation for OpenGL 1.1 architecture

#from gfx.libutil import nvcode
   
class TransparentCombinerPass:
    def setupCode(self, texinputs, texscales, colors, rnd, trans = 0):
	# 4 colors
	colorbase = rnd.nextInt()
	c0, c1, c2, c3 = [ colors.getColorStr(colorbase+i)
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

        #c0, c1, c2 = [ "1 1 1", "1 0 0", "0 0 0"]
        
	constantcode = """
            Enable BLEND
            BlendFunc ONE_MINUS_SRC_ALPHA SRC_ALPHA

            # BlendEquation FUNC_ADD

            Disable ALPHA_TEST

            # Enable REGISTER_COMBINERS_NV
            # CombinerParameterNV NUM_GENERAL_COMBINERS_NV 2

            ActiveTexture TEXTURE0
            TexEnv TEXTURE_ENV TEXTURE_ENV_MODE BLEND
            TexEnv TEXTURE_ENV TEXTURE_ENV_COLOR %(c1)s


            #BlendFunc SRC_ALPHA ONE_MINUS_SRC_ALPHA 


            ActiveTexture TEXTURE1
            TexEnv TEXTURE_ENV TEXTURE_ENV_MODE BLEND
            TexEnv TEXTURE_ENV TEXTURE_ENV_COLOR %(c2)s

            # back to texture0
            ActiveTexture TEXTURE0

            # Multitexturing
            #Multitexture 

	    Color %(c0)s %(trans)s
            # SecondaryColorEXT %(c1)s
	    #Fog FOG_COLOR %(c2)s
	""" % locals()

        type = rnd.nextInt(3)
        # types: 0=BAND-LIKE, 1=3-COL-LERP, 2=FRACTION-LINE

        if type == 0:
            #scale = "SCALE_BY_ONE_HALF_NV"
            #scale = "NONE"
            #scale = "SCALE_BY_TWO_NV"
            scale = "SCALE_BY_FOUR_NV"

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
		#CI0 RGB A TEXTURE%(t0)s EXPAND_NORMAL_NV RGB

                # SPARE1 <- SPARE0 * SPARE0 * 2 
                # SPARE0 <- (TEX0 . CONST0) * 2

                # EF <- SPARE0 * SPARE1

                # lerp(EF, PRI_COL, SEC_COL)
	    """)
        elif type == 1:
            #scale = "SCALE_BY_ONE_HALF_NV"
            #scale = "NONE"
            scale = "SCALE_BY_TWO_NV"
            #scale = "SCALE_BY_FOUR_NV"

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

                # PRI_COL <- lerp(SPARE0, PRI_COL, SEC_COL)

                # SPARE1.alpha <- SPARE0^2 - SPARE1^2

                # lerp(SPARE1, PRI_COL, FOG)
	    """)
        else:
            #scale = "SCALE_BY_ONE_HALF_NV"
            scale = "NONE"
            #scale = "SCALE_BY_TWO_NV"
            #scale = "SCALE_BY_FOUR_NV"

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

                # PRI_COL <- lerp(SPARE1, SEC_COL, PRI_COL)

                # lerp(SPARE0, PRI_COL, FOG)

                # SPARE1.alpha <- TEX0.b * CONST0.b + TEX1.b * CONST1.b
	    """)
            
            
	c = (constantcode + c) % locals()
	#c = nvcode.combinercode(c)
	# print "c: ",c
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

