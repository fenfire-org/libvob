/*
AbstractSequence.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein
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
package org.nongnu.libvob.lob;
import org.nongnu.libvob.*;
import org.nongnu.libvob.fn.*;
import javolution.realtime.*;
import java.util.*;

public abstract class AbstractSequence extends AbstractLob {

    protected List lobs;

    public boolean key(String key) {
	for(int i=0; i<lobs.size(); i++)
	    if(((Lob)lobs.get(i)).key(key)) return true;
	return false;
    }

    public List getFocusableLobs() {
	List result = Lists.list();

	for(int i=0; i<lobs.size(); i++) {
	    PoolContext.enter();
	    try {
		Lob lob = (Lob)lobs.get(i);
		result.addAll(lob.getFocusableLobs());
	    } finally {
		PoolContext.exit();
	    }
	}

	return result;
    }
}
