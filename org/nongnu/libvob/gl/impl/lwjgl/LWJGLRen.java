// (c): Matti J. Katila

package org.nongnu.libvob.gl.impl.lwjgl;

import java.awt.Graphics;

import org.nongnu.libvob.AbstractVob;
import org.nongnu.libvob.Vob;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.Vob.RenderInfo;
import org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial.CallListBoxCoorder;
import org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial.CallListCoorder;
import org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial.NonFilledRectangleVob;

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

    
    
}
