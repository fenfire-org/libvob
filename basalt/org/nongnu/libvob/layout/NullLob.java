/*
NullLob.java
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;

/** An infinitely stretchable lob with zero natural size that draws nothing.
 */
public class NullLob extends AbstractLob {
    public static final NullLob instance = new NullLob();

    protected Replaceable[] getParams() { return NO_PARAMS; }
    protected Object clone(Object[] params) { return this; }

    public void render(VobScene scene, int into, int matchingParent, 
		       float w, float h, float d,
		       boolean visible) {
    }

    public float getNatSize(Axis axis) { 
	return 0;
    }

    public float getMaxSize(Axis axis) {
	return Float.POSITIVE_INFINITY;
    }
}
