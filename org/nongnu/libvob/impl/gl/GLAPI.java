/*
GLAPI.java
 *    
 *    Copyright (c) 2002, Tuomas Lukka
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
 * Written by Tuomas Lukka
 */

package org.nongnu.libvob.impl.gl;

import java.awt.Dimension;

import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;


/** Java.awt.Graphics implementation of GraphicsAPI
 */
public class GLAPI extends GraphicsAPI {

    public void startUpdateManager(final Runnable r) {
	GLUpdateManager.startGLUpdateManager(
		new Runnable() {
		    public void run() {
			GL.init();
			r.run();
		    }
		});
    }
    public RenderingSurface createStableOffscreen(int w, int h) {
	return new GLRenderingSurface(this, 0, 0, w, h);
    }
    public Window createWindow() {
	return new GLScreen(this);
    }
    public TextStyle getTextStyle(String family, int style, int size) {
	return GLTextStyle.create(family, style, size);
    }

}




