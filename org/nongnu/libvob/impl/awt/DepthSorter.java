/*
OrthoDepthSorter.java
 *    
 *    Copyright (c) 2002, Benja Fallenstein
 *                  2004, Matti J. Katila 
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
 * Written by Benja Fallenstein and Matti J. Katila
 */
package org.nongnu.libvob.impl.awt;
import org.nongnu.libvob.gl.*;
import org.nongnu.libvob.*;

final class DepthSorter {
    AWTVobCoorderBase sys;

    int[] sorted = new int[256];
    int nsorted;

    boolean allSorted;

    /** Helper array for the quicksort implementation */
    private int[] helper = new int[256];

    private float[] depth = new float[256];

    DepthSorter(AWTVobCoorderBase sys) {
        this.sys = sys;
    }

    void invalidate() {
	allSorted = false;
    }

    int getN(int i) {
	//System.out.println("cs: "+i+", sys: "+sys.inds[i]+
	//    ", ? "+(sys.inds[i] & (~GL.CSFLAGS)));
	if (sys.inds[i] == -2 ||
	    sys.inds[i] == -1) {
	    switch(sys.inds[i]) {
	    case -2: return 3;
	    case -1: return sys.inds[i+2]+3;
	    }
	} switch(sys.inds[i] & (~GL.CSFLAGS)) {
	case 0:
	case 1:
	case 2:
	case 3: return 3;
	case 4: return 5;
	case 5:
	case 6: return 4;
	case 7:
	case 8:
	case 9: return 3;
	case 10: return 4;
	case 11: 
	case 12: 
	case 13: 
	case 14: 
	case 15: 
	case 16: return 3;
	case 17: 
	case 18: return 4;
	case 19: return 3;
	case 20: return 3;
	case 21: return 4;
	case 22: return 3;
	}
	throw new Error("ARgh - no indexed cs "+(sys.inds[i] & (~GL.CSFLAGS)));
    }
    void sort() {
	if(allSorted) return;

	long m1 = System.currentTimeMillis();
	int n = 0;
	for (int i=sys.numberOfParameterCS; i<sys.ninds; n++) {
	    i += getN(i);
	}
	nsorted = n;

	if(sorted.length < n || sorted.length*4 > n) {
	    sorted = new int[n];
	    helper = new int[n];
	}

	if(depth.length < sys.ninds || depth.length*4 > sys.ninds) {
	    depth = new float[sys.ninds];
	}

	n = 0;
	for (int i=sys.numberOfParameterCS; i<sys.ninds; n++) {
	    sorted[n] = i;
	    depth[i] = getDepth(i);
	    i += getN(i);
	}
	    
	long m2 = System.currentTimeMillis();
	nqscalls = 0; cmpcalls = 0;
	quicksort(0, nsorted);
	long m3 = System.currentTimeMillis();

	allSorted = true;
    }

    private int nqscalls, cmpcalls;

    float [] d = new float[5];
    float getDepth(int cs) {
	/*
	for (int i=0; i<5; i++) d[i] = 0;

	AWTVobCoorderBase.Trans t = sys.getTrans(cs);
	t.transformRect(d);
	float depth = d[4];
	t.pop();

	//sys.check();

	return depth;
	*/

	sys.coordinates.check();
	return sys.coordinates.d(cs);
    }

    int cmp(int cs1, int cs2) {
	cmpcalls++;
	//p("cs1: "+cs1);

	float d1 = depth[cs1], d2 = depth[cs2];

	if(d1 > d2) return -1;
	if(d1 < d2) return 1;
	if(cs1 < cs2) return -1;
	if(cs1 > cs2) return 1;

	return 0;
    }

    /** Sort part of the array, using quicksort.
     *  @param start The start of the range to be sorted.
     *  @param n The length of the range to be sorted.
     */
    void quicksort(int start, int n) {
	nqscalls++;

	if(n <= 1) return;
	if(n == 2) {
	    if(cmp(sorted[start], sorted[start+1]) > 0)
	        exchange(start, start+1);
	}

        int pivot = sorted[start + n/2];
	int l = 0, r = n;

	for(int i=0; i<n; i++) {
	    int s = sorted[start + i];
	    int c = cmp(s, pivot);
	    if(c < 0) { helper[l] = s; l++; }
	    if(c > 0) { r--; helper[r] = s; }
	}

	for(int i=l; i<r; i++)
	    helper[i] = pivot;

	System.arraycopy(helper, 0, sorted, start, n);
	quicksort(start, l);
	quicksort(start+r, n-r);
    }

    void exchange(int l, int r) {
        int tmp = sorted[l]; 
	sorted[l] = sorted[r]; 
	sorted[r] = tmp;
    }
}
