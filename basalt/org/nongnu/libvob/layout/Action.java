/*
Action.java
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

public interface Action extends Replaceable {
    
    void run();


    Action NULL = new Null();

    class Null extends AbstractReplaceable implements Action {
	public void run() {}

	protected Replaceable[] getParams() { return NO_PARAMS; }
	protected Object clone(Object[] params) { return this; }
    }

    class Concat extends AbstractReplaceable implements Action {
	protected Action a1, a2;
	public Concat(Action a1, Action a2) { this.a1 = a1; this.a2 = a2; }
	public void run() {
	    a1.run();
	    a2.run();
	}
	protected Replaceable[] getParams() {
	    return new Replaceable[] { a1, a2 };
	}
	protected Object clone(Object[] params) {
	    return new Concat((Action)params[0], (Action)params[1]);
	}
    }
}
