/*
MipzipFile.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
import java.awt.Dimension;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/** A class for representing Mipzip -format files.
 * The mipzip file format packs all texture mipmap levels of a single
 * image into a single .zip file.
 * The mipzip file format is defined in the Libvob
 * design documentation.
 * <p>
 * This class manages the
 * file accesses to a mipzip file, providing access to the metadata and
 * the texture level data.
 */
public class MipzipFile {
    public static boolean dbg = false;
    final static void pa(String s) { System.out.println(s); }

    /** A lock for the use of ZLib.
     * I have a hunch that zlib might not be re-entrant
     * and that that would be causing some of our problems,
     * since loading a level synchronously
     * at the beginning mixed everything (random crashes also).
     */
    public static final Object zlibLock = new Object();

    /** We'll allow a fixed number of open zip files.
     */
    private static final int NOPEN = 5;

    /** Set of open MipzipFile objects.
     * Access synchronized through zlibLock.
     */
    private static final Set openMipzips = new HashSet();

// --- immutable per-object data

    /** The file in which the mipmaps are stored.
     */
    private final File file;

    /** The dimensions of the mipmap levels.
     * The length of this array is the number of levels in the file.
     */
    private Dimension[] levelDimensions;

    /** The physical sizes (in bytes) of the mipmap levels.
     * This is the size needed after zip decompression.
     */
    private int[] levelSizes;

    /** The texture format stored in the file.
     * Interned string, can be compared with "==".
     */
    private final String texFormat;

    /** The internal texture format recommended to use.
     * Interned string, can be compared with "==".
     * For compressed formats, null.
     */
    private final String internalFormat;

    /** The data type of the texture data.
     * Interned string, can be compared with "==".
     * For compressed formats, null.
     */
    private final String datatype;

    /** Whether the texture format is a compressed format.
     * OpenGL requires the use of different calls for compressed
     * and uncompressed formats.
     */
    private final boolean compressedFormat;

    /** The size of the original image embedded in the mipzip file, 
     * in texture coordinates.
     */
    private final float origWidth, origHeight;

// --- changing data

    /** The opened Zip file. At any given
     * time, may be null or open. 
     * Use getZipFile() to get it inside these classes.
     */
    private ZipFile zipFile;

// --- methods

    /** Create a new Mipzip file.
     */
    public MipzipFile(File file) throws IOException {
	this.file = file;
	synchronized(zlibLock) {
	    openZipFile();
	    if(dbg) {
		pa("Creating new mipzip file: "+this.file);
		for(Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
		    pa("Entry: " + e.nextElement());
		}
	    }
	    ZipEntry e = zipFile.getEntry("texformat");
	    if(e == null) throw new IOException("Invalid format: no texformat in mipzip");
	    this.texFormat = e.getComment().intern();
	    this.compressedFormat = texFormat.indexOf("COMPRESS") != -1;

	    e = zipFile.getEntry("internaltexformat");
	    if(e != null) {
		this.internalFormat = e.getComment().intern();
	    } else
		this.internalFormat = null;
	    e = zipFile.getEntry("datatype");
	    if(e != null) {
		this.datatype = e.getComment().intern();
	    } else
		this.datatype = null;


	    e = zipFile.getEntry("origsize");
	    if(e == null) throw new IOException("Invalid format: no origsize in mipzip");
	    String origSize = e.getComment();

	    {
		int i = origSize.indexOf('x');
		if(i < 0) throw new IOException("Invalid size string");
		origWidth = Float.parseFloat(origSize.substring(0,i));
		origHeight = Float.parseFloat(origSize.substring(i+1));
	    }


	    ArrayList l = new ArrayList();
	    for(int i=0; i<100; i++) {
		e = zipFile.getEntry(""+i);
		if(e == null) break;
		String levelSize = e.getComment();

		int xindex = levelSize.indexOf('x');
		if(xindex < 0) 
		    throw new IOException("Invalid size string "+levelSize);
		int w = Integer.parseInt(levelSize.substring(0,xindex));
		int h = Integer.parseInt(levelSize.substring(xindex+1));
		l.add(new Dimension(w, h));

	    }
	    levelDimensions = (Dimension[])l.toArray(new Dimension[l.size()]);
	    levelSizes = new int[levelDimensions.length];
	    for(int i=0; i<levelDimensions.length; i++) {
		e = zipFile.getEntry(""+i);
		levelSizes[i] = (int)e.getSize();
	    }

	    if( levelDimensions[levelDimensions.length-1].width != 1 ||
		levelDimensions[levelDimensions.length-1].height != 1 ) {
		throw new IOException("Not all levels there!");
	    }
	}
    }

