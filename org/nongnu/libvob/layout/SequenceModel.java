/*
SequenceModel.java
 *    
 *    Copyright (c) 2003-2004, Benja Fallenstein
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
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public interface SequenceModel extends ListModel {

    void add(Object o, Object key, int intKey);

    Object getKey(int index);
    int getIntKey(int index);


    class SimpleModel extends AbstractListModel implements SequenceModel {

	protected int nlobs = 0;
	protected Lob[] lobs;
	protected Object[] keys;
	protected int[] intKeys;

	protected boolean isUpdating;
	
	public SimpleModel() {
	    this(16);
	}
	
	public SimpleModel(int len) {
	    lobs = new Lob[len];
	    keys = new Object[len];
	    intKeys = new int[len];
	}

	public void clear() {
	    nlobs = 0;
	    if(!isUpdating) obses.trigger();
	}

	public boolean add(Object lob) {
	    add(lob, null);
	    return true;
	}
	public void add(Object lob, Object key) {
	    add(lob, key, -1);
	}
	public void add(Object _lob, Object key, int intKey) {
	    Lob lob = (Lob)_lob;

	    if(lobs.length <= nlobs)
		expand(2*lobs.length);
	    
	    lobs[nlobs] = lob;
	    keys[nlobs] = key;
	    intKeys[nlobs] = intKey;
	    nlobs++;

	    if(!isUpdating) obses.trigger();
	}

	public Object set(int index, Object o) {
	    Object old = lobs[index];
	    lobs[index] = (Lob)o;
	    keys[index] = null;
	    intKeys[index] = 0;
	    return old;
	}

	public int size() { return nlobs; }

	public Object get(int index) { return lobs[index]; }
	public Object getKey(int index) { return keys[index]; }
	public int getIntKey(int index) { return intKeys[index]; }

	protected void expand(int len) {
	    Lob[] n = new Lob[len];
	    Object[] nk = new Object[len];
	    int[] ni = new int[len];
	    System.arraycopy(lobs, 0, n, 0, lobs.length);
	    System.arraycopy(keys, 0, nk, 0, lobs.length);
	    System.arraycopy(intKeys, 0, ni, 0, lobs.length);
	    lobs = n; keys = nk; intKeys = ni;
	}


	protected Replaceable[] getParams() {
	    // this is mucking a bit with how this method is meant...
	    return lobs;
	}
	protected Object clone(Object[] newParams) {
	    SimpleModel s = new SimpleModel();
	    for(int i=0; i<nlobs; i++)
		s.add(newParams[i], keys[i], intKeys[i]);
	    return s;
	}
    }


    /*
    abstract class DelegateSequenceModel extends ListModel.Simple
	implements SequenceModel, Obs {

	protected abstract SequenceModel getDelegate();

	public int size() { return getDelegate().size(); }

	public Lob getLob(int index) { return getDelegate().getLob(index); }
	public Object getKey(int index) { return getDelegate().getKey(index); }
	public int getIntKey(int index) {
	    return getDelegate().getIntKey(index); 
	}

	public void chg() {
	    obses.trigger();
	}

	protected Replaceable[] getParams() { return NO_PARAMS; }
	protected Object clone(Object[] params) { return this; }
    }
    */


    class ListSequenceModel extends AbstractListModel 
	implements SequenceModel {

	protected ListModel elements;
	
	public int size() { return elements.size(); }
	public Object get(int index) { return elements.get(index); }
	public Object getKey(int index) { return null; }
	public int getIntKey(int index) { return 0; }

	public void add(Object lob, Object key, int intKey) {
	    throw new UnsupportedOperationException();
	}

	public ListSequenceModel(ListModel elements) {
	    this.elements = elements;
	    elements.addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { elements };
	}
	protected Object clone(Object[] params) {
	    return new ListSequenceModel((ListModel)params[0]);
	}
    }

    class ModelSequenceModel extends ListModel.ModelListModel 
	implements SequenceModel {

	public ModelSequenceModel(Model modelModel) {
	    super(modelModel);
	}

	protected Object clone(Object[] params) {
	    return new ModelSequenceModel((Model)params[0]);
	}

	public void add(Object o, Object key, int intKey) {
	    throw new UnsupportedOperationException();
	}
	
	public Object getKey(int index) {
	    return ((SequenceModel)currentModel).getKey(index);
	}
	public int getIntKey(int index) {
	    return ((SequenceModel)currentModel).getIntKey(index);
	}
    }


    class Singleton extends SimpleModel {
	public Singleton(Lob lob) {
	    this(lob, null);
	}
	public Singleton(Lob lob, Object key) {
	    this(lob, key, 0);
	}
	public Singleton(Lob lob, Object key, int intKey) {
	    super(1);
	    add(lob, key, intKey);
	}
    }
}
