/*
Margin.java
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

/** A lob placing a margin around its contents.
 */
public class Margin extends AbstractDelegateLob {

    protected float left, right, top, bottom;

    private Margin() {}

    public Margin newInstance(Lob content, float margin) {
	return newInstance(content, margin, margin, margin, margin);
    }

    public Margin newInstance(Lob content, float x, float y) {
	return newInstance(content, x, x, y, y);
    }

    public Margin newInstance(Lob content, float left, float right, 
			      float top, float bottom) {
	Margin m = (Margin)FACTORY.object();
	m.delegate = content;

	m.left = left;
	m.right = right;
	m.top = top;
	m.bottom = bottom;
	return m;
    }

    public SizeRequest getSizeRequest() {
	float dx = left+right, dy = top+bottom;
	SizeRequest r = delegate.getSizeRequest();

	return SizeRequest.newInstance(r.minW+dx, r.natW+dx, r.maxW+dx,
				       r.minH+dy, r.natH+dy, r.maxH+dy);
    }

    public Layout layout(float w, float h) {
	return super.layout(w-left-right, h-top-bottom);
    }

    public void render(Layout delegateLayout,
		       VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {
	int cs = scene.coords.box(into, left, top, 
				  w - left - right, h - top - bottom);

	delegateLayout.render(scene, cs, matchingParent, d, visible);
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new Margin();
	    }
	};
}
