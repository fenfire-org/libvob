/*   
ColorUtil.java
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
package org.nongnu.libvob.util;
import java.util.*;
import java.awt.*;

/** Some color handling utilities.
 */

public final class ColorUtil {

    public static Color[] fadingColors_line(int n) {
	Color[] res = new Color[n];
	for(int i=0; i<n; i++) {
	    int bluegreen = (0xFF * i)/n;
	    res[i] = new Color(0xFF0000 + (bluegreen << 8) + bluegreen);
	}
	return res;
    }

    public static Color[] fadingColors_solid(int n) {
	Color[] res = new Color[n];
	for(int i=0; i<n; i++) {
	    float f = i/(float)(n-1);
	    res[i] = Color.getHSBColor(f/2, 0.8f*(1-f), 1);
	}
	return res;
    }

    public static Color[] fadingColors_solid_darker(int n) {
	Color[] res = new Color[n];
	for(int i=0; i<n; i++) {
	    float f = i/(float)(n-1);
	    res[i] = Color.getHSBColor(f/2, 0.8f*(1-f), 0.9f);
	}
	return res;
    }

    /** Emulate Color.getRGBColorComponents
     *  for Java 1.1 (including Kaffe).
     */
    public static float[] getRGBColorComponents(Color c, float[] f) {
	if(f == null) f = new float[3];

	final int RED_MASK = 255 << 16;
	final int GREEN_MASK = 255 << 8;
	final int BLUE_MASK = 255;
	
	int value = c.getRGB();
	f[0] = ((value & RED_MASK) >> 16) / 255f;
	f[1] = ((value & GREEN_MASK) >> 8) / 255f;
	f[2] = (value & BLUE_MASK) / 255f;
        return f;
    }


    public static String colorGLString(Color c) {
	float[] f = getRGBColorComponents(c, null);
	return ""+f[0]+" "+f[1]+" "+f[2];
    }

    /** Get the average color of an array of colors.
     */
    public static Color avgColor(int[] colors) {
	float r = 0, g = 0, b = 0;
	for(int i=0; i<colors.length; i++) {
	    int c = colors[i];
	    r += (c >> 16) & 0xff;
	    g += (c >> 8) & 0xff;
	    b += (c >> 0) & 0xff;
	}
	r /= colors.length;
	g /= colors.length;
	b /= colors.length;
	r /= 255;
	g /= 255;
	b /= 255;
	return new Color(r, g, b);
    }
}
