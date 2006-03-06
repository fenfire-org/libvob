package org.nongnu.libvob.gl.impl.lwjgl.texture;

import java.nio.Buffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class TextureUtils {

    public static void buildMipmaps(int target, int intformat, int w, int h, int format, int type, FloatBuffer pixels) {

	if (!((w - 1 & w) == 0 && (h - 1 & h) == 0)) throw new Error("aasert");

	int comp = 0;
	switch (format) {
		default: 
		case GL11.GL_COLOR_INDEX: 
		    throw new Error();
//		    assert(0); 
//		    break;
		case GL11.GL_DEPTH_COMPONENT:
		case GL11.GL_RED:
		case GL11.GL_GREEN:  
		case GL11.GL_BLUE: 
		case GL11.GL_ALPHA:
		case GL11.GL_LUMINANCE:  
		    comp = 1; break;
		case GL11.GL_LUMINANCE_ALPHA: 
		    comp = 2; break;
		case GL11.GL_RGB:   
		case GL12.GL_BGR:   
		    comp = 3; break;
		case GL11.GL_RGBA:   
		case GL12.GL_BGRA:
		    comp = 4; break;
	}

	int w2 = w, h2 = h;
	int level = 0;

	Buffer data = null;
//	char *data = new char[w * h * comp * 4];

	while (true) {
	    int xf = w / w2;
	    int yf = h / h2;
	    switch (type) {
//	    	case GL11.GL_UNSIGNED_BYTE: 
//		  filter((unsigned char *)data, (unsigned char *)pixels, w, h, comp, xf, yf); break;
//		case GL11.GL_BYTE: 
//		  filter((char *)data, (char *)pixels, w, h, comp, xf, yf); break;
//		case GL11.GL_UNSIGNED_SHORT: 
//		  filter((unsigned short *)data, (unsigned short *)pixels, w, h, comp, xf, yf); break;
//		case GL11.GL_SHORT: 
//		  filter((short *)data, (short *)pixels, w, h, comp, xf, yf); break;
//		case GL11.GL_UNSIGNED_INT: 
//		  filter((unsigned int *)data, (unsigned int *)pixels, w, h, comp, xf, yf); break;
//		case GL11.GL_INT: 
//		  filter((int *)data, (int *)pixels, w, h, comp, xf, yf); break;
		case GL11.GL_FLOAT: {
		    if (data == null)
			data = BufferUtils.createFloatBuffer(w * h * comp * 4);
		    filter(data, pixels, w, h, comp, xf, yf); 
		    break;
		}
		default:
		    throw new Error();
		    //  assert(0);
	    }
	    data.flip();
	    
	    if (data instanceof FloatBuffer)
		GL11.glTexImage2D(target, level, intformat, w2, h2, 0, format, type, (FloatBuffer)data);
	    level++;
		
	    if (w2 <= 1 && h2 <= 1) break;
	    w2 = w2 + 1 >> 1;
	    h2 = h2 + 1 >> 1;
	}
	      
	GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
	GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, level - 1);
    }

    static void filter(Buffer dst, Buffer src, int w, int h, int comp, int xfact, int yfact) {
	for (int y = 0; y < h; y += yfact)
	    for (int x = 0; x < w; x += xfact)
		for (int c = 0; c < comp; c++) {
		    float value = 0;
		    for (int j = 0; j < xfact; j++)
			for (int i = 0; i < yfact; i++)
			    if (src instanceof FloatBuffer)
				value += ((FloatBuffer) src).get(c + (x + i + w * (y + j)) * comp);
		    //*dst++ = (TYPE)(value / (xfact * yfact) + (.5f - (TYPE).5f));
		    if (dst instanceof FloatBuffer)
			((FloatBuffer) dst).put(value / (xfact * yfact) + (.5f - .5f));
		}
    }
    
    
    
    /*
     *     template <class TYPE>
    void filter(TYPE *dst, TYPE *src, GLsizei w, GLsizei h, int comp, int xfact, int yfact) {
      for (int y = 0; y < h; y += yfact)
	for (int x = 0; x < w; x += xfact)
	  for (int c = 0; c < comp; c++)
	    {
	      float value = 0;
	      for (int j = 0; j < xfact; j++)
		for (int i = 0; i < yfact; i++)
		  value += src[c + (x + i + w * (y + j)) * comp];
	      *dst++ = (TYPE)(value / (xfact * yfact) + (.5f - (TYPE).5f));
	    }
    }
     * 
     */
    
}
