/*   
ListBox.java
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
package org.nongnu.libvob.layout.component;
import org.nongnu.libvob.*;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.vobs.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public class ListBox extends LobLob implements Component {

    protected static final String 
	URI = "XXX/ListBox",
	ELEMENTS = "XXX/ListBox/elements",
	SELECTED = "XXX/ListBox/selected",
	TEMPLATE = "XXX/ListBox/template";

    public ListBox(ListModel elements) {
	this(elements, NullLob.instance);
    }

    public ListBox(ListModel elements, Lob template) {
	this(elements, template, new ObjectModel(null));
    }

    public ListBox(ListModel elements, Lob template, Model key) {

	Map params = new HashMap();
	params.put(ELEMENTS, elements);
	params.put(TEMPLATE, template);
	params.put(KEY, key);

	if(!elements.isEmpty())
	    params.put(SELECTED, new ObjectModel(elements.get(0)));
	else
	    params.put(SELECTED, new ObjectModel(null));

	Theme t = Theme.getDefaultTheme();

	setDelegate(t.getLob(URI, params));
    }

    public void setTemplate(Lob l) {
	delegate.setTemplateParameter(TEMPLATE, l);

	// XXX
	((Obs)delegate.getTemplateParameter(ELEMENTS)).chg();
    }

    public Model getSelectionModel() { 
	return (Model)delegate.getTemplateParameter(SELECTED); 
    }
    public void setSelectionModel(Model m) {
	delegate.setTemplateParameter(SELECTED, m);
    }
}
