/*
TransformList.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein
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
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;
import javolution.realtime.*;
import java.util.*;

public class TransformList extends RealtimeList { 

    private List elements;
    private Transform transform;

    private TransformList() {}

    public static TransformList newInstance(List elements,
					       Transform transform) {
	TransformList l = (TransformList)FACTORY.object();
	l.elements = elements; l.transform = transform;
	return l;
    }

    public int size() {
	return elements.size();
    }

    public Object get(int index) {
	return transform.transform(elements.get(index));
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    if(elements instanceof Realtime)
		((Realtime)elements).move(os);
	    transform.move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new TransformList();
	    }
	};
}
