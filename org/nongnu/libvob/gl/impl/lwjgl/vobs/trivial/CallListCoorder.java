package org.nongnu.libvob.gl.impl.lwjgl.vobs.trivial;

import java.awt.Graphics;

import org.lwjgl.opengl.GL11;
import org.nongnu.libvob.AbstractVob;
import org.nongnu.libvob.Vob;
import org.nongnu.libvob.Vob.RenderInfo;
import org.nongnu.libvob.gl.impl.lwjgl.CallGL;
import org.nongnu.libvob.gl.impl.lwjgl.Transform;
import org.nongnu.libvob.gl.impl.lwjgl.LWJGLRen.Vob1;

public class CallListCoorder extends CallGL implements Vob1 {

    public CallListCoorder(String s) {
	super(s);
    }

    public void render(Transform t, int callList) {
	CallGL.checkGlError("before call list coorder");
	GL11.glPushMatrix();
	if(t.performGL()) {
	    GL11.glCallList(callList);
	} else {
	    System.out.println("Error: CallistCoorded with non-glperformable.");
//	    t.dump(std::cout);
	}
	GL11.glPopMatrix();
	CallGL.checkGlError("After coorded calling list "+callList);

    }
    
}
