package org.nongnu.libvob.gl.impl;

import org.lwjgl.opengl.GL11;
import org.nongnu.libvob.gl.GL;

public class LWJGL_Wrapper implements GL.GLinstance {

    public String iGetGLString(String name) {
	if (name.equalsIgnoreCase("EXTENSIONS"))
	    return GL11.glGetString(GL11.GL_EXTENSIONS);
	else if (name.equalsIgnoreCase("VERSION"))
	    return GL11.glGetString(GL11.GL_VERSION);
	throw new Error("Unknown name: "+name);
    }

    public static String getGLString(String name) {
	return GL11.glGetString(GL11.GL_EXTENSIONS);
    }

}
