/*
TranslateLob.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein
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

/** A lob placing its contents into a translation cs.
 */
public class TranslateLob extends AbstractDelegateLob {

    protected float x, y, z;
    protected boolean testBoundsInMouse;
    protected float width, height;

    private TranslateLob() {}

    public static TranslateLob newInstance(Lob content, 
					   float x, float y, float z, 
					   boolean testBoundsInMouse) {
	TranslateLob m = (TranslateLob)FACTORY.object();
	m.delegate = content;

	m.x = x; m.y = y; m.z = z;
	m.testBoundsInMouse = testBoundsInMouse;

	m.width = m.height = -1;

	return m;
    }

    public SizeRequest getSizeRequest() {
	if(width < 0)
	    return SizeRequest.newInstance(0, 0, SizeRequest.INF,
					   0, 0, SizeRequest.INF);
	else
	    return SizeRequest.newInstance(width, width, width,
					   height, height, height);
    }

    public Lob layout(float w, float h) {
	SizeRequest r = delegate.getSizeRequest();
	Lob l = delegate.layout(r.natW, r.natH);
	TranslateLob t = newInstance(l, x, y, z, testBoundsInMouse);
	t.width = w; t.height = h;
	return t;
    }

    public Axis getLayoutableAxis() {
	return null;
    }

    public Lob layoutOneAxis(float size) {
	throw new UnsupportedOperationException();
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {
	int cs = scene.coords.translate(into, x, y, z);
	delegate.render(scene, cs, matchingParent, d, visible);
    }

    public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			 float x, float y) {

	if(width < 0 || height < 0)
	    throw new UnsupportedOperationException("not layouted");

	SizeRequest r = delegate.getSizeRequest();

	if(testBoundsInMouse) {
	    if(x < this.x || x > this.x + r.width())  return false;
	    if(y < this.y || y > this.y + r.height()) return false;
	}

	return delegate.mouse(e, scene, cs, x-this.x, y-this.y);
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new TranslateLob();
	    }
	};
}
