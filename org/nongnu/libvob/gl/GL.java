/*
GL.java
 *
 *    Copyright (c) 2001-2003, Tuomas J. Lukka
 *                  2004 Matti J. Katila
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
 * Written by Tuomas J. Lukka
 */
package org.nongnu.libvob.gl;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.impl.LWJGL_Wrapper;
import org.nongnu.libvob.gl.impl.NativeLoopBack;

/** The interface to the native OpenGL library.
 * Note: here we must be VERY careful, as this is one of the places
 * where foreign code is not sandboxed automatically for us.
 *
 * All parameters that go to C level must be checked either here
 * or at the C level, otherwise -- BOOM.
 */
public class GL {
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println(s); }

    /** Initialize the native library by creating
     * OpenGL contexts and the like.
     */
    private static native int init(int debug);

    static private boolean firstWindowTaken = false;

    /** Whether the library is loaded.
     */
    private static boolean libLoaded = false;

    /** Load the OpenGL library.
     * Used to set debug variables prior to 
     * initializing (which does quite a lot of stuff
     * and needs to be debugged sometimes).
     */
    public static void loadLib() {
	if(dbg) pa("libvob-GL loadLib" + libLoaded);
	if(!libLoaded) {
	    System.loadLibrary("vobjni");
	    libLoaded = true;
	}
    }

    /** ATI Linux drivers for the R300 family still have some serious bugs.
     * We have workarounds for some in place; this flag
     * will enable them.
     * <p>
     * ATI: if you want to know what you're still doing
     * wrong, just grep the source for this variable name ;)
     * <p>
     * A brief list of some of the issues:
     * <ul>
     * <li> Loading compressed textures using glCompressedTexImage2D
     *      does not work. VERY bad for us, need to load full
     *      uncompressed levels from disk. Can't work around
     *      without losing a lot of performance
     * <li> glCopyTexImage does not obey GENERATE_MIPMAP_SGIS.
     *      Can work around; not fun.
     * <li> Something about our texture image copying code
     *      makes it take it from the wrong location - 
     *      glGet raster position?
     * <li> pbuffers not usable on non-firegl cards
     * </ul>
     */
    public static boolean workaroundStupidBuggyAtiDrivers = false;

    /**
     * The instance of GL. 
     */
    private static GLinstance instance = null;

    static public interface GLinstance {
	String iGetGLString(String s);
	
    }
    
    
    /** Init the library - to be called once during startup.
     */
    public static void init() {
	if (GraphicsAPI.getInstance() instanceof org.nongnu.libvob.impl.lwjgl.LWJGL_API)
	{
	    instance = new LWJGL_Wrapper();
	} else {
	    instance = new NativeLoopBack();
	    loadLib();
	    init(1);
	    if(getGLString("VENDOR").startsWith("ATI ")) {
		pa("WARNING: ATI Linux drivers still unstable - trying to work around what I can.");
		workaroundStupidBuggyAtiDrivers = true;
	    }
	}
    }

    /** An interface to which GL provides events.
     */
    public interface EventHandler {
	int PRESS = 1;
	int MOTION = 2;
	int RELEASE = 3;
	void repaint();
	/** Receive a keystroke event.
	 */
	void keystroke(String s);
	/** Receive a mouse event.
	 * The modifiers are coded (hard-coded) with the
	 * org.nongnu.libvob.VobMouseEvent modifier masks.
	 */
	void mouse(int x, int y, int button, int type, int modifiers);
	void timeout(int id);
	void windowClosed();
    }

    /** Constants to bit-or with coordsys types.
     */
    public static final int CSFLAG_ACTIVE = 0x80000000,
	                    CSFLAG_ACTIVE_REGION = 0x70000000,
			    CSFLAGS = 0xf0000000; 

   

    /** Constant for the int array to be passed to
     * C++, or'ed together with the ID, indicating
     * a zero-argument renderable.
     */
    public static final int RENDERABLE0 = 0x1000000;
    /** See RENDERABLE0.
     */
    public static final int RENDERABLE1 = 0x2000000;
    /** See RENDERABLE0.
     */
    public static final int RENDERABLE2 = 0x3000000;
    /** See RENDERABLE0.
     */
    public static final int RENDERABLE3 = 0x4000000;
    /** See RENDERABLE0.
     */
    public static final int RENDERABLEN = 0x5000000;
    /** See ChildVobScene.
     */
    public static final int RENDERABLE_VS = 0x6000000;

    /** The mask for different renderables.
     */
    public static final int RMASK = GL.RENDERABLE0 | GL.RENDERABLE1 | GL.RENDERABLE2 | GL.RENDERABLE3 | GL.RENDERABLEN | GL.RENDERABLE_VS;
    
    static ArrayList queue = new ArrayList();

    /** Because objects have to be released by the same thread
     * that obtained them, this method needs to be called every
     * once in a while.
     */
    static public void freeQueue() {
	synchronized(queue) {
	    for(Iterator i = queue.iterator(); i.hasNext(); ) {
		GLDeletable obj = ((GLDeletable)i.next());
		obj.deleteObject();
	    }
	    queue.clear();
	}
    }

    /** To be called from d.finalize(): set this object's
     * deleteObject() to be called in GL thread.
     */
    static public void addDeletable(GLDeletable d) {
	synchronized(queue) {
	    queue.add(d);
	}
    }

    /** The Java proxy for a C++ object.
     */
    static public abstract class JavaObject extends AbstractVob 
	    implements GLDeletable {
	private int id = 0;
	JavaObject(int id) { super(); this.id = id; }
	/** During garbage collection: destroy the C++ object associated with this object.
	 */
	public void finalize() {
	    if(GL.dbg) pa("Finalizing "+this+" "+id);
	    // Would like to
	    // 		deleteObj();
	    // but can't due to thread problems.
	    if(id != 0) addDeletable(this);
	}
	public void render(java.awt.Graphics g, 
			    boolean fast,
			    Vob.RenderInfo info1,
			    Vob.RenderInfo info2
			    )  { }
	/** Delete the C++ object corresponding to this object.
	 * Note that using this object anywhere afterwards
	 * may cause Null pointers or even worse on the C++ side.
	 * Use with MUCH care.
	 */
	public void deleteObject() {
	    if(GL.dbg) pa("DeleteObj "+this+" "+id);
	    deleteObj();
	    id = 0;
	}
	protected abstract void deleteObj();
	/** Get the C++ integer id associated with this object.
	 */
	protected int getId() { return id; }
    }

    /** A Java object which is not supposed to be placed on display lists.
     * This is here because Vob is a class, not an interface and
     * we don't want to implement JavaObject twice.
     */
    static public abstract class NonRenderableJavaObject extends JavaObject {
	protected NonRenderableJavaObject(int id) { super(id); }
    }

    /** The Java proxy representing a Renderable object.
     */
    static public abstract class Renderable0JavaObject extends JavaObject {
	public Renderable0JavaObject(int id) { super(id); }
	public int putGL(VobScene vs) {
	    return getId();
	}
	protected void deleteObj() {
	    deleteRenderable0(getId());
	}
    }
    static private native void deleteRenderable0(int id);


    /** The Java proxy representing a Renderable object.
     */
    static public abstract class Renderable1JavaObject extends JavaObject {
	public Renderable1JavaObject(int id) { super(id); }
	public int putGL(VobScene vs, int coordsys1) {
	    return getId();
	}
	protected void deleteObj() {
	    deleteRenderable1(getId());
	}
    }
    static private native void deleteRenderable1(int id);

    /** The Java proxy representing a Renderable2 object.
     */
    static public abstract class Renderable2JavaObject extends JavaObject {
	public Renderable2JavaObject(int id) { super(id); }
	public int putGL(VobScene vs, int coordsys1, int coordsys2) {
	    return getId();
	}
	protected void deleteObj() {
	    deleteRenderable2(getId());
	}
    }
    static private native void deleteRenderable2(int id);

    /** The Java proxy representing a Renderable3 object.
     */
    static public abstract class Renderable3JavaObject extends JavaObject {
	public Renderable3JavaObject(int id) { super(id); }
	public int putGL(VobScene vs, int coordsys1, int coordsys2, int coordsys3) {
	    return getId();
	}
	protected void deleteObj() {
	    deleteRenderable3(getId());
	}
    }
    static private native void deleteRenderable3(int id);

    /** The Java proxy representing a RenderableN object.
     */
    static public abstract class RenderableNJavaObject extends JavaObject {
	public RenderableNJavaObject(int id) { super(id); }
	public int putGL(VobScene vs, int[] cs) {
	    return getId();
	}
	protected void deleteObj() {
	    deleteRenderableN(getId());
	}
    }
    static private native void deleteRenderableN(int id);


