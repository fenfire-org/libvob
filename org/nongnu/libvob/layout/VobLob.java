/*
VobLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
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

/** A lob drawing a single vob.
 */
public class VobLob extends AbstractLob {

    protected Vob vob;
    protected Object key;

    public VobLob(Vob vob, Object key) {
	this.vob = vob;
	this.key = key;
    }

    protected Replaceable[] getParams() { return NO_PARAMS; }
    protected Object clone(Object[] params) { return this; }

    public float getNatSize(Axis axis) {
	return 0;
    }

    public Set getTemplateParameters() {
	if(vob instanceof Replaceable)
	    return ((Replaceable)vob).getTemplateParameters();
	else
	    return Collections.EMPTY_SET;
    }

    public Object getTemplateParameter(Object key) {
	if(vob instanceof Replaceable)
	    return ((Replaceable)vob).getTemplateParameter(key);
	else
	    return null;
    }

    public void setTemplateParameter(Object key, Object value) {
	if(vob instanceof Replaceable)
	    ((Replaceable)vob).setTemplateParameter(key, value);
    }

    public Object instantiateTemplate(java.util.Map map) {
	if(map.get(this) != null) return map.get(this);

	if(!(vob instanceof Replaceable)) {
	    map.put(this, this);
	    return this;
	}

	Replaceable repl = (Replaceable)vob;
	Vob newVob = (Vob)repl.instantiateTemplate(map);
	if(newVob == vob) {
	    map.put(this, this);
	    return this;
	}

	Lob newThis = new VobLob(newVob, key);
	map.put(this, newThis);
	return newThis;
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {
	// XXX create a coordsys? which key?
	int cs = scene.coords.box(into, w, h);
	scene.matcher.add(matchingParent, cs, key);
	if(visible)
	    scene.put(vob, cs);
    }
}
