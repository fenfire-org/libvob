/*
AbstractDelegateLob.java
 *    
 *    Copyright (c) 2003-2004, Benja Fallenstein
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
package org.nongnu.libvob.lob;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public abstract class AbstractDelegateLob extends AbstractLob {

    public Lob delegate;

    /** Create a lob like this one, but with a different delegate.
     *  Used in default implementations of layout() and layoutOneAxis().
     *  Default behavior: just return the new delegate.
     */
    protected Lob wrap(Lob l) {
	return l;
    }

    public Lob getImplementation(Class clazz) {
	if(clazz.isInstance(this))
	    return this;
	else
	    return delegate.getImplementation(clazz);
    }

    public SizeRequest getSizeRequest() {
	return delegate.getSizeRequest();
    }

    public Lob layout(float w, float h) {
	return wrap(delegate.layout(w, h));
    }

    public Axis getLayoutableAxis() {
	return delegate.getLayoutableAxis();
    }

    public Lob layoutOneAxis(float size) {
	return wrap(delegate.layoutOneAxis(size));
    }

    /**
     *  This implementation doesn't re-compute anything, not size etc.
     *  Note, however, that most classes inheriting from
     *  AbstractDelegateLob actually don't cache any information
     *  about their child lobs.
     */
    public void add(Lob lob) {
	delegate.add(lob);
    }

    public void render(VobScene scene, int into,
			  int matchingParent, float d, boolean visible) {
	delegate.render(scene, into, matchingParent, d, visible);
    }

    public boolean key(String key) {
	return delegate.key(key);
    }

    public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			 float x, float y) {
	return delegate.mouse(e, scene, cs, x, y);
    }

    public List getFocusableLobs() {
	return delegate.getFocusableLobs();
    }

    public float getBreakQuality(Axis axis) {
	return delegate.getBreakQuality(axis);
    }

    public Lob getBreakLob(Axis axis, int dir) {
	Lob lob = delegate.getBreakLob(axis, dir);
	if(dir < 0)
	    return (lob != null) ? wrap(lob) : null;
	else
	    return lob;
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    delegate.move(os);
	    return true;
	}
	return false;
    }
}
