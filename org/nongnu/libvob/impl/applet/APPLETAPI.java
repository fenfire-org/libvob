/*
APPLETAPI.java
 *    
 *    Copyright (c) 2004 Matti J. Katila
 *
 *    This file is part of Libvob.
 *    
 *    Libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Matti J. Katila
 */

package org.nongnu.libvob.impl.applet;
import org.nongnu.libvob.impl.awt.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.applet.*;


/** Java.awt.applet.Applet implementation of GraphicsAPI.
 *  Of course it's common 
 */
public class APPLETAPI extends AWTAPI {
    
    public Applet applet = null;
    
    static class AppletScreen extends AWTScreen {
	public AppletScreen(GraphicsAPI api, Component c) {
	    super(api, c);
	}

    }



    public Window createWindow() {
	if (applet == null) 
	    throw new Error("No applet set.");
	return new AppletScreen(this, applet);
    }
    public RenderingSurface createStableOffscreen(int w, int h) {
	throw new Error("not possible..");
	// XXX
	/*
	FrameScreen fs = new FrameScreen(this);
	fs.setLocation(0, 0, w, h);
	return fs;
	*/
    }
}



