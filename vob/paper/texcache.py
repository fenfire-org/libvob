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


# XXX: this code should probably be moved to GLCache

# Kluge: a directive to the gldemo.py code; this module should
# not be deleted but simply reloaded with "r".
__do_not_delete__ = 1
# Side effect: any modules imported from this module will not get
# automatically reloaded.

from org.nongnu.libvob.gl import GL
from java.io import File
import os

dbg = 0

if not globals().has_key("texcache"):
    texcache = {}
    
def getCachedTexture(args, shade_all_levels = 0):
    global texcache

    name = args[6]
    file = "../libvob/src/texture/" + name
    binfile = file + ".bin"
    srcfile = file + ".texture"

    bintime = File(binfile).lastModified()
    srctime = File(srcfile).lastModified()

    if srctime > bintime:
        os.system("make -C ./libvob/src/texture " + name + ".bin")
        
    key = str((args,shade_all_levels))
    if texcache.has_key(key) and texcache[(key,"ctime")] == srctime:
        #print "Returning cached texture"
        return texcache[key]

    tex = GL.createTexture()
    if shade_all_levels:
        res = tex.shade_all_levels(*args)
    else:
        res = tex.shade(*args)
        
    texcache[key] = tex
    texcache[(key,"ctime")] = srctime
    if dbg:
	print "SHADER: ", res, tex.getTexId()
    return tex

