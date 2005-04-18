/*
Example_Scale.java
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
public class Example_Scale {

    public Lob getLob() {
	LobFont font = Components.font();
	Lob vbox = Lobs.vbox();
	
	int N = 5;
	float scale = 1;
	for (int i=0; i<N; i++) {
	    Lob l = Lobs.hbox(font.textLn("Hello!"));
	    l = Lobs.scale(l, scale, scale); // scaleX, scaleY
	    scale *= 1.2;
	    vbox.add(l);
	    if ((i+1) != N)
		vbox.add(Lobs.vglue());
	}
	return vbox;
    }
}
