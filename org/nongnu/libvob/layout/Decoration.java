/*
Decoration.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import java.awt.Color;

/** Draws a vob between two anchors (lobs).
 */
public class Decoration extends AbstractMonoLob {

    protected Vob vob;
    protected AnchorLob start, end;

    public Decoration(Lob content, Vob vob, AnchorLob start, AnchorLob end) {
	super(content);
	this.vob = vob;
	this.start = start;
	this.end = end;
    }

    protected Replaceable[] getParams() { 
	return new Replaceable[] { content, start, end };
    }
    protected Object clone(Object[] params) {
	return new Decoration((Lob)params[0], vob, (AnchorLob)params[1],
			      (AnchorLob)params[2]);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float x, float y, float w, float h, float d,
		       boolean visible) {
	content.render(scene, into, matchingParent, x, y, w, h, d, visible);
	if(visible)
	    scene.put(vob, start.getCS(), end.getCS());
    }
}
