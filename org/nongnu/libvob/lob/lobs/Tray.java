/*
Tray.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *
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
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.*;
import javolution.realtime.*;
import java.util.*;

/** A sequence that stacks the lobs it contains, i.e., places them
 *  over each other.
 */
public class Tray extends AbstractLob {

    private LobList lobs;
    private boolean sendEventsOnlyToFrontLob;

    private float width, height;

    private Tray() {}

    public static Tray newInstance(LobList lobs, 
				   boolean sendEventsOnlyToFrontLob) {
	Tray l = (Tray)FACTORY.object();
	l.lobs = lobs;
	l.sendEventsOnlyToFrontLob = sendEventsOnlyToFrontLob;
	l.width = l.height = -1;
	return l;
    }

    private static float min(float a, float b) { return (a<b) ? a : b; }
    private static float max(float a, float b) { return (a>b) ? a : b; }

    public SizeRequest getSizeRequest() {
	SizeRequest r = SizeRequest.newInstance(SizeRequest.INF, 0, 0,
						SizeRequest.INF, 0, 0);
	
	for(int i=0; i<lobs.getLobCount(); i++) {
	    PoolContext.enter();
	    try {
		SizeRequest s = lobs.getLob(i).getSizeRequest();
		
		r.minW = min(r.minW, s.minW);
		r.natW = max(r.natW, s.natW);
		r.maxW = max(r.maxW, s.maxW);

		r.minH = min(r.minH, s.minH);
		r.natH = max(r.natH, s.natH);
		r.maxH = max(r.maxH, s.maxH);
	    } finally {
		PoolContext.exit();
	    }
	}

	return r;
    }

    public boolean key(String key) {
	if(sendEventsOnlyToFrontLob) {
	    return lobs.getLob(0).key(key);
	} else {
	    for(int i=0; i<lobs.getLobCount(); i++) {
		PoolContext.enter();
		try {
		    if(lobs.getLob(i).key(key))
			return true;
		} finally {
		    PoolContext.exit();
		}
	    }
	}

	return false;
    }

    public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			 float x, float y) {
	
	if(sendEventsOnlyToFrontLob) {
	    return lobs.getLob(0).mouse(e, scene, cs, x, y);
	} else {
	    for(int i=0; i<lobs.getLobCount(); i++) {
		PoolContext.enter();
		try {
		    if(width < 0 || height < 0)
			throw new UnsupportedOperationException("not layouted");

		    Lob layout = lobs.getLob(i).layout(width, height);

		    if(layout.mouse(e, scene, cs, x, y))
			return true;
		} finally {
		    PoolContext.exit();
		}
	    }
	}

	return false;
    }


    public Lob layout(float w, float h) {
	Tray t = newInstance(lobs, sendEventsOnlyToFrontLob);
	t.width = w; t.height = h;
	return t;
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {

	if(width < 0 || height < 0)
	    throw new UnsupportedOperationException("not layouted");

	int nlobs = lobs.getLobCount();

	float z = 0;
	float dd = d/nlobs;

	for(int i=0; i<nlobs; i++) {

	    z += dd;
	    int cs = scene.coords.translate(into, 0, 0, z);

	    PoolContext.enter();
	    try {
		Lob layout = lobs.getLob(i).layout(width, height);
		layout.render(scene, cs, matchingParent, dd, visible);
	    } finally {
		PoolContext.exit();
	    }
	}
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new Tray();
	    }
	};
}
