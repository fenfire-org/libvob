/*
KeyController.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein
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
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.fn.*;
import org.nongnu.libvob.*;
import java.util.*;

public class KeyController extends AbstractDelegateLob {

    protected Map actions;

    private KeyController() {}

    public static KeyController newInstance(Lob content, Map actions) {
	KeyController c = (KeyController)FACTORY.object();
	c.delegate = content;
	c.actions = actions;
	return c;
    }

    public Lob wrap(Lob l) {
	return newInstance(l, actions);
    }

    public boolean key(String key) {
	Action action = (Action)actions.get(key);
	if(action != null) {
	    action.run();
	    return true;
	} else {
	    return delegate.key(key);
	}
    }

    public List getFocusableLobs() {
	return Lists.list(this);
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new KeyController();
	    }
	};
}
