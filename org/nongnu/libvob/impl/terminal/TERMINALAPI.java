// (c): Matti J. Katila

package org.nongnu.libvob.impl.terminal;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.awt.*;
import org.nongnu.libvob.util.*;
import jline.*;

public class TERMINALAPI extends GraphicsAPI {

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
	return new RawTextStyle(new ScalableFont(family, style, size), null);
    }
}
