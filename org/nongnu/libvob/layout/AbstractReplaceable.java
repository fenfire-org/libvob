/*
AbstractReplaceable.java
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
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public abstract class AbstractReplaceable implements Replaceable {

    /** The code in this class is DUPLICATED in 
     *  CollectionModel.AbstractCollectionModel, 
     *  because Java doesn't support multiple 
     *  inheritance. Please
     *
     *       MAKE CHANGES IN BOTH PLACES!
     */


    protected static final Replaceable[] NO_PARAMS = new Replaceable[] {};

    protected abstract Object clone(Object[] newParams);
    protected abstract Replaceable[] getParams();


    public Set getTemplateParameters() {
	Replaceable[] params = getParams();
	if(params.length == 0) return Collections.EMPTY_SET;
	if(params.length == 1) return params[0].getTemplateParameters();

	Set parameters = new HashSet();
	for(int i=0; i<params.length; i++) {
	    if(params[i] != null)
		parameters.addAll(params[i].getTemplateParameters());
	}

	return parameters;
    }

    public Object getTemplateParameter(Object key) {
	Replaceable[] params = getParams();
	for(int i=0; i<params.length; i++) {
	    if(params[i] == null) continue;
	    Object value = params[i].getTemplateParameter(key);
	    if(value != null) return value;
	}
	return null;
    }

    public void setTemplateParameter(Object key, Object value) {
	Replaceable[] params = getParams();
	for(int i=0; i<params.length; i++)
	    if(params[i] != null)
		params[i].setTemplateParameter(key, value);
    }

    public Object instantiateTemplate(java.util.Map map) {
	if(map.get(this) != null) return map.get(this);

	Replaceable[] params = getParams();
	for(int i=0; i<params.length; i++) {
	    if(params[i] == null) continue;

	    Object newParam = params[i].instantiateTemplate(map);
	    if(newParam != params[i]) {
		Object[] newParams = new Object[params.length];
		System.arraycopy(params, 0, newParams, 0, i);
		newParams[i] = newParam;

		for(i++; i<params.length; i++) {
		    if(params[i] != null)
			newParams[i] = params[i].instantiateTemplate(map);
		}

		Object newThis = clone(newParams);
		map.put(this, newThis);
		return newThis;
	    }
	}

	return this;
    }

    public String toString() {
	try {
	    return toString(getParams(), getClass());
	} catch(Throwable t) {
	    return super.toString();
	}
    }

    public static String toString(Replaceable[] params, Class klass) {
	String classname = klass.getName();
	StringBuffer buf = new StringBuffer(classname.substring(classname.lastIndexOf(".")+1));
	buf.append("(");
	for(int i=0; i<params.length; i++) {
	    buf.append(""+params[i]);
	    if(i<params.length-1)
		buf.append(", ");
	}
	buf.append(")");

	return buf.toString();
    }


    public static abstract class AbstractObservable 
	extends AbstractReplaceable implements Observable, Obs {

	protected ObsSet obses = new ObsSet();
	public void addObs(Obs o) { obses.addObs(o); }
	public void removeObs(Obs o) { obses.removeObs(o); }
	public void chg() { obses.trigger(); }
    }
}
