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


import java
from org.nongnu import libvob as vob

def benchScene(vs,
	npoints = 100,
	nvobs = 100,
	ncs = 10,
	type = 0,
	):
    vs.put(vob.vobs.SolidBackdropVob(java.awt.Color(.3,.7,.6)))
    cs = 0
    for i in range(0, ncs):
	if type == 0:
	    cs = vs.translateCS(cs, "A", 1.5, 1.5)
	elif type == 1:
	    cs = vs.scaleCS(cs, "A", 1.5, 1.5)
	elif type == 2:
	    cs = vs.orthoCS(cs, "A", 1.5, 1.5, 1.5, 1.5, 1.5)
	elif type == 3:
	    cs = vs.affineCS(cs, "A", 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5)
    tt = vob.gl.GLRen.createTransTest(npoints, 1)
    for i in range(0, nvobs):
	vs.put(tt, cs)

args = {
"npoints" : (100),
"nvobs" : (500),
"ncs" : (1, 10),
"type" : (0,1,2,3),
}
