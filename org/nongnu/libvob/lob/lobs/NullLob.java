/*
NullLob.java
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

/** An infinitely stretchable lob with zero natural size that draws nothing.
 */
public class NullLob extends AbstractLob {
    public static final NullLob instance = new NullLob();

    protected NullLob() {}

    public SizeRequest getSizeRequest() {
	return SizeRequest.newInstance(0, 0, SizeRequest.INF,
				       0, 0, SizeRequest.INF);
    }

    public Layout layout(float w, float h) {
	NullLayout l = (NullLayout)FACTORY.object();
	l.width = w; l.height = h;
	return l;
    }

    private static class NullLayout extends AbstractLayout {
	private float width, height;

	private NullLayout() {}

	public Size getSize() {
	    return Size.newInstance(width, height);
	}

	public void render(VobScene scene, int into, int matchingParent, 
			   float d, boolean visible) {
	}
    }

    private static final Factory FACTORY = new Factory() {
	    protected Object create() {
		return new NullLayout();
	    }
	};
}