    /** Get the File object this MipzipFile represents.
     */
    public File getFile() { return file; }

    /** Whether the texture format is compressed.
     * Compressed texture formats require different calls
     * to be used:  glCompressedTexImage instead of glTexImage2D.
     */
    public boolean getIsCompressedFormat() {
	return compressedFormat;
    }

    /** Get the texture format as a GL string, without the GL_ prefix.
     * For example, "COMPRESSED_RGB_S3TC_DXT1_EXT'
     * Guaranteed to return an interned string to allow fast comparisons.
     */
    public String getTexFormat() {
	return texFormat;
    }

    /** Get the internal format recommended for this texture.
     * Null for compressed textures.
     * Guaranteed to return an interned string to allow fast comparisons.
     */
    public String getInternalFormat() {
	return internalFormat;
    }

    /** Get the data type used for this texture in the file.
     * Null for compressed textures.
     * Guaranteed to return an interned string to allow fast comparisons.
     */
    public String getDatatype() {
	return datatype;
    }


    /** Get the number of mipmap levels in the file.
     */
    public int getNLevels() {
	return levelDimensions.length;
    }

    /** Get the texel dimensions of the given mipmap level.
     */
    public Dimension getLevelDimension(int level) {
	return (Dimension)(levelDimensions[level].clone());
    }

    /** Get the physical size (in bytes) of a given mipmap level.
     */
    public int getLevelSize(int level) { 
	return levelSizes[level];
    }

    /** Get the width of the original image inside 
     * the mipzip file, in texture coordinates.
     */
    public float getOrigWidth() {
	return origWidth;
    }
    /** Get the height of the original image inside 
     * the mipzip file, in texture coordinates.
     */
    public float getOrigHeight() {
	return origHeight;
    }

    /** Get the binary data from the given level.
     * This class does NOT cache any of the binary data.
     */
    public byte[] getLevelData(int level) throws IOException {
	return getLevelData(level, null);
    }

    /** Get the binary data from the given level into the given array.
     * This class does NOT cache any of the binary data.
     * If the array is too small or null, return a new, larger array
     * (analogous to java.util.Collection.toArray).
     */
    public byte[] getLevelData(int level, byte[] into) 
					throws IOException {
	String name = ""+level;
	synchronized(zlibLock) {
	    openZipFile();
	    ZipEntry e = zipFile.getEntry(name);
	    int size = getLevelSize(level);
	    byte[] loadedData;
	    if(into != null && into.length >= size)
		loadedData = into;
	    else
		loadedData = new byte[(int)e.getSize()];
	    InputStream i = zipFile.getInputStream(e);
	    int offs = 0;
	    while(offs < loadedData.length) {
		int res = i.read(loadedData, offs, loadedData.length - offs);
		if(res < 0) throw new IOException("EOF");
		offs += res;
	    }
	    i.close();
	    return loadedData;
	}
    }

// --- Private methods to manage the zipfile member

    private static Random random = new Random();

    /** Open (if not already open) and return the ZipFile.
     */
    private void openZipFile() throws IOException {
	synchronized(zlibLock) {
	    if(zipFile == null) {
		if(openMipzips.size() >= NOPEN) {
		    int nth = random.nextInt(NOPEN);
		    Iterator iter = openMipzips.iterator();
		    for(int i=0; i < nth; i++) iter.next();
		    MipzipFile f = (MipzipFile)openMipzips.iterator().next();
		    f.closeZipFile();
		}
		openMipzips.add(this);
		zipFile = new ZipFile(file);
	    }
	}
    }

    /** Close the zip file, if open.
     */
    private void closeZipFile() throws IOException {
	synchronized(zlibLock) {
	    if(zipFile != null) {
		zipFile.close();
		zipFile = null;
		openMipzips.remove(this);
	    }
	}
    }

}



