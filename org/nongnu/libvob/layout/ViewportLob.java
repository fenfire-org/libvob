/*   
ViewportLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
 *
 *    This file is part of Fenfire.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
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
import org.nongnu.navidoc.util.Obs;

public class ViewportLob extends AbstractMonoLob {

    protected Axis axis;
    protected Model positionModel;
    protected Model alignWhenAllShown;

    protected float width, height;

    protected Model visibleFractionModel;

    public ViewportLob(Axis axis, Lob content, Model positionModel) {
	this(axis, content, positionModel, null);
    }

    /**
     *  alignWhenAllShown: where to show the content when its smaller than
     *  the visible box; ranges from 0 to 1 (fraction inside the visible box).
     */
    public ViewportLob(Axis axis, Lob content, Model positionModel,
		       Model alignWhenAllShown) {
	super(content);
	this.axis = axis;
	this.positionModel = positionModel;
	this.alignWhenAllShown = alignWhenAllShown;
	this.visibleFractionModel = new FloatModel();
    }

    protected Replaceable[] getParams() { 
	return new Replaceable[] { content, positionModel, alignWhenAllShown };
    }
    protected Object clone(Object[] params) {
	ViewportLob ret =  new ViewportLob(axis, (Lob)params[0], (Model)params[1], (Model)params[2]);
	ret.visibleFractionModel = this.visibleFractionModel;
	return ret;
    }

    public Model getPositionModel() { return positionModel; }

    public Model getVisibleFractionModel() {
	return visibleFractionModel;
    }

    public void setSize(float requestedWidth, float requestedHeight) {
	if(axis == X) {
	    content.setSize(Float.NaN, requestedHeight);
	    content.setSize(content.getNatSize(X), requestedHeight);
	} else {
	    content.setSize(requestedWidth, Float.NaN);
	    content.setSize(requestedWidth, content.getNatSize(Y));
	}

	width = requestedWidth;
	height = requestedHeight;

	visibleFractionModel.setFloat(axis.coord(width,height)/content.getNatSize(axis));
    }

    protected float getScroll() {
	float pos = positionModel.getFloat();
	float lobSize = content.getNatSize(axis);
	float viewportSize = (axis==X) ? width : height;
	
	float scroll;
	if(lobSize < viewportSize) {
	    if(alignWhenAllShown != null)
		scroll = alignWhenAllShown.getFloat()*viewportSize - pos;
	    else
		scroll = 0;
	} else if(pos < viewportSize/2)
	    scroll = 0;
	else if(pos > lobSize-(viewportSize/2))
	    scroll = viewportSize - lobSize;
	else
	    scroll = viewportSize/2 - pos;

	return scroll;
    }

    public boolean mouse(VobMouseEvent e, float x, float y,
			 float origX, float origY) {
	if(axis == X)
	    return content.mouse(e, x-getScroll(), y, 
				 origX-getScroll(), origY);
	else
	    return content.mouse(e, x, y-getScroll(),
				 origX, origY-getScroll());
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {

	if(axis == X) {
	    int cs = scene.coords.translate(into, getScroll(), 0);
	    super.render(scene, cs, matchingParent,
			 content.getNatSize(X), h, d, visible);

	} else {
	    int cs = scene.coords.translate(into, 0, getScroll());
	    super.render(scene, cs, matchingParent,
			 w, content.getNatSize(Y), d, visible);
	}
    }
}
