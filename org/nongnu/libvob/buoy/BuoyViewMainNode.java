/*
BuoyViewMainNode.java
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
import java.awt.event.MouseEvent;

/** An interface representing a single node of the buoy view.
 * This node must keep its own information about cursor location 
 * and handle keystrokes when it is a main view.
 */
public interface BuoyViewMainNode {

    /** Render this main view into the given box in the given vs.
     * This call has side effects: it should callback to
     * the BuoyLinkListener given to this BuoyViewMainNode
     * at creation. The callbacks indicate that buoy links should
     * be made.
     */
    void renderMain(VobScene vs, int into);

    /** A keystroke was not interpreted to the buoy framework and is
     * passed to this node as the main buoy.
     */
    void keystroke(String s);

    /** Whether a mouse click hit this main node.
     * This method is needed because the "into" coordsys given to renderMain
     * is instructive, not mandatory. For example, fisheye views can easily stray beyond
     * the edges of "into".
     * @param oldVobScene The vob scene
     * @param x The mouse event's x point.
     * @param y The mouse event's y point.
     * @param zout If non-null, the window z coord of the hit will be stored here.
     *             If some main nodes overlap, this can be used to determine
     *             which got the event. 
     */
    boolean hasMouseHit(VobScene oldVobScene, int x, int y, float[] zout);

    /** A mouse event was not interpreted to the buoy framework and is
     * passed to this node as the main buoy.
     * @return Whether the oldVobScene can be reused.
     *  	If true, this call has changed the coordsys parameters
     *  	inside oldVobScene and it can be just rendered.
     *  	If false, the buoy manager should regenerate the vobscene.
     */
    boolean mouse(VobMouseEvent e, VobScene oldVobScene);

}
