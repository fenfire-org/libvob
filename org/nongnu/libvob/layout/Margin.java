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

    protected float left, right, top, bottom;

    public Margin(Lob content, float margin) {
	this(content, margin, margin, margin, margin);
    }

    public Margin(Lob content, float x, float y) {
	this(content, x, x, y, y);
    }

    public Margin(Lob content, float left, float right, 
		  float top, float bottom) {
	super(content);
	this.left = left;
	this.right = right;
	this.top = top;
	this.bottom = bottom;
    }

    protected Object clone(Object[] params) {
	return new Margin((Lob)params[0], left, right, top, bottom);
    }

    public float getMinSize(Axis axis) {
	if(axis == X) 
	    return content.getMinSize(axis) + left + right;
	else
	    return content.getMinSize(axis) + top + bottom;
    }

    public float getNatSize(Axis axis) {
	if(axis == X) 
	    return content.getNatSize(axis) + left + right;
	else
	    return content.getNatSize(axis) + top + bottom;
    }

    public float getMaxSize(Axis axis) {
	if(axis == X) 
	    return content.getMaxSize(axis) + left + right;
	else
	    return content.getMaxSize(axis) + top + bottom;
    }

    public boolean mouse(VobMouseEvent e, float x, float y,
			 float origX, float origY) {
	return content.mouse(e, x - left, y - top, origX - left, origY - top);
    }

    public void setSize(float requestedWidth, float requestedHeight) {
	content.setSize(requestedWidth - left - right,
			requestedHeight - top - bottom);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {
	int cs = scene.coords.box(into, left, top, 
				  w - left - right, h - top - bottom);

	content.render(scene, cs, matchingParent,
		       w - left - right, h - top - bottom, d,
		       visible);
    }
}
