/*
AbstractDelegateSequence.java
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

public abstract class AbstractDelegateSequence
    extends AbstractDelegateLob implements Sequence {

    protected abstract Sequence getDelegateSequence();

    protected Lob getDelegate() {
	return getDelegateSequence();
    }
    
    public SequenceModel getModel() { 
	return getDelegateSequence().getModel();
    }
    public void setModel(SequenceModel model) {
	getDelegateSequence().setModel(model);
    }

    public int length() {
	return getDelegateSequence().length();
    }
    public Lob getLob(int index) {
	return getDelegateSequence().getLob(index);
    }
    public void clear() {
	getDelegateSequence().clear();
    }
    public void add(Lob lob) {
	getDelegateSequence().add(lob);
    }
    public void add(Lob lob, Object key) {
	getDelegateSequence().add(lob, key, -1);
    }
    public void add(Lob lob, Object key, int index) {
	getDelegateSequence().add(lob, key, index);
    }
    public float getPosition(Axis axis, int index) {
	return getDelegateSequence().getPosition(axis, index);
    }
    public int getLobIndexAt(float x, float y) {
	return getDelegateSequence().getLobIndexAt(x, y);
    }
    public int getCursorIndexAt(float x, float y) {
	return getDelegateSequence().getCursorIndexAt(x, y);
    }
    public Model indexModel(Model key, Model intKey) {
	return getDelegateSequence().indexModel(key, intKey);
    }
    public Model positionModel(Axis axis, Model indexModel) {
	return new PositionModel(axis, this, indexModel);
    }
}
