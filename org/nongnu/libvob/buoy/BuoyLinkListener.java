/*
BuoyLinkListener.java
 *    
 *    Copyright (c) 2003, : Tuomas J. Lukka
 *    
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
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

/** A callback interface for placing buoy links.
 * @see BuoyViewConnector
 */
public interface BuoyLinkListener {

    /** A buoy link was found.
     * @param dir Usually -1 or 1 for left and right
     * @param anchorCS The coordinate system of the anchor inside the main view.
     * @param otherNodeType The node type to be rendered as the buoy
     * @param linkId The identity of the link. This is used for interpolation:
     * 			this object needs to be the same for the link
     * 			*in both directions*. No other restrictions,
     * 			and no-one should ever look inside a linkId object
     * 			so it's private to the BuoyViewConnector
     * @param otherAnchor The anchor inside the buoy. This will be passed to otherNodeType
     * 			for it to know what the object to be rendered actually is.
     */
    void link(int dir, int anchorCS, BuoyViewNodeType otherNodeType, 
	    Object linkId, Object otherAnchor);


    /** A buoy links were found.
     * See the other method for the rest of the parameters.
     * @param count is count of buoys from the anchor.
     */
    void link(int dir, int anchorCS, BuoyViewNodeType otherNode, 
	    Object linkId, Object otherAnchor, int count);
}
