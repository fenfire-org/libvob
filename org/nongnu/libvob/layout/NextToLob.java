/*
NextToLob.java
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

/** Renders its content, and another lob next to it.
 */
public class NextToLob extends AbstractMonoLob {

    protected Axis axis;
    protected Lob lob;

    public NextToLob(Axis axis, Lob content, Lob lob) {
	super(content);
	this.axis = axis;
	this.lob = lob;
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content, lob };
    }
    protected Object clone(Object[] params) {
	return new NextToLob(axis, (Lob)params[0], (Lob)params[1]);
    }

    public boolean mouse(VobMouseEvent e, float x, float y) {
	if(axis == X) {
	    if(x < content.getNatSize(X)) 
		return super.mouse(e, x, y);
	    else
		return lob.mouse(e, x-content.getNatSize(X), y);
	} else {
	    if(y < content.getNatSize(X)) 
		return super.mouse(e, x, y);
	    else
		return lob.mouse(e, x, y-content.getNatSize(Y));
	}
    }

    public void setSize(float w, float h) {
	super.setSize(w, h);
	lob.setSize(lob.getNatSize(X), lob.getNatSize(Y));
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {

	super.render(scene, into, matchingParent, w, h, d, visible);
	
	float lw = lob.getNatSize(X), lh = lob.getNatSize(Y);

	int cs;
	if(axis == X)
	    cs = scene.coords.box(into, w, 0, lw, lh);
	else
	    cs = scene.coords.box(into, 0, h, lw, lh);

	lob.render(scene, cs, matchingParent, lw, lh, d, visible);
    }

    public boolean isLargerThanItSeems() {
	return true;
    }
}
