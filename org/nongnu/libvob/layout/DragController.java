/*   
DragController.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
 *
 *    This file is part of Fenfire.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
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
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;

public class DragController extends AbstractMonoLob {
    public static boolean dbg = false;
    private void p(String s) { System.out.println("DragController:: "+s); }

    protected int button, modifiers;
    protected org.nongnu.libvob.mouse.MouseDragListener listener;

    protected float w, h;

    public DragController(Lob content, int button, 
			  org.nongnu.libvob.mouse.MouseDragListener listener) {
	this(content, button, 0, listener);
    }
    public DragController(Lob content, int button, int modifiers, 
			  org.nongnu.libvob.mouse.MouseDragListener listener) {
	super(content);
	this.button = button;
	this.modifiers = modifiers;
	this.listener = listener;
    }

    public int getButton() { return button; }
    
    public void setButton(int button) { this.button = button; }

    protected Object clone(Object[] newParams) { 
	return new DragController((Lob)newParams[0], button, listener);
    }

    public void setSize(float w, float h) {
	super.setSize(w, h);
	this.w = w;
	this.h = h;
    }

    protected boolean isDragging;
    public boolean isLargerThanItSeems() { 
	return isDragging || content.isLargerThanItSeems(); 
    }
    

    public boolean mouse(VobMouseEvent e, float x, float y, 
			 float origX, float origY) {
	if (dbg) p("got event! "+e+" ("+System.identityHashCode(this)+")"); 
	if (dbg) p("x: "+x+", y: "+y+", w: "+w+", h: "+h+", b: "+button);
	if(x >= 0 && y >= 0 && x < w && y < h &&
	   e.getType() == e.MOUSE_PRESSED && 
	   e.getButton() == button &&
	   e.getModifiers() == modifiers) {
	    if (dbg) p("start drag");
	    listener.startDrag((int)x, (int)y);
	    isDragging = true;
	    return true;
	} else if(isDragging && e.getType() == e.MOUSE_DRAGGED) {
	    if (dbg) p("dragging");
	    listener.drag((int)x, (int)y);
	    return true;
	} else if(isDragging && e.getType() == e.MOUSE_RELEASED && 
		  e.getButton() == button) {
	    listener.endDrag((int)x, (int)y);
	    if (dbg) p("end drag");
	    isDragging = false;
	    return true;
	} else {
	    if (dbg) p("I don't care about this: "+e);
	    return super.mouse(e, x, y, origX, origY);
	}
    }
}
