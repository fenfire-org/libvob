/*
RatioMainGeometer.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 */
/*
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.buoy.impl;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.buoy.*;

/** A BuoyMainViewGeometer that maintains a constant ratio
 * of the box given to it.
 */
public class RatioMainGeometer implements BuoyMainViewGeometer {
    static float[] tmp = new float[2];

    /** The coordinates of the rectangle in (0,1)x(0,1).
     */
    float x, y, w, h;

    public RatioMainGeometer(float x, float y, float w, float h) {
	this.x = x;
	this.y = y;
	this.w = w;
	this.h = h;
    }

    public int mainCS(VobScene vs, int into, Object key, boolean create) {
	int cs;
	if(create) {
	    cs = vs.orthoBoxCS(into, key, 1, 1, 1, 1, 1, 1, 1);
	} else {
	    cs = vs.matcher.getCS(into, key);
	}
	vs.coords.getSqSize(into, tmp);

	vs.coords.setOrthoBoxParams(cs,
		    0, x * tmp[0], y * tmp[1], 1, 1, w * tmp[0], h * tmp[1]);
	return cs;
    }

    public String toString() {
	return "["+super.toString()+": "+
		x+","+
		y+","+
		w+","+
		h+
		"]";
    }

}


