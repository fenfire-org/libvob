/*
FixedIndirectMipzipManager.java
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

/** An IndirectMipzipManager that never changes what's inside a single
 * texture, until that texture gets deleted.
 * Kind-of the opposite of NonDeletingIndirectMipzipManager.
 * <p>
 * XXX setSlotContents_synchronously IS NOT IMPLEMENTED
 */
public class FixedIndirectMipzipManager 
	extends AbstractIndirectMipzipManager 
	implements IndirectMipzipManager 
{
    public static boolean dbg = true;
    private static void pa(String s) { System.out.println("FMZM: "+s); }

    /** The temporary array to use for loading from disk.
     */
    private MipmapArray loadTmp;

    /** The state of a single texture slot.
     * A single VirtualTexture active in this manager gets one slot.
     */
    protected class Slot {
	/** The base level of this slot.
	 */
	int slotLevel;

	/** The index of this slot.
	 */
	int slotIndex;

	/** The texture.
	 */
	GL.Texture texture ;

	/** The virtual image that the image in this slot is currently
	 * associated to.
	 */
	final VirtualTexture virtualTexture;

	/** Bitmap: which levels have been loaded.
	 * Index: absolute levels.
	 */
	final boolean[] levelsLoaded = new boolean[nlevels];

	/** Base level (absolute) currently set.
	 */
	int baselevel;

	/** Do one step for the OpenGL thread.
	 * Only really quick stuff here.
	 */
	public boolean step_openGL() {
	    if(texture == null) {
		texture = GL.createTexture();
		virtualTexture.indirectTexture.setTexture(texture);
		setTexParameters(defaultTexParameters);
		setBaseLevel();
		return true;
	    }
	    
	    for(int i=0; i<slotLevel; i++)
		if(levelsLoaded[i]) {
		    demoteTo(slotLevel);
		    setBaseLevel();
		    return true;
		}
	    return false;
	}

	/** Call the base level setting on the texture.
	 */
	synchronized void setBaseLevel() {
	    for(int i=nlevels-1; i>=0; i--) {
		if(!levelsLoaded[i]) {
		    // Allow the 1x1 mipmap image to be 
		    // reused between textures - avoid 
		    // having an inconsistent state and blacking
		    // out stuff.
		    baselevel = i + 1;
		    if(i == nlevels) i--;
		    texture.setTexParameter("TEXTURE_2D", 
				"TEXTURE_BASE_LEVEL", i +1);
		    if(dbg) pa(""+this+" Baselevel set to "+(i+1));
		    return;
		}
	    }
	    baselevel = 0;
	    texture.setTexParameter("TEXTURE_2D", 
			"TEXTURE_BASE_LEVEL", 0 );
	    if(dbg) pa(""+this+" Baselevel set to 0 - all loaded");
	}


	Slot(VirtualTexture virtualTexture) {
	    this.virtualTexture = virtualTexture;
	}

	protected void demoteTo(int newLevel) {
	    if(true) {
		// Doesn't work right on NV 44.96 - 
		// corrupts these and other textures :( :( :(

		// Put in 1x1 texture images 
		for(int i=0; i<newLevel && i < levelsLoaded.length; i++) {
		    levelsLoaded[i] = false;
		}
		setBaseLevel();
		
		for(int i=0; i<newLevel && i < levelsLoaded.length; i++) {
		    levelsLoaded[i] = false;
		    texture.texImage2D(i, "LUMINANCE", 4, 4, 
			    0, "LUMINANCE", "UNSIGNED_BYTE",
			    new byte[] {0, 0, 0, 0,
			     0, 0, 0, 0,
			     0, 0, 0, 0,
			     0, 0, 0, 0});
		}
	    } else {
	    }
	}

	public void changeLevel(int newLevel) {
	    this.slotLevel = newLevel;
	}

	boolean allLoaded() { 
	    return baselevel <= slotLevel; 
	}

	public void glDelete() {
	    virtualTexture.indirectTexture.setTexture(null);
	    texture.deleteTexture();
	    texture = null;
	}
	void setTexParameters(String[] params) {
	    if(params == null) return;
	    for(int i=0; i<params.length; i+=2) {
		texture.setTexParameter("TEXTURE_2D", params[i], params[i+1]);
	    }
	}


	/** Load data from a byte array.
	 * @param level The **absolute** mipmap level
	 */
	synchronized void loadData(int level, byte[] data, int size) {
	    {
		StringBuffer s = new StringBuffer();
		for(int j=0; j < 24 && j< size; j ++) {
		    s.append(data[j]);
		    s.append(",");
		}
		if(dbg) pa("LoadData: "+this+" "+level+" "+data+" "+size+" "+s);
	    }
	    if(level < slotLevel) 
		throw new Error("Tried to load too high a level");
	    if(size <= 0) 
		throw new Error("LoadData: NO DATA???");

	    /*
		texture.compressedTexImage(level - slotLevel,
			format,
			1, 1, 0,
			8, data);
	    */
	    texture.compressedTexImage(level ,
		    format,
		    levelWidths[level], levelHeights[level], 0,
		    size, data);
	    levelsLoaded[level] = true;
	}

	/** Request loading the missing levels to the given
	 * tmp mipmaparray.
	 */
	synchronized void requestLoad(MipmapArray into) {
	    for(int i=slotLevel; i<nlevels; i++) {
		if(!levelsLoaded[i])
		    into.setLoadRequest(i, virtualTexture);
	    }
	}

	/** Load all relevant levels from the temporary array.
	 */
	synchronized void loadFromTmp(MipmapArray from) {
	    synchronized(from) {
		for(int i=nlevels-1; i>=0; i--) {
		    if(i >= slotLevel &&
			from.levels[i] != null &&
		       (!levelsLoaded[i]) &&
			from.virtualTextures[i] == this.virtualTexture) {
			    if(from.levelSizes[i] > 0) 
				loadData(i, from.levels[i], from.levelSizes[i]);
			    if(from.levelSizes[i] == 0) {
				from.levelSizes[i] = -1;
				from.virtualTextures[i] = null;
			    }
		    }
		}
	    }
	    setBaseLevel();
	}
	public String toString() {
	    return "[Slot: "+slotLevel+" "+" "+virtualTexture+
			" ("+texture.getTexId() +")]";
	}

    }

    
    Slot[][] slots;

    /** Mapping of virtual images to Slot objects.
     */
    HashMap virtualImage2slot = new HashMap();

    Set toBeDeleted = new HashSet();

// ---- External API

    public void setAllocations(int[] ntextures) {
	// For this texture model, setAllocations doesn't make as much sense..

	loadTmp = new MipmapArray(nlevels, 0);

	slots = new Slot[nlevels][];
	for(int i=0; i<nlevels; i++) {
	    slots[i] = new Slot[ntextures[i]];
	}

    }

    private Runnable r_runBg = new Runnable() {
	public void run() {
	    synchronized(FixedIndirectMipzipManager.this) {
		if(dbg) pa("runbg");
		boolean res;
		try {
		    res =loadTmp.loadData();
		} catch(IOException e) {
		    pa("IO EXCEPTION LOADING MIPZIP");
		    res = true;
		}
		if(res) {
		    background.addTask(r_runBg, bgPriority);
		    glBackground.addTask(r_runGL, glPriority);
		}
	    }
	}
    };



    private Runnable r_runGL = new Runnable() {
	public void run() {
	    if(dbg) pa("rungl");
	    /* Figure out what's missing from having all the 
	     * slots filled with the correct textures.
	     */
	    synchronized(FixedIndirectMipzipManager.this) {
	    synchronized(slots) {
		for(Iterator i = toBeDeleted.iterator(); i.hasNext();) {
		    VirtualTexture d = (VirtualTexture)i.next();
		    Slot s = (Slot)virtualImage2slot.get(d);
		    if(s != null) 
			s.glDelete();
		    virtualImage2slot.remove(d);
		}
		Slot unfinished = null;
		boolean loadScheduled = false;
		boolean demoted = false;

		for(int i=0; i<nlevels; i++) {
		    for(int j=0; j<slots[i].length; j++) {
			if(slots[i][j] == null)
			    continue;
			slots[i][j].step_openGL();

			if(!loadScheduled && 
				  !slots[i][j].allLoaded()) {
			    // Have we loaded something for it?
			    slots[i][j].loadFromTmp(loadTmp);
			    // If they still weren't all there:
			    if(!slots[i][j].allLoaded()) {
				// Start loading the first incomplete
				// in-place slot we have
				slots[i][j].requestLoad(loadTmp);
				loadScheduled = true;
				background.addTask(r_runBg, bgPriority);
			    }
			}
			if(loadScheduled && demoted) 
			    glBackground.addTask(r_runGL, glPriority);
		    }
		}
		if(loadScheduled) 
		    glBackground.addTask(r_runGL, glPriority);
	    }
	    }
	}
    };

    public synchronized void setSlotContents(VirtualTexture[][] newContents) {
	if(dbg) pa("setSlotContents");

	Set old = new HashSet(virtualImage2slot.keySet());

	boolean changed = false;
	
	for(int i=0; i<nlevels; i++) {
	    for(int j=0; j<slots[i].length; j++) {
		if(newContents[i][j] == null) {
		    slots[i][j] = null;
		    continue;
		}
		old.remove(newContents[i][j]);
		Slot slot = (Slot)virtualImage2slot.get(newContents[i][j]) ;
		if(slot == null) {
		    slot = new Slot(newContents[i][j]);
		    virtualImage2slot.put(newContents[i][j], slot);
		    changed = true;
		} 
		slot.slotIndex = j;
		if(slot.slotLevel != i) {
		    slot.changeLevel(i);
		    changed = true;
		}
		slots[i][j] = slot;
	    }
	}
	old.remove(null);
	if(!old.isEmpty()) changed = true;
	this.toBeDeleted = old;
	if(dbg) pa("Slot contents: changed "+changed);
	if(changed)
	    glBackground.addTask(r_runGL, glPriority);

	/*
	return new VirtualTexture(GL.createIndirectTexture(), mipzipFile);
	*/
    }

    public int getSlotLevel(VirtualTexture virtualTexture) {
	Slot slot = (Slot)virtualImage2slot.get(virtualTexture) ;
	return slot.slotLevel;
    }

    public synchronized void setSlotContents_synchronously(VirtualTexture[][] newContents) {
	throw new Error("Unimplemented");
    }

    public synchronized void decommission() {
	background.removeTask(r_runBg);
	glBackground.removeTask(r_runGL);
	slots = null;
    }


}
