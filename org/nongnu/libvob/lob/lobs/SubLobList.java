/*
SubLobList.java
 *    
 *    Copyright (c) 2003-2005, Benja Fallenstein
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

public class SubLobList extends RealtimeObject implements LobList { 

    private LobList list;
    private int start, end;

    private SubLobList() {}

    public static SubLobList newInstance(LobList list, int start, int end) {
	if(end < start)
	    throw new IllegalArgumentException("end == "+end+" < "+start+" == start");
	if(end > list.getLobCount())
	    throw new IllegalArgumentException("end == "+end+" > "+list.getLobCount()+" == list.getLobCount()");

	SubLobList l = (SubLobList)FACTORY.object();
	l.list = list; l.start = start; l.end = end;
	return l;
    }

    public int getLobCount() {
	return end-start;
    }

    public Lob getLob(int index) {
	if(start+index >= end)
	    throw new IndexOutOfBoundsException(index+" >= "+(end-start));
	
	return list.getLob(start+index);
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    list.move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new SubLobList();
	    }
	};
}
