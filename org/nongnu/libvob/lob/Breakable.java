/*
Breakable.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *
 *    This file is part of Libvob.
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
package org.nongnu.libvob.lob;

public interface Breakable extends Lob {

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

    /** Return the Lob to put before a break at this Lob */
    Lob getPreBreakLob(Axis axis);

    /** Return the Lob to put after a break at this Lob */
    Lob getPostBreakLob(Axis axis);

    /** Return the Lob to put between the two lines in a linebreak,
     *  the two columns in a column break, etc. This is used to implement
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
    Lob getInBreakLob(Axis axis);
}
