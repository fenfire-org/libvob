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

    public interface View {
	boolean accepts(Object state);
	Lob getViewLob(Model state);
    }

    protected Model state;
    protected ListModel views;

    protected Lob delegate;

    public BrowserLob(Model state, ListModel views) {
	this.state = state;
	this.views = views;

	state.addObs(new Obs() { public void chg() {
	    if(delegate != null) delegate.removeObs(BrowserLob.this);
	    delegate = null;
	    BrowserLob.this.chg();
	}});
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { state, views };
    }
    protected Object clone(Object[] params) {
	return new BrowserLob((Model)params[0], (ListModel)params[1]);
    }

    protected Lob getDelegate() {
	if(delegate == null) {
	    Object s = state.get();

	    for(Iterator i=views.iterator(); i.hasNext();) {
		View view = (View)i.next();
		if(view.accepts(s)) {
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
