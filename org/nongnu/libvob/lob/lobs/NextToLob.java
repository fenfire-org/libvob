/*
NextToLob.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein
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

public class NextToLob extends AbstractDelegateLob {

    protected Axis axis;
    protected Lob popup;

    private NextToLob() {}

    public static NextToLob newInstance(Axis axis, Lob content, Lob popup) {
	NextToLob l = (NextToLob)FACTORY.object();
	l.delegate = content;
	l.axis = axis;
	l.popup = popup;
	return l;
    }

    protected Lob wrap(Lob lob) {
	return newInstance(axis, lob, popup);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {
	SizeRequest s = delegate.getSizeRequest();

	float x, y;
	if(axis == Axis.X) {
	    x = s.width();
	    y = 0;
	} else {
	    x = 0;
	    y = s.height();
	}
	
	delegate.render(scene, into, matchingParent, d, visible);

	int cs = scene.coords.translate(into, x, y, -1);
	scene.matcher.add(matchingParent, cs, "popup");

	PopupManager.addPopup(popup, scene.matcher.getPath(cs));
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new NextToLob();
	    }
	};
}
