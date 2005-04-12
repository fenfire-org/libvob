/*
DragManager.java
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
import org.nongnu.libvob.lob.lobs.DragController;
import javolution.realtime.*;
import javolution.util.*;
import java.util.*;

public class DragManager extends AbstractDelegateLob {
    public static boolean dbg = false;
    private static final void p(String s) { System.out.println("DragManager:: "+s); }
    
    private static DragController dragController;
    private static Object path;

    public static DragManager newInstance(Lob delegate) {
	DragManager m = (DragManager)FACTORY.object();
	m.delegate = delegate;
	return m;
    }

    public static void setDragController(DragController dragController, 
					 Object path) {
	if(dbg) p("set drag controller: "+dragController+" "+path);

	dragController.move(ObjectSpace.HEAP);

	if(path instanceof Realtime)
	    ((Realtime)path).move(ObjectSpace.HEAP);

	DragManager.dragController = dragController;
	DragManager.path = path;
    }

    public Lob wrap(Lob lob) {
	return newInstance(lob);
    }

    float[] coords = new float[3];
    public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			 float x, float y) {

	if (dbg) p("got event! "+e+" ("+System.identityHashCode(this)+")"); 
	if(dragController != null && e.getType() == e.MOUSE_DRAGGED) {
	    if (dbg) p("dragging");

	    int dragCS = scene.matcher.getPathCS(path);
	    
	    coords[0] = x; coords[1] = y;
	    scene.coords.inverseTransformPoints3(dragCS, coords, coords);
	    dragController.drag(scene, dragCS, coords[0], coords[1]);
	    return true;
	} else if(dragController != null && e.getType() == e.MOUSE_RELEASED) {
	    if (dbg) p("end drag");

	    int dragCS = scene.matcher.getPathCS(path);
	    
	    coords[0] = e.getX(); coords[1] = e.getY();
	    scene.coords.inverseTransformPoints3(dragCS, coords, coords);
	    dragController.endDrag(scene, dragCS, coords[0], coords[1]);

	    dragController = null;
	    return true;
	} else {
	    if (dbg) p("I don't care about this: "+e);
	    return super.mouse(e, scene, cs, x, y);
	}
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new DragManager();
	    }
	};
}
