/*
SizeRequest.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein
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
package org.nongnu.libvob.lob;
import javolution.realtime.*;

public final class SizeRequest {

    /** "Infinity". Can be used as the maximum size request of lobs
     *  that can be used at arbitrarily large sizes.
     *  <p>
     *  Float.POSITIVE_INFINITY is not used so that we can play a trick:
     *  if we want to distribute two thirds of the available space
     *  to one component and one third to another, we can use 2*INF
     *  as the size request of one and 1*INF as the size request of the other.
     */
    float INF = (float)Math.pow(2, 1024);


    private SizeRequest() {}

    public float minW, natW, maxW;
    public float minH, natH, maxH;

    public static SizeRequest newInstance(float minW, float natW, float maxW,
					  float minH, float natH, float maxH) {
	SizeRequest r = (SizeRequest)FACTORY.object();
	r.minW = minW; r.natW = natW; r.maxW = maxW;
	r.minH = minH; r.natH = natH; r.maxH = maxH;
	return r;
    }

    private static ObjectFactory FACTORY = new ObjectFactory() {
	    protected Object create() {
		return new SizeRequest();
	    }
	};
}
