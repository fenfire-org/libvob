/*
DelegateSequence.java
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

public class LobSequence extends AbstractDelegateSequence {

    protected Sequence delegate;

    protected LobSequence(Sequence delegate) {
	this.delegate = delegate;
	delegate.addObs(this);
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { delegate };
    }
    protected Object clone(Object[] params) {
	return new LobSequence((Sequence)params[0]);
    }

    protected Sequence getDelegateSequence() {
	return delegate;
    }

    protected void setDelegate(Sequence delegate) {
	this.delegate = delegate;
    }

    public static class Unmodifiable extends LobSequence {

	protected Unmodifiable(Sequence delegate) {
	    super(delegate);
	}

	public void add(Lob l) { 
	    throw new Error("can't add to unmodifiable sequence"); 
	}
	public void add(Lob l, Object key) { 
	    throw new Error("can't add to unmodifiable sequence"); 
	}
	public void add(Lob l, Object key, int index) { 
	    throw new Error("can't add to unmodifiable sequence"); 
	}
	public void remove(Lob l) {
	    throw new Error("can't remove from unmodifiable sequence"); 
	}
	public void clear() { 
	    throw new Error("can't clear unmodifiable sequence"); 
	}
    }
}
