/*
BoxLob.java
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
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.DefaultVobMap; // for chaining coordinate systems
import org.nongnu.navidoc.util.Obs;
import javolution.realtime.*;
import java.util.*;

/** A lob that renders an hbox or vbox of lobs.
 */
public class BoxLob extends AbstractLob {
    private static void p(String s) { System.out.println("BoxLob:: "+s); }

    private class BoxTable extends RealtimeObject implements TableLob.Table {
	
	public int getRowCount() {
	    return (axis == Axis.Y) ? items.size() : 1;
	}
	public int getColumnCount() {
	    return (axis == Axis.X) ? items.size() : 1;
	}

	public Lob getLob(int row, int column) {
	    if((axis == Axis.X && row != 0) ||
	       (axis == Axis.Y && column != 0))
		throw new IndexOutOfBoundsException(row+" "+column);

	    Lob lob = (Lob)items.get((axis == Axis.X) ? column : row);

	    if(otherAxisSize < 0 || axis.other() != lob.getLayoutableAxis())
		return lob;
	    else
		return lob.layoutOneAxis(otherAxisSize);
	}
    }

    private Axis axis;
    private List items;

    private float otherAxisSize; // size along the other axis; -1 if unknown

    private BoxTable table = new BoxTable();

    private BoxLob() {}

    public static BoxLob newInstance(Axis axis, List items) {
	return newInstance(axis, items, -1);
    }

    private static BoxLob newInstance(Axis axis, List items, 
				      float otherAxisSize) {
	BoxLob bl = (BoxLob)FACTORY.object();
	bl.axis = axis; bl.items = items; bl.otherAxisSize = otherAxisSize;
	return bl;
    }

    public SizeRequest getSizeRequest() {
	// XXX should cache the TableLob between getSizeRequest() and layout()
	// but I'm not sure how to make that work with Javolution
	return TableLob.newInstance(table).getSizeRequest();
    }

    public boolean key(String key) {
	for(int i=0; i<items.size(); i++)
	    if(((Lob)items.get(i)).key(key)) return true;
	return false;
    }

    public boolean mouse(VobMouseEvent e, VobScene sc, int cs, 
			 float x, float y) { 
	throw new UnsupportedOperationException("not layouted");
    }

    public Lob layout(float width, float height) {
	return TableLob.newInstance(table).layout(width, height);
    }

    public Axis getLayoutableAxis() {
	return axis.other();
    }

    public Lob layoutOneAxis(float size) {
	return newInstance(axis, items, size);
    }

    public void render(VobScene scene, int into, int matchingParent, 
		       float d, boolean visible) {
	throw new UnsupportedOperationException("not layouted");
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    table.move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new BoxLob();
	    }
	};
}
