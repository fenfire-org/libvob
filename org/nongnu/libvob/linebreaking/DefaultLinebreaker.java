/*
SimpleLinebreaker.java
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

/** A not too capable, if not conceptually simple, implementation of the
 *  <code>Linebreaker</code> interface. <code>;-)</code>
 */
public class DefaultLinebreaker implements Linebreaker {

    // Trivial linebreaker implementation
    // XXX refactor
    public int chompTriv(LinebreakableChain ch, int start, int w, float scale) {
	double prev = 1000000000;
	int prevAccept = -1;
	for(int end=start+1; end<=ch.nboxes; end++) {
	    if(ch.glues[end*3 + GLUE_LENGTH] == 0) // Can we break here?
		continue;
	    double sf =
		LinebreakingUtil.stretchFactor(ch, start, end, scale, w);
	    // System.out.println("SF: "+start+" "+w+" "+scale+" "+sf);
	    if(sf < 0) {
		if((sf < -1 && end > start + 1) // prefer loose lines to overtight
		    || (prev < -sf)) // getting worse
		{
		    if(prevAccept >= 0)
			return prevAccept;
		    else return end;
		}
		else
		    return end;
	    }
	    prev = sf;
	    prevAccept = end;
	}
	return ch.nboxes;
    }

    /** Perform simple line-breaking.
     * @param lines An array of line widths in pixels
     * @param scales an array of the scales of the individual lines
     * @param ctoken The index of the cursor in this lbchain.
     * @param crow The index of the row in lines that the token
     *		indicated by ctoken should land on
     * @param into An array that contains the starts of the lines
     *	     in terms of tokens in this box.
     * @return A configuration of lines as a <code>Broken</code> object.
     */
    public Linebreaker.Broken breakLines(LinebreakableChain ch,
			     int[] lines, float[] scales, int ctoken, int crow) {
	int cur = 0;
	int line;
	Linebreaker.Broken bro = new Linebreaker.Broken();
	bro.ch = ch;
	bro.lineWidths = lines; bro.lineScales = scales;
	bro.tokenStarts = new int[lines.length+1];
	bro.firstLine = crow;
	bro.lineHeights = new int[lines.length+1];
	int height = 0;
	for(line = crow; line < lines.length &&
			    cur < ch.nboxes; line ++) {
	    bro.tokenStarts[line] = cur;
	    int w = lines[line];
	    float scale;
	    if(line >= scales.length) scale = scales[scales.length-1];
	    else scale = scales[line];
	    cur = chompTriv(ch, cur, w, scale);
	    int h = (int)ch.getAscent(bro.tokenStarts[line], cur, scale);
	    bro.lineHeights[line] += h;
	    height += h;
	    int d = (int)ch.getDescent(bro.tokenStarts[line], cur, scale);
	    bro.lineHeights[line+1] += d;
	    height += d;
	}
	bro.tokenStarts[line] = cur;
	bro.endLine = line; // one past end.
	bro.height = height;
	return bro;
    }
}



