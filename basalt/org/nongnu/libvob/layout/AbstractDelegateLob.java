/*
AbstractMonoLob.java
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public abstract class AbstractDelegateLob extends AbstractLob 
    implements Obs {

    protected abstract Lob getDelegate();

    public float getMinSize(Axis axis) {
	return getDelegate().getMinSize(axis);
    }

    public float getNatSize(Axis axis) {
	return getDelegate().getNatSize(axis);
    }

    public float getMaxSize(Axis axis) {
	return getDelegate().getMaxSize(axis);
    }

    public boolean key(String key) {
	return getDelegate().key(key);
    }

    public boolean mouse(VobMouseEvent e, float x, float y, 
			 float origX, float origY) {
	return getDelegate().mouse(e, x, y, origX, origY);
    }

    public List getFocusableLobs() {
	return getDelegate().getFocusableLobs();
    }

    public void setFocusModel(Model m) {
	getDelegate().setFocusModel(m);
    }

    public void setSize(float requestedWidth, float requestedHeight) {
	getDelegate().setSize(requestedWidth, requestedHeight);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {
	getDelegate().render(scene, into, matchingParent, w, h, d,
			     visible);
    }

    public boolean isLargerThanItSeems() {
	return getDelegate().isLargerThanItSeems();
    }
}
