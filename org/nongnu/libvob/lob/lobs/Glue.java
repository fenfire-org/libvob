/*
Glue.java
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

public class Glue extends NullLob {

    protected float minX, natX, maxX;
    protected float minY, natY, maxY;

    public static Glue newInstance(Axis axis, float min, float nat, float max) {
	Axis X = Axis.X, Y = Axis.Y;
	float inf = SizeRequest.INF;

	return newInstance(axis==X ? min : 0, 
			   axis==X ? nat : 0, 
			   axis==X ? max : inf, 
			   
			   axis==Y ? min : 0, 
			   axis==Y ? nat : 0, 
			   axis==Y ? max : inf);
    }

    public static Glue newInstance(float minX, float natX, float maxX,
				   float minY, float natY, float maxY) {
	Glue g = (Glue)FACTORY.object();
	g.minX = minX; g.natX = natX; g.maxX = maxX;
	g.minY = minY; g.natY = natY; g.maxY = maxY; 
	return g;
    }

    public SizeRequest getSizeRequest() {
	return SizeRequest.newInstance(minX, natX, maxX, minY, natY, maxY);
    }

    private static final Factory FACTORY = new Factory() {
	    protected Object create() {
		return new Glue();
	    }
	};
}
