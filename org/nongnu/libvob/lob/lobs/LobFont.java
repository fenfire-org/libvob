/*   
LobFont.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
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
import org.nongnu.libvob.impl.DefaultVobMap; // special text rendering for AWT
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.vobs.TextVob;
import org.nongnu.navidoc.util.Obs;
import java.awt.Color;
import java.util.*;

public class LobFont /*extends AbstractHashtable*/ {

    /*
    private static final float inf = Float.POSITIVE_INFINITY;
    private static final int INITIAL_SIZE = 512;

    protected static final Object KEY = new Object();

    protected TextStyle style;
    protected Color color;

    protected char[] glyphChar = new char[INITIAL_SIZE];
    protected Lob[] glyph = new Lob[INITIAL_SIZE];

    protected Lob space, lineEnd, newline;

    / *
    public LobFont(String family, int fstyle, int fsize, Color color) {
	this(GraphicsAPI.getInstance().getTextStyle(family, fstyle, fsize),
	     color);
    }
    * /

    public LobFont(String family, int fstyle, float height, Color color) {
	this(GraphicsAPI.getInstance().getTextStyleByHeight(family, fstyle, 
							    height),
	     color);
    }

    public LobFont(TextStyle style, Color color) {
	super(INITIAL_SIZE);

	this.style = style;
	this.color = color;
	
	Lob.Axis X = Lob.X, Y = Lob.Y;

	float textHeight = style.getHeight(1);

	space = new TextLob(style, ' ', color);

	float w = style.getWidth(" ", 1);
	space = 
	    new RequestChangeLob(space, w, w, 2*w, 0, textHeight, textHeight);

	Lob strut = new Glue(0, 0, 0, 0, textHeight, textHeight);

	space = new BreakPoint(X, space, 0, strut, null, null);

	lineEnd = new Glue(0, 0, inf, textHeight, textHeight, textHeight);

	newline = // forced break followed by indent
	    new BreakPoint(X, lineEnd, inf, 
			   lineEnd, null, new Glue(X, 25, 25, 25));
    }

    protected int hashCode(int entryIndex) { 
	return glyphChar[entryIndex]; 
    }

    protected void expandArrays(int newSize) {
	char[] nc = new char[newSize]; Lob[] ng = new Lob[newSize];
	System.arraycopy(glyphChar, 0, nc, 0, glyphChar.length);
	System.arraycopy(glyph, 0, ng, 0, glyph.length);
	glyphChar = nc; glyph = ng;
    }

    public TextStyle getTextStyle() {
	return style;
    }

    public Lob getLabel(String text) {
	return getLabel(text, KEY);
    }

    public Lob getLabel(String text, Object key) {
	Box box = new Box(Lob.X);
	for(int i=0; i<text.length(); i++)
	    box.add(getGlyph(text.charAt(i)), key, i);
	box.add(lineEnd);
	return box;
    }

    / ** In the future, we need to transform a list of characters into
     *  a list of glyphs -- characters don't really map 1:1 to glyphs,
     *  that's an oversimplifying hack. (Read about Unicode combining marks
     *  if you don't know about this, or just google for something like
     *  'unicode characters glyphs'.)
     * /
    public Lob getGlyph(char ch) {
	if(ch == ' ')
	    return space;
	else if(ch == '\n')
	    return newline;
	else {
	    for(int i=first(ch); i>=0; i=next(i))
		if(glyphChar[i] == ch)
		    return glyph[i];

	    int i = newEntry();

	    glyphChar[i] = ch;
	    glyph[i] = new TextLob(style, ch, color);

	    put(i);
	    return glyph[i];
	}
    }

    public Lob getLineEnd() {
	return lineEnd;
    }


    protected static class TextLob extends AbstractLob {
	protected char character;
	protected TextStyle style;
	protected Color color;

	public TextLob(TextStyle style, char character, Color color) {
	    this.style = style;
	    this.character = character;
	    this.color = color;
	}

	/ ** Text lobs never change size and they are used by many parents:
	 *  they mustn't store large numbers of obs that are never triggered
	 * /
	public void addObs(Obs obs) {
	}
	public void removeObs(Obs obs) {
	}

	protected Replaceable[] getParams() { return NO_PARAMS; }
	protected Object clone(Object[] params) {
	    return this;
	}

	private float myWidth = -1, myHeight = -1;
	
	private void computeSizes() {
	    if(myWidth < 0) {
		// argl -- NOT nice to create string!!!
		myWidth = style.getWidth(""+character, 1);
		myHeight = style.getHeight(1);
	    }
	}

	public float getNatSize(Axis axis) {
	    computeSizes();
	    return (axis==X) ? myWidth : myHeight;
	}

	protected TextVob textvob;

	public void render(VobScene scene, int into, int matchingParent,
			   float _w, float _h, float d,
			   boolean visible) {

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
    */
}
