/*
FocusWithBuoysManager.java
 *    
 *    Copyright (c) 2003, Matti J. Katila 
 *    This file is part of Libvob.
 *    
 *    Libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Matti J. Katila 
 */

package org.nongnu.libvob.buoy.impl;
import org.nongnu.libvob.buoy.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;
import org.nongnu.libvob.impl.*;
import org.nongnu.libvob.impl.gl.GLAPI;
import java.util.*;

import org.nongnu.libvob.vobs.SimpleConnection;

/** The traditional buoymanager with single focus view port.
 */
public class FocusWithBuoysManager implements BuoyManager {
    static public boolean dbg = false;
    static private void p(String s) { System.out.println("FocusWithBuoysManager:: "+s); }

    // move this to somewhere else!?
    SimpleConnection lineConnector = new SimpleConnection(0.5f, 0.5f, 0.5f, 0.5f);
    {
    if (GraphicsAPI.getInstance() instanceof GLAPI) {
    lineConnector.glsetup = GLCache.getCallList(
        "PushAttrib ENABLE_BIT LINE_BIT\n"+
	"Disable TEXTURE_2D\n"+
	"LineWidth 5\n"+
	"Enable BLEND\n"+
	"Color 0 0 0 0.6");
    lineConnector.glteardown = GLCache.getCallList("PopAttrib");
    }
    }

    static final int NO_ANIMATION = -1;
    private int animationCsBuoyToMain = NO_ANIMATION;
    private Object animationMainVPtoBuoy = null;


    // implement
    public void replaceMainNodeWith(BuoyViewMainNode main) { mainNode = main;}

    // implement
    public BuoyViewMainNode getMainNode() { return mainNode; }
    private BuoyViewMainNode mainNode;
    private BuoyViewConnector[] connectors;
    private BuoyGeometryConfiguration geometryConf;

    public FocusWithBuoysManager(BuoyViewMainNode mainNode,
				 BuoyViewConnector[] connectors,
				 BuoyGeometryConfiguration configuration) 
    {
	this.mainNode = mainNode;
	this.connectors = connectors;
	this.geometryConf = configuration;
    }

    private Map cs = new HashMap();
    // implement
    public BuoyManager.Buoy getBuoy(int cs) { 
	return (BuoyManager.Buoy)this.cs.get(""+cs); 
    }

    // All buoy link calls are collected in this list.
    private List linkCalls = new ArrayList();

    class LinkCall {
	BuoySizer sizer;
	BuoyGeometer geometer;
	List links;
	LinkCall(BuoySizer sizer, 
		 BuoyGeometer geometer,
		 List links) {
	    this.sizer = sizer;
	    this.geometer = geometer;
	    this.links = links;
	}
    }

    
    private List links = null;
    private int mainVP_CS = -1;
    // implement
    public void draw(VobScene vs, int mainBoxInto) {
	if (dbg) p("start drawing");
	// first take care of clearing and setting things up.
	buoyInd = 0;
	cs.clear();
	linkCalls.clear();

	BuoyMainViewGeometer mainGeom = geometryConf.getMainViewGeometer(mainNode);
	int into = mainGeom.mainCS(vs, mainBoxInto, "K", true);

	for(Iterator it = geometryConf
		.getGeometers(mainNode).iterator(); it.hasNext();) {
	    BuoyGeometer buoyGeom = (BuoyGeometer)it.next();
	    buoyGeom.prepare(vs, mainBoxInto, buoyGeom, true);
	}
	vs.activate(into);
	((org.nongnu.libvob.impl.gl.GLVobCoorderBase)
	 vs.coords).activateRegion(into);
	//p("mark as region: "+into);
	mainNode.renderMain(vs, into);


	// while running this loop all connectors 
	// call back for linking.
	for(int i=0; i<connectors.length; i++) {
	    if (dbg) p("connectors: "+connectors[i]);

	    // for every buoy connector must be own list.
	    this.links = new ArrayList();
	    connectors[i].addBuoys(vs, into, mainNode, this);
	    linkCalls.add(new LinkCall(
		geometryConf.getSizer(mainNode, connectors[i]),
		geometryConf.getGeometer(mainNode, connectors[i]),
		this.links));
	}

	if (dbg) p("next link really");
	// after all call backs are done, link really
	for (int i=0; i<linkCalls.size(); i++) {
	    LinkCall call = (LinkCall)linkCalls.get(i);
	    for (int j=0; j<call.links.size(); j++) {
		Link l = (Link)call.links.get(j);
		linkReally(vs, call.sizer, call.geometer, l.dir, l.anchorCS,
			   l.otherNode, l.linkId, l.otherAnchor, l.count);
	    }
	}
	if (dbg) p("links done");

	// interpolation : old buoy -> to new main view port
	if (animationCsBuoyToMain != NO_ANIMATION) {
	    if (dbg) p("");
	    ((DefaultVobMatcher)vs.matcher).keymapSingleCoordsys(into, animationCsBuoyToMain);
	    animationCsBuoyToMain = NO_ANIMATION;
	}
	// this *MUST* be after real linking to 
	// interpolate correctly.
	mainVP_CS = into;
    }

