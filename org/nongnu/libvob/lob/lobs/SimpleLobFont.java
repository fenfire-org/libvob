/*   
SimpleLobFont.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein.
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

package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.impl.DefaultVobMap; // special text rendering for AWT
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.vobs.TextVob;
import javolution.realtime.*;
import java.awt.Color;
import java.util.*;

public class SimpleLobFont extends RealtimeObject implements LobFont {

    protected static final Object KEY = new Object();

    protected TextStyle style;
    protected Color color;

    private SimpleLobFont() {}

    /*
    public LobFont(String family, int fstyle, int fsize, Color color) {
	this(GraphicsAPI.getInstance().getTextStyle(family, fstyle, fsize),
	     color);
    }
    */

    public static SimpleLobFont newInstance(String family, int fstyle, 
					    float height, Color color) {
	GraphicsAPI api = GraphicsAPI.getInstance();
	return newInstance(api.getTextStyleByHeight(family, fstyle, height),
			   color);
    }

    public static SimpleLobFont newInstance(TextStyle style, Color color) {
	SimpleLobFont f = (SimpleLobFont)FACTORY.object();
	f.style = style;
	f.color = color;
	return f;
    }

    public Lob getLob(char ch) {
	Glyph g = (Glyph)GLYPH_FACTORY.object();

	g.character = ch;
	g.style = style;
	g.color = color;
	g.textvob = null;

	return g;
    }


    protected static char[] charArray = new char[1];

    protected static class Glyph extends AbstractLayout {
	protected char character;
	protected TextStyle style;
	protected Color color;

	private Glyph() {}
	
	public Size getSize() {
	    charArray[0] = character;
	    return Size.newInstance(style.getWidth(charArray, 0, 1, 1),
				    style.getHeight(1));
	}

	protected TextVob textvob;

	public void render(VobScene scene, int into, int matchingParent,
			   float d, boolean visible) {

	    if(scene.map instanceof DefaultVobMap) {
		float h = style.getHeight(1);

		if(visible)
		    ((DefaultVobMap)scene.map).putChar(style, character, color,
						       h, into);
	    } else {
		if(textvob == null)
		    textvob = new TextVob(style, ""+character, false, KEY, 
					  color);

		float h = textvob.style.getHeight(1);
		int cs = scene.coords.scale(into, h, h);
		scene.matcher.add(matchingParent, cs, KEY);
		if(visible)
		    scene.put(textvob, cs);
	    }
	}
    }


    private static Factory FACTORY = new Factory() {
	    protected Object create() { return new SimpleLobFont(); }
	};

    private static Factory GLYPH_FACTORY = new Factory() {
	    protected Object create() { return new Glyph(); }
	};
}
