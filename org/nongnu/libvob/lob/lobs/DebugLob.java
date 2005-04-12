/*
DebugLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
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

/** A lob placing a margin around its contents.
 */
public class DebugLob extends AbstractDelegateLob {
    private void p(String s) { System.out.println("DebugLob "+name+":: "+s); }

    protected String name;

    private DebugLob() {}

    public static DebugLob newInstance(Lob content, String name) {
	DebugLob m = (DebugLob)FACTORY.object();
	m.delegate = content;
	m.name = name;
	return m;
    }

    protected Lob wrap(Lob l) {
	p("wrap "+l);
	return newInstance(l, name);
    }

    public SizeRequest getSizeRequest() {
	p("getSizeRequest");
	return super.getSizeRequest();
    }

    public Lob layoutOneAxis(float size) {
	Lob l = super.layoutOneAxis(size);
	p("layoutOneAxis("+size+") returns "+l+" size "+l.getSizeRequest());
	return l;
    }

    public Lob layout(float w, float h) {
	p("layout("+w+", "+h+")");
	return super.layout(w, h);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {
	p("render");
	super.render(scene, into, matchingParent, d, visible);
    }

    public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			 float x, float y) {
	p("mouse: "+e+" "+cs+" "+x+" "+y);
	return super.mouse(e, scene, cs, x, y);
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new DebugLob();
	    }
	};
}
