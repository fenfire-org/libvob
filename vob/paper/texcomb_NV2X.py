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


# Register combiners for NV2x architecture, where there
# are plenty of them.
    
class TransparentCombinerPass:
    def setupCode(self, texinputs, colors, rnd, trans = 0):
	colorbase = rnd.nextInt()
	c0, c1, c2, c3 = [ colors.getColorStr(colorbase+i)
				for i in range(0,4) ]

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
	c = ("""
            Enable BLEND
            BlendFunc SRC_ALPHA ONE_MINUS_SRC_ALPHA
            Disable ALPHA_TEST

	    Enable REGISTER_COMBINERS_NV
	    Enable PER_STAGE_CONSTANTS_NV
	    CombinerParameterNV NUM_GENERAL_COMBINERS_NV 4
	    
	    CombinerStageParameterNV COMBINER0_NV CONSTANT_COLOR0_NV %(r0)s
	    CombinerStageParameterNV COMBINER0_NV CONSTANT_COLOR1_NV %(r1)s
	    CombinerStageParameterNV COMBINER1_NV CONSTANT_COLOR0_NV %(r2)s
	    CombinerStageParameterNV COMBINER1_NV CONSTANT_COLOR1_NV %(r3)s

	    CombinerStageParameterNV COMBINER2_NV CONSTANT_COLOR0_NV %(c0)s %(alphascale)s
	    CombinerStageParameterNV COMBINER2_NV CONSTANT_COLOR1_NV %(c1)s 1
	    CombinerStageParameterNV COMBINER3_NV CONSTANT_COLOR0_NV %(c2)s 1
	    CombinerStageParameterNV COMBINER3_NV CONSTANT_COLOR1_NV %(c3)s 1
	"""+
	# Stage 0: multiply texture outputs by each other componentwise:
	# T0 * T1 -> SPARE0,  T1 * T2 -> SPARE1, SPARE0 + SPARE1 -> PRIMARY_COLOR_NV
	# Since some might be the same, use different input mappings.
	"""
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_A_NV TEXTURE%(t0)s UNSIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_B_NV TEXTURE%(t1)s UNSIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_C_NV TEXTURE%(t2)s EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_D_NV TEXTURE%(t3)s EXPAND_NORMAL_NV RGB
	    CombinerOutputNV COMBINER0_NV RGB SPARE0_NV SPARE1_NV PRIMARY_COLOR_NV NONE NONE FALSE FALSE FALSE

	"""+
	# Stage 1: Dot the results of the previous stage with the constant colors
	# Write dot products into TEXTURE0 and TEXTURE1
	"""
	    CombinerInputNV COMBINER1_NV RGB VARIABLE_A_NV CONSTANT_COLOR0_NV EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER1_NV RGB VARIABLE_B_NV SPARE0_NV EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER1_NV RGB VARIABLE_C_NV CONSTANT_COLOR1_NV EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER1_NV RGB VARIABLE_D_NV PRIMARY_COLOR_NV EXPAND_NORMAL_NV RGB
	    CombinerOutputNV COMBINER1_NV RGB TEXTURE0 TEXTURE1 DISCARD_NV SCALE_BY_TWO_NV NONE TRUE TRUE FALSE
	"""+
	# Start mixing the colors
	"""
	    CombinerInputNV COMBINER2_NV RGB VARIABLE_A_NV CONSTANT_COLOR0_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER2_NV RGB VARIABLE_B_NV TEXTURE0 UNSIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER2_NV RGB VARIABLE_C_NV CONSTANT_COLOR1_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER2_NV RGB VARIABLE_D_NV TEXTURE0 UNSIGNED_INVERT_NV RGB
	    CombinerOutputNV COMBINER2_NV RGB DISCARD_NV DISCARD_NV SPARE0_NV NONE NONE FALSE FALSE FALSE

	    CombinerInputNV COMBINER2_NV ALPHA VARIABLE_A_NV CONSTANT_COLOR0_NV UNSIGNED_IDENTITY_NV ALPHA
	    CombinerInputNV COMBINER2_NV ALPHA VARIABLE_B_NV TEXTURE1 UNSIGNED_INVERT_NV BLUE
	    CombinerOutputNV COMBINER2_NV ALPHA SPARE0_NV DISCARD_NV DISCARD_NV SCALE_BY_FOUR_NV NONE FALSE FALSE FALSE

	    CombinerInputNV COMBINER3_NV RGB VARIABLE_A_NV CONSTANT_COLOR0_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER3_NV RGB VARIABLE_B_NV TEXTURE1 UNSIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER3_NV RGB VARIABLE_C_NV CONSTANT_COLOR1_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER3_NV RGB VARIABLE_D_NV TEXTURE1 UNSIGNED_INVERT_NV RGB
	    CombinerOutputNV COMBINER3_NV RGB DISCARD_NV DISCARD_NV SPARE1_NV NONE NONE FALSE FALSE FALSE

            CombinerInputNV COMBINER3_NV ALPHA VARIABLE_A_NV ZERO UNSIGNED_INVERT_NV ALPHA
	    CombinerInputNV COMBINER3_NV ALPHA VARIABLE_B_NV SPARE0_NV SIGNED_IDENTITY_NV ALPHA
	    CombinerOutputNV COMBINER3_NV ALPHA SPARE0_NV DISCARD_NV DISCARD_NV SCALE_BY_FOUR_NV NONE FALSE FALSE FALSE
	"""+
	# Finally, combine the results: mix the colors
	"""
	    FinalCombinerInputNV VARIABLE_A_NV TEXTURE1 UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_B_NV SPARE0_NV UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_C_NV SPARE1_NV UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_D_NV ZERO UNSIGNED_IDENTITY_NV RGB

	    FinalCombinerInputNV VARIABLE_G_NV SPARE0_NV UNSIGNED_INVERT_NV ALPHA

	""") % locals()
	return c


