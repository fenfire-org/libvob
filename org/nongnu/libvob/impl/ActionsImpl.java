/*
ActionsImpl.java
 *    
 *    Copyright (c) 2005, Matti J. Katila
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

package org.nongnu.libvob.impl;
import org.nongnu.libvob.mouse.*;
import org.nongnu.libvob.*;
import java.util.*;

public class ActionsImpl implements Actions {
    public static boolean dbg = false;
    private void p(String s) { System.out.println("ActionsImpl:: "+s); }

    private VobScene vs;
    private Map actions;

    public ActionsImpl(VobScene vs) { 
	this.vs = vs;
	actions = new HashMap();
    } 

    // impl.
    public Action justPerform(VobMouseEvent ev) {
	int cs = vs.getCSAt(0, ev.getX(), ev.getY(), null);
	if (dbg) p("found cs: "+cs);
	return execAction(cs, ev);
    }

    // impl.
    public Action execAction(int cs, VobMouseEvent ev) {
	return execAction(null, new int[0], cs, ev);
    }
    private Action execAction(ChildVobScene cvs, int [] actCSs, 
			      int cs, VobMouseEvent ev) {
	VobScene scene = (cvs != null? cvs: vs);
	if (dbg) p("Exec.. "+cs);
	
	if(scene.actions.get(cs) != null) {
	    if (dbg) p("what is found("+cs+")? "+scene.actions.get(cs));
	    Action a = (Action) scene.actions.get(cs);
	    if (!a.deliverEvent(ev)) {
		if (scene.coords.hasActiveChildVS(cs)) {
		    ChildVobScene child = scene.coords.getChildByCS(cs);
		    //p("child: "+child);
		    //p("child Coords: "+child.coords);
		    int [] NactCSs = addAct(actCSs, cs);
		    int parent = -1;
		    int cs_ = vs.coords.getChildCSAt(NactCSs, parent, ev.getX(), ev.getY(), null);
		    if (cs_ > 0)
			return execAction(child, NactCSs, cs_, ev); 
		    // nothing found - try normal getCSAt
		    //p("try NORMLAL "+cs);
		}
		if (cvs == null)
		    return execAction(cvs, actCSs, vs.getCSAt(cs, ev.getX(), ev.getY(), null), ev); 
		else
		    return execAction(cvs, actCSs, vs.coords.getChildCSAt(actCSs, cs, ev.getX(), ev.getY(), null), ev); 
	    }
	    return a;
	}
	return null;
    }
	
    private int[] addAct(int[] i, int j) {
	int [] ret = new int[i.length+1];
	System.arraycopy(i, 0, ret, 0, i.length);
	ret[i.length] = j;
	return ret;
    }
    
    // impl.
    public void put(int cs, Action action) {
	Integer cs_ = new Integer(cs);
	actions.put(cs_, action);
    }

    public Action get(int cs) {
	Integer cs_ = new Integer(cs);
	return (Action) actions.get(cs_);
    }
}
