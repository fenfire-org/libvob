/*   
RelativeDragController.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
 *                  2004, Matti J. Katila
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
 * Written by Benja Fallenstein and Matti J. Katila
 */
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;

public class RelativeDragController extends AbstractMonoLob {
    public static boolean dbg = false;
    private void p(String s) { System.out.println("RelativeDragController:: "+s); }

    protected int button, modifiers;
    protected RelativeDragListener listener;

    protected float w, h;

    public RelativeDragController(Lob content, int button,
				  RelativeDragListener listener) {
	this(content, button, 0, listener);
    }
    public RelativeDragController(Lob content, int button, int modifiers,
				  RelativeDragListener listener) {
	super(content);
	this.button = button;
	this.modifiers = modifiers;
	this.listener = listener;
    }

    public int getButton() { return button; }
    
    public void setButton(int button) { this.button = button; }

    protected Object clone(Object[] newParams) { 
	return new RelativeDragController((Lob)newParams[0], button, listener);
    }

    public void setSize(float w, float h) {
	super.setSize(w, h);
	this.w = w;
	this.h = h;
    }

    protected boolean isDragging;
    protected float oldX=0, oldY=0;


    public boolean isLargerThanItSeems() { 
	return isDragging || content.isLargerThanItSeems(); 
    }
    
    public boolean mouse(VobMouseEvent e, float x, float y,
			 float origX, float origY) {
	if (dbg) p("got event!"); 

	if(x >= 0 && y >= 0 && x < w && y < h &&
	   e.getType() == e.MOUSE_PRESSED && e.getButton() == button &&
	   e.getModifiers() == modifiers) {
	    if (dbg) p("start drag");
	    oldX = x; oldY = y;
	    isDragging = true;

	} else if(isDragging && e.getType() == e.MOUSE_DRAGGED) {
	    listener.change(x-oldX, y-oldY);
	    return true;
	} else if(isDragging && e.getType() == e.MOUSE_RELEASED && 
		  e.getButton() == button) {
	    listener.change(x-oldX, y-oldY);
	    if (dbg) p("end drag");
	    isDragging = false;
	    return true;
	}
	return super.mouse(e, x, y, origX, origY);

    }
}
