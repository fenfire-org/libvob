/*
TextLobList.java
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
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.*;
import javolution.lang.*;
import javolution.realtime.*;

public class TextLobList extends RealtimeObject implements LobList {
    private static void p(String s) { System.out.println("TextLobList:: "+s); }

    private LobFont font;
    private Text text;

    private TextLobList() {}

    public static TextLobList newInstance(LobFont font, Text text) {
	TextLobList l = (TextLobList)FACTORY.object();
	l.font = font;
	l.text = text;
	return l;
    }

    public int getLobCount() {
	return text.length() + 1;
    }

    public Lob getLob(int index) {
	if(index < text.length())
	    return font.getLob(text.charAt(index));
	else if(index == text.length())
	    return font.getTextEndLob();
	else
	    throw new IndexOutOfBoundsException(""+index);
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    font.move(os);
	    text.move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new TextLobList();
	    }
	};
}
