/*
Lobs.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lob;
import org.nongnu.libvob.*;
import org.nongnu.libvob.lob.lobs.*;
import org.nongnu.libvob.vobs.*;
import org.nongnu.libvob.util.*;
import javolution.lang.*;
import javolution.realtime.*;
import java.awt.Color;
import java.util.*;

/** Static methods for creating common kinds of lobs.
 */
public class Lobs {

    public static final LobFont defaultFont =
	SimpleLobFont.newInstance("serif", 0, 16, Color.black);

    public static Lob rect(Color color, float lineWidth) {
	return VobLob.newInstance(RectVob.newInstance(color, lineWidth));
    }

    public static Lob rect3d(Color color, float lineWidth, boolean raised) {
	return VobLob.newInstance(RectVob.newInstance(color, lineWidth, 
						      raised));
    }

    public static Lob filledRect(Color color) {
	return VobLob.newInstance(FilledRectVob.newInstance(color));
    }

    public static Lob margin(Lob l, float margin) {
	return Margin.newInstance(l, margin);
    }

    public static Lob clip(Lob l) {
	return ClipLob.newInstance(l);
    }

    public static Lob glue(Axis axis, float min, float nat, float max) {
	return Glue.newInstance(axis, min, nat, max);
    }

    public static Lob glue(float minW, float natW, float maxW,
			   float minH, float natH, float maxH) {
	return Glue.newInstance(minW, natW, maxW, minH, natH, maxH);
    }

    public static Lob align(Lob content, float childX, float childY, 
			    float parentX, float parentY) {
	return AlignLob.newInstance(content, childX, childY, parentX, parentY);
    }

    public static Lob between(Lob back, Lob middle, Lob front) {
	return Between.newInstance(back, middle, front);
    }

    public static Lob request(Axis axis, Lob content, 
			      float min, float nat, float max) {
	return RequestChangeLob.newInstance(axis, content, min, nat, max);
    }

    public static Lob request(Lob content, float minW, float natW, float maxW,
			      float minH, float natH, float maxH) {
	return RequestChangeLob.newInstance(content, minW, natW, maxW, 
					    minH, natH, maxH);
    }

    public static Lob frame(Lob content, Color bg, Color border, 
			    float lineWidth, float margin, boolean clip) {
	Lob _bg     = bg==null     ? null : filledRect(bg);
	Lob _border = border==null ? null : rect(border, lineWidth);

	content = margin(content, margin);
	if(clip) content = clip(content);

	return between(_bg, content, _border);
    }

    public static Lob frame3d(Lob content, Color bg, Color border, 
			      float lineWidth, float margin, boolean clip, 
			      boolean raised) {
	Lob _bg     = bg==null     ? null : filledRect(bg);
	Lob _border = border==null ? null : rect3d(border, lineWidth, raised);

	content = margin(content, margin);
	if(clip) content = clip(content);

	return Between.newInstance(_bg, content, _border);
    }

    public static Lob text(String s) {
	return text(defaultFont, s);
    }

    public static Lob text(LobFont font, String s) {
	return text(font, Text.valueOf(s));
    }

    public static Lob text(Text text) {
	return text(defaultFont, text);
    }

    public static Lob text(LobFont font, Text text) {
	return BoxLob.newInstance(Axis.X, TextLobList.newInstance(font, text));
    }
}
