/*
OrthoCoordsys.java
 *    
 *    Copyright (c) 2002, Benja Fallenstein
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
 * Written by Benja Fallenstein and Tuomas Lukka
 */
package org.nongnu.libvob.impl;
import org.nongnu.libvob.*;

class OrthoCoordsys {
    OrthoDepthSorter sorter;

    boolean[] active;
    float[] depth, x, y, sx, sy, w, h;
    int[] parent;
    int[] nancestors;

    int[] nextHashtableEntry;

    int nsys;

    OrthoCoordsys(float mw, float mh) {
        this(mw, mh, 50);
    }

    OrthoCoordsys(float mw, float mh, int initialSize) {
	sorter = new OrthoDepthSorter(this, initialSize);

	active = new boolean[initialSize];
	depth = new float[initialSize];
	x = new float[initialSize];
	y = new float[initialSize];
	sx = new float[initialSize];
	sy = new float[initialSize];
	w = new float[initialSize];
	h = new float[initialSize];
        parent = new int[initialSize];
	nancestors = new int[initialSize];
	nextHashtableEntry = new int[initialSize];

	clear(mw, mh);
    }

    void clear(float mw, float mh) {
        active[0] = false;
        depth[0] = 0;
	x[0] = 0; y[0] = 0; sx[0] = mw; sy[0] = mh;
	w[0] = 1; h[0] = 1;
        parent[0] = -1; nancestors[0] = 0;

	nsys = 1;

	sorter.clear();
    }

    int add(int mparent, float mdepth, float mx, float my, float msx, float msy,
					    float mw, float mh) {
	int cs = next();

	active[cs] = false;
        parent[cs] = mparent;
	setParams(cs, mdepth, mx, my, msx, msy, mw, mh);

        nancestors[cs] = nancestors[mparent] + 1;

	nextHashtableEntry[cs] = 0;

	return cs;
    }

    void setParams(int cs, float mdepth, float mx, float my, float msx, float msy,
			float mw, float mh) {
	int mparent = parent[cs];

	sx[cs] = msx;
	sy[cs] = msy;
	w[cs] = mw;
	h[cs] = mh;

	// depth are stored as absolute, not relative to parent
	depth[cs] = depth[mparent] + mdepth;
	// XXX Depth doesn't change right with setXParams later
	x[cs] = mx;
	y[cs] = my;
    }

    public void getSqSize(int cs, float[] into) {
	into[0] = w[cs];
	into[1] = h[cs];
    }

    void transformRect(int cs, float[] rect) {
	if(cs == 0) return;
	rect[0] *= sx[cs];
	rect[1] *= sy[cs];
	rect[2] *= sx[cs];
	rect[3] *= sy[cs];

	rect[0] += x[cs];
	rect[1] += y[cs];
	transformRect(parent[cs], rect);
    }

    void getAbsoluteRect(int cs, float[] into) {
	into[0] = into[1] = 0;
	into[2] = w[cs]; into[3] = h[cs];
	transformRect(cs, into);
    }

    void inverseTransformPoints3(int cs, float[] pt) {
	if(cs == 0) return;
	inverseTransformPoints3(parent[cs], pt);

	for(int i=0; i<pt.length; i+=3) {
	    pt[i+0] -= x[cs];
	    pt[i+1] -= y[cs];
	    
	    pt[i+0] /= sx[cs];
	    pt[i+1] /= sy[cs];
	}
    }

    protected int next() {
        int n = nsys;
        nsys++;
	if(nsys > depth.length) {
	    int o = depth.length, l = depth.length * 2;

	    boolean[] nactive = new boolean[l];
	    float[] ndepth = new float[l];
	    float[] nx = new float[l];
	    float[] ny = new float[l];
	    float[] nsx = new float[l];
	    float[] nsy = new float[l];
	    float[] nw = new float[l];
	    float[] nh = new float[l];
            int[] nparent = new int[l];
	    int[] nnancestors = new int[l];
	    int[] nnext = new int[l];

	    System.arraycopy(active, 0, nactive, 0, o);
	    System.arraycopy(depth, 0, ndepth, 0, o);
	    System.arraycopy(x, 0, nx, 0, o);
	    System.arraycopy(y, 0, ny, 0, o);
	    System.arraycopy(sx, 0, nsx, 0, o);
	    System.arraycopy(sy, 0, nsy, 0, o);
	    System.arraycopy(w, 0, nw, 0, o);
	    System.arraycopy(h, 0, nh, 0, o);
            System.arraycopy(parent, 0, nparent, 0, o);
	    System.arraycopy(nancestors, 0, nnancestors, 0, o);
	    System.arraycopy(nextHashtableEntry, 0, nnext, 0, o);

	    active = nactive;
	    depth = ndepth;
	    x = nx; y = ny; 
	    sx = nsx; sy = nsy; 
	    w = nw; h = nh;
            parent = nparent;
	    nancestors = nnancestors;
	    nextHashtableEntry = nnext;
	}
	return n;
    }
}
