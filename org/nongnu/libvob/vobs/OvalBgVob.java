/*
OvalBgVob.java
 *    
 *    Copyright (c) 2002, 2003 by Asko Soukka
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
 * Written by Asko Soukka
 */

package org.nongnu.libvob.vobs;

import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;

import org.nongnu.libvob.util.ColorUtil;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.Math;

/** A vob which is a circular background and frame.
 * Draws a filled (possibly with several colors) background circle,
 * surrounded by a circle of the current foreground color.
 */
public class OvalBgVob extends AbstractColorableVob {
    public static boolean dbg = false;
    private static final void pa(String s) { System.out.println(s); }

    /** Background color */
    protected final Color bgColor;

    /** Border drawing flag */
    protected final boolean drawBorder;

    /** Border color */
    protected final Color borderColor;

    static protected float [][] circleGL = new float[360][2];
    static protected boolean circleGLReady = false;
    static protected void prepareCircleGL(float r) {
	if (!circleGLReady) {
	    for (int i=0;i<360;i++) {
		float a = (float)((Math.PI / 180) * (360-i));
		circleGL[i][0] = 0.5f + x(a, r);
		circleGL[i][1] = 0.5f + y(a, r);
	    }
	    circleGLReady = true;
	}
    }
    protected static float x(float angle, float radius) {
	return (float)(Math.cos((double)angle) * radius);
    }
    protected static float y(float angle, float radius) {
	return (float)(Math.sin((double)angle) * radius);
    }

    protected int start;
    protected int sector;
    protected int step;

    /** Constructor shorthands */
    public OvalBgVob() { this(Color.white, true, null); }
    public OvalBgVob(Color bgColor) { this(bgColor, true, null); }
    public OvalBgVob(Color bgColor, boolean drawBorder) { this(bgColor, drawBorder, null); }
    public OvalBgVob(Color bgColor, boolean drawBorder, Color borderColor) {
	this(bgColor, drawBorder, borderColor, 90, 360, 10);
    }
    public OvalBgVob(int start, int sector, int step) {
	this(Color.white, true, null, start, sector, step);
    }
  
    /** Constructor for OvalBgVob.
     * @param bgColor Background color.
     * @param drawBorder Border drawing flag.
     * @param borderColor Border color. If null, default fgColor is used.
     * @param start Start angle of the sector. 0 degree is at clock three.
     *              90 degrees is at clock twelve.
     * @param sector The angle of the sector to be drawn. Max 360 degrees. 
     * @param step Every step:th vertex is drawn. This affects only the
     *             GL implementation. Currently the maximum
     *             resolution is 360 vertexes. XXX this will be removed
     *             after DiceableMesh renderable works.
     */
    public OvalBgVob(Color bgColor, boolean drawBorder, Color borderColor,
		     int start, int sector, int step) {
	super();
	this.bgColor = bgColor;
	this.drawBorder = drawBorder;
	this.borderColor = borderColor;
	this.start = Math.abs(start);
	this.sector = Math.abs(sector);
	this.step = Math.abs(step);
    }

    static Rectangle rect = new Rectangle();

    public void render(Graphics g,
		       boolean fast,
		       Vob.RenderInfo info1,
		       Vob.RenderInfo info2) {
	int mx = (int)info1.x, my=(int)info1.y,
	    mw = (int)info1.width, mh = (int)info1.height;

	Color oldfg = g.getColor();

	// Draw an oval in the background color, wiping out
	// the already drawn stuff a little wider than we will draw.
	g.setColor(info1.getBgColor());
	if(mh >= 14) g.fillOval(mx-2, my-2, mw+4, mh+4);
	else g.fillOval(mx-1, my-1, mw+2, mh+2);

	if(colors == null || colors.length == 0) {
	    g.setColor(bgColor);
	    g.fillOval(mx, my, mw, mh);
	} else {
	    /** Draws colored stripes. Each stripe is composed of
	     * on filled rectangle and two filled arcs. Drawing proceeds from
	     * the boundaries to the centre. Two stripes at time. If there is
	     * odd number of stripes the centered stipes is drawn as two
	     * pieces.  __
	     *         /__ first arc
	     *  one    |  |rectangle
	     *  sripe  |__|
	     *         \__ second arc
	     *
	     */
	    g.setColor(colors[0]);
	    g.fillArc(mx, my, mw, mh, -90, 180);
	    g.setColor(colors[colors.length-1]);
	    g.fillArc(mx, my, mw, mh, 90, 180);
	    int lastColor = colors.length-1;
	    int colorWidth = mw / colors.length;
	    float a = mw/2;
	    float b = mh/2;
	    for(int i=1; i<lastColor; i++) {
		float w = a-i*colorWidth;
		float h = b * (float)Math.sqrt(1-(Math.pow(w,2)/Math.pow(a, 2)));
		int arc = 90 - (int)((180/Math.PI) * Math.atan2(h*(a/b),w));

		// stripe on the left
		g.setColor(colors[i]);
		g.fillRect((int)(mx+(i*colorWidth)), (int)(my+(b-h)),
			   (int)w, (int)(h*2));
		g.fillArc(mx, my, mw, mh, 90, arc);
		g.fillArc(mx, my, mw, mh, -90-arc, arc);
		
		// stripe on the right
		g.setColor(colors[lastColor-1]);
		g.fillRect((int)(mx+a), (int)(my+(b-h)), (int)w, (int)(h*2));
		g.fillArc(mx, my, mw, mh, 90-arc, arc);
		g.fillArc(mx, my, mw, mh, -90, arc);
		
		lastColor--;
	    }
	}
	
	if(drawBorder) {
	    g.setColor(borderColor);
	    g.drawOval(mx, my, mw, mh);
	    if(mh >= 14) {
		/** Heavier border for greter Vobs */
		g.drawOval(mx-1, my-1, mw+2, mh+2);
	    }
	}

	g.setColor(oldfg);
    }

