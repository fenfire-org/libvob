
==========================================================================
PEG single_focus_manager--mudyc: Single Buoy Manager - One Focus and Buoys
==========================================================================

:Authors:  Matti J. Katila
:Date-Created: 2003-09-05
:Last-Modified: $Date: 2003/09/18 11:13:41 $
:Revision: $Revision: 1.6 $
:Status:   Accepted
:Stakeholders: mudyc, tjl, benja
:Scope:    Major
:Type:     Interface


The buoy oriented interface is in real use and must be stabilized.
Here is the proposed interface.


Issues
======

..

Changes
=======

We define the following interface.

::

    /** An interface for managing the buoy links, focus main node and 
     * the construction of base coordinate systems related to buoy view.
     * Basicly the manager constructs activated coordinate systems for focus, main node,
     * and for every buoy links. The information of buoys, i.e. buoy anchor, node type etc.,
     * are needed afterwards when user clicks any of the buoys to perform an action.
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

        /** Return the buoy found by coordinate system.
         * To found the buoy which is clicked, ask activated
         * coordinate system from VobScene. If coordinate system
         * is not constructed in this BuoyManager, null is returned.
         */
        Buoy getBuoy(int cs);

    }


