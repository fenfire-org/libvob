/*
LinebreakerLob.java
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
public class LinebreakerLob extends AbstractSequence {
    private static void p(String s) { System.out.println("LinebreakerLob:: "+s); }

    private Axis lineAxis;

    private LinebreakerLob() {}

    public static LinebreakerLob newInstance(Axis lineAxis, List lobs) {
	LinebreakerLob bl = (LinebreakerLob)FACTORY.object();
	bl.lineAxis = lineAxis; bl.lobs = lobs;
	return bl;
    }

    public Axis getLayoutableAxis() {
	return lineAxis;
    }

    public Lob layoutOneAxis(float size) {
	List lines = LinebreakerLobList.newInstance(lineAxis, lobs, size);
	return BoxLob.newInstance(lineAxis.other(), lines);
    }

    public SizeRequest getSizeRequest() {
	return SizeRequest.newInstance(0, 0, SizeRequest.INF,
				       0, 0, SizeRequest.INF);
    }

    public Lob layout(float width, float height) {
	Lob l = layoutOneAxis(lineAxis.coord(width, height));
	return l.layout(width, height);
    }

    public boolean key(String key) {
	for(int i=0; i<lobs.size(); i++)
	    if(((Lob)lobs.get(i)).key(key)) return true;
	return false;
    }

    public boolean mouse(VobMouseEvent e, VobScene sc, int cs, 
			 float x, float y) { 
	throw new UnsupportedOperationException("not layouted");
    }

    public void render(VobScene scene, int into, int matchingParent, 
		       float d, boolean visible) {
	throw new UnsupportedOperationException("not layouted");
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    if(lobs instanceof Realtime) ((Realtime)lobs).move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new LinebreakerLob();
	    }
	};
}
