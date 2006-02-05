package org.nongnu.libvob.gl.impl;

import org.nongnu.libvob.gl.GL;

public class NativeLoopBack implements GL.GLinstance {

    public String iGetGLString(String name) {
	return GL.getGLString(name);
    }
    
    
}
