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
import javolution.realtime.*;
import java.util.*;

/** A <em>layoutable</em> object.
 */
public interface Lob extends Realtime {

    /** Go through the hierarchy of delegate lobs to see whether
     *  we can find one that implements the given interface.
     *  The intent is that we can extend the Lob interface (see,
     *  for example, the Breakable interface), and still wrap
     *  an arbitrary delegating lob around an implementation
     *  of the extended interface; e.g., if you wrap a KeyLob,
     *  which doesn't implement Breakable, around a BreakPoint, which does,
     *  keyLob.getInterface(Breakable.class) will return the BreakPoint.
     *  <p>
     *  The search stops at lobs with multiple children; we only
     *  go through the "wrapper" lobs that have only a single child.
     *  (An exception is Between, which has three children, but treats
     *  the 'middle' child as the special one that is being wrapped.)
     *  <p>
     *  If no implementation of the interface is found, return null.
     */
    Lob getImplementation(Class iface);


    SizeRequest getSizeRequest();

    /** Returns a renderable lob with fixed size.
     *  The returned lob will have size request minW == natW == maxW == width
     *  and the same for height.
     */
    Lob layout(float width, float height);


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
    Lob layoutOneAxis(float size) throws UnsupportedOperationException;



    /**
     *  @param visible Whether to put lobs into the coordinate systems.
     *         If false, the tree of coordinate systems is created,
     *         but no lobs are put into them.
     *  @param d depth -- is to the z-axis like width is to the x-axis 
     *           and like height is to the y-axis
     *  @throws UnsupportedOperationException if this lob isn't fully
     *          layouted yet, i.e., if the size hasn't been fixed yet.
     *          Lobs returned by layout() never throw this.
     */
    void render(VobScene scene, int into, int matchingParent, 
		float d, boolean visible) throws UnsupportedOperationException;




    /** A key has been pressed and this Lob is asked to handle it.
     *  @returns Whether the key was handled.
     */
    boolean key(String key);

    /** This vob is asked to handle a mouse event.
     *  @returns Whether the mouse event was handled.
     */
    boolean mouse(VobMouseEvent e, VobScene scene, int cs, float x, float y);


    // List of decendants that can receive the focus.
    // XXX should be Model, I guess? or perhaps the list should always reflect
    // the current state -- but then we don't have Obs
    //List getFocusableLobs(); 

    //void setFocusModel(Model m);
}
