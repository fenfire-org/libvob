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
from vob.putil.misc import *

def parstr(name):
    return name + ": " + js(GL.getGLFloat(name))

class Scene:
    """Show various OpenGL information: extensions and limits.
    """
    def __init__(self):
        self.bgcolor = (1,1,1)
    def key(self, k):
        pass
    def scene(self, vs):
	vs.put( background(self.bgcolor))

        size = vs.getSize()

        colchars = 90
        scale = min(size.width*1.0, size.height*4.0/3) / colchars

	cs1 = vs.coords.affine(0, 10, 0, 0, scale, 0, 0, scale)
        vs.matcher.add(cs1, "1")

        vendor = GL.getGLString("VENDOR")
        renderer = GL.getGLString("RENDERER")
        version = GL.getGLString("VERSION")
        extensions = GL.getGLString("EXTENSIONS")

        params = [ parstr(name) for name in [
            "MAX_LIGHTS",
            "MAX_CLIP_PLANES",
            "MAX_COLOR_MATRIX_STACK_DEPTH",
            "MAX_MODELVIEW_STACK_DEPTH",
            "MAX_PROJECTION_STACK_DEPTH",
            "MAX_TEXTURE_STACK_DEPTH",
            "SUBPIXEL_BITS",
            "MAX_3D_TEXTURE_SIZE",
            "MAX_TEXTURE_SIZE",
            #ifdef GL_MAX_CUBE_MAP_TEXTURE_SIZE
            "MAX_CUBE_MAP_TEXTURE_SIZE",
            #endif
            "MAX_PIXEL_MAP_TABLE",
            "MAX_NAME_STACK_DEPTH",
            "MAX_LIST_NESTING",
            "MAX_EVAL_ORDER",
            
            "MAX_VIEWPORT_DIMS",
            
            "MAX_ATTRIB_STACK_DEPTH",
            "MAX_CLIENT_ATTRIB_STACK_DEPTH",
            "AUX_BUFFERS",
            
            "RGBA_MODE",
            "INDEX_MODE",
            "DOUBLEBUFFER",
            "STEREO",

            "ALIASED_POINT_SIZE_RANGE",
            "SMOOTH_POINT_SIZE_RANGE",
            "POINT_SIZE_RANGE",
            "SMOOTH_POINT_SIZE_GRANULARITY",
            "POINT_SIZE_GRANULARITY",
            "ALIASED_LINE_WIDTH_RANGE",
            "SMOOTH_LINE_WIDTH_RANGE",
            "LINE_WIDTH_RANGE",
            "SMOOTH_LINE_WIDTH_GRANULARITY",
            "LINE_WIDTH_GRANULARITY",

            #// ignoring convolution

            "MAX_ELEMENTS_INDICES",
            "MAX_ELEMENTS_VERTICES",

            #ifdef GL_MAX_TEXTURE_UNITS
            "MAX_TEXTURE_UNITS",
            #endif
            #ifdef GL_MAX_TEXTURE_UNITS_ARB
            "MAX_TEXTURE_UNITS_ARB",
            #endif
            
            #ifdef GL_SAMPLE_BUFFERS
            "SAMPLE_BUFFERS",
            #endif
            #ifdef GL_SAMPLES
            "SAMPLES",
            #endif
            #/// ignoring ctformats
    
            #ifdef GL_NUM_COMPRESSED_TEXTURE_FORMATS
            "NUM_COMPRESSED_TEXTURE_FORMATS",
            #endif

            "RED_BITS",
            "GREEN_BITS",
            "BLUE_BITS",
            "ALPHA_BITS",
            "INDEX_BITS",
            "DEPTH_BITS",
            "STENCIL_BITS",
            
            "ACCUM_RED_BITS",
            "ACCUM_GREEN_BITS",
            "ACCUM_BLUE_BITS",
            "ACCUM_ALPHA_BITS",
            
            #ifdef GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
            "MAX_TEXTURE_MAX_ANISOTROPY_EXT",
            #endif
            #ifdef GL_MAX_GENERAL_COMBINERS_NV
            "MAX_GENERAL_COMBINERS_NV",
            #endif

            #ifdef GL_MAX_TRACK_MATRICES_NV
            "MAX_TRACK_MATRICES_NV",
            "MAX_TRACK_MATRIX_STACK_DEPTH_NV",
            #endif
            
	    "MAX_VERTEX_ATTRIBS_ARB",
	    "MAX_PROGRAM_MATRICES_ARB",
	    "MAX_PROGRAM_MATRIX_STACK_DEPTH_ARB",

	    "MAX_TEXTURE_COORDS_ARB",
	    "MAX_TEXTURE_IMAGE_UNITS_ARB",
            ]]

	params.append("");
	params.append("vpARB");
	params.extend( [ name + ": "+ js(GL.getGLProgram("VERTEX_PROGRAM_ARB", name)) 
	 for name in [
	    "MAX_PROGRAM_INSTRUCTIONS_ARB",
	    "MAX_PROGRAM_TEMPORARIES_ARB",
	    "MAX_PROGRAM_PARAMETERS_ARB",
	    "MAX_PROGRAM_ATTRIBS_ARB",
	    "MAX_PROGRAM_ADDRESS_REGISTERS_ARB",

	    "MAX_PROGRAM_NATIVE_INSTRUCTIONS_ARB",
	    "MAX_PROGRAM_NATIVE_TEMPORARIES_ARB",
	    "MAX_PROGRAM_NATIVE_PARAMETERS_ARB",
	    "MAX_PROGRAM_NATIVE_ATTRIBS_ARB",
	    "MAX_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB",

	    "MAX_PROGRAM_LOCAL_PARAMETERS_ARB",
	    "MAX_PROGRAM_ENV_PARAMETERS_ARB",
	    ]])

	params.append("");
	params.append("fpARB");
	params.extend( [ name + ": "+ js(GL.getGLProgram("FRAGMENT_PROGRAM_ARB", name)) 
	 for name in [
	    "MAX_PROGRAM_INSTRUCTIONS_ARB",
	    "MAX_PROGRAM_ALU_INSTRUCTIONS_ARB",
	    "MAX_PROGRAM_TEX_INSTRUCTIONS_ARB",
	    "MAX_PROGRAM_TEX_INDIRECTIONS_ARB",
	    "MAX_PROGRAM_TEMPORARIES_ARB",
	    "MAX_PROGRAM_PARAMETERS_ARB",
	    "MAX_PROGRAM_ATTRIBS_ARB",

	    "MAX_PROGRAM_NATIVE_INSTRUCTIONS_ARB",
	    "MAX_PROGRAM_NATIVE_ALU_INSTRUCTIONS_ARB",
	    "MAX_PROGRAM_NATIVE_TEX_INSTRUCTIONS_ARB",
	    "MAX_PROGRAM_NATIVE_TEX_INDIRECTIONS_ARB",
	    "MAX_PROGRAM_NATIVE_TEMPORARIES_ARB",
	    "MAX_PROGRAM_NATIVE_PARAMETERS_ARB",
	    "MAX_PROGRAM_NATIVE_ATTRIBS_ARB",

	    "MAX_PROGRAM_LOCAL_PARAMETERS_ARB",
	    "MAX_PROGRAM_ENV_PARAMETERS_ARB",
	    ]])


                   
        # double size text
        putText(vs, cs1, vendor, color=(0,0,0), h=2, y = 2)
        putText(vs, cs1, renderer, color=(0,0,0), h=2, y = 4)
        putText(vs, cs1, version, color=(0,0,0), h=2, y = 6)

        foo = params
        i = 0
        cols = 5
        lines = (len(foo)+cols-1) / cols
        for x in range(0, cols):
            for y in range(0, lines):
                if i >= len(foo): break
                
                putText(vs, cs1, foo[i], color=(0,0,0), h=1,
                        x=x*(colchars/cols), y=y+8, key = str(i))
                i += 1



        foo = extensions.split();
        i = 0
        cols = 5
        lines = (len(foo)+cols-1) / cols
        for x in range(0, cols):
            for y in range(0, lines):
                if i >= len(foo): break
                
                putText(vs, cs1, foo[i], color=(0,0,0), h=1,
                        x=x*(colchars/cols), y=y+28)
                i += 1

	return vs



