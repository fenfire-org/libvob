/*
VobMouseEvent.java
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

package org.nongnu.libvob;

/** A mouse event.
 * This class exists because of the strange behaviour of
 * java.awt.MouseEvent (i.e. aliasing BUTTON2 and ALT etc.).
 * It also allows our OpenGL code to never depend on AWT classes.
 * <p>
 * Limitation: we do not allow mouse button chords.
 */
public class VobMouseEvent {
    public final static int MOUSE_PRESSED = 1827;
    public final static int MOUSE_RELEASED = 1828;
    public final static int MOUSE_CLICKED = 1829;
    public final static int MOUSE_DRAGGED = 1830;
    public final static int MOUSE_WHEEL = 1831;
    public final static int MOUSE_MOVED = 1832;

    // DO NOT CHANGE WITHOUT CHANGING OPENGL CODE AS WELL
    public final static int SHIFT_MASK = 1;
    public final static int CONTROL_MASK = 2;
    public final static int ALT_MASK = 4;

    private final int type, x, y, wheelDelta, modifiers, button;

    /** Corresponds to getID in MouseEvent.
     */
    public int getType() { return this.type; }
    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public int getWheelDelta() { return this.wheelDelta; }
    public int getModifiers() { return this.modifiers; }
    
    /** The button index. 
     * This is just a number, 1, 2 or 3.
     */
    public int getButton() { return this.button; }

    /** Create a new vob mouse event.
     * @param type MOUSE_PRESSED, MOUSE_RELEASED, MOUSE_CLICKED, MOUSE_DRAGGED, or MOUSE_WHEEL
     * @param x,y The coordinates
     * @param wheelDelta The wheel movement
     * @param modifiers Bitwise or of SHIFT_MASK, CONTROL_MASK, ALT_MASK
     * @param button The mouse button being pressed.
     */
    public VobMouseEvent(
	    int type, 
	    int x, 
	    int y, 
	    int wheelDelta,
	    int modifiers,
	    int button) {
	this.type = type;
	this.x = x;
	this.y = y;
	this.wheelDelta = wheelDelta;
	this.modifiers = modifiers;
	this.button = button;
    }

    private String type2str(int type) {
	switch(type) {
	case MOUSE_PRESSED: return "pressed";
	case MOUSE_RELEASED: return "released";
	case MOUSE_CLICKED: return "clicked";
	case MOUSE_DRAGGED: return "dragged";
	case MOUSE_WHEEL: return "wheel";
	case MOUSE_MOVED: return "moved";
	default:
	    return "unknown type";
	}
    }

    public String toString() {
	return "[VobMouseEvent: ["+type2str(type)+"] "+x+" "+y+" "
		+wheelDelta+" "+modifiers+" "+button+"]";
    }


    public boolean equals(Object obj) {
	if (obj instanceof VobMouseEvent)
	    return ((VobMouseEvent)obj).getObjectStateStr().
		equals(getObjectStateStr());
	return false;
    }


    /** Get the the state of the instance as string to be 
     * writen, e.g., to hard disk.
     * <p>
     * Implementation note: The right way would be to use 
     * java.io.Serializable but it would be rather heavy.
     */
    public String getObjectStateStr() {
	return "VobMouseEvent "+type+" "+x+" "+y+" "+wheelDelta+" "+
	    modifiers+" "+button;
    }

    /** Create a new instance of this object from the given 
     * state string and return the instance or null if any error
     * is found.
     * <p>
     * Implementation note: The right way would be to use 
     * java.io.Serializable but it would be rather heavy.
     */
    public static VobMouseEvent createObjectFromStateStr(String vobMouseEventState) {
	java.util.StringTokenizer st = new java.util.StringTokenizer(vobMouseEventState);
	java.util.ArrayList tokens = new java.util.ArrayList(7);
	while (st.hasMoreTokens()) tokens.add(st.nextToken());

	if (tokens.size() != 7) return null;
	try {
	    // check if not correct identifier.
	    if ( ! ((String)tokens.get(0)).equals("VobMouseEvent"))
		return null;
	    tokens.remove(0);
	    int[] ints = new int[6];
	    for (int i=0; i<tokens.size(); i++) {
		ints[i] = (new Integer((String) tokens.get(i))).intValue();
	    }
	    int[] i = ints; 
	    return new VobMouseEvent(i[0],i[1],i[2],i[3],i[4],i[5]);

	} catch (Exception e) {
	    return null;
	}
    }
}