class EmbossCombinerPass:
    def setupCode(self, texinputs, rnd, trans = 0):

	# 4 random dot product vectors
	r0, r1, r2, r3 = [ js(randvec2(rnd,.5)) + " 1" for j in range(0,4) ]


        # map alpha dot product a \in [0,1] into clamp(1 - (1-a) * alphascale)
        if trans > 0:
            alphascale = 1 - 1.0/trans
        else:
            alphascale = 0
            
        alphascale = alphascale * (1. / 16)

	assert len(texinputs) != 0
	while len(texinputs) < 4:
	    texinputs = texinputs + texinputs

        # Value at when both textures are at the same level
        level=1

        # The lightness is computed clamped in the alpha component and
        # the part overflowing 1.0 is computed in the rgb components.
        # Using "BlendFunc DST_COLOR SRC_ALPHA" allows going beyond
        # the original destination color in brightness and one could even
        # use "BlendFunc ONE SRC_ALPHA" to add the overflowing
        # part as white.

        level=js([1-level for i in range(0,3)] + [level])
            
	t0, u0, t1, u1 = texinputs[0:4]
	c = """
            Enable BLEND
            BlendFunc DST_COLOR SRC_ALPHA
            Disable ALPHA_TEST

	    Enable REGISTER_COMBINERS_NV
	    Enable PER_STAGE_CONSTANTS_NV
	    CombinerParameterNV NUM_GENERAL_COMBINERS_NV 3

	    CombinerStageParameterNV COMBINER0_NV CONSTANT_COLOR0_NV %(r0)s
	    CombinerStageParameterNV COMBINER2_NV CONSTANT_COLOR0_NV %(level)s

	    CombinerInputNV COMBINER0_NV RGB VARIABLE_A_NV CONSTANT_COLOR0_NV EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_B_NV TEXTURE%(t0)s EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_C_NV CONSTANT_COLOR0_NV EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_D_NV TEXTURE%(u0)s EXPAND_NORMAL_NV RGB
	    CombinerOutputNV COMBINER0_NV RGB PRIMARY_COLOR_NV SECONDARY_COLOR_NV DISCARD_NV NONE NONE TRUE TRUE FALSE

	    CombinerInputNV COMBINER1_NV RGB VARIABLE_A_NV ZERO EXPAND_NEGATE_NV RGB
	    CombinerInputNV COMBINER1_NV RGB VARIABLE_B_NV PRIMARY_COLOR_NV EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER1_NV RGB VARIABLE_C_NV ZERO EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER1_NV RGB VARIABLE_D_NV SECONDARY_COLOR_NV EXPAND_NORMAL_NV RGB
	    CombinerOutputNV COMBINER1_NV RGB DISCARD_NV DISCARD_NV PRIMARY_COLOR_NV SCALE_BY_FOUR_NV NONE FALSE FALSE FALSE

	    CombinerInputNV COMBINER2_NV ALPHA VARIABLE_A_NV ZERO UNSIGNED_INVERT_NV ALPHA
	    CombinerInputNV COMBINER2_NV ALPHA VARIABLE_B_NV PRIMARY_COLOR_NV SIGNED_IDENTITY_NV BLUE
	    CombinerInputNV COMBINER2_NV ALPHA VARIABLE_C_NV ZERO UNSIGNED_INVERT_NV ALPHA
	    CombinerInputNV COMBINER2_NV ALPHA VARIABLE_D_NV CONSTANT_COLOR0_NV UNSIGNED_IDENTITY_NV ALPHA
	    CombinerOutputNV COMBINER2_NV ALPHA DISCARD_NV DISCARD_NV PRIMARY_COLOR_NV NONE NONE FALSE FALSE FALSE

	    CombinerInputNV COMBINER2_NV RGB VARIABLE_A_NV ZERO UNSIGNED_INVERT_NV RGB
	    CombinerInputNV COMBINER2_NV RGB VARIABLE_B_NV PRIMARY_COLOR_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER2_NV RGB VARIABLE_C_NV ZERO UNSIGNED_INVERT_NV RGB
	    CombinerInputNV COMBINER2_NV RGB VARIABLE_D_NV CONSTANT_COLOR0_NV SIGNED_NEGATE_NV RGB
	    CombinerOutputNV COMBINER2_NV RGB DISCARD_NV DISCARD_NV PRIMARY_COLOR_NV NONE NONE FALSE FALSE FALSE

	    FinalCombinerInputNV VARIABLE_A_NV ZERO UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_B_NV ZERO UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_C_NV ZERO UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_D_NV PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_G_NV PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV ALPHA

	""" % locals()
	return c



