/*   
MapModel.java
 *    
 *    Copyright (c) 2004, Matti J. Katila
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

public interface MapModel extends Map, Observable, Replaceable {

    class MapValueModel 
	extends AbstractModel.AbstractObjectModel {

	protected MapModel map;
	protected Model key;

	public MapValueModel(MapModel map, Model key) {
	    this.map = map; map.addObs(this);
	    this.key = key; key.addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { map, key };
	}
	protected Object clone(Object[] params) {
	    return new MapValueModel((MapModel)params[0], (Model)params[1]);
	}

	public Object get() {
	    return map.get(key.get());
	}

	public void set(Object value) {
	    map.put(key.get(), value);
	}
    }

    class Simple extends Delegate {
	protected final Map map;

	public Simple() {
	    this(new HashMap()); 
	}
	public Simple(Map map) {
	    this.map = map;
	}

	public Map getDelegate() {
	    return map;
	}


	// implement Replaceable

	public Set getTemplateParameters() {
	    Set s = new HashSet();
	    for(Iterator i=values().iterator(); i.hasNext();) {
		Object o = i.next();
		if(!(o instanceof Replaceable)) continue;
		s.addAll(((Replaceable)o).getTemplateParameters());
	    }

	    return s;
	}

	public Object getTemplateParameter(Object key) {
	    for(Iterator i=values().iterator(); i.hasNext();) {
		Object o = i.next();
		if(!(o instanceof Replaceable)) continue;
		Object value = ((Replaceable)o).getTemplateParameter(key);
		if(value != null) return value;
	    }
	    return null;
	}

	public void setTemplateParameter(Object key, Object value) {
	    for(Iterator i=values().iterator(); i.hasNext();) {
		Object o = i.next();
		if(!(o instanceof Replaceable)) continue;
		((Replaceable)o).setTemplateParameter(key, value);
	    }
	}

	public Object instantiateTemplate(java.util.Map map) {
	    if(map.get(identity) != null) return map.get(identity);

	    Map newMap = new HashMap(size());

	    /** XXX this could be more efficient -- first see whether there
	     *  are any Replaceables that change?
	     */
	
	    for(Iterator i=keySet().iterator(); i.hasNext();) {
		Object key = i.next();
		Object value = get(key);

		if(!(value instanceof Replaceable)) {
		    newMap.put(key, value);
		    continue;
		}

		Replaceable r = (Replaceable)value;
		Object __o;
		newMap.put(key, __o = r.instantiateTemplate(map));
	    }
	    
	    Map newThis = new Simple(newMap);
	    map.put(identity, newThis);
	    return newThis;
	}

	protected Object clone(Object[] newParams) {
	    throw new UnsupportedOperationException("MapModel.Simple "+
						    "doesn't use "+
						    "AbstractReplaceable "+
						    "infrastructure");
	}
	protected Replaceable[] getParams() {
	    throw new UnsupportedOperationException("MapModel.Simple "+
						    "doesn't use "+
						    "AbstractReplaceable "+
						    "infrastructure");
	}
    }

    abstract class Delegate 
	extends AbstractReplaceable.AbstractObservable implements MapModel {

	protected abstract Map getDelegate();

	protected final CollectionModel.IdentityWrapper identity;
	public Delegate() { 
	    this.identity = new CollectionModel.IdentityWrapper(this); 
	}

	public MapValueModel valueModel(Model key) {
	    return new MapValueModel(this, key);
	}
    
	public void clear() {
	    getDelegate().clear();
	    obses.trigger();
	}
	public boolean containsKey(Object o) {
	    return getDelegate().containsKey(o);
	}
	public boolean containsValue(Object o) {
	    return getDelegate().containsValue(o);
	}
	public Set entrySet() {
	    return getDelegate().entrySet(); // XXX modifications
	}
	public boolean equals(Object o) {
	    return getDelegate().equals(o);
	}
	public Object get(Object o) {
	    return getDelegate().get(o);
	}
	public int hashCode() {
	    return getDelegate().hashCode();
	}
	public boolean isEmpty() {
	    return getDelegate().isEmpty();
	}
	public Set keySet() {
	    return getDelegate().keySet(); // XXX modifications
	}
	public Object put(Object k, Object v) {
	    try { return getDelegate().put(k, v); }
	    finally { obses.trigger(); }
	}
	public void putAll(Map m) {
	    getDelegate().putAll(m);
	    obses.trigger();
	}
	public Object remove(Object k) {
	    try { return getDelegate().remove(k); }
	    finally { obses.trigger(); }
	}
	public int size() {
	    return getDelegate().size();
	}
	public Collection values() {
	    return getDelegate().values(); // XXX modifications
	}

	public String toString() {
	    String classname = getClass().getName();
	    classname = classname.substring(classname.lastIndexOf(".")+1);
	    return classname+"("+getDelegate().toString()+")";
	}
    }



    class ModelMapModel extends Delegate implements Obs {

	protected Model modelModel;
	protected MapModel currentModel;

	public ModelMapModel(Model modelModel) {
	    this.modelModel = modelModel;
	    currentModel = (MapModel)modelModel.get();
	    if(currentModel != null) currentModel.addObs(this);

	    modelModel.addObs(new Obs() { public void chg() {
		//currentModel.removeObs(this);
		currentModel = (MapModel)ModelMapModel.this.modelModel.get();
		if(currentModel != null) currentModel.addObs(this);
		obses.trigger();
	    }});
	}

	public void chg() { obses.trigger(); }

	protected Replaceable[] getParams() {
	    return new Replaceable[] { modelModel };
	}
	protected Object clone(Object[] params) {
	    return new ModelMapModel((Model)params[0]);
	}

	protected Map getDelegate() {
	    return currentModel;
	}

	protected CollectionModel clone(Collection newContents) {
	    throw new UnsupportedOperationException();
	}

	public String toString() {
	    return "ModelMapModel("+modelModel+")";
	}
    }

}
