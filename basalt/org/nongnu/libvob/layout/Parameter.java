/*
Parameter.java
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
import java.util.Set;
import java.util.Map;
import java.util.Collections;

/** A parameter of a template.
 */
public class Parameter extends AbstractModel.AbstractObjectModel {

    protected Object key;
    protected Object value;

    public Parameter(Object key) {
	this(key, null);
    }

    public Parameter(Object key, Object value) {
	this.key = key;
	this.value = value;
    }


    public static Lob lob(Object key) {
	return lob(key, NullLob.instance);
    }
    public static Model model(Object key) {
	return model(key, new ObjectModel());
    }
    public static SequenceModel sequenceModel(Object key) {
	return sequenceModel(key, new SequenceModel.SimpleModel());
    }
    public static TextModel textModel(Object key) {
	return textModel(key, new TextModel.StringTextModel("", org.nongnu.libvob.layout.component.Theme.getFont()));
    }
    public static ListModel listModel(Object key) {
	return listModel(key, new ListModel.Simple());
    }
    public static SetModel setModel(Object key) {
	return setModel(key, new SetModel.Simple());
    }
    public static MapModel mapModel(Object key) {
	return mapModel(key, new MapModel.Simple());
    }
    public static Action action(Object key) {
	return action(key, Action.NULL);
    }

    public static Lob lob(Object key, Lob value) {
	return new ModelLob(new Parameter(key, value));
    }
    public static Model model(Object key, Model value) {
	return new ModelModel(new Parameter(key, value));
    }
    public static ListModel listModel(Object key, ListModel value) {
	return new ListModel.ModelListModel(new Parameter(key, value));
    }
    public static SetModel setModel(Object key, SetModel value) {
	return new SetModel.ModelSetModel(new Parameter(key, value));
    }
    public static SequenceModel sequenceModel(Object key, SequenceModel value){
	return new SequenceModel.ModelSequenceModel(new Parameter(key, value));
    }
    public static TextModel textModel(Object key, TextModel value) {
	return new TextModel.ModelTextModel(new Parameter(key, value));
    }
    public static MapModel mapModel(Object key, MapModel value) {
	return new MapModel.ModelMapModel(new Parameter(key, value));
    } 
   public static Action action(Object key, Action value) {
	return new ModelAction(new Parameter(key, value));
    }


    protected Object clone(Object[] newParams) {
	throw new UnsupportedOperationException("Parameter doesn't use "+
						"AbstractReplaceable "+
						"infrastructure");
    }
    protected Replaceable[] getParams() {
	throw new UnsupportedOperationException("Parameter doesn't use "+
						"AbstractReplaceable "+
						"infrastructure");
    }


    public Object get() {
	return value;
    }
    
    public void set(Object value) {
	this.value = value;
	chg();
    }


    public Set getTemplateParameters() {
	return Collections.singleton(key);
    }

    public Object getTemplateParameter(Object key) {
	if(key.equals(this.key))
	    return value;
	else
	    return null;
    }

    public void setTemplateParameter(Object key, Object value) {
	if(key.equals(this.key)) {
	    this.value = value;
	    chg();
	}
    }

    public Object instantiateTemplate(Map map) {
	if(map.get(this) != null) return map.get(this);

	Object newValue;

	if(map.get(key) != null)
	    newValue = map.get(key);
	else if(value instanceof Replaceable) {
	    Replaceable r = (Replaceable)value;
	    newValue = r.instantiateTemplate(map);
	} else {
	    newValue = value;
	}

	Parameter newThis;

	if(newValue == value)
	    newThis = this;
	else
	    newThis = new Parameter(key, newValue);

	map.put(this, newThis);

	return newThis;
    }

    public String toString() {
	return "Parameter@"+System.identityHashCode(this)+"("+key+", "+value+")";
    }
}
