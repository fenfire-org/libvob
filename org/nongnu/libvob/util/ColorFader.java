/*
ColorFader.java
 *
 *    Copyright (c) 2005 by Benja Fallenstein
 *
 *    This file is part of Fenfire.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.util;
import org.nongnu.navidoc.util.WeakValueMap;
import java.awt.Color;
import java.util.*;

/** A 'fog setting' for AWT, which, given a color and a depth,
 *  will fade that color according to some fading settings.
 */
public final class ColorFader {

    private Color bg;
    private float minDepth, maxDepth;

    /** A fader that doesn't change the color it is passed.
     */
    public static final ColorFader NULL = new ColorFader(Color.white);

    public ColorFader(Color bg, float minDepth, float maxDepth) {
	this.bg = bg;
	this.minDepth = minDepth;
	this.maxDepth = maxDepth;
    }

    public ColorFader(Color bg) {
	this(bg, -1, -1);
    }

    public Color getBg() { return bg; }

    public Color getColor(Color c, float depth) {
	if(minDepth < 0) return c;

	float fract = (depth < minDepth) ? 0 : (depth > maxDepth) ? 1 : 
	    (depth-minDepth)/(maxDepth-minDepth);

	//System.out.println("given depth "+depth+", use fract "+fract);

	return getColorScale(c, bg).get((int)(31.999*fract));
    }


    private static final class ColorScale {
	private Color from, to;
	private float[] fromRGB, toRGB;
	
	private Color[] colors = new Color[32];
	
	private ColorScale(Color from, Color to) {
	    this.from = from; this.to = to;
	    fromRGB = from.getRGBColorComponents(null);
	    toRGB = to.getRGBColorComponents(null);
	}
	
	public Color get(int i) {
	    if(colors[i] == null) {
		float fraction = i/32f;
		float r = ((1-fraction)*fromRGB[0]) + (fraction*toRGB[0]);
		float g = ((1-fraction)*fromRGB[1]) + (fraction*toRGB[1]);
		float b = ((1-fraction)*fromRGB[2]) + (fraction*toRGB[2]);
		colors[i] = new Color(r,g,b);
	    }
	    return colors[i];
	}
    }

    private static WeakValueMap mapsOfColors = new WeakValueMap();

    private static ColorScale getColorScale(Color from, Color to) {
	Map m = (Map)mapsOfColors.get(to);
	if(m == null) mapsOfColors.put(to, m = new WeakValueMap());
	ColorScale c = (ColorScale)m.get(from);
	if(c == null) m.put(from, c = new ColorScale(from, to));
	return c;
    }
}
