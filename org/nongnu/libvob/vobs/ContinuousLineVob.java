/*
ContinuousLineVob.java
 *    
 *    Copyright (c) 2002, Tuomas Lukka, Tero Maeyraenen (ae = a umlaut! Fixed for gcj), Asko Soukka and Matti Katila
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
 * Written by Tuomas Lukka, Tero Maeyraenen (ae = a umlaut! Fixed for gcj), Asko Soukka and Matti Katila
 */

package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;
import org.nongnu.libvob.util.*;
import java.awt.*;


/** Continuous line needs x,y,z points in float array.
 */
public class ContinuousLineVob extends AbstractVob {
    static private void pa(String s) { System.out.println("ContinuousLineVob::"+s); }
    public static boolean dbg = false;

    static private GL.Texture tex;
    static private void loadTex() {
	if (tex == null) {
	    String[] texparam = new String[0];
	    tex = GL.createTexture(); 
	    //tex.shade_all_levels(256, 256, 0, 1, "ALPHA", "ALPHA", "line", texparam);
	    tex.shade(128, 128, 0, 1, "ALPHA", "ALPHA", "line", texparam);
	}
    }

    private float width;
    private float[] points;
    private Color color;
    private boolean chain;
    private int joinStyle;

    /** Bevel shape model of the bending with ContinuousLine
     */
    static public final int BEVEL = 100; 
    /** Miter shape model of the bending with ContinuousLine
     */
    static public final int MITER = 200;
    /** Round shape model of the bending with ContinuousLine
     */
    static public final int ROUND = 300;

    public ContinuousLineVob(float width, float[] points) {
	this(width, points, false, BEVEL, java.awt.Color.black);
    }

    /** @param points Points in 3D model. One point is x,y and z.
     */
    public ContinuousLineVob(float width, float[] points, boolean chain, int joinStyle, Color color) {
	super();
	this.width = width;
	this.points = points;
	this.chain = chain;
	this.joinStyle = joinStyle;
	this.color = color;
    }


    // AWT implementation
    // ==================

    public void render(Graphics g, 
				boolean fast,
				Vob.RenderInfo info1,
				Vob.RenderInfo info2) {
	if(fast) return;
	Color oldColor = g.getColor();
	if(color != null) g.setColor(color);
	for (int i=3; i+2<points.length; i+=3) {
	    g.drawLine(info1.box_x(points[i-3]), info1.box_y(points[i-2]),
		       info1.box_x(points[i]), info1.box_y(points[i+1]));
	}
	g.setColor(oldColor);
    }


    // GL implementation
    // =================

    private Vob initVob;
    private Vob lineVob;
    private Vob finishVob;

    public int putGL(VobScene vs, int coordsys1) { 
	/*
	 * Mudyc's original implementation.
	 * Left in here until he reads this and the corrected implementation and understands
	 * what was wrong.
	if (needInit) loadTex();

	vs.map.put(GLCache.getCallList(
	    "PushAttrib ENABLE_BIT \n"+
            "Color "+ ColorUtil.colorGLString(color)
	    ));
	if (dbg) pa("tex:"+tex+", w:"+width+", jS:"+joinStyle+", chain:"+chain+", points"+points);
 	GLRen.ContinuousLine cl = 
	    GLRen.createContinuousLine(tex.getTexId(), width, joinStyle, chain, points);
	vs.map.put(cl, coordsys1);
	vs.map.put(GLCache.getCallList("PopAttrib"));
	return  0;
	*/
	if (tex == null) loadTex();
	if(initVob == null) {
	    initVob = GLCache.getCallList(
		"PushAttrib ENABLE_BIT \n"+
		"Color "+ ColorUtil.colorGLString(color)
		);
	    if (dbg) pa("tex:"+tex+", w:"+width+", jS:"+joinStyle+", chain:"+chain+", points"+points);
	    lineVob = 
		GLRen.createContinuousLine(tex.getTexId(), width, joinStyle, chain, points);
	    finishVob = GLCache.getCallList("PopAttrib");
	}

	vs.map.put(initVob);
	vs.map.put(lineVob, coordsys1);
	vs.map.put(finishVob);
	return  0;

    }
}


