/*
SimpleLinebreaker.java
 *
 *    Copyright (c) 2002, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.linebreaking;
import org.nongnu.libvob.*;

/** A first-match linebreaker.
 *  XXX This assumes you can break after any box. That's wrong: you can't
 *  break after a box with no glue... This makes things more difficult :(
 */
public class SimpleLinebreaker {
    public HBroken breakLines(HChain ch, float lineWidth, float scale) {
        HBroken br = new HBroken(ch, scale);
	float x = scale * ch.getGlue(0, ch.GLUE_LENGTH);

	for(int i=0; i<ch.length(); i++) {
	    HBox box = ch.getBox(i);
	    float w = box.getWidth(scale) +
	              scale * ch.getGlue(i+1, ch.GLUE_LENGTH);

	    for(int j=0; j<ch.getBreaks(i); j++) {
	        br.addBreak(i);
		x = 0;
	    }

	    if(x + w > lineWidth) {
                br.addBreak(i);
                x = 0;
	    }

	    x += w;
	}

	for(int i=0; i<ch.getBreaks(ch.length()); i++)
	    br.addBreak(ch.length());

	return br;
    }
}
