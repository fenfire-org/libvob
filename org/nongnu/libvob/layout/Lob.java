/*
Lob.java
 *    
 *    Copyright (c) 2003-2004, Benja Fallenstein
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

/** A <em>layoutable</em> object.
 */
public interface Lob extends Observable, Replaceable {

    final class Axis {
	private Axis() { }
	public Axis other() { return (this==X) ? Y : X; }
	public float coord(float x, float y) { return (this==X) ? x : y; }
	public String toString() { return (this==X) ? "Axis(X)" : "Axis(Y)"; }
    }

    Lob.Axis X = new Lob.Axis();
    Lob.Axis Y = new Lob.Axis();

    final class Rect {
	public float x, y, w, h;
    }

    /**
     *  @param visible Whether to put lobs into the coordinate systems.
     *         If false, the tree of coordinate systems is created,
     *         but no lobs are put into them.
     *  @param d depth -- is to the z-axis like width is to the x-axis 
     *           and like height is to the y-axis
     */
    void render(VobScene scene, int into, int matchingParent, 
		float w, float h, float d,
		boolean visible) 
	throws UnknownSizeError;


    /** A key has been pressed and this Lob is asked to handle it.
     *  @returns Whether the key was handled.
     */
    boolean key(String key);

    /** This vob is asked to handle a mouse event.
     *  @returns Whether the mouse event was handled.
     */
    boolean mouse(VobMouseEvent e, float x, float y);


    // List of decendants that can receive the focus.
    // XXX should be Model, I guess? or perhaps the list should always reflect
    // the current state -- but then we don't have Obs
    List getFocusableLobs(); 

    void setFocusModel(Model m);


    /** The natural size. */
    float getNatSize(Lob.Axis axis) throws UnknownSizeError;
    /** The minimum size. */
    float getMinSize(Lob.Axis axis) throws UnknownSizeError;
    /** The maximum size. */
    float getMaxSize(Lob.Axis axis) throws UnknownSizeError;

    void setSize(float width, float height);



    boolean isLargerThanItSeems();
}
