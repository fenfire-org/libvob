/*
SimpleAlphaFont.java
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


package org.nongnu.libvob.gl;
import org.nongnu.libvob.util.Mosaic2D;

/** A class for converting a FTFont into a GLFont.
 * <p>
 * Although it could be done more modularily, this class is more
 * of an example - other implementations should work on sharing code.
 */
public class SimpleAlphaFont {
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println(s); }

    // Even though it's wasteful, allocates all tiles.
    static public Mosaic2D.Tile[] generateTiles(int[] chars, int[] meas, int border) {
	
	int texsize = (int)GL.getGLFloat("MAX_TEXTURE_SIZE")[0];
	if(texsize > 512)
	    texsize = 512; // A reasonable maximum
	Mosaic2D mosaic2D = new Mosaic2D(texsize, texsize);

	Mosaic2D.Tile[] tiles = new Mosaic2D.Tile[chars.length];
	for(int i=0; i<chars.length; i++) {
	    int w=0, h=0;

	    if(meas[i*6 + 2] > 0) {
		w = (meas[i*6 + 2] + 2 * border);
		h = (meas[i*6 + 3] + 2 * border);
		System.out.println("::w="+w+",h="+h+" for '"+chars[i]+"'");
	    }

	    tiles[i] = mosaic2D.alloc(w,h);
	}
	return tiles;
    }

    static public GL.Texture[] createTextures(Mosaic2D.Tile[] tiles, 
				    byte[][] bitmaps, String[] texParams) {

	int ntex = tiles[0].getMosaic2D().getNPages();
	int width = tiles[0].getMosaic2D().getPageWidth();
	int height = tiles[0].getMosaic2D().getPageHeight();

	GL.Texture[] textures = new GL.Texture[ntex];
	// Zeroes to load
	byte[] nulldata = new byte[width * height];
	// For debugging: make it slightly gray
	// for(int i=0; i<width * height; i++) {
	//     nulldata[i] = (byte)(Math.random() * 255 - 128);
	// }
	for(int i=0; i<ntex; i++) {
	    textures[i] = GL.createTexture();
	    textures[i].setTexParameter("TEXTURE_2D", 
			"GENERATE_MIPMAP_SGIS", "TRUE");
	    textures[i].texImage2D(0,
			"ALPHA", width, height, 0,
			"ALPHA", "UNSIGNED_BYTE", nulldata);
	    for(int j=0; j<texParams.length; j+= 2) 
		textures[i].setTexParameter("TEXTURE_2D", 
					texParams[j], texParams[j+1]);
	}
	return textures;
    }

    static public GLFont convertFont(GL.FTFont ftfont, int border, 
					float xscale, float yscale,
					String[] texParams) {
	return convertFont(ftfont, border, xscale, yscale, texParams, 1);
    }

    /** Convert a file name to FTFont to a GLFont.
     * @param ftfont The FTFont to convert.
     * @param border The amount of border (in texels) to add around the
     * 			characters to ensure all gets rendered.
     * @param xscale The scale to apply to the X axis.
     * @param yscale The scale to apply to the Y axis.
     */
    static public GLFont convertFont(GL.FTFont ftfont, int border, 
					float xscale, float yscale,
					String[] texParams, int ntexunits) {

	float height = (ftfont.getHeight()  >> 6) * yscale;
	float yoffs = (ftfont.getYOffs()  >> 6) * yscale;

	int[] chars = new int[256];
	for(int i=0; i<chars.length; i++)
	    chars[i] = i;
	int[] meas = ftfont.getMeasurements(chars);
	byte[][] bitmaps = ftfont.getBitmaps(chars);

	// Now we're done with ftfont and start
	// the real work
	
	Mosaic2D.Tile[] tiles = generateTiles(chars, meas, border);

	GL.Texture[] textures = createTextures(tiles, bitmaps, texParams);
	GL.Texture[] multextures = new GL.Texture[ntexunits * textures.length];
	for(int i=0; i<textures.length; i++) {
	    for(int j=0; j<ntexunits; j++) {
		multextures[j + ntexunits*i] = textures[i];
	    }
	}

	for(int i=0; i<chars.length; i++) {
	    if(tiles[i].w == 0) continue;
	    // dbg: make it grayer
	    // for(int j=0; j<bitmaps[i].length; j++)
	    // 	bitmaps[i][j] |= 31;

	    textures[tiles[i].page].texSubImage2D(0,
		tiles[i].x + border,
		tiles[i].y + border,
		tiles[i].w - 2*border, 
		tiles[i].h - 2*border,
		0,
		"ALPHA", "UNSIGNED_BYTE", bitmaps[i]);
	}

	GL.QuadFont quadFont = GL.createQuadFont();

	String[] texUnits = new String[ntexunits];
	String[] texTypes = new String[ntexunits];
	for(int i=0; i<ntexunits; i++) {
	    texUnits[i] = "TEXTURE"+i+"_ARB";
	    texTypes[i] = "TEXTURE_2D";
	}

	quadFont.setTextures(
		texUnits, texTypes, texUnits,
		multextures);

	quadFont.setNGlyphs(chars.length);

	float tmultx = 1.0f / tiles[0].getMosaic2D().getPageWidth() ;
	float tmulty = 1.0f / tiles[0].getMosaic2D().getPageHeight() ;
	float[] widths = new float[chars.length];
	for(int i=0; i<chars.length; i++) {
	    widths[i] = xscale * (meas[6*i + 4] >> 6);
	    if(tiles[i].w == 0 && i != 32) continue;
	    quadFont.setMeasurements(i, tiles[i].page,
		xscale * (meas[6*i + 0] - border),
		yscale * (meas[6*i + 1] - border),
		xscale * (meas[6*i + 0] - border + tiles[i].w),
		yscale * (meas[6*i + 1] - border + tiles[i].h),
		tmultx * (tiles[i].x),
		tmulty * (tiles[i].y),
		tmultx * (tiles[i].x + tiles[i].w),
		tmulty * (tiles[i].y + tiles[i].h),
		xscale * (meas[6*i + 4] >> 6),
		yscale * (meas[6*i + 5] >> 6));
	}
	return new GLFont(height, yoffs, widths, quadFont);

    }

    static private int toint(byte a) {
	if(a < 0) return a + 256;
	return a;
    }
    static private byte tobyte(int a) {
	if(a < 0) return 0;
	if(a > 255) return -1;
	if(a > 127) return (byte)(a - 256);
	return (byte)a;
    }
    static private byte avg(byte a, byte b) {
	return tobyte((toint(a) + toint(b))/2);
    }

    static private void shrinkX(byte[] data, int width, int height, int fac) {
	// Assert power of two!
	while(fac > 1) {
	    for(int i=0; i<data.length / 2; i++) 
		data[i] = avg(data[2*i], data[2*i+1]);
	    fac /= 2;
	}
    }

    static private void shrinkY(byte[] data, int width, int height, int fac) {
	// Assert power of two!
	while(fac > 1) {
	    for(int y=0; y<height/2; y++) {
		for(int x=0; x<width; x++) {
		    data[x+width*y] = avg(data[x+width*(2*y)], data[x+width*(2*y+1)]);
		}
	    }
	    fac /= 2;
	}
    }

    /** Post-process a quadfont by reducing resolution by n in the given direction.
     * Useful for aniso font experiments, where we want to 
     * retain the exact same layout in the mipmaps, not much else.
     */
    static public void postprocessGLFont_reduceReso(GL.QuadFont f, int xfac, int yfac) {
	if(xfac <= 0 || yfac <= 0) throw new Error("Not allowed: "+xfac+" "+yfac);
	if(xfac > 1 && yfac > 1) {
	    postprocessGLFont_reduceReso(f, xfac, 1);
	    postprocessGLFont_reduceReso(f, 1, yfac);
	}
	GL.Texture[] textures = f.getTextures();
	for(int i=0; i<textures.length; i++) {
	    int width = (int)(textures[i].getLevelParameter(0, "TEXTURE_WIDTH")[0]);
	    int height = (int)(textures[i].getLevelParameter(0, "TEXTURE_HEIGHT")[0]);
	    byte[] tex = new byte[width * height];
	    textures[i].getTexImage(0, "ALPHA", "UNSIGNED_BYTE", tex);

	    if(xfac > 1) 
		shrinkX(tex, width, height, xfac);
	    if(yfac > 1) 
		shrinkY(tex, width, height, yfac);

	    textures[i].texImage2D(0, "ALPHA", width / xfac, height / yfac, 0, 
					"ALPHA", "UNSIGNED_BYTE", tex);

	}
    }

    static private void sharpen(byte[] data, byte[] into, int w, int h, float a, float b) {
	float c = 1 - 4*a - 4*b;
	for(int x=1; x<w-1; x++) {
	    for(int y=1; y<h-1; y++) {
		into[x + y*w] = 
		    tobyte((int)(
		    c * toint(data[(x+0) + (y+0)*w]) +
		    a * toint(data[(x+1) + (y+0)*w]) + 
		    a * toint(data[(x-1) + (y+0)*w]) + 
		    a * toint(data[(x+0) + (y+1)*w]) + 
		    a * toint(data[(x+0) + (y-1)*w]) + 
		    b * toint(data[(x+1) + (y+1)*w]) +
		    b * toint(data[(x+1) + (y-1)*w]) +
		    b * toint(data[(x-1) + (y+1)*w]) +
		    b * toint(data[(x-1) + (y-1)*w]))) ;
	    }
	}
    }

    /** Filter the downsampled texture levels of a given texture 
     * with a sharpening filter.
     * @param f The font to filter
     * @param a The coefficient to use for direct s or t neighbour
     * @param b The coefficient to use for diagonal neighbours
     */
    static public void filterDownsampled(GL.QuadFont f, float a, float b, String[] texParams) {
	GL.Texture[] textures = f.getTextures();
	for(int t=0; t<textures.length; t++) {
	    GL.Texture tex = textures[t];
	    tex.setTexParameter("TEXTURE_2D", 
			"GENERATE_MIPMAP_SGIS", "FALSE");
	    int[] widths = new int[20];
	    int[] heights = new int[20];
	    byte[][] bytes = new byte[20][];
	    for(int lod = 0; ; lod++) {
		widths[lod] = (int)(tex.getLevelParameter(lod, "TEXTURE_WIDTH")[0]);
		heights[lod] = (int)(tex.getLevelParameter(lod, "TEXTURE_HEIGHT")[0]);
		int width = widths[lod];
		int height = heights[lod];
		bytes[lod] = new byte[width * height];
		tex.getTexImage(lod, "ALPHA", "UNSIGNED_BYTE", bytes[lod]);
		if(width == 1 && height == 1)
		    break;
	    }

	    GL.call("DeleteTextures "+tex.getTexId()+"\n");

	    for(int i=0; i<texParams.length; i+=2) 
		tex.setTexParameter("TEXTURE_2D", texParams[i], texParams[i+1]);

	    for(int lod = 0; bytes[lod] != null; lod++) {
		int width = widths[lod];
		int height = heights[lod];
		byte[] data = bytes[lod];

		byte[] dataInto = new byte[width * height];

		sharpen(data, dataInto, width, height, a, b);

		tex.texImage2D(lod, "ALPHA", width, height, 0, 
					"ALPHA", "UNSIGNED_BYTE", dataInto);

		tex.getTexImage(lod, "ALPHA", "UNSIGNED_BYTE", data);
		/*
		pa("Converted "+a+" "+b+" "+width+" "+height);
		for(int i=0; i<data.length; i++) {
		    pa("Comp: "+i+" "+data[i]+" "+dataInto[i]);
		    if(data[i] != dataInto[i]) 
			throw new Error("?!?!");
		}
		*/

	    }
	}
    }

    static public void changeFormat(GL.QuadFont f, String newFormat,
					    String[] texParams) {
	GL.Texture[] textures = f.getTextures();
	for(int t=0; t<textures.length; t++) {
	    GL.Texture tex = textures[t];
	    tex.setTexParameter("TEXTURE_2D", 
			"GENERATE_MIPMAP_SGIS", "FALSE");
	    int[] widths = new int[20];
	    int[] heights = new int[20];
	    byte[][] bytes = new byte[20][];
	    for(int lod = 0; ; lod++) {
		widths[lod] = (int)(tex.getLevelParameter(lod, "TEXTURE_WIDTH")[0]);
		heights[lod] = (int)(tex.getLevelParameter(lod, "TEXTURE_HEIGHT")[0]);
		int width = widths[lod];
		int height = heights[lod];
		bytes[lod] = new byte[width * height];
		tex.getTexImage(lod, "ALPHA", "UNSIGNED_BYTE", bytes[lod]);
		if(width == 1 && height == 1)
		    break;
	    }

	    GL.call("DeleteTextures "+tex.getTexId()+"\n");

	    for(int i=0; i<texParams.length; i+=2) 
		tex.setTexParameter("TEXTURE_2D", texParams[i], texParams[i+1]);

	    for(int lod = 0; bytes[lod] != null; lod++) {
		int width = widths[lod];
		int height = heights[lod];
		byte[] data = bytes[lod];


		tex.texImage2D(lod, newFormat, width, height, 0, 
					newFormat, "UNSIGNED_BYTE", data);

		/*
		pa("Converted "+a+" "+b+" "+width+" "+height);
		for(int i=0; i<data.length; i++) {
		    pa("Comp: "+i+" "+data[i]+" "+dataInto[i]);
		    if(data[i] != dataInto[i]) 
			throw new Error("?!?!");
		}
		*/

	    }
	}
    }

    /** Get a DrawPixels gl renderable for drawing the actual texels
     * of a given level for a given character using a GL_LUMINANCE mapping.
     */
    static public org.nongnu.libvob.Vob getLevelDrawPixels(GL.QuadFont f, int glyph, int lod) {
	GL.Texture[] textures = f.getTextures();
	float[] meas = f.getMeasurements(glyph);
	GL.Texture tex = textures[(int)meas[0]];

	float tx0 = meas[5];
	float ty0 = meas[6];
	float tx1 = meas[7];
	float ty1 = meas[8];

	int width = (int)(tex.getLevelParameter(lod, "TEXTURE_WIDTH")[0]);
	int height = (int)(tex.getLevelParameter(lod, "TEXTURE_HEIGHT")[0]);
	byte[] data = new byte[width * height];
	tex.getTexImage(lod, "ALPHA", "UNSIGNED_BYTE", data);

	int x = (int)(tx0 * width);
	int w = (int)Math.ceil(tx1 * width) - x;

	int y = (int)(ty0 * height);
	int h = (int)Math.ceil(ty1 * height) - y;

	byte[] ndata = new byte[w*h];
	for(int xi = 0; xi < w; xi ++) {
	    for(int yi = 0; yi < h; yi ++) {
		ndata[xi + w*yi] = data[x + xi + width * (y + (h-1 - yi))];
	    }
	}
	GL.ByteVector v = GL.createByteVector(w*h);
	v.set(ndata);

	pa("getleveldp: "+glyph+" "+lod+" "+x+" "+y+" "+w+" "+h);
	if(dbg) if(w < 20 && h < 20) {
	    StringBuffer s = new StringBuffer();
	    StringBuffer s0 = new StringBuffer();
	    StringBuffer s1 = new StringBuffer();
	    for(int i=0; i<w*h; i++){
		s.append(" ");
		s.append(v.get(i));
		s0.append(" ");
		s0.append(ndata[i]);
	    }
	    pa(""+s);
	    pa(""+s0);
	}

	return GLRen.createDrawPixels(w, h, "LUMINANCE", "UNSIGNED_BYTE", v);

    }

}



