/*
AnchorLob.java
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;

/** A lob that remembers the last cs it was rendered into.
 *  This allows you to place things involving more than one cs,
 *  for example a connection between two different lobs.
 */
public class AnchorLob extends AbstractMonoLob {

    protected Object key;

    protected int cs, matchingParent;
    protected float width, height;

    public AnchorLob(Lob content) {
	super(content);
	this.key = this;
    }

    public AnchorLob(Lob content, Object key) {
	super(content);
	this.key = key;
    }

    protected Object clone(Object[] params) {
	return new AnchorLob((Lob)params[0], key);
    }

    public int getCS() { return cs; }
    public int getMatchingParent() { return matchingParent; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    

    public void render(VobScene scene, int into, int matchingParent,
		       float x, float y, float w, float h, float d,
		       boolean visible) {
	int cs = scene.coords.box(into, x, y, w, h);
	scene.matcher.add(matchingParent, cs, key);

	this.cs = cs;
	this.matchingParent = matchingParent;
	this.width = w;
	this.height = h;

	content.render(scene, cs, cs, 0, 0, w, h, d, visible);
    }
}
