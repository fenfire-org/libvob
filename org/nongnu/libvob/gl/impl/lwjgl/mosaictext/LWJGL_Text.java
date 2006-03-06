// (c): Matti J. Katila

package org.nongnu.libvob.gl.impl.lwjgl.mosaictext;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.StringTokenizer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;
import org.nongnu.libvob.AbstractVob;
import org.nongnu.libvob.GraphicsAPI;
import org.nongnu.libvob.TextStyle;
import org.nongnu.libvob.Vob;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.Vob.RenderInfo;
import org.nongnu.libvob.gl.impl.lwjgl.CallGL;
import org.nongnu.libvob.gl.impl.lwjgl.LWJGLRen;
import org.nongnu.libvob.gl.impl.lwjgl.Transform;
import org.nongnu.libvob.impl.lwjgl.LWJGL_SimpleVobTester;
import org.nongnu.libvob.vobs.RectVob;
import org.nongnu.libvob.vobs.SolidBackdropVob;
import org.nongnu.libvob.vobs.TestSpotVob;
import org.nongnu.libvob.vobs.TextVob;

import com.sixlegs.image.png.PngImage;

/** A class that can load a set of images which together form 
 *  a mosaic texture of glyphs on specific font family. 
 *  There are different size of images according to mipmap levels, e.g., 64x64, 128x128 or 256x256.
 *  
 * http://www.opengl.org/resources/code/rendering/mjktips/TexFont/TexFont.html
 * 
 * @author Matti J. Katila
 */
public class LWJGL_Text {

    FloatBuffer bits = BufferUtils.createFloatBuffer(256*256);

    ImageConsumer imgConsumer = new ImageConsumer(){
	public void imageComplete(int status) {
	}
	public void setHints(int hintflags) {
	}
	public void setDimensions(int width, int height) {
	}
	public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
	}
	public void setPixels(int x, int y, int w, int h, ColorModel m, int[] pixels, int off, int scansize) {
	    for (int i=0; i<w; i++) {
		int p = pixels[i];
		float r = m.getRed(p);
		float g = m.getGreen(p);
		float b = m.getBlue(p);
		
		float fl = 0;
		fl = (r/256f+g/256f+b/256f);
		fl = fl/3;
//		bits.put((w-1-y)*scansize + i, fl);
		bits.put(y*scansize + i, fl);
	    }	    
	}
	public void setColorModel(ColorModel model) {
	}
	public void setProperties(Hashtable props) {
	}
    };
    
    public class TextStyleImpl extends TextStyle {

	/** Reference size of the font */
	float refSize;

	float descent = 0;
	
	// reserve room for two ints.
	IntBuffer textures = BufferUtils.createIntBuffer(2);
	TextStyleImpl(IntBuffer textures, float size, float descent)
	{
	    this.textures = textures;
	    this.refSize = size;
	    this.descent = descent;
	}
	TextStyleImpl(float size) {
	    this.refSize = size;
	    System.out.println("refSize:: "+refSize);
	    GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, textures.capacity());
	    GL11.glGenTextures(textures);
	    for (int i=0; i<textures.capacity(); i++) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(i));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	    }
	}
	
	void setTexture(String size, String descent, int nro, File f) {
	    setTexture(Integer.parseInt(size), Integer.parseInt(descent), nro, f);
	}
	void setTexture(int size, int descent, int nro, File f) {
	    int ind = 0;
	    switch (size) {
	    case 256: {
		ind = 0;
		this.descent = (descent * this.refSize)/(256f/8f);
		break;
	    }
	    case 128: ind = 1; break;
	    case 64: ind = 2; break;
	    default: 
		throw new Error();
	    }
	   
	    try {
		bits = BufferUtils.createFloatBuffer(size*size);
		bits.limit(size*size);
		bits.position(0);

		PngImage png = new PngImage(new FileInputStream(f), true);
		png.setFlushAfterNextProduction(true);
		png.startProduction(imgConsumer);
		png.removeConsumer(imgConsumer);	
		bits.position(0);

		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(nro));
		System.out.println(nro+", "+ind+", "+size);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, ind, GL11.GL_INTENSITY, size, size, 0, 
			GL11.GL_LUMINANCE, GL11.GL_FLOAT ,bits);
		bits.flip();
