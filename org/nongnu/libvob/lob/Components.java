/*
Components.java
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
import org.nongnu.libvob.fn.*;
import org.nongnu.libvob.lob.lobs.*;
import javolution.lang.*;
import java.awt.Color;

public class Components {

    public static Lob frame(Lob lob) {
	return Lobs.frame(lob, null, Color.black, 1, 5, false);

    }

    public static Lob textBox(Model text, Model cursor, LobFont font) {
	return textComponent(text, cursor, font, false);
    }

    public static Lob textArea(Model text, Model cursor, LobFont font) {
	return textComponent(text, cursor, font, true);
    }

    public static Lob textComponent(Model text, Model cursor, LobFont font,
				    boolean multiline) {
	Text txt = Text.valueOf((String)text.get());
	LobList list = Lobs.text(font, txt);
	list = KeyLobList.newInstance(list, "text");
	Lob lob = multiline ? Lobs.linebreaker(list) : Lobs.hbox(list);

        Lob cursor_lob = Lobs.line(java.awt.Color.black, 0, 0, 0, 1);
        cursor_lob = Lobs.key(cursor_lob, "textcursor");
        lob = Lobs.decorate(lob, cursor_lob, "text", cursor.getInt());
	
        lob = TextKeyController.newInstance(lob, text, cursor);

	return frame(lob);
    }
}
