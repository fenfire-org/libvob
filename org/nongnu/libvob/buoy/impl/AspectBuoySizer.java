/*
AspectBuoySizer.java
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

/** A BuoySizer that maintains the aspect ratio.
 * This BuoySizer shrinks buoys if one edge is larger than
 * the given size, by scaling down.
 * The box size is not touched..
 */
public class AspectBuoySizer implements BuoySizer{
    /** The maximum size.
     */
    float w, h;

    /** The scale to apply after minimizing.
     */
    float scale;

    public AspectBuoySizer(float w, float h, float scale) {
	this.w = w;
	this.h = h;
	this.scale = scale;
    }

    public float getBuoySize(float w, float h, float[] whout) {
	float sca = 1;
	float w0 = w, h0 = h;
	if(w > this.w) {
	    h /= w / this.w;
	    sca /= w / this.w;
	    w = this.w;
	}
	if(h > this.h) {
	    w /= h / this.h;
	    sca /= h / this.h;
	    h = this.h;
	}
	whout[0] = w0;
	whout[1] = h0;
	return sca * this.scale;
    }
}