//		CallGL.checkGlError("after text image");
	    } catch (Exception e) { //FileNotFoundException e) {
		e.printStackTrace();
	    }
	}
	
	public float getScaleByHeight(float h) {
	    return 1; //h/refSize;
	}	
	public TextStyle getScaledStyle(float h) {
	    if (true) return this;
	    if (h == 1) return this;
	    return new TextStyleImpl(textures, h*refSize, descent*h);
	}
	public float getWidth(String s, float scale) {
	    return 100; //refSize*s.length()*scale;
	}
	public float getWidth(char[] chars, int offs, int len, float scale) {
	    return 100; //refSize*scale*(len-offs);
	}
	public float getHeight(float scale) {
	    return 100; //refSize * scale;
	}
	public float getAscent(float scale) {
	    return 10; //(refSize - descent)*scale;
	}
	public float getDescent(float scale) {
	    return 20; //descent * scale;
	}
	public float getLeading(float scale) {
	    // TODO Auto-generated method stub
	    return 0;
	}
	public Vob createVob(String text) {
	    System.out.println("create");
	    return new Text(text);
	}
	class Text extends AbstractVob implements LWJGLRen.Vob1 {

	    protected String str;
	    public Text(String text) { 
		this.str = text != null? text: ""; 
		System.out.println("Text: "+str+", "+refSize);
		System.out.println(getDescent(1));
		System.out.println(getAscent(1));
		System.out.println(getWidth(str, 1));
		System.out.println(getScaleByHeight(8));
	    }
	    public void render(Graphics g, boolean fast, RenderInfo info1, RenderInfo info2) {  }
	    public void render(Transform t, int callList) {
		CallGL.checkGlError("before call list coorder");
		GL11.glPushMatrix();
		if(t.performGL()) {
		    Vector4f glyphCoords = new Vector4f();
		    for (int i=0; i<str.length(); i++) {

			    GL11.glEnable(GL11.GL_TEXTURE_2D);
			    GL11.glAlphaFunc(GL11.GL_GREATER, 0.15f);
			    GL11.glEnable(GL11.GL_ALPHA_TEST);
			    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			    GL11.glEnable(GL11.GL_BLEND);
	
			    int textureIndex = getGlyphSetIndex(str.charAt(i));
			    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(textureIndex));
			    
			    GL11.glBegin(GL11.GL_QUADS);
			    
			    getGlyphCoordsFor(str.charAt(i), textureIndex, glyphCoords);
			    GL11.glTexCoord2f(glyphCoords.x, glyphCoords.y); GL11.glVertex3f(i, 0, 0.0f);
			    GL11.glTexCoord2f(glyphCoords.x, glyphCoords.w); GL11.glVertex3f(i, 1f, 0.0f);
			    GL11.glTexCoord2f(glyphCoords.z, glyphCoords.w); GL11.glVertex3f(i+1f, 1f, 0.0f);
			    GL11.glTexCoord2f(glyphCoords.z, glyphCoords.y); GL11.glVertex3f(i+1f, 0, 0.0f);
			    GL11.glEnd();

		    }
		} else {
		    System.out.println("Error: CallistCoorded with non-glperformable.");
//		    t.dump(std::cout);
		}
		GL11.glPopMatrix();
		CallGL.checkGlError("After coorded calling list "+callList);

	    }
	    public int putGL(VobScene vs, int cs)
	    {
		return cs;
	    }
	}
	public int getGlyphSetIndex(char c) {
	    int ret = 0;
	    if ("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c) >= 0) 
		ret = 0;
	    else if ("?????????????????? !\"@#$%&/{([)]=}?\\+`\'^*<>|;,:._-".indexOf(c) >= 0)
		ret = 1;
	    return ret;
	}
	public void getGlyphCoordsFor(char c, int setInd, Vector4f gc) {
	    gc.x = 0; gc.y=0; gc.z = 1; gc.w = 1;
	    int ind = 0; 
	    switch (setInd) {
	    case 0: {
		String s = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		ind = s.indexOf(c);
		if (ind < 0) return;
		break;
	    }
	    case 1: {
		String s = "?????????????????? !\"@#$%&/{([)]=}?\\+`\'^*<>|;,:._-";
		ind = s.indexOf(c);
		if (ind < 0) return;
		break;
	    }
	    }
	    int row = ind / 8;
	    int col = ind % 8;
	    float d = 1f/8f;
	    gc.x = col*d;
	    gc.y = row*d;
	    gc.z = gc.x + d;
	    gc.w = gc.y + d;
	}
    }
    
    
    
    
    
    static private final Object NOT_LOADED = new Object();
    static private LWJGL_Text instance = null;
    
    private File dir = null;
    
    /** Keys to TextStyles which contais int to texture bind. 
     */
    private HashMap loadedFonts = new HashMap();
    
    private LWJGL_Text() {}
    static public LWJGL_Text getInstance() { 
	if (instance == null)
	    instance = new LWJGL_Text();
	return instance;
    }

    
    
    
    public void setImageDirectory(File dir) {
	this.dir = dir;
	// get all png images
	File[] images = dir.listFiles(new FileFilter(){
	    public boolean accept(File pathname) {
		if (pathname.getName().endsWith("png"))
		    return true;
		return false;
	    }
	});

	for (int i = 0; i < images.length; i++) {
	    String name = images[i].getName();
	    name = name.substring(0, name.indexOf('.'));
	    StringTokenizer st = new StringTokenizer(name, "-");

	    String family = null;
	    boolean bold = false, italic = false;
	    
	    while (st.hasMoreTokens()) {
		String token = st.nextToken();
		if (family == null) family = token;
		if (token.equals("bold=1")) bold = true;
		if (token.equals("italic=1")) italic = true;
	    }
	    int style = Font.PLAIN;
	    if (bold) style |= Font.BOLD;
	    if (italic) style |= Font.ITALIC;
//	    System.out.println(family+style);
	    loadedFonts.put(family+style, NOT_LOADED);
	}
    }
    
    
    
    

    // size is ignored
    public synchronized TextStyle create(String family, int style, int size) {
	if (family == null) 
	    family = "sans";
	
	String key = family+style;

	if (loadedFonts.containsKey(key)) {
	    Object textStyle = loadedFonts.get(key);
	    if (textStyle != NOT_LOADED)
		return (TextStyle) textStyle;
	    else {
		TextStyle text = loadStyle(family, style, size);
		loadedFonts.put(key, text);
		return text;
	    }
	}
	return null;
    }

    
    private TextStyle loadStyle(String family, int style, float size_) {
	// get all png images
	File[] images = dir.listFiles(new FileFilter(){
	    public boolean accept(File pathname) {
		if (pathname.getName().endsWith("png"))
		    return true;
		return false;
	    }
	});

	TextStyleImpl ret = new TextStyleImpl(size_);
	for (int i = 0; i < images.length; i++) {
	    String name = images[i].getName();
	    name = name.substring(0, name.indexOf('.'));
	    if (!name.substring(0, name.indexOf('-')).equals(family)) continue;
	    
	    StringTokenizer st = new StringTokenizer(name, "-");
	    String desc = null, size = null;
	    int nro = 0;
	    boolean bold = false, italic = false;
	    while (st.hasMoreTokens()) {
		String token = st.nextToken();
		if (token.equals("nro=1")) nro = 1;
		if (token.equals("bold=1")) bold = true;
		if (token.equals("italic=1")) italic = true;
		if (token.startsWith("descent=")) desc = token;
		if (token.startsWith("size=")) size = token;
	    }
	    int style_ = Font.PLAIN;
	    if (bold) style_ |= Font.BOLD;
	    if (italic) style_ |= Font.ITALIC;
	    if (style != style_) continue;

	    ret.setTexture(size.substring(size.lastIndexOf('x')+1), 
		    desc.substring(desc.lastIndexOf('=')+1), nro, images[i]);
	}
	return ret;
    }


    public static void _main(String[] argv) {
    
	try {
	    Display.setDisplayMode(new DisplayMode(500, 500));
	    Display.setFullscreen(false);
	    Display.create();
	    Display.setTitle("Test text image textures.");
	    Dimension d = new Dimension(500,500);

	    GL11.glViewport(0, 0, d.width, d.height);
	    GL11.glMatrixMode(GL11.GL_PROJECTION);
	    GL11.glLoadIdentity();
	    GLU.gluPerspective(60.0f, d.width/d.height, 1.0f, 30.0f);
	    GL11.glMatrixMode(GL11.GL_MODELVIEW);
	    GL11.glLoadIdentity();
	    GL11.glTranslatef(0,0, -3.6f);

	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	    GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);


	    File f = new File("./tmp/");
	    LWJGL_Text.getInstance().setImageDirectory(f);
	    TextStyleImpl impl = (TextStyleImpl)LWJGL_Text.getInstance().create("sans", Font.BOLD, 12);

	    GL11.glClearColor(1, 1, 1, 1);
	    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	    GL11.glColor4f(0, 0, 0, 1);


	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	    GL11.glAlphaFunc(GL11.GL_GREATER, 0.15f);
	    GL11.glEnable(GL11.GL_ALPHA_TEST);
	    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	    GL11.glEnable(GL11.GL_BLEND);

	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, impl.textures.get(0));
	    
	    GL11.glBegin(GL11.GL_QUADS);
	    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(-2.0f, -1.0f, 0.0f);
	    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(-2.0f, 1.20f, 0.0f);
	    GL11.glTexCoord2f(1, 1); GL11.glVertex3f(0.0f, 1.20f, 0.0f);
	    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(0.0f, -1.0f, 0.0f);
	    GL11.glEnd();

	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, impl.textures.get(1));
	    
	    GL11.glBegin(GL11.GL_QUADS);
	    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(0.1f, -1.0f, 0.0f);
	    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(0.1f, 1.20f, 0.0f);
	    GL11.glTexCoord2f(1, 1); GL11.glVertex3f(2.0f, 1.20f, 0.0f);
	    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(2.0f, -1.0f, 0.0f);
	    GL11.glEnd();
	    CallGL.checkGlError("end");

	    Display.update();
	    
	    Thread.sleep(12800);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    
    }
    
    
    
    public static void main(String[] args) {
	if (false) {
	    _main(args);
	    return;
	}
	
	LWJGL_SimpleVobTester test = new LWJGL_SimpleVobTester() {
	    protected VobScene gen(VobScene vs) {
		vs = super.gen(vs);
		vs.put(new SolidBackdropVob(new Color(.9f, .8f, .5f)));
		
	        int cs = 0;
	        cs = vs.translateCS(cs, "move", 10,50);

//	        vs.put(new TestSpotVob(0,0,0, Color.MAGENTA), cs);
	        cs = vs.scaleCS(cs, "scale for text", 9,9);

//	        vs.put(new TestSpotVob(0,0,0, Color.WHITE), cs);
//	        vs.put(new RectVob(Color.MAGENTA,2), cs);
	        vs.put(new TextVob(GraphicsAPI.getInstance().getTextStyle("sans", 0,12), "0123456789"), cs);
	        cs = vs.translateCS(cs, "move2", 0,1);
	        vs.put(new TextVob(GraphicsAPI.getInstance().getTextStyle("sans", 0,12), "abcdefghijklmnopqrstuvxyz"), cs);
	        cs = vs.translateCS(cs, "move2", 0,1);
	        vs.put(new TextVob(GraphicsAPI.getInstance().getTextStyle("sans", 0,12), "abcdefghijklmnopqrstuvxyz".toUpperCase()), cs);
	        cs = vs.translateCS(cs, "move2", 0,1);
	        vs.put(new TextVob(GraphicsAPI.getInstance().getTextStyle("sans", 0,12), "??????L<>-:;,.-?=)"), cs);
	        cs = vs.translateCS(cs, "move2", 0,1);
	        vs.put(new TextVob(GraphicsAPI.getInstance().getTextStyle("sans", 0,12), "(/\\}{][@?$!\"#?%&"), cs);
	        		        
	        return vs;
	    }
	};
	try {
	    LWJGL_SimpleVobTester.main(test);
	    Thread.sleep(15000);
	    System.exit(0);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}    
   