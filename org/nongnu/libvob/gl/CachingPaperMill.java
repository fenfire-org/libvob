/*
CachingPaperMill.java
 *    
 *    Copyright (c) 2003, : Tuomas J. Lukka
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
 * Written by : Tuomas J. Lukka
 */

package org.nongnu.libvob.gl;
import org.nongnu.libvob.GraphicsAPI;

/** A papermill instance that caches a predetermined
 * number of papers.
 */
public class CachingPaperMill extends PaperMill {

    private PaperMill realPaperMill;
    private IntCache realCache;
    private IntCache optCache;

    public CachingPaperMill(PaperMill realPaperMill, int n) {
	this.realPaperMill = realPaperMill;
	this.realCache = new IntCache(n);
	this.optCache = new IntCache(n);
    }

    private class IntCache {
	private int[] keys;
	private Paper[] values;
	int ind(int n) { 
	    int i = n % keys.length;
	    if(i < 0) i += keys.length;
	    return i;
	}
	IntCache(int n) {
	    keys = new int[n];
	    values = new Paper[n];
	}
	Paper get(int val) { 
	    int i = ind(val);
	    if(keys[i] == val) return values[i];
	    return null;
	}
	void put(int val, Paper p) { 
	    int i = ind(val);
	    keys[i] = val;
	    values[i] = p;
	}
    }

    public Paper getOptimizedPaper(int seed) {
	Paper p = optCache.get(seed);
	if(p == null) {
	    p = realPaperMill.getOptimizedPaper(seed);
	    optCache.put(seed, p);
	}
	return p;
    }

    public Paper getPaper(int seed) {
	Paper p = realCache.get(seed);
	if(p == null) {
	    p = realPaperMill.getPaper(seed);
	    realCache.put(seed, p);
	}
	return p;
    }

}
