/*   
RawTextStyle.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.impl.awt;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import java.awt.*;


/** A style drawing raw text.
 */

public class RawTextStyle extends AWTTextStyle {

    public RawTextStyle(ScalableFont font, Color col) { super(font, col); }

    public TextStyle getScaledStyle(float h) {
	return new RawTextStyle(font.getScaledFont(font.getScale(h)), col);
    }

    public void render(java.awt.Graphics g, int x, int y, 
		       String s,
		       float scale, 
		       Vob.RenderInfo info) {
        Font f = font.getFont(scale);
	if(g.getFont() != f) g.setFont(f);
        g.drawString(s, x, y);
    }

    public void render(java.awt.Graphics g, int x, int y, 
		       char[] chars, int offs, int len,
		       float scale, 
		       Vob.RenderInfo info) {
        Font f = font.getFont(scale);
	if(g.getFont() != f) g.setFont(f);
        g.drawChars(chars, offs, len, x, y);
    }
}


