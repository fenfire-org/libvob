/*
OrthoCoorder.java
 *
 *    Copyright (c) 2000-2002, Tuomas Lukka
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
package org.nongnu.libvob.impl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.vobs.SolidBackdropVob;
import java.awt.Color;
import java.util.*;

/** A set of ortho coordinate systems for vobs.
 */

public class OrthoCoorder extends VobCoorder {
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println("OrthoCoorder::"+s); }

    public OrthoCoorder(float width, float height) {
        this.width = width; this.height = height;
	sys = new OrthoCoordsys(width, height);
    }

    static float i(float a, float b, float fract)
	{ return (a + fract * (b-a)); }


    float[] cs1rect = new float[4];
    float[] cs2rect = new float[4];
    float[] wh = new float[4];

    void setInterpInfo(int cs1, OrthoCoorder other, int cs2,
			      float fract,
			      OrthoRenderInfo info
			) {
	OrthoCoordsys sys1 = sys, sys2 = other.sys;
	
	sys1.getAbsoluteRect(cs1, cs1rect);
	sys2.getAbsoluteRect(cs2, cs2rect);
	
	info.setCoords(sys1.depth[cs1],
		       i(cs1rect[0], cs2rect[0], fract),
		       i(cs1rect[1], cs2rect[1], fract),
		       i(cs1rect[2], cs2rect[2], fract),
		       i(cs1rect[3], cs2rect[3], fract),
		       i(sys1.sx[cs1], sys2.sx[cs2], fract),
		       i(sys1.sy[cs1], sys2.sy[cs2], fract));
    }

    void setInfo(int cs, OrthoRenderInfo info) {
	setInterpInfo(cs, this, cs, 0, info);
    }

    static float[] rect = new float[4];
    public boolean contains(int cs, float px, float py, float[] internalcoords) {
	    rect[0] = 0;
	    rect[1] = 0;
	    rect[2] = sys.w[cs];
	    rect[3] = sys.h[cs];

	    sys.transformRect(cs, rect);

	    if(px >= rect[0] && py >= rect[1] &&
		px < rect[0] + rect[2] && py < rect[1] + rect[3]) {
		if(internalcoords != null) {
		    internalcoords[0] = (px-rect[0])/(rect[2]);
		    internalcoords[1] = (py-rect[1])/(rect[3]);
		}
		return true;
	    }
	    return false;
    }

    public boolean needInterp(int cs1, OrthoCoorder other, int cs2) {
	OrthoCoordsys sys1 = sys, sys2 = other.sys;
	sys1.getAbsoluteRect(cs1, cs1rect);
	sys2.getAbsoluteRect(cs2, cs2rect);

	float 
	    x1 = cs1rect[0], y1 = cs1rect[1], w1 = cs1rect[2], h1 = cs1rect[3],
	    x2 = cs2rect[0], y2 = cs2rect[1], w2 = cs2rect[2], h2 = cs2rect[3];

	if(Math.abs(x1 - x2) + Math.abs(y1 - y2) + 
	   Math.abs(w1 - w2) + Math.abs(h1 - h2) > 5) // heuristic
	    
	    return true;
	else
	    return false;
    }

    float width, height;

    OrthoCoordsys sys;
    float maxdepth = 0;

    public int orthoBox(int into, float depth, float x, float y, float sx, float sy, float w, float h) {
	if(maxdepth < depth) maxdepth = depth;
	return sys.add(into, depth, x, y, sx, sy, w, h);
    }
    public int ortho(int into, float depth, float x, float y, float sx, float sy) {
	return orthoBox(into, depth, x, y, sx, sy, 1, 1);
    }

    /** place buoys on circle
     * @param into the cs where to make the translation and box.
     *              XXX currently doesn't use transformation at all.
     * @param anchor the anchor of the buoy.
     * @param cx and cy are the coords of the circle's center point
     * @param rad radius from the (cx, cy) point
     * @param px and py are coords for the point of affine.
     * @param min the size of min w/h box for buoy.
     * @param max the size of max width/height of buoy
     *
     * @return boxCS for the buoy fragment.
     */
    public int buoyOnCircle(int into, int anchor, float cx, float cy, float rad,
			     float px, float py, float min, float max)
    {
	// check how far from center point anchor is
	float len = 0;
	float [] size = new float[2];
	getSqSize(anchor, size);

	float[] pt = new float[3];
	float[] xy = transformPoints3(anchor, pt, null);
	
	float x = xy[0] + size[0]/2;
	float y = xy[1] + size[1]/2;

	// distance/legth from center to anchor
	len = (float)Math.sqrt( (x-cx)*(x-cx) + (y-cy)*(y-cy) );

	// if len is longer than rad - do nothing.
	int cs = anchor;


	// project and translate buoy
	if (len <= rad) { 
	    float boxSize = max - (max-min)*(len/rad);
		
	    float ux = x - px;
	    float uy = y - py;
	    float vx = px - cx;
	    float vy = py - cy;
	    float rad2 = rad*rad;
	    float A = ux * ux / rad2 + uy * uy / rad2;
	    float B = ux * vx / rad2 + uy * vy / rad2;
	    float C = vx * vx / rad2 + vy * vy / rad2 - 1;
	    float t = (float)(-B + Math.sqrt(B * B - A * C)) / A;

	    x = px + t * ux;
	    y = py + t * uy;
	    
	    cs = orthoBox(0, 1+(len/rad)*99f, // most close go up - others down.
			  x-boxSize/2, y-boxSize/2, 
			  1,1, boxSize,boxSize ); 
	}
	return cs;
    }

    /** This operation is a transformation h,
     * for which h(x) = f(g(x)) always.
     */
    public int concat(int f, int g) {
	rect[0] = 0;
	rect[1] = 0;
	rect[2] = sys.w[g];
	rect[3] = sys.h[g];

	sys.transformRect(g, rect);
	if (dbg) for (int i=0; i<rect.length; i++) pa("g: "+i + ": "+ rect[i]);
	return ortho(f, f,
		     rect[0], rect[1], 
		     rect[2], rect[3]);
    }


    public void setOrthoBoxParams(int cs, float depth, float x, float y, float sx, float sy, float w, float h) {
	if(maxdepth < depth) maxdepth = depth;
	sys.setParams(cs, depth, x, y, sx, sy, w, h);
    }
    public void setOrthoParams(int cs, float depth,
		float x, float y, float w, float h) {
	setOrthoBoxParams(cs, depth, x, y, w, h, 1, 1);
    }

    public void getSqSize(int cs, float[] into) {
	sys.getSqSize(cs, into);
    }

    /** Remove all rectangles (except root rect);
     *  start creating new coordsys from start.
     */
    public void clear() {
        sys.clear(width, height);
    }

    public void activate(int cs) {
	sys.active[cs] = true;
    }

    // Stupid implementation...
    public int getCSAt(int parent, float x, float y, float[] internalcoords) {
	sys.sorter.sort();
	int[] sorted = sys.sorter.sorted;
	int nsorted = sys.sorter.nsorted;

	for(int i=nsorted-1; i>=0; i--) {
	    if(sys.active[sorted[i]] && 
	       contains(sorted[i], x, y, internalcoords))
 		return sorted[i];
	}
	return -1;
    }

    /** Render the vobs, fract towards the interpTo coordinates.
     */
    public void renderVobs(final DefaultVobMap map, final OrthoCoorder interpTo, final int[] interpList,
			   final float fract, java.awt.Graphics g, Color fg)
    {
	if(interpTo != null && interpList == null) {
	    System.out.println("map "+map+" interpTo "+interpTo+" fract "+fract+" fg "+fg);
	    throw new NullPointerException("interpList is null even though interpTo != null");
	}

	sys.sorter.sort();
	if(dbg) dump();
        if(dbg) map.dump();
        Color bg;
        Vob bgvob = map.getVobByCS(0);
        if(bgvob instanceof SolidBackdropVob) {
            bg = ((SolidBackdropVob)bgvob).color;
            if(dbg) pa("Background color: "+bg);
        } else {
            bg = Color.white;
            if(dbg) pa("NO SOLIDBG VOB: Fall back on white bg color");
        }
	OrthoRenderInfo info = new OrthoRenderInfo(bg, maxdepth);
	OrthoRenderInfo info2 = new OrthoRenderInfo(bg, maxdepth);
	DefaultVobMap.RenderInfoSetter setter = new DefaultVobMap.RenderInfoSetter() {
	    public boolean set(Vob.RenderInfo info, int my) {
		int other = -1;
		if(interpTo != null) {
		    if(dbg) pa("...interpTo != null");
                    if(my == 0) {
                        if(dbg) pa("...my == 0.");
                        setInterpInfo(my, interpTo, 0, fract,
                                      (OrthoRenderInfo) info);
                        return true;
                    }
                    
		    try {
		        other = interpList[my];
		        if(other < 0) return false;
		    } catch(ArrayIndexOutOfBoundsException _) {
		        // XXX Not all coordsys must be in the matcher.
			// Therefore, it is legal for the matcher
			// to return an array that is too short.
			return false;
		    }

		    setInterpInfo(my, interpTo, other, fract,
			          (OrthoRenderInfo)info);
		} else {
		    if(dbg) pa("...interpTo == null");
		    setInfo(my, (OrthoRenderInfo)info);
		}
		if(dbg) pa("...ok.");
		return true;
	    }
	};
	if(dbg) pa("Start rendering.");
	int[] sorted = sys.sorter.sorted;
	int nsorted = sys.sorter.nsorted;
	java.awt.Shape noClip = g.getClip();
	int lastClip = map.renderCS(0, info, g, setter, info2, 0, noClip,
				    null, interpList);
	for(int i=0; i<nsorted; i++) {
	    if(sorted[i]==0) continue;
	    if(dbg) pa("...set: "+sorted[i]);
	    if(setter.set(info, sorted[i])) {
	        if(dbg) pa("...render: "+sorted[i]);
	        lastClip = map.renderCS(sorted[i], info, g, setter, info2,
					lastClip, noClip, null, interpList);
	    }
	}
	if(dbg) pa("End rendervobs");
    }

    public boolean needInterp(VobCoorder interpTo0, int[] interpList) {
        OrthoCoorder interpTo = (OrthoCoorder)interpTo0;
	for(int my=1; my<sys.nsys; my++) {
	    int other;

	    try {
		other = interpList[my];
	    } catch(IndexOutOfBoundsException _) {
		continue;
	    }

	    if(other > 0)
	        if(needInterp(my, interpTo, other)) return true;
	}
	return false;
    }

    public Vob.RenderInfo getRenderInfo(int cs) {
	if(dbg) pa("GetRenderinfo "+cs);
	OrthoRenderInfo info = new OrthoRenderInfo();
	setInfo(cs, info);
	return info;
    }

    public float[] transformPoints3(int withCS, float[] pt, float[]into) {
	if(into == null)
	    into = new float[pt.length];
	float[] rect = new float[] { 0, 0, 1, 1 };
	sys.transformRect(withCS, rect);

	float ox = rect[0];
	float oy = rect[1];
	float sx = rect[2];
	float sy = rect[3];
	for(int i=0; i<pt.length; i+=3) {
	    into[i + 0] = ox + sx * pt[i + 0];
	    into[i + 1] = oy + sy * pt[i + 1];
	    into[i + 2] = sys.depth[withCS] + pt[i + 2];
	}
	return into;
    }

    public float[] inverseTransformPoints3(int withCS, float[] pt, 
					   float[]into) {
	if(into == null)
	    into = new float[pt.length];
	System.arraycopy(pt, 0, into, 0, pt.length);
	sys.inverseTransformPoints3(withCS, into);
	return into;
    }



    public void dump() {
	pa("OrthoCoorder: "+sys.nsys);
	/*for(int i=0; i<sys.nsys; i++) {
	    pa("  "+i);
	}*/
	sys.sorter.sort();
	pa("Depth sorted:");
	for(int i=sys.sorter.nsorted-1; i>=0; i--)
	    pa("  "+sys.sorter.sorted[i]);
    }
}
