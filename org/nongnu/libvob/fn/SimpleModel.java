/*
SimpleModel.java
 *    
 *    Copyright (c) 2005 Benja Fallenstein
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
package org.nongnu.libvob.fn;
import javolution.realtime.*;
import java.util.*;

public class SimpleModel extends RealtimeObject implements Model {

    private Object value;

    private SimpleModel() {}

    public static SimpleModel newInstance() {
	return newInstance(null);
    }

    public static SimpleModel newInstance(int value) {
	return newInstance(FastInt.newInstance(value));
    }

    public static SimpleModel newInstance(Object value) {
	SimpleModel m = (SimpleModel)FACTORY.object();
	m.value = value;
	return m;
    }

    public Object get() {
	return value;
    }

    public int getInt() {
	return ((Number)value).intValue();
    }
    
    public void set(Object value) {
	this.value = value;
    }

    public void set(int value) {
	set(FastInt.newInstance(value));
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    if(value instanceof Realtime) {
		((Realtime)value).move(os);
	    }
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new SimpleModel();
	    }
	};
}
