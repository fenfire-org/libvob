/*
HChain.java
 *    
 *    Copyright (c) 2002, Benja Fallenstein and Tuomas Lukka
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
 * Written by Benja Fallenstein and Tuomas Lukka
 */
package org.nongnu.libvob.linebreaking;
import org.nongnu.libvob.*;

/** A chain with support for linebreaking operations.
 * This chain knows about where lines can be broken, at what cost, and
 * the amount of glue (as in TeX) on a given line.
 * @see Linebreaker, AbstractLinebreaker, SimpleLinebreaker
 */
public interface HChain {

    int GLUE_LENGTH = 0;
    int GLUE_STRETCH = 1;
    int GLUE_SHRINK = 2;

    /** Return the length of, i.e., the number of HBoxes in, this vob chain. */
    int length();

    /** Return the <code>n</code>th box in this chain. */
    HBox getBox(int n);

    /** Return one of the values of the glue
     * before the <code>n</code>th box in this chain.
     *  @param property <code>GLUE_LENGTH</code>, <code>GLUE_STRETCH</code>,
     *                  or <code>GLUE_SHRINK</code>.
     */
    float getGlue(int n, int property);
    
    /** Get the number of forced line breaks before the
     *  <code>n</code>th box.
     */
    int getBreaks(int n);

    void addBox(HBox box);
    void addGlue(float length, float stretch, float shrink);

    /** Add a forced line break at the current position.
     *  This is a "logical" line break, i.e. one in the text content,
     *  not one created by the line breaker. Line breakers should use
     *  <code>HBroken</code> to represent linebroken versions of an
     *  <code>HChain</code>.
     *  <p>
     *  Breaks don't have a 'length' in characters like
     *  HBoxes do. You may want to use a HBox.Null to represent
     *  the break character.
     */
    void addBreak();

    /** Get the height of a line given the index of the first and the
     *  index after the last box in that line. The height of the line is
     *  simply the maximum of the heights of the individual boxes at
     *  the given scale.
     */
    float getAscent(int start, int end, float scale);

    /** Get the depth of a line given the index of the first and the
     *  index after the last box in that line. The depth of the line is
     *  simply the maximum of the heights of the individual boxes at
     *  the given scale.
     */
    float getDescent(int start, int end, float scale);
    
    float getHeight(int start, int end, float scale);
}
