/*
AbstractSequence.java
 *    
 *    Copyright (c) 2003-2004, Benja Fallenstein
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
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

public abstract class AbstractSequence 
    extends AbstractLob implements Sequence {

    protected SequenceModel model;
    protected boolean isLargerThanItSeems;

    public AbstractSequence(SequenceModel model) {
	setModel(model);
    }

    public AbstractSequence() {
	this(new SequenceModel.SimpleModel());
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { model };
    }

    public void chg() { //modelChanged() {
	isLargerThanItSeems = false;

	for(int i=0; i<length(); i++) {
	    getLob(i).addObs(this);

	    if(getLob(i).isLargerThanItSeems()) {
		isLargerThanItSeems = true;
	    }
	}

	super.chg();
	//chg();
    }

    public SequenceModel getModel() {
	return model;
    }
    public void setModel(SequenceModel model) {
	if(this.model != null) this.model.removeObs(this);
	this.model = model;
	//model.addObs(new Obs() { public void chg() { modelChanged(); }});
	model.addObs(this);

	isLargerThanItSeems = false;

	for(int i=0; i<length(); i++) {
	    getLob(i).addObs(this);

	    if(getLob(i).isLargerThanItSeems()) {
		isLargerThanItSeems = true;
	    }
	}
    }

    public final int length() {
	return model.size();
    }

    protected static class IndexModel extends AbstractModel.AbstractIntModel {
	protected Sequence seq;
	private Model key, intKey;

	private IndexModel(Sequence seq, Model key, Model intKey) { 
	    this.seq = seq;
	    this.key = key; 
	    this.intKey = intKey;

	    seq.addObs(this); key.addObs(this); intKey.addObs(this);
	}

	protected Replaceable[] getParams() { 
	    return new Replaceable[] { seq, key, intKey };
	}
	protected Object clone(Object[] params) {
	    return new IndexModel((Sequence)params[0], 
				  (Model)params[1], (Model)params[2]);
	}

	public int getInt() {
	    SequenceModel model = seq.getModel();

	    Object k = key.get();
	    int ik = intKey.getInt();

	    //System.out.println("IndexModel@"+System.identityHashCode(this)+": Elems "+k+" "+ik+" in "+seq);

	    for(int i=0; i<model.size(); i++) {
		//System.out.println("- "+model.getKey(i)+" "+model.getIntKey(i));
		if(model.getIntKey(i) != ik) continue;
		if((k!=null) ? (k.equals(model.getKey(i))) 
		   : (model.getKey(i)==null))
		    return i;
	    }

	    //System.out.println("in "+seq);
	    throw new NoSuchElementException(k+" "+ik);
	}
	
	public void setInt(int value) {
	    SequenceModel model = seq.getModel();
	    key.set(model.getKey(value));
	    intKey.setInt(model.getIntKey(value));
	}

	public String toString() {
	    return "IndexModel@"+System.identityHashCode(this)+"("+seq+key.get()+", "+intKey.get()+")";
	}
    }

    public Model indexModel(Model key, Model intKey) {
	return new IndexModel(this, key, intKey);
    }

    public void clear() { 
	((SequenceModel.SimpleModel)model).clear(); 
    }
    public void add(Lob lob) { 
	((SequenceModel.SimpleModel)model).add(lob); 
    }
    public void add(Lob lob, Object key) { 
	((SequenceModel.SimpleModel)model).add(lob, key); 
    }
    public void add(Lob lob, Object key, int intKey) { 
	((SequenceModel.SimpleModel)model).add(lob, key, intKey); 
    }

    public final Lob getLob(int index) {
	return (Lob)model.get(index);
    }
    public final Object getKey(int index) {
	return model.getKey(index);
    }
    public final int getIntKey(int index) {
	return model.getIntKey(index);
    }

    public boolean key(String key) {
	for(int i=0; i<length(); i++)
	    if(getLob(i).key(key)) return true;

	return false;
    }

    public boolean mouse(VobMouseEvent e, float x0, float y0) {
	if(length() == 0) return false;

	boolean handled = false;

	int index = getLobIndexAt(x0, y0);
	float x = x0 - getPosition(X, index);
	float y = y0 - getPosition(Y, index);
	handled = handled || getLob(index).mouse(e, x, y);

	for(int i=0; i<length(); i++)
	    if(getLob(i).isLargerThanItSeems())
		handled = handled || getLob(i).mouse(e, x0 - getPosition(X, i),
						     y0 - getPosition(Y, i));

	return handled;
    }

    public List getFocusableLobs() {
	List l = new ArrayList();
	for(int i=0; i<length(); i++)
	    l.addAll(getLob(i).getFocusableLobs());
	return l;
    }

    public void setFocusModel(Model m) {
	for(int i=0; i<length(); i++)
	    getLob(i).setFocusModel(m);
    }

    public Model positionModel(Axis axis, Model indexModel) {
	return new PositionModel(axis, this, indexModel);
    }

    public boolean isLargerThanItSeems() {
	return isLargerThanItSeems;
    }
}
