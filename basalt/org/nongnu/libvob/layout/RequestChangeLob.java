/*
RequestChangeLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
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

/** A monolob that changes the requested size of its child.
 *  If one of the values (minimum, natural or maximum size)
 *  is Float.NaN ("not a number"), the child's value is used. 
 *  However, if the natural size is not specified, but a minimal size
 *  is specified which is greater than the child's natural size,
 *  this lob's natural size is the specified minimal size.
 *  If the natural size is not specified but a maximum size is specified
 *  which is smaller than the child's natural size, this lob's
 *  natural size is the specified maximum size.
 */
public class RequestChangeLob extends AbstractMonoLob {

    protected Axis axis;
    protected Model min, nat, max;

    public RequestChangeLob(Axis axis, Lob content,
			    float min, float nat, float max) {
	this(axis, content,
	     new FloatModel(min), new FloatModel(nat), new FloatModel(max));
    }

    public RequestChangeLob(Axis axis, Lob content,
			    Model min, Model nat, Model max) {
	super(content); this.axis = axis; 
	this.min = min; this.nat = nat; this.max = max;
	min.addObs(this); nat.addObs(this); max.addObs(this);
    }

    public RequestChangeLob(Lob content, float minx, float natx, float maxx,
			    float miny, float naty, float maxy) {
	this(Y, new RequestChangeLob(X, content, minx, natx, maxx),
	     miny, naty, maxy);
    }

    public RequestChangeLob(Lob content, Model minx, Model natx, Model maxx,
			    Model miny, Model naty, Model maxy) {
	this(Y, new RequestChangeLob(X, content, minx, natx, maxx),
	     miny, naty, maxy);
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content, min, nat, max };
    }
    protected Object clone(Object[] params) {
	return new RequestChangeLob(axis, (Lob)params[0], (Model)params[1],
				    (Model)params[2], (Model)params[3]);
    }

    public float getMinSize(Axis axis) {
	if(axis!=this.axis || Float.isNaN(min.getFloat()))
	    return content.getMinSize(axis);
	else
	    return min.getFloat();
    }

    public float getNatSize(Axis axis) { 
	if(axis!=this.axis) {
	    return content.getNatSize(axis);
	} else if(Float.isNaN(nat.getFloat())) {
	    float _min = min.getFloat(), _max = max.getFloat();
	    float size = content.getNatSize(axis);
	    if(size < _min) size = _min; // these are false 
	    if(size > _max) size = _max; // if size is NaN
	    return size;
	} else {
	    return nat.getFloat();
	}
    }

    public float getMaxSize(Axis axis) {
	if(axis!=this.axis || Float.isNaN(max.getFloat()))
	    return content.getMaxSize(axis);
	else
	    return max.getFloat();
    }
}
