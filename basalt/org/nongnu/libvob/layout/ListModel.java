/*   
ListModel.java
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

public interface ListModel extends CollectionModel, List {

    void startUpdate(); // XXX hmmm
    void endUpdate(); 

    class Simple extends CloneableDelegate {
	protected final List list;

	protected List getListDelegate() { return list; }
	
	public Simple(List list) {
	    this.list = list;
	}

	public Simple(Object[] elems) {
	    this(new ArrayList(Arrays.asList(elems)));
	}
	
	public Simple() {
	    this(new ArrayList());
	}

	protected CollectionModel clone(Collection newCollection) {
	    return new ListModel.Simple(new ArrayList(newCollection));
	}

	protected void changeMethodCalled() {
	    obses.trigger();
	}
    }

    abstract class Delegate extends CollectionModel.Delegate 
	implements ListModel {

	protected abstract List getListDelegate();

	public void startUpdate() { obses.startUpdate(); }
	public void endUpdate() { obses.endUpdate(); }

	protected Collection getDelegate() { return getListDelegate(); }

	protected class ListModelIterator implements ListIterator {
	    protected ListIterator i;
	    protected ListModelIterator(ListIterator i) { this.i = i; }
	    public void add(Object o) { i.add(o); changeMethodCalled(); }
	    public boolean hasNext() { return i.hasNext(); }
	    public boolean hasPrevious() { return i.hasPrevious(); }
	    public Object next() { return i.next(); }
	    public int nextIndex() { return i.nextIndex(); }
	    public Object previous() { return i.previous(); }
	    public int previousIndex() { return i.previousIndex(); }
	    public void remove() { i.remove(); changeMethodCalled(); }
	    public void set(Object o) { i.set(o); changeMethodCalled(); }
	}
	
	protected class SubListModel extends ListModel.Simple {
	    protected SubListModel(List list) {
		super(list);
	    }
	    public void addObs(Obs obs) {
		throw new UnsupportedOperationException();
	    }
	    public void removeObs(Obs obs) {
		throw new UnsupportedOperationException();
	    }
	    protected void changeMethodCalled() {
		ListModel.Delegate.this.changeMethodCalled();
	    }
	}

	public Model containsModel(Model m) {
	    return new ContainsModel(this, m);
	}

	public void add(int index, Object o) {
	    getListDelegate().add(index, o);
	    changeMethodCalled();
	}
	public boolean addAll(int index, Collection c) {
	    boolean result = getListDelegate().addAll(index, c);
	    changeMethodCalled();
	    return result;
	}
	public Object get(int index) {
	    return getListDelegate().get(index);
	}
	public int indexOf(Object o) {
	    return getListDelegate().indexOf(o);
	}
	public int lastIndexOf(Object o) {
	    return getListDelegate().lastIndexOf(o);
	}
	public ListIterator listIterator() {
	    return new ListModelIterator(getListDelegate().listIterator());
	}
	public ListIterator listIterator(int index) {
	    return new ListModelIterator(getListDelegate().listIterator(index));
	}
	public Object remove(int index) {
	    Object result = getListDelegate().remove(index);
	    changeMethodCalled();
	    return result;
	}
	public Object set(int index, Object o) {
	    Object result = getListDelegate().set(index, o);
	    changeMethodCalled();
	    return result;
	}
	public int size() {
	    return getListDelegate().size();
	}
	public List subList(int fromIndex, int toIndex) {
	    return new SubListModel(getListDelegate().subList(fromIndex, toIndex));
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


    abstract class AbstractListModel extends AbstractList
	implements ListModel, Observable, Replaceable, Obs {

	protected ObsSet obses = new ObsSet();

	protected final IdentityWrapper identity;

	public AbstractListModel() { 
	    this.identity = new IdentityWrapper(this); 
	}
	

	public Model containsModel(Model m) {
	    return new ContainsModel(this, m);
	}

	public void addObs(Obs o) {
	    obses.addObs(o);
	}
	public void removeObs(Obs o) {
	    obses.removeObs(o);
	}

	public void chg() {
	    obses.trigger();
	}

	
	public void startUpdate() {
	    obses.startUpdate();
	}

	public void endUpdate() {
	    obses.endUpdate();
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

	protected Object clone(Object[] newParams) {
	    throw new UnsupportedOperationException("AbstractListModel doesn't use "+
						    "AbstractReplaceable "+
						    "infrastructure");
	}
	protected Replaceable[] getParams() {
	    throw new UnsupportedOperationException("AbstractListModel doesn't use "+
						    "AbstractReplaceable "+
						    "infrastructure");
	}

	public String toString() {
	    try {
		return AbstractReplaceable.toString(getParams(), getClass());
	    } catch(Throwable t) {
		return super.toString();
	    }
	}
    }


    Object
	PARAM = "XXX/TransformListModel/param";

    class Transform extends AbstractListModel {
	protected ListModel model;
	protected Replaceable template;
	protected List cache = new ArrayList();
	
	public Transform(ListModel model, Replaceable template) {
	    this.model = model;
	    this.template = template;
	    
	    model.addObs(this); 
	    chg();
	}

	protected Replaceable[] getParams() { 
	    return new Replaceable[] { model, template };
	}
	protected Object clone(Object[] params) { 
	    return new Transform((ListModel)params[0], (Replaceable)params[1]);
	}

	public int size() {
	    return cache.size();
	}
	public Object get(int index) {
	    return cache.get(index);
	}
	public Object remove(int index) {
	    Object old = cache.get(index);
	    model.remove(index);
	    return old;
	}

	boolean updateInProgress = false;
	public void chg() {
	    if(updateInProgress) throw new Error("recursive update");
	    updateInProgress = true;
	    Map params = new HashMap();
	    
	    cache.clear();

	    for(int i=0; i<model.size(); i++) {
		params.clear();
		Object o = model.get(i);
		if(o instanceof Replaceable)
		    params.put(PARAM, o);
		else
		    params.put(PARAM, new ObjectModel(o));

		cache.add(template.instantiateTemplate(params));
	    }

	    updateInProgress = false;
	    obses.trigger();
	}

	public String toString() {
	    return "<ListModel.Transform("+super.toString()+")>";
	}
    }




    class Concat extends AbstractListModel {
	
	protected ListModel models;
	protected int sizes[];

	public Concat(ListModel m1, ListModel m2) {
	    this(new ListModel.Simple(new Object[] { m1, m2 }));
	}
	public Concat(ListModel m1, ListModel m2, ListModel m3) {
	    this(new ListModel.Simple(new Object[] { m1, m2, m3 }));
	}

	public Concat(final ListModel models) {
	    this.models = models;

	    Obs o = new Obs() { public void chg() {
		sizes = new int[models.size()];
		for(int i=0; i<sizes.length; i++)
		    model(i).addObs(Concat.this);
		Concat.this.chg();
	    }};

	    o.chg();
	    models.addObs(o);
	}

	protected Replaceable[] getParams() { 
	    return new Replaceable[] { models };
	}
	protected Object clone(Object[] params) { 
	    return new Concat((ListModel)params[0]);
	}

	protected ListModel model(int i) {
	    return (ListModel)models.get(i);
	}

	public int size() { 
	    int s = 0;
	    for(int i=0; i<sizes.length; i++) s += sizes[i];
	    return s;
	}

	public Object get(int index) { 
	    int rel_idx = index;
	    for(int i=0; i<sizes.length; i++) {
		if(rel_idx < sizes[i])
		    return model(i).get(rel_idx);

		rel_idx -= sizes[i];
	    }
	    throw new IndexOutOfBoundsException(""+index);
	}

	public void chg() {
	    for(int i=0; i<sizes.length; i++)
		sizes[i] = model(i).size();

	    obses.trigger();
	}
    }



    class ModelListModel extends Delegate {

	protected Model modelModel;
	protected ListModel currentModel;

	public ModelListModel(Model modelModel) {
	    this.modelModel = modelModel;
	    currentModel = (ListModel)modelModel.get();
	    if(currentModel != null) currentModel.addObs(this);

	    modelModel.addObs(new Obs() { public void chg() {
		//currentModel.removeObs(this);
		currentModel = (ListModel)ModelListModel.this.modelModel.get();
		if(currentModel != null) currentModel.addObs(this);
		obses.trigger();
	    }});
	}

	public void startUpdate() { obses.startUpdate(); }
	public void endUpdate() { obses.endUpdate(); }

	protected Replaceable[] getParams() {
	    return new Replaceable[] { modelModel };
	}
	protected Object clone(Object[] params) {
	    return new ModelListModel((Model)params[0]);
	}

	protected List getListDelegate() {
	    return currentModel;
	}

	protected CollectionModel clone(Collection newContents) {
	    throw new UnsupportedOperationException();
	}
    }


    class IndexModel extends AbstractModel.AbstractIntModel implements Obs {
	protected ListModel model;
	protected Model element;

	public IndexModel(ListModel model, Model element) {
	    this.model = model;
	    this.element = element;

	    model.addObs(this); element.addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { model, element };
	}
	protected Object clone(Object[] params) {
	    return new IndexModel((ListModel)params[0], (Model)params[1]);
	}

	public void chg() {
	    super.chg();
	}

	public int getInt() {
	    return model.indexOf(element.get());
	}
	public void setInt(int value) {
	    element.set(model.get(value));
	}
    }


    /** Join([1,2,3,4,5], X) = [1,X,2,X,3,X,4,X,5].
     *  Insipired by Python's string.join(); to archieve the same effect
     *  as string.join(l,s), use TextModel.Concat(ListModel.Join(l,O(s))),
     *  where l is a list of text models, s is a text model to place
     *  between the elements of l, and O is an ObjectModel containing s.
     */
    class Join extends AbstractListModel {

	protected ListModel model;
	protected Model inBetween;

	public Join(ListModel model, Model inBetween) {
	    this.model = model;
	    this.inBetween = inBetween;

	    model.addObs(this); inBetween.addObs(this);
	}

	protected Replaceable[] getParams() { 
	    return new Replaceable[] { model, inBetween };
	}
	protected Object clone(Object[] params) {
	    return new Join((ListModel)params[0], (Model)params[1]);
	}

	public Object get(int index) {
	    if(index % 2 == 0)
		return model.get(index/2);
	    else
		return inBetween.get();
	}

	public int size() {
	    int s = model.size();
	    if(s == 0)
		return 0;
	    else
		return 2*s-1;
	}
    }


    class ListCache extends AbstractListModel {

	protected CollectionModel model;
	protected List cache;

	public ListCache(CollectionModel model) {
	    this.model = model;
	    model.addObs(this);

	    cache = new ArrayList(model);
	}

	protected Replaceable[] getParams() { 
	    return new Replaceable[] { model };
	}
	protected Object clone(Object[] params) {
	    return new ListCache((CollectionModel)params[0]);
	}

	public void chg() {
	    cache.clear();
	    cache.addAll(model);
	    obses.trigger();
	}

	public Object get(int index) {
	    return cache.get(index);
	}

	public int size() {
	    return cache.size();
	}
    }
}
