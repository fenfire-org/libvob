/*
NoGrowLob.java
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
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;

public class NoGrowLob extends AbstractDelegateLob {

    protected Axis axis;

    private NoGrowLob() {}

    public NoGrowLob newInstance(Lob content) {
	return newInstance(null, content); // don't grow on either axis
    }

    public NoGrowLob newInstance(Axis axis, Lob content) {
	NoGrowLob l = (NoGrowLob)FACTORY.object();
	l.axis = axis;
	l.delegate = content;
	return l;
    }

    public Lob wrap(Lob l) {
	return newInstance(axis, l);
    }

    public SizeRequest getSizeRequest() {
	SizeRequest r = delegate.getSizeRequest();
	SizeRequest s = SizeRequest.newInstance(r.minW, r.natW, r.maxW, 
						r.minH, r.natH, r.maxH);

	if(axis == Axis.X || axis == null) {
	    s.minW = r.natW; s.maxW = r.natW;
	}
	if(axis == Axis.Y || axis == null) {
	    s.minH = r.natH; s.maxH = r.natH;
	}

	return s;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new NoGrowLob();
	    }
	};
}
