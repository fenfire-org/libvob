/*
GLFont.java
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


package org.nongnu.libvob.gl;

/** A wrapper for QuadFont that knows about 
 * measurements.
 * Later on, this may change to an interface for using
 * different font types, but not yet.
 */
public class GLFont {

    private float height;
    private float yoffs;
    private float[] widths;

    private GL.QuadFont quadFont;

    public GLFont(float height, float yoffs, float[] widths, GL.QuadFont quadFont) {
	this.height = height;
	this.yoffs = yoffs;
	this.widths = widths;
	this.quadFont = quadFont;
    }

    /** Get the height of a single line.
     */
    public float getHeight() { return height; }
    /** Get the offset (downwards from the top
     * of the line) to the baseline.
     */
    public float getYOffs() { return yoffs; }
    /** Get the widths (advances) of the characters
     * in the font.
     * The returned array MUST NOT BE MODIFIED.
     */
    public float[] getWidths() { return widths; }

    public GL.QuadFont getQuadFont() { return quadFont; }


}
