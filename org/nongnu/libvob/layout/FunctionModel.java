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
    
    public static abstract class Fn {
	protected abstract Model getModel(Object value);
    }


    protected Model model;
    protected Fn fn;

    protected Model currentModel;

    public FunctionModel(final Model model, final Fn fn) {
	this.fn = fn;
	this.model = model;

	Obs o = new Obs() { public void chg() {
	    if(currentModel != null)
		currentModel.removeObs(FunctionModel.this);
	    currentModel = fn.getModel(model.get());
	    currentModel.addObs(FunctionModel.this);

	    obses.trigger();
	}};

	model.addObs(o);
	o.chg();
    }

    public FunctionModel(Object modelKey, Fn fn) {
	this(Parameter.model(modelKey, null), fn);
    }


    public Replaceable[] getParams() {
	return new Replaceable[] { model };
    }
    public Object clone(Object[] params) {
	return new FunctionModel((Model)params[0], fn);
    }


    public Object get() {
	return currentModel.get();
    }

    public void set(Object value) {
	currentModel.set(value);
    }
}
