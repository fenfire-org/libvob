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

    public void render(Graphics g,
				boolean fast,
				Vob.RenderInfo info1,
				Vob.RenderInfo info2) {
	if(fast) return;
	if(color != null) g.setColor(color);
	g.drawLine(info1.box_x(x0), info1.box_y(y0), 
		   info2.box_x(x1), info2.box_y(y1));
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
}


