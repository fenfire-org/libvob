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
from org.nongnu import libvob as vob

def benchScene(vs,
	nitems = 200, 
	nletters = 20,
	csdepth = 1,
	multics = 0,
	multitext = 1,
	batchstartcode = 0
	):
    size = vs.getSize()

    w = size.width
    h = size.height
    r = java.util.Random()

    bgcolor = (0.6, 0.7, 0.8)
    vs.map.put(vob.vobs.SolidBackdropVob(java.awt.Color.green))

    alph = 'abcdefghijklmnopqrstuvwxyz'
    alph = 2*alph[:len(alph)]

    coords = []
#     print locals()
    for i in range(0, multics * nitems + (1-multics)):
	cs = 0
	for j in range(0, csdepth):
	    cs = vs.orthoCS(cs, str((i,j)), 0, 0, 0, 1.01, 1.01)
	coords.append(cs)

    print len(coords)

    textvob = vob.vobs.TextVob(vob.GraphicsAPI.getInstance().getTextStyle("foo",0,10), 
		    alph[0:nletters], 0)

    if isinstance(vob.GraphicsAPI.getInstance(), vob.impl.awt.AWTAPI):
        batchstartcode = 0

    if batchstartcode:
	vs.map.put(textvob.getStartCode())
	for i in range(0, nitems):
	    cs = coords[i % len(coords)]
	    vs.map.put(textvob.getPlainRenderableForBenchmarking(), cs)
	vs.map.put(textvob.getStopCode())

    else:
	for i in range(0, nitems):
	    cs = coords[i % len(coords)]
	    vs.map.put(textvob, cs)

    
args = { 
#    "nitems": (0, 100, 200),
    "csdepth" : (1, 15 ), #20),
    "nletters" : (0, 20),
#    "multics": (0, 1),
    "batchstartcode": (0, 1),
}
