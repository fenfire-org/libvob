/*
DecoratedVob.java
 *
 *    Copyright (c) 2002, Tuomas Lukka
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

package org.nongnu.libvob.impl;
import org.nongnu.libvob.*;
import java.awt.Point;
import java.awt.Color;
import java.awt.Rectangle;

public class OrthoRenderInfo extends Vob.RenderInfo {

    Color bg;
    float maxdepth = -1;

    public OrthoRenderInfo(Color bg, float maxdepth) {
        this.maxdepth = maxdepth;
        this.bg = bg;
    }
    public OrthoRenderInfo() {
        bg = null;
    }

    public Color getBgColor() { return bg; }

    float depth;

    public void setCoords(float depth, float x, float y, float w, float h,
			  float scaleX, float scaleY) {
        this.depth = depth;
	this.x = x;
	this.y = y;
	this.width = w;
	this.height = h;
	this.scaleX = scaleX;
	this.scaleY = scaleY;
    }

    public int box_x(float px) { return (int)(x + width*px); }
    public int box_y(float py) { return (int)(y + height*py); }
}
