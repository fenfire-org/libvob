/*
AlignLob.java
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
import org.nongnu.libvob.util.*;

/** A monolob that aligns a child relative to its parent.
 *  For each axis, you give it a fraction for the child
 *  and a fraction for the parent. It will then align the point
 *  inside the child box with the point inside the parent box.
 *  For example, with all fractions set to 0.5f, it will center
 *  its child inside its own box, because on each axis, it will align
 *  the child's center (0.5f) with the center of its own box (0.5f).
 */
public class AlignLob extends AbstractDelegateLob {

    protected float childX, childY;
    protected float parentX, parentY;

    private AlignLob() {}

    public static AlignLob newInstance(Lob delegate, 
				       float childX, float childY, 
				       float parentX, float parentY) {
	AlignLob l = (AlignLob)FACTORY.object();

	l.delegate = delegate;
	l.childX = childX; l.childY = childY;
	l.parentX = parentX; l.parentY = parentY;

	return l;
    }

    public Lob layout(float w, float h) {
	SizeRequest s = delegate.getSizeRequest();

	AlignLayout l = (AlignLayout)LAYOUT_FACTORY.object();
	l.setSize(w, h);
	l.child = delegate.layout(s.natW, s.natH);

	l.childX = childX; l.childY = childY;
	l.parentX = parentX; l.parentY = parentY;

	return l;
    }
 
    public void render(VobScene scene, int into, int matchingParent, 
		       float d, boolean visible) {
	throw new UnsupportedOperationException();
    }

    private static class AlignLayout extends AbstractLayout {
	private Lob child;

	protected float childX, childY;
	protected float parentX, parentY;

	protected void setSize(float w, float h) { super.setSize(w, h); }

	public void render(VobScene scene, int into, int matchingParent,
			   float d, boolean visible) {
	    
	    SizeRequest s = child.getSizeRequest();
	    float w = s.width(), h = s.height();
	    
	    float x = parentX*w - childX*s.width();
	    float y = parentY*h - childY*s.height();
	    
	    int cs = scene.coords.translate(into, x, y);
	    
	    child.render(scene, cs, matchingParent, d, visible);
	}
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new AlignLob();
	    }
	};

    private static final Factory LAYOUT_FACTORY = new Factory() {
	    public Object create() {
		return new AlignLayout();
	    }
	};
}
