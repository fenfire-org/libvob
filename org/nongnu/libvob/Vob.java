/*
Vob.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2003 Tuomas Lukka
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

public interface Vob {

    boolean intersect(int x, int y, RenderInfo info1, RenderInfo info2) ;

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
    void render(Graphics g, boolean fast, RenderInfo info1, RenderInfo info2) ;

    /** For OpenGL: Add the current vob to the given display list, using
     * the given coordinate systems.
     * There are two alternatives: for composite Vobs, this will
     * call vs.put for the components, for "real" GL Vobs,
     * this will return the GL index.
     */
    int putGL(VobScene vs) ;
    int putGL(VobScene vs, int cs1) ;
    int putGL(VobScene vs, int cs1, int cs2) ;
    int putGL(VobScene vs, int cs1, int cs2, int cs3) ;
    int putGL(VobScene vs, int[] cs) ;

    /** An interface which provides information about the current 
     * rendering context.
     * This is given as a parameter to Vob.render() and can be used
     * to get various parameters.
     * The (x,y,width,height) are the absolute coordinates relative to the
     * graphics content, that is, relative to the vob scene origin.
     * The width and height come from the box width and box height
     * of the coordinate system, but they have been scaled into the
     * coordinate system of the graphics context. 
     * scaleX and scaleY say how much
     * we are scaled in the X and Y directions.
     */
    public static abstract class RenderInfo {
	public float x=0, y=0, width=0, height=0;

	public float scaleX, scaleY;

	/**The background color of the canvas where we're being drawn.
	 * 		 What about background images?!?
	 */
	public abstract Color getBgColor();

	public abstract int box_x(float px);
	public abstract int box_y(float py);

	public String toString() { 
	    return "RenderInfo x: "+x+", y: "+y+", w: "+width+", h: "+height; 
	}
    }

}


