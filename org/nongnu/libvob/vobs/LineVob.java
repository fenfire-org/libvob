/*   
LineVob.java
 *    
 *    Copyright (c) 2001-2002, Tuomas Lukka
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
 * Written by Tuomas Lukka
 */
package org.nongnu.libvob.vobs;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.gl.*;
import org.nongnu.libvob.*;
import java.awt.*;

/** A plain line drawn in given color.
 * This vob draws a line between the given points in the coordinate system
 * it is put into.
 * <p>
 * This is rather inefficient if you need to draw several lines, but can be useful
 * in some situations.
 */

public class LineVob extends AbstractVob {

    int x0, y0, x1, y1;
    Color color;

    public LineVob(int x0, int y0, int x1, int y1) {
	this(x0, y0, x1, y1, null);
    }

    public LineVob(int x0, int y0, int x1, int y1, Color color) {
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
		   info1.box_x(x1), info1.box_y(y1));
    }

    Vob dlist;
    public int putGL(VobScene vs, int coordsys1) { 
	if(dlist == null)
	    dlist = GLCache.getCallListBoxCoorded(
		    "PushAttrib CURRENT_BIT ENABLE_BIT\nColor "+ColorUtil.colorGLString(color)+"\n"+
		    "Disable TEXTURE_2D\nBegin LINE_STRIP\nVertex "+x0+" "+y0+
		    "\nVertex "+x1+" "+y1+"\nEnd\nPopAttrib\n");
	
	vs.map.put(dlist, coordsys1);
	return 0;
    }

}