//--------- Renderable surface
    static public class RenderingSurface extends NonRenderableJavaObject {
	private RenderingSurface(int id) { super(id); }
	protected void deleteObj() { deleteWindow(getId()); }

	/** Get the current bounds of the window on screen.
	 * For off-screen surfaces, only width and height are
	 * significant.
	 */
	public Rectangle getBounds() {
	    Rectangle rect = new Rectangle();
	    getWindowSize(getId(), rect);
	    return rect;
	}

	/** Set this window to be the current OpenGL context.
	 */
	public void setCurrent() { impl_Window_setCurrent(getId()); }
	/** Release this window from being the current OpenGL context.
	 */
	public void release() { impl_Window_release(getId()); }
    }
    static public RenderingSurface createStableRenderingSurface(int w, int h) {
	return new RenderingSurface(createStableRenderingSurfaceImpl(w, h));
    }
    static private native int createStableRenderingSurfaceImpl(int w, int h);
//--------- Window
    /** An on-screen GLX window into which graphics can be drawn.
     */
    final static public class Window extends RenderingSurface {

	private Window(int id) { super(id); }


	/** Call the EventHandler.timeout(id) with the given id,
	 * after at least ms milliseconds have passed.
	 */
	public void addTimeout(int ms, int id) {
	    addTimeoutWindow(getId(), ms, id);
	}

	/** Move the upper left corner of the window to the given coordinates.
	 */
	public void move(int x, int y) { impl_Window_move(getId(), x, y); }
	/** Resize the window.
	 */
	public void resize(int w, int h) { impl_Window_resize(getId(), w, h); }
	/** Set the mouse cursor of the window.
	 */
	public void setCursor(String name) { impl_Window_setCursor(getId(), name); }
    }
    final private static Window defaultWindow = new Window(-1);

    /** Create a new window.
     */
    static public Window createWindow(int x, int y, int w, int h, EventHandler eh) {
	boolean first = false;
	if(! firstWindowTaken) {
	    first = true;
	    firstWindowTaken = true;
	}
	return new Window(createWindowImpl(first, x, y, w, h, eh));
    }
    static private native int createWindowImpl(boolean first, int x, int y, int w, int h, EventHandler eh);
    static private native void deleteWindow(int i);

    static private native void getWindowSize(int id, Rectangle into);

    static private native void addTimeoutWindow(int id, int ms, int tid);

    static private native void impl_Window_setCurrent(int id);
    static private native void impl_Window_release(int id);
    static private native void impl_Window_move(int id, int x, int y);
    static private native void impl_Window_setCursor(int id, String name);
    static private native void impl_Window_resize(int id, int w, int h);

