/*
LineVob.java
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
import java.awt.Graphics;
import org.nongnu.libvob.util.ScalableFont;

/** A vob drawing a string.
 */
public class TextVob extends AbstractVob {
    
    public final String text;
    public final ScalableFont font;
   
    public TextVob(String text, ScalableFont font) {
	this.text = text;
	this.font = font;
    }
    
    public float getSize(Vob.Axis axis) {
	if(axis == X) {
	    return font.getFontMetrics(1).stringWidth(text);
	} else {
	    return font.getFontMetrics(1).getHeight();
	}
    }

    public void render(Graphics g, RenderTraversal t) {
	g.setFont(font.getFont(1));
	g.drawString(text, t.x, t.y + font.getFontMetrics(1).getAscent());
    }
}
