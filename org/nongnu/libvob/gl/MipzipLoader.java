/*
MipzipLoader.java
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
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.Background;
import java.awt.Dimension;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/** A loader for Mipzip files: files of zipped, 
 * compressed mipmap levels.
 * <p>
 * RESPONSIBILITIES: Manage the combination of 
 * a single texture and a mipzip file, creating a "virtualized" texture
 * of which better-quality versions can be loaded and discarded at will. 
 */
public class MipzipLoader {
    public static boolean dbg = false;
    final static void pa(String s) { System.out.println(s); }

    private int goalBaseLevel = -15;
    private int currentBaseLevel = 1000;

    /** The base level will never be set higher than this.
     * Apparently, the NV driver prefers to have more than one
     * mipmap level loaded in the beginning, possibly
     * because of the array shape?
     */
    private int maxBaseLevel;

    private Background bg = null;
    private float priority = 0;

    private final MipzipFile mipzipFile;

    public MipzipFile getMipzipFile() {
	return mipzipFile;
    }

    /** The texture.
     */
    private final GL.Texture tex;


    private int bitsPerTexel;

    private final Level[] levels;


    private String filterSettings;

    /** Set the callgl string for filtering the texture.
     * The texture should be assumed to be loaded to TEXTURE_2D already.
     */
    public void setFilterSettings(String s) {
	filterSettings = s;
	reFilter();
    }

    private static final int STATE_NONE = 1,
			     STATE_DATALOADED = 2,
			     STATE_INTEXTURE = 3;

    /** A single mipmap level.
     * Responsibilities: Know if loaded, load data, teximage data, discard.
     */
    private class Level implements Runnable {

	/** Whether this level is loaded into
	 * the texture.
	 */
	int state = STATE_NONE;

	/** The index of this level.
	 */
	int level;

	/** The data loaded from the disk.
	 */
	byte[] loadedData;

	/** Load the data for this mipmap level
	 * synchronously into memory. This method
	 * does not cause TexImage to be called
	 * and can therefore be called in any
	 * thread.
	 */
	synchronized void loadData() throws IOException {
	    if(dbg)
		pa("LoadData "+MipzipLoader.this+" "+level+
			" "+state);
	    if(state != STATE_NONE) return;
	    if(loadedData == null) 
		loadedData = mipzipFile.getLevelData(level);
	    state = STATE_DATALOADED;
	}

	synchronized public void run() { texImage(); }

	/** Call TexImage and afterwards discard the loaded data.
	 * Must be called in GL thread.
	 */
	synchronized void texImage() {
	    if(dbg)
		pa("TexImage "+MipzipLoader.this+" "+level+
			" "+state);
	    if(state != STATE_DATALOADED) return;
	    Dimension size = mipzipFile.getLevelDimension(level);
	    if(mipzipFile.getIsCompressedFormat())
		tex.compressedTexImage(level, mipzipFile.getTexFormat(),
			    size.width, size.height, 0,
			    loadedData);
	    else
		tex.texImage2D(level, "RGB", size.width, size.height,
			    0, mipzipFile.getTexFormat(), "BYTE", loadedData);
			    
	    state = STATE_INTEXTURE;
	    loadedData = null;
	    AbstractUpdateManager.doWhenIdle( r_runGL, priority);
	}

	/** Discard the texture level from the GL side.
	 * Done by loading an 1x1 image in its place.
	 * Hope this really works.
	 */
	synchronized void discard() {
	    if(dbg)
		pa("Discard "+MipzipLoader.this+" "+level+
			" "+state);
	    switch(state) {
	    case STATE_INTEXTURE:
		/*
		tex.loadNull2D(level, texFormat, 
			4, 2, 0, "RGB", "FLOAT");
		*/
	    case STATE_DATALOADED:
		break;
	    }
	    loadedData = null;
	    state = STATE_NONE;
	}

	/** The texture level from the GL side was discarded.
	 * Because discard() doesn't work on NV drivers,
	 * do it this way.
	 */
	synchronized void wasDiscarded() {
	    if(dbg)
		pa("Discarded "+MipzipLoader.this+" "+level+
			" "+state);
	    loadedData = null;
	    state = STATE_NONE;
	}


	/** Create a level.
	 * @param l The mipmap level index
	 */
	Level(int l) throws IOException {
	    this.level = l;
	}

    }

