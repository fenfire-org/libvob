/*
MipzipMemoryConsumer.java
 *    
 *    Copyright (c) 2003, : Tuomas J. Lukka
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
 * Written by : Tuomas J. Lukka
 */

package org.nongnu.libvob.gl;
import org.nongnu.libvob.memory.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.ThreadBackground;

/** An adapter between MipzipLoader and MemoryConsumer.
 */
public class MipzipMemoryConsumer implements MemoryConsumer {
    public static boolean dbg = false;
    final static void pa(String s) { System.out.println(s); }
    
    MemoryPartitioner pool;
    MipzipLoader mipzip;

    public int getMaxBytes(float quality) {
	int mb = mipzip.getMemory(
		    mipzip.getLevelForQuality(quality));
	return mb;
    }
    public int setReservation(float priority, int bytes, float quality) {
	if(dbg) pa("MipzipMemcons setres "+mipzip+" "+
		bytes+" "+quality);
	int lq = mipzip.getLevelForQuality(quality);
	int lb = mipzip.getLevelForBytes(bytes);
	int level = Math.max(lb, lq);

	mipzip.setGoalBaseLevel(level, 
			    ThreadBackground.getDefaultInstance(),
			    priority);

	return mipzip.getMemory(level);
    }
    public void loadToBaseLevelSynch(int level) {
	try {
	    mipzip.loadToBaseLevelSynch(level);
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new Error("Error", e);
	}
    }
    public int getReservation() {
	return mipzip.getMemory();
    }
    public float getQuality() {
	return mipzip.getQuality();
    }

    /** The public API: ask for the texture at given
     * importance and quality.
     */
    public GL.Texture getTexture(float importance, float quality) { 
	if(dbg) pa("MipzipMemcons getTexture "+mipzip+" "+
		    importance+" "+quality);
	if(pool != null)
	    pool.request(this, importance, quality);
	return mipzip.getTexture(); 
    }

    public MipzipMemoryConsumer(MemoryPartitioner pool,
			    MipzipLoader mipzip) {
	this.pool = pool;
	this.mipzip = mipzip;
    }

}


