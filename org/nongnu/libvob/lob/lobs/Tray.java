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
import java.util.*;

/** A sequence that stacks the lobs it contains, i.e., places them
 *  over each other.
 */
public class Tray /*extends AbstractSequence*/ {

    /*
    protected boolean sendEventsOnlyToFrontLob;

    public Tray(boolean sendEventsOnlyToFrontLob) {
	this.sendEventsOnlyToFrontLob = sendEventsOnlyToFrontLob;
    }

    public Tray(SequenceModel model, boolean sendEventsOnlyToFrontLob) {
	super(model);
	this.sendEventsOnlyToFrontLob = sendEventsOnlyToFrontLob;
    }

    public Object clone(Object[] params) {
	return new Tray((SequenceModel)params[0], sendEventsOnlyToFrontLob);
    }

    public void chg() {
	super.chg();
	for(int i=0; i<length(); i++)
	    getLob(i).setSize(width, height);
    }

    private static float min(float a, float b) { return (a<b) ? a : b; }
    private static float max(float a, float b) { return (a>b) ? a : b; }

    public float getMinSize(Lob.Axis axis) {
	float result = Float.POSITIVE_INFINITY;
	for(int i=0; i<length(); i++) 
	    result = min(result, getLob(i).getMinSize(axis));
	return result;
    }

    public float getNatSize(Lob.Axis axis) {
	float result = Float.POSITIVE_INFINITY;
	for(int i=0; i<length(); i++) 
	    result = min(result, getLob(i).getNatSize(axis));
	return result;
    }

    public float getMaxSize(Lob.Axis axis) {
	float result = Float.POSITIVE_INFINITY;
	for(int i=0; i<length(); i++) 
	    result = max(result, getLob(i).getMaxSize(axis));
	return result;
    }

    public boolean mouse(VobMouseEvent e, float x0, float y0,
			 float ox0, float oy0) {
	if(sendEventsOnlyToFrontLob)
	    return getLob(length()-1).mouse(e, x0, y0, ox0, oy0);
	else {
	    for(int i=length()-1; i>=0; i--) {
		if(getLob(i).mouse(e, x0, y0, ox0, oy0)) return true;
	    }
	    return false;
	}
    }

    public boolean key(String key) {
	if(sendEventsOnlyToFrontLob)
	    return getLob(length()-1).key(key);
	else
	    return super.key(key);
    }

    protected float width, height;

    public void setSize(float w, float h) {
	width = w; height = h;

	for(int i=0; i<length(); i++)
	    getLob(i).setSize(w, h);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {
	float z = d;
	float dd = d/length();

	for(int i=0; i<length(); i++) {

	    z -= dd;
	    int cs = scene.coords.translate(into, 0, 0, z);

	    if(model.getKey(i) == null) {
		getLob(i).render(scene, cs, matchingParent, w, h, dd,
				 visible);
	    } else {
		if(scene.matcher instanceof IndexedVobMatcher) {
		    IndexedVobMatcher m = (IndexedVobMatcher)scene.matcher;
			m.add(matchingParent, cs, model.getKey(i), 
			      model.getIntKey(i));
		} else {
		    scene.matcher.add(matchingParent, cs, model.getKey(i));
		}
		getLob(i).render(scene, cs, cs, w, h, dd, visible);
	    }
	}
    }

    public float getPosition(Axis a, int i) {
	return 0;
    }

    public int getLobIndexAt(float x, float y) {
	if(length() == 0) throw new NoSuchElementException("empty tray");
	return length()-1;
    }

    public int getCursorIndexAt(float x, float y) {
	throw new UnsupportedOperationException("not implemented");
    }
    */
}
