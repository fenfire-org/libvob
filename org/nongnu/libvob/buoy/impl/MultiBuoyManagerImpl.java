/*
MultiBuoyManagerImpl.java
 *    
 *    Copyright (c) 2003, Matti Katila
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
 * Written by Matti Katila
 */

package org.nongnu.libvob.buoy.impl;
import org.nongnu.libvob.buoy.*;
import org.nongnu.libvob.VobScene;
import java.util.*;

public class MultiBuoyManagerImpl implements MultiBuoyManager {
    public static boolean dbg = false;
    private void p(String s) { System.out.println("MultiBuoyManager:: "+s); }

    // implement
    public BuoyManager.Buoy getLastFoundBuoy() {return lastBuoy;}
    private BuoyManager.Buoy lastBuoy = null;

    // implement
    public BuoyManager getManagerByLastBuoyHit() { return lastManagerByBuoyHit; }
    private BuoyManager lastManagerByBuoyHit = null;

    // implement
    public void setActiveBuoyManager(BuoyManager manager) {activeManager = manager; }
    // implement
    public BuoyManager getActiveBuoyManager() {return activeManager;}
    private BuoyManager activeManager = null;


    /** Return the iterator for buoy managers.
     */
    public Iterator iterator() { return managers.iterator(); }
    protected List managers = new ArrayList();
    protected BuoyGeometryConfiguration configuration; 
    protected BuoyViewConnector[] connectors;
    protected FocusViewPortsGeometer geometer;
    

    public MultiBuoyManagerImpl(BuoyViewMainNode[] mainNodes,
			    BuoyViewConnector[] connectors,
			    FocusViewPortsGeometer geometer,
			    BuoyGeometryConfiguration configuration) 
    {
	for (int i=0; i<mainNodes.length; i++) {
	    managers.add(new FocusWithBuoysManager(
		mainNodes[i], connectors, configuration)
	    );
	}
	this.configuration = configuration;
	this.connectors = connectors;
	this.geometer = geometer;
	if (managers.size() > 0)
	    activeManager = (BuoyManager)managers.get(0);
	managersCS = new int[mainNodes.length];
    }

    private int[] managersCS;

    // implement
    public void draw(VobScene vs) {
	for (int i=0; i<managersCS.length; i++) {
	    managersCS[i] = -1;
	}
	geometer.place(vs, managersCS);
	for (int i=0; i<managers.size(); i++) {
	    if (managersCS.length > i && managersCS[i] > 0) {
		((BuoyManager)managers.get(i)).draw(vs, managersCS[i]);
	    }
	}
    }

    // implement
    public BuoyManager findTopmostBuoyManager(VobScene oldVS, int x, int y) {
	BuoyManager topmost = null;
	float depth = 0;
	float hit[] = new float[3];
	
	for (int i=0; i<managers.size(); i++) {

	    // Not every bouy manager is drawn!
            if (managersCS[i] < 1) continue;

	    if (dbg) p("manager index: "+i+", vs: "+oldVS);
	    BuoyManager m = (BuoyManager)managers.get(i);
	    if (m.getMainNode().hasMouseHit(oldVS, x,y, hit)) {
		if (dbg) p("Hit: "+hit[0]);
		// first time only topmost is compared
		if (topmost == null || depth < hit[0]) {
		    topmost = m;
		    depth = hit[0];
		}
		if (dbg) p("topmost: "+topmost+", depth: "+depth);
	    }
	}
	return topmost;
    }

    // implement
    public BuoyManager.Buoy findIfBuoyHit(VobScene oldVS, int x, int y) {
	if (oldVS==null) return null;

	int cs = oldVS.getCSAt(0, x,y, null);
	if (dbg) p("Buoy cs found: "+cs);
	for (int i=0; i<managers.size(); i++) {
	    BuoyManager m = (BuoyManager)managers.get(i);
	    BuoyManager.Buoy buoy = m.getBuoy(cs);
	    if (buoy != null) {
		lastManagerByBuoyHit = m;
		lastBuoy = buoy;
		return buoy;
	    }
	}
	return null;
    }

}
