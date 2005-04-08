/*
SimpleConnection.java
 *
 *    Copyright 2002, Benja Fallenstein
 *    Portions Copyright (c) 2001, Tuomas Lukka
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
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;
import org.nongnu.libvob.util.ColorUtil;
import java.awt.*;

/** A plain line drawn between two coordinate sets.
 */

public class SimpleConnection extends AbstractVob {

    float x0, y0, x1, y1;
    Color color;

    /**
     *  @param x0,y0 Coordinate inside first coord system
     *  @param x1,y1 Coordinate inside second coord system
     */
    public SimpleConnection(float x0, float y0, float x1, float y1) {
	this(x0, y0, x1, y1, null);
    }

    /**
     *  @param x0,y0 Coordinate inside first coord system
     *  @param x1,y1 Coordinate inside second coord system
     */
    public SimpleConnection(float x0, float y0, float x1, float y1,
                            Color color) {
	super();
	this.x0 = x0;
	this.y0 = y0;
	this.x1 = x1;
	this.y1 = y1;
	this.color = color;
    }

    public static SimpleConnection newInstance(float x0, float y0,
					       float x1, float y1, 
					       Color color) {
	SimpleConnection c = (SimpleConnection)FACTORY.object();
	c.x0 = x0; c.y0 = y0; c.x1 = x1; c.y1 = y1;
	c.color = color;
	return c;
    }

    public void render(Graphics g,
				boolean fast,
				Vob.RenderInfo info1,
				Vob.RenderInfo info2) {
	if(fast) return;
	if(color != null) {
	    if(info1.depth > info2.depth)
		g.setColor(info1.fade(color));
	    else
		g.setColor(info2.fade(color));
	}
	int x0r = info1.box_x(x0), y0r = info1.box_y(y0),
	    x1r = info2.box_x(x1), y1r = info2.box_y(y1);
	// AWT pen "hangs down and to the right", but vobs don't,
	// so substract one from the lower and rightmost values
	g.drawLine(x0r-(x0r>x1r?1:0), y0r-(y0r>y1r?1:0), 
		   x1r-(x1r>x0r?1:0), y1r-(y1r>y0r?1:0));
    }

    public Vob glsetup;
    Vob setColor;
    Vob line;
    public Vob glteardown;

    public int putGL(VobScene vs, int coordsys1, int coordsys2) {
	if(line == null) line = GLRen.createLineConnector(x0, y0, x1, y1);
	if(color != null && setColor == null)
	    setColor = GLRen.createCallList("Color "+ColorUtil.colorGLString(color)+" 1\n");

	if(glsetup != null) vs.map.put(glsetup);
	if(setColor != null) vs.map.put(setColor);
	vs.map.put(line, coordsys1, coordsys2);
	if(glteardown != null) vs.map.put(glteardown);
	return 0;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() { 
		return new SimpleConnection(0, 0, 0, 0);
	    }
	};
}


