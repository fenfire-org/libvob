/**
 * 
 */
package org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial;

import java.awt.Graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.nongnu.libvob.AbstractVob;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.Vob.RenderInfo;
import org.nongnu.libvob.gl.impl.lwjgl.CallGL;
import org.nongnu.libvob.gl.impl.lwjgl.LWJGLRen;
import org.nongnu.libvob.gl.impl.lwjgl.Transform;

public class NonFilledRectangleVob extends AbstractVob implements LWJGLRen.Vob1 {

    private final float lineWidth, r1,g1,b1,a1, r2,g2,b2,a2;
    
    public NonFilledRectangleVob(float lineWidth, float r1, float g1,
	    float b1, float a1, float r2, float g2, float b2,
	    float a2) {
	super();
	this.a2 = a2;
	this.g2 = g2;
	this.g1 = g1;
	this.r2 = r2;
	this.a1 = a1;
	this.r1 = r1;
	this.lineWidth = lineWidth;
	this.b2 = b2;
	this.b1 = b1;
    }

    public int putGL(VobScene vs, int cs1) {
	// it's not clear what should be returned in here!
	// if 0 is returned, this vob is skipped.
	return cs1;
    }
    
    public void render(Transform t, int callList) {
	CallGL.checkGlError("Before NonFilledRect");
	GL11.glPushMatrix();
	if (t.performGL()) {
	    GL11.glPushAttrib(GL11.GL_CURRENT_BIT
		    | GL11.GL_ENABLE_BIT);
	    GL11.glDisable(GL11.GL_TEXTURE_2D);

	    Vector2f box = t.getSqSize();

	    GL11.glColor4d(r1, g1, b1, a1);

	    // top strip
	    GL11.glBegin(GL11.GL_QUAD_STRIP);
	    GL11.glVertex2f(0, 0);
	    GL11.glVertex2f(lineWidth, lineWidth);
	    GL11.glVertex2f(box.x, 0);
	    GL11.glVertex2f(box.x - lineWidth, lineWidth);
	    GL11.glEnd();

	    // left strip
	    GL11.glBegin(GL11.GL_QUAD_STRIP);
	    GL11.glVertex2f(0, 0);
	    GL11.glVertex2f(0, box.y);
	    GL11.glVertex2f(lineWidth, lineWidth);
	    GL11.glVertex2f(lineWidth, box.y - lineWidth);
	    GL11.glEnd();

	    GL11.glColor4d(r2, g2, b2, a2);

	    // right strip
	    GL11.glBegin(GL11.GL_QUAD_STRIP);
	    GL11.glVertex2f(box.x - lineWidth, lineWidth);
	    GL11.glVertex2f(box.x - lineWidth, box.y - lineWidth);
	    GL11.glVertex2f(box.x, 0);
	    GL11.glVertex2f(box.x, box.y);
	    GL11.glEnd();

	    // bottom strip
	    GL11.glBegin(GL11.GL_QUAD_STRIP);
	    GL11.glVertex2f(lineWidth, box.y - lineWidth);
	    GL11.glVertex2f(0, box.y);
	    GL11.glVertex2f(box.x - lineWidth, box.y - lineWidth);
	    GL11.glVertex2f(box.x, box.y);
	    GL11.glEnd();

	    GL11.glPopAttrib();
	} else {
	    System.out
		    .println("Error: NonFilledRectangle with non-glperformable.");
	    // t.dump(std::cout);
	}
	GL11.glPopMatrix();
	CallGL.checkGlError("After NonFilledRect");

    }

    public void render(Graphics g, boolean fast, RenderInfo info1, RenderInfo info2) {
    }

}