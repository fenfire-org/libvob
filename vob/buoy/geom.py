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


class SingleGeometer:
    def place(self, vs):
	ctrx = vs.size.width / 2
	ctry = vs.size.height * .485 # A *little* above real ctr

	center = vs.translateCS(0, "FocusCenter", ctrx, ctry)

	mainbox = vs.orthoBoxCS(center, "MainFrame", 0,
			    -vs.size.width * .5, -vs.size.height * .5,
			    1, 1,
			    vs.size.width, vs.size.height);

	return [(mainbox, None, None)]




class DummyGeometryConfiguration(BuoyGeometryConfiguration):
    """A dummy constant geometry.
    """
    def __init__(self):
	self.buoyMainViewGeometer = vob.buoy.impl.RatioMainGeometer(
					.21, .25, .6, .5)
	self.buoySizer = vob.buoy.impl.AspectBuoySizer(400, 400, 1.5)
	self.buoyGeometer = vob.buoy.impl.RatioBuoyOnCircleGeometer(
				    .1, .1, .8, .8)
    def getMainViewGeometer(self, node):
	return self.buoyMainViewGeometer
    def getSizer(self, node, connector):
	return self.buoySizer
    def getGeometer(self, node, connector):
	return self.buoyGeometer
    def getGeometers(self, node):
	return [self.buoyGeometer]

