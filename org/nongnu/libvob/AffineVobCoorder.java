/*
AffineVobCoorder.java
 *    
 *    Copyright (c) 2000-2002, Tuomas Lukka
 *
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Tuomas Lukka
 */
package org.nongnu.libvob;

/** A set of coordinate systems for vobs.
 * <p>
 * Guarantees about rotate and scale!! Not same as affineCoordsys!
 */

public abstract class AffineVobCoorder extends VobCoorder {
    /** Default implementation using affineCoordsys.
     */
    public int ortho(int into, float depth,
		float x, float y, float sx, float sy) {
	return affine(into, depth, 
		x, y, sx, 0, 0, sy);
    }
    public void setOrthoParams(int cs, float depth,
		float x, float y, float sx, float sy) {
	setAffineParams(cs, depth, 
		x, y, sx, 0, 0, sy);
    }


    public abstract int affine(int into, float depth,
	    float x, float y, 
 		float xx, float xy, float yx, float yy);

    /** Get a subcs rotated clockwise.
     */
    public abstract int rotate(int into, float degrees);
    public abstract int scale(int into, float sx, float sy, float sz);

    public abstract void setAffineParams(
	    int cs, float depth, float cx, float cy, 
	    float x_x, float x_y, float y_x, float y_y) ;

    public abstract void setRotateParams(int into, float degrees);
    public abstract void setScaleParams(int into, float sx, float sy, float sz);

}



