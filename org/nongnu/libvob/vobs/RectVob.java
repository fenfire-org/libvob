/*
RectVob.java
 *    
 *    Copyright (c) 2002-2003, Tuomas Lukka
 *    Copyright (c) 2004, Benja Fallenstein
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
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Graphics;

/** A vob drawing the outline of a rectangle.
 */
public class RectVob extends AbstractVob implements Obs, Replaceable {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    protected final Model colorModel;
    protected final float lineWidth;

    protected final boolean is3d, raised;

    public RectVob(Color color, float lineWidth, boolean is3d,
		   boolean raised) {
	this(new ObjectModel(color), lineWidth, is3d, raised);
    }

    public RectVob(Model colorModel, float lineWidth) {
	this(colorModel, lineWidth, false, false);
    }

    public RectVob(Model colorModel, float lineWidth, boolean raised) {
	this(colorModel, lineWidth, true, raised);
    }

    public RectVob(Model colorModel, float lineWidth, boolean is3d, 
		   boolean raised) {
	this.colorModel = colorModel;
	this.lineWidth = lineWidth;
	this.is3d = is3d;
	this.raised = raised;

	colorModel.addObs(this);
    }


    public Object instantiateTemplate(java.util.Map map) {
	if(map.get(this) != null) return map.get(this);

	Model newColorModel = (Model)colorModel.instantiateTemplate(map);
	if(newColorModel == colorModel) {
	    map.put(this, this);
	    return this;
	}

	Vob newThis = new RectVob(newColorModel, lineWidth, is3d, raised);
	map.put(this, newThis);
	return newThis;
    }
    public java.util.Set getTemplateParameters() {
	return colorModel.getTemplateParameters();
    }
    public Object getTemplateParameter(Object key) {
	return colorModel.getTemplateParameter(key);
    }
    public void setTemplateParameter(Object key, Object value) {
	colorModel.setTemplateParameter(key, value);
    }


    static Rectangle rect = new Rectangle();

    public void render(Graphics g, boolean fast,
		       Vob.RenderInfo info1, Vob.RenderInfo info2) {
	Color oldfg = g.getColor();
	g.setColor(info1.fade((Color)colorModel.get()));

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
	    Color color = (Color)colorModel.get();
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
}

