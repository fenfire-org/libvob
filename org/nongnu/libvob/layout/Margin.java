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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;

/** A lob placing a margin around its contents.
 */
public class Margin extends AbstractMonoLob {

    protected float margin;

    public Margin(Lob content, float margin) {
	super(content);
	this.margin = margin;
    }

    public void setMargin(float margin) {
	this.margin = margin;
	chg();
    }

    protected Object clone(Object[] params) {
	return new Margin((Lob)params[0], margin);
    }

    public float getMinSize(Axis axis) {
	return content.getMinSize(axis) + 2*margin;
    }

    public float getNatSize(Axis axis) {
	return content.getNatSize(axis) + 2*margin;
    }

    public float getMaxSize(Axis axis) {
	return content.getMaxSize(axis) + 2*margin;
    }

    public boolean mouse(VobMouseEvent e, float x, float y) {
	return content.mouse(e, x - margin, y - margin);
    }

    public void setSize(float requestedWidth, float requestedHeight) {
	content.setSize(requestedWidth - 2*margin,
			requestedHeight - 2*margin);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float x, float y, float w, float h, float d,
		       boolean visible) {
	content.render(scene, into, matchingParent,
		       x + margin, y + margin, 
		       w - 2*margin, h - 2*margin, d,
		       visible);
    }
}
