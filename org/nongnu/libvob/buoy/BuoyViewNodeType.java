/*
BuoyViewNodeType.java
 *    
 *    Copyright (c) 2003, : Tuomas J. Lukka
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
 * Written by : Tuomas J. Lukka
 */
package org.nongnu.libvob.buoy;
import org.nongnu.libvob.*;

/** An interface representing a single node of the buoy view.
 */
public interface BuoyViewNodeType {

    /** Get the ideal size for this buoy.
     * @return An object that, if passed to renderBuoy, may help
     * 		performance a little.
     */
    Object getSize(Object linkId, Object anchor, float[] wh);

    /** Render portion of the view relevant to linkId and anchor
     * into the given box. This should not have any side effects,
     * beyond e.g. beginning to load images.
     * The box should be filled <b>completely</b> because it will
     * be used for sensing mouse clicks to this buoy
     * (XXX kludge! Will change later)
     * @param w Box width. Do not call getSqSize() on into, since
     * 		this buoy might be culled at generation time.
     * @param h Box height
     * @return The coordinate system of the anchor
     */
    int renderBuoy(VobScene vs, int into, float w, float h,
			Object linkId, Object anchor,
			Object cachedSize);

    /** Focus was set to the buoy -- create an object that knows
     * the user interface and motion in that space.
     */
    BuoyViewMainNode createMainNode(Object linkId, Object anchor);
}

