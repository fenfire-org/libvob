/*
KeyLob.java
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

public class KeyLob extends AbstractMonoLob {

    protected Model keyModel, intKeyModel;

    public KeyLob(Lob content, Object key) {
	this(content, new ObjectModel(key));
    }

    public KeyLob(Lob content, Model keyModel) {
	super(content);
	this.keyModel = keyModel;
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content, keyModel };
    }
    protected Object clone(Object[] params) { 
	return new KeyLob((Lob)params[0], (Model)params[1]);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float x, float y, float w, float h, float d,
		       boolean visible) {
	int cs = scene.coords.box(into, x, y, w, h);
	int mp = matchingParent;

	if(keyModel.get() != null)
	    // only change matching parent if key is non-null
	    mp = scene.matcher.add(matchingParent, cs, keyModel.get());
	
	content.render(scene, cs, mp, 0, 0, w, h, d, visible);
    }
}
