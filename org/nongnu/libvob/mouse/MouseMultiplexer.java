/*
MouseMultiplexer.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 */
/*
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.mouse;

import org.nongnu.libvob.VobMouseEvent;
import org.nongnu.libvob.input.RelativeAxisListener;

/** A class to send mouse events to the listeners that want them.
 */
public class MouseMultiplexer {
    static public boolean dbg = false;
    private void p(String s) { System.out.println("MouseMultiplexer:: "+s); }

    
    /** Direction(s) of controller to use.
     *  This is a typesafe enumeration.
     */
    private static final class Direction { private Direction() {} }

    /** A constant representing the horizontal direction.
     */
    public static final Direction HORIZONTAL = new Direction();

    /** A constant representing the vertical direction.
     */
    public static final Direction VERTICAL = new Direction();
    
    /** The largest button number accepted plus one.
     */
    public final static int MAXBUTTON = 4;

    /** A constant that is all modifier masks ored together.
     */
    private final static int ALLMASK;
    static {
	VobMouseEvent e = null;
	ALLMASK = e.SHIFT_MASK + e.CONTROL_MASK + e.ALT_MASK;
    }

    /** The maximum modifier mask plus one.
     */
    public final static int MAXMASK = ALLMASK + 1;


    //-- Internal data structures

    private MousePressListener[] mousePressListeners = 
		new MousePressListener[ MAXBUTTON * MAXMASK ];

    private MouseClickListener[] mouseClickListeners = 
		new MouseClickListener[ MAXBUTTON * MAXMASK ];

    private RelativeAxisListener[] wheelListeners = 
		new RelativeAxisListener[ MAXMASK ];

    /** Helper class which is used to send events to RelativeAxisListeners.
     */
    private class RelAxisAdapter implements MousePressListener, MouseDragListener {
	/** The constant to scale the motion with.
	 */
	float multx, multy;
	/** The listeners to call when dragged.
	 */
	RelativeAxisListener listx, listy;

	/** The previous mouse position.
	 */
	int curx, cury;

	public MouseDragListener pressed(int x, int y) { return this; }

	public void startDrag(int x, int y) {
	    curx = x; cury = y;
	}
	public void drag(int x, int y) {
	    if(listx != null && x - curx != 0) {
		listx.changedRelative(multx * (x - curx));
		curx = x;
	    }
	    if(listy != null && y - cury != 0) {
		listy.changedRelative(multy * (y - cury));
		cury = y;
	    }
	}
	public void endDrag(int x, int y) {
	}

    }

    /** If dragging, the draglistener for the current drag.
     * Otherwise null.
     */
    MouseDragListener currentDrag;
    /** If dragging, the mouse button number that started the drag.
     * Otherwise null.
     */
    int currentDragButton;

    //-- Public API

    /** Set a listener for mouse presses (for starting drags).
     */
    public void setListener(int button, int modifiers, String description, MousePressListener l) {
	mousePressListeners[ modifiers + MAXMASK * button ] = l;
    }

    /** Set a listener for mouse drags, translating one of the axes of the drag
     * to the RelativeAxisListener.
     */
    public void setListener(int button, int modifiers, Direction dir, float multiplier,
				String description, RelativeAxisListener l) {
	int ind = modifiers + MAXMASK * button;
	if(mousePressListeners[ind] == null || !(mousePressListeners[ind] instanceof RelAxisAdapter))
	    mousePressListeners[ind] = new RelAxisAdapter();
	RelAxisAdapter adap = (RelAxisAdapter)mousePressListeners[ind];
	if(dir == HORIZONTAL) {
	    adap.multx = multiplier;
	    adap.listx = l;
	} else if(dir == VERTICAL) {
	    adap.multy = multiplier;
	    adap.listy = l;
	}
    }

    /** Set a listener for mouse clicks.
     */
    public void setListener(int button, int modifiers, String description, MouseClickListener l) {
	mouseClickListeners[ modifiers + MAXMASK * button ] = l;
    }

    /** Set a listener for the mouse wheel.
     */
    public void setWheelListener(int modifiers, String description, RelativeAxisListener l) {
	wheelListeners[modifiers] = l;
    }

    private int[] oldXY = new int[2];
    /** Flush all internal settings to begin state.
     */
    public void flush() {
	currentDragButton = 0;
	if (currentDrag != null && dragStarted) 
	    currentDrag.endDrag(oldXY[0], oldXY[1]);
	currentDrag = null;
    }

    /** Whether a drag from a mouse press and a 
     * subsequent mouse motion event has been started.
     * It is extremely not allowed to call the 
     * endDrag() method if there were no drag events, 
     * otherwise clicked and endDrag methods 
     * are probably both called.
     */
    private boolean dragStarted = false;
    public boolean hasDragStarted() { return dragStarted; }

    /** Send an event to the correct listener.
     * @return true if the event was used by this multiplexer.
     */
    public boolean deliverEvent(VobMouseEvent e) {
	oldXY[0] = e.getX(); oldXY[1] = e.getY();
	if (dbg) p("ev: "+e);
	if(e.getType() == e.MOUSE_PRESSED) {
	    // Ignore second button while dragging
	    if(currentDrag != null) return false;
	    MousePressListener mpl = mousePressListeners[(e.getModifiers() & ALLMASK) + MAXMASK * e.getButton()];
	    if(mpl == null) return false;
	    currentDrag = mpl.pressed(e.getX(), e.getY());
	    if(currentDrag != null) {
		dragStarted = false;
		currentDragButton = e.getButton();
		currentDrag.startDrag(e.getX(), e.getY());
		return true;
	    }
	} else if(e.getType() == e.MOUSE_DRAGGED) {
	    if(currentDrag != null) {
		dragStarted = true;
		currentDrag.drag(e.getX(), e.getY());
		return true;
	    }
	} else if(e.getType() == e.MOUSE_RELEASED) {
	    if(currentDragButton != 0 && 
	       e.getButton() == currentDragButton) {
		currentDragButton = 0;
		boolean ret = false;
		if (dragStarted) {
		    currentDrag.endDrag(e.getX(), e.getY());
		    ret = true;
		}
		dragStarted = false;
		currentDrag = null;
		return ret;
	    }
	} else if(e.getType() == e.MOUSE_CLICKED) {
	    MouseClickListener l = mouseClickListeners[(e.getModifiers() & ALLMASK) + MAXMASK * e.getButton()];
	    if(l != null) {
		l.clicked(e.getX(), e.getY());
		return true;
	    }
	} else if(e.getType() == e.MOUSE_WHEEL) {
	    RelativeAxisListener l = wheelListeners[e.getModifiers() & ALLMASK];
	    if(l != null) {
		l.changedRelative(e.getWheelDelta());
		return true;
	    }
	}
	return false;
    }
}






