/*   
ObjectModel.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *
 *    This file is part of Libvob.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *
 */
package org.nongnu.libvob.layout;
import org.nongnu.navidoc.util.*;
import java.util.*;

public class ObjectModel extends AbstractModel.AbstractObjectModel {

    protected Object value;

    public ObjectModel() {
	this(null);
    }

    public ObjectModel(Object value) { 
	this.value = value; 
    }

    public Object get() { 
	return value;
    }
    
    public void set(Object value) {
	if(this.value == value) return;
	if(this.value != null && this.value.equals(value)) return;
	this.value = value;
	obses.trigger();
    }

    public String toString() {
	return "ObjectModel("+value+")";
    }


    public static class StaticModel extends ObjectModel {
	public StaticModel(Replaceable value) {
	    super(value);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { (Replaceable)value };
	}
	protected Object clone(Object[] params) {
	    return new StaticModel((Replaceable)params[0]);
	}

	public String toString() {
	    return "StaticModel("+value+")";
	}
    }
}
