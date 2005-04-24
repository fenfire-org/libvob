/*   
DragController.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein.
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
import org.nongnu.libvob.*;
import org.nongnu.libvob.lob.*;
import javolution.realtime.*;

public abstract class DragController extends AbstractDelegateLob {
    public static boolean dbg = false;
    private static final void p(String s) { System.out.println("DragController:: "+s); }

    protected float startx, starty;

    public int button, eventType, modifiers;

    protected DragController() {}

    protected Object getKey() {
	return "drag-controller-cs";
    }

    public void startDrag(VobScene scene, int cs, float x, float y, 
			  VobMouseEvent e) {
    }

    public void drag(VobScene scene, int cs, float x, float y, 
		     VobMouseEvent e) {
    }

    public void endDrag(VobScene scene, int cs, float x, float y, 
			VobMouseEvent e) {
    }

    public void render(VobScene scene, int cs, int matchingParent,
		       float d, boolean visible) {
	int xcs = scene.coords.translate(cs, 0, 0);
	scene.matcher.add(matchingParent, xcs, getKey());
	super.render(scene, cs, matchingParent, d, visible);
    }

    private float[] coords = new float[3];
    public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			 float x, float y) {
	SizeRequest r = getSizeRequest();
	float w = r.width(), h = r.height();

	if (dbg) p("got event! "+e+" ("+System.identityHashCode(this)+")"); 
	if (dbg) p("x: "+x+", y: "+y+", w: "+w+", h: "+h+", b: "+button);
	if(/*x >= 0 && y >= 0 && x < w && y < h &&*/
	   e.getType() == e.MOUSE_PRESSED && 
	   e.getButton() == button &&
	   e.getModifiers() == modifiers) {
	    if (dbg) p("start drag");

	    int xcs = scene.matcher.getCS(cs, getKey());
	    
	    Object path = scene.matcher.getPath(xcs);

	    coords[0] = e.getX(); coords[1] = e.getY();
	    scene.coords.inverseTransformPoints3(xcs, coords, coords);

	    startx = coords[0];
	    starty = coords[1];

	    DragManager.setDragController(this, path);
	    startDrag(scene, cs, startx, starty, e);

	    return true;
	} else {
	    if (dbg) p("I don't care about this: "+e);
	    return super.mouse(e, scene, cs, x, y);
	}
    }
}
