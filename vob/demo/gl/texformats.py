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


import vob

formats = [

"ALPHA",
"ALPHA4",   
"ALPHA8",   
"ALPHA12",   
"ALPHA16",   
"LUMINANCE",   
"LUMINANCE4",   
"LUMINANCE8",    
"LUMINANCE12",    
"LUMINANCE16",
"LUMINANCE_ALPHA",   
"LUMINANCE4_ALPHA4",   
"LUMINANCE6_ALPHA2",  
"LUMINANCE8_ALPHA8",  
"LUMINANCE12_ALPHA4",  
"LUMINANCE12_ALPHA12",
"LUMINANCE16_ALPHA16", 
"INTENSITY", 
"INTENSITY4", 
"INTENSITY8", 
"INTENSITY12", 
"INTENSITY16", 
"R3_G3_B2", 
"RGB", 
"RGB4",  
"RGB5",
"RGB8", 
"RGB10", 
"RGB12", 
"RGB16", 
"RGBA", 
"RGBA2", 
"RGBA4", 
"RGB5_A1", 
"RGBA8", 
"RGB10_A2", 
"RGBA12", 
"RGBA16",
]

for f in formats:
    tex = vob.gl.GL.createTexture()
    tex.texImage2D(2, f, 1, 1, 0, "RGBA", "BYTE", 
	[0,0,0,0])

    str = ""
    for p in (
	    "TEXTURE_RED_SIZE",
	    "TEXTURE_GREEN_SIZE",
	    "TEXTURE_BLUE_SIZE",
	    "TEXTURE_ALPHA_SIZE",
	    "TEXTURE_DEPTH_SIZE",
	    "TEXTURE_LUMINANCE_SIZE",
	    "TEXTURE_INTENSITY_SIZE",
	    ):
	s = int(tex.getLevelParameter(2, p)[0])
	str = "%s %s" % (str, s)

    print str, f


