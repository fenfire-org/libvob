/*
AbstractVob.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2003, Tuomas Lukka
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
 * Written by Tuomas Lukka
 */
package org.nongnu.libvob;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Color;
import javolution.realtime.*;

/** A two-dimensional renderable, interpolatable object.
 * A Vob is simply an object able to render itself.
 * Instead of just painting we create a temporary structure of Vobs
 * (VobScene)
 * in order to be able to paint all the data in the correct depth-order
 * and to interpolate between views..
 * <p>
 * Vobs are just renderable things; they do <em>not</em> contain any
 * information about identity. Identity is handled in VobCoorder,
 * where each coordinate system can be given a key.
 * @see VobScene
 * @see VobCoorder
 */
public abstract class AbstractVob extends RealtimeObject implements Vob {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    public AbstractVob() {
    }

    public boolean intersect(int x, int y, Vob.RenderInfo info1,
			    Vob.RenderInfo info2) {
	return false;
    }

    /** Renders this vob at the given screen coordinates and in given size. 
     * @param g The graphics context to draw into
     * 		The color should already be set to the default foreground 
     *          color, mixed by the caller. 
     * @param boxDrawn Whether a box background has been drawn.
     *			If false, the background is transparent and
     *			this Vob may draw itself differently,
     *			e.g. by drawing a border or drop-shadow around text
     *			to clarify the visual appearance.
     * @param fast Whether to draw quickly (true) or beautifully (false)
     * @param info1 General parameters and coordinate systems of cs1
     * @param info2 General parameters and coordinate systems of cs2
     * @see VobPlacer#put(Vob vob, int depth, int x, int y, int w, int h)
     * @see RenderInfo
     */
    abstract public void render(Graphics g, 
				boolean fast,
				Vob.RenderInfo info1,
				Vob.RenderInfo info2
				) ;

    private final void render(Graphics g, 
				boolean fast,
				boolean foo,
				Vob.RenderInfo info1,
				Vob.RenderInfo info2) { }

    public final int addToListGL(
			int[] list, int curs, int coordsys1,
				int coordsys2) { 
	throw new Error();
    }

    /** For OpenGL: Add the current vob to the given display list, using
     * the given coordinate systems.
     * There are two alternatives: for composite Vobs, this will
     * call vs.put for the components, for "real" GL Vobs,
     * this will return the GL index.
     */
    public int putGL(VobScene vs) {
	throw new Error("Wrong # coordsys: 0 for " + this);
    }
    public int putGL(VobScene vs, int cs1) {
	throw new Error("Wrong # coordsys: 1 for " + this);
    }
    public int putGL(VobScene vs, int cs1, int cs2) {
	throw new Error("Wrong # coordsys: 2 for " + this);
    }
    public int putGL(VobScene vs, int cs1, int cs2, int cs3) {
	throw new Error("Wrong # coordsys: 3 for " + this);
    }
    public int putGL(VobScene vs, int[] cs) {
	throw new Error("Wrong # coordsys: N for " + this);
    }



    public final int addToListGL(GraphicsAPI.RenderingSurface win,
			int[] list, int curs, int coordsys1,
				int coordsys2) { 
	throw new Error();
    }

}



