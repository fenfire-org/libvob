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
	this(elements, new Object[0]);
    }
    public ListBox(ListModel elements, String k1, Object v1) {
	this(elements, params(k1, v1));
    }
    public ListBox(ListModel elements, String k1, Object v1, 
		   String k2, Object v2) {
	this(elements, params(k1, v1, k2, v2));
    }
    public ListBox(ListModel elements, String k1, Object v1, 
		   String k2, Object v2, String k3, Object v3) {
	this(elements, params(k1, v1, k2, v2, k3, v3));
    }

    private Object[] PARAMS_SPEC = {
	"template", TEMPLATE, Object.class,
            new Label(Models.adaptMethod(Parameter.model(ListModel.PARAM, new ObjectModel("")), Object.class, "toString")),
	"key", KEY, Model.class, null,
	"selectionModel", SELECTED, Model.class, null,
    };

    public ListBox(ListModel elements, Object[] keys) {

	Map params = parseParams(keys, PARAMS_SPEC);
	params.put(ELEMENTS, elements);

	Model selectionModel = (Model)params.get(SELECTED);
	if(!elements.isEmpty() && selectionModel.get() == null)
	    selectionModel.set(elements.get(0));

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
