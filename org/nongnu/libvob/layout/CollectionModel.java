/*   
CollectionModel.java
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

public interface CollectionModel extends Collection, Observable, Replaceable {


    class Simple extends CloneableDelegate {

	protected final Collection collection;

	public Simple(Collection collection) {
	    this.collection = collection;
	}

	protected CollectionModel clone(Collection newCollection) {
	    return new Simple(newCollection);
	}

	protected Collection getDelegate() { return collection; }

	protected void changeMethodCalled() {
	    obses.trigger();
	}
    }

    abstract class Delegate extends AbstractReplaceable.AbstractObservable
	implements CollectionModel, Obs {

	protected abstract Collection getDelegate();

	protected final IdentityWrapper identity;
	public Delegate() { this.identity = new IdentityWrapper(this); }

	protected void changeMethodCalled() {
	}

	public Model sizeModel() {
	    AbstractModel m = new AbstractModel.AbstractIntModel() {
		    public int getInt() {
			return size();
		    }
		};
	    addObs(m);
	    return m;
	}
	
	public boolean add(Object o) {
	    boolean result = getDelegate().add(o);
	    changeMethodCalled();
	    return result;
	}
	public boolean addAll(Collection c) {
	    boolean result = getDelegate().addAll(c);
	    changeMethodCalled();
	    return result;
	}
	public void clear() {
	    getDelegate().clear();
	    changeMethodCalled();
	}
	public boolean contains(Object o) {
	    return getDelegate().contains(o);
	}
	public boolean containsAll(Collection c) {
	    return getDelegate().containsAll(c);
	}
	public boolean equals(Object o) {
	    return getDelegate().equals(o);
	}
	public int hashCode() {
	    return getDelegate().hashCode();
	}
	public boolean isEmpty() {
	    return getDelegate().isEmpty();
	}
	public Iterator iterator() {
	    final Iterator i = getDelegate().iterator();
	    return new Iterator() {
		    public boolean hasNext() { return i.hasNext(); }
		    public Object next() { return i.next(); }
		    public void remove() { i.remove(); changeMethodCalled(); }
		};
	}
	public boolean remove(Object o) {
	    boolean result = getDelegate().remove(o);
	    changeMethodCalled();
	    return result;
	}
	public boolean removeAll(Collection c) {
	    boolean result = getDelegate().removeAll(c);
	    changeMethodCalled();
	    return result;
	}
	public boolean retainAll(Collection c) {
	    boolean result = getDelegate().retainAll(c);
	    changeMethodCalled();
	    return result;
	}
	public int size() {
	    return getDelegate().size();
	}
	public Object[] toArray() {
	    return getDelegate().toArray();
	}
	public Object[] toArray(Object[] a) {
	    return getDelegate().toArray(a);
	}
	
	public String toString() {
	    String classname = getClass().getName();
	    classname = classname.substring(classname.lastIndexOf(".")+1);
	    return classname+"("+getDelegate().toString()+")";
	}
    }

    abstract class CloneableDelegate extends Delegate {
	protected abstract CollectionModel clone(Collection newContents);

	public Set getTemplateParameters() {
	    Set s = new HashSet();
	    for(Iterator i=getDelegate().iterator(); i.hasNext();) {
		Object o = i.next();
		if(!(o instanceof Replaceable)) continue;
		s.addAll(((Replaceable)o).getTemplateParameters());
	    }

	    return s;
	}

	public Object getTemplateParameter(Object key) {
	    for(Iterator i=getDelegate().iterator(); i.hasNext();) {
		Object o = i.next();
		if(!(o instanceof Replaceable)) continue;
		Object value = ((Replaceable)o).getTemplateParameter(key);
		if(value != null) return value;
	    }
	    return null;
	}

	public void setTemplateParameter(Object key, Object value) {
	    for(Iterator i=getDelegate().iterator(); i.hasNext();) {
		Object o = i.next();
		if(!(o instanceof Replaceable)) continue;
		((Replaceable)o).setTemplateParameter(key, value);
	    }
	}

	public Object instantiateTemplate(java.util.Map map) {
	    if(map.get(identity) != null) return map.get(identity);

	    List newCollection = new ArrayList(size());

	    /** XXX this could be more efficient -- first see whether there
	     *  are any Replaceables that change?
	     */
	
	    for(Iterator i=getDelegate().iterator(); i.hasNext();) {
		Object o = i.next();
		if(!(o instanceof Replaceable)) {
		    newCollection.add(o);
		    continue;
		}

		Replaceable r = (Replaceable)o;
		newCollection.add(r.instantiateTemplate(map));
	    }
	    
	    CollectionModel newThis = clone(newCollection);
	    map.put(identity, newThis);
	    return newThis;
	}

	protected Object clone(Object[] newParams) {
	    throw new UnsupportedOperationException("CollectionModel.Simple "+
						    "doesn't use "+
						    "AbstractReplaceable "+
						    "infrastructure");
	}
	protected Replaceable[] getParams() {
	    throw new UnsupportedOperationException("CollectionModel.Simple "+
						    "doesn't use "+
						    "AbstractReplaceable "+
						    "infrastructure");
	}
    }


    abstract class AbstractCollectionModel extends AbstractCollection
	implements CollectionModel, Observable, Replaceable, Obs {

	protected ObsSet obses = new ObsSet();

	protected abstract Object clone(Object[] newParams);
	protected abstract Replaceable[] getParams();

	protected final IdentityWrapper identity;

	public AbstractCollectionModel() { 
	    this.identity = new IdentityWrapper(this); 
	}
	

	public void addObs(Obs o) {
	    obses.addObs(o);
	}
	public void removeObs(Obs o) {
	    obses.removeObs(o);
	}

	protected void trigger() {
	    obses.trigger();
	}

	public void chg() {
	    trigger();
	}


	/** From here on, the rest of the code is DUPLICATED from
	 *  AbstractReplaceable, because Java doesn't support
	 *  multiple inheritance. Please MAKE CHANGES IN BOTH PLACES!
	 */

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
	    if(map.get(identity) != null) return map.get(identity);

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
		    map.put(identity, newThis);
		    return newThis;
		}
	    }

	    return this;
	}
    }


    class SizeModel extends AbstractModel.AbstractIntModel implements Obs {
	protected CollectionModel model;

	public SizeModel(CollectionModel model) {
	    this.model = model;
	    model.addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { model };
	}
	protected Object clone(Object[] params) {
	    return new SizeModel((CollectionModel)params[0]);
	}

	public void chg() {
	    super.chg();
	}

	public int getInt() {
	    return model.size();
	}
    }


    final class IdentityWrapper {
	private Object o;
	public IdentityWrapper(Object o) { this.o = o; }
	public int hashCode() { return System.identityHashCode(o); }
	public boolean equals(Object other) {
	    if(!(other instanceof IdentityWrapper)) return false;
	    return o == ((IdentityWrapper)other).o;
	}
	public String toString() {
	    return "IdentityWrapper("+o+")";
	}
    }
}
