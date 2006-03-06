package org.nongnu.libvob.impl.lwjgl;

import java.io.File;

import org.nongnu.libvob.GraphicsAPI;
import org.nongnu.libvob.TextStyle;
import org.nongnu.libvob.gl.impl.lwjgl.mosaictext.LWJGL_Text;

public class LWJGL_API extends GraphicsAPI {

    public void startUpdateManager(Runnable r) {
	LWJGLUpdateManager.startUpdateManager(r);
	LWJGL_Text.getInstance().setImageDirectory(new File("./tmp/"));
    }

    public Window createWindow() {
	return new LWJGL_Screen(this);
    }

    public RenderingSurface createStableOffscreen(int w, int h) {
	return null;
    }

    public TextStyle getTextStyle(String family, int style, int size) {
	return LWJGL_Text.getInstance().create(family, style, size);
    }

}
