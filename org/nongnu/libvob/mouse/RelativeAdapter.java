/*
RelativeAdapter.java
 *    
 *    Copyright (c) 2003, Matti J. Katila
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
 * Written by Matti J. Katila
 */

package org.nongnu.libvob.mouse;

/** Adapts MouseDragListener to RelativeMouseDragListener 
 */
public class RelativeAdapter implements RelativeMouseDragListener,
					MouseDragListener {

    private float x_, y_;
    protected float multiplier = 1.0f;

    public void endDrag(int x, int y) {
	//doIt(x,y);
    }
    public void startDrag(int x, int y) {
	x_=x; y_=y;
    }
    public void drag(int x, int y) {
	doIt(x,y);
    }

    private void doIt(int x, int y) {
	float dx=(x-x_)*multiplier, 
	    dy=(y-y_)*multiplier;
	changedRelative(dx,dy);
	x_=x; y_=y;
    }

    public void changedRelative(float x, float y) { 
	throw new Error("unimplemented"); 
    }
}
