/*
ConcatList.java
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

public class ConcatList extends RealtimeList { 

    private List l1, l2;

    private ConcatList() {}

    public static ConcatList newInstance(List l1, List l2) {
	ConcatList l = (ConcatList)FACTORY.object();
	l.l1 = l1; l.l2 = l2;
	return l;
    }

    public static ConcatList newInstance(List l1, List l2, 
					    List l3) {
	return newInstance(l1, newInstance(l2, l3));
    }

    public static ConcatList newInstance(List l1, List l2, 
					    List l3, List l4) {
	return newInstance(newInstance(l1, l2), newInstance(l3, l4));
    }


    public int size() {
	return l1.size() + l2.size();
    }

    public Object get(int index) {
	if(index >= l1.size())
	    return l2.get(index - l1.size());
	else
	    return l1.get(index);
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    if(l1 instanceof Realtime) ((Realtime)l1).move(os);
	    if(l2 instanceof Realtime) ((Realtime)l2).move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new ConcatList();
	    }
	};
}
