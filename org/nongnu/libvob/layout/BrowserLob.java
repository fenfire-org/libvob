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

    public Type ALL = new Type() {
	    public boolean contains(Object state) {
		return true;
	    }
	};

    protected Model state;
    protected Set views;

    protected List types; // the current *order* of types
    protected Map viewByType;

    protected Lob delegate;

    public BrowserLob(Model state, Set views) {
	this.state = state;
	this.views = views;

	this.types = new ArrayList();
	this.viewByType = new HashMap();

	for(Iterator i=views.iterator(); i.hasNext();) {
	    View v = (View)i.next();
	    Set typeSet = (Set)v.getTypes();

	    for(Iterator j=typeSet.iterator(); j.hasNext();) {
		Type t = (Type)j.next();

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

    protected Lob getDelegate() {
	if(delegate == null) {
	    Object s = state.get();

	    for(Iterator i=types.iterator(); i.hasNext();) {
		Type type = (Type)i.next();
		if(type.contains(s)) {
		    View view = (View)viewByType.get(type);
		    delegate = view.getViewLob(state);
		    break;
		}
	    }

	    if(delegate == null) delegate = new AlignLob(new org.nongnu.libvob.layout.component.Label("No matching view found!"), .5f, .5f, .5f, .5f);

	    delegate.addObs(this);
	}

	return delegate;
    }
}