    class Link {
	int dir;
	int anchorCS;
	BuoyViewNodeType otherNode;
	Object linkId;
	Object otherAnchor;
	int count;
	public Link(int dir, int anchorCS, BuoyViewNodeType otherNode, 
	    Object linkId, Object otherAnchor, int count) {
	    this.dir = dir;
	    this.anchorCS = anchorCS;
	    this.otherNode = otherNode;
	    this.linkId = linkId;
	    this.otherAnchor = otherAnchor;
	    this.count = count;
	}
    }

    // implement
    public void link(int dir, int anchorCS, BuoyViewNodeType otherNode, 
	    Object linkId, Object otherAnchor) {
	link(dir, anchorCS, otherNode, linkId, otherAnchor, 1);
    }
    // implement
    public void link(int dir, int anchorCS, BuoyViewNodeType otherNode, 
	    Object linkId, Object otherAnchor, int count) {
	links.add(new Link(dir, anchorCS, otherNode, linkId, otherAnchor, count));
    }


    private void linkReally(VobScene vs, BuoySizer sizer, BuoyGeometer geometer,
			    int dir, int anchorCS, BuoyViewNodeType otherNode,
			    Object linkId, Object otherAnchor, int count) {
	if (dbg) p(//"sizer: "+sizer+" geom: "+geometer+
		    " dir: "+dir+
		    " anchorCS: "+anchorCS+" otherNode: "+otherNode+
		    " linkId: "+linkId+" otherAnchor: "+otherAnchor);

	float[] size = new float[2];
	Object obj = otherNode.getSize(linkId, otherAnchor, size);
	float scale = sizer.getBuoySize(size[0], size[1], size);
	float
	    width = size[0], 
	    height = size[1];

	if (dbg) p("linkR: buoy cs");
	// linkId.toString()+dir ???
	int into = geometer.buoyCS(vs, anchorCS, dir, linkId.toString()+dir+count, 
				   count, 1, width, height, scale);

	vs.activate(into);
	((org.nongnu.libvob.impl.gl.GLVobCoorderBase)
	 vs.coords).activateRegion(into);
	//p("buoy cs: "+into);
	
	if (into < 1) return;

	if (dbg) p("linkR: render the buoy");
	// render yhe buoy, anchor cs back.
	int otherAnchorCS = otherNode.renderBuoy(vs, into, width, 
	      height, linkId, otherAnchor, null);
	if (dbg) p("linkR: render the buoy..");
	cs.put(""+into, getNewBuoy(otherNode, linkId, otherAnchor, into, dir));

	if (anchorCS >= 0)
	    vs.map.put(lineConnector, vs.unitSqCS(anchorCS, "UN"), 
		       vs.unitSqCS(otherAnchorCS, "UN"));
	
	// interpolation : old main view port -> to new buoy
	if (linkId.equals(animationMainVPtoBuoy)) {
	    if (dbg) p("linkR: buoy link interpolation");
	    ((DefaultVobMatcher)vs.matcher).keymapSingleCoordsys(into, this.mainVP_CS);
	    animationMainVPtoBuoy = null;
	}

	if (dbg) p("linkR: done");
    }

    // implement
    public void moveFocusTo(BuoyManager.Buoy buoy) {
	animationCsBuoyToMain = buoy.getBuoyCS();
	animationMainVPtoBuoy = buoy.getLinkId();
	mainNode = buoy.getNodeType()
	    .createMainNode(buoy.getLinkId(), buoy.getBuoyAnchor());
    }



    private int buoyInd = 0, buoyInc = 5;
    private BuoyManager.Buoy [] buoys = new BuoyManager.Buoy[5];
    private BuoyManager.Buoy getNewBuoy(BuoyViewNodeType nodeType, 
					Object linkId, Object anchor, 
					int buoyCS, int direction) 
    {
	// take care if we ran out of array size
	if (buoyInd >= buoys.length) {
	    BuoyManager.Buoy [] newArray = 
		new BuoyManager.Buoy[buoys.length + buoyInc];
	    System.arraycopy(buoys, 0, newArray, 0, buoys.length);
	    // is this efficient for memory?
	    for (int i=0; i<buoys.length; i++) buoys[i] = null;
	    buoys = newArray;
	}
	
	if (buoys[buoyInd] == null)
	    buoys[buoyInd] = new BuoyImpl();
	BuoyManager.Buoy buoy =
	    ((BuoyImpl)buoys[buoyInd]).set(nodeType, linkId, anchor, 
					   buoyCS, direction);
	buoyInd += 1;
	return buoy;
    }
       


    class BuoyImpl implements BuoyManager.Buoy {
	BuoyViewNodeType nodeType = null;
	Object linkId = null, anchor = null;
	int cs = -1, direction = 0;
	
	BuoyManager.Buoy set(BuoyViewNodeType nodeType, Object linkId, 
		Object anchor, int buoyCS, int direction) {
	    this.nodeType = nodeType;
	    this.linkId = linkId;
	    this.anchor = anchor;
	    this.cs = buoyCS;
	    this.direction = direction;
	    return this;
	}

	// implement
	public BuoyViewNodeType getNodeType() { return nodeType; }
	public Object getLinkId() { return linkId; }
	public Object getBuoyAnchor() { return anchor; }
	public int getBuoyCS() { return cs; }
	public int getDirection() { return direction; }
    }

}
