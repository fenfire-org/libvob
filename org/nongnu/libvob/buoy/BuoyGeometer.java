/*
BuoyGeometer.java
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
 * of buoys in a buoy system.
 * <p>
 * Objects implementing this interface may have some state: 
 * before calling buoyCS, the prepare method
 * should be called for the correct coordinate systems to make sure
 * the state is correct.
 */
public interface BuoyGeometer {

    /** Create any parameter coordinate systems depending on the surrounding
     * rectangle. 
     * This method **may** set state inside the BuoyGeometer.
     * @param into The matching parent and parent, giving the rectangle in
     *             which the whole buoyview and mainview are placed.
     * @param key The key to use for the *FIRST* coordsys if a hierarchy
     * 		  is 
     *            placed. This is so that several BuoyGeometers may
     *            be used in one coordinate system.
     * @param create Whether to create the coordinate systems or just set parameters
     *		in existing ones.
     */
    void prepare(VobScene vs, int into, Object key, boolean create);

    /** Create or set the main coordinate system size.
     * @param anchor The coordinate system of the anchor, or -1 if not applicable.
     * @param direction 1 for right, -1 for left.
     * @param key The key to use for the returned coordinate system
     *             in into. There may be others in between.
     * @param index The index of the buoy (counted from the anchor)
     * @param total The total number of buoys from the anchor
     *		(might be inaccurate)
     * @param w The width the buoy should be closest
     * to the focus. This should be the box size of the CS returned.
     * @param h The height the buoy should be closest
     * to the focus. This should be the box size of the CS returned.
     * @param scale The scale that should be applied to the w, h at
     * focus
     */
    int buoyCS(VobScene vs, int anchor, 
		int direction,
		Object key, 
		int index, int total,
		float w, float h, float scale);


}


