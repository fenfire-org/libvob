/*
ScaleLob.java
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
import org.nongnu.libvob.*;

/** A lob placing its contents into a scaling cs.
 */
public class ScaleLob /*extends AbstractMonoLob*/ {

    /*
    protected float scale;

    public ScaleLob(Lob content, float scale) {
	super(content);
	this.scale = scale;
    }

    protected Object clone(Object[] params) {
	return new ScaleLob((Lob)params[0], scale);
    }

    public float getMinSize(Axis axis) {
	return scale*content.getMinSize(axis);
    }

    public float getNatSize(Axis axis) {
	return scale*content.getNatSize(axis);
    }

    public float getMaxSize(Axis axis) {
	return scale*content.getMaxSize(axis);
    }

    public boolean mouse(VobMouseEvent e, float x, float y, 
			 float origX, float origY) {
	return content.mouse(e, x/scale, y/scale, origX/scale, origY/scale);
    }

    public void setSize(float requestedWidth, float requestedHeight) {
	content.setSize(requestedWidth/scale, requestedHeight/scale);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {
	int cs = scene.coords.scale(into, scale, scale);

	content.render(scene, cs, matchingParent,
		       w/scale, h/scale, d, visible);
    }
    */
}