    /** Set the texture parameter "BASE_LEVEL" to the given value.
     */
    synchronized private void setBaseLevel(int level) {
	if(level < 0) level = 0;
	if(level >= maxBaseLevel) level = maxBaseLevel;
	tex.setTexParameter("TEXTURE_2D", "TEXTURE_BASE_LEVEL", level);
	tex.setTexParameter("TEXTURE_2D", "TEXTURE_MIN_LOD", level);
	currentBaseLevel = level;
    }

    /** Load to the base level synchronously: no discards.
     * @param level The base level: the lowest-detail level to be loaded
     */
    synchronized public void loadToBaseLevelSynch(int level) throws IOException {
	int cur = currentBaseLevel;
	setBaseLevel(level);
	if(currentBaseLevel > cur) {
	    for(int j=0; j<currentBaseLevel; j++)
		levels[j].wasDiscarded();
	    GLUtil.reloadCompressed(tex, 
			currentBaseLevel, levels.length);
	    reFilter();
	}
	for(int i=currentBaseLevel; i<levels.length; i++) {
	    levels[i].loadData();
	    levels[i].texImage();
	}
    }

    /** Set the base level goal for
     * asynchronous loading.
     * Calling this method starts the asynchronous process of loading
     * or discarding 
     * mipmap levels.
     * @param level The base level to move towards
     * @param bg The background thread in which to run the part of loading 
     * 		which does not need to be in the OpenGL thread.
     * @param priority The priority to pass to the background thread and UpdateManager
     */
    synchronized public void setGoalBaseLevel(int level,
				Background bg, float priority) {
	if(level < 0) level = 0;
	if(level >= maxBaseLevel) level = maxBaseLevel;
	if(level != goalBaseLevel || level != currentBaseLevel) {
	    if(dbg) pa("MipzipLoader "+this+" goal "+level+
			    " now at "+currentBaseLevel);
	    this.goalBaseLevel = level;
	    this.bg = bg;
	    this.priority = priority;
	    bg.addTask(r_runBg, priority);
	}
    }

    /** Get the currently loaded level.
     * For informational or waiting purposes only --- the value often changes
     * asynchronously.
     */
    public int getCurrentLevel() {
	return currentBaseLevel;
    }

    private Runnable r_runBg = new Runnable() {
	public void run() {
	    runBg();
	}
    };
    private Runnable r_runGL = new Runnable() {
	public void run() {
	    runGL();
	}
    };

    synchronized private void runBg() {
	if(dbg) pa("MipzipLoader "+this+" runBg!!");
	// See what data is missing and load it
	for(int i=levels.length-1; i >= goalBaseLevel && i >= 0;
		    i--) {
	    if(levels[i].state == STATE_NONE) {
		// Ah, data missing -> load now
		// and reschedule
		try {
		    levels[i].loadData();
		    AbstractUpdateManager.doWhenIdle( 
			    levels[i], priority - 10 - .1f * i);
		    priority += .05;
		    bg.addTask(r_runBg, priority);
		} catch(IOException e) {
		    pa("Exception while loading mipzip data "+e);
		    e.printStackTrace();
		}
		break;
	    }
	}
	// Schedule runGL always
	AbstractUpdateManager.doWhenIdle( r_runGL, priority - 5);
    }

    // See if any too large mipmaps still here
    // If yes, discard and reschedule.
    // Then, set base level and return.
    synchronized private void runGL() {
	if(dbg) pa("MipzipLoader "+this+" runGL!! "+
			goalBaseLevel);
	for(int i=0; i<goalBaseLevel && i < levels.length-1; 
						i++) {
	    if(levels[i].state != STATE_NONE) {
		if(dbg) pa("MipzipLoader "+this+" discard: "+
			i+" "+levels[i].state+" "+
			currentBaseLevel);
		if(false) {
		    levels[i].discard();
		    if(currentBaseLevel <= i)
			setBaseLevel(i+1);
		    AbstractUpdateManager.doWhenIdle(r_runGL, 
				priority);
		    return;
		}
		for(int j=0; j<goalBaseLevel; j++)
		    levels[j].wasDiscarded();

		GLUtil.reloadCompressed(tex, 
			    goalBaseLevel, levels.length);
		reFilter();
		if(currentBaseLevel < goalBaseLevel)
		    setBaseLevel(goalBaseLevel);
		else
		    setBaseLevel(currentBaseLevel); // need to set always after reloadCompressed
		break;

	    }
	}
	if(dbg) pa("MipzipLoader "+this+" no discards");

	// Find first texture that has not been teximaged.
	int i = levels.length-1; 
	for(;i >= goalBaseLevel && i >= 0;
		    i--) {
	    if(levels[i].state != STATE_INTEXTURE)
		break;
	}

	if(dbg) pa("MipzipLoader "+this+" firstNotIn!! "+ i);
	if(currentBaseLevel != i+1) {
	    boolean wasGreater = currentBaseLevel > i+1;
	    setBaseLevel(i+1);
	    if(wasGreater)
		AbstractUpdateManager.chgAfter(1000);
	    return;
	}
    }

