/*   
AbstractModel.java
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

public abstract class AbstractModel 
    extends AbstractReplaceable.AbstractObservable implements Model {

    protected Replaceable[] getParams() { 
	return NO_PARAMS;
    }
    protected Object clone(Object[] params) {
	return this;
    }

    public void set(Object value) {
	throw new UnsupportedOperationException("Read-only model: "+this);
    }
    public void setInt(int value) {
	throw new UnsupportedOperationException("Read-only model: "+this);
    }
    public void setFloat(float value) {
	throw new UnsupportedOperationException("Read-only model: "+this);
    }
    public boolean getBool() {
	return getInt() != 0;
    }
    public void setBool(boolean value) {
	setInt(value ? 1 : 0);
    }

    public Model equalsModel(Model other) {
	return new EqualsModel(this, other);
    }
    public Model equalsObject(Object other) {
	return new EqualsModel(this, new ObjectModel(other));
    }
    public Model equalsInt(int other) {
	return new EqualsModel(this, new IntModel(other));
    }
    public Model equalsFloat(float other) {
	return new EqualsModel(this, new FloatModel(other));
    }
    public Model equalsBool(boolean other) {
	return new EqualsModel(this, new BoolModel(other));
    }

    public Model plus(Model m) {
	return new PlusModel(this, m);
    }
    public Model plus(float f) {
	return new PlusModel(this, new FloatModel(f));
    }
    public Model minus(Model m) {
	return new MinusModel(this, m);
    }
    public Model minus(float f) {
	return new MinusModel(this, new FloatModel(f));
    }
    public Model times(Model m) {
	return new TimesModel(this, m);
    }
    public Model times(float f) {
	return new TimesModel(this, new FloatModel(f));
    }
    public Model divide(Model m) {
	return new DivideModel(this, m);
    }
    public Model divide(float f) {
	return new DivideModel(this, new FloatModel(f));
    }
    public Model divide(Model m, float ifZero) {
	return new DivideModel(this, m, ifZero);
    }
    public Model divide(float f, float ifZero) {
	return new DivideModel(this, new FloatModel(f), ifZero);
    }

    public Model select(Model ifTrue, Model ifFalse) {
	return new SelectModel(this, ifTrue, ifFalse);
    }


    public static abstract class AbstractObjectModel extends AbstractModel {
	public int getInt() {
	    Number n = (Number)get();
	    if(n == null) throw new ClassCastException("null is not "+
						       "an int value");
	    return n.intValue();
	}
	public void setInt(int value) {
	    set(new Integer(value));
	}
	
	public float getFloat() {
	    Number n = (Number)get();
	    if(n == null) throw new ClassCastException("null is not "+
						       "a float value");
	    return n.floatValue();
	}
	public void setFloat(float value) {
	    set(new Float(value));
	}

	public boolean getBool() {
	    return ((Boolean)get()).booleanValue();
	}
	public void setBool(boolean value) {
	    set(new Boolean(value));
	}
    }

    public static abstract class AbstractIntModel extends AbstractModel {
	public float getFloat() { 
	    return getInt();
	}
	public void setFloat(float value) {
	    setInt((int)value);
	}
	
	public Object get() {
	    return new Integer(getInt());
	}
	public void set(Object value) {
	    setInt(((Number)value).intValue());
	}
    }

    public static abstract class AbstractFloatModel extends AbstractModel {
	public int getInt() { 
	    return (int)getFloat();
	}
	public void setInt(int value) {
	    setFloat(value);
	}
	
	public Object get() {
	    return new Float(getFloat());
	}
	public void set(Object value) {
	    setFloat(((Number)value).floatValue());
	}
    }

    public static abstract class AbstractBoolModel extends AbstractModel {
	public int getInt() { 
	    return getBool() ? 1 : 0;
	}
	public void setInt(int value) {
	    setBool(value != 0);
	}

	public float getFloat() {
	    return getBool() ? 1 : 0;
	}
	public void setFloat(float value) {
	    setBool(value != 0);
	}
	
	public Object get() {
	    return new Boolean(getBool());
	}
	public void set(Object value) {
	    setBool(((Boolean)value).booleanValue());
	}
    }


    public static class EqualsModel extends AbstractBoolModel {
	protected Model a, b;

	public EqualsModel(Model a, Model b) {
	    this.a = a; a.addObs(this);
	    this.b = b; b.addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { a, b };
	}
	protected Object clone(Object[] params) {
	    return new EqualsModel((Model)params[0], (Model)params[1]);
	}

	public boolean getBool() {
	    try {
		// first, try to compute the result without creating objects;
		// will throw an exception if a or b is an ObjectModel holding
		// and object that can not be cast to Number
		return a.getFloat() == b.getFloat();
	    } catch(ClassCastException e) {
		Object o = a.get();
		if(o == null) return b.get() == null;
		return o.equals(b.get());
	    }
	}
    }

    public static class PlusModel extends AbstractFloatModel {
	protected Model a, b;

	public PlusModel(Model a, Model b) {
	    this.a = a; a.addObs(this);
	    this.b = b; b.addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { a, b };
	}
	protected Object clone(Object[] params) {
	    return new PlusModel((Model)params[0], (Model)params[1]);
	}

	public float getFloat() {
	    return a.getFloat() + b.getFloat();
	}
	public void setFloat(float value) {
	    a.setFloat(value - b.getFloat());
	}
    }

    public static class MinusModel extends AbstractFloatModel {
	protected Model a, b;

	public MinusModel(Model a, Model b) {
	    this.a = a; a.addObs(this);
	    this.b = b; b.addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { a, b };
	}
	protected Object clone(Object[] params) {
	    return new MinusModel((Model)params[0], (Model)params[1]);
	}

	public float getFloat() {
	    return a.getFloat() - b.getFloat();
	}
	public void setFloat(float value) {
	    a.setFloat(value + b.getFloat());
	}
    }

    public static class TimesModel extends AbstractFloatModel {
	protected Model a, b;

	public TimesModel(Model a, Model b) {
	    this.a = a; a.addObs(this);
	    this.b = b; b.addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { a, b };
	}
	protected Object clone(Object[] params) {
	    return new TimesModel((Model)params[0], (Model)params[1]);
	}

	public float getFloat() {
	    return a.getFloat() * b.getFloat();
	}
	public void setFloat(float value) {
	    a.setFloat(value / b.getFloat());
	}
    }

    public static class DivideModel extends AbstractFloatModel {
	protected Model a, b;
	protected boolean allowZero;
	protected float ifZero;

	public DivideModel(Model a, Model b) {
	    this.a = a; a.addObs(this);
	    this.b = b; b.addObs(this);
	    this.allowZero = false;
	}

	public DivideModel(Model a, Model b, float ifZero) {
	    this.a = a; a.addObs(this);
	    this.b = b; b.addObs(this);
	    this.allowZero = true;
	    this.ifZero = ifZero;
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { a, b };
	}
	protected Object clone(Object[] params) {
	    return new DivideModel((Model)params[0], (Model)params[1],
				   ifZero);
	}

	public float getFloat() {
	    float bf = b.getFloat();
	    if(allowZero && bf == 0) return ifZero;
	    float f = a.getFloat() / bf;
	    return f;
	}
	public void setFloat(float value) {
	    a.setFloat(value * b.getFloat());
	}
    }

    public static class SelectModel extends AbstractObjectModel {
	protected Model m, ifTrue, ifFalse;

	public SelectModel(Model m, Model ifTrue, Model ifFalse) {
	    this.m = m;
	    this.ifTrue = ifTrue;
	    this.ifFalse = ifFalse;
	    
	    m.addObs(this); ifTrue.addObs(this); ifFalse.addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { m, ifTrue, ifFalse };
	}
	protected Object clone(Object[] params) {
	    return new SelectModel((Model)params[0], (Model)params[1],
				   (Model)params[2]);
	}

	public Object get() {
	    return m.getBool() ? ifTrue.get() : ifFalse.get();
	}
	public int getInt() {
	    return m.getBool() ? ifTrue.getInt() : ifFalse.getInt();
	}
	public float getFloat() {
	    return m.getBool() ? ifTrue.getFloat() : ifFalse.getFloat();
	}
	public boolean getBool() {
	    return m.getBool() ? ifTrue.getBool() : ifFalse.getBool();
	}
    }
}
