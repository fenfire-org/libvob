/*
MipmapArray.java
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

package org.nongnu.libvob.gl.virtualtexture;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;
import org.nongnu.libvob.util.Background;
import org.nongnu.libvob.util.ThreadBackground;
import java.awt.Dimension;
import java.io.*;
import java.util.*;
import java.util.zip.*;


/** (Semi-internal): A mipmap array stored in memory, with a given maximum detail
 * level.
 * LOCKING: synchronization of this whole object.
 */
public class MipmapArray {
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println("MipmapArray: "+s); }

    /** The minimum level that this object should
     * contain data for.
     */
    public final int minLevel;

    /** The levels.
     */
    public byte[][] levels;

    /** The number of bytes of data
     * in a level.
     * -1 = should be loaded, 0 = couldn't load,
     *  greater than 0 = contains the data.
     */
     final int[] levelSizes;

    /** What virtual texture the data in each level array is for.
     */
     final VirtualTexture[] virtualTextures;

    /** Create a mipmap array.
     * @param nlevels The number of levels, total
     * @param minLevel The minimum index with a reserved byte[] for the level
     */
    MipmapArray(int nlevels, int minLevel) {

	levels = new byte[nlevels][];
	levelSizes = new int[nlevels];
	virtualTextures = new VirtualTexture[nlevels];

	this.minLevel = minLevel;
	for(int i=minLevel; i<levels.length; i++)
	    levels[i] = new byte[0];
    }

    public void clear() {
	for(int i=0; i<levels.length; i++)
	    virtualTextures[i] = null;
    }

    /** If data is scheduled to be loaded but has not 
     * yet been loaded, load it.
     * This is separated from teximageData because
     * we can run this in a non-gl thread, allowing a speed
     * boost especially on SMP systems.
     * @return true, if something was done.
     */
    public boolean loadData() throws IOException {
	// Start from the small levels; having a large
	// level without the small ones is useless
	for(int i=levels.length-1; i>=0; i--) {
	    synchronized(MipmapArray.this) {
		if(virtualTextures[i] != null && levelSizes[i] == -1) {
		    // load this level
		    // This may change the member we locked,
		    // but it's ok at this point.
		    levels[i] = 
			virtualTextures[i].mipzipFile.getLevelData(i,
				    levels[i]);
		    // Mark it loaded by setting the size
		    levelSizes[i] = 
			virtualTextures[i].mipzipFile.getLevelSize(i);
		    if(dbg)
			pa("Loaded "+i+" "+levels[i]+
				    " "+levelSizes[i]
				 + " "+virtualTextures[i].mipzipFile.getFile()
				    );
		    return true;
		}
	    }
	    Thread.yield();
	}
	return false;
    }

    public void setLoadRequest(int level, VirtualTexture virtualTexture) {
	synchronized(levels[level]) {
	    levelSizes[level] = -1;
	    virtualTextures[level] = virtualTexture;
	}
    }
}

