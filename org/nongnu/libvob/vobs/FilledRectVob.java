/*
FilledRectVob.java
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
import org.nongnu.libvob.util.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Graphics;

/** A vob drawing a filled rectangle.
 */
public class FilledRectVob extends AbstractVob {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    protected Color color;

    public FilledRectVob(Color color) {
	this.color = color;
    }

    public static FilledRectVob newInstance(Color color) {
	FilledRectVob vob = (FilledRectVob)FACTORY.object();
	vob.color = color;
	return vob;
    }

    public void render(Graphics g, boolean fast,
		       Vob.RenderInfo info1, Vob.RenderInfo info2) {
	Color oldfg = g.getColor();
	g.setColor(info1.fade(color));

	g.fillRect((int)info1.x, (int)info1.y, (int)info1.width, (int)info1.height);

	g.setColor(oldfg);
    }

    protected static Vob setup, teardown;
    protected Vob draw;

    public void chg() {
	draw = null;
    }

    public int putGL(VobScene vs, int coordsys1) {
        if(dbg) pa("Addtolistgl rectbg "+coordsys1);
	if(setup == null) {
	    setup = GLRen.createCallList(
		"PushAttrib CURRENT_BIT ENABLE_BIT\n"+
                "Disable TEXTURE_2D\n");

	    teardown = GLRen.createCallList(
                "PopAttrib\n"
		    );
	}
	if(draw == null) {
	    String s = (
			"Color "+ColorUtil.colorGLString(color)+" 1\n" +
			"Begin QUAD_STRIP\n"+
			"Vertex 1 1\nVertex 1 0\n"+
			"Vertex 0 1\nVertex 0 0\n"+
			"End\n");
    
	    draw = GLRen.createCallListBoxCoorded(s);
	}

        vs.map.put(setup);
	vs.map.put(draw, coordsys1);
        vs.map.put(teardown);
	return 0;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() { 
		return new FilledRectVob(null);
	    }
	};
}

