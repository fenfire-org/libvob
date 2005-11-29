
package org.nongnu.libvob.impl.terminal;

import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.*;


public class Text implements Runnable {

    protected GraphicsAPI.Window window;

    public void start() {
	GraphicsAPI.getInstance().startUpdateManager(this);
    }

    public void run(Binder binder, Shower shower) {
	String geometry = java.lang.System.getProperty("vob.windowsize", 
						       "1024x768");
	int x = geometry.indexOf('x');
	int width = Integer.parseInt(geometry.substring(0, x)),
	    height = Integer.parseInt(geometry.substring(x+1));

	window = GraphicsAPI.getInstance().createWindow();
    }

    public void run() {
	try {
	    while (true) {
		System.out.print(window+".");
		Thread.sleep(1000);
	    }
	} catch (Exception e) { 
	    e.printStackTrace(); 
	}
    }

    static public void main(String[] argv) {
	new Text().start();
    }

}
