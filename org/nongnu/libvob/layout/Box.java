/*
Box.java
 *    
 *    Copyright (c) 2003-2004, Benja Fallenstein
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.DefaultVobMap; // for chaining coordinate systems
import org.nongnu.navidoc.util.Obs;
import java.util.*;

/** A lob that renders a list of lobs, stretching or shrinking them
 *  to fill the requested size.
 */
public class Box extends AbstractSequence {
    static public final Object 
	URI = "XXX_Box",
	AXIS = "XXX_axis_YYYY_axis_ZZZZZZZ_axis";

    public static final Object[] PARAMS = { AXIS };

    protected final Lob.Axis axis;

    protected float[] positions;

    protected float natSize=0, minSize=0, maxSize=0;
    protected int nInfiniteStretch = 0;

    protected float minSize2=0, natSize2=0, maxSize2=Float.POSITIVE_INFINITY;

    protected boolean sizesAreCurrent, layoutIsCurrent;
    protected boolean isLayouting;

    public Box(Lob.Axis axis) {
	this(axis, new SequenceModel.SimpleModel());
    }

    public Box(Lob.Axis axis, SequenceModel model) {
	super(model);
	this.axis = axis;
    }

    /** Convenience function for add(new Glue(axis, min, nat, max))
     */
    public void glue(float min, float nat, float max) {
	add(new Glue(axis, min, nat, max));
    }

    public Object clone(Object[] params) {
	return new Box(axis, (SequenceModel)params[0]);
    }

    public void addRequest(Lob l, float min, float nat, float max) {
	add(new RequestChangeLob(axis, l, min, nat, max));
    }
    public void addRequest(Lob l, Object key, 
			   float min, float nat, float max) {
	add(new RequestChangeLob(axis, l, min, nat, max), key);
    }
    public void addRequest(Lob l, Object key, int index,
			   float min, float nat, float max) {
	add(new RequestChangeLob(axis, l, min, nat, max), key, index);
    }

    protected void computeSizes() {
	minSize = natSize = maxSize = minSize2 = natSize2 = 0;
	maxSize2 = Float.POSITIVE_INFINITY;
	nInfiniteStretch = 0;

	for(int i=0; i<length(); i++) {
	    Lob l = getLob(i);

	    if(!Float.isNaN(natSize)) {
		try {
		    minSize += l.getMinSize(axis);
		    natSize += l.getNatSize(axis);
		    maxSize += l.getMaxSize(axis);
		    if(l.getMaxSize(axis) == Float.POSITIVE_INFINITY)
			nInfiniteStretch++;
		} catch(UnknownSizeError e) {
		    minSize = natSize = maxSize = Float.NaN;
		}
	    }
	    
	    if(!Float.isNaN(minSize2)) {
		try {
		    float min2 = l.getMinSize(axis.other());
		    if(min2>minSize2) minSize2 = min2;
		    float nat2 = l.getNatSize(axis.other());
		    if(nat2>natSize2) natSize2 = nat2;
		    float max2 = l.getMaxSize(axis.other());
		    if(max2<maxSize2) maxSize2 = max2;

		    if(natSize2>maxSize2) natSize2 = maxSize2;
		} catch(UnknownSizeError e) {
		    minSize2 = natSize2 = maxSize2 = Float.NaN;
		}
	    }
	}

	sizesAreCurrent = true;
    }

    public float getMinSize(Lob.Axis axis) {
	if(!sizesAreCurrent) computeSizes();
	if(axis == this.axis) {
	    if(!Float.isNaN(minSize))
		return minSize;
	} else {
	    if(!Float.isNaN(minSize2))
		return minSize2;
	}
	throw new UnknownSizeError(axis.toString());
    }

    public float getNatSize(Lob.Axis axis) {
	if(!sizesAreCurrent) computeSizes();
	if(axis == this.axis) {
	    if(!Float.isNaN(natSize))
		return natSize;
	} else {
	    if(!Float.isNaN(natSize2))
		return natSize2;
	    else
		return minSize2;
	}
	throw new UnknownSizeError(axis.toString());
    }

    public float getMaxSize(Lob.Axis axis) {
	if(!sizesAreCurrent) computeSizes();
	if(axis == this.axis) {
	    if(!Float.isNaN(maxSize))
		return maxSize;
	} else {
	    if(!Float.isNaN(maxSize2))
		return maxSize2;
	}
	throw new UnknownSizeError(axis.toString());
    }

    public float getPosition(Axis a, int i) {
	if(positions == null || length() == 0) return 0;
	if(i >= positions.length) return positions[positions.length-1]; // hmm
	return (a==axis) ? positions[i] : 0;
    }

