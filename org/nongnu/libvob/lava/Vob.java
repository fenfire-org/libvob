/*
Vob.java
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

public interface Vob {

    final class Axis {
	private Axis() {}
    }

    Vob.Axis X = new Vob.Axis();
    Vob.Axis Y = new Vob.Axis();
    
    void render(Graphics g, RenderTraversal t);

    /** Get the allocated size of this graphic on the given axis.
     *  This is the size that the vob uses in layout, i.e.
     *  it will be allocated a box of this size in layout.
     *  It may actually draw outside this box; for example,
     *  a slanted character may draw outside its allocated box.
     */
    float getSize(Vob.Axis axis);

    /** Get the count of paths into this vob.
     */
    int getCount();

    /** Get the vob at a given path inside this vob.
     *  If index is 0, return this vob.
     */
    Vob getVob(int index);

    /** Transform the RenderTraversal by the transform
     *  associated with vob at the given path.
     */
    RenderTraversal transform(RenderTraversal t, int index);
}
