/*
HBroken.java
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

/** A way to break an <code>HChain</code> into lines.
 *  Currently simplistic, not fit for billowing. Simply lists
 *  all the positions where there's a line break.
 */
public class HBroken {
    protected HChain chain;
    protected float scale;
    protected int[] breaks = new int[10];
    protected int length;

    public HBroken(HChain chain, float scale) {
        this.chain = chain;
	this.scale = scale;
    }

    /** Add a break.
     *  pos == hbox before which to break
     */
    public void addBreak(int pos) {
        try {
	    breaks[length] = pos;
        } catch(ArrayIndexOutOfBoundsException e) {
	    int[] nbreaks = new int[length * 2];
	    System.arraycopy(breaks, 0, nbreaks, 0, breaks.length);
	    breaks = nbreaks;
	    breaks[length] = pos;
	}

	length++;
    }

    public void put(VobScene vs, int coordsys) {
	/*
	  float [] boxwh = new float[2];
	  vs.coords.getSqSize(coordsys, boxwh);
	  float w = boxwh[0], h = boxwh[1];
	*/

        int x = 0, y = 0;

        for(int i=0; i<=length; i++) {
	    int from, to;

	    if(i>0)
	        from = breaks[i-1];
	    else
	        from = 0;

	    if(i<length)
	        to = breaks[i];
	    else
	        to = chain.length();

	    for(int j=from; j<to; j++) {
	        HBox box = chain.getBox(j);
		Object key = box.getKey();
		
		/* int cs = vs.orthoBoxCS(coordsys, key, 0, x, y, 1, 1, w, h); */
		int cs = vs.orthoCS(coordsys, key, 0, x, y, 1, 1);
		box.place(vs, cs, scale);

		x += box.getWidth(scale);
	    }

	    x = 0;
	    y += chain.getHeight(from, to, scale);
	}
    }

    /** Get the ith line start/end-- counting
     *  the beginning and end of the chain.
     */
    protected int getLineDivision(int i) {
	if(i == 0) 
	    return 0;
	else if(i <= length)
	    return breaks[i-1];
	else
	    return chain.length();
    }

    public float getHeight() {
	return getLineOffset(length);
    }
    
    /** Get the Y position under a given line.
     *  If line < 0, return 0.
     */
    public float getLineOffset(int line) {
	if(line < 0) return 0;

        float h = 0;
	if(line > length) throw new IndexOutOfBoundsException(""+line);

	for(int i=0; i<=line; i++)
	    h += chain.getHeight(getLineDivision(i),
				 getLineDivision(i+1), scale);

	return h;
    }

    /** Get the number of lines.
     */
    public int getLineCount() {
	return length+1;
    }

    public float getLineWidth(int line) {
	if(line >= (length+1))
	    throw new IndexOutOfBoundsException(line+"; line count is "+
						(length+1));

	float result = 0;
	int start = getLineDivision(line),
	    end   = getLineDivision(line+1);

	for(int i=start; i<end; i++)
	    result += chain.getBox(i).getWidth(scale);

	return result;
    }

    /** Get the line a given character is at.
     *  Additionally, if passed an array
     *  as the second parameter, return
     *  the x offset before the character.
     *  Passing a pos one more than the last
     *  character is valid.
     */
    public int getLine(int pos, float[] xoffs) {
	int i=0, n=0;
	while(true) {
	    if(i >= chain.length()) {
		if(xoffs != null) {
		    xoffs[0] = getLineWidth(length);
		}
		return length;
	    }

	    int l = chain.getBox(i).getLength();
	    if(n+l > pos) break;
	    n += l;
	    i++;
	}

	int line=0;
	while(getLineDivision(line+1) <= i) line++;

	if(xoffs != null) {
	    xoffs[0] = 0;
	    for(int j=getLineDivision(line); j<i; j++)
		xoffs[0] += chain.getBox(j).getWidth(scale);

	    xoffs[0] += chain.getBox(i).getX(pos-n, scale);
	}

	return line;
    }
}
