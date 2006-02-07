package org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.nongnu.libvob.Vob;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.gl.impl.lwjgl.CallGL;
import org.nongnu.libvob.gl.impl.lwjgl.LWJGLRen;
import org.nongnu.libvob.gl.impl.lwjgl.Transform;

public class CallListBoxCoorder extends CallGL implements LWJGLRen.Vob1  {

    public CallListBoxCoorder(String s) {
	super(s);
    }
    
    public void render(Transform t, int callList) {
	CallGL.checkGlError("Start boxcoorder");
	GL11.glPushMatrix();
	if(t.performGL()) {           
	    Vector2f boxwh = t.getSqSize();
	    GL11.glScalef(boxwh.x, boxwh.y, 1.0f);

	    GL11.glCallList(callList);
	} else {
	    System.out.println("Error: CallisBoxtCoorded with non-glperformable.");
//	    t.dump(std::cout);
	}
	GL11.glPopMatrix();
	CallGL.checkGlError("After boxcoorded calling list ");

    }
    
}
