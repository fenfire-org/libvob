/*
ConcatLobList.java
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

public class ConcatLobList extends RealtimeObject implements LobList { 

    private LobList l1, l2;

    private ConcatLobList() {}

    public static ConcatLobList newInstance(LobList l1, LobList l2) {
	ConcatLobList l = (ConcatLobList)FACTORY.object();
	l.l1 = l1; l.l2 = l2;
	return l;
    }

    public static ConcatLobList newInstance(LobList l1, LobList l2, 
					    LobList l3) {
	return newInstance(l1, newInstance(l2, l3));
    }

    public static ConcatLobList newInstance(LobList l1, LobList l2, 
					    LobList l3, LobList l4) {
	return newInstance(newInstance(l1, l2), newInstance(l3, l4));
    }


    public int getLobCount() {
	return l1.getLobCount() + l2.getLobCount();
    }

    public Lob getLob(int index) {
	if(index >= l1.getLobCount())
	    return l2.getLob(index - l1.getLobCount());
	else
	    return l1.getLob(index);
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    l1.move(os);
	    l2.move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new ConcatLobList();
	    }
	};
}
