/*
GLScreen.java
 *    
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
*/
/*
 * Written by Tuomas J. Lukka
 */
package org.nongnu.libvob.impl.gl;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Cursor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.InputEvent;

import java.util.HashMap;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;

public class GLScreen extends GLRenderingSurface implements GraphicsAPI.Window {
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println("GLScreen:: " + s); }

    HashMap timeouts = new HashMap();

    class GLEventHandler implements GL.EventHandler {
	    public void repaint() {
		if(dbg) pa("GLScreen repaint ");
		if(binder != null)
		    binder.repaint();
	    }
	    public void keystroke(String s) {
		if(dbg) pa("GLScreen keyevent "+s);
		// XXX Hack, move to GZZGL-linux.cxx
		if(s.equals("adiaeresis")) { s = "\u00e4";
		} else if(s.equals("Adiaeresis")) { s = "\u00c4";
		} else if(s.equals("odiaeresis")) { s = "\u00f6";
		} else if(s.equals("Odiaeresis")) { s = "\u00d6";
		} else if(s.equals("udiaeresis")) { s = "\u00fc";
		} else if(s.equals("Udiaeresis")) { s = "\u00dc";
		} else if(s.equals("aring")) { s = "\u00e5";
		} else if(s.equals("Aring")) { s = "\u00c5";
		} else if(s.equals("BackSpace")) { s = "Backspace";
                }
                if(s.startsWith("Ctrl-") || s.startsWith("Alt-")) {
                    char c = s.charAt(s.length()-1);
                    if(Character.isUpperCase(c))
                        s = s.substring(0, s.length()-1) + "Shift-" + c;
                    else
                        s = s.substring(0, s.length()-1) +
                            Character.toUpperCase(c);
                }
		if(s.startsWith("Ctrl-Shift-")) {
		     s = "Alt-" + s.substring("Ctrl-Shift-".length());
		}
		if(binder != null)
		    binder.keystroke(s);
	    }
	    boolean didDrag = false;

	    /** Initial X, Y coordinates of the button press
	     * for determining whether we're dragging or not.
	     * Having a threshold is good.
	     */
	    int dX, dY;

	    void notDragging(int x, int y) {
		didDrag = false; dX = x; dY = y;
	    }
	    /** Whether we're already dragging or whether we have
	     * stepped over the limit.
	     * This function sets the didDrag variable if the threshold has been
	     * crossed.
	     */
	    boolean isDragging(int button, int x, int y) {
		if(didDrag) return true;
		if(button==0) return false;
		if((x-dX)*(x-dX) + (y-dY)*(y-dY) > 8) {
		    didDrag = true;
		    return true;
		}
		return false;
	    }

	    public void mouse(int x, int y, int button, int what,
			    int modifiers) {
		if(binder == null) return;

		if (dbg) {
		    System.out.println("MouseEvent: "+x+":"+y+", button: "+button+", what: "+what+", modif: "+modifiers);
		    System.out.println("didDrag: "+didDrag);
		}
		// Handle mouse wheel.
		if(button == 4 || button == 5) {
		    int r = (button == 4 ? 1 : -1);
		    VobMouseEvent ev = new VobMouseEvent(
			    VobMouseEvent.MOUSE_WHEEL,
			    x, y,
			    r, modifiers, 0);
		    binder.mouse(ev);
		    return;
		}

		if(dbg) pa("GLScreen mouseEvent: "+x+" "+y+" "+button+" "+what);
		VobMouseEvent ev = null;
		switch(what) {
		case PRESS: case RELEASE:
		    ev = new VobMouseEvent(
				    (what==PRESS ? VobMouseEvent.MOUSE_PRESSED
					: VobMouseEvent.MOUSE_RELEASED),
				    x, y,
				    0, modifiers, button
				    );
		    if(what == RELEASE && !didDrag) {
			binder.mouse(ev);
			ev = new VobMouseEvent(
					VobMouseEvent.MOUSE_CLICKED,
					x, y,
					0, modifiers, button
					);
		    }
		    notDragging(x, y);
		    break;
		case MOTION:
		    // makes no sense for motion events 
		    //if(button == 0) return ;
		    if(isDragging(button, x, y))
			ev = new VobMouseEvent(
			    VobMouseEvent.MOUSE_DRAGGED,
			    x, y,
			    0, modifiers, button
			    );
		    else ev = new VobMouseEvent(
			    VobMouseEvent.MOUSE_MOVED,
			    x, y,
			    0, modifiers, button
			    );
		    break;
		default:
		    System.out.println("Error!");
		    return ;
		}
		if(dbg) pa("GLScreen mouseEvent: "+x+" "+y+" "+button+" -> "+ev);
		binder.mouse(ev);
	    }

	    public void timeout(int id) {
		if(binder != null)
		    binder.timeout(timeouts.remove(new Integer(id)));
	    }

	    public void windowClosed() {
		if(binder != null)
		    binder.windowClosed();
	    }
    }



    Binder binder;
    public void registerBinder(Binder s) {
	binder = s;
    }

    GL.Window window;

    public GL.Window getWindow() { return window; }

    GLEventHandler h;

    protected GL.RenderingSurface createGLObj(int x, int y, int w, int h) {
	if(dbg) pa("Create glwindow "+x+" "+y+" "+w+" "+h);
	this.h = new GLEventHandler();
	window = GL.createWindow(x, y, w, h, this.h);
	return window;
    }


    public GLScreen(GraphicsAPI api) {
	super(api, 0, 0, 200, 200);
	if(dbg) pa("Created glscreen");
    }

    public void setCursor(String name) {
	name = name.toUpperCase();
	if (name.equals("CROSSHAIR") ||
	    name.equals("DEFAULT") ||
	    name.equals("E_RESIZE") ||
	    name.equals("HAND") ||
	    name.equals("MOVE") ||
	    name.equals("N_RESIZE") ||
	    name.equals("NE_RESIZE") ||
	    name.equals("NW_RESIZE") ||
	    name.equals("S_RESIZE") ||
	    name.equals("SE_RESIZE") ||
	    name.equals("SW_RESIZE") ||
	    name.equals("TEXT") ||
	    name.equals("W_RESIZE") ||
	    name.equals("WAIT"))
            window.setCursor(name);
	else throw new IllegalArgumentException("Unknown cursor: "+name);
    }
    
    public void setLocation(int x, int y, int w, int h) {
	if(dbg) pa("glscreen setloc "+x+" "+y+" "+w+" "+h);
	window.move(x,y);
	window.resize(w,h);
    }
    static int curT = 42;
    public void addTimeout(int ms, Object o) {
	int id = curT++;
	timeouts.put(new Integer(id), o);
	window.addTimeout(ms, id);
    }

    public void chg() {
    }
}
