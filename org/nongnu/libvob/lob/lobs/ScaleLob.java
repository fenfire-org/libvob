/*
ScaleLob.java
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

/** A lob placing its contents into a scaling cs.
 */
public class ScaleLob extends AbstractDelegateLob {

    protected float scaleX, scaleY;

    private ScaleLob() {}

    public static ScaleLob newInstance(Lob content, float scale) {
	return newInstance(content, scale, scale);
    }

    public static ScaleLob newInstance(Lob content, float scaleX, 
				       float scaleY) {
	ScaleLob m = (ScaleLob)FACTORY.object();
	m.delegate = content;

	m.scaleX = scaleX;
	m.scaleY = scaleY;

	return m;
    }

    public SizeRequest getSizeRequest() {
	float sx = scaleX, sy = scaleY;
	SizeRequest r = delegate.getSizeRequest();

	return SizeRequest.newInstance(r.minW*sx, r.natW*sx, r.maxW*sx,
				       r.minH*sy, r.natH*sy, r.maxH*sy);
    }

    public Lob layout(float w, float h) {
	Lob l = delegate.layout(w/scaleX, h/scaleY);
	return newInstance(l, scaleX, scaleY);
    }

    public Axis getLayoutableAxis() {
	return null;
    }

    public Lob layoutOneAxis(float size) {
	throw new UnsupportedOperationException();
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {
	int cs = scene.coords.scale(into, scaleX, scaleY);
	delegate.render(scene, cs, matchingParent, d, visible);
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new ScaleLob();
	    }
	};
}
