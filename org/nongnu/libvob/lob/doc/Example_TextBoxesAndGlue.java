/*
Example_TextBoxesAndGlue.java
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
import java.util.*;

// class is run from printter util to 
// generate visual screenshot.
public class Example_TextBoxesAndGlue {

    public Lob getLob() {
	
	LobFont font = Components.font();
	
	// vertical box or list
	Lob vbox = Lobs.vbox();
	
	vbox.add(Lobs.hbox(font.textLn("Hello world!")));
	vbox.add(Lobs.hbox(font.text("Hello world!")));

	List l1 = font.text("Hello");
	List l2 = Lists.list(Lobs.glue(Axis.X, 1));
	List l3 = font.text("world!");
	List l4 = Lists.list(Lobs.hglue());
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3,l4)));
	
	l1 = Lists.list(Lobs.hglue());
	l2 = font.text("Hello");
	l3 = font.text("world!");
	l4 = Lists.list(Lobs.hglue());
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3,l4)));

	l1 = Lists.list(Lobs.hglue());
	l2 = font.text("Hello world!");
	l3 = Lists.list(Lobs.hglue());
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3)));
		 
	l1 = Lists.list(Lobs.hglue());
	l2 = font.text("Hello");
	l3 = font.text(" ");
	l4 = font.text("world!");
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3,l4)));

	return vbox;
    }
}