//--------- Image
    /** A buffer on the C++ side, containing a single image.
     */
    static public class Image extends NonRenderableJavaObject {
	private Image(int id) { super(id); }
	protected void deleteObj() { deleteImage(getId()); }
	/** Get the size of this Image.
	 * @param dimNo The dimension (0=x, 1=y) to get.
	 */
	public int getSize(int dimNo) { return getImageSize(getId(), dimNo); }
	public int getPixel(int offset) { return getImagePixel(getId(), offset); }
	public Dimension getSize() {
	    return new Dimension( getSize(0), getSize(1) );
	}
    }

    /** Create a new image from the prescribed file.
     * THIS METHOD IS A SEVERE SECURITY HOLE AND WILL BE REMOVED OR ADJUSTED
     * TO USE A SECURITY MANAGER OR SO.
     * Exploit: load something that the image loader library doesn't like...
     * Need to work out how this should properly interact with mediaserver.
     * <p>
     * This method is VERY special: it is NOT necessary to have
     * an OpenGL context for the thread it uses since
     * it is <b>guaranteed</b> not to use OpenGL.
     */
    static public Image createImage(String filename) {
	return new Image(createImageImpl(filename));
    }
    static private native int createImageImpl(String filename);
    static private native void deleteImage(int i);
    static private native int getImageSize(int id, int dimNo);
    static private native int getImagePixel(int id, int offset);


