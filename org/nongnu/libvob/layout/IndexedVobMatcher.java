/*   
IndexedVobMatcher.java
 *    
 *    Copyright (c) 2003-2004, Benja Fallenstein.
 *
 *    This file is part of Fenfire.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
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
import org.nongnu.libvob.impl.ArrayVobMatcher;
import javolution.realtime.*;

public class IndexedVobMatcher extends ArrayVobMatcher {
    public static boolean dbg = false;
    private static void p(String s) { System.out.println(s); }

    protected int[] csIndex = new int[16];

    protected int hash(int parent, Object key, int index) {
	return hash(parent, key) + index + 1;
    }

    protected int csHash(int cs) {
	return hash(csParent[cs], csKey[cs], csIndex[cs]);
    }

    public int add(int parent, int cs, Object key) {
	return add(parent, cs, key, -1);
    }

    public int add(int parent, int cs, Object key, int index) {
	ensureMaxCS(cs);
	csIndex[cs] = index;

	super.add(parent, cs, key);
	return cs;
    }

    public int getCS(int parent, Object key) {
	return getCS(parent, key, -1);
    }

    public int getCS(int parent, Object key, int index) {
	int i = hashIndex(hash(parent, key, index));
	for(int cs=hashtable[i]; cs>0; cs=csNextInHashtable[cs]) {
	    if(parent==csParent[cs] && index==csIndex[cs] && 
	       equals(key, csKey[cs]))
		return cs;
	}
	return -1;
    }

    public int getIndex(int cs) {
	return csIndex[cs];
    }

    protected Object getPath(int cs, Object subpath) {
	if(cs == 0) {
	    return subpath;
	} else {
	    IndexedTreePath tp = 
		(IndexedTreePath)INDEXED_TREE_PATH_FACTORY.object();
	    tp.key = getKey(cs);
	    tp.intKey = getIndex(cs);
	    tp.subpath = subpath;
	    return getPath(getParent(cs), tp);
	}
    }

    protected int getPathCS(int parent, Object path) {
	if(path == null || parent < 0) {
	    return parent;
	} else if(path instanceof IndexedTreePath) {
	    IndexedTreePath tp = (IndexedTreePath)path;
	    return getPathCS(getCS(parent, tp.key, tp.intKey), tp.subpath);
	} else {
	    return super.getPathCS(parent, path);
	}
    }

    protected int getOtherCS(VobMatcher other, int oparent, int mycs) {
	IndexedVobMatcher o = (IndexedVobMatcher)other;
	return o.getCS(oparent, csKey[mycs], csIndex[mycs]);
    }

    protected void expandMaxCS(int n) {
	super.expandMaxCS(n);

	int[] nindex = new int[n];
	System.arraycopy(csIndex, 0, nindex, 0, csIndex.length);
	csIndex = nindex;
    }


    public static class IndexedTreePath extends TreePath {
	public int intKey;

	public boolean equals(Object o) {
	    IndexedTreePath tp = (IndexedTreePath)o;
	    if(intKey != tp.intKey) return false;
	    if(subpath != null)
		return key.equals(tp.key) && subpath.equals(tp.subpath);
	    else
		return key.equals(tp.key) && tp.subpath == null;
	}

	public int hashCode() {
	    return 43289*key.hashCode() + intKey +
		(subpath!=null ? 2388*subpath.hashCode() : 0);
	}
    }
    
    public static final RealtimeObject.Factory INDEXED_TREE_PATH_FACTORY = 
	new RealtimeObject.Factory() {
	    public Object create() {
		return new IndexedTreePath();
	    }
	};
}
				      
