# 
# Copyright (c) 2003, Tuomas J. Lukka and Matti Katila
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


import java
import math
from org.nongnu import libvob as vob

def benchScene(vs,
	nreps = 10,
	dicelen = 10,
	dicelen2 = 100,
	size = 400,
	):
    vs.put(vob.vobs.SolidBackdropVob(java.awt.Color(.3,.7,.6)))
    dice = vob.gl.GLRen.createDiceTester(dicelen, dicelen2, 1, 40)
    for i in range(0,nreps):
	cs = vs.orthoCS(0 ,str(i), -i, 2, 2, size, size)
	cs2 = vs.coords.distort(cs, 0, 0, .2, .2, math.log(5), math.log(.5))
	vs.map.put(dice, cs2)


    
args = { 
    "nreps" : (100,),
    "dicelen" : (1,2,4,8),
    "size" : (400, 800),
}
