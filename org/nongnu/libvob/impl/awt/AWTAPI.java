/*
AWTAPI.java
 *    
 *    Copyright (c) 2001-2002, Ted Nelson and Tuomas Lukka
 *
 *    This file is part of Gzz.
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
 * Written by Tuomas Lukka
 */

package org.nongnu.libvob.impl.awt;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import java.util.*;
import java.io.*;
import java.awt.*;


/** Java.awt.Graphics implementation of GraphicsAPI
 */
public class AWTAPI extends GraphicsAPI {
    public void startUpdateManager(Runnable r) {
	JUpdateManager.startJUpdateManager(r);
    }
    public Window createWindow() {
	return new FrameScreen(this);
    }
    public RenderingSurface createStableOffscreen(int w, int h) {
	// XXX
	FrameScreen fs = new FrameScreen(this);
	fs.setLocation(0, 0, w, h);
	return fs;
    }
    public TextStyle getTextStyle(String family, int style, int size) {
	return new RawTextStyle(new ScalableFont(family, style, size), null);
    }
}



