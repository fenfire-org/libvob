/*
Sequence.java
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
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;

/** A lob that may have many children.
 */
public interface Sequence extends Lob {

    SequenceModel getModel();
    void setModel(SequenceModel model);

    void clear();
    void add(Lob lob);
    void add(Lob lob, Object key);
    void add(Lob lob, Object key, int intKey);

    int length();
    Lob getLob(int index);

    float getPosition(Axis axis, int index);

    int getLobIndexAt(float x, float y);
    int getCursorIndexAt(float x, float y);


    Model indexModel(Model key, Model intKey);


    Model positionModel(Axis axis, Model indexModel);

    class PositionModel extends AbstractModel.AbstractFloatModel {
	protected Axis axis;
	protected Sequence sequence;
	protected Model indexModel;

	public PositionModel(Axis axis, Sequence sequence, Model indexModel) {
	    this.axis = axis;
	    this.sequence = sequence;
	    this.indexModel = indexModel;

	    sequence.addObs(this); indexModel.addObs(this);
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { sequence, indexModel };
	}
	protected Object clone(Object[] params) {
	    return new PositionModel(axis, (Sequence)params[0], 
				     (Model)params[1]);
	}

	public float getFloat() {
	    int index = indexModel.getInt();
	    if(index < 0) return 0;
	    if(sequence.length() == 0) return 0;
	    if(index >= sequence.length()) index = sequence.length()-1;
	    return sequence.getPosition(axis, index)
		+ sequence.getLob(index).getNatSize(axis)/2f;
	}
    }
}
