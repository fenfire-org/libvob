/*   
MsgBox.java
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.layout.component.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.vobs.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public class MsgBox extends LobLob {

    protected WindowManager windowManager;

    public MsgBox(String message, String title,
		  WindowManager windowManager) {

	this.windowManager = windowManager;

	final float inf = Float.POSITIVE_INFINITY;
	final float nan = Float.NaN;

	final LobFont font = new LobFont("SansSerif", 0, 14, 
					 java.awt.Color.black);
	Box box = new Box(Lob.Y);

	box.add(new AlignLob(font.getLabel(message), .5f, .5f, .5f, .5f));

	box.glue(5, 5, 5);

	Box buttons = new Box(Lob.X);

	Lob l = font.getLabel("Ok");
	l = new Button(l, new AbstractAction() { public void run() {
	    MsgBox.this.windowManager.remove(MsgBox.this);
	}});
	buttons.add(new KeyLob(l, "Ok button"));

	box.add(new AlignLob(buttons, .5f, .5f, .5f, .5f));

	l = box;

	//l = new Window(l, title);

	setDelegate(l);

	windowManager.add(this, title);
    }
}
