/*
RepeatLob.java
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;

/** A monolob that draws its child many times at its natural size
 *  to fill out the space assigned by its parent. Another way to say it
 *  is that this lob <em>tiles</em> its child to fill out its allocation
 *  (like with tiled background images).
 */
public class RepeatLob extends AbstractMonoLob {
    
    public static final Object KEY = new Object();

    public RepeatLob(Lob content) {
	super(content);
	setChildSize();
    }

    protected Object clone(Object[] params) {
	return new RepeatLob((Lob)params[0]);
    }

    public boolean mouse(VobMouseEvent e, float x, float y) {
	return false; // XXX should this propagate events?
    }

    public void setSize(float w, float h) {
    }

    public void chg() {
	super.chg();

	setChildSize();
    }

    protected void setChildSize() {
	float childW = content.getNatSize(X);
	float childH = content.getNatSize(Y);

	content.setSize(childW, childH);
    }

    public float getMinSize(Axis axis) {
	return 0;
    }
    public float getNatSize(Axis axis) {
	return 0;
    }
    public float getMaxSize(Axis axis) {
	return Float.POSITIVE_INFINITY;
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {

	float cw = content.getNatSize(X);
	float ch = content.getNatSize(Y);

	for(int i=0; i*cw<w; i++) {
	    for(int j=0; j*ch<h; j++) {

		int cs = scene.coords.box(into, i*cw, j*ch, cw, ch);
		content.render(scene, cs, cs, cw, ch, d, visible);
	    }
	}

    }
}
