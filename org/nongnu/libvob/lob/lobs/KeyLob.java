/*
KeyLob.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein
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
import org.nongnu.libvob.*;
import org.nongnu.libvob.layout.IndexedVobMatcher;
import javolution.realtime.*;

/** A lob placing its contents into a translation cs.
 */
public class KeyLob extends AbstractDelegateLob {

    protected Object key;
    protected int intKey;

    private KeyLob() {}

    public static KeyLob newInstance(Lob content, Object key, int intKey) {
	KeyLob m = (KeyLob)FACTORY.object();
	m.delegate = content;

	m.key = key;
	m.intKey = intKey;

	return m;
    }

    public Lob layout(float w, float h) {
	return newInstance(delegate.layout(w, h), key, intKey);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {

	int cs = scene.coords.translate(into, 0, 0, 0);

	if(scene.matcher instanceof IndexedVobMatcher) {
	    IndexedVobMatcher m = (IndexedVobMatcher)scene.matcher;
	    m.add(matchingParent, cs, key, intKey);
	} else {
	    scene.matcher.add(matchingParent, cs, key);
	}

	delegate.render(scene, cs, cs, d, visible);
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    if(key instanceof Realtime)
		((Realtime)key).move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new KeyLob();
	    }
	};
}
