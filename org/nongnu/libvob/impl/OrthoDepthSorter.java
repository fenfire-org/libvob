/*
OrthoDepthSorter.java
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
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.impl;
import org.nongnu.libvob.*;
final class OrthoDepthSorter {
    OrthoCoordsys sys;

    int[] sorted;
    int nsorted;

    /** Helper array for the quicksort implementation */
    private int[] helper;

    OrthoDepthSorter(OrthoCoordsys sys, int initialSize) {
        this.sys = sys;
	sorted = new int[initialSize];
	helper = new int[initialSize];
	nsorted = 0;
    }

    void clear() {
        nsorted = 0;
	lastSorted = -1;
    }

    private int lastSorted = -1;
    void sort() {
	if (lastSorted == nsorted) return;
        if(nsorted < sys.nsys) {
	    if(sorted.length < sys.nsys) {
	        sorted = new int[sys.depth.length];
		helper = new int[sys.depth.length];
	    }

	    for(int i=nsorted; i<sys.nsys; i++)
	        sorted[i] = i;

	    lastSorted = nsorted = sys.nsys;
	    //javasort();
            quicksort(1, nsorted-1);
	}
    }

    /** Sort the array, using java.util.Arrays' sort.
     *  NOTE: This creates an Integer object for every item in the array
     *  and therefore gives the garbage collector lots of work.
     */
    void javasort() {
	Integer[] is = new Integer[nsorted];
	for(int i=0; i<nsorted; i++)
	    is[i] = new Integer(sorted[i]);
	java.util.Arrays.sort(is, new java.util.Comparator() {
	    public boolean equals(Object a, Object b) { return a.equals(b); }
	    public int compare(Object a, Object b) {
		int i = ((Integer)a).intValue();
		int j = ((Integer)b).intValue();
		return cmp(i, j);
	    }
	});
	for(int i=0; i<nsorted; i++)
	    sorted[i] = is[i].intValue();
    }

    int cmp(int cs1, int cs2) {
	if(sys.depth[cs1] > sys.depth[cs2]) return -1;
	if(sys.depth[cs1] < sys.depth[cs2]) return 1;
	if(cs1 < cs2) return -1;
	if(cs1 > cs2) return 1;
	return 0;
    }

    /** Sort part of the array, using quicksort.
     *  @param start The start of the range to be sorted.
     *  @param n The length of the range to be sorted.
     */
    void quicksort(int start, int n) {
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
        int tmp = sorted[l]; sorted[l] = sorted[r]; sorted[r] = tmp;
    }
}
