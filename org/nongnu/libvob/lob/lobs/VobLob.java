/*
VobLob.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein
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
import org.nongnu.libvob.util.*;
import javolution.realtime.*;
import java.util.*;

/** A lob drawing a single vob.
 */
public class VobLob extends AbstractLob {

    protected Vob vob;

    public VobLob() {}

    public static VobLob newInstance(Vob vob) {
	VobLob vl = (VobLob)LOB_FACTORY.object();
	vl.vob = vob;
	return vl;
    }

    public SizeRequest getSizeRequest() {
	return SizeRequest.newInstance(0, 0, SizeRequest.INF,
				       0, 0, SizeRequest.INF);
    }

    public Layout layout(float w, float h) {
	VobLayout vl = (VobLayout)LAYOUT_FACTORY.object();
	vl.vob = vob;
	vl.size.width = w;
	vl.size.height = h;
	return vl;
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    if(vob instanceof Realtime)
		((Realtime)vob).move(os);
	    return true;
	}
	return false;
    }

    private static final class VobLayout extends AbstractLayout {
	private Vob vob;

	private Size size = new Size();

	private VobLayout() {}

	public Size getSize() {
	    return size;
	}

	public void render(VobScene scene, int into, int matchingParent,
			   float d, boolean visible) {

	    int cs = scene.coords.box(into, size.width, size.height);
	    if(visible) scene.put(vob, cs);
	}

	public boolean move(ObjectSpace os) {
	    if(super.move(os)) {
		if(vob instanceof Realtime)
		    ((Realtime)vob).move(os);
		return true;
	    }
	    return false;
	}
    }

    private static final Factory LAYOUT_FACTORY = new Factory() {
	    public Object create() {
		return new VobLayout();
	    }
	};

    private static final Factory LOB_FACTORY = new Factory() {
	    public Object create() {
		return new VobLob();
	    }
	};
}
