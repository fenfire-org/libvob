/*
Between.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein
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
import java.awt.Color;
import java.util.*;

/** A lob drawing a lobs in front of and behind another lob.
 */
public class Between extends AbstractLob {

    protected Lob back, middle, front;

    private Between() {}

    public Between newInstance(Lob back, Lob middle, Lob front) {
	Between b = (Between)LOB_FACTORY.object();
	
	b.back   = (back == null) ?   NullLob.instance : back;
	b.middle = (middle == null) ? NullLob.instance : middle;
	b.front  = (front == null) ?  NullLob.instance : front;

	return b;
    }

    public SizeRequest getSizeRequest() {
	return middle.getSizeRequest();
    }

    public Layout layout(float w, float h) {
	BetweenLayout l = (BetweenLayout)LAYOUT_FACTORY.object();
	l.back   = back.layout(w, h);
	l.middle = middle.layout(w, h);
	l.front  = front.layout(w, h);
	return l;
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    back.move(os); middle.move(os); front.move(os);
	    return true;
	}
	return false;
    }

    protected static class BetweenLayout extends AbstractLayout {
	private Layout back, middle, front;

	public Size getSize() {
	    return middle.getSize();
	}

	public void render(VobScene scene, int into, int matchingParent,
			   float d, boolean visible) {
	    int _cs = into;
	    int cs = scene.coords.translate(_cs, 0, 0, 2*d/4);
	    back.render(scene, cs, matchingParent, d/4, visible);
	    cs = scene.coords.translate(_cs, 0, 0, d/4);
	    middle.render(scene, cs, matchingParent, d/4, visible);
	    cs = _cs;
	    front.render(scene, cs, matchingParent, d/4, visible);
	}

	public boolean move(ObjectSpace os) {
	    if(super.move(os)) {
		back.move(os); middle.move(os); front.move(os);
		return true;
	    }
	    return false;
	}
    }

    private static final Factory LAYOUT_FACTORY = new Factory() {
	    public Object create() {
		return new BetweenLayout();
	    }
	};

    private static final Factory LOB_FACTORY = new Factory() {
	    public Object create() {
		return new Between();
	    }
	};
}
