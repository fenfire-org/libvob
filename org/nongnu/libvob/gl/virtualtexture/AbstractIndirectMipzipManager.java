/*
AbstractIndirectMipzipManager.java
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

/** A class implementing the parts of IndirectMipzipManager 
 * that are usually the same.
 */

public abstract class AbstractIndirectMipzipManager 
	implements IndirectMipzipManager {
    public static boolean dbg = true;
    private static void pa(String s) { System.out.println("ANDMZM: "+s); }

// ---- Final metadata
    protected String format;
    protected boolean isCompressedFormat;
    protected int width, height;
    protected int bitsPerTexel;
    protected int nlevels;

    protected int[] levelWidths;
    protected int[] levelHeights;

    protected String[] defaultTexParameters;
    /** Whether the default parameters have been altered
     * but the alteration has not yet been propagated to
     * all textures.
     */
    protected boolean defaultTexParametersChanged;

    /** The priority to give to the GL update manager.
     */
    public float glPriority = 5;

    /** The priority to give to the Bg update manager.
     */
    public float bgPriority = 5;

    /** The background object to run the non-gl jobs in.
     */
    public Background background = ThreadBackground.getDefaultInstance();

    /** The background object to run the gl jobs in.
     * Usually AbstractUpdateManager.getInstance()
     */
    public Background glBackground = AbstractUpdateManager.getInstance();

// ---- Implementation of IndirectMipzipManager

    public void init(String format, int width, int height) {
	this.format = format.intern();
	this.bitsPerTexel = GL.bitsPerTexel(format);
	this.width = width;
	this.height = height;

	int w = width, h = height;
	int nlevels = 0;
	while(w > 0 && h > 0) {
	    nlevels ++;
	    w /= 2;
	    h /= 2;
	}
	this.nlevels = nlevels;
	this.levelWidths = new int[nlevels];
	this.levelHeights = new int[nlevels];
	w = width; h = height;
	for(int i=0; i<nlevels; i++) {
	    levelWidths[i] = w;
	    levelHeights[i] = h;
	    w /= 2; if(w == 0) w = 1;
	    h /= 2; if(h == 0) h = 1;
	}

	isCompressedFormat = (format.indexOf("COMPRESS") != -1);

    }

    /** Return the number of mipmap levels
     * that textures of this size have.
     */
    public int getNLevels() {
	return nlevels;
    }

    synchronized public void setDefaultTexParameters(String[] params) {
	this.defaultTexParameters = (String[])params.clone();
	this.defaultTexParametersChanged = true;
    }

    synchronized public void setBackgrounds(Background background, float bgPriority,
			    Background glBackground, float glPriority) {
	this.background = background;
	this.bgPriority = bgPriority;
	this.glBackground = glBackground; 
	this.glPriority = glPriority;
    }


}
