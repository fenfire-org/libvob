/*
RectBgVob.java
 *    
 *    Copyright (c) 1999-2002, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka and Tero Maeyraenen (ae = a umlaut! Fixed for gcj)
 */

package org.nongnu.libvob.vobs;

import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;

import org.nongnu.libvob.util.ColorUtil;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/** A vob which is a rectangular background and frame.
 * Draws a filled (possibly with several colors) background rectangle,
 * surrounded by a rectangle of the current foreground color.
 */
public class RectBgVob extends AbstractColorableVob {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    /** Background color */
    protected final Color bgColor;

    /** Border drawing flag */
    protected final boolean drawBorder;

    /** Border color */
    protected final Color borderColor;

    public boolean isSquare = false;

    /** Constructor shorthands */
    public RectBgVob() { this(Color.white, true, null); }
    public RectBgVob(Color bgColor) { this(bgColor, true, null); }
    public RectBgVob(Color bgColor, boolean drawBorder) { this(bgColor, drawBorder, null); }
  
    /** Construct RectBgVob.
     * @param bgColor Background color.
     * @param drawBorder Border drawing flag.
     * @param borderColor Border color. If null, default fgColor is used.
     */
    public RectBgVob(Color bgColor, boolean drawBorder, Color borderColor) {
	super();
	this.bgColor = bgColor;
	this.drawBorder = drawBorder;
	this.borderColor = borderColor;
    }

    static Rectangle rect = new Rectangle();

    public void render(Graphics g,
		       boolean fast,
		       Vob.RenderInfo info1,
		       Vob.RenderInfo info2) {
	int mx = (int)info1.x, my=(int)info1.y,
	    mw = (int)info1.width, mh = (int)info1.height;

	Color oldfg = g.getColor();

	// Draw a rectangle in the background color, wiping out
	// the already drawn stuff a little wider than we will draw.
	g.setColor(info1.getBgColor());
	if (mh > 14) g.fillRect(mx-2, my-2, mw+4, mh+4);
	else g.fillRect(mx-1, my-1, mw+2, mh+2);

	if(colors == null || colors.length == 0) {
	    g.setColor(bgColor);
	    g.fillRect(mx, my, mw, mh);
	} else {
	    for(int i=0; i<colors.length; i++) {
		g.setColor(colors[i]);
		g.fillRect(mx+(mw*i)/colors.length, my,
			   mw/colors.length, mh);
	    }
	}

	if(drawBorder) {
	    g.setColor(borderColor);
	    g.drawRect(mx, my, mw-1, mh-1);
	    if(mh >= 14) {
	    	g.drawRect(mx+1, my+1, mw-3, mh-3);
	    }
	}

	g.setColor(oldfg);
    }

    Vob setup, draw, border, teardown;

    public int putGL(VobScene vs, int coordsys1) {
        if(dbg) pa("Addtolistgl rectbg "+coordsys1);
	if(draw == null) {
	    String bgcall = "";
	    if(colors != null && colors.length > 0) {
	        double w = 1.0 / colors.length;

	        for(int i=0; i<colors.length; i++) {
		    double x1 = i*w;
		    double x2 = x1 + w;
                    bgcall += (
		        "Color "+ColorUtil.colorGLString(colors[i])+" 1\n"+
	        	"Begin QUAD_STRIP\n"+
		        "Vertex "+x1+" 1\n" +
			"Vertex "+x1+" 0\n"+
			"Vertex "+x2+" 1\n"+
			"Vertex "+x2+" 0\n"+
		        "End\n");
	        }
	    } else {
	        bgcall = (
			  "Color "+ColorUtil.colorGLString(bgColor)+" 1\n" +
			  "Begin QUAD_STRIP\n"+
			  "Vertex 1 1\nVertex 1 0\n"+
			  "Vertex 0 1\nVertex 0 0\n"+
			  "End\n");
	    }
    
	    setup = GLRen.createCallList(
		"PushAttrib CURRENT_BIT ENABLE_BIT\n"+
                "Disable TEXTURE_2D\n");

	    draw = GLRen.createCallListBoxCoorded(bgcall);

	    if (drawBorder) 
		border = GLRen.createCallListBoxCoorded(glBorderString());

	    teardown = GLRen.createCallList(
                "PopAttrib\n"
		    );
	}

        vs.map.put(setup);
	vs.map.put(draw, coordsys1);
	if(drawBorder) vs.map.put(border, coordsys1);
        vs.map.put(teardown);
	return 0;
    }

    protected String glBorderString() {
	String glString = "";
	String glBorderColorString = "0 0 0";
	if (borderColor != null)
	    glBorderColorString = ColorUtil.colorGLString(borderColor);
	if(!isSquare)
	    glString += (
		     "Color "+glBorderColorString+" 1\n"+
		     "Begin QUAD_STRIP\n"+
		     "Vertex 1 .06\n"+
		     "Vertex 1 0\n"+
		     "Vertex 0 .06\n"+
		     "Vertex 0 0\n"+
		     "End\n"+
		     "Begin QUAD_STRIP\n"+
		     "Vertex 1 1\n"+
		     "Vertex 1 0\n"+
		     "Vertex .98 1\n"+
		     "Vertex .98 0\n"+
		     "End\n"+
		     "Begin QUAD_STRIP\n"+
		     "Vertex 1 1\n"+
		     "Vertex 1 .94\n"+
		     "Vertex 0 1\n"+
		     "Vertex 0 .94\n"+
		     "End\n"+
		     "Begin QUAD_STRIP\n"+
		     "Vertex .02 1\n"+
		     "Vertex .02 0\n"+
		     "Vertex 0 1\n"+
		     "Vertex 0 0\n"+
		     "End\n" );
	else
	    glString += (
		     "Color "+glBorderColorString+" 1\n"+
		     "Begin QUAD_STRIP\n"+
		     "Vertex 1 .06\n"+
		     "Vertex 1 0\n"+
		     "Vertex 0 .06\n"+
		     "Vertex 0 0\n"+
		     "End\n"+
		     "Begin QUAD_STRIP\n"+
		     "Vertex 1 1\n"+
		     "Vertex 1 0\n"+
		     "Vertex .94 1\n"+
		     "Vertex .94 0\n"+
		     "End\n"+
		     "Begin QUAD_STRIP\n"+
		     "Vertex 1 1\n"+
		     "Vertex 1 .94\n"+
		     "Vertex 0 1\n"+
		     "Vertex 0 .94\n"+
		     "End\n"+
		     "Begin QUAD_STRIP\n"+
		     "Vertex .06 1\n"+
		     "Vertex .06 0\n"+
		     "Vertex 0 1\n"+
		     "Vertex 0 0\n"+
		     "End\n" );
	return glString;
    }
}

