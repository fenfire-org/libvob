/*
TextVob.java
 *    
 *    Copyright (c) 2001-2002, Ted Nelson and Tuomas Lukka
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
 * Written by Benja Fallenstein and Tuomas Lukka
 */
package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.awt.*;
import org.nongnu.libvob.impl.gl.*;
import org.nongnu.libvob.linebreaking.*;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.gl.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.Collections;

/** A single contiguous text string as a Vob.
 * This vob implements the HBox interface so it is 
 * possible to create a paragraph
 * from these and use a LineBreaker.
 * <p>
 * A note on the relationship between this class and TextStyle:
 * TextStyle can be constructed from GraphicsAPI by giving a size.
 * That size is the virtual point or pixel size. 
 * However, since
 * we are in a flexibly scaling world, that size has no bearing
 * on how tall the text in a TextVob will appear: only the height of the coordinate
 * system into which the textvob is placed affects that.
 * <p>
 * To get text the right way, give an isotropic coordinate system, i.e. one that
 * keeps squares squares.
 * XXX Diagram!
 */
public class TextVob extends HBox.VobHBox {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    public final TextStyle style;
    // was protected but [mudyc] sees no need for this.
    public final String text;
    protected final boolean baselined;
    protected Color color = null;
    protected String colorString = null;

    protected Object key;


    /** Create a new TextVob.
     * @param style The textstyle to use.
     * @param text The text that the Vob should show
     * @param baselined If true, the baseline of the text will be at 
     * 			the bottom of the unit square of the coordinate system.
     * 			If false, the text will be comfortably within the box.
     */
    public TextVob(TextStyle style, String text, boolean baselined) {
	this(style, text, baselined, (Color)null);
    }
    
    /** Same as another constructor but in GL side you can give the color 
     * with string like "Color 1 0.5 0.5"
     * @param color OpenGL Color used to draw the text.
     */
    public TextVob(TextStyle style, String text, boolean baselined, Color color) {
	super();
	this.style = style;
	this.baselined = baselined;
	this.text = (text != null ? text: "");
	this.color = color;
    }
    
    
    /** Create a non-baselined text vob.
     */
    public TextVob(TextStyle style, String text) {
	this(style, text, false);
    }

    public TextVob(TextStyle style, String text, boolean baselined, Object key) {
        this(style, text, baselined, (Color)null);
	this.key = key;
    }
    public TextVob(TextStyle style, String text, boolean baselined, Object key, Color color) {
        this(style, text, baselined, color);
	this.color = color;
	this.key = key;
    }

    public TextStyle getTextStyle() { return style; }

    public String getText() { return text; }

    public Object getKey() {
        return key;
    }
    public float getScale() { return scale; }


    //////////////////////////////
    // IMPLEMENTATION OF Vob

    public void render(java.awt.Graphics g,
		       boolean fast,
	               Vob.RenderInfo info1,
		       Vob.RenderInfo info2) {
	// XXX Needs adjusting to baselined!
	if(color != null)
	    g.setColor(info1.fade(color));

        float x = info1.x, y = info1.y;
        float w = info1.width, h = info1.height;

	float scale = style.getScaleByHeight(h);
        if(dbg) pa("Render @ scale " + scale + ": '"+text+"' "+getWidth(scale)+" -- pos: "+x+" "+y+" "+w+" "+h);

	float fasc = style.getAscent(scale);
	float fdsc = style.getDescent(scale);
	float fh = fasc + fdsc;
	float ty = y + h/2 + fasc/2;
        
        if(dbg) g.drawRect((int)x, (int)y, (int)getWidth(scale), (int)h);

	((AWTTextStyle)style).render(g, (int)x, (int)ty, text, scale, null);
    }

    static private Vob start, stop;
    private Vob setColor;

    static public Vob getStartCode() {
	if(start == null) {
	    if (GL.hasExtension("GL_NV_register_combiners")) {
		start = GLCache.getCallList(
"   PushAttrib ENABLE_BIT TEXTURE_BIT CURRENT_BIT  \n"+
"   Enable TEXTURE_2D			\n" +
"   Enable REGISTER_COMBINERS_NV			\n" +
"   Enable BLEND			\n" +
"   CombinerParameterNV NUM_GENERAL_COMBINERS_NV 1	\n" +
"   CombinerParameterNV CONSTANT_COLOR0_NV 0 0 0 0.4	\n" +
"   CombinerInputNV COMBINER0_NV ALPHA VARIABLE_A_NV TEXTURE0 UNSIGNED_IDENTITY_NV ALPHA  \n" +
"   CombinerInputNV COMBINER0_NV ALPHA VARIABLE_B_NV CONSTANT_COLOR0_NV UNSIGNED_IDENTITY_NV ALPHA	\n" +
"   CombinerOutputNV COMBINER0_NV ALPHA SPARE0_NV DISCARD_NV DISCARD_NV SCALE_BY_FOUR_NV  NONE FALSE FALSE FALSE			\n" +
"    \n" +
"    FinalCombinerInputNV VARIABLE_A_NV ZERO UNSIGNED_IDENTITY_NV RGB			\n" +
"    FinalCombinerInputNV VARIABLE_B_NV ZERO UNSIGNED_IDENTITY_NV RGB			\n" +
"    FinalCombinerInputNV VARIABLE_C_NV ZERO UNSIGNED_IDENTITY_NV RGB			\n" +
"    FinalCombinerInputNV VARIABLE_D_NV PRIMARY_COLOR_NV UNSIGNED_IDENTITY_NV RGB			\n" +
"			\n" +
"    FinalCombinerInputNV VARIABLE_G_NV SPARE0_NV UNSIGNED_IDENTITY_NV ALPHA			\n" +
// "    Disable TEXTURE_2D\nColor 0 0 0\nDisable BLEND\nDisable ALPHA_TEST\n"+
//"    Color 0 0 0\nEnable BLEND\nDisable ALPHA_TEST\n"+
"");
	    } else {
		start = GLCache.getCallList(
"   PushAttrib ENABLE_BIT TEXTURE_BIT");	       
	    }
	}
	return start;
    }
    static public Vob getStopCode() {
	if(stop == null) {
	    stop = GLCache.getCallList("PopAttrib");
	}
	return stop;
    } 

