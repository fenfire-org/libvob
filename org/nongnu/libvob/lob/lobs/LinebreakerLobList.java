/*
LinebreakerLobList.java
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
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.*;
import javolution.realtime.*;
import java.util.*;

/** A list of "lines" created by line-breaking another lob list.
 *  The "lines" can be horizontal or vertical since they can be
 *  hboxes or vboxes. Depending on the lobs in the underlying list,
 *  they don't need to be lines of characters, either; for example,
 *  they can be columns, each containing a vbox of text lines.
 */
public class LinebreakerLobList extends RealtimeList {
    private static void p(String s) { System.out.println("LinebreakerLobList:: "+s); }

    private static final float INF = Lob.INF;

    public static int MAXSIZE = (1 << 10);

    protected Axis lineAxis;
    protected List items;
    protected float lineSize;

    protected int[] breaks = new int[MAXSIZE];
    protected int lineCount;

    private LinebreakerLobList() {}

    public static LinebreakerLobList newInstance(Axis lineAxis, List items,
						float lineSize) {
	LinebreakerLobList br = (LinebreakerLobList)FACTORY.object();
	br.init(lineAxis, items, lineSize);
	return br;
    }

    private void init(Axis lineAxis, List items, float lineSize) {
	this.lineAxis = lineAxis;
	this.items = items;
	this.lineSize = lineSize;

	int nitems = items.size();

	breaks[0] = -1;
	float pos = 0;
	lineCount = 1;
	int i = -1;
	float breakQuality = -INF;
	float preBreakWid = 0, postBreakWid = 0;

	float lastwid = 0;

	do {
	    int next;
	    float wid = 0, nextwid = 0;
	    float nextBreakQuality = INF;

	    float nextPreBreakWid = 0, nextPostBreakWid = 0;

	    PoolContext.enter();
	    try {
		for(next=i+1; next<items.size(); next++) {
		    Lob l = (Lob)items.get(next);
		    SizeRequest r = l.getSizeRequest();

		    nextBreakQuality = l.getBreakQuality(lineAxis);
		    
		    Lob preBreak = l.getBreakLob(lineAxis, -1);
		    if(preBreak != null)
			nextPreBreakWid = 
			    preBreak.getSizeRequest().nat(lineAxis);
		    
		    Lob postBreak = l.getBreakLob(lineAxis, +1);
		    if(postBreak != null)
			nextPostBreakWid = 
			    postBreak.getSizeRequest().nat(lineAxis);

		    if(nextBreakQuality < 0) {
			wid += r.nat(lineAxis);
		    } else {
			nextwid = r.nat(lineAxis);
			break;
		    }
		}
	    } finally {
		PoolContext.exit();
	    }

	    if(i>=0 && (pos+wid>lineSize || breakQuality >= INF)) {
		breaks[lineCount] = i;
		lineCount++;
		
		pos = postBreakWid;

		if(pos + wid > lineSize) {
		    OUTER: 
		    while(true) {
			float xwid = 0;
			for(int x=i+1; x<next; x++) {
			    Lob l = (Lob)items.get(x);
			    SizeRequest r = l.getSizeRequest();
			    
			    if(pos + xwid + r.nat(lineAxis) > lineSize) {
				breaks[lineCount] = x;
				lineCount++;
				i = x;
				pos = 0;
				continue OUTER;
			    } else {
				xwid += r.nat(lineAxis);
			    }
			}

			break;
		    }
		}
	    }

	    pos += wid + nextwid;

	    breakQuality = nextBreakQuality;
	    preBreakWid = nextPreBreakWid;
	    postBreakWid = nextPostBreakWid;
	    i = next;

	    lastwid = nextwid;

	} while(i < items.size());

	breaks[lineCount] = items.size();
    }

    public int size() {
	return lineCount+1;
    }

    public Object get(int line) {
	if(line > lineCount)
	    throw new IndexOutOfBoundsException(line+" >= "+lineCount);

	if(line == lineCount)
	    return Lobs.glue(lineAxis.other(), 0, 0, SizeRequest.INF);

	int start = breaks[line]+1, end = breaks[line+1];
	//if(end > items.size()) end = items.size(); // XXX huh?

	if(end < start) end = start; // XXX

	List lobs = items.subList(start, end);

	if(breaks[line] >= 0) {
	    Lob br = (Lob)items.get(breaks[line]);

	    Lob before = br.getBreakLob(lineAxis, +1);
	    if(before != null) {
		lobs = Lists.concat(Lists.list(before), lobs);
	    }
	}

	if(breaks[line+1] < items.size()) {
	    Lob br = (Lob)items.get(breaks[line+1]);

	    Lob after = br.getBreakLob(lineAxis, -1);
	    if(after != null) {
		lobs = Lists.concat(lobs, Lists.list(after));
	    }
	}

	Lob l = BoxLob.newInstance(lineAxis, lobs);
	//l = Lobs.frame(l, null, java.awt.Color.red, 1, 0, false); // debug
	return l;
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    if(items instanceof Realtime) ((Realtime)items).move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new LinebreakerLobList();
	    }
	};
}
