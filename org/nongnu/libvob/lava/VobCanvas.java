/*
VobCanvas.java
 *    
 *    Copyright (c) 2003, Benja Fallenstein
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lava;
import java.awt.*;

/** An AWT component showing a vob.
 */
public class VobCanvas extends Canvas {

    protected Vob vob;

    public VobCanvas() {}

    public VobCanvas(Vob vob) {
	setVob(vob);
    }

    public VobCanvas(Vob vob, Color background) {
	setVob(vob);
	setBackground(background);
    }

    public void setVob(Vob vob) {
	this.vob = vob;
	repaint();
    }

    public void paint(Graphics g) {
	if(vob != null)
	    vob.render(g, new RenderTraversal());
    }
}
