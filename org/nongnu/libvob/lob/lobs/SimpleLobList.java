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

public class SimpleLobList extends RealtimeObject implements LobList { 

    private List lobs = new ArrayList();

    private SimpleLobList() {}

    public static SimpleLobList newInstance() {
	SimpleLobList l = (SimpleLobList)FACTORY.object();
	l.lobs.clear();
	return l;
    }

    public void add(Lob l) {
	lobs.add(l);
    }

    public int getLobCount() {
	return lobs.size();
    }

    public Lob getLob(int index) {
	return (Lob)lobs.get(index);
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    for(int i=0; i<lobs.size(); i++)
		((Lob)lobs.get(i)).move(os);
	    return true;
	}
	return false;
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new SimpleLobList();
	    }
	};
}
