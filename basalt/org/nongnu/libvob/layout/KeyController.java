/*   
KeyController.java
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.AbstractUpdateManager;
import org.nongnu.libvob.VobMouseEvent;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public class KeyController extends AbstractMonoLob {

    protected MapModel actions;

    public KeyController(Lob content, MapModel actions) {
	super(content);
	this.actions = actions;
    }

    public KeyController(Lob content) {
	this(content, new MapModel.Simple());
    }

    public void add(String key, Action action) {
	actions.put(key, action);
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content, actions };
    }
    protected Object clone(Object[] params) {
	return new KeyController((Lob)params[0], (MapModel)params[1]);
    }

    public boolean key(String key) {
	Action a = (Action)actions.get(key);
	if(a == null) return content.key(key);

	a.run();
	AbstractUpdateManager.chg();
	return true;
    }
}
