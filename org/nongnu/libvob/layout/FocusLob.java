/*   
FocusLob.java
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import java.util.*;

/** A lob that keeps a current focused lob and delegates
 *  keyboard events to that lob.
 */
public class FocusLob extends AbstractMonoLob {

    protected Model currentFocus;

    public FocusLob(Lob content) {
	this(content, new ObjectModel());
    }

    public FocusLob(Lob content, Model currentFocus) {
	super(content);

	this.currentFocus = currentFocus;

	List lobs = content.getFocusableLobs();
	if(!lobs.isEmpty()) currentFocus.set((Lob)lobs.get(0));
	content.setFocusModel(currentFocus);
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content, currentFocus };
    }
    protected Object clone(Object[] params) { 
	return new FocusLob((Lob)params[0], (Model)params[1]);
    }

    public Model getFocusModel() {
	return currentFocus;
    }

    public boolean key(String key) {
	if(key.equals("Tab")) {
	    nextFocus();
	    AbstractUpdateManager.chg();
	    return true;
	}

	Lob f = (Lob)currentFocus.get();

	if(f == null) return false;
	return f.key(key);
    }

    private void nextFocus() {
	List lobs = content.getFocusableLobs();
	if(lobs.isEmpty()) {
	    currentFocus = null;
	    return;
	}

	Lob f = (Lob)currentFocus.get();

	int index = 0;
	if(lobs.contains(f))
	    index = lobs.indexOf(f);

	index++;
	if(index >= lobs.size()) index = 0;
	
	currentFocus.set((Lob)lobs.get(index));
    }
}
