/*
Linebreaker.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka and Benja Fallenstein
 */
package org.nongnu.libvob.linebreaking;
import org.nongnu.libvob.*;
import java.util.*;

/** Interface to a linebreaking algorithm.
 *  A linebreaker is responsible for deciding how a
 *  <code>LinebreakableChain</code> should be broken into lines.
 *  @see org.nongnu.libvob.LinebreakableChain
 */

public interface Linebreaker {

    int GLUE_LENGTH = 0;
    int GLUE_STRETCH = 1;
    int GLUE_SHRINK = 2;

    /** Perform simple line-breaking.
     * @param lines An array of line widths in pixels
     * @param scales an array of the scales of the individual lines
     * @param ctoken The index of the cursor in this lbchain.
     * @param crow The index of the row in lines that the token
     *		indicated by ctoken should land on
     * @return A configuration of lines as a <code>Broken</code> object.
     */
    Broken breakLines(LinebreakableChain chain,
		      int[] lines, float[] scales, int ctoken, int crow);



    /** A configuration of lines with different scales.
     *  This is basically a list of lines, where each line has: the index
     *  of the first box in that line; the index after the last box in that
     *  line; and the scale at which that line shall be placed.
     *  <p>
     *  In addition, some other information is stored too... XXX doc
     */
    class Broken {
	LinebreakableChain ch;

	public int[] lineWidths;
	public float[] lineScales;

	public int firstLine;
	public int endLine;
	public int height;
	/** Difference between current and next line's baseline.
	 */
	public int[] lineHeights;
	public int[] tokenStarts;

	/** Put this configuration of lines into a <code>VobPlacer</code>.
	 *  This calls <code>LinebreakableChain.putLine</code> for each
	 *  individual line.
	 *  @param x The x coordinate of the upper left corner.
	 *  @param y The y coordinate of the upper left corner.
	 */
	public void putLines(VobScene into, int cs) {
	    int x = 0, y = 0, d = 0;

	    for(int line = firstLine; line < endLine; line ++) {
		y+= lineHeights[line];
		float scale;
		if(line >= lineScales.length)
		    scale = lineScales[lineScales.length-1];
		else
		    scale = lineScales[line];

		LinebreakingUtil.putLine(ch, into, cs, x, y,
					 lineWidths[line], d,
					 tokenStarts[line],
					 tokenStarts[line+1],
					 scale);
	    }

	}

    }
}
