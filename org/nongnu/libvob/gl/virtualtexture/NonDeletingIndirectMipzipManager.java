/*
NonDeletingIndirectMipzipManager.java
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

/** An IndirectMipzipManager that doesn't delete or move
 * textures in OpenGL (as a workaround for driver problems).
 * This IndirectMipzipManager will
 * not call glDeleteTexture() or re-teximag textures
 * but instead, when detail levels are shifted,
 * shifts them from one constant, fixed texture to
 * another. Only uses glTexSubImage() and glCompressedTexSubImage
 * after initialization.
 * <p>
 * There are two main issues that make this class complicated: 
 * 1) textures should be loaded
 * from the disk (zip files) in a background thread, and 
 * 2) the OpenGL thread must be kept occupied only for short amounts
 * of time at a time, to avoid loss of interactivity.
 * <p>
 */
public class NonDeletingIndirectMipzipManager 
	extends AbstractIndirectMipzipManager 
	implements IndirectMipzipManager 
{
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println("NDMZM: "+s); }

    /** Whether the latest setSlotContents call
     * was synchronous.
     */
    boolean synchronousMode = false;

// ---- Stable temporary data arrays

    /** The termporary array where data can be left
     * for a longer time.
     * No element 0.
     */
    private MipmapArray stableTmp;

    /** The temporary array to use for swapping
     * data between slots.
     * No element 0.
     */
    private MipmapArray swapTmp;

    /** The temporary array to use for loading from disk.
     */
    private MipmapArray loadTmp;

    /** The lod (importance level) of loadTmp currently.
     * Used to set the priority of the next GL task.
     */
    int loadTmpLevel;

