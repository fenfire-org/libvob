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
from vob.putil.misc import *
from vob.putil.effects import *

class Scene:
    def __init__(self):
	self.ps2 = vob.input.impl.PS2MouseDevice("/dev/input/mouse1", "main",
			vob.input.impl.PS2MouseDevice.IMPS_PROTO)
	self.naxes = len(self.ps2.getAxes())
	self.axes = [
	    vob.input.impl.StandardBoundedFloatModel(0, 400,
		actionPerformed = lambda x: vob.AbstractUpdateManager.chg())
	    for i in range(0,self.naxes)]
	for i in range(0,self.naxes):
	    self.ps2.getAxes()[i].setMainListener(
		vob.input.BoundedFloatLinearAbsoluteAdapter(self.axes[i]))
    def scene(self, vs):
	vs.put( background((0.1,0.3,0.9)))
	#print "SC"
	#for i in range(0,self.naxes):
	#    print self.axes[i].getValue()

	for i in range(0,self.naxes):
	    vs.put(coloredQuad((1,1,1)),
	      vs.orthoCS(0, i, 0, 60 + i * 60, 700, 40, 
		- self.axes[i].getValue()))






