/*
Lob.java
 *    
 *    Copyright (c) 2003-2005, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lob;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

/** A <em>layoutable</em> object.
 */
public interface Lob {

    SizeRequest getSizeRequest();

    Layout layout(float width, float height);


    /** If setting the size of this lob along one axis 
     *  changes the size request along the other axis,
     *  return the first axis. Else, return 'null.'
     *  <p>
     *  This is meant for use with linebreakers:
     *  A linebreaker with lines along the x-axis
     *  doesn't have a natural size request by itself,
     *  but if you give it a width, it can do the linebreaking,
     *  and then it has a natural height. In this example,
     *  getLayoutableAxis() would return Axis.X.
     *
     *  @see #layout(float)
     */
    Axis getLayoutableAxis();

    /** Fix the size of this lob along the axis returned by 
     *  getLayoutableAxis(). 
     *
     *  @return The lob with the fixed width/height.
     *  @throws UnsupportedOperationException if getLayoutableAxis() == null.
     *  @see #getLayoutableAxis()
     */
    Lob layout(float size) throws UnsupportedOperationException;




    /** A key has been pressed and this Lob is asked to handle it.
     *  @returns Whether the key was handled.
     */
    //boolean key(String key);

    /** This vob is asked to handle a mouse event.
     *  When the user is dragging, origX and origY are the coordinates
     *  where the dragging started. (At least normally, events should be
     *  dispatched to the lob containing the point (origX, origY), not
     *  necessarily the lob containing (x,y).)
     *  @returns Whether the mouse event was handled.
     */
    //boolean mouse(VobMouseEvent e, float x, float y, float origX, float origY);


    // List of decendants that can receive the focus.
    // XXX should be Model, I guess? or perhaps the list should always reflect
    // the current state -- but then we don't have Obs
    //List getFocusableLobs(); 

    //void setFocusModel(Model m);
}
