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

    protected Lob delegate;

    public SizeRequest getSizeRequest() {
	return delegate.getSizeRequest();
    }

    public Layout layout(float w, float h) {
	return newDelegateLayout(delegate.layout(w, h), w, h);
    }

    protected Layout newDelegateLayout(Layout delegate, float w, float h) {
	DelegateLayout l = (DelegateLayout)DELEGATE_LAYOUT_FACTORY.object();
	l.delegate = delegate;
	l.lob = this;
	l.width = w; l.height = h;
	return l;
    }

    protected void render(Layout delegate, VobScene scene, int into,
			  int matchingParent, float w, float h,
			  float d, boolean visible) {
	delegate.render(scene, into, matchingParent, d, visible);
    }


	
    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    delegate.move(os);
	    return true;
	}
	return false;
    }

    protected static class DelegateLayout extends AbstractLayout {
	protected DelegateLayout() {}
	
	protected Layout delegate;
	protected AbstractDelegateLob lob;
	protected float width, height;

	public Size getSize() {
	    return Size.newInstance(width, height);
	}

	public void render(VobScene scene, int into, int matchingParent,
			   float d, boolean visible) {
	    lob.render(delegate, scene, into, matchingParent, 
		       width, height, d, visible);
	}

	public boolean move(ObjectSpace os) {
	    if(super.move(os)) {
		lob.move(os);
		delegate.move(os);
		return true;
	    }
	    return false;
	}
    }

    private static final Factory DELEGATE_LAYOUT_FACTORY = new Factory() {
	    public Object create() {
		return new DelegateLayout();
	    }
	};
}