class SimpleCombinerPass:
    def setupCode(self, texinputs, rnd):
	# 4 colors
	minlum = 60
	c0 = js(getRandomColor(minlum,100, rnd))
	c1 = js(getRandomColor(minlum,minlum + (100-minlum)*0.5, rnd))
	c2 = js(getRandomColor(minlum + (100-minlum)*0.5, 100, rnd))
	c3 = js(getRandomColor(minlum + (100-minlum)*0.5, 100, rnd))
	# 4 random dot product vectors
	r0, r1, r2, r3 = [
	    js([rnd.nextDouble() for i in range(0,4)])
	    for j in range(0,4)]

	assert len(texinputs) != 0
	while len(texinputs) < 4:
	    texinputs = texinputs + texinputs
	t0, t1, t2, t3 = texinputs[0:4]
	c = """
	    Enable REGISTER_COMBINERS_NV
	    Enable PER_STAGE_CONSTANTS_NV
	    CombinerParameterNV NUM_GENERAL_COMBINERS_NV 4
	    
	    CombinerStageParameterNV COMBINER0_NV CONSTANT_COLOR0_NV %(r0)s
	    CombinerStageParameterNV COMBINER0_NV CONSTANT_COLOR1_NV %(r1)s
	    CombinerStageParameterNV COMBINER1_NV CONSTANT_COLOR0_NV %(r2)s
	    CombinerStageParameterNV COMBINER1_NV CONSTANT_COLOR1_NV %(r3)s

	    CombinerStageParameterNV COMBINER2_NV CONSTANT_COLOR0_NV %(c0)s 1
	    CombinerStageParameterNV COMBINER2_NV CONSTANT_COLOR1_NV %(c1)s 1
	    CombinerStageParameterNV COMBINER3_NV CONSTANT_COLOR0_NV %(c2)s 1
	    CombinerStageParameterNV COMBINER3_NV CONSTANT_COLOR1_NV %(c3)s 1

	    CombinerInputNV COMBINER0_NV RGB VARIABLE_A_NV CONSTANT_COLOR0_NV EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_B_NV TEXTURE%(t0)s EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_C_NV CONSTANT_COLOR1_NV EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER0_NV RGB VARIABLE_D_NV TEXTURE%(t1)s EXPAND_NORMAL_NV RGB
	    CombinerOutputNV COMBINER0_NV RGB SPARE0_NV SPARE1_NV DISCARD_NV SCALE_BY_TWO_NV NONE TRUE TRUE FALSE

	    CombinerInputNV COMBINER1_NV RGB VARIABLE_A_NV CONSTANT_COLOR0_NV EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER1_NV RGB VARIABLE_B_NV TEXTURE%(t2)s EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER1_NV RGB VARIABLE_C_NV CONSTANT_COLOR1_NV EXPAND_NORMAL_NV RGB
	    CombinerInputNV COMBINER1_NV RGB VARIABLE_D_NV TEXTURE%(t3)s EXPAND_NORMAL_NV RGB
	    CombinerOutputNV COMBINER1_NV RGB PRIMARY_COLOR_NV SECONDARY_COLOR_NV DISCARD_NV SCALE_BY_TWO_NV NONE TRUE TRUE FALSE

	    CombinerInputNV COMBINER2_NV RGB VARIABLE_A_NV CONSTANT_COLOR0_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER2_NV RGB VARIABLE_B_NV SPARE0_NV UNSIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER2_NV RGB VARIABLE_C_NV CONSTANT_COLOR1_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER2_NV RGB VARIABLE_D_NV SPARE0_NV UNSIGNED_INVERT_NV RGB
	    CombinerOutputNV COMBINER2_NV RGB DISCARD_NV DISCARD_NV SPARE0_NV NONE NONE FALSE FALSE FALSE

	    CombinerInputNV COMBINER3_NV RGB VARIABLE_A_NV CONSTANT_COLOR0_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER3_NV RGB VARIABLE_B_NV SPARE1_NV UNSIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER3_NV RGB VARIABLE_C_NV CONSTANT_COLOR1_NV SIGNED_IDENTITY_NV RGB
	    CombinerInputNV COMBINER3_NV RGB VARIABLE_D_NV SPARE1_NV UNSIGNED_INVERT_NV RGB
	    CombinerOutputNV COMBINER3_NV RGB DISCARD_NV DISCARD_NV SPARE1_NV NONE NONE FALSE FALSE FALSE


	    FinalCombinerInputNV VARIABLE_A_NV PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_B_NV SPARE0_NV UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_C_NV SPARE1_NV UNSIGNED_IDENTITY_NV RGB
	    FinalCombinerInputNV VARIABLE_D_NV ZERO UNSIGNED_IDENTITY_NV RGB

	    FinalCombinerInputNV VARIABLE_G_NV ZERO UNSIGNED_INVERT_NV ALPHA

	""" % locals()
	return c

