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

    public static Margin newInstance(Lob content, float margin) {
	return newInstance(content, margin, margin, margin, margin);
    }

    public static Margin newInstance(Lob content, float x, float y) {
	return newInstance(content, x, x, y, y);
    }

    public static Margin newInstance(Lob content, float left, float right, 
				     float top, float bottom) {
	Margin m = (Margin)FACTORY.object();
	m.delegate = content;

	m.left = left;
	m.right = right;
	m.top = top;
	m.bottom = bottom;
	return m;
    }

    protected Lob wrap(Lob l) {
	return newInstance(l, left, right, top, bottom);
    }

    public SizeRequest getSizeRequest() {
	float dx = left+right, dy = top+bottom;
	SizeRequest r = delegate.getSizeRequest();

	return SizeRequest.newInstance(r.minW+dx, r.natW+dx, r.maxW+dx,
				       r.minH+dy, r.natH+dy, r.maxH+dy);
    }

    public Lob layout(float w, float h) {
	Lob l = delegate.layout(w-left-right, h-top-bottom);
	return newInstance(l, left, right, top, bottom);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {
	SizeRequest s = delegate.getSizeRequest();
	float w = s.width(), h = s.height();
	
	int cs = scene.coords.box(into, left, top, 
				  w - left - right, h - top - bottom);

	delegate.render(scene, cs, matchingParent, d, visible);
    }

    public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			 float x, float y) {
	
	return delegate.mouse(e, scene, cs, x-left, y-top);
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new Margin();
	    }
	};
}
