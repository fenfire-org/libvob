/*
Breaker.java
 *    
 *    Copyright (c) 2003-2004, Benja Fallenstein
 *
 *    This file is part of Libvob.
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

public class Breaker extends AbstractSequence {

    /** The axis along which to do breaking:
     *  X is for left-to-right lines, Y for 
     *  top-to-bottom lines. 
     */
    protected Axis axis;

    protected Broken broken;
    protected Line firstLine;

    protected boolean layoutIsCurrent;
    protected boolean isLayouting;

    public Breaker(Axis axis) {
	this.axis = axis;
	firstLine = new Line();
    }

    public Breaker(Axis axis, SequenceModel model) {
	super(model);

	this.axis = axis;
	firstLine = new Line();
    }

    public int getLobLine(int index) {
	if(!layoutIsCurrent) layout();
	return broken.getLobLine(index);
    }
    public int getLobIndexInLine(int index) {
	if(!layoutIsCurrent) layout();
	return broken.getLobIndexInLine(index);
    }
    public int getLobIndex(int line, int index) {
	if(!layoutIsCurrent) layout();
	return broken.getLobIndex(line, index);
    }
    public Sequence getLines() {
	if(!layoutIsCurrent) layout();
	return broken.delegate;
    }
    public Model lineModel(final Model indexModel) {
	return new LineModel(indexModel);
    }
    public Model lineCountModel() {
	return new LineCountModel();
    }

    public float getMinSize(Axis axis) {
	if(!layoutIsCurrent) layout();
	return broken.getMinSize(axis);
    }
    public float getNatSize(Axis axis) {
	if(!layoutIsCurrent) layout();
	return broken.getNatSize(axis);
    }
    public float getMaxSize(Axis axis) {
	if(!layoutIsCurrent) layout();
	return broken.getMaxSize(axis);
    }

    public Object clone(Object[] params) {
	return new Breaker(axis, (SequenceModel)params[0]);
    }

    public float getPosition(Axis axis, int index) {
	if(!layoutIsCurrent) layout();
	return broken.getPosition(axis, index);
    }

    public int getLobIndexAt(float x, float y) {
	if(!layoutIsCurrent) layout();
	return broken.getLobIndexAt(x, y);
    }

    public int getCursorIndexAt(float x, float y) {
	if(!layoutIsCurrent) layout();
	return broken.getCursorIndexAt(x, y);
    }

    protected final boolean isBreakable(Lob l) {
	if(!(l instanceof Breakable)) return false;
	return ((Breakable)l).getBreakQuality(axis) >= 0;
    }

    protected final int nextBreak(int atOrAfter) {
	for(int i=atOrAfter; i<length(); i++)
	    if(isBreakable(getLob(i))) return i;
	return length();
    }

    protected float width = -1, height = -1;

    public void setSize(float requestedWidth, float requestedHeight) {
	float max = (axis==X) ? requestedWidth : requestedHeight;

	if(Float.isNaN(max)) return;

	if(width != requestedWidth || height != requestedHeight) {
	    width = requestedWidth; height = requestedHeight;
	    layoutIsCurrent = false;
	}
    }


    public void chg() {
	if(isLayouting) return;
	layoutIsCurrent = false;
	super.chg();
    }

    protected void layout() {
	isLayouting = true;

	float pos = 0;
	float max = (axis==X) ? width : height;

	if(max < 0) {
	    //throw new UnknownSizeError("Linebreaker size not set yet");
	    max = 0;
	    for(int i=0; i<length(); i++)
		max += getLob(i).getNatSize(axis);
	}

	firstLine.clear();
	Line line = firstLine;
	if(broken == null) broken = new Broken(new Box(axis.other()));
	Sequence lines = broken.delegate;
	lines.clear();

	int i = -1;
	while(true) {
	    int next = nextBreak(i+1);
	    float wid = 0;
	    for(int j=i+1; j<next; j++)
		wid += getLob(j).getNatSize(axis);

	    float breakQuality = i<0 ? Float.NEGATIVE_INFINITY : 
		((Breakable)getLob(i)).getBreakQuality(axis);

	    if(i>=0 && pos+wid>max || breakQuality==Float.POSITIVE_INFINITY) {

		Breakable br = (Breakable)getLob(i);
		Lob pre = br.getPreBreakLob(axis), 
		    in = br.getInBreakLob(axis),
		    post = br.getPostBreakLob(axis);

		if(pre != null) {
		    line.add(pre, getKey(i), getIntKey(i));
		    //line.setIgnEnd(1);
		}
		lines.add(line);
		if(in != null) lines.add(in);
		line = line.next();
		pos = 0;
		if(post != null) {
		    line.add(post);
		    line.setIgnStart(1);
		    pos += post.getNatSize(axis);
		}
	    } else if(i >= 0) {
		line.add(getLob(i), getKey(i), getIntKey(i));
	    }

	    for(int j=i+1; j<next; j++)
		line.add(getLob(j), getKey(j), getIntKey(j));

	    if(next >= length()) break;

	    pos += wid + getLob(next).getNatSize(axis);
	    i = next;
	}

	lines.add(line);

	lines.setSize(width, height);
	isLayouting = false;
	layoutIsCurrent = true;
    }

    protected class Broken extends LobSequence.Unmodifiable {
	protected Broken(Sequence delegate) { super(delegate); }
	protected Sequence clone(Sequence delegate) { 
	    return new Broken(delegate); 
	}

	public int getLobLine(int index0) {
	    int index = index0;
	    int len=delegate.length();
	    for(int i=0; i<len; i++) {
		Sequence s = (Sequence)delegate.getLob(i);
		if(index < s.length()) return i;
		index -= s.length();
	    }
	    return len-1;
	    /*if(index == 0) return len-1;
	    throw new IndexOutOfBoundsException(""+index0);*/
	}

	public int getLobIndexInLine(int index0) {
	    int index = index0;
	    int len=delegate.length();
	    for(int i=0; i<len; i++) {
		Sequence s = (Sequence)delegate.getLob(i);
		if(index < s.length()) return index;
		index -= s.length();
	    }
	    throw new IndexOutOfBoundsException(""+index0);
	}

	public int getLobIndex(int line, int index) {
	    for(int i=0; i<line; i++) {
		Sequence s = (Sequence)delegate.getLob(i);
		index += s.length();
	    }
	    return index;
	}

	public Sequence getLines() { return delegate; }

	public int length() {
	    int len=delegate.length(), result=0;
	    for(int i=0; i<len; i++)
		result += ((Sequence)delegate.getLob(i)).length();
	    return result;
	}

	public Lob getLob(int index0) {
	    int index = index0;
	    int len=delegate.length();
	    for(int i=0; i<len; i++) {
		Sequence s = (Sequence)delegate.getLob(i);
		if(index < s.length()) return s.getLob(index);
		index -= s.length();
	    }
	    throw new IndexOutOfBoundsException(""+index0);
	}

	public float getPosition(Axis axis, int index0) {
	    int index = index0;
	    int len=delegate.length();
	    for(int i=0; i<len; i++) {
		Sequence s = (Sequence)delegate.getLob(i);
		if(index < s.length()) {
		    if(axis == Breaker.this.axis) {
			return s.getPosition(axis, index);
		    } else {
			return delegate.getPosition(axis, i);
		    }
		}
		index -= s.length();
	    }
	    throw new IndexOutOfBoundsException(""+index0);
	}

	public int getLobIndexAt(float x, float y) {
	    int line = delegate.getLobIndexAt(x, y);
	    x -= delegate.getPosition(X, line);
	    y -= delegate.getPosition(Y, line);
	    Sequence s = (Sequence)delegate.getLob(line);
	    int inLine = s.getLobIndexAt(x, y);
	    return getLobIndex(line, inLine);
	}

	public int getCursorIndexAt(float x, float y) {
	    int line = delegate.getLobIndexAt(x, y);
	    x -= delegate.getPosition(X, line);
	    y -= delegate.getPosition(Y, line);
	    Sequence s = (Sequence)delegate.getLob(line);
	    int inLine = s.getCursorIndexAt(x, y);
	    return getLobIndex(line, inLine);
	}
    }

    protected class Line extends LobSequence {
	/** The next in a chain of Line objects used by this breaker.
	 *  Because we don't want to create and garbage collect a lot
	 *  of Line objects each time we re-layout the Breaker, we
	 *  keep a linked list of them which we re-use, only creating
	 *  new objects when we don't have enough.
	 */
	protected Line nextLine;

	protected Line next() {
	    if(nextLine == null) nextLine = new Line();
	    else nextLine.clear();
	    return nextLine;
	}

	protected Line() { 
	    super(new Box(axis, new SequenceModel.SimpleModel(128))); 
	}
	protected Line(Sequence delegate, int s, int e) { 
	    super(delegate); ignStart=s; ignEnd=e; 
	}
	int ignStart=0, ignEnd=0;
	protected void setIgnStart(int ignStart) { this.ignStart = ignStart; }
	protected void setIgnEnd(int ignEnd) { this.ignEnd = ignEnd; }
	protected Sequence clone(Sequence delegate) { 
	    return new Line(delegate, ignStart, ignEnd); 
	}
	public void clear() { 
	    super.clear();
	    obses.removeAll();
	    ignStart = ignEnd = 0;
	}
	public int length() {
	    return delegate.length() - ignStart - ignEnd;
	}
	public Lob getLob(int index) {
	    return delegate.getLob(index + ignStart);
	}
	public float getPosition(Axis axis, int index) {
	    return delegate.getPosition(axis, index + ignStart);
	}
	public int getLobIndexAt(float x, float y) {
	    int idx = delegate.getLobIndexAt(x, y);
	    if(idx < ignStart) return 0;
	    if(idx > delegate.length()-ignEnd) return length();
	    return idx - ignStart;
	}
	public int getCursorIndexAt(float x, float y) {
	    int idx = delegate.getCursorIndexAt(x, y);
	    if(idx < ignStart) return 0;
	    if(idx > delegate.length()-ignEnd) return length();
	    return idx - ignStart;
	}
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float x, float y, float w, float h, float d,
		       boolean visible) {
	if(!layoutIsCurrent) layout();
	broken.render(scene, into, matchingParent, x, y, w, h, d, visible);
    }


    protected class LineCountModel extends AbstractModel.AbstractIntModel {
	protected LineCountModel() {
	    Breaker.this.addObs(this);
	}

	public int getInt() {
	    // XXX notify observers if *this* lob changes
	    
	    try {
		return getLines().length();
	    } catch(UnknownSizeError e) {
		return 0;
	    }
	}
    }

    protected class LineModel extends  AbstractModel.AbstractIntModel {
	protected Model indexModel;

	protected LineModel(Model indexModel) {
	    this.indexModel = indexModel;
	    indexModel.addObs(this);
	}

	public int getInt() {
	    // XXX notify observer if *this* lob changes
	    // (re-breaking, sequence changed...)
	    
	    try {
		return getLobLine(indexModel.getInt());
	    } catch(UnknownSizeError e) {
		return 0;
	    }
	}

	public void setInt(int value) {
	    if(!layoutIsCurrent) layout();

	    if(value >= getLines().length())
		value = getLines().length()-1;
	    if(value < 0)
		value = 0;

	    int oldIndex = indexModel.getInt();
	    int line = getLobLine(oldIndex);
	    if(line == value) return;
	    
	    int index = broken.getLobIndexInLine(oldIndex);
	    Line lineLob = (Line)getLines().getLob(line);
	    float pos = lineLob.getPosition(axis, index);
	    
	    Line l = (Line)getLines().getLob(value);
	    if(axis==X)
		index = l.getCursorIndexAt(pos, 0);
	    else
		index = l.getCursorIndexAt(0, pos);
	    int newIndex = getLobIndex(value, index);

	    indexModel.setInt(newIndex);
	}
    }
}
