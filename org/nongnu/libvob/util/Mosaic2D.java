/*
Mosaic2D.java
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
import java.util.*;

/** A Malloc for 2D rectangular areas from larger areas.
 * XXX To be made an interface, for potentially more efficient implementations.
 */
public class Mosaic2D {
    public class Tile {
	public final int page;
	public final int x, y, w, h;
	public Tile(int page, int x, int y, int w, int h) {
	    this.page = page;
	    this.x = x;
	    this.y = y;
	    this.w = w;
	    this.h = h;
	}
	public Mosaic2D getMosaic2D() { return Mosaic2D.this; }
    }

    /** The width and height of the papers.
     */
    int pageWidth, pageHeight;

    /** The pages currently required for all the tiles.
     */
    List pages = new ArrayList();

    /** Keep track of allocations on a single page.
     * Currently rather simplistic:
     * Keeps track of a single new row, tries to fit on it,
     * if it doesn't fit, 
     */
    private class Page {
	/** The index of this page.
	 */
	final int index;

	/** The first Y-coordinate of the current row.
	 */
	int firstY = 0;
	/** The first free X-coordinate in the current row.
	 */
	int firstX = 0;
	/** The minimum Y-coordinate currently known 
	 * for the next row.
	 */
	int nextY = 0;

	private Page(int index) {
	    this.index = index;
	}

	/** Try to allocate an area.
	 * @return A Tile, or null if it doesn't fit.
	 */
	public Tile tryAlloc(int w, int h) {
	    // Is this a simple case from the current row?
	    if(w <= pageWidth - firstX &&
	       h <= pageHeight - firstY) {
		Tile ret = new Tile(index, firstX, firstY, w, h);
		firstX += w;
		if(nextY < firstY + h) nextY = firstY + h;
		return ret;
	    }
	    // Otherwise, we need a new row - does it fit?
	    if(h > pageHeight - nextY)
		return null;
	    // Start a new row
	    Tile ret = new Tile(index, 0, nextY, w, h);
	    firstY = nextY;
	    firstX = w;
	    nextY = firstY + h;
	    return ret;
	}
    }
    

    public Mosaic2D(int pageWidth, int pageHeight) {
	this.pageWidth = pageWidth;
	this.pageHeight = pageHeight;
    }

    /** Allocate a tile.
     * Throws an error if the request can't be satisfied.
     */
    public Tile alloc(int w, int h) {
	if(w > pageWidth || h > pageHeight) 
	    throw new Error("Couldn't allocate");
	Tile ret;
	for(int i=0; i<pages.size(); i++) {
	    ret = ((Page)pages.get(i)).tryAlloc(w, h);
	    if(ret != null) return ret;
	}
	Page newPage = new Page(pages.size());
	pages.add(newPage);
	ret = newPage.tryAlloc(w, h);
	if(ret == null) throw new Error("WHAT?!?! FATAL ERROR");
	return ret;
    }

    public int getNPages() {
	return pages.size();
    }
    public int getPageWidth() {
	return pageWidth;
    }
    public int getPageHeight() {
	return pageHeight;
    }
}
