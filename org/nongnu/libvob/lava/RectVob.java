/*
RectVob.java
 *    
 *    Copyright (c) 2003, Benja Fallenstein
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
package org.nongnu.libvob.lava;
import java.awt.Graphics;

/** A vob drawing the outline of a rectangle.
 */
public class RectVob extends AbstractVob {
    
    public final int width, height;
   
    public RectVob(int width, int height) {
	this.width = width; this.height = height;
    }

    public float getSize(Vob.Axis axis) {
	if(axis == X) return width;
	else return height;
    }

    public void render(Graphics g, RenderTraversal t) {
	g.drawRect(t.x, t.y, width, height);
    }
}
