/*
LobLob.java
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
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

/** A lob that is defined by an internal structure of lobs.
 *  XXX better name?
 */
public class LobLob extends AbstractDelegateLob {

    protected Lob delegate;
    protected Set parameters;

    protected LobLob() {
    }

    public LobLob(Lob delegate) {
	setDelegate(delegate);
    }

    protected Object clone(Object[] newParams) {
	throw new UnsupportedOperationException("LobLob doesn't use "+
						"AbstractReplaceable "+
						"infrastructure");
    }
    protected Replaceable[] getParams() {
	throw new UnsupportedOperationException("LobLob doesn't use "+
						"AbstractReplaceable "+
						"infrastructure");
    }

    protected Lob clone(Lob delegate, Map map) {
	return new LobLob(delegate);
    }

    protected Lob getDelegate() {
	return delegate;
    }

    protected void setDelegate(Lob delegate) {
	this.delegate = delegate;
	this.parameters = delegate.getTemplateParameters();
	delegate.addObs(this);
    }

    public Object getParameter(Object key) {
	return delegate.getTemplateParameter(key);
    }
    public void setParameter(Object key, Object value) {
	delegate.setTemplateParameter(key, value);
    }

    public Object getTemplateParameter(Object key) {
	for(Iterator i=parameters.iterator(); i.hasNext();) {
	    Replaceable param = (Replaceable)delegate.getTemplateParameter(i.next());
	    Object o = param.getTemplateParameter(key);
	    if(o != null) return o;
	}

	return null;
    }

    public void setTemplateParameter(Object key, Object value) {
	for(Iterator i=parameters.iterator(); i.hasNext();) {
	    Replaceable param = (Replaceable)delegate.getTemplateParameter(i.next());
	    param.setTemplateParameter(key, value);
	}
    }

    public Set getTemplateParameters() {
	Set s = new HashSet();

	for(Iterator i=parameters.iterator(); i.hasNext();) {
	    Replaceable param = (Replaceable)delegate.getTemplateParameter(i.next());
	    if(param != null) 
		s.addAll(param.getTemplateParameters());
	}

	return s;
    }

    public Object instantiateTemplate(java.util.Map map) {
	if(map.get(this) != null) return map.get(this);

	Map myMap = new HashMap();

	for(Iterator i=parameters.iterator(); i.hasNext();) {
	    Object key=i.next();

	    Replaceable param = (Replaceable)delegate.getTemplateParameter(key);
	    if(param != null) {
		Object newParam = param.instantiateTemplate(map);
		if(newParam != param)
		    myMap.put(key, newParam);
	    }
	}

	Lob newDelegate = (Lob)delegate.instantiateTemplate(myMap);
	if(newDelegate == delegate) return this;

	Lob newThis = clone(newDelegate, map);
	map.put(this, newThis);
	return newThis;
    }


    public String toString() {
	Set params = new TreeSet(delegate.getTemplateParameters());

	String classname = getClass().getName();
	StringBuffer buf = new StringBuffer(classname.substring(classname.lastIndexOf(".")+1));
	buf.append("(");
	for(Iterator i=params.iterator(); i.hasNext();) {
	    Object key = i.next();
	    Object value = delegate.getTemplateParameter(key);

	    buf.append(key.toString());
	    buf.append(" = ");
	    buf.append(value.toString());

	    if(i.hasNext())
		buf.append(", ");
	}
	buf.append(")");

	return buf.toString();
    }
}
