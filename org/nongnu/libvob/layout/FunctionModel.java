/*   
FunctionModel.java
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

public class FunctionModel extends AbstractModel.AbstractObjectModel {
    
    protected Model model;
    protected FunctionModel function;

    protected Object currentValue;

    protected Object f(Object o, Obs obs) {
	throw new Error("override this!");
    }

    protected Object inv(Object o, Obs obs) { // inverse
	throw new UnsupportedOperationException("setting not supported");
    }

    private class WrapperModel extends AbstractModel.AbstractObjectModel {
	private Replaceable value;
	private WrapperModel(Replaceable value) {
	    this.value = value;
	    if(value instanceof Observable)
		((Observable)value).addObs(this);
	}

	public Object get() {
	    return value;
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { value };
	}
	protected Object clone(Object[] params) {
	    return new WrapperModel((Replaceable)params[0]);
	}
    }

    public FunctionModel(Replaceable o) {
	if(o instanceof Model) {
	    this.model = (Model)o;
	} else {
	    this.model = new WrapperModel(o);
	}

	this.function = this;
	init();
    }

    public FunctionModel(Model model, FunctionModel function) {
	this.model = model;
	this.function = function;
	init();
    } 

    private void init() {
	Obs o = new Obs() { public void chg() {
	    currentValue = function.f(model.get(), this);
	    obses.trigger();
	}};

	model.addObs(o);
	o.chg();
    }

    public Replaceable[] getParams() {
	return new Replaceable[] { model };
    }
    public Object clone(Object[] params) {
	return new FunctionModel((Model)params[0], function);
    }


    public Object get() {
	return currentValue;
    }

    public void set(Object value) {
	model.set(function.inv(value, this));
    }
}
