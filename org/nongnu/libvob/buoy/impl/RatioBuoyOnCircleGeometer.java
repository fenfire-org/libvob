/*
RatioBuoyOnCircleGeometer.java
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

/** A BuoyOnCircleGeometer that uses a constant ratio
 * of the box given to it.
 */
public class RatioBuoyOnCircleGeometer extends BuoyOnCircleGeometer {
    static float[] tmp = new float[2];

    /** The coordinates of the rectangle in (0,1)x(0,1).
     */
    float x, y, w, h;

    public RatioBuoyOnCircleGeometer(float x, float y, float w, float h) {
	this.x = x;
	this.y = y;
	this.w = w;
	this.h = h;
    }


    public void prepare(VobScene vs, int into, Object key, boolean create) {
	if(create) {
	    this.buoyinto = vs.orthoBoxCS(into, key, 1, 1, 1,  1, 1, 1, 1);
	} else {
	    this.buoyinto = vs.matcher.getCS(into, key);
	}
	vs.coords.getSqSize(into, tmp);

	// For now, must use ,1,1 because that's what the BuoyOnCircle2
	// expects
	vs.coords.setOrthoBoxParams(this.buoyinto,
		    0, x * tmp[0], y * tmp[1], 1, 1, w * tmp[0], h * tmp[1]);
    }
}