    public Vob getPlainRenderableForBenchmarking() {
	if(ht == null) {
	    GLTextStyle gls = (GLTextStyle)style;
	    ht = GLRen.createText1(
		    gls.getQuadFont(),
		    text, 
		    (baselined ? 1 : gls.getGLFont().getYOffs()),
		    0);
	}
	return ht;
    }

    private Vob setColor() {
	if(setColor == null) {
	    if(color != null && colorString == null)
		this.colorString ="Color "+ ColorUtil.colorGLString(color);
	    setColor = GLCache.getCallList(this.colorString == null ? "" : this.colorString);
	}
	return setColor;
    }

    


    /** The OpenGL renderable for this TextVob.
     */
    private Vob ht;
    public int putGL(VobScene vs, int coordsys1) {
	if(dbg) pa("Addtolistgl text "+text);
	vs.map.put(getStartCode());
	vs.map.put(setColor());
	vs.map.put(getPlainRenderableForBenchmarking(), coordsys1);
	vs.map.put(getStopCode());
	return 0;
    }


    //////////////////////////////
    // HBox implementation
    //
    public float getWidth(float scale) { return style.getWidth(text, scale); }
    public float getHeight(float scale) { return style.getAscent(scale); }
    public float getDepth(float scale) { return style.getDescent(scale); }

    public int getLength() { return text.length(); }
    public float getX(int i, float scale) { 
	return style.getWidth(text.substring(0, i), scale);
    }
    public void setPrev(HBox b) { }
    public void setPosition(int depth, int x, int y, int w, int h) { }

    public String toString() {
	return super.toString() + " '"+text+"' ";
    }

    //////////////////////////////
    // Lob implementation
    //

    /****
    private float myWidth = -1, myHeight = -1;

    private void computeSizes() {
	if(myWidth < 0) {
	    myWidth = style.getWidth(text, 1);
	    myHeight = style.getHeight(1);
	}
    }

    public float getMinSize(Axis axis) {
	computeSizes();
	return (axis==X) ? myWidth : myHeight;
    }
    public float getNatSize(Axis axis) {
	computeSizes();
	return (axis==X) ? myWidth : myHeight;
    }
    public float getMaxSize(Axis axis) {
	computeSizes();
	return (axis==X) ? myWidth : myHeight;
    }

    public void setSize(float requestedWidth, float requestedHeight) { 
    }

    public Object find(Object key) {
	return null;
    }

    public Set getParameters() {
	return Collections.EMPTY_SET;
    }

    public Object replace(java.util.Map map) {
	if(map.get(this) != null) return map.get(this);
	return this;
    }

    public void setParent(Obs obs) {
    }

    public void removeParent() {
    }

    public boolean key(String key) {
	return false;
    }
    
    public boolean mouse(VobMouseEvent e, float x, float y) {
	return false;
    }
    
    public List getFocusableLobs() {
	return Collections.EMPTY_LIST;
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float x, float y, float _w, float _h, boolean visible) {
	float h = style.getHeight(1);
	int cs = scene.coords.ortho(into, 0, x, y, h, h);
	scene.matcher.add(matchingParent, cs, key);
	if(visible)
	    scene.put(this, cs);
    }

    public void setFocusModel(Model m) {
    }

    public void add(Lob l) {
	throw new UnsupportedOperationException("changing TextVob");
    }
    public void add(Lob l, Object key) {
	throw new UnsupportedOperationException("changing TextVob");
    }
    public void add(Lob l, Object key, int index) {
	throw new UnsupportedOperationException("changing TextVob");
    }

    public void clear() {
	throw new UnsupportedOperationException("changing TextVob");
    }
    public Sequence cloneEmpty() {
	throw new UnsupportedOperationException("changing TextVob");
    }

    public int length() {
	return text.length();
    }
    public Lob getLob(int index) {
	if(index == 0 && text.length() == 1) return this;
	throw new UnsupportedOperationException("getting a glyph at a "+
						"particular position in "+
						"a TextVob");
    }

    private char[] chars = null;

    public float getPosition(Axis axis, int index) {
	if(axis == Y) return 0;

	if(chars == null) chars = text.toCharArray();
	return style.getWidth(chars, 0, index, 1);
    }

    public int getLobIndexAt(float x, float y) {
	return style.getOffsetInText(text, 1, x);
    }
    public int getCursorIndexAt(float x, float y) {
	return style.getOffsetInText(text, 1, x);
    }

    public Model positionModel(Axis axis, Model indexModel) {
	return new PositionModel(axis, this, indexModel);
    }

    public boolean isLargerThanItSeems() {
	return false;
    }
    ****/
}

