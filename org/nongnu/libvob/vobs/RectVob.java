/*
RectVob.java
 *    
 *    Copyright (c) 2002-2003, Tuomas Lukka
 *    Copyright (c) 2004-2005, Benja Fallenstein 
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
 * Written by Tuomas Lukka and Benja Fallenstein
 */
package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;
import org.nongnu.libvob.util.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Graphics;

/** A vob drawing the outline of a rectangle.
 */
public class RectVob extends AbstractVob {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    protected Color color;
    protected float lineWidth;

    protected boolean is3d, raised;

    public RectVob(Color color, float lineWidth) {
	this(color, lineWidth, false, false);
    }

    public RectVob(Color color, float lineWidth, boolean raised) {
	this(color, lineWidth, true, raised);
    }

    public RectVob(Color color, float lineWidth, boolean is3d, 
		   boolean raised) {
	this.color = color;
	this.lineWidth = lineWidth;
	this.is3d = is3d;
	this.raised = raised;
    }

    public static RectVob newInstance(Color color, float lineWidth) {
	return newInstance(color, lineWidth, false, false);
    }

    public static RectVob newInstance(Color color, float lineWidth, 
				      boolean raised) {
	return newInstance(color, lineWidth, true, raised);
    }

    public static RectVob newInstance(Color color, float lineWidth,
				      boolean is3d, boolean raised) {
	RectVob vob = (RectVob)FACTORY.object();
	vob.color = color;
	vob.lineWidth = lineWidth;
	vob.is3d = is3d;
	vob.raised = raised;
	return vob;
    }


    static Rectangle rect = new Rectangle();

    public void render(Graphics g, boolean fast,
		       Vob.RenderInfo info1, Vob.RenderInfo info2) {
	Color oldfg = g.getColor();
	g.setColor(info1.fade(color));

	float xlw = lineWidth * info1.scaleX;
	float ylw = lineWidth * info1.scaleY;
	if(xlw < 1) xlw = 1; 
	if(ylw < 1) ylw = 1;

	float lw = (xlw > ylw) ? xlw : ylw;

	for(int i=0; i<lw; i++) {
	    float fact = i/lw;
	    int xoffs = (int)(xlw*fact), yoffs = (int)(ylw*fact);
	    if(!is3d)
		g.drawRect((int)info1.x+xoffs, (int)info1.y+yoffs, 
			   (int)info1.width-2*xoffs-1, (int)info1.height-2*yoffs-1);
	    else
		g.draw3DRect((int)info1.x+xoffs, (int)info1.y+yoffs, 
			     (int)info1.width-2*xoffs-1, (int)info1.height-2*yoffs-1,
			     raised);
	}

	g.setColor(oldfg);
    }

    
    protected Vob vob;

    public void chg() {
	vob = null;
    }

    public int putGL(VobScene vs, int coordsys1) {
	if(vob == null) {
	    Color tlColor, brColor; // top/left, bottom/right colors

	    if(is3d) {
		if(raised) {
		    tlColor = color.brighter(); brColor = color.darker();
		} else {
		    tlColor = color.darker(); brColor = color.brighter();
		}
	    } else {
		tlColor = brColor = color;
	    }

	    float[] tlc = ColorUtil.getRGBColorComponents(tlColor, null);
	    float[] brc = ColorUtil.getRGBColorComponents(brColor, null);

	    vob = GLRen.createNonFilledRectangle(lineWidth, 
						 tlc[0], tlc[1], tlc[2], 1, 
						 brc[0], brc[1], brc[2], 1);
	}

        vs.map.put(vob, coordsys1);
	return 0;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() { 
		return new RectVob(null, 0);
	    }
	};
}

