/*
TableLob.java
 *    
 *    Copyright (c) 2003-2005, Benja Fallenstein
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
package org.nongnu.libvob.lob;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.DefaultVobMap; // for chaining coordinate systems
import org.nongnu.navidoc.util.Obs;
import javolution.realtime.*;
import java.util.*;

public class Linebreaker extends RealtimeObject implements LobList {
    private static void p(String s) { System.out.println("LobList:: "+s); }

    public static int MAXSIZE = (1 << 14);

    protected Axis lineAxis;
    protected LobList items;
    protected float lineSize;

    protected int[] breaks = new int[MAXSIZE];
    protected int lineCount;

    private Linebreaker() {}

    public static Linebreaker newInstance(Axis lineAxis, LobList items,
					  float lineSize) {
	Linebreaker br = (Linebreaker)FACTORY.object();
	br.init(lineAxis, items, lineSize);
	return br;
    }

    private void init(Axis lineAxis, LobList items, float lineSize) {
	this.lineAxis = lineAxis;
	this.items = items;
	this.lineSize = lineSize;

	int nitems = items.getLobCount();

	// XXX do the layout :-)
    }

    public int getLobCount() {
	return lineCount;
    }

    public Lob getLob(int line) {
	if(line >= lineCount)
	    throw new IndexOutOfBoundsException(line+" >= "+lineCount);

	LobList lobs = SubLobList.newInstance(items, 
					      breaks[line], breaks[line+1]);

	return BoxLob.newInstance(lineAxis, lobs);
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    items.move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new Linebreaker();
	    }
	};
}