    /** Get the OpenGL texture for this MipzipLoader.
     * The only operations on the texture should be 
     * drawing it, or setting the following OpenGL parameters:
     * <ul>
     * <li> filtering modes
     * <li> LOD min, max, bias
     * <li> shadow parameters
     * </ul>
     * Texture base level and max level and the actual texture
     * images should not be touched.
     */
    public GL.Texture getTexture() {
	return tex;
    }

    /** CallGL to set the texture filters right.
     */
    private void reFilter() {
	GL.call("BindTexture TEXTURE_2D "+tex.getTexId()+"\n"+
		filterSettings+"\n"+
		"BindTexture TEXTURE_2D 0\n");
    }

    /** Get the amount of memory currently used.
     * @return Memory, in bytes
     */
    public int getMemory() {
	return getMemory(currentBaseLevel);
    }

    /** Get the amount of memory used if given level is loaded.
     * @return Memory, in bytes
     */
    public int getMemory(int level) {
	if(level < 0) level = 0;
	if(level > levels.length-1) level = levels.length-1;
	Dimension size = mipzipFile.getLevelDimension(level);
	int bytesForLevel = size.width * size.height * bitsPerTexel / 8;
	int totalBytes = (bytesForLevel * 4) / 3;
	return totalBytes;
    }

    /** Get the level that uses at most given amount of memory.
     * @return The level index.
     */
    public int getLevelForBytes(int memory) {
	for(int i = 0; i<levels.length; i++) {
	    if(getMemory(i) <= memory) return i;
	}
	return levels.length-1; // XXX
    }

    float LG2 = (float)Math.log(2);
    /** Get the level that needs to be used to obtain 
     * the given quality.
     *
     * Quality = pixels per texcoord unit, i.e. 1 = 
     * the whole texture is shown in 1 pixel, 100 = texture
     * shown in 100x100 square (or like).
     */
    public int getLevelForQuality(float quality) {
	Dimension size = mipzipFile.getLevelDimension(0);
	int maxdim = Math.max(size.height, size.width);
	float ratio = maxdim / quality;
	int l = (int)(Math.log(ratio) / LG2);
	int lc = Math.max(0, l);
	lc = Math.min(levels.length-1, lc);
	if(false) pa("Mipzip level4qual: "+this+" "+
		    quality+" "+maxdim+" "+ratio+" "+
		    l+" "+levels.length+" "+lc);
	return l;
    }

    public float getQuality() {
	return getQuality(currentBaseLevel);
    }

    public float getQuality(int level) {
	Dimension size = mipzipFile.getLevelDimension(level);
	int maxdim = Math.max(size.height, 
				size.width);
	return maxdim;
    }

    /** Get the number of mipmap levels in this mipzip.
     */
    public int getNLevels() {
	return levels.length;
    }

    /** Get the size, in texels, of a texture level.
     * The return value must not be altered!
     */
    public Dimension getLevelDimension(int level) {
	return mipzipFile.getLevelDimension(level);
    }

    /** Create a new MipzipLoader for the given mipzip file.
     * Must be run in the GL thread.
     */
    public MipzipLoader(File file) throws IOException {
	this.mipzipFile = new MipzipFile(file);
	this.tex = GL.createTexture();
	bitsPerTexel = GL.bitsPerTexel(mipzipFile.getTexFormat());

	levels = new Level[this.mipzipFile.getNLevels()];
	for(int i=0; i<levels.length; i++)
	    levels[i] = new Level(i);

	if(bitsPerTexel < 0) {
	    pa("------- Warning: memory consumption for "+
			mipzipFile.getTexFormat()+" not known, assuming 32bpt");
	    bitsPerTexel = 32; 
	}
	maxBaseLevel = levels.length - 3;
	if(maxBaseLevel < 0) maxBaseLevel = 0;
	loadToBaseLevelSynch(maxBaseLevel);
    }

    public String toString() {
	StringBuffer b = new StringBuffer();
	b.append(super.toString());
	b.append("  ");
	for(int i=0; i<levels.length; i++)
	    b.append(levels[i].state);
	return b.toString();
    }

}
