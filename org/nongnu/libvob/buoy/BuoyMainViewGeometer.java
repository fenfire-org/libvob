/*
BuoyMainViewGeometer.java
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

package org.nongnu.libvob.buoy;
import org.nongnu.libvob.VobScene;

/** An interface for objects that determine the size and placement
 * of a main view
 * in a buoy system.
 */
public interface BuoyMainViewGeometer {
    /** Create or set the main coordinate system size.
     * @param into The matching parent and parent, giving the rectangle into
     *             which to place the cs.
     * @param key The key to use for the returned coordinate system
     *             in into. There may be others in between.
     * @param create Whether to create the coordinate systems or just set parameters
     *		in existing ones.
     * @return The coordinate system into which to place the main view.
     */
    int mainCS(VobScene vs, int into, Object key, boolean create);
}

