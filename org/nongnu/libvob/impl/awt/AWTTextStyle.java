/*   
AWTTextStyle.java
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
 * Written by Benja Fallenstein and Tuomas Lukka
 */
package org.nongnu.libvob.impl.awt;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import java.awt.*;


/** AWT implementation of TextStyle.
 */

public abstract class AWTTextStyle extends TextStyle {

    public ScalableFont font;
    public Color col;

    public AWTTextStyle(ScalableFont font, Color col) {
	this.font = font;
	this.col = col;
    }

    public float getScaleByHeight(float h) {
	return font.getScale(h);
    }

    public float getWidth(String s, float scale) {
	return font.getFontMetrics(scale).stringWidth(s);
    }
    public float getWidth(char[] chars, int offs, int len, float scale) {
	return font.getFontMetrics(scale).charsWidth(chars, offs, len);
    }

    public float getHeight(float scale) {
	return font.getFontMetrics(scale).getHeight();
    }


    public float getAscent(float scale) {
	return font.getFontMetrics(scale).getAscent();
    }

    public float getDescent(float scale) {
	return font.getFontMetrics(scale).getDescent();
    }

    public float getLeading(float scale) {
	return font.getFontMetrics(scale).getLeading();
    }

    /** Render the given string, in the given scale, at the given coordinates. 
     * @param g The graphics context to draw into
     * 		The color should be set to the default foreground color,
     *		already mixed.
     * @param s The string to draw.
     * @param scale The scale to draw at.
     * @param info General parameters about rendering.
     */
    abstract public void render(java.awt.Graphics g,
				int x, int y,
				String s, float scale,
				Vob.RenderInfo info);

    abstract public void render(java.awt.Graphics g,
				int x, int y,
				char[] chars, int offs, int len,
				float scale, 
				Vob.RenderInfo info);

}



