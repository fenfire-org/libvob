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
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.util.*;
import javolution.realtime.*;

/** A monolob that changes the requested size of its child.
 *  If one of the values (minimum, natural or maximum size)
 *  is < 0, the child's value is used. 
 *  However, if the natural size is not specified, but a minimal size
 *  is specified which is greater than the child's natural size,
 *  this lob's natural size is the specified minimal size.
 *  If the natural size is not specified but a maximum size is specified
 *  which is smaller than the child's natural size, this lob's
 *  natural size is the specified maximum size.
 */
public class RequestChangeLob extends AbstractDelegateLob {

    protected float minW, natW, maxW;
    protected float minH, natH, maxH;

    private RequestChangeLob() {}

    public static RequestChangeLob newInstance(
            Axis axis, Lob content, float min, float nat, float max) {

	if(axis == Axis.X) {
	    return newInstance(content, min, nat, max, -1, -1, -1);
	} else {
	    return newInstance(content, -1, -1, -1, min, nat, max);
	}
    }

    public static RequestChangeLob newInstance(
            Lob content, float minW,  float natW, float maxW,
	    float minH, float natH, float maxH) {

	RequestChangeLob l = (RequestChangeLob)FACTORY.object();
	l.delegate = content;
	l.minW = minW; l.natW = natW; l.maxW = maxW;
	l.minH = minH; l.natH = natH; l.maxH = maxH;
	return l;
    }

    public Lob wrap(Lob l) {
	return newInstance(l, minW, natW, maxW, minH, natH, maxH);
    }

    public Lob layout(float w, float h) {
	return delegate.layout(w, h);
    }

    public SizeRequest getSizeRequest() {
	SizeRequest r = delegate.getSizeRequest();
	SizeRequest l = SizeRequest.newInstance(0, 0, 0, 0, 0, 0);

	l.minW = (minW >= 0) ? minW : r.minW;
	l.natW = (natW >= 0) ? natW : r.natW;
	l.maxW = (maxW >= 0) ? maxW : r.maxW;

	if(natW < 0 && minW > l.natW) l.natW = minW;
	if(natW < 0 && maxW < l.natW) l.natW = maxW;

	l.minH = (minH >= 0) ? minH : r.minH;
	l.natH = (natH >= 0) ? natH : r.natH;
	l.maxH = (maxH >= 0) ? maxH : r.maxH;

	if(natH < 0 && minH > l.natH) l.natH = minH;
	if(natH < 0 && maxH < l.natH) l.natH = maxH;

	return l;
    }

    private static Factory FACTORY = new Factory() {
	    protected Object create() {
		return new RequestChangeLob();
	    }
	};
}
