/*
AbstractAction.java
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

/**
 *  The most important use for this abstract class is being able to write 
 *  anonymous inner classes:
 *  <pre>
 *      new AbstractAction() { public void run() { ... } }
 *  </pre>
 */
public abstract class AbstractAction extends AbstractReplaceable 
    implements Action {
    
    public abstract void run();

    protected Replaceable[] getParams() { return NO_PARAMS; }
    protected Object clone(Object[] params) { return this; }


    public static class Inline extends AbstractAction {

	protected void run(Object[] params) {
	    throw new UnsupportedOperationException("override this!");
	}

	private Replaceable[] params;
	private Inline inline0;

	public Inline(Object p1) {
	    this(new Object[] { p1 });
	}
	public Inline(Object p1, Object p2) {
	    this(new Object[] { p1, p2 });
	}
	public Inline(Object p1, Object p2, Object p3) {
	    this(new Object[] { p1, p2, p3 });
	}
	public Inline(Object[] _params) {
	    this(_params, null);
	}

	private Inline(Object[] _params, Inline inline0) {
	    this.params = new Replaceable[_params.length];
	    for(int i=0; i<params.length; i++) 
		params[i] = (Replaceable)_params[i];

	    if(inline0 != null)
		this.inline0 = inline0;
	    else
		this.inline0 = this;
	}

	public void run() {
	    inline0.run(params);
	}

	protected Replaceable[] getParams() { return params; }

	protected Object clone(Object[] params) {
	    return new Inline(params, this);
	}
    }
}
