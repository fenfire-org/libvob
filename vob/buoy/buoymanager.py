# 
# Copyright (c) 2003, Tuomas J. Lukka
# 
# This file is part of Fenfire.
# 
# Fenfire is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Fenfire is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Fenfire; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 

from __future__ import nested_scopes
import jarray

import vob
from org.nongnu.libvob.buoy import BuoyLinkListener, BuoyGeometryConfiguration

from vob.putil.misc import *

# Slow animation for the demo.
vob.AbstractUpdateManager.defaultAnimationTime = 1500
vob.AbstractUpdateManager.fractCalc = vob.AbstractUpdateManager.LinearCalculator(0)

# The connector is just a vob.
lineconn = vob.vobs.SimpleConnection(.5, .5, .5, .5)
lineconn.glsetup = GLCache.getCallList("""
	    PushAttrib ENABLE_BIT LINE_BIT
	    Disable TEXTURE_2D
	    LineWidth 5
	    Enable BLEND
	    Color 0 0 0 0.6
	""")
lineconn.glteardown = GLCache.getCallList("""
	    PopAttrib
	""")

#lineconn = GLRen.createSqFilletConnection(30, 1, .125, 6)

size = jarray.zeros(2, 'f')

dbg = 0
def pa(*s):
    print 'buoymanager::',s

pa("LOADING BUOYMANAGER")
class SingleFocusManager(BuoyLinkListener):
    """Manage buoys through the new APIs

    This is a demo "scene"
    """
    def __init__(self, mainNode, connectors, buoyGeometryConfiguration):
	"""Create a new RealBuoyManager.

        Parameters are the initial main node
        and the BuoyViewConnectors to be used
        by the buoy manager to create buoys.
	"""
	self.mainNode = mainNode
        self.connectors = connectors

	self.geometryConfiguration = buoyGeometryConfiguration

    def getMainNode(self):
        return self.mainNode
    def draw(self, vs, mainboxinto):
	self.vs = vs
	self.cs = { }


	buoyMainViewGeometer = self.geometryConfiguration\
				.getMainViewGeometer(self.mainNode)
	into = buoyMainViewGeometer.mainCS(vs, mainboxinto, "K", 1)

        iter =  self.geometryConfiguration \
              .getGeometers(self.mainNode).iterator()
        while iter.hasNext():
            buoyGeometer = iter.next()
	    buoyGeometer.prepare(vs, mainboxinto, buoyGeometer, 1)

	self.vs.activate(into)

	self.mainNode.renderMain(vs, into)

	self.linkCalls = []
        for connector in self.connectors:
	    self.links = []
            connector.addBuoys(vs, into, self.mainNode, self)
	    self.linkCalls.append(
		(self.geometryConfiguration.getSizer(
			self.mainNode, connector),
		 self.geometryConfiguration.getGeometer(
			self.mainNode, connector),
		self.links))

	for l in self.linkCalls: 
	    (sizer, geometer, list) = l
	    for call in list:
		self._linkReally(sizer, geometer, *call)

	self.cs[into] = None
        
        # Interpolation : old buoy -> to new main vp
        if hasattr(self, "animationCS_buoy") and \
               self.animationCS_buoy != None:
            self.vs.matcher.keymapSingleCoordsys(into, self.animationCS_buoy[0])
            self.animationCS_buoy = None

        self.mainCS = into

    def link(self, *args):
	self.links.append(args)

    def _linkReally(self, sizer, geometer, direction, anchorCS, otherNode, linkId, otherAnchor, shift=0):
	"""Create the real buoy.
	"""
        if dbg: pa('link really')

	#### Buoy size
	obj = otherNode.getSize(linkId, otherAnchor, size)

	sca = sizer.getBuoySize(size[0], size[1], size)

	w = size[0]
	h = size[1]

        if dbg: pa("BuoyScaling: ",w, h, sca)

	into = geometer.buoyCS(self.vs, anchorCS, direction,
		(linkId, direction),
		shift, 1,
		w, h, sca)

	if into < 0: return
	
	if dbg:
	    dbg1 = self.vs.unitSqCS(into, "U")
	    self.vs.put(coloredQuad((0,1,0)), dbg1)

	self.vs.activate(into)

	### Render the buoy, get back anchor cs
	if dbg: pa("Render buoy ",into, linkId, otherAnchor)
	otherAnchorCS = otherNode.renderBuoy(self.vs, into, w, h, linkId, otherAnchor, None)
	self.cs[into] = (otherNode, linkId, otherAnchor, into)

	if anchorCS >= 0:
	    # anchorCS < 0 when there is no anchor, e.g., for ttconnector
	    # in FenPDF
	    self.vs.map.put(lineconn, self.vs.unitSqCS(anchorCS, "UN"), 
			    self.vs.unitSqCS(otherAnchorCS, "UN"))


        # Interpolation : old mainvp -> to new buoy
        if hasattr(self, "animationCS_main") and \
               self.animationCS_main != None and \
               linkId == self.animationCS_main[1][1]:
            self.vs.matcher.keymapSingleCoordsys(into,self.animationCS_main[0])
            self.animationCS_main = None
        if dbg: pa('link really..DONE')
	    
    def followLink(self, link):
        cs = link[3]
        self.animationCS_buoy = [cs, link, self.vs]
        self.animationCS_main = [self.mainCS, link, self.vs]

        pa( "Following link", link)

        self.mainNode = link[0].createMainNode(link[1], link[2])


    def followLinkByAnchor(self, anchor):
        pa("Searching for anchor", anchor)
        for key in self.cs.keys():
            pa("Comparing to", self.cs[key])
            if self.cs[key] != None:
                a = self.cs[key][2]
                if a == anchor:
                    self.followLink(self.cs[key])
                    return
                
        pa("Anchor not found!!!")
        

