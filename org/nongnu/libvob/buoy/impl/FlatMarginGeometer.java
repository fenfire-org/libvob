/*
FlatMarginGeometer.java
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

/** A Buoy geometer that places all buoys 
 * (without regard to anchor) in the margin,
 * based on the "index" given to it.
 * This is currently going to be used
 * for the TreeTime viewing.
 */
public class FlatMarginGeometer implements BuoyGeometer {

    /** The x-coordinate inside the surrounding rectangle that
     * the margin buoys' centers should be on.
     */
    public float marginfraction = 0.03f;

    /** The y offset between the buoys.
     */
    public float yfraction = .1f;

    private int into;
    private float[] size = new float[2];

    public void prepare(VobScene vs, int into, Object key, boolean create) {
	this.into = into;
	vs.coords.getSqSize(into, size);
    }

    public int buoyCS(VobScene vs, int anchor, 
		int direction,
		Object key, 
		int index, int total,
		float w, float h, float scale) {

	// Shift the buoy based on the number given
	float shift = index;
	if(index > 0)
	    if(index % 2 == 1) {
		shift += 1;
		shift *= -1;
	    }
	shift *= .5;

	// Calculate buoy center
	float ycenter = (.5f + yfraction * shift) * size[1];

	float xcenter = marginfraction * size[0];
	if(direction > 0) xcenter = size[0] - xcenter;

	// Change scale -- we always fit exactly to the given fraction
	float desiredw = size[0] * marginfraction * 2;
	if(scale * w > desiredw)
	    scale = desiredw / w;

	// Create coordinate system
	int buoyCS = vs.orthoBoxCS(
		this.into, key, -50, 
		xcenter - w/2 * scale, 
		ycenter - h/2 * scale, 
		scale, scale,
		w, h);
	return buoyCS;

    }


}
