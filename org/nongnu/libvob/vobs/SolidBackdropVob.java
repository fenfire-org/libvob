/*
SolidBackdropVob.java
 *    
 *    Copyright (c) 2002, Matti Katila and Tuomas J. Lukka
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 * Written by Matti Katila and Tuomas J. Lukka
 */

package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.gl.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/** A solid clear-and-paint-background vob.
 * This vob must be placed *without* coordinate systems.
 */
public class SolidBackdropVob extends AbstractVob {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    public Color color;

    /** Create a new SolidBackdropVob.
     * @param color The color to clear to. If null, color buffer will not be cleared
     * 			on OpenGL.
     */
    public SolidBackdropVob(Color color) {
	this.color = color;
    }

    // AWT
    static Rectangle rect = new Rectangle();
    public void render(Graphics g, boolean fast,
		Vob.RenderInfo info1,
		Vob.RenderInfo info2)
    {
        if(dbg) pa("Render solidbg");

	g.setColor(color);
	g.fillRect(0, 0, 2000, 2000);
	g.setColor(Color.black);
    }

    // GL
    private String getGLStr() {
	String common = 
		"Disable TEXTURE_2D\n"+
		"Enable ALPHA_TEST\n"+
		"AlphaFunc GREATER 0.1\n"+
		"Disable BLEND\n"+  // Don't want to enable this by default
				    // as it's fairly expensive
		"Enable DEPTH_TEST\n"+
		"DepthFunc LEQUAL\n"+
		"BlendFunc SRC_ALPHA ONE_MINUS_SRC_ALPHA\n"+
		"Color 0 0 0 1\n"+
		"";
	if(color != null) {
	    return
		"ClearColor " + ColorUtil.colorGLString(color) + " 0\n"+
		"ColorMask 1 1 1 1\n"+
		"DepthMask 1\n"+
		"StencilMask 255\n"+
		"Clear COLOR_BUFFER_BIT DEPTH_BUFFER_BIT STENCIL_BUFFER_BIT\n"+
		common;
	} else {
	    return 
		"ColorMask 1 1 1 1\n"+
		"DepthMask 1\n"+
		"StencilMask 255\n"+
		"Clear DEPTH_BUFFER_BIT STENCIL_BUFFER_BIT\n"+
		common;
	}
	    
    }

    Vob glList;

    public int putGL(VobScene vs) {
	if(glList == null) {
	    glList = GLRen.createCallList( getGLStr() );
	}
	vs.map.put(glList);
	return 0;
    }

}

