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
import org.nongnu.libvob.fn.*;
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



    /** Add a lob to a sequence (hbox, vbox, linebreaker...).
     *  If this is a sequence, the lob is added to the underlying list
     *  through its add() method, which may throw an 
     *  UnsupportedOperationException. If this is a
     *  delegate lob, add() is called on the child lob.
     *  If this is neither a sequence nor a delegate lob, an 
     *  UnsupportedOperationException is thrown.
     *  <p>
     *  If no exception is thrown, and the lob caches some information,
     *  like size information or the positions of line breaks,
     *  then this information must be re-calculated.
     *  <p>
     *  This is a convenience method for building hboxes/vboxes etc. 
     *  more easily in Java code.
     */
    void add(Lob lob) throws UnsupportedOperationException;



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
    List getFocusableLobs();



    /**
     *  For infinitely good or infinitely bad breaks.
     */
    float INF = SizeRequest.INF;



    /** Return a value representing how good an idea it would be
     *  to insert a break (line break, page break, etc., depending
     *  on what kind of sequence this is inserted to) in place of this Lob.
     *  A value of zero means the break quality of a normal space
     *  between two words; positive infinity means that a break must
     *  be inserted at this point; negative infinity means that no break
     *  is allowed here.
     */
    float getBreakQuality(Axis axis);

    /** Return the Lob to put before or after a break at this Lob,
     *  or between the two lines in a linebreak.
     *  <p>
     *  dir is < 0 for the lob before the break, > 0 for the lob
     *  after the break, and == 0 for the lob "in" the break.
     *  <p>
     *  The "in the break" lob is used to implement
     *  line spacing, paragraph spacing, column spacing, inserting
     *  a particular lob between two paragraphs, and so on.
     *  <p>
     *  For a fancy example, consider a section boundary in a novel.
     *  If the end of one section and the beginning of the next are
     *  on the same page, you would render the boundary simply as a space
     *  between the sections. If they are on different pages, you want to
     *  insert a star on the next page to tell the reader that there
     *  is a section boundary here. (XXX actually it should go on the
     *  page before the break if it fits there; that's not yet possible
     *  with the current model, need to think about that.) So what you do
     *  is you create a "must break" break point (to end the last line
     *  of the last paragraph) whose InBreakLob is another break point.
     *  This second break point is a vertical glue if not broken
     *  (the section separator), but if broken, its PostBreakLob
     *  is a line with the star that signals a new section.
     *  <p>
     *  This function may return null if no lob should be placed
     *  "between" the two broken parts (e.g. in page breaking, this would
     *  insert an additional page, which you don't want).
     */
    Lob getBreakLob(Axis axis, int dir);
    
}
