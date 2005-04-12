/*
ImageVob.java
 *    
 *    Copyright (c) 2004-2005, Matti J. Katila and Benja Fallenstein
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
 */
/*
 * Written by Matti J. Katila and Benja Fallenstein
 */

package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import java.awt.*;
import java.io.*;

public class ImageVob extends AbstractVob {
    protected float width, height;
    protected java.awt.Image img;
    protected int id;

    public float getWidth() { return width; }
    public float getHeight() { return height; }

    static protected int ID = 0;
    static protected MediaTracker tracker;
    static public void setComponent(Component c) {
	tracker = new MediaTracker(c);
    }

    /** NOTE: this implementation do not work 
     *  with OpenGL API!!!
     */
    public ImageVob(File f) throws IOException {
	this((InputStream)new FileInputStream(f));
    }

    public ImageVob(InputStream in) throws IOException {
	com.sixlegs.image.png.PngImage png = 
		 new com.sixlegs.image.png.PngImage(in, true);
	width = (float) png.getWidth();
	height = (float) png.getHeight();

	//System.out.println("Image: "+png+", w: "+width+", h: "+height);
	img = Toolkit.getDefaultToolkit().createImage(png);
	id = ID;
	tracker.addImage(img, ID++);
	png = null;
    }

    public void render(Graphics g, boolean fast, 
		       RenderInfo info1, RenderInfo info2) {
	if (width < 0 || height < 0) return;
	int x = (int)info1.x, 
	    y = (int)info1.y;
	int w = (int)(info1.width), 
	    h = (int)(info1.height);
	
	try {
	    tracker.waitForID(id);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	g.drawImage(img, x,y,w,h, null); 
    }
}
