/*
TexManip.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 */
/*
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.util;

/** Various small texture manipulations.
 */
public class TexManip {

    /** "Wipe" a single line for minimum distance.
     */
    static private void wipe(float[] d, int[] mx, int[] my, int x, int y, int dx, int dy, int n, int w) {
	int cmx = -10000, cmy = -10000;
	int clx = -10000, cly = -10000;
	for(int i = 0; i<n; i++) {
	    int cx = x + i*dx;
	    int cy = y + i*dy;
	    int ind = cx + w * cy;

	    if(d[ind] < 0) {
		float dix = cx - clx;
		float diy = cy - cly;
		float dcur = - (float)Math.sqrt(dix*dix + diy*diy);

		if(d[ind] > dcur) {
		    clx = mx[ind];
		    cly = my[ind];
		} else {
		    d[ind] = dcur;
		    mx[ind] = clx;
		    my[ind] = cly;
		}
		cmx = cx;
		cmy = cy;

	    } else {
		float dix = cx - cmx;
		float diy = cy - cmy;
		float dcur = (float)Math.sqrt(dix*dix + diy*diy);

		if(d[ind] < dcur) {
		    cmx = mx[ind];
		    cmy = my[ind];
		} else {
		    d[ind] = dcur;
		    mx[ind] = cmx;
		    my[ind] = cmy;
		}
		clx = cx;
		cly = cy;
	    }
	}
    }

    static public void minDist(byte[] from, short[] to, int w, int h, int scale) {
	float[] d = new float[w*h];
	int[] mx = new int[w*h];
	int[] my = new int[w*h];

	for(int ind = 0; ind < w*h; ind++) {
	    d[ind] = (from[ind] != 0 ? -1000000 : 1000000);
	    mx[ind] = ind % w;
	    my[ind] = ind / w;
	}

	// Reasonable approximation
	for(int i=0; i<4; i++) {
	    for(int x = 0; x < w; x++) 
		wipe(d, mx, my, 	x, 0, 		0, 1, 	w, w);
	    for(int y = 0; y < h; y++) 
		wipe(d, mx, my, 	0, y, 		1, 0, 	h, w);
	    for(int x = 0; x < w; x++) 
		wipe(d, mx, my, 	x, h-1, 	0, -1, 	w, w);
	    for(int y = 0; y < h; y++) 
		wipe(d, mx, my, 	w-1, y, 	-1, 0, 	h, w);
	    // XXX Diagonals...
	}

	for(int ind = 0; ind < w*h; ind++) {
	    float dist = d[ind];
	    dist *= scale;
	    if(dist > 65535) dist = 65535;
	    to[ind] = (short)dist;
	}

    }

    static public void b2s(short[] from, byte[] to, int spacing) {
	for(int i=0; i<from.length; i++) {
	    to[i*spacing] = (byte)(from[i] % 256);
	    to[i*spacing+1] = (byte)(from[i] / 256);
	    if(to[i*spacing+1] == 0 && from[i] < 0)
		to[i*spacing+1] = (byte)255;
	}
    }
}
