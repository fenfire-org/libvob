/*   
Main.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
 *
 *    This file is part of Fenfire.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.impl;
import org.nongnu.libvob.*;

public abstract class Main implements Runnable {
    protected WindowAnimation windowAnim;
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
	window.setLocation(0,0,width,height);
	

	// static set
	if (GraphicsAPI.getInstance() instanceof org.nongnu.libvob.impl.awt.AWTAPI) {
	    if (GraphicsAPI.getInstance() instanceof org.nongnu.libvob.impl.applet.APPLETAPI) {
		;//org.nongnu.libvob.layout.unit.Image.setComponent(applet);
	    } else {
		org.nongnu.libvob.layout.unit.Image.setComponent(
		    ((org.nongnu.libvob.impl.awt.FrameScreen)window).getFrame());
	    }
	} else if (GraphicsAPI.getInstance() instanceof org.nongnu.libvob.impl.gl.GLAPI) {
	    //org.fenfire.spanimages.gl.PoolManager.getInstance(
	    //	).setBackgroundProcessUpdate(windowAnim);
	}

	windowAnim = new WindowAnimationImpl(window, binder, shower, false);


	AbstractUpdateManager.addWindow((Screen)windowAnim);
	AbstractUpdateManager.chg();
    }
}
