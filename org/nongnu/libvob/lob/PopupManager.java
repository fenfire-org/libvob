/*
PopupManager.java
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
package org.nongnu.libvob.lob;
import org.nongnu.libvob.*;
import javolution.realtime.*;
import javolution.util.*;
import java.util.*;

public class PopupManager extends AbstractDelegateLob {
    
    private static FastMap popups = new FastMap();

    public static PopupManager newInstance(Lob delegate) {
	PopupManager m = (PopupManager)FACTORY.object();
	m.delegate = delegate;
	return m;
    }

    public static void addPopup(Lob lob, Object path) {
	//Map popups = (Map)POPUPS.getValue();
	/*
	if(popups == null)
	    throw new IllegalStateException("no popup manager available");
	*/
	((Realtime)path).move(ObjectSpace.HEAP);
	popups.put(lob, path);
    }

    public Lob wrap(Lob lob) {
	return newInstance(lob);
    }

    float[] coords = new float[3];
    public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			 float x, float y) {

	for(Iterator i = popups.fastKeyIterator(); i.hasNext();) {
	    Lob lob = (Lob)i.next();
	    Object path = popups.get(lob);
	    int pcs = scene.matcher.getPathCS(path);

	    coords[0] = coords[1] = coords[2] = 0;
	    scene.coords.transformPoints3(pcs, coords, coords);

	    float x0 = coords[0], y0 = coords[1];

	    scene.coords.getSqSize(pcs, coords);
	    scene.coords.transformPoints3(pcs, coords, coords);

	    float x1 = coords[0], y1 = coords[1];

	    lob = lob.layout(x1-x0, y1-y0);

	    if(x0 <= x && y0 <= y && x <= x1 && y <= y1) {
		if(lob.mouse(e, scene, pcs, x-x0, y-y0)) // XXX transform x and y relative to 'cs'!!!
		    return true;
	    }
	}

	return delegate.mouse(e, scene, cs, x, y);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {

	popups.clear();

	LocalContext.enter();
	try {
	    //FastMap popups = FastMap.newInstance();
	    //RoleContext.getState().put("popups", popups);
	    //POPUPS.setValue(popups);
	    
	    delegate.render(scene, into, matchingParent, d, visible);

	    for(Iterator i = popups.fastKeyIterator(); i.hasNext();) {
		Lob lob = (Lob)i.next();
		Object path = (Object)popups.get(lob);

		SizeRequest r = lob.getSizeRequest();
		lob = lob.layout(r.natW, r.natH);
		
		int cs = scene.matcher.getPathCS(path);
		lob.render(scene, cs, cs, d, visible);
	    }
	} finally {
	    LocalContext.exit();
	}
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new PopupManager();
	    }
	};
}
