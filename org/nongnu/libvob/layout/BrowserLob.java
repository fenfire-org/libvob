/*
BrowserLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
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
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public class BrowserLob extends AbstractDelegateLob {

    public interface Type {
	boolean contains(Object state);
    }

    public interface View {
	Set getTypes();
	Lob getViewLob(Model state);
    }

    public static Type ALL = new Type() {
	    public boolean contains(Object state) {
		return true;
	    }
	};

    protected Model state;

    protected Set views;

    protected List types; // the current *order* of types
    protected Map viewByType;
    protected Map viewListByType;

    protected Lob delegate;

    public BrowserLob(Model state, Set views) {
	this.state = state;
	this.views = views;

	this.types = new ArrayList();
	this.viewByType = new HashMap();
	this.viewListByType = new HashMap();

	for(Iterator i=views.iterator(); i.hasNext();) {
	    View v = (View)i.next();
	    Set typeSet = (Set)v.getTypes();

	    for(Iterator j=typeSet.iterator(); j.hasNext();) {
		Type t = (Type)j.next();
		
		List l = (List)viewListByType.get(t);
		if(l == null) {
		    l = new ArrayList();
		    viewListByType.put(t, l);
		}

		l.add(v);

		if(!types.contains(t)) 
		    types.add(t);

		if(!viewByType.containsKey(t))
		    viewByType.put(t, v);
	    }
	}

	state.addObs(new Obs() { public void chg() {
	    if(delegate != null) delegate.removeObs(BrowserLob.this);
	    delegate = null;
	    BrowserLob.this.chg();
	}});
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { state };
    }
    protected Object clone(Object[] params) {
	return new BrowserLob((Model)params[0], views);
    }

    protected View getView() {
	Object s = state.get();
	
	for(Iterator i=types.iterator(); i.hasNext();) {
	    Type type = (Type)i.next();
	    if(type.contains(s))
		return (View)viewByType.get(type);
	}

	return null;
    }

    protected Lob getDelegate() {
	if(delegate == null) {

	    View view = getView();
	    if(view != null)
		delegate = view.getViewLob(state);
	    else
		delegate = new AlignLob(new org.nongnu.libvob.layout.component.Label("No matching view found!"), .5f, .5f, .5f, .5f);

	    delegate.addObs(this);
	}

	return delegate;
    }

    public boolean key(String key) {
	if(key.equals("Ctrl-J"))
	    changeView(1);
	else if(key.equals("Ctrl-K"))
	    changeView(-1);
	else
	    return super.key(key);

	return true;
    }

    public void changeView(int steps) {
	List list = new ArrayList();
	System.out.println("views: "+list);

	for(Iterator i=views.iterator(); i.hasNext();) {
	    View v = (View)i.next();

	    for(Iterator j=v.getTypes().iterator(); j.hasNext();) {
		Type t = (Type)j.next();
		if(t.contains(state.get())) {
		    list.add(v);
		    break;
		}
	    }
	}

	int index = list.indexOf(getView());
	index = (index + steps) % list.size();
	if(index < 0) index += list.size();
	System.out.println("move to index "+index);
	changeView((View)list.get(index));
    }

    public void changeView(View v) {
	// find first matching type

	Type t = null;
	for(Iterator i=types.iterator(); i.hasNext();) {
	    t = (Type)i.next();
	    if(v.getTypes().contains(t) && t.contains(state.get()))
		break;
	}

	if(t == null)
	    // no matching type
	    return;

	types.remove(t);
	types.add(0, t);
	viewByType.put(t, v);

	delegate = null;
	chg();
    }
}
