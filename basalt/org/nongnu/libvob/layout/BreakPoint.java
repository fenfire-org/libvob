/*
BreakPoint.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *
 *    This file is part of Libvob.
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
import org.nongnu.navidoc.util.Obs;

public class BreakPoint extends Stamp implements Breakable {

    protected Axis axis;
    protected float quality;
    protected Lob pre, in, post;
    

    public BreakPoint(Axis axis, Lob content, float quality, 
		      Lob pre, Lob in, Lob post) {
	super(content);
	this.axis = axis;
	this.quality = quality;
	this.pre = pre;
	this.in = in;
	this.post = post;
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content, pre, in, post };
    }
    protected Object clone(Object[] params) {
	return new BreakPoint(axis, (Lob)params[0], quality, 
			      (Lob)params[1], (Lob)params[2], (Lob)params[3]);
    }

    public float getBreakQuality(Axis axis) {
	if(axis==this.axis)
	    return quality;
	else if(content instanceof Breakable)
	    return ((Breakable)content).getBreakQuality(axis);
	else
	    return Float.NEGATIVE_INFINITY;
    }

    public Lob getPreBreakLob(Axis axis) {
	if(axis==this.axis)
	    return pre;
	else if(content instanceof Breakable)
	    return ((Breakable)content).getPreBreakLob(axis);
	else
	    return null;
    }

    public Lob getInBreakLob(Axis axis) {
	if(axis==this.axis)
	    return in;
	else if(content instanceof Breakable)
	    return ((Breakable)content).getInBreakLob(axis);
	else
	    return null;
    }

    public Lob getPostBreakLob(Axis axis) {
	if(axis==this.axis)
	    return post;
	else if(content instanceof Breakable)
	    return ((Breakable)content).getPostBreakLob(axis);
	else
	    return null;
    }
}
