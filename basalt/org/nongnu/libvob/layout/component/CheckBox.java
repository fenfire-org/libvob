/*
CheckBox.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein and Matti J. Katila
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
 * Written by Benja Fallenstein and Matti J. Katila
 */
package org.nongnu.libvob.layout.component;
import org.nongnu.libvob.layout.*;
import java.util.*;

public class CheckBox extends LobMonoLob implements Component {

    public static final String 
	URI = "http://fenfire.org/2004/07/layout/CheckBox",
	CHECKED = "http://fenfire.org/2004/07/layout/checkBoxChecked";

    public CheckBox(String label) { 
	this(label, new BoolModel()); 
    }

    public CheckBox(String label, Model checked) { 
	this(new Label(label), checked); 
    }

    public CheckBox(String label, Model checked, Model key) { 
	this(new Label(label), checked, key); 
    }

    public CheckBox(Lob content) { 
	this(content, new BoolModel()); 
    }

    public CheckBox(Lob content, Model checked) {
	this(content, checked, new ObjectModel(null));
    }

    public CheckBox(Lob content, Model checked, Model key) {
	Map params = new HashMap();
	params.put(CONTENT, content);
	params.put(CHECKED, checked);
	params.put(KEY, key);

	Theme t = Theme.getDefaultTheme();

	setDelegate(t.getLob(URI, params));
    }
}
