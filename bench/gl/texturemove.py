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
import java
GL = vob.gl.GL

texture = GL.createTexture()
for i in range(0, 12):
    texture.loadNull2D("TEXTURE_2D", i,
	"COMPRESSED_RGB_S3TC_DXT1_EXT", 2048>>i, 2048>>i, 0,
	"RGB", "FLOAT")

bytes = [
    texture.getCompressedTexImage(level, None)
    for level in range(0,12)
    ]

def bench(nrounds, level):
    tim = java.lang.System.currentTimeMillis
    s = 2048 >> level
    tbytes = bytes[level]
    t0 = tim()
    for i in range(0, nrounds):
	texture.getCompressedTexImage(level, tbytes)
	texture.compressedTexSubImage2D(level, 0, 0, s, s,
	    "COMPRESSED_RGB_S3TC_DXT1_EXT", len(tbytes), tbytes)
    t1 = tim()
    return ((t1-t0)/1000.0, "X")

args = {
    "level": (0,1,2)
}
