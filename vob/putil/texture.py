# 
# Copyright (c) 2003, Tuomas J. Lukka
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

from org.nongnu.libvob.gl import GL

def printTex(id):
    print "TexDump for",id
    for p in (
	    "TEXTURE_BASE_LEVEL",
	    "TEXTURE_MAX_LEVEL",
	    "TEXTURE_MAX_ANISOTROPY_EXT",
	    ):
	print "p:\t",p,"\t", GL.getGLTexParameterFloat("TEXTURE_2D", id, p)
    for p in (
	    "TEXTURE_MIN_FILTER",
	    "TEXTURE_MAG_FILTER",
	    ):
	print "p:\t",p,"\t", GL.getGLTokenString(
		int(GL.getGLTexParameterFloat("TEXTURE_2D", id, p)[0]))
    for i in range(0,4):
	print "Level",i
	for p in (
	    "TEXTURE_WIDTH", 
	    "TEXTURE_HEIGHT",
	    "TEXTURE_RED_SIZE",
	    "TEXTURE_GREEN_SIZE",
	    "TEXTURE_BLUE_SIZE",
	    "TEXTURE_ALPHA_SIZE",
	    "TEXTURE_DEPTH_SIZE",
	    "TEXTURE_LUMINANCE_SIZE",
	    "TEXTURE_INTENSITY_SIZE",
			):
	    print "p:\t",p,"\t", GL.getGLTexLevelParameterFloat("TEXTURE_2D", id, i, p)
	print "if:\t",GL.getGLTokenString(
	    int(GL.getGLTexLevelParameterFloat("TEXTURE_2D", id, i, 
			    "TEXTURE_INTERNAL_FORMAT")[0]))


