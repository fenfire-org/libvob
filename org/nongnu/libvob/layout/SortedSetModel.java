/*   
SortedSetModel.java
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

public interface SortedSetModel extends SetModel, SortedSet {

    /*
    class Simple extends SetModel.Simple implements SortedSetModel {
	protected final SortedSet set;

	public Simple(SortedSet set) {
	    super(set);
	    this.set = set;
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
    */

    abstract class Delegate extends SetModel.Delegate 
	implements SortedSetModel {

	protected abstract SortedSet getSortedSetDelegate();

	protected Collection getDelegate() {
	    return getSortedSetDelegate();
	}

	public Comparator comparator() { 
	    return getSortedSetDelegate().comparator();
	}
	public Object first() {
	    return getSortedSetDelegate().first();
	}
	public Object last() {
	    return getSortedSetDelegate().last();
	}

	public SortedSet headSet(Object toElement) {
	    throw new UnsupportedOperationException("not implemented");
	}
	public SortedSet subSet(Object fromElement, Object toElement) {
	    throw new UnsupportedOperationException("not implemented");
	}
	public SortedSet tailSet(Object fromElement) {
	    throw new UnsupportedOperationException("not implemented");
	}
    }

    /*
    abstract class AbstractSortedSetModel 
	extends SetModel.AbstractSetModel implements SortedSetModel {
    }
    */

    class SortedSetCache extends Delegate {

	protected CollectionModel model;
	protected Comparator comparator;
	protected SortedSet cache;

	protected SortedSet getSortedSetDelegate() { return cache; }

	public SortedSetCache(CollectionModel model) {
	    this(model, null);
	}

	public SortedSetCache(CollectionModel model, 
			      Comparator comparator) {
	    this.model = model;
	    model.addObs(this);

	    this.comparator = comparator;

	    if(comparator instanceof Observable)
		((Observable)comparator).addObs(this);
	    
	    cache = new TreeSet(comparator);
	    cache.addAll(model);
	}

	protected Replaceable[] getParams() { 
	    return new Replaceable[] { model, (Replaceable)comparator };
	}
	protected Object clone(Object[] params) {
	    return new SortedSetCache((CollectionModel)params[0],
				      (Comparator)params[1]);
	}

	public void chg() {
	    cache.clear();
	    cache.addAll(model);
	    obses.trigger();
	}
    }
}
