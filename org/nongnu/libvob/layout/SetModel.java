/*   
SetModel.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein
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

public interface SetModel extends CollectionModel, Set {

    class Simple extends CollectionModel.Simple implements SetModel {
	public Simple(Set set) {
	    super(set);
	}

	public Simple(Object[] elems) {
	    this(new HashSet(Arrays.asList(elems)));
	}
	
	public Simple() {
	    this(new HashSet());
	}

	protected CollectionModel clone(Collection newCollection) {
	    return new SetModel.Simple(new HashSet(newCollection));
	}
    }

    abstract class Delegate extends CollectionModel.Delegate 
	implements SetModel {
    }

    abstract class AbstractSetModel 
	extends CollectionModel.AbstractCollectionModel implements SetModel {
    }

    abstract class Cached extends Delegate {
	protected Set cache = new HashSet();
	protected boolean current = false;

	protected abstract void updateCache();

	protected Collection getDelegate() { 
	    if(!current) {
		updateCache();
		current = true;
	    }

	    return cache; 
	}

	public void chg() {
	    current = false;
	    super.chg();
	}
    }

    class SetCache extends Cached {

	protected CollectionModel model;

	public SetCache(CollectionModel model) {
	    this.model = model;
	    model.addObs(this);
	}

	protected Replaceable[] getParams() { 
	    return new Replaceable[] { model };
	}
	protected Object clone(Object[] params) {
	    return new SetCache((CollectionModel)params[0]);
	}

	public void updateCache() {
	    cache.clear();
	    cache.addAll(model);
	}
    }

    /** Set of things that are in a collection A but not in a collection B,
     *  i.e. A\B.
     *  XXX changes to the set should propagate to the underlying model
     *  in the right way...
     */
    class Difference extends Cached {

	protected CollectionModel collection, remove;

	public Difference(CollectionModel collection, CollectionModel remove) {
	    this.collection = collection; this.remove = remove;
	    collection.addObs(this); remove.addObs(this);
	}

	protected Replaceable[] getParams() { 
	    return new Replaceable[] { collection, remove };
	}
	protected Object clone(Object[] params) {
	    return new Difference((CollectionModel)params[0],
				  (CollectionModel)params[1]);
	}

	public void updateCache() {
	    cache.clear();
	    cache.addAll(collection);
	    cache.removeAll(remove);
	}
    }
}
