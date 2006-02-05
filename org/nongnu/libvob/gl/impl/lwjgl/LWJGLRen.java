// (c): Matti J. Katila

package org.nongnu.libvob.gl.impl.lwjgl;

import java.awt.Graphics;

import org.nongnu.libvob.AbstractVob;
import org.nongnu.libvob.Vob;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.Vob.RenderInfo;

public class LWJGLRen {

    static AbstractVob stubVob1 = new AbstractVob(){
	public void render(Graphics g, boolean fast, RenderInfo info1, RenderInfo info2) {
	}
	public int putGL(VobScene vs, int cs1) {
	    return 0;
	}
    };
    public static final Object NONE = new Object();
    public static final Object COORDER = new Object();
    public static final Object BOX_COORDER = new Object();
    
    public static Vob createNonFilledRectangle(float lineWidth, float f, float g, float h, int i, float j, float k, float l, int m) {
	return stubVob1;
    }

    // putGL(vs);
    public static Vob createCallList(String s, Object k) {
	CallGL callGl = new CallGL(s);
	return callGl;
    }

    
    
}