    public int getLobIndexAt(float x, float y) {
	if(positions == null) return 0;
	float pos = (axis==X) ? x : y;
	for(int i=0; i<length()-1; i++)
	    if(pos < positions[i+1]) return i;
	return length()-1;
    }

    public int getCursorIndexAt(float x, float y) {
	if(positions == null) return 0;
	float pos = (axis==X) ? x : y;
	for(int i=0; i<length()-1; i++)
	    if(pos < (positions[i]+positions[i+1])/2) return i;
	return length()-1;
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float x, float y, float w, float h, float d,
		       boolean visible) {
	if(!layoutIsCurrent)
	    //layout(width, height);
	    layout(w, h);

	boolean isHBox = (axis==X);
	
	if(!isHBox) {
	    float tmp = w; w = h; h = tmp;
	}

	int lastCS = -1;

	for(int i=0; i<length(); i++) {
	    float p = positions[i], lw = positions[i+1] - positions[i];

	    int cs = scene.coords.box(into, isHBox?x+p:x, isHBox?y:y+p,
				      isHBox?lw:h, isHBox?h:lw);

	    int mp = matchingParent;

	    if(getKey(i) != null) {
		if(scene.matcher instanceof IndexedVobMatcher) {
		    IndexedVobMatcher m = (IndexedVobMatcher)scene.matcher;
			m.add(matchingParent, cs, getKey(i), getIntKey(i));
		} else {
		    scene.matcher.add(matchingParent, cs, getKey(i));
		}

		mp = cs;
	    }

	    getLob(i).render(scene, cs, mp, 0, 0, isHBox?lw:h, isHBox?h:lw,
			     d, visible);
	
	    if(lw == getLob(i).getNatSize(axis)) {
		if(lastCS >= 0 && scene.map instanceof DefaultVobMap)
		    ((DefaultVobMap)scene.map).chain(lastCS, cs);
		
		lastCS = cs;
	    } else {
		// do not chain lobs that were stretched or shrunken:
		// chaining is for rendering contiguous characters as a
		// single AWT call, and of course that always renders
		// spaces etc. the same width, so when a space between
		// two words is stretched, we need to render the words
		// separately.
		lastCS = -1;
	    }
	}
    }

    protected float width = Float.NaN, height = Float.NaN;

    public void setSize(float w, float h) {
	/*
	if(Float.isNaN(minSize) || Float.isNaN(natSize) || 
	   Float.isNaN(maxSize)) {
	    throw new UnknownSizeError(this.axis);
	}
	*/

	if(Float.isNaN(axis.coord(w, h))) return;

	if(!Float.isNaN(axis.other().coord(w, h))) {
	    if(axis.other().coord(w, h) != axis.other().coord(width, height)) {
		for(int i=0; i<model.size(); i++) {
		    if(axis == X)
			getLob(i).setSize(Float.NaN, h);
		    else if(axis == Y)
			getLob(i).setSize(w, Float.NaN);
		}

		sizesAreCurrent = false;
	    }
	}

	width = w;
	height = h;

	layoutIsCurrent = false;
    }

    protected void layout(float w, float h) {
	isLayouting = true;

	if(!sizesAreCurrent) computeSizes();

	if(positions == null || positions.length < length()+1 ||
	   positions.length > 4*length())
	    positions = new float[length()+1];

	boolean isHBox = (this.axis == Lob.X);

	if(!isHBox) {
	    float tmp = w; w = h; h = tmp;
	}

	float totalStretch = maxSize-natSize;
	float totalShrink = natSize-minSize;

	// the amount by which we need to stretch/shrink
	float totalDiff = w-natSize;

	float x = 0;

	for(int i=0; i<length(); i++) {
	    Lob l = getLob(i);
	    //result.pos[i] = x;
	    positions[i] = x;

	    float diff = 0;
	    if(totalDiff > 0) {
		if(totalStretch != 0) {
		    if(nInfiniteStretch > 0) {
			if(l.getMaxSize(axis) == Float.POSITIVE_INFINITY)
			    diff = totalDiff / nInfiniteStretch;
		    } else {
			float stretch = 
			    l.getMaxSize(axis) - l.getNatSize(axis);
			diff = totalDiff * (stretch / totalStretch);
		    }
		}
	    } else if(totalDiff < 0) {
		if(totalShrink != 0) {
		    float shrink = l.getNatSize(axis) - l.getMinSize(axis);
		    diff = totalDiff * (shrink / totalShrink);
		}
	    }

	    float lw = l.getNatSize(axis) + diff;
	    x += lw;

	    l.setSize(isHBox?lw:h, isHBox?h:lw);
	}

	positions[length()] = w;

	isLayouting = false;
	layoutIsCurrent = true;
    }

    int nc;

    public void chg() {
	if(!isLayouting) {
	    sizesAreCurrent = layoutIsCurrent = false;
	    super.chg();
	}
    }
}
