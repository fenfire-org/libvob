/*
GLUtil.java
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

/** Miscellaneous GL utilities that don't belong.
 */
public class GLUtil {
    public static boolean dbg = false;
    final static void pa(String s) { System.out.println(s); }

    /** Delete the texture and recopy the compressed
     * levels start..end-1 to it,.
     * It appears that there's a small driver bug in NVIDIA's
     * driver (or that I'm mistaken again ;), where we can't
     * easily unload texture levels.
     * This method takes the drastic approach: delete the
     * texture and then teximage it again.
     * <p>
     * The texture parameters are not touched, except that
     * BASE_LEVEL and .
     * <p>
     * This MIGHT have now gotten fixed with the realization that
     * we should always keep so many levels of the texture
     * that both width and height are greater than 1 - not sure though.
     */
    static public void reloadCompressed(GL.Texture tex, int start, int end) {
	if(dbg) pa("Reload "+tex+" "+start+" "+end);
	String texformat = null;
	int[] sizes = new int[end*2];
	byte[][] backup = new byte[end][];
	if(dbg) pa("ch: "+checkMipmap(tex));
	for(int i=start; i<end; i++) {
	    sizes[2*i] = (int)tex.getLevelParameter(i, 
				"TEXTURE_WIDTH")[0];
	    sizes[2*i+1] = (int)tex.getLevelParameter(i, 
				"TEXTURE_HEIGHT")[0];
	    if(sizes[2*i] > 0 && sizes[2*i+1] > 0) {
		if(dbg) pa("Level "+i+" "+sizes[2*i]+" "+
			    sizes[2*i+1]+" "+
			GL.getGLTokenString((int)
			tex.getLevelParameter(i, 
			    "TEXTURE_INTERNAL_FORMAT")[0]));

		backup[i] = tex.getCompressedTexImage(i);
		if(texformat == null)
		    texformat = GL.getGLTokenString((int)
			tex.getLevelParameter(i, 
			    "TEXTURE_INTERNAL_FORMAT")[0]);
	    }
	}
	if(texformat == null) {
	    pa("TEXTURE RELOADCOMPRESSED NOFORMAT!");
	    return;
	}
	boolean nonnull = false;
	// Check that there was at least one non-null level!
	for(int i=start; i<end; i++) {
	    if(backup[i] != null) 
		nonnull = true;
	}
	if(! nonnull) {
	    throw new Error("Can't discard - no levels to load, would cause BIG trouble");
	}

	GL.call("DeleteTextures "+tex.getTexId()+"\n");
	for(int i=start; i<end; i++) {
	    if(backup[i] == null) {
		pa("Level null: "+i);
		// throw new Error("Why is backup null!?!?");
	    } else {
		tex.compressedTexImage(i,
		    texformat, sizes[2*i], sizes[2*i+1], 0,
		    backup[i]);
	    }
	    backup[i] = null;
	}
	backup = null;
    }

    /** Check mipmap levels of a texture.
     * @return null, if everything is OK, an error string otherwise.
     */
    static public String checkMipmap(GL.Texture tex) {
	int base = (int)tex.getParameter("TEXTURE_BASE_LEVEL")[0];
	int max = (int)tex.getParameter("TEXTURE_MAX_LEVEL")[0];

	int w = (int)tex.getLevelParameter(base, "TEXTURE_WIDTH")[0];
	int h = (int)tex.getLevelParameter(base, "TEXTURE_HEIGHT")[0];
	int format = (int)tex.getLevelParameter(base, "TEXTURE_INTERNAL_FORMAT")[0];

	if(w == 0 || h == 0) {
	    return "Warning: texture base level size zero: "+w+" "+h;
	}

	for(int i=base+1; i<max; i++) {
	    if(w == 1 && h == 1) break;
	    w /= 2; if(w == 0) w = 1;
	    h /= 2; if(h == 0) h = 1;

	    int wc = (int)tex.getLevelParameter(i, "TEXTURE_WIDTH")[0];
	    int hc = (int)tex.getLevelParameter(i, "TEXTURE_HEIGHT")[0];
	    int formatc = (int)tex.getLevelParameter(i, "TEXTURE_INTERNAL_FORMAT")[0];

	    if(wc != w || hc != h)
		return "Texture level size wrong: "+base+" "+i+" "+w+" "+h+" "+wc+" "+hc;

	    if(formatc != format)
		return "Texture level format wrong: "+base+" "+i+" "+format+" "+formatc;


	    
	}
	return null;
    }


    /** How many bytes will one texel of a normal (uncompressed)
     * texture format cover.
     * For instance, findBpt("RGB", "UNSIGNED_INT") == 12
     */
    static public int findBpt(String format, String datatype) {
	int ncomps = 0;
	if(format.equals("RED") ||
	    format.equals("GREEN") ||
	    format.equals("BLUE") ||
	    format.equals("ALPHA") ||
	    format.equals("LUMINANCE"))
	    ncomps = 1;
	else if(format.equals("LUMINANCE_ALPHA"))
	    ncomps = 2;
	else if(format.equals("RGB") ||
		format.equals("BGR"))
	    ncomps = 3;
	else if(format.equals("RGBA") ||
		format.equals("BGRA"))
	    ncomps = 4;

	if(ncomps == 0) throw new Error("Unknown texture format " + format);

	if(datatype.equals("UNSIGNED_BYTE") ||
	    datatype.equals("BYTE"))
	    return ncomps * 1;
	if(datatype.equals("UNSIGNED_SHORT") ||
	datatype.equals("SHORT"))
	    return ncomps * 2;
	if(datatype.equals("UNSIGNED_INT") ||
	    datatype.equals("INT") ||
	    datatype.equals("FLOAT"))
	    return ncomps * 4;

	throw new Error("Unknown datatype " + datatype);

    }

}
