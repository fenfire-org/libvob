/*   
TreeBox.java
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
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.impl.Main;
import org.nongnu.libvob.impl.LobMain;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public class TreeBox extends LobLob implements Component {

    public static final String 
	URI = "XXX/TreeBox",
	TEMPLATE = "XXX/TreeBox/treeNodeTemplate",
	ROOT = "XXX/TreeBox/root",
	CHILDREN = "XXX/TreeBox/children",
	SELECTED = ListBox.SELECTED;

    public TreeBox(Model root, ListModel children, Lob template) {
	this(root, children, template, new ObjectModel(URI));
    }

    public TreeBox(Model root, ListModel children, Lob template, Model key) {
	Map params = new HashMap();
	params.put(ROOT, root);
	params.put(CHILDREN, children);
	params.put(TEMPLATE, template);
	params.put(KEY, key);

	params.put(SELECTED, new ObjectModel(root.get()));

	Theme t = Theme.getDefaultTheme();

	setDelegate(t.getLob(URI, params));
    }


    protected static MapModel elem(String title) {
	MapModel m = new MapModel.Simple();
	m.put("Title", title);
	m.put("Children", new ListModel.Simple());
	return m;
    }
    protected static void add(MapModel parent, MapModel child) {
	ListModel m = (ListModel)parent.get("Children");
	m.add(child);
    }

    public static void main(String[] argv) {
	final MapModel a = elem("The root");

	add(a, elem("Bar"));

	MapModel b = elem("Baz");
	add(a, b);
	add(b, elem("Bengbie"));

	Main m = new LobMain(new java.awt.Color(1, 1, .8f)) {
		protected Lob createLob() {
		    MapModel m0 = new MapModel.Simple();
		    Model elem0 = new Parameter(TreeBox.ROOT, new ObjectModel(m0));
		    MapModel elem = new MapModel.ModelMapModel(new ModelModel(elem0));

		    Model _t = new ObjectModel("Title");
		    //Model title = new ToStringModel(elem);
		    Model title = new MapModel.MapValueModel(elem, _t);
		    title = title.equalsObject(null)
			.select(new ObjectModel("NULL"), title);
		    title = elem0.equalsObject(m0)
			.select(new ObjectModel("Elem is pseudoNULL"), title);

		    Model _c = new ObjectModel("Children");
		    Model children = new MapModel.MapValueModel(elem, _c);
		    children = children.equalsObject(null)
			.select(new ObjectModel(new ListModel.Simple()), 
				children);
		    
		    return new TreeBox(new ObjectModel(a),
				       new ListModel.ModelListModel(children),
				       new Label(title));
		}
	    };
	m.start();
    }


    private static class ToStringModel 
	extends AbstractModel.AbstractObjectModel {

	protected Replaceable object;
	protected String string;

	public ToStringModel(Replaceable object) {
	    this.object = object;
	    ((org.nongnu.libvob.layout.Observable)object).addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { object };
	}
	protected Object clone(Object[] params) {
	    return new ToStringModel((Replaceable)params[0]);
	}

	public void chg() {
	    string = null;
	}

	public Object get() {
	    if(string == null) { 
		string = (""+object);
		System.out.println(string);
	    }
	    return string;
	}
    }
}