class MultiBuoyManager:
    def __init__(self, mainNodes, connectors,
		    geometer, buoyGeometryConfiguration):
        # jython implementation
	self.singles = [
	    SingleFocusManager(i, connectors, buoyGeometryConfiguration) for i in mainNodes]
        # java implementation
	#self.singles = [
	#    vob.buoy.impl.FocusWithBuoysManager(i, connectors, buoyGeometryConfiguration) for i in mainNodes]
        self.buoyGeometryConfiguration = buoyGeometryConfiguration
        self.connectors = connectors
	self.geometer = geometer
        self.lastIndex = 0
        class BuoyHit:
            def set(self, single, link):
                self.single, self.link = single, link
        self.buoyHit = BuoyHit()

    def setActiveBuoyManager(self, mgr): pass
    def getActiveBuoyManager(self): return self.singles[0]
    def key(self, key):
        raise 'no key events here, thank you'
    def replaceManager(self, index, replace):
        self.singles[index] = SingleFocusManager(replace, self.connectors, self.buoyGeometryConfiguration)
    def getVs(self):
        return self.vs;
    def getSingles(self):
        return self.singles
    def getLastMain(self):
	"""Return the main node that was the latest target of a mouse event.
	"""
        return self.singles[self.lastIndex].getMainNode()
    def draw(self, vs): self.scene(vs)
    def scene(self, vs):
	self.vs = vs

        geoms = jarray.zeros(2, 'i')
        # set jarray items to -1
        for i in range(len(geoms)): geoms[i] = -1
	self.geometer.place(vs, geoms)

	for i in range(0, len(self.singles)):
	    if geoms[i] >= 0:
		self.singles[i].draw(vs, 
			geoms[i])

    def findTopmostBuoyManager(self, vs, x,y):
        main = self.findTopmostMainNode(vs, x,y)
        for s in self.singles:
            if main == s.getMainNode(): return s

    def findTopmostMainNode(self, vs, x, y):
	"""Get the topmost main node at x, y.
	"""

	# Fall through if no link - need main node
        # See if anyone hit.
        hit = jarray.zeros(1, 'f')
        theTopmostMainNode = None
        depth = None
	if dbg: pa( "Topmost:")
	for i in range(len(self.singles)):
            single = self.singles[i]
	    if dbg: pa( "look at:",single, single.mainNode)
            if single.mainNode.hasMouseHit(vs, x, y, hit):
		if dbg: pa( "hit:",hit[0])
                if depth == None or depth < hit[0]:
                    depth = hit[0]
                    theTopmostMainNode = single.mainNode
                    self.lastIndex = i
	if dbg: pa( "Ret: ", theTopmostMainNode)
	return theTopmostMainNode
        
    def findIfBuoyHit(self, vs, x, y):
	"""If a buoy was hit by the coordinates, return the 
	tuple (index, (otherNode, linkId, otherAnchor, buoyCoordsys)).
	"""
	cs = vs.getCSAt(0, x, y, None)
	for single in range(0, len(self.singles)):
	    link = self.singles[single].cs.get(cs, None)
	    if link != None:
		return (single, link)

vob.impl.gl.GLScreen.dbg = 0