    Vob glStencil, glBorder, glList;

    public int putGL(final VobScene vs, final int coordsys1) {
	if(dbg) pa("Addtolistgl ovalbg "+coordsys1);

	if(glList == null) {
	    if (!circleGLReady) prepareCircleGL(0.5f);
	    String bgcall = "";
	    if(colors != null && colors.length > 0) {
		bgcall += glColorsString();
	    } else bgcall += glRectBgString();
    	    
	    glList = GLRen.createCallListBoxCoorded(
		"PushAttrib CURRENT_BIT ENABLE_BIT\n"+
		"Disable TEXTURE_2D\n"+
      		bgcall +
		"PopAttrib\n"
		);
	}

	if(glStencil == null) {
	    glStencil = GLRen.createCallListBoxCoorded(
		"PushAttrib CURRENT_BIT ENABLE_BIT\n"+
		"Disable TEXTURE_2D\n"+
      		glOvalBgString() +
		"PopAttrib\n"
		);
	}

	if(glBorder == null) {
	    glBorder = GLRen.createCallListBoxCoorded(
		"PushAttrib CURRENT_BIT ENABLE_BIT\n"+
		"Disable TEXTURE_2D\n"+
      		glBorderString() +
		"PopAttrib\n"
		);
	}
	
	org.nongnu.libvob.gl.Stencil.drawStenciled(
	   vs,
	   new Runnable() { public void run() {
	       vs.map.put(glStencil, coordsys1);
	   }},
	   null,
	   null,
	   new Runnable() { public void run() {
	       vs.map.put(glList, coordsys1);
	   }},
	   false
	   );

 	if(drawBorder) vs.map.put(glBorder, coordsys1);
	return 0;
    }

    protected String glBorderString() {
	String glString = "";
	String glBorderColorString = "0 0 0";
	if (borderColor != null)
	    glBorderColorString = ColorUtil.colorGLString(borderColor);
	glString += (
		     "Color "+glBorderColorString+" 1\n" +
		     "Begin LINE_LOOP\n");
	if (sector < 360) glString += ("Vertex 0.5 0.5\n");
	for (int i=0;i<sector;i+=step) {
	    glString += ("Vertex " + circleGL[(i+start)%360][0] +
		       " " + circleGL[(i+start)%360][1] + "\n");
	}
	glString += ("Vertex " + circleGL[(sector-1+start)%360][0] +
		   " " + circleGL[(sector-1+start)%360][1] + "\n" +
		   "End\n");
	return glString;
    }

    protected String glRectBgString() {
	String glString = "";
	glString += (
		     "Color "+ColorUtil.colorGLString(bgColor)+" 1\n" +
		     "Begin QUAD_STRIP\n"+
		     "Vertex 1 1\nVertex 1 0\n"+
		     "Vertex 0 1\nVertex 0 0\n"+
		     "End\n");
	return glString;
    }

    protected String glOvalBgString() {
	String glString = "";
	glString = (
		    "Color "+ColorUtil.colorGLString(bgColor)+" 1\n" +
		    "Begin POLYGON\n");
	if (sector < 360) glString += ("Vertex 0.5 0.5\n");
	for (int i=0;i<sector;i+=step) {
	    glString += ("Vertex " + circleGL[(i+start)%360][0] +
		       " " + circleGL[(i+start)%360][1] + "\n");
	}
	glString += ("Vertex " + circleGL[(sector-1+start)%360][0] +
		     " " + circleGL[(sector-1+start)%360][1] + "\n" +
		     "End\n");
	return glString;
    }

    protected String glColorsString() {
	double w = 1.0 / colors.length;
	String glString = "";
	for(int i=0; i<colors.length; i++) {
	    double x1 = i*w;
	    double x2 = x1 + w;
	    Color c = colors[i];
	    glString += (
	      "Color "+ColorUtil.colorGLString(colors[i])+" 1\n"+
	      "Begin QUAD_STRIP\n"+
	      "TexCoord "+x1+" 1\n"+
	      "Vertex "+x1+" 1\n"+
	      "TexCoord "+x1+" 0\n"+
	      "Vertex "+x1+" 0\n"+
	      "TexCoord "+x2+" 1\n"+
	      "Vertex "+x2+" 1\n"+
	      "TexCoord "+x2+" 0\n"+
	      "Vertex "+x2+" 0\n"+
	      "End\n");
	}
	return glString;
    }
}
