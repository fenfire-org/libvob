/*
GLTextStyle.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka
 */
package org.nongnu.libvob.impl.gl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;
import java.awt.*;
import java.util.*;


/** OpenGL implementation of TextStyle.
 * This class should be instantiated only in special cases: usually
 * it's much better to use GraphicsAPI.getTextStyle.
 */
public class GLTextStyle extends TextStyle {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    /** A particular loaded font.
     */
    static private class Face {
	GLFont glFont;
	float height;
	float yoffset;

	float[] charWidths = new float[256];
	GLTextStyle[] instances = new GLTextStyle[250];
    };

    /** The different styles within a family.
     */
    static private class Family {
	Face normal;
	Face bold;
	Face bolditalic;
	Face italic;

	Face get(int flags) {
	    if((flags & Font.BOLD) != 0) {
		if((flags & Font.ITALIC) != 0)
		    return bolditalic;
		else 
		    return bold;
	    } else {
		if((flags & Font.ITALIC) != 0)
		    return italic;
		else 
		    return normal;
	    }
	}
	void set(int flags, Face f) {
	    if((flags & Font.BOLD) != 0) {
		if((flags & Font.ITALIC) != 0)
		    bolditalic = f;
		else 
		    bold = f;
	    } else {
		if((flags & Font.ITALIC) != 0)
		    italic = f;
		else 
		    normal = f;
	    }
	}
    }

    /** The families, keyed by name.
     */
    static private Map families = Collections.synchronizedMap(new HashMap());

    /** The actual font faces: several name-flags pair may map to the same
     * font, which is why we cache them here.
     */
    static private Map loadedFonts = Collections.synchronizedMap(new HashMap());

    static private Face getFace(String fileName) {
	Face f = (Face)loadedFonts.get(fileName);
	if(f == null) {
	    GL.FTFont ftfont = GL.createFTFont(fileName, 64, 64);

	    f = new Face();

	    float scale = 1.0f / (ftfont.getHeight() >> 6);
	    f.glFont = SimpleAlphaFont.convertFont(ftfont, 10, scale, scale, 
		    new String[] {
			"TEXTURE_MAG_FILTER", "LINEAR",
			"TEXTURE_MIN_FILTER", "LINEAR_MIPMAP_LINEAR"
		    });
	    f.charWidths = f.glFont.getWidths();
	    if(dbg) for(int i=0; i<256; i++) {
		pa("char '"+((char)i)+"': width "+f.charWidths[i]);
	    }
	    loadedFonts.put(fileName, f);
	}
	return f;
    }

    static private String styleToFile(String family, int style, int size) {
	family = family.toLowerCase();
	String baseName;
	String directory = "/usr/share/fonts/type1/gsfonts/";
	if(family.equals("sans") || family.equals("sansserif")) {
	    if((style & Font.BOLD) == 0) {
		baseName = "n019003l.pfb";
	    } else {
		baseName = "n019004l.pfb";
	    }
	} else if(family.equals("monospaced")) {
	    if((style & Font.BOLD) == 0) {
		baseName = "n022003l.pfb";
	    } else {
		baseName = "n022004l.pfb";
	    }
	} else {
	    pa("Strange font family '"+family+"' - using default (sans)");
	    return styleToFile("sans", style, size);
	}
	return directory + baseName;
    }

    static public GLTextStyle create(String family, int style, int size) {
	if(size > 200) size = 200;

	Family fam = (Family)families.get(family);
	if(fam == null) {
	    fam = new Family();
	    families.put(family, fam);
	}
	Face fac = fam.get(style);
	if(fac == null) {
	    fac = getFace(styleToFile(family, style, size));
	    fam.set(style, fac);
	}
	GLTextStyle s = fac.instances[size];
	if(s == null) {
	    s = new GLTextStyle(fac, size);
	    fac.instances[size] = s;
	}
	return s;
    }


    public float fontScale;
    public Face face;

    private GLTextStyle(Face f, float scale) {
	this.face = f;
	this.fontScale = scale;
    }

    public float getScaleByHeight(float h) {
	return h / face.glFont.getHeight() / fontScale;
    }

    public TextStyle getScaledStyle(float h) {
	float scale = h / face.glFont.getHeight();
	return new GLTextStyle(face, scale);
    }

    private float getWidth(String s) {
	float sum = 0;
	for(int i=0; i<s.length(); i++) {
	    char c = s.charAt(i);
	    if(c < face.charWidths.length)
		sum += face.charWidths[c];
	}
	return sum * fontScale;
    }
    private float getWidth(char[] chars, int offs, int len) {
	float sum = 0;
	for(int i=offs; i<offs+len; i++) {
	    char c = chars[i];
	    if(c < face.charWidths.length)
		sum += face.charWidths[c];
	}
	return sum * fontScale;
    }

    public float getWidth(String s, float scale) {
	return getWidth(s) * scale;
    }
    public float getWidth(char[] chars, int offs, int len, float scale) {
	return getWidth(chars, offs, len) * scale;
    }

    public float getHeight(float scale) {
	return scale * fontScale * face.glFont.getHeight() ;
    }

    public float getAscent(float scale) {
	return scale * fontScale * face.glFont.getYOffs();
    }

    public float getDescent(float scale) {
	return scale * fontScale * (face.glFont.getHeight() - face.glFont.getYOffs());
    }

    public float getLeading(float scale) {
	return scale * 0.05f * fontScale * face.glFont.getHeight();
    }

    public GLFont getGLFont() {
	return face.glFont;
    }
    public GL.QuadFont getQuadFont() {
	return face.glFont.getQuadFont();
    }

}
