/*   
TextStyle.java
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
package org.nongnu.libvob;
import java.awt.*;

/** A style to draw text in.
 * To get an instance suited for the current graphics API, call
 * gzz.client.GraphicsAPI.getTextStyle()
 */

public abstract class TextStyle {

    /** Get the scale to use to get a font in this style of height h.
     */
    public abstract float getScaleByHeight(float h) ;

    public abstract float getWidth(String s, float scale) ;
    public abstract float getWidth(char[] chars, int offs, int len, float scale) ;

    public abstract float getHeight(float scale) ;

    public abstract float getAscent(float scale) ;

    public abstract float getDescent(float scale) ;

    public abstract float getLeading(float scale) ;

    /** DEPRECATED
     * @deprecated Just use getWidth(substring)
     */
    public float getX(String s, float scale, int offs) {
	return getWidth(s.substring(0, offs), scale);
    }
    /** DEPRECATED
     * @deprecated Just use getWidth(substring)
     */
    public float getX(char[] chars, int offs, int len, float scale, int xoffs) {
	return getWidth(chars, offs, xoffs, scale);
    }

    /** Return the offset in the string which corresponds to the x
     * coordinate given.
     * For example, return value
     * 0 = beginning of string, and s.length() = end, etc.
     */
    public int getOffsetInText(String s, float scale, float x) {
	return getOffsetInText(s.toCharArray(), 0, s.length(), scale, x);
    }

    /** Return the offset in the string which corresponds to the x
     * coordinate given.
     * 0 = beginning of string, s.length() = end, etc.
     */
    public int getOffsetInText(char[] chars, int offs, int len, 
	    float scale, float x) {
	if(len == 0) return 0;
	if(len == 1) 
	    return (x < getWidth(chars, offs, 1, scale) / 2  ?  0 : 1);
	float w = getWidth(chars, offs, len/2, scale);
	if(x < w)
	    return getOffsetInText(chars, offs, len/2, scale, x);
	else
	    return getOffsetInText(chars, offs+len/2, len-len/2, scale, x-w) 
			+ len/2;

    }

}


