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
package org.nongnu.libvob.layout;
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
public class AlignLob extends AbstractMonoLob {

    protected float childX, childY;
    protected float parentX, parentY;

    public AlignLob(Lob content,
		    float childX, float childY, float parentX, float parentY) {
	super(content);
	this.childX = childX; this.childY = childY;
	this.parentX = parentX; this.parentY = parentY;

	setChildSize();
    }

    protected Object clone(Object[] params) {
	return new AlignLob((Lob)params[0], childX, childY, parentX, parentY);
    }

    protected float childW, childH, parentW, parentH;

    public boolean mouse(VobMouseEvent e, float x, float y) {
	return content.mouse(e, x - (parentX*parentW - childX*childW),
			        y - (parentY*parentH - childY*childH));
    }

    public void setSize(float w, float h) {
	parentW = w; parentH = h;

	content.setSize(childW, childH);
    }

    public void chg() {
	super.chg();

	setChildSize();
    }

    protected void setChildSize() {
	childW = content.getNatSize(X);
	childH = content.getNatSize(Y);

	content.setSize(childW, childH);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {

	float x = parentX*w - childX*childW;
	float y = parentY*h - childY*childH;

	int cs = scene.coords.translate(into, x, y);

	content.render(scene, cs, matchingParent, 
		       childW, childH, d, visible);
    }
}
