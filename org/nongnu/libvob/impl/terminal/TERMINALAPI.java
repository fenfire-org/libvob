// (c): Matti J. Katila

package org.nongnu.libvob.impl.terminal;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.awt.*;
import org.nongnu.libvob.util.*;


public class TERMINALAPI extends GraphicsAPI {
    static private void p(String s) { System.out.println("TERMAPI:: "+s); }


    public void startUpdateManager(java.lang.Runnable r){
	TerminalUpdateManager.startUpdateManager(r);
    }

    private Window window = null;

    public Window createWindow() {
	if (window != null) 
	    throw new Error("Window in use already.");
	else window = new TerminalWindow(this);
	return window;
    }

    public RenderingSurface createStableOffscreen(int w, int h) {
	return null;
    }

    public TextStyle getTextStyle(java.lang.String family, 
				  int style, int size) {
	//p("STYLE for: "+size);
	return this.style; //new RawTextStyle(new ScalableFont(family, style, size), null);
    }


    TextStyle style = new TextStyle(){

	    /** Get the scale to use to get a font in this style of height h.
	     */
	    public float getScaleByHeight(float h) {
		return 1;
	    }

	    /** Get a copy of this style, scaled so that getHeight(1)
	     *  is (approximately) h.
	     */
	    public TextStyle getScaledStyle(float h) { return this; }

	    public float getWidth(String s, float scale) {
		return s.length()*8*scale;
	    }
	    public float getWidth(char[] chars, int offs,
				  int len, float scale){
		return chars.length*8*scale;
	    }

	    public float getHeight(float scale) { return 16*scale;}

	    public float getAscent(float scale) { return 15*scale; }

	    public float getDescent(float scale) { return scale; }

	    public float getLeading(float scale) { return 0; }
	};

}
