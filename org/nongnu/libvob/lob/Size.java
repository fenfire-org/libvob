/*
Size.java
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

/** The size of a thing -- like java.awt.Dimension, but with floats.
 */
public final class Size extends RealtimeObject {

    public Size() {}

    public float width, height;

    public static Size newInstance(float width, float height) {
	Size s = (Size)FACTORY.object();
	s.width = width; s.height = height;
	return s;
    }

    private static final Factory FACTORY = new Factory() {
	    protected Object create() {
		return new Size();
	    }
	};
}
