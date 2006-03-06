package org.nongnu.libvob.gl.impl;

import org.nongnu.libvob.gl.GL;
import org.nongnu.libvob.gl.GL.Image;
import org.nongnu.libvob.gl.GL.NonRenderableJavaObject;
import org.nongnu.libvob.gl.GL.RenderingSurface;
import org.nongnu.libvob.gl.GL.Texture;

public class NativeLoopBack implements GL.GLinstance {

    public String iGetGLString(String name) {
	return GL.getGLString(name);
    }

    public GL.Texture iCreateTexture() {
	return new NatTexture(GL.impl_createTexture(), true);
    }
    
    
    
    
    
//  --------- Texture
    /** A texture object. Represents a single OpenGL texture object.
     * Here, id == directly the texture id.
     */
    static public class NatTexture extends GL.Texture { 
	/** Whether the destruction of this texture object
	 * should cause the underlying implementation texture
	 * object to be deleted.
	 */
	boolean delReal;

	/** Create a texture object whose GL texture will not be deleted
	 * upon the deletion of the Java object.
	 */
	private NatTexture(int id) { super(id); delReal = false; }
	/** Create a texture object whose GL texture may be deleted
	 * upon the deletion of the Java object.
	 * @param delReal If true, delete the OpenGL texture
	 */
	private NatTexture(int id, boolean delReal) { super(id); 
	    this.delReal = delReal; }
	protected void deleteObj() { 
	    if(delReal) GL.impl_deleteTexture(getId()); 
	}

	/** Delete this texture.
	 * Do not use this object any more after calling this
	 * method.
	 */
	public void deleteTexture() {
	    delReal = false;
	    GL.impl_deleteTexture(getId());
	}

	/** Get the OpenGL texture id of this texture.
	 */
	public int getTexId() { return getId(); }

	public void setTexParameter(String target, String param, float value) {
	    this.setTexParameter(target, param, ""+value);
	}
	public void setTexParameter(String target, String param, String value) {
	    GL.call("BindTexture "+target+" "+getTexId()+"\n"+
		    "TexParameter "+target+" "+param+" "+value+"\n"+
		    "BindTexture "+target+" 0\n");
	}

	/** Call libtexture to create the image into this texture object.
	 */
	public int shade(int w, int h, int d, int comps, 
		String internalFormat, String format,
		String shaderName, String[] params) {
	    return GL.impl_Texture_shade(getId(), w, h, d, comps, internalFormat, format,
		shaderName, params, false);
	}

	/** Call glGetCompressedTexImage.
	 */
	public byte[] getCompressedTexImage(int lod) {
	    return GL.impl_Texture_getCompressedTexImage(getId(), lod, null);
	}

	/** Call glGetCompressedTexImage, with an array for the data.
	 */
	public byte[] getCompressedTexImage(int lod, byte[] prearr) {
	    return GL.impl_Texture_getCompressedTexImage(getId(), lod, prearr);
	}

	public void getTexImage(int lod, String format, String type,
			byte[] array) {
	    GL.impl_Texture_getTexImage(getId(), lod, format, type,
			    array);
	}

	/** Call glCompressedTexImage.
	 * The length of data is used so it needs to be right.
	 */
	public void compressedTexImage(int level, 
		    String internalFormat, int width, int height, 
			int border, byte[] data) {
	    compressedTexImage(level, internalFormat, width, height,
		    border, data.length, data);
	}
	public void compressedTexImage(int level, 
		    String internalFormat, int width, int height, 
			int border, int size, byte[] data) {
	    GL.impl_Texture_compressedTexImage(getId(), 
		    level, internalFormat, width, height,
			border, size, data);
	}

	public void compressedTexSubImage2D(int level,
		    int xoffs, int yoffs, int width, int height,
		    String format, int size, byte[] data) {
	    GL.impl_Texture_compressedTexSubImage2D(getId(),
		    level, xoffs, yoffs, width, height, format, size, data);
	}

	/** Call glTexImage2D.
	 * The length of data is used so it needs to be right.
	 */
	public void texImage2D(int level, 
			String internalFormat, int w, int h, 
			int border, String format, String type, 
			byte[] data) {
	    GL.impl_Texture_texImage2D(getId(), 
			level, internalFormat, w, h, border, format, 
			type, data);
	}

	/** Call glTexSubImage2D.
	 * The length of data is used so it needs to be right.
	 */
	public void texSubImage2D(int level, 
			int x, int y, int w, int h, 
			int border, String format, String type, 
			byte[] data) {
	    GL.impl_Texture_texSubImage2D(getId(), 
			level, x, y, w, h, border, format, 
			type, data);
	}

	/** Call libtexture to create the image for each mipmap level separately.
	 */
	public int shade_all_levels(int w, int h, int d, int comps, 
		String internalFormat, String format,
		String shaderName, String[] params) {
	    return GL.impl_Texture_shade(getId(), w, h, d, comps, internalFormat, format,
		shaderName, params, true);
	}

	/** Load a NULL pointer to the texture, which clears the image
	 * and sets the mip maps.
	 */
	public void loadNull2D(String target, int level, 
			String internalFormat, int w, int h, 
			int border, String format, String type) {
	    GL.impl_Texture_loadNull2D(getId(), target,
			level, internalFormat, w, h, border, format, type);
	}


	/** Load an image into a part of this texture.
	 */
	public void loadSubImage(int level, Image img, int x, int y, int xoffs, int yoffs, int w, int h) {
	    GL.impl_Texture_loadSubImage(getId(), level, img.getId(), x, y, xoffs, yoffs, w, h);
	}

	/** Read into this texture from screen.
	 */
	public void copyTexImage2D(RenderingSurface win, String buffer,
		    String target, int level,
		    String internalFormat, int x, int y,
		    int w, int h, int border) {
	    GL.impl_Texture_copyTexImage2D(getId(), win.getId(), 
			buffer, target,
			level, internalFormat, x, y, w, h,
			border);
	}
	public float[] getParameter(String name) {
	    return GL.getGLTexParameterFloat("TEXTURE_2D", getId(), name);
	}
	public float[] getLevelParameter(int level, String name) {
	    return GL.getGLTexLevelParameterFloat("TEXTURE_2D", getId(), level, name);
	}

    }

    
}
