/*
SimpleTextPlaceable.java
 *    
 *    Copyright (c) 2004, Matti J. Katila
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


package org.nongnu.libvob.lava.placeable.impl;
import org.nongnu.libvob.vobs.*;
import org.nongnu.libvob.*;

/** Very simple text placeable
 */
public class SimpleTextPlaceable 
    implements org.nongnu.libvob.lava.placeable.Placeable {

    private final TextVob vob; 

    public SimpleTextPlaceable(String text, TextStyle style) {
	this(text, style, java.awt.Color.black);
    }
    public SimpleTextPlaceable(String text, TextStyle style, java.awt.Color color) {
	vob = new TextVob(style, text, false, color);
    }

    public void place(VobScene vs, int cs) {
	float [] size = new float[3];
	vs.coords.getSqSize(cs, size);
	int textCS = vs.scaleCS(cs, "CS", getHeight()+vob.getDepth(1), getHeight()+vob.getDepth(1));
	vs.put(vob, textCS);
    }

    public float getHeight() { return vob.getHeight(1); }
    public float getWidth() { return vob.getWidth(1); }
    public String toString() { return vob.text; }
    
}
