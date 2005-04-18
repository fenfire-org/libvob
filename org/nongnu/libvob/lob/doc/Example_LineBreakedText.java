/*
Example_LineBreakedText.java
 *    
 *    Copyright (c) 2005, Matti J. Katila
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
 */
/*
 * Written by Matti J. Katila
 */

package org.nongnu.libvob.lob.doc;
import org.nongnu.libvob.lob.*;
import java.awt.Color;
import java.util.*;

// class is run from printter util to 
// generate visual screenshot.
public class Example_LineBreakedText {

    public Lob getLob() {
	String [] texts = {
	    "Abc", "cat", "walks", "to", "some", "tree", "which",
	    "has", "ugly", "green", "tea", "next", "to", "see.", 
	};


	Random r = new Random(0);
	LobFont[] fonts = new LobFont[texts.length];
	for (int i=0; i<texts.length; i++)
	    fonts[i] = Components.font(new Color(r.nextInt(255),
						 r.nextInt(255),
						 r.nextInt(255)));

	List textList = Lists.list();
	for (int i=0; i<texts.length; i++) {
	    textList = Lists.concat(textList, 
				    fonts[i].text(texts[i]+" "));
	}
	Lob l = Lobs.linebreaker(textList);
	l = Lobs.margin(l, 10);
	return l;
    }
}
