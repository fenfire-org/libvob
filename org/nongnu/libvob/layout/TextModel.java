/*
TextModel.java
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
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public interface TextModel extends SequenceModel {

    /** pos is the index before which the text should be inserted */
    public void insert(int pos, String text);

    /** end is one past the last character to delete */
    public void delete(int start, int end);


    Object SIMPLE_TEXT_MODEL_KEY = new Object();

    abstract class AbstractTextModel extends AbstractListModel
	implements TextModel {

	protected Model stringModel;
	protected Model fontModel;

	protected boolean includeLineEnd = true;

	protected LobFont font;   // cached value of fontModel

	public AbstractTextModel(Model stringModel, Model fontModel,
				 boolean includeLineEnd) {
	    this.fontModel = fontModel;
	    this.stringModel = stringModel;
	    this.includeLineEnd = includeLineEnd;

	    fontModel.addObs(this); stringModel.addObs(this);

	    chg();
	}

	public void setIncludeLineEnd(boolean value) {
	    if(value != includeLineEnd) {
		includeLineEnd = value;
		chg();
	    }
	}

	public int hashCode() { return System.identityHashCode(this); }
	
	public abstract void insert(int pos, String newText);
	public abstract void delete(int start, int end);

	protected abstract int getCharCount();
	protected abstract char getChar(int index);

	protected Replaceable[] getParams() {
	    return new Replaceable[] { stringModel, fontModel };
	}
	
	public int size() { 
	    if(!includeLineEnd)
		return getCharCount();
	    else
		return getCharCount() + 1; 
	}
	
	public void clear() { 
	    throw new UnsupportedOperationException("changing the model");
	}
	public void add(Object lob, Object key, int intKey) {
	    throw new UnsupportedOperationException("changing the model");
	}
	public void remove(Lob lob) {
	    throw new UnsupportedOperationException("changing the model");
	}
	
	public Object get(int index) { 
	    if(index < getCharCount())
		return font.getGlyph(getChar(index));
	    else if(index == getCharCount() && includeLineEnd)
		return font.getLineEnd();
	    else
		throw new IndexOutOfBoundsException(""+index);
	}
	public Object getKey(int index) {
	    return SIMPLE_TEXT_MODEL_KEY;
	}
	public int getIntKey(int index) { 
	    return index;
	}
	
	public void chg() {
	    font = (LobFont)fontModel.get();
	    obses.trigger();
	}
    }

    class StringTextModel extends AbstractTextModel {
	protected Model keyModel;

	public StringTextModel(Model stringModel, Model fontModel) {
	    super(stringModel, fontModel, true);
	    this.keyModel = new ObjectModel(null);
	}

	public StringTextModel(Model stringModel, Model fontModel,
			       Model keyModel) {
	    super(stringModel, fontModel, true);
	    this.keyModel = keyModel;
	}

	public StringTextModel(String s, Model fontModel) {
	    this(new ObjectModel(s), fontModel);
	}

	public StringTextModel(Model fontModel) {
	    this("", fontModel);
	}

	public StringTextModel(Model stringModel, Model fontModel,
			       Model keyModel, boolean includeLineEnd) {
	    super(stringModel, fontModel, includeLineEnd);
	    this.keyModel = keyModel;
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { stringModel, fontModel, keyModel };
	}
	protected Object clone(Object[] params) {
	    return new StringTextModel((Model)params[0], (Model)params[1],
				       (Model)params[2], includeLineEnd);
	}

	public Object getKey(int index) {
	    return keyModel.get();
	}

	public void insert(int pos, String newText) {
	    String text = (String)stringModel.get();
	    char[] chars = new char[text.length() + newText.length()];
	    text.getChars(0, pos, chars, 0);
	    newText.getChars(0, newText.length(), chars, pos);
	    text.getChars(pos, text.length(), chars, pos+newText.length());
	    stringModel.set(new String(chars));
		/*
	    stringModel.set(text.substring(0, pos) + newText + 
			    text.substring(pos));
		*/
	}
	
	public void delete(int start, int end) {
	    String text = (String)stringModel.get();
	    stringModel.set(text.substring(0, start) + text.substring(end));
	}

	protected int getCharCount() {
	    return ((String)stringModel.get()).length();
	}

	protected char getChar(int index) {
	    return ((String)stringModel.get()).charAt(index);
	}
    }

    /*
    abstract class DelegateTextModel extends DelegateSequenceModel 
	implements TextModel {

	protected abstract TextModel getTextDelegate();

	protected SequenceModel getDelegate() { return getTextDelegate(); }

	public void insert(int pos, String text) {
	    getTextDelegate().insert(pos, text);
	}

	public void delete(int start, int end) {
	    getTextDelegate().delete(start, end);
	}
    }
    */

    class Concat extends SequenceModel.Concat implements TextModel {

	public Concat(TextModel m1, TextModel m2) {
	    this(new ListModel.Simple(new Object[] { m1, m2 }));
	}
	public Concat(TextModel m1, TextModel m2, TextModel m3) {
	    this(new ListModel.Simple(new Object[] { m1, m2, m3 }));
	}

	public Concat(ListModel models) {
	    super(models);
	}

	protected TextModel tmodel(int i) {
	    return (TextModel)models.get(i);
	}

	public void insert(int pos, String text) {
	    int rel_idx = pos;
	    for(int i=0; i<sizes.length; i++) {
		if(rel_idx < sizes[i]) {
		    tmodel(i).insert(rel_idx, text);
		    return;
		}

		rel_idx -= sizes[i];
	    }
	    throw new IndexOutOfBoundsException(""+pos);
	}

	public void delete(int start, int end) {
	    int rel_idx = start;
	    int len = end-start;

	    int i=0;
	    for(; i<sizes.length; i++) {
		if(rel_idx < sizes[i])
		    break;

		rel_idx -= sizes[i];
	    }

	    while(len>0) {
		if(i>=sizes.length)
		    throw new IndexOutOfBoundsException(start+" "+end);

		int del = sizes[i] - rel_idx;
		if(len < del) del = len;

		tmodel(i).delete(rel_idx, rel_idx+del);

		len -= del;

		i++;
		rel_idx = 0;
	    }
	}
    }
    
    class ModelTextModel extends SequenceModel.ModelSequenceModel 
	implements TextModel {

	public ModelTextModel(Model modelModel) {
	    super(modelModel);
	}

	protected Object clone(Object[] params) {
	    return new ModelTextModel((Model)params[0]);
	}

	public void insert(int pos, String text) {
	    ((TextModel)currentModel).insert(pos, text);
	}

	public void delete(int start, int end) {
	    ((TextModel)currentModel).delete(start, end);
	}
    }
}
