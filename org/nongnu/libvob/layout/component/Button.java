/*   
Button.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
 *
 *    This file is part of Fenfire.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.layout.component;
import org.nongnu.libvob.layout.*;
import java.util.*;

public class Button extends LobMonoLob implements Component {

    public static final String 
	URI = "http://fenfire.org/2004/07/layout/Button",
	ACTION = "http://fenfire.org/2004/07/layout/action";

    public static final Object[] PARAMS = { CONTENT, ACTION, KEY };

    public Button(String text, Action action) {
	this(new Label(text), action, new ObjectModel(null));
    }

    public Button(String text, Action action, Model key) {
	this(new Label(text), action, key);
    }

    public Button(Lob content, Action action) {
	this(content, action, new ObjectModel(null));
    }

    public Button(Lob content, Action action, Model key) {
	Map params = new HashMap();
	params.put(CONTENT, content);
	params.put(ACTION, action);
	params.put(KEY, key);

	Theme t = Theme.getDefaultTheme();

	setDelegate(t.getLob(URI, params));
    }
}
