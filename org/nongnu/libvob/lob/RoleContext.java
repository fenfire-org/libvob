/*
RoleContext.java
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
package org.nongnu.libvob.lob;
import org.nongnu.libvob.*;
import org.nongnu.libvob.lob.lobs.*;
import org.nongnu.libvob.fn.*;
import javolution.realtime.*;
import java.util.*;

public class RoleContext {

    private static final LocalContext.Variable STATE =
	new LocalContext.Variable(new HashMap());

    private static final LocalContext.Variable KEY =
	new LocalContext.Variable(null);

    

    public static void enter(Object key) {
	if(key == null) throw new NullPointerException("null key");

	LocalContext.enter();
	try {
	    Map state = getState();
	    Map nstate = (Map)state.get(key);
	    if(nstate == null) {
		nstate = new HashMap();
		state.put(key, nstate);
	    }

	    KEY.setValue(key);
	    STATE.setValue(nstate);
	} catch(Error e) {
	    LocalContext.exit();
	    throw e;
	} catch(RuntimeException e) {
	    LocalContext.exit();
	    throw e;
	}
    }

    public static Map getState() {
	return (Map)STATE.getValue();
    }

    public static Lob lob(Lob lob) {
	Object key = KEY.getValue();

	if(key == null)
	    throw new IllegalStateException("RoleContext.lob() cannot be called outside a RoleContext");

	return KeyLob.newInstance(lob, key, -1);
    }

    public static void exit() {
	LocalContext.exit();
    }
}
