/*
StateModel.java
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
package org.nongnu.libvob.lob;
import org.nongnu.libvob.fn.*;
import javolution.realtime.*;
import java.util.*;

public class StateModel extends RealtimeObject implements Model {

    public static final LocalContext.Variable STATE =
	new LocalContext.Variable(new HashMap());


    public static void enterSubState(Object key) {
	Map state = (Map)STATE.getValue();
	Map nstate = (Map)state.get(key);
	if(nstate == null) {
	    nstate = new HashMap();
	    state.put(key, nstate);
	}
	STATE.setValue(nstate);
    }


    private String name;
    private Object _default;
    private Map state;

    private StateModel() {}

    public static StateModel newInstance(String name) {
	return newInstance(name, null);
    }

    public static StateModel newInstance(String name, int _default) {
	FastInt i = FastInt.newInstance(_default);
	i.move(ObjectSpace.HEAP);
	return newInstance(name, i);
    }

    public static StateModel newInstance(String name, Object _default) {
	StateModel m = (StateModel)FACTORY.object();
	m.name = name;
	m._default = _default;
	m.state = (Map)STATE.getValue();
	return m;
    }

    public Object get() {
	//Map state = (Map)STATE.getValue();
	if(state.get(name) == null) return _default;
	return state.get(name);
    }

    public int getInt() {
	return ((FastInt)get()).intValue();
    }
    
    public void set(Object value) {
    	if(value instanceof Realtime) {
	    ((Realtime)value).move(ObjectSpace.HEAP);
	}

	//Map state = (Map)STATE.getValue();
	state.put(name, value);
    }

    public void set(int value) {
	set(FastInt.newInstance(value));
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new StateModel();
	    }
	};
}
