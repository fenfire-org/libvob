package org.nongnu.libvob.impl.lwjgl;

import org.nongnu.libvob.GraphicsAPI;
import org.nongnu.libvob.TextStyle;

public class LWJGL_API extends GraphicsAPI {

    public void startUpdateManager(Runnable r) {
	LWJGLUpdateManager.startUpdateManager(r);
    }

    public Window createWindow() {
	return new LWJGL_Screen(this);
    }

    public RenderingSurface createStableOffscreen(int w, int h) {
	return null;
    }

    public TextStyle getTextStyle(String family, int style, int size) {
	return new TextStyle(){

	    public float getScaleByHeight(float h) {
		return 0;
	    }

	    public TextStyle getScaledStyle(float h) {
		return this;
	    }

	    public float getWidth(String s, float scale) {
		return 0;
	    }

	    public float getWidth(char[] chars, int offs, int len, float scale) {
		return 0;
	    }

	    public float getHeight(float scale) {
		return 0;
	    }

	    public float getAscent(float scale) {
		return 0;
	    }

	    public float getDescent(float scale) {
		return 0;
	    }

	    public float getLeading(float scale) {
		return 0;
	    }
	};
    }

}
