/*
BuoyManager.java
 *    
 *    Copyright (c) 2003, Matti J. Katila
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


package org.nongnu.libvob.buoy;
import org.nongnu.libvob.VobScene;

/** An interface for managing the buoy links, focus main node and 
 * the construction of base coordinate systems related to buoy view.
 * Basicly the manager constructs activated coordinate systems 
 * for focus, main node, and for every buoy links. 
 * <p>
 * The information of buoys, i.e. buoy anchor, node type etc.,
 * are needed afterwards when user clicks any of the buoys to 
 * perform an action.
 */
public interface BuoyManager extends BuoyLinkListener {

    /** Represantion of anchor object rendered with some node type.
     * Buoy implementation should be memory efficient
     * and this implies that it's not clear that reference of buoy, 
     * which you asked from BuoyManager, is same after new draw is done.
     */
    public interface Buoy {

	/** Get the node type of this buoy.
	 */
	BuoyViewNodeType getNodeType();

	/** Get the link identification which is 
	 * used in interpolations. The identification must be unique 
	 * to get proper interpolation.
	 */
	Object getLinkId();


	/** Get the anchor inside of this buoy. 
	 * The anchor is the object which was the reason to render this buoy.
	 */
	Object getBuoyAnchor();


	/** Get the coordinate system of this buoy.
	 * The cs is given and activated by BuoyManager and the node type
	 * of this Buoy renders into it.
	 */
	int getBuoyCS();

	/** Get the direction of this buoy. If direction > 0 
	 * buoy is on the rigth side, else the buoy is on the left side.
	 */
	int getDirection();
    }


    /** Moves the focus to given buoy with interpolation from old buoy to new focus.
     * If the Buoy is not from this BuoyManager an error is thrown.
     * The old focus view port should be interpolated to new buoy.
     */
    void moveFocusTo(Buoy buoy);

    /** Draw the focus main node. While rendering BuoyViewMainNode 
     * BuoyManager get buoys with LinkListener's call back 
     * interface which it implements. The buoys must not be rendered
     * while call back linking but after every link, because 
     * main node might render into stenciled buffer.
     * @param into The coordinate system where the focus is drawn.
     */
    void draw(VobScene vs, int into);

    /** Returns the focused main node.
     */
    BuoyViewMainNode getMainNode();

    /** Replace the main node with one which is given.
     */
    void replaceMainNodeWith(BuoyViewMainNode mainNode);

    /** Return the buoy found by coordinate system.
     * To found the buoy which is clicked, ask activated
     * coordinate system from VobScene. If coordinate system
     * is not constructed in this BuoyManager, null is returned.
     */
    Buoy getBuoy(int cs);

}

