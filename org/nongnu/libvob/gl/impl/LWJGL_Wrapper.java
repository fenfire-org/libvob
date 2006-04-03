package org.nongnu.libvob.gl.impl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.glu.GLU;
import org.nongnu.libvob.gl.GL;
import org.nongnu.libvob.gl.GL.Image;
import org.nongnu.libvob.gl.GL.RenderingSurface;
import org.nongnu.libvob.gl.impl.lwjgl.CallGL;
import org.nongnu.libvob.gl.impl.lwjgl.texture.NamedTexture;
import org.nongnu.libvob.gl.impl.lwjgl.texture.TextureParam;
import org.nongnu.libvob.gl.impl.lwjgl.texture.TextureUtils;

public class LWJGL_Wrapper implements GL.GLinstance {

    public String iGetGLString(String name) {
	if (name.equalsIgnoreCase("EXTENSIONS"))
	    return GL11.glGetString(GL11.GL_EXTENSIONS);
	else if (name.equalsIgnoreCase("VERSION"))
	    return GL11.glGetString(GL11.GL_VERSION);
	throw new Error("Unknown name: "+name);
    }

    public static String getGLString(String name) {
	return GL11.glGetString(GL11.GL_EXTENSIONS);
    }

    public GL.Texture iCreateTexture() {
	return new LwjglTexture();
    }

    static class LwjglTexture extends GL.Texture {
	IntBuffer textId = BufferUtils.createIntBuffer(1);
	LwjglTexture() {
	    super(-1);
	    GL11.glGenTextures(textId);
	}
	/** Get the OpenGL texture id of this texture.
	 */
	public int getTexId() { return textId.get(0); }

	public void deleteTexture() {
	}
	public int shade(int w, int h, int d, int comps, String internalFormat, String format, String shaderName, String[] params) {
	    return shadeImpl(w,h,d,comps, internalFormat, format, shaderName, params, false);
	}
	public int shade_all_levels(int w, int h, int d, int comps, String internalFormat, String format, String shaderName, String[] params) {
	    return shadeImpl(w,h,d,comps, internalFormat, format, shaderName, params, true);
	}
	public int shadeImpl(int w, int h, int d, int comps, String internalFormat, String format, String shaderName, String[] params, boolean shade_all_levels) {
	    System.out.println(w+", "+h+", "+d+", "+comps+", "+internalFormat+", "+format+", "+shaderName+", ");
	    for (int i = 0; i < params.length; i++) {
		System.out.println(i+": "+params[i]);
	    }
	    
	    NamedTexture s = NamedTexture.getTexture(shaderName);
	    if (s == null) return 0;

	    TextureParam p = new TextureParam(params);
	    FloatBuffer value = BufferUtils.createFloatBuffer(w * h * (d==0?1:d) * comps);

	    int target = (d == 0) ? GL11.GL_TEXTURE_2D : GL12.GL_TEXTURE_3D;

	    GL11.glBindTexture(target, textId.get(0));

	    boolean buildmipmaps = false;

	      if (!shade_all_levels) {
		  if (false && GL.hasExtension("GL_SGIS_generate_mipmap")) {
		      throw new Error("unimpl.");
//		      GL11.glTexParameteri(target, GLU.GL_GENERATE_MIPMAP_SGIS, GL11.GL_TRUE);
//		      
//		      GLERR;
		  } else {
		      buildmipmaps = true;
		  }
	      } 

	      value.position(0);
	      s.render(p, w, h, (d==0?1:d), comps, value);

	      int level;
	      for (level = 0;; level++) {

		  if (buildmipmaps) {
		      if (d != 0) //assert(d==0); // 3D buildmipmaps not implemented in libutil
			  throw new Error("un impl. 3D buildmipmaps");
//		      Util::buildmipmaps(GL_TEXTURE_2D, 
//					 tokenFromJstring(env, internalFormat),
//					 w, h, 
//					 tokenFromJstring(env, format),
//					 GL_FLOAT,
//					 value);
		      TextureUtils.buildMipmaps(GL11.GL_TEXTURE_2D, CallGL.getToken(internalFormat),
			      w,h, CallGL.getToken(format), GL11.GL_FLOAT, value);
		  } else 
		      value.flip();
		      if (d == 0)
			  GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, CallGL.getToken(internalFormat),
				   w, h, 0, CallGL.getToken(format), GL11.GL_FLOAT, value);
		      else
			  GL12.glTexImage3D(GL12.GL_TEXTURE_3D, level, CallGL.getToken(internalFormat),
				   w, h, d, 0, CallGL.getToken(format), 
				   GL11.GL_FLOAT,  value);
		  CallGL.checkGlError("after shade");
		      
		  if (! (shade_all_levels && (w > 1 || h > 1 || d > 1))) break;
		  
		  w = (w + 1) >> 1;
		  h = (h + 1) >> 1;
		  d = (d + 1) >> 1;
	      } 
	      
	      if (shade_all_levels) {
		  GL11.glTexParameterf(target, GL12.GL_TEXTURE_BASE_LEVEL, 0);
		  GL11.glTexParameterf(target, GL12.GL_TEXTURE_MAX_LEVEL, level);
	      }

	      GL11.glBindTexture(target, 0);
	      CallGL.checkGlError("shade done.");

	      return 1;
	}

	public void setTexParameter(String target, String param, String value) {
	    CallGL.call("BindTexture "+target+" "+textId.get(0)+"\n"+
	    "TexParameter "+target+" "+param+" "+value+"\n"+
	    "BindTexture "+target+" 0\n");
	}

	
	public byte[] getCompressedTexImage(int lod) {
	    return null;
	}
	public byte[] getCompressedTexImage(int lod, byte[] prearr) {
	    return null;
	}
	public void getTexImage(int lod, String format, String type, byte[] array) {
	}
	public void compressedTexImage(int level, String internalFormat, int width, int height, int border, byte[] data) {
	}
	public void compressedTexImage(int level, String internalFormat, int width, int height, int border, int size, byte[] data) {
	}
	public void compressedTexSubImage2D(int level, int xoffs, int yoffs, int width, int height, String format, int size, byte[] data) {
	}
	public void texImage2D(int level, String internalFormat, int w, int h, int border, String format, String type, byte[] data) {
	}
	public void texSubImage2D(int level, int x, int y, int w, int h, int border, String format, String type, byte[] data) {
	}
	public void loadNull2D(String target, int level, String internalFormat, int w, int h, int border, String format, String type) {
	}
	public void loadSubImage(int level, Image img, int x, int y, int xoffs, int yoffs, int w, int h) {
	}
	public void copyTexImage2D(RenderingSurface win, String buffer, String target, int level, String internalFormat, int x, int y, int w, int h, int border) {
	}
	public float[] getParameter(String name) {
	    return null;
	}
	public float[] getLevelParameter(int level, String name) {
	    return null;
	}
	protected void deleteObj() {
	}
    }
}