// ---- The loaded state

    /** The state of a single texture slot.
     * A single VirtualTexture can be bound to the texture at a time.
     */
    private class Slot {
	/** The base level of this slot.
	 */
	final int slotLevel;
	/** The index of this slot within the level.
	 */
	final int slotIndex;
	/** The texture.
	 * To make sure no OpenGL implementation will reserve
	 * too much space, the texture's level 0 is the level "slotLevel"
	 * of the real texture size.
	 */
	final GL.Texture texture = GL.createTexture();

	/** The virtual image that the image in this slot is currently
	 * associated to.
	 */
	VirtualTexture currentImage;

	/** Bitmap: which levels have been loaded.
	 * Index: absolute levels.
	 */
	final boolean[] levelsLoaded = new boolean[nlevels];

	/** Base level (absolute) currently set.
	 */
	int baselevel;

	Slot(int level, int index) {
	    this.slotLevel = level;
	    this.slotIndex = index;
	    for(int i=0; i < nlevels - slotLevel; i++) {
		if(dbg) pa("Creating slot: "+
			    i+" "+
			    levelWidths[i+slotLevel]+" "+
			    levelWidths[i+slotLevel]+" ");
		texture.loadNull2D("TEXTURE_2D", i,
		    format, 
		    levelWidths[i + slotLevel],
		    levelHeights[i + slotLevel], 0,
		    "RGB", "FLOAT");
	    }
	}

	/** Change image assigned to slot.
	 * If assigning an image to another slot,
	 * it's important to *first* assign the image away
	 * from its current slot (using changeImage(null) if
	 * no other image is replacing it yet) and only then 
	 * changeImage(im)
	 * on the new slot.
	 */
	synchronized void changeImage(VirtualTexture newImage) {
	    if(dbg) pa("ChangeImage "+this+" "+newImage);
	    if(currentImage != null) {
		currentImage.indirectTexture.setTexture(null);
		virtualImage2slot.remove(currentImage);
	    }
	    for(int i=0; i<nlevels; i++) levelsLoaded[i] = false;
	    if(newImage != null) {
		newImage.indirectTexture.setTexture(texture);
		virtualImage2slot.put(newImage, this);
	    }
	    currentImage = newImage;
	    setBaseLevel();
	}

	void setTexParameters(String[] params) {
	    if(params == null) return;
	    for(int i=0; i<params.length; i+=2) {
		texture.setTexParameter("TEXTURE_2D", params[i], params[i+1]);
	    }
	}

	/** Whether all the levels needed for the current image have been loaded.
	 */
	boolean allLoaded() { 
	    return currentImage == null || baselevel == slotLevel; 
	}

	/** Call the base level setting on the texture.
	 */
	synchronized void setBaseLevel() {
	    if(dbg) pa("Calling chg()");
	    AbstractUpdateManager.chgAfter(50);
	    for(int i=nlevels-1; i>=0; i--) {
		if(!levelsLoaded[i]) {
		    // Allow the 1x1 mipmap image to be 
		    // reused between textures - avoid 
		    // having an inconsistent state and blacking
		    // out stuff.
		    baselevel = i + 1;
		    if(i == nlevels) i--;
		    texture.setTexParameter("TEXTURE_2D", 
				"TEXTURE_BASE_LEVEL", i - slotLevel +1);
		    if(dbg) pa(""+this+" Baselevel set to "+(i-slotLevel+1));
		    setTexParameters(defaultTexParameters);
		    return;
		}
	    }
	    baselevel = slotLevel;
	    texture.setTexParameter("TEXTURE_2D", 
			"TEXTURE_BASE_LEVEL", 0 );
	    setTexParameters(defaultTexParameters);
	    if(dbg) pa(""+this+" Baselevel set to 0 - all loaded");
	}

	/** Save data of a level to a byte array.
	 * If there's not enough room in the data[][] array, makes more.
	 * @param level The absolute mipmap level to save
	 * @param data The data array
	 * @param index The index of data to use
	 * @return Data size saved.
	 */
	synchronized int saveData(int level, byte[][] data, int index) {
	    int size;
	    if(isCompressedFormat) {
		size = (int)(texture.getLevelParameter(level - slotLevel, 
				"TEXTURE_COMPRESSED_IMAGE_SIZE_ARB")[0]);
	    } else {
		size = levelWidths[level] * levelHeights[level] 
			    * GLUtil.findBpt(format, 
				    currentImage.mipzipFile.getDatatype());
	    }
	    if(data[index].length < size)
		data[index] = new byte[size];
	    if(dbg) pa("SaveData "+this+" "+level+" "+data[index]);

	    if(isCompressedFormat) {
		texture.getCompressedTexImage(level - slotLevel, data[index]);
	    } else {
		texture.getTexImage(level - slotLevel, format,
			    currentImage.mipzipFile.getDatatype(), 
			    data[index]);
	    }
	    return size;
	}


	/** Load data from a byte array.
	 * @param level The **absolute** mipmap level
	 */
	synchronized void loadData(int level, byte[] data, int size) {
	    if(dbg) {
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

	    /* Set baselevel to 0 temporarily. NV driver 44.96 has a bug
	     * where texImage calls to levels below BASE_LEVEL affect
	     * the OpenGL state (i.e. getTexImage returns the correct values)
	     * but not the texture that will be rendered.
	     * The code in lava/bugs/nvtex shows this.
	     * 
	     * N.B. finding that this was the problem took Tjl several days
	     * of EXTREME hard work and stress, since this is a vital code
	     * path for us.
	     */
	    texture.setTexParameter("TEXTURE_2D", 
			"TEXTURE_BASE_LEVEL", 0 );

	    if(isCompressedFormat) {
		texture.compressedTexSubImage2D(level - slotLevel,
			0, 0, levelWidths[level], levelHeights[level],
			format, size, data);
	    } else {
		texture.texSubImage2D(level - slotLevel,
			    0, 0, levelWidths[level], levelHeights[level],
			    0, currentImage.mipzipFile.getTexFormat(),
			    currentImage.mipzipFile.getDatatype(),
			    data);
	    }


	    levelsLoaded[level] = true;
	}

	/** Save all levels above the given absolute level 
	 * to the temporary array.
	 */
	synchronized void saveToTmp(MipmapArray into, int minLevel) {
	    for(int i=minLevel; i<nlevels; i++) {
		if(levelsLoaded[i] && into.levels[i] != null) {
		    synchronized(into.levels[i]) {
			into.levelSizes[i] = saveData(i, into.levels, i);
			into.virtualTextures[i] = this.currentImage;
		    }
		}
	    }
	}

	/** Request loading the missing levels to the given
	 * tmp mipmaparray.
	 */
	synchronized void requestLoad(MipmapArray into) {
	    for(int i=slotLevel; i<nlevels; i++) {
		if(!levelsLoaded[i])
		    into.setLoadRequest(i, currentImage);
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
			from.virtualTextures[i] == this.currentImage) {
			    // Have to make sure this still stands
			    // in the synchronized section
			    if(from.virtualTextures[i] == this.currentImage) {
				if(from.levelSizes[i] > 0) 
				    loadData(i, from.levels[i], from.levelSizes[i]);
				if(from.levelSizes[i] == 0) {
				    from.levelSizes[i] = -1;
				    from.virtualTextures[i] = null;
				}
			    }
		    }
		}
	    }
	    setBaseLevel();
	}

	public String toString() {
	    return "[Slot: "+slotLevel+" "+slotIndex+" ("+texture.getTexId()
		    +")]";
	}
    }

    Slot[][] slots;

    /** Mapping of virtual images to Slot objects.
     * The Slot objects contain their indices in the slots array,
     * which makes it easy to just store the Slot here.
     */
    HashMap virtualImage2slot = new HashMap();

    /** The images that should eventually be stored in each slot.
     */
    VirtualTexture[][] slotTargets;

    /** Mapping of virtual images to target Slot objects.
     * The inverse of slotTargets.
     */
    HashMap virtualImage2targetSlot = new HashMap();