//--------- Texture
    /** A texture object. Represents a single OpenGL texture object.
     * Here, id == directly the texture id.
     */
    static public class Texture extends NonRenderableJavaObject {
	/** Whether the destruction of this texture object
	 * should cause the underlying implementation texture
	 * object to be deleted.
	 */
	boolean delReal;

	/** Create a texture object whose GL texture will not be deleted
	 * upon the deletion of the Java object.
	 */
	private Texture(int id) { super(id); delReal = false; }
	/** Create a texture object whose GL texture may be deleted
	 * upon the deletion of the Java object.
	 * @param delReal If true, delete the OpenGL texture
	 */
	private Texture(int id, boolean delReal) { super(id); 
	    this.delReal = delReal; }
	protected void deleteObj() { 
	    if(delReal) impl_deleteTexture(getId()); 
	}

	/** Delete this texture.
	 * Do not use this object any more after calling this
	 * method.
	 */
	public void deleteTexture() {
	    delReal = false;
	    impl_deleteTexture(getId());
	}

	/** Get the OpenGL texture id of this texture.
	 */
	public int getTexId() { return getId(); }

	public void setTexParameter(String target, String param, float value) {
	    this.setTexParameter(target, param, ""+value);
	}
	public void setTexParameter(String target, String param, String value) {
	    call("BindTexture "+target+" "+getTexId()+"\n"+
		    "TexParameter "+target+" "+param+" "+value+"\n"+
		    "BindTexture "+target+" 0\n");
	}

	/** Call libtexture to create the image into this texture object.
	 */
	public int shade(int w, int h, int d, int comps, 
		String internalFormat, String format,
		String shaderName, String[] params) {
	    return impl_Texture_shade(getId(), w, h, d, comps, internalFormat, format,
		shaderName, params, false);
	}

	/** Call glGetCompressedTexImage.
	 */
	public byte[] getCompressedTexImage(int lod) {
	    return impl_Texture_getCompressedTexImage(getId(), lod, null);
	}

	/** Call glGetCompressedTexImage, with an array for the data.
	 */
	public byte[] getCompressedTexImage(int lod, byte[] prearr) {
	    return impl_Texture_getCompressedTexImage(getId(), lod, prearr);
	}

	public void getTexImage(int lod, String format, String type,
			byte[] array) {
	    impl_Texture_getTexImage(getId(), lod, format, type,
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
	    impl_Texture_compressedTexImage(getId(), 
		    level, internalFormat, width, height,
			border, size, data);
	}

	public void compressedTexSubImage2D(int level,
		    int xoffs, int yoffs, int width, int height,
		    String format, int size, byte[] data) {
	    impl_Texture_compressedTexSubImage2D(getId(),
		    level, xoffs, yoffs, width, height, format, size, data);
	}

	/** Call glTexImage2D.
	 * The length of data is used so it needs to be right.
	 */
	public void texImage2D(int level, 
			String internalFormat, int w, int h, 
			int border, String format, String type, 
			byte[] data) {
	    impl_Texture_texImage2D(getId(), 
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
	    impl_Texture_texSubImage2D(getId(), 
			level, x, y, w, h, border, format, 
			type, data);
	}

	/** Call libtexture to create the image for each mipmap level separately.
	 */
	public int shade_all_levels(int w, int h, int d, int comps, 
		String internalFormat, String format,
		String shaderName, String[] params) {
	    return impl_Texture_shade(getId(), w, h, d, comps, internalFormat, format,
		shaderName, params, true);
	}

	/** Load a NULL pointer to the texture, which clears the image
	 * and sets the mip maps.
	 */
	public void loadNull2D(String target, int level, 
			String internalFormat, int w, int h, 
			int border, String format, String type) {
	    impl_Texture_loadNull2D(getId(), target,
			level, internalFormat, w, h, border, format, type);
	}


	/** Load an image into a part of this texture.
	 */
	public void loadSubImage(int level, Image img, int x, int y, int xoffs, int yoffs, int w, int h) {
	    impl_Texture_loadSubImage(getId(), level, img.getId(), x, y, xoffs, yoffs, w, h);
	}

	/** Read into this texture from screen.
	 */
	public void copyTexImage2D(RenderingSurface win, String buffer,
		    String target, int level,
		    String internalFormat, int x, int y,
		    int w, int h, int border) {
	    impl_Texture_copyTexImage2D(getId(), win.getId(), 
			buffer, target,
			level, internalFormat, x, y, w, h,
			border);
	}
	public float[] getParameter(String name) {
	    return getGLTexParameterFloat("TEXTURE_2D", getId(), name);
	}
	public float[] getLevelParameter(int level, String name) {
	    return getGLTexLevelParameterFloat("TEXTURE_2D", getId(), level, name);
	}

    }

    /** Create a new OpenGL texture object.
     */
    static public Texture createTexture() { 
	return new Texture(impl_createTexture(), true);
    }
    static private native int impl_createTexture();
    static private native void impl_deleteTexture(int id);
    static private native int impl_Texture_shade(int id, int w, int h, int d, int comps, 
		String internalFormat, String format,
		String shaderName, String[] params, 
		boolean shade_all_levels);

    static private native void impl_Texture_loadNull2D(int id, String target, 
			int level, 
			String internalFormat, int w, int h, 
			int border, String format, String type) ;
    static private native void impl_Texture_texImage2D(int id, int level, 
			String internalFormat, int w, int h, 
			int border, String format, String type, byte[] data) ;
    static private native void impl_Texture_texSubImage2D(int id, int level, 
			int x, int y, int w, int h, 
			int border, String format, String type, byte[] data) ;
    static private native void impl_Texture_loadSubImage(int id,
	int level, int imgid, int x, int y, int xoffs, int yoffs, int w, int h) ;
    static private native void impl_Texture_copyTexImage2D(
		int id, int wid, String buffer,
		    String target, int level,
		    String internalFormat, int x, int y,
		    int w, int h, int border) ;
    static private native byte[] impl_Texture_getCompressedTexImage(int id, int lod, byte[] preArray);
    static private native void impl_Texture_compressedTexImage(int id, int level, String internalFormat,
		    int width, int height, int border, int size, byte[] data);
    static private native void impl_Texture_compressedTexSubImage2D(int id, int level,
		    int xoffs, int yoffs, int width, int height, String format, int size, byte[] data);
    static private native void impl_Texture_getTexImage(int id, 
		    int lod, String format, String type, byte[] array) ;

// -------- IndirectTexture

    /** An indirect texture object. Like a pointer: can be set to 
     * point to any given real GL.Texture object.
     * This object exists because it's apparently unhealthy to allocate and
     * deallocate lots of textures in some OpenGL implementations, and we need
     * a fixed set of textures that we keep switching with each other.
     * On the implementation side, the value zero is used for a non-bound
     * indirect texture.
     */
    static public class IndirectTexture extends NonRenderableJavaObject {
	/** The real texture this indirect texture points to.
	 * May be null.
	 * This value is mirrored on the C++ side.
	 */
	private GL.Texture texture;

	private IndirectTexture(int id, GL.Texture texture) {
	    super(id);
	    setTexture(texture);
	}
	protected void deleteObj() { 
	    impl_IndirectTexture_delete(getId()); 
	}

	/** Set the texture this indirect texture should point to.
	 */
	public void setTexture(GL.Texture texture) {
	    this.texture = texture;
	    impl_IndirectTexture_setTexture(getId(), 
			texture == null ? 0 : texture.getTexId());
	}
	/** Get the texture this indirect texture currently points to.
	 */
	public GL.Texture getTexture() {
	    return texture;
	}

	public int getIndirectTextureId() {
	    return getId();
	}

    }

    static public IndirectTexture createIndirectTexture() {
	return createIndirectTexture(null);
    }
    static public IndirectTexture createIndirectTexture(Texture initialValue) {
	return new IndirectTexture(impl_IndirectTexture_create(),
				    initialValue);
    }

    static private native int impl_IndirectTexture_create();
    static private native int impl_IndirectTexture_setTexture(int id, int texid);
    static private native int impl_IndirectTexture_delete(int id);

// -------- FTFont
    /** A freetype font.
     * Not directly renderable - see GLFont for that.
     * This class exists for getting freetype-rendered glyphs
     * from C++ to Java.
     */
    static public class FTFont extends NonRenderableJavaObject{
	private FTFont(int id) {
	    super(id);
	}
	protected void deleteObj() { impl_FTFont_delete(getId()); }
	/** A low-level character measurements routine.
	 * Intended to be used by a higher-level wrapper.
	 * @return an int array, containing 6*characters.length
	 * 		elements, in groups of
	 *              (x,y,w,h, xadvance, yadvance).
	 *              The coordinates are in pixels, and
	 *              the advances are in FT fixed point units,
	 *              scaled by 2**6. For nonexistent glyphs, w=0 and h=0.
	 */
	public int[] getMeasurements(int[] characters) {
	    return impl_FTFont_getMeasurements(getId(), characters);
	}

	/** Get bitmaps corresponding to a number of characters.
	 * For nonexistent glyphs, null is returned.
	 */
	public byte[][] getBitmaps(int[] characters) {
	    return impl_FTFont_getBitmaps(getId(), characters);
	}

	/** Get the font recommended row height, in 
	 * pix * 2^6.
	 */
	public int getHeight() {
	    return impl_FTFont_getHeight(getId());
	}
	/** Get the font baseline offset, in pix.
	 */
	public int getYOffs() {
	    return impl_FTFont_getYOffs(getId());
	}
	
    }

    public static FTFont createFTFont(String filename, int pixelSize_X, 
						    int pixelSize_Y) {
	return createFTFont(filename, pixelSize_X, pixelSize_Y, 1 << 16, 0, 0, 1 << 16);
    }
    /** Create a freetype font, with the given
     * pixel sizes.
     */
    public static FTFont createFTFont(String filename, int pixelSize_X, 
						    int pixelSize_Y,
						    int xx, int xy, int yx, int yy) {
	int id = impl_FTFont_create(filename, pixelSize_X, pixelSize_Y, xx, xy, yx, yy);
	if(id < 0) 
	    throw new Error("Couldn't create font '"+filename+"'");
	return new FTFont(id);
    }
    private static native int impl_FTFont_create(String filename, int psx, int psy, 
						    int xx, int xy, int yx, int yy);
    private static native void impl_FTFont_delete(int id);
    private static native int[] impl_FTFont_getMeasurements(int id, int[] characters) ;
    private static native byte[][] impl_FTFont_getBitmaps(int id, int[] characters) ;
    private static native int impl_FTFont_getHeight(int id) ;
    private static native int impl_FTFont_getYOffs(int id) ;

//-------- QuadFont
    /** A font that is rendered as quads from an OpenGL texture.
     */
    static public class QuadFont extends NonRenderableJavaObject {
	protected QuadFont(int id) {
	    super(id);
	}
	protected void deleteObj() { impl_QuadFont_delete(getId()); }

	/** Store explicitly the GL.Texture objects
	 * to avoid GC.
	 */
	private GL.Texture[] textures;

	/** Set up the textures to use.
	 * @param texUnits The names of the texunits to bind textures to.
	 * @param targets The names of the texture targets to bind textures to.
	 *                targets.length == texUnits.length
	 * @param texCoordUnits The names of the texunits to set coordinates for.
	 * @param textures The textures. 
	 *                 textures.length = texUnits.length * number of font pages.
	 */
	public void setTextures(
	    String[] texUnits,
	    String[] targets,
	    String[] texCoordUnits,
	    GL.Texture[] textures) {

	    // Store away
	    this.textures = new GL.Texture[textures.length];
	    System.arraycopy(textures, 0, this.textures, 0, textures.length);

	    int[] texids = new int[textures.length];
	    for(int i=0; i<texids.length; i++)
		texids[i] = textures[i].getTexId();

	    impl_QuadFont_setTextures(getId(), texUnits, targets, texCoordUnits, 
					texids);

	}

	public String[] getTexUnits() {
	    return impl_QuadFont_getTexUnits(getId());
	}
	public String[] getTexTargets() {
	    return impl_QuadFont_getTexTargets(getId());
	}
	public GL.Texture[] getTextures() {
	    GL.Texture[] ret = new GL.Texture[textures.length];
	    System.arraycopy(this.textures, 0, ret, 0, this.textures.length);
	    return ret;
	}


	public void setNGlyphs(int n) {
	    impl_QuadFont_setNGlyphs(getId(), n);
	}

	/** Set the measurements of a single glyph.
	 * @param glyph The index of the glyph.
	 * @param texInd The index of the textures to be bound
	 *                for this glyph. 
	 */
	public void setMeasurements(int glyph, 
				int texInd,
				float x0, float y0, float x1, float y1,
				float tx0, float ty0, float tx1, float ty1,
				float xadvance, float yadvance) {
	    impl_QuadFont_setMeasurements(getId(),
			glyph, texInd, 
			x0, y0, x1, y1, 
			tx0, ty0, tx1, ty1,
			xadvance, yadvance);
	}

	/** Get the measurements for a single glyph.
	 * @param glyph The index of the glyph
	 * @return A float array of 11 elements, in order of parameters
	 * of setMeasurements
	 */
	public float[] getMeasurements(int glyph) {
	    return impl_QuadFont_getMeasurements(getId(),
			glyph);
	}
    }

    public static QuadFont createQuadFont() {
	return new QuadFont(impl_QuadFont_create());
    }
    private static native int impl_QuadFont_create();
    private static native void impl_QuadFont_delete(int id);
    private static native void impl_QuadFont_setTextures(int id,
	    String[] texUnits,
	    String[] targets,
	    String[] texCoordUnits,
	    int[] textures) ;
    private static native void impl_QuadFont_setNGlyphs(int id, int n);
    private static native void impl_QuadFont_setMeasurements(int id, int glyph, 
			    int texInd,
			    float x0, float y0, float x1, float y1,
			    float tx0, float ty0, float tx1, float ty1,
			    float xadvance, float yadvance) ;
    private static native float[] impl_QuadFont_getMeasurements(int id, int glyph);
    private static native String[] impl_QuadFont_getTexUnits(int id);
    private static native String[] impl_QuadFont_getTexTargets(int id);


//--------- OpenGL Program
    /** An OpenGL program object.
     * Like Texture, id = directly the program id
     */
    static public class Program extends NonRenderableJavaObject {
	private Program(int id) { super(id); }
	protected void deleteObj() { impl_deleteProgram(getId()); }
	public int getProgId() { return getId(); }
	public int load(String program) {
	    return impl_Program_load(getId(), program);
	}
	public int getInt(String target, String name) {
	    return getGLProgrami(target, getId(), name);
	}
    }
    static public Program createProgram() { 
	return new Program(impl_createProgram());
    }
    static public Program createProgram(String program) { 
	Program prog = new Program(impl_createProgram());
	prog.load(program);
	return prog;
    }
    static private native int impl_createProgram();
    static private native void impl_deleteProgram(int id);
    static private native int impl_Program_load(int id, String program);

//--------- DisplayList
    /** An OpenGL display list.
     */
    static public class DisplayList extends NonRenderableJavaObject{
	private ArrayList depends;

	private DisplayList(int id) {
	    super(id);
	}
	protected void deleteObj() { deleteDisplayList(getId()); }

	/** Start compiling the display list.
	 * @deprecated Use CallGL
	 */
	public void startCompile(RenderingSurface w) { GL.startCompile(getId(), 
						    w.getId()); }
	/** Start compiling the display list.
	 * @deprecated Use CallGL
	 */
	public void endCompile(RenderingSurface w) { GL.endCompile(getId(), 
						    w.getId()); }

	/** Compile the given string using the CallGL library
	 * into this display list.
	 */
	public void compileCallGL(String s) 
			{ GL.compileCallGL(getId(), s); }

	/** Call the display list in the given window.
	 */
	public void call(RenderingSurface w) { GL.impl_DisplayList_call(getId(), w.getId()); }
	/** Call the display list in the default context.
	 */
	public void call() { GL.impl_DisplayList_call0(getId()); }

	/** Get the display list ID.
	 * For use, e.g., in nesting display lists.
	 * Note that you must ensure no GC will occur.
	 * Calling addDepend is recommended.
	 */
	public int getDisplayListID() { return getId(); }

	/** Add a dependency.
	 * Adds an object that mustn't get garbage collected
	 * before this one.
	 * For example, if you use a texture id that you get from
	 * a GL.Texture object's getTexId() method, you should
	 * probably ensure that that texture won't get deleted
	 * before this display list. Calling displayList.addDepend()
	 * with the texture object ensures this as it stores a reference
	 * to the texture in this display list.
	 */
	public void addDepend(Object o) {
	    if(depends == null) depends = new ArrayList();
	    depends.add(o);
	}
    }

    /** Create a new, empty display list.
     */
    static public DisplayList createDisplayList() {
	return new DisplayList(createDisplayListImpl());
    }

    /** Create a new display list and compile the given string
     * using CallGL into it.
     */
    static public DisplayList createDisplayList(String s) {
	DisplayList d = new DisplayList(createDisplayListImpl());
	d.compileCallGL(s);
	return d;
    }
    static private native int createDisplayListImpl();
    static private native void startCompile(int id, int wid);
    static private native void endCompile(int id, int wid);
    static private native void compileCallGL(int id, String s);
    static private native void deleteDisplayList(int id);
    static private native void impl_DisplayList_call(int id, int winid);
    static private native void impl_DisplayList_call0(int id);

//---------- ByteVector
    /** A vector of bytes stored in C++ space.
     */
    static public class ByteVector extends NonRenderableJavaObject {
	private ByteVector(int id) {
	    super(id);
	    if(GL.dbg) pa("Create bytevector\n");
	}
	protected void deleteObj() { 
	    if(GL.dbg) pa("Delete bytevector\n");
	    deleteByteVector(getId()); 
	}

	/** Get a value from this byte vector.
	 * @param ind The index of the value to get.
	 */
	public int get(int ind) { return impl_ByteVector_get(getId(), ind); }

	/** Read pixels from an OpenGL buffer into this bytevector.
	 * @param win The window to read from
	 * @param buffer The OpenGL buffer to read from, e.g. "FRONT", "BACK"
	 * @param x The x coordinate to read from
	 * @param y The y coordinate to read from - note that this uses
	 * 		OpenGL's default coordinate system which is
	 * 		the opposite of ours in the y direction.
	 * @param width The width to read
	 * @param height The height to read
	 * @param format The format which to store in the vector, 
	 * 		e.g. "RGB", "RGBA"
	 * @param type The type to read to, e.g. "FLOAT" or "UNSIGNED_BYTE"
	 */
	public void readFromBuffer(RenderingSurface win,
	    String buffer, 
	    int x, int y, int width, int height, 
	    String format, String type) {
	    impl_ByteVector_readFromBuffer(getId(), win.getId(),
		buffer, x, y, width, height, format, type);
	}

	/** 
	 * This is a bit kludgy...
	 * @param x The x coordinate to set raster position to.
	 * @param y The y coordinate to set raster position to.
	 */
	public void drawPixels(RenderingSurface win,
		int x, int y, float z, int width, int height,
		String format, String type) {
	    impl_ByteVector_drawPixels(getId(), win.getId(),
		    x, y, z, width, height, format, type);
	}

	/** Get the values from the vector as an array of bytes.
	 * Not sure if this is good since bytes are signed in Java.
	 * XXX Same for shorts?
	 */
	public byte[] get() {
	    return impl_ByteVector_get(getId());
	}

	/** Set the values from a byte array.
	 */
	public void set(byte[] bytes) {
	    impl_ByteVector_set(getId(), bytes);
	}

	/** Get the values from the vector as an array of ints.
	 * Each integer is formed by packing 4 bytes.
	 */
	public int[] getInts() {
	    return impl_ByteVector_getInts(getId());
	}

	/** Shade the given Texture into this bytevector, using 32-bit floats.
	 * Sets the bytevector length to w*h*(d &gt; 0?d : 1) * comps * 4
	 */
	public int shade(int w, int h, int d, int comps, 
		String shaderName, String[] params) {
	    return impl_ByteVector_shade(getId(), w, h, d, comps, shaderName, params);
	}


    }
    /** Create a new bytevector of size 0.
     */
    static public ByteVector createByteVector() {
	return createByteVector(0);
    }
    /** Create a new bytevector of the given size.
     */
    static public ByteVector createByteVector(int size) {
	return new ByteVector(createByteVectorImpl(size));
    }
    static private native int createByteVectorImpl(int size);
    static private native void deleteByteVector(int id);

    static private native void impl_ByteVector_readFromBuffer(
	    int id, int winid, String buffer,
	    int x, int y, int width, int height, 
	    String format, String type) ;
    static private native void impl_ByteVector_drawPixels(
	    int id, int winid, 
	    int x, int y, float z, int width, int height, 
	    String format, String type) ;
    static private native int impl_ByteVector_get(int id, int ind);
    static private native int impl_ByteVector_set(int id, byte[] bytes);
    static private native byte[] impl_ByteVector_get(int id);
    static private native int[] impl_ByteVector_getInts(int id);

    static private native int impl_ByteVector_shade(int id, int w, int h, int d, 
		    int comps, String shaderName, String[] params);

// -------------------- TexAccum
    /** An object that can collect information about which sizes
     * a texture was rendered at.
     */
    static public class TexAccum extends NonRenderableJavaObject {
	private TexAccum(int id) {
	    super(id);
	    if(GL.dbg) pa("Create texaccum "+id+"\n");
	}
	protected void deleteObj() { 
	    if(GL.dbg) pa("Delete texaccum "+getId()+"\n");
	    impl_TexAccum_delete(getId()); 
	}
	public void clear() {
	    if(GL.dbg) pa("Clear texaccum "+getId()+"\n");
	    impl_TexAccum_clear(getId());
	}
	public double get(int mip) {
	    return impl_TexAccum_get(getId(), mip);
	}
    }
    static public TexAccum createTexAccum() {
	return new TexAccum(impl_TexAccum_create());
    }
    static public TexAccum createTexAccum(StatsCallback cb, Object clos) {
	return new TexAccum(impl_TexAccum_create_cb(
		    new WeakStatsCaller(cb, clos)
		    ));
    }
    static private native int impl_TexAccum_create();
    static private native int impl_TexAccum_create_cb(Object obj);
    static private native void impl_TexAccum_delete(int id);
    static private native double impl_TexAccum_get(int id, int mip);
    static private native void impl_TexAccum_clear(int id);

    static public native void callQueuedStatistics();
    static public native void clearQueuedStatistics();

// ---------------------  WeakStatsCaller
    /** The class that handles StatsCallbacks using weak references.
     */
    static protected class WeakStatsCaller {
	java.lang.ref.WeakReference obj;
	java.lang.ref.WeakReference clos;
	public WeakStatsCaller(StatsCallback obj, Object clos) {
	    if(dbg) pa("Create weakstatscaller: "+obj+" "+clos);
	    this.obj = new java.lang.ref.WeakReference(obj);
	    this.clos = (clos == null ? null : new java.lang.ref.WeakReference(clos));
	}
	public void call() {
	    StatsCallback cb = (StatsCallback) obj.get();
	    if(dbg) pa("weakstatscaller: cb "+obj.get()+" "+clos+" "+(
			    clos != null ? clos.get() : ""));
	    if(cb == null) return;
	    cb.call((clos == null ? null : clos.get()));
	}
    }

// ---------------------- StatsCallback
    /** An object to be called when statistics have been 
     * added to a statistics-collecting class, e.g.TexAccum.
     */
    public interface StatsCallback {
	void call(Object clos);
    }


//----------Misc

    /** CallGL the given string.
     * Useful for throwaway things such as setting texture parameters.
     */
    public static native void call(String s) ;

    /** CallGL the given string in the given window.
     */
    public static void call(String s, RenderingSurface w) {
	call(s, w.getId());
    }
    private static native void call(String s, int id) ;

    public static void render(
		RenderingSurface win, 
		int ninds, int[] inds1, float[] pts1,
		    int[] interpinds, 
		    int[] inds2, float[] pts2, 
		    int[] codes,
		    float fract, boolean standardcoords,
		boolean showFinal) {
	renderImpl(win.getId(), ninds, inds1, pts1, interpinds, inds2, pts2, codes,
			    fract, standardcoords, showFinal);
    }

    /** Part of the GL state-retaining test mechanism - see TestStateRetainSetup and
     * TestStateRetainTest.
     */
    public static native String getTestStateRetainCorrect();
    private static native void renderImpl(
		int window, 
		int ninds, int[] inds1, float[] pts1, 
		    int[] interpinds, 
		    int[] inds2, float[] pts2, 
		    int[] codes,
		    float fract, boolean standardcoords,
		boolean showFinal);

    public static double timeRender(
		    RenderingSurface win, int iters,
		    int ninds, int[] inds1, float[] pts1, 
		    int[] codes,
		    boolean standardcoords, boolean swapbuf) {
	return timeRenderImpl(win.getId(), iters,
		    ninds, inds1, pts1, codes, standardcoords, swapbuf);
    }           
    private static native float timeRenderImpl(
		    int window, int iters,
		    int ninds, int[] inds1, float[] pts1, 
		    int[] codes,
		    boolean standardcoords, boolean swapbuf);

    public static native boolean transform(int ninds, int[] inds, float[] pts,
		    int coordsys, boolean inverse, float[] points, float[]into);

    public static native boolean transform2(int ninds, int[] inds, float[] pts,
			int[] interpinds, int[] inds2, float[] pts2, float fract,
			boolean show1,
		    int coordsys, boolean inverse, float[] points, float[]into);

    public static native boolean transformSq(int ninds, int[] inds,
	    float[] pts, int coordsys, float[]into);

    /** Get all the activated coordinate systems in whose unit squares the
     * given point falls..
     * @param parent (currently ignored, may be used to restrict in future)
     */
    public static native int[] getAllCSAt(int ninds, int[] inds, 
					  float[] pts, int parent, 
					  float x, float y);

    /** Get all the activated coordinate systems in child vob scene 
     *  in whose unit squares the given point falls..
     */
    public static native int[] getAllChildCSAt(int ninds, int[] inds,
					       int[] activeInts, 
					       int[] childInts, 
					       float[] pts,
					       float x, float y);



    /** Process native events.
     * @param wait If false, this function will return once there are no more
     * 		native events to process. If true, this function will wait
     * 		for the next native event.
     * @return Whether something happened
     */
    public static native boolean eventLoop(boolean wait);
    /** Interrupt the event loop. 
     * Unlike most functions in GL, this can be called from other threads
     * to interrup a waiting event loop at any time.
     * This is useful as a response e.g. for incoming network data.
     */
    public static native void interruptEventloop();


    public static native void setDebugVar(String name, int value);
    public static native int getDebugVar(String name);
    public static native String[] getDebugVarNames();



    /** Get a string describing the current OpenGL context.
     * See the manpage of glGetString(3)
     * @param name The parameter to query, e.g. "VENDOR", "VERSION"
     */
    public static native String getGLString(String name);

    /** Get float(s) describing the current OpenGL context.
     * See the manpage of glGetFloatv(3)
     * Note: this uses the default window.
     * @param name The parameter to query, e.g. "ALPHA_BITS"
     */
    public static float[] getGLFloat(String name) {
	return implgetGLFloat(-1, name);
    }


    /** Get float(s) describing the current OpenGL context.
     * See the manpage of glGetFloatv(3)
     * @param name The parameter to query, e.g. "ALPHA_BITS"
     */
    public static float[] getGLFloat(RenderingSurface s, String name) {
	return implgetGLFloat(s.getId(), name);
    }

    private static native float[] implgetGLFloat(int rsid, String name);

    /** Get the given program parameter.
     * @param target The name of the program target, e.g. "VERTEX_PROGRAM_ARB"
     * @param name The name of the parameter to return, e.g. "MAX_PROGRAM_INSTRUCTIONS_ARB"
     */
    public static native float[] getGLProgram(String target, String name);

    public static native int getGLProgrami(String target, int progId,
				String name);

    /** Get float(s) describing the current state of the given 
     * OpenGL texture.
     * See the manpage of glGetTexParameterfv(3)
     * @param target The texture target to bind the given
     * 		texture to for querying, e.g. "TEXTURE_2D"
     * @param tex The OpenGL texture id
     * @param name The parameter to query, e.g. "TEXTURE_RESIDENT"
     */
    public static native float[] getGLTexParameterFloat(String target, int tex, String name);
    /** Get float(s) describing the current state of the given 
     * level of the given OpenGL texture.
     * See the manpage of glGetTexParameterfv(3)
     * @param target The texture target to bind the given
     * 		texture to for querying, e.g. "TEXTURE_2D"
     * @param tex The OpenGL texture id
     * @param level The mipmap level to query
     * @param name The parameter to query, e.g. "TEXTURE_INTERNAL_FORMAT"
     */
    public static native float[] getGLTexLevelParameterFloat(String target, int tex, int level, String name);

    /** The extensions as strings.
     */
    private static Set extensions = null;

    /** Return a boolean showing whether the given extension is supported
     * by the current OpenGL environment.
     * XXX SHould be With / without GL_!!!
     */
    public static boolean hasExtension(String name) {
	if(extensions == null) {
	    extensions = new HashSet();
	    String s = instance.iGetGLString("EXTENSIONS");
	    StringTokenizer st = new StringTokenizer(s);
	    while (st.hasMoreTokens()) 
		extensions.add(st.nextToken());
	}
	return extensions.contains(name);
    }

    /** Get the OpenGL token string corresponding to the given integer value.
     * This is useful for producing human-readable output 
     * from the result of getGLFloats.
     * For example,
     * <pre>
	GL.getGLTokenString(
	    (int)(GL.getGLTexLevelParameterFloat(
			"TEXTURE_2D", id, 0, "TEXTURE_INTERNAL_FORMAT")[0]
		)
	)
     * </pre>
     * yields the string representation for the internal format 
     * of the given texture id.
     */
    public static native String getGLTokenString(int value);

    /** If known, return bits per texel of a given format.
     */
    public static int bitsPerTexel(String format) {
	if(format.equals("COMPRESSED_RGB_S3TC_DXT1_EXT")) return 4;
	return -1;
    }

}


