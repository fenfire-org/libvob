/*   
Instance.java
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
import java.util.*;

public class Instance extends AbstractModel.AbstractObjectModel {

    protected Replaceable template;
    protected MapModel parameters;

    protected Replaceable instance;

    public Instance(Replaceable template, MapModel parameters) {
	this.template = template;
	this.parameters = parameters; 

	parameters.addObs(this);

	if(template instanceof Observable) ((Observable)template).addObs(this);
    }


    public static Lob lob(Lob template, MapModel parameters) {
	return new ModelLob(new Instance(template, parameters));
    }
    public static Model model(Model template, MapModel parameters) {
	return new ModelModel(new Instance(template, parameters));
    }
    public static ListModel listModel(ListModel template, MapModel params) {
	return new ListModel.ModelListModel(new Instance(template, params));
    }
    public static MapModel mapModel(MapModel template, MapModel params) {
	return new MapModel.ModelMapModel(new Instance(template, params));
    }
    public static Action action(Action template, MapModel parameters) {
	return new ModelAction(new Instance(template, parameters));
    }


    public Replaceable[] getParams() {
	return new Replaceable[] { template, parameters };
    }
    public Object clone(Object[] params) {
	return new Instance((Replaceable)params[0], (MapModel)params[1]);
    }

    protected void update() {
	Map params = new HashMap(parameters);
	instance = (Replaceable)template.instantiateTemplate(params);
	if(instance == null) throw new NullPointerException();
    }
	
    public void chg() {
	instance = null;
	obses.trigger();
    }

    public Object get() {
	if(instance == null) update();
	return instance;
    }



    public static class RecursionInstance extends Instance {
	public RecursionInstance(Replaceable template, MapModel parameters) {
	    super(template, parameters);
	}
	public Replaceable[] getParams() {
	    return new Replaceable[] { parameters };
	}
	public Object clone(Object[] params) {
	    return new RecursionInstance(template, (MapModel)params[0]);
	}
    }
}
