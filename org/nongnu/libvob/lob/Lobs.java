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

    public static Lob line(Color color, float x1, float y1, 
			   float x2, float y2) {
	Vob conn = SimpleConnection.newInstance(x1, y1, x2, y2, color);
	return VobLob.newInstance(conn);
    }

    public static Lob translate(Lob l, float x, float y) {
	return translate(l, x, y, 0);
    }

    public static Lob translate(Lob l, float x, float y, float z) {
	return TranslateLob.newInstance(l, x, y, z);
    }

    public static Lob scale(Lob l, float scale) {
	return scale(l, scale, scale);
    }

    public static Lob scale(Lob l, float sx, float sy) {
	return translate(l, sx, sy);
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

    public static Lob key(Lob content, Object key) {
	return key(content, key, -1);
    }

    public static Lob key(Lob content, Object key, int intKey) {
	return KeyLob.newInstance(content, key, intKey);
    }

    public static Lob frame(Lob content, Color bg, Color border, 
			    float lineWidth, float margin, boolean clip) {
	Lob _bg     = bg==null     ? null : filledRect(bg);
	Lob _border = border==null ? null : rect(border, lineWidth);

	content = margin(content, lineWidth+margin);
	if(clip) content = clip(content);

	return between(_bg, content, _border);
    }

    public static Lob frame3d(Lob content, Color bg, Color border, 
			      float lineWidth, float margin, boolean clip, 
			      boolean raised) {
	Lob _bg     = bg==null     ? null : filledRect(bg);
	Lob _border = border==null ? null : rect3d(border, lineWidth, raised);

	content = margin(content, lineWidth+margin);
	if(clip) content = clip(content);

	return Between.newInstance(_bg, content, _border);
    }

    public static Lob decorate(Lob child, Lob decoration, Object key,
			       int intKey) {
	return DecoratorLob.newInstance(child, decoration, key, intKey);
    }

    public static Lob hbox(LobList items) {
	return box(Axis.X, items);
    }

    public static Lob vbox(LobList items) {
	return box(Axis.Y, items);
    }

    public static Lob box(Axis axis, LobList items) {
	return BoxLob.newInstance(axis, items);
    }

    public static Lob linebreaker(LobList items) {
	return linebreaker(Axis.X, items);
    }

    public static Lob linebreaker(Axis lineAxis, LobList items) {
	return LinebreakerLob.newInstance(lineAxis, items);
    }

    public static LobFont font(Color color) {
	LobFont sf = SimpleLobFont.newInstance("serif", 0, 16, color);
	return FilterLobFont.newInstance(sf);
    }

    public static LobFont font() {
	return font(Color.black);
    }

    public static LobList text(String s) {
	return text(font(), s);
    }

    public static LobList text(LobFont font, String s) {
	return text(font, Text.valueOf(s));
    }

    public static LobList text(Text text) {
	return text(font(), text);
    }

    public static LobList text(LobFont font, Text text) {
	return TextLobList.newInstance(font, text);
    }
}
