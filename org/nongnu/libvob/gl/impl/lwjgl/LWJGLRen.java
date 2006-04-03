// (c): Matti J. Katila

package org.nongnu.libvob.gl.impl.lwjgl;

import java.awt.Graphics;

import org.lwjgl.opengl.EXTMultiDrawArrays;
import org.nongnu.libvob.AbstractVob;
import org.nongnu.libvob.TextStyle;
import org.nongnu.libvob.Vob;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.gl.PaperMill;
import org.nongnu.libvob.gl.Ren.PaperQuad;
import org.nongnu.libvob.gl.Ren.Text1;
import org.nongnu.libvob.gl.impl.lwjgl.mosaictext.LWJGL_Text;
import org.nongnu.libvob.gl.impl.lwjgl.vobs.paper.PaperQuadVob;
import org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial.CallListBoxCoorder;
import org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial.CallListCoorder;
import org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial.NonFilledRectangleVob;

/** A class that implements objects that can render OpenGL with lwjgl. 
 * 
 * @author Matti J. Katila
 */
public class LWJGLRen {

    static public interface Vob0 {
	public void render(int callList);
    }
    static public interface Vob1 {
	public void render(Transform cs0, int callList);
    }
    static public interface Vob2 {
	public void render(Transform cs0, Transform cs1, int callList);
    }
    static public interface Vob3 {
	public void render(Transform cs0, Transform cs1, Transform cs2, int callList);
    }
    static public interface VobN  {
	public void render(Transform[] ncs, int callList);
    }
    
    
    
    
    static AbstractVob stubVob1 = new AbstractVob(){
	public void render(Graphics g, boolean fast, RenderInfo info1, RenderInfo info2) {
	}
	public int putGL(VobScene vs, int cs1) {
	    return 0;
	}
    };
    
    static class GLVob extends AbstractVob {
	public void render(Graphics g, boolean fast, RenderInfo info1, RenderInfo info2) {
	}
    }
    

    public static Vob createNonFilledRectangle(float lineWidth, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2) {
	return new NonFilledRectangleVob(lineWidth, r1,g1,b1,a1,r2,g2,b2,a2);
    }

    // putGL(vs);
    public static Vob createCallList(String s) {
	return new CallGL(s);
    }

    public static Vob createCallListCoorder(String s) {
	return new CallListCoorder(s);
    }

    public static Vob createCallListBoxCoorder(String s) {
	return new CallListBoxCoorder(s);
    }

    public static Vob createText(TextStyle style, String text) {
	return ((LWJGL_Text.TextStyleImpl)style).createVob(text);
    }

    
    // ---- NON-STATIC METHODS --------------------------
    
    
    public PaperQuad createPaperQuad(PaperMill.Paper paper, float x0, float y0, float x1, float y1, float dicefactor) {
	return new PaperQuadVob(paper, x0,y0,x1,y1, 1f, dicefactor);
    }

    public Text1 createText1(TextStyle p0, String p1, float p2, int p3) {
	return null;
    }

    
    
}
