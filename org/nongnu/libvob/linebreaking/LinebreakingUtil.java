/*
LinebreakingUtil.java
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
 * Written by Tuomas Lukka
 */
package org.nongnu.libvob.linebreaking;
import org.nongnu.libvob.*;
import java.util.*;

/** Some more complex helper functions that can be used by linebreakers.
 */

public class LinebreakingUtil {

    public static final int GLUE_LENGTH = 0;
    public static final int GLUE_STRETCH = 1;
    public static final int GLUE_SHRINK = 2;

    /** Compute the stretch factor for a line given the part of the chain that
     ** will form the line, and the width of the line.
     *  XXX This is a GUESS, I don't really know what this thing does.
     *  Am I right? --Benja
     */
    public static double stretchFactor(LinebreakableChain ch,
				       int start, int end,
				       float scale, int width) {
	int wid = 0;   // Width
	int gwid = 0;
	int str = 0;   // Stretch
	int shr = 0;   // Shrink

	float[] glues = ch.glues;
	for(int i=start; i<end; i++) {
	    wid += ch.boxes[i].getWidth(scale);
	    if(i > start /* || i == 0 */ ) { // XXX Think out and test
		gwid += glues[i*3 + GLUE_LENGTH];
		str += glues[i*3 + GLUE_STRETCH];
		shr += glues[i*3 + GLUE_SHRINK];
	    }
	}

	gwid *= scale; gwid /= 1000;
	str *= scale; str /= 1000;
	shr *= scale; shr /= 1000;

	wid += gwid;
	if(wid < width) {
	    if(shr == 0) return 1000;
	    return (width-wid) / (double)str;
	} else if(wid > width) {
	    if(str == 0) return -1000;
	    return (width-wid) / (double)shr;
	}
	return 0;
    }

    /** Get the badness of fitting the given tokens
     * into the width.
     * XXX Not yet complete information: also need prevention
     * of neighbouring loose lines etc.
     * See TeX source, section 851, 859.
     */
    public static int badness(LinebreakableChain ch,
		              int start, int end, int scale, int width) {
	int str = (int) (1000 * stretchFactor(ch, start, end, scale, width));
	if(str < 0) return -str;
	return str;
    }

    /** Put a line into the given vobPlacer.
     * @param x The x coordinate of the leftmost point
     *			of the baseline.
     * @param y The y coordinate of the leftmost point
     *			of the baseline.
     * @param end The index of the first 
     * 			box to place on the line.
     * @param end The index after the last
     * 			box to place on the line.
     * @param cs coordsys to place stuff into
     */
    public static void putLine(LinebreakableChain ch, VobScene into, int cs,
			       int x, int y, int w, int d,
			       int start, int end, float scale) {
	double sf = stretchFactor(ch, start, end, scale, w);
	int wid = 0;
	double over = 0;

	HBox[] boxes = ch.boxes;
	float[] glues = ch.glues;
	for(int i=start; i<end; i++) {
	    int curwid = (int)boxes[i].getWidth(scale);
	    int curhei = (int)boxes[i].getHeight(scale);
	    int curdep = (int)boxes[i].getDepth(scale);

	    double dw = curwid;
	    dw += glues[3 * (i+1) + GLUE_LENGTH];
	    if(i < end-1)
		dw += (sf > 0 ? sf * glues[3 * (i+1) + GLUE_STRETCH] :
			 sf * glues[3 * (i+1) + GLUE_SHRINK])
		         // XXX make testcase of both loose and tight lines
			+ over;

	    curwid = (int)dw;
	    over = dw - curwid;

	    int boxcs = into.orthoCS(cs, boxes[i].getKey(), d,
		    x + wid, y - curhei, 2, 2); //curwid, curhei + curdep);

	    boxes[i].place(into, boxcs, scale);
	    wid += curwid;
	}
    }
}



