/*
RectBgLob.java
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
import java.util.*;

/** A lob drawing a lobs in front of and behind another lob.
 */
public class Between extends AbstractMonoLob {

    protected Lob back, front;

    public Between(Lob back, Lob content, Lob front) {
	super(content);
	this.front = front;
	this.back = back;
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { back, content, front };
    }
    protected Object clone(Object[] params) {
	return new Between((Lob)params[0], (Lob)params[1], (Lob)params[2]);
    }

    public Lob getBack() { return back; }
    public Lob getFront() { return front; }
    
    public void setBack(Lob back) { this.back = back; chg(); }
    public void setFront(Lob front) { this.front = front; chg(); }

    public void setSize(float reqW, float reqH) {
	back.setSize(reqW, reqH);
	content.setSize(reqW, reqH);
	front.setSize(reqW, reqH);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {
	int _cs = scene.coords.box(into, w, h);
	int cs = scene.coords.box(_cs, 2*d/3, 0, 0, w, h);
	back.render(scene, cs, matchingParent, w, h, d/3, visible);
	cs = scene.coords.box(_cs, d/3, 0, 0, w, h);
	content.render(scene, cs, matchingParent, w, h, d/3, visible);
	cs = _cs;
	front.render(scene, cs, matchingParent, w, h, d/3, visible);
    }
}
