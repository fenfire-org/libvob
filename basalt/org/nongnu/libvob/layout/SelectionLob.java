/*   
SelectionLob.java
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
import org.nongnu.libvob.*;
import org.nongnu.libvob.vobs.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public class SelectionLob extends AbstractDelegateSequence {
    protected Sequence list;
    protected Model keyModel, intKeyModel;
    
    protected Lob selector;
    
    public SelectionLob(Sequence list, Lob selector, 
			Model keyModel, Model intKeyModel) {
	this.list = list;
	this.selector = selector;
	this.keyModel = keyModel;
	this.intKeyModel = intKeyModel;
    }
    
    protected Sequence getDelegateSequence() {
	return list;
    }
    public Replaceable[] getParams() { 
	return new Replaceable[] { list, selector, keyModel, intKeyModel };
    }
    public Object clone(Object[] params) { 
	return new SelectionLob((Sequence)params[0], (Lob)params[1],
				(Model)params[2], (Model)params[3]);
    }

    private static final float[] wh = new float[2];

    public void render(VobScene scene, int into, 
		       int matchingParent, 
		       float w, float h, float d,
		       boolean visible) {
	int cs = scene.coords.translate(into, 0, 0, 0);
	list.render(scene, cs, matchingParent, w, h, d/2, visible);
	
	Object key = keyModel.get();
	int intKey = intKeyModel.getInt();

	int anchor;
	if(scene.matcher instanceof IndexedVobMatcher) {
	    IndexedVobMatcher m = (IndexedVobMatcher)scene.matcher;
	    anchor = m.getCS(matchingParent, key, intKey);
	} else {
	    anchor = scene.matcher.getCS(matchingParent, key);
	}

	if(anchor < 0) return;

	scene.coords.getSqSize(anchor, wh);

	cs = scene.coords.translate(anchor, 0, 0, d/2);
	selector.render(scene, cs, matchingParent, wh[0], wh[1], d/2,
			visible);
    }
}