// ---- External API

    public void setAllocations(int[] ntextures) {
	slots = new Slot[nlevels][];
	slotTargets = new VirtualTexture[nlevels][];

	stableTmp = new MipmapArray(nlevels, 1);
	swapTmp = new MipmapArray(nlevels, 1);
	loadTmp = new MipmapArray(nlevels, 0);

	if(ntextures.length != nlevels) 
	    throw new Error("Invalid length "+ntextures.length+" "+nlevels);
	// Assuming no previous allocations...
	for(int i=0; i<nlevels; i++) {
	    slots[i] = new Slot[ntextures[i]];
	    slotTargets[i] = new VirtualTexture[ntextures[i]];
	    for(int j=0; j<ntextures[i]; j++) {
		slots[i][j] = new Slot(i, j);
		slots[i][j].setTexParameters(defaultTexParameters);
	    }
	}
    }

    private float getGLPriority(int level) {
	return glPriority + 2 * level;
    }

    private Runnable r_runBg = new Runnable() {
	public void run() {
	    synchronized(NonDeletingIndirectMipzipManager.this) {
		if(synchronousMode) return;
		if(slots == null) return;
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
		    glBackground.addTask(r_runGL, getGLPriority(loadTmpLevel));
		}
	    }
	}
    };

    /** Run the demotion chain starting from the given slot.
     * Must be run in the GL thread
     * Postcondition: slot.currentImage == target
     */
    private void glDemote(int i, int j) {
	Slot slot = slots[i][j];
	VirtualTexture target = slotTargets[i][j];
	VirtualTexture oldImage = slot.currentImage;

	Slot demoteTo = null;
	    
	// First, save the contents of the slot into tmp
	if(oldImage != null) {
	    demoteTo = (Slot)virtualImage2targetSlot.get(oldImage);
	    if(demoteTo != null)
		slot.saveToTmp(stableTmp, demoteTo.slotLevel);
	}

	if(target != null) {
	    // Where the one to be *promoted* here is coming 
	    // from
	    Slot source = (Slot)virtualImage2slot.get(target);
	    if(source != null) {
		source.saveToTmp(swapTmp, i);
		source.changeImage(null);
	    }
	}

	// Change the current image to the right one
	slot.changeImage(target);
	slot.loadFromTmp(swapTmp);

	// Measure chain length.
	int chain = 1;

	// Now we have set the image we started to set.
	// demote in a chain

	// If there was no slot for the image that was evicted,
	// our work is done.
	while(demoteTo != null) {
	    // Here, stableTmp = image of oldImage,
	    // swapTmp = empty,
	    // demoteTo = where the image of oldImage is going to.

	    VirtualTexture nextImage = demoteTo.currentImage;
	    Slot nextSlot = (Slot)virtualImage2targetSlot.get(nextImage);
	    if(nextSlot != null)
		demoteTo.saveToTmp(swapTmp, nextSlot.slotLevel);

	    demoteTo.changeImage(oldImage);
	    demoteTo.loadFromTmp(stableTmp);

	    // Swap the two arrays
	    MipmapArray s = swapTmp;
	    swapTmp = stableTmp ;
	    stableTmp = s;

	    oldImage = nextImage;
	    demoteTo = nextSlot;
		
	    chain ++;
	}
	if(dbg) pa("Demote: "+chain);

    }

    private Runnable r_runGL = new Runnable() {
	public void run() {
	    if(dbg) pa("rungl");
	    /* Figure out what's missing from having all the 
	     * slots filled with the correct textures.
	     */
	    synchronized(NonDeletingIndirectMipzipManager.this) {
		if(synchronousMode) return;
		if(slots == null) return;
		if(defaultTexParametersChanged) {
		    for(int i=0; i<nlevels; i++)
			for(int j=0; j<slots[i].length; j++)
			    slots[i][j].setTexParameters(defaultTexParameters);
		    defaultTexParametersChanged = false;
		}
	    synchronized(slots) {
		Slot unfinished = null;
		boolean loadScheduled = false;
		boolean demoted = false;
		int highestLevel = 100;
		for(int i=0; i<nlevels; i++) {
		    for(int j=0; j<slots[i].length; j++) {
			if(!demoted &&
			   slots[i][j].currentImage != slotTargets[i][j]) {
			    // Start a demotion chain
			    // XXX
			    // should check whether we
			    // have we loaded any of the images there -
			    // demoting before the new images come
			    // out from the disk is not good...?
			    glDemote(i, j);
			    if(i < highestLevel) highestLevel = i;
			    demoted = true;
			} else if(!loadScheduled && 
				  !slots[i][j].allLoaded()) {
			    // Have we loaded something for it?
			    slots[i][j].loadFromTmp(loadTmp);
			    // If they still weren't all there:
			    if(!slots[i][j].allLoaded()) {
				// Start loading the first incomplete
				// in-place slot we have
				slots[i][j].requestLoad(loadTmp);
				loadTmpLevel = i;
				loadScheduled = true;
				background.addTask(r_runBg, bgPriority);
			    }
			}
			if(loadScheduled && demoted) 
			    glBackground.addTask(r_runGL, getGLPriority(highestLevel));
		    }
		}
		if(loadScheduled || demoted) 
		    glBackground.addTask(r_runGL, getGLPriority(highestLevel));
	    }
	    }
	}
    };

    public synchronized void setSlotContents(VirtualTexture[][] newContents) {
	if(dbg) pa("setSlotContents");
	synchronousMode=false;
	Set onLevel = new HashSet();
	Set toBe = new HashSet();
	Set newComers = new HashSet();
	int changed = 0;
	// Keep the existing ones there, others are assigned
	// arbitrarily. Should we optimize?
	synchronized(slots) {
	    virtualImage2targetSlot.clear();
	    for(int i=0; i<nlevels; i++) {
		onLevel.clear();
		toBe.clear();
		newComers.clear();
		for(int j=0; j<slots[i].length; j++) {
		    if(slots[i][j].currentImage != null)
			onLevel.add(slots[i][j].currentImage);
		}
		for(int j=0; j<slots[i].length; j++) {
		    if(newContents[i][j] != null) {
			toBe.add(newContents[i][j]);
			if(! onLevel.contains(newContents[i][j]) )
			    newComers.add(newContents[i][j]);
		    }
		}
		changed += newComers.size();
		Iterator newIter = newComers.iterator();
		while(newIter.hasNext()) {
		    VirtualTexture vt = (VirtualTexture)newIter.next();
		    Dimension size = vt.mipzipFile.getLevelDimension(0);
		    if(size.width != width && size.height != height)
			throw new Error("Invalid mipzip added:" + size + "; expected: "+width+", "+height);
		    if(!(vt.mipzipFile.getTexFormat().equals(format)))
			throw new Error("Invalid mipzip added:" + vt.mipzipFile.getTexFormat());
		}
		newIter = newComers.iterator();
		for(int j=0; j<slots[i].length; j++) {
		    if(!toBe.contains(slots[i][j].currentImage)) {
			if(! newIter.hasNext()) 
			    slotTargets[i][j] = null;
			else 
			    slotTargets[i][j] = (VirtualTexture)newIter.next();
		    } else {
			slotTargets[i][j] = slots[i][j].currentImage;
		    }
		    if(slotTargets[i][j] != null)
			virtualImage2targetSlot.put(slotTargets[i][j], slots[i][j]);

		}
	    }
	}
	if(dbg) pa("Slot contents: changed "+changed);
	glBackground.addTask(r_runGL, glPriority);

	/*
	return new VirtualTexture(GL.createIndirectTexture(), mipzipFile);
	*/
    }

    public synchronized int getSlotLevel(VirtualTexture virtualTexture) {
	Slot slot = (Slot)virtualImage2targetSlot.get(virtualTexture);
	if(slot == null) return -1;
	return slot.slotLevel;
    }

    public synchronized void setSlotContents_synchronously(VirtualTexture[][] newContents) throws java.io.IOException {
	// Do it the slow way since we can.
	synchronousMode=true;

	for(int i=0; i<nlevels; i++) {
	    for(int j=0; j<slots[i].length; j++) {
		slotTargets[i][j] = null;
		Slot oldSlot = (Slot)virtualImage2slot.get(newContents[i][j]);
		if(oldSlot != null) {
		    oldSlot.changeImage(null);
		}
		if(oldSlot != slots[i][j]) {
		    slots[i][j].changeImage(newContents[i][j]);
		}
		while(!slots[i][j].allLoaded()) {
		    slots[i][j].requestLoad(loadTmp);
		    loadTmpLevel = i;
		    loadTmp.loadData();
		    slots[i][j].loadFromTmp(loadTmp);
		}
	    }
	}

    }

    public synchronized void decommission() {
	background.removeTask(r_runBg);
	glBackground.removeTask(r_runGL);
	slots = null;
    }
}


