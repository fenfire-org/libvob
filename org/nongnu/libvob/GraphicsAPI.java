/*
GraphicsAPI.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka
 */

package org.nongnu.libvob;
import java.util.*;
import java.io.*;
import java.awt.*;

/** An interface that both the OpenGL and java.awt.Graphics subsystems
 * provide.
 * This is currently a singleton class but that may change in the future.
 * <p>
 * The System property <code>vob.api</code> sets the API type: either "awt" or "gl"
 * should be used.
 */
public abstract class GraphicsAPI {
    /** The singleton instance.
     */
    private static GraphicsAPI gfxapi = null;
    private static String type;

    static private void init() {
	String cl;
	try {
	    cl = System.getProperty("vob.api");
	} catch (java.security.AccessControlException e) { 
	    cl = "applet"; 
	}
	
	if(cl == null || cl.equals("awt")) {
	    gfxapi = new org.nongnu.libvob.impl.awt.AWTAPI();
            type = "awt";
	} else if(cl.equals("applet")) {
		gfxapi = new org.nongnu.libvob.impl.applet.APPLETAPI();
            type = "applet";
	} else if(cl.equals("gl")) {
	    gfxapi = new org.nongnu.libvob.impl.gl.GLAPI();
            type = "gl";
	} else if(cl.equals("terminal")) {
	    gfxapi = new org.nongnu.libvob.impl.terminal.TERMINALAPI();
            type = "terminal";
	} else
	    throw new Error("Invalid client type '"+cl+"': should be "+
			    "awt, applet, terminal or gl");
    }

    /** Get the singleton instance.
     */
    static public GraphicsAPI getInstance() { 
	if(gfxapi == null) init();
	return gfxapi; 
    }
    
    /** Return the type string of the API ("gl" or "awt").
     */
    static public String getTypeString() { 
	if(gfxapi == null) init();
	return type; 
    }

    /** Start the update manager thread and run r from that thread.
     * Used because e.g. OpenGL and GLX are easier to manage if everything
     * is done in one thread.
     * <b>Windows must not be created by any other thread.</b>
     */
    public abstract void startUpdateManager(Runnable r);

    /** Create a new window.
     */
    public abstract Window createWindow();

    /** Create an off-screen rendering surface that does not
     * get corrupted by other windows.
     */
    public abstract RenderingSurface createStableOffscreen(int w, int h) ;

    /** Obtain a text style object for an abstract font at a given
     * size. The parameters are as
     * in java.awt.Font.Font().
     *
     */
    public abstract TextStyle getTextStyle(String family, int style, int size);

    public TextStyle getTextStyleByHeight(String family, int style, float h) {
	TextStyle s0 = getTextStyle(family, style, 12);
	return s0.getScaledStyle(h);
    }

    /** An interface for generic rendering surfaces.
     */
    static public interface RenderingSurface {
	/** Get the current size of the window.
	 */
	Dimension getSize();
	/** Render the still image of the vobscreen.
	 */
	void renderStill(VobScene vs, float lod);

	/** Render the interpolated form between the two vobscenes.
	 */
	void renderAnim(VobScene from, VobScene to, float fract, float lod, boolean showFinal);

	/** Whether an change between the two given scenes
	 *  should be animated.
	 */
	boolean needInterp(VobScene from, VobScene to);

	/** Create a new vobscene of the appropriate type, with current window
	 * size as size.
	 * This vobscene will <b>not</b> contain any instructions
	 * to clear the window etc., since vobscenes can be used as
	 * viewports.
	 */
	VobScene createVobScene() ;

	/** Create a new vobscene of the appropriate type for this window.
	 */
	VobScene createVobScene(Dimension size) ;

	/** Create a new child VobScene that may be placed into a VobScene
	 * by the putChildVobScene call.
	 * @param numberOfParameterCS The number of initial coordinate systems in the child scene
	 * 			that are to be given to it as parameters from the parent.
	 */
	ChildVobScene createChildVobScene(Dimension size, int numberOfParameterCS);

	/** Read pixels from the screen.
	 * @return An array of size w*h, of A, R, G, B from 
	 * highest to lowest bit. (BGRA)
	 */
	int[] readPixels(int x, int y, int w, int h) ;
    }

    /** An interface for windows visible on the screen.
     */
    static public interface Window extends RenderingSurface {

	/** Try to set the location and size of the window.
	 * Not guaranteed to succeed.
	 */
	void setLocation(int x, int y, int w, int h) ;

	/** Set the mouse cursor for the window.
	 * Available cursor types. These are similar to ones
	 * in java.awt.Cursor, tough the "_CURSOR" suffix
	 * is dropped.
	 *
	 * "CROSSHAIR"  The crosshair cursor type.
	 * "DEFAULT" The default cursor type (gets set if no cursor is defined).
	 * "E_RESIZE" The east-resize cursor type.
	 * "HAND" The hand cursor type.
	 * "MOVE" The move cursor type.
	 * "N_RESIZE" The north-resize cursor type.
	 * "NE_RESIZE" The north-east-resize cursor type.
	 * "NW_RESIZE" The north-west-resize cursor type.
	 * "S_RESIZE" The south-resize cursor type.
	 * "SE_RESIZE" The south-east-resize cursor type.
	 * "SW_RESIZE" The south-west-resize cursor type.
	 * "TEXT" The text cursor type.
	 * "W_RESIZE" The west-resize cursor type.
	 * "WAIT" The wait cursor type.
	 */
	void setCursor(String name);

	/** Set the event handler for the window.
	 */
	void registerBinder(Binder s);

	/** Add a timeout: Binder.timeout() is called after
	 * given number of milliseconds.
	 */
	void addTimeout(int ms, Object o);

	/** Get the GraphicsAPI this window is associated with.
	 */
	GraphicsAPI getGraphicsAPI();

    }


    static public abstract class AbstractRenderingSurface implements RenderingSurface {
	private final GraphicsAPI gfxapi;
	public AbstractRenderingSurface(GraphicsAPI api) {
	    this.gfxapi = api;
	}
	public GraphicsAPI getGraphicsAPI() { return gfxapi; }
	public VobScene createVobScene() {
	    return createVobScene(getSize());
	}

	
	// The following are here for the benefit of Kaffe, which gets
	// confused if a method is called from this class, but
	// not implemented in the abstract class.
	public Dimension getSize() {
	    throw new UnsupportedOperationException("not implemented");
	}
	public VobScene createVobScene(Dimension size) {
	    throw new UnsupportedOperationException("not implemented");
	}
    }

    static public abstract class AbstractWindow extends AbstractRenderingSurface implements Window {
	public AbstractWindow(GraphicsAPI api) {
	    super(api);
	}

	public void addTimeout(int ms, Object o) {
	    throw new UnsupportedOperationException("Not in this gfxapi");
	}


    }

}


