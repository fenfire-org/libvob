/*
DefaultVobMatcher.java
 *
 *    Copyright (c) 2002, Tuomas J. Lukka
 *
 *    This file is part of Gzz.
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
 * Written by Tuomas J. Lukka
 */
package org.nongnu.libvob.impl;
import org.nongnu.libvob.*;
import java.awt.*;
import java.util.*;

/** A simple hierarchical implementation of VobMatcher.
 */
public class DefaultVobMatcher implements VobMatcher {
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println(s); }

    static final int INITIAL_SIZE = 20;
    static final Object NULL = new Object();

    Key[] keyByCs = new Key[INITIAL_SIZE];
    Map csByKey = new HashMap();

    int maxcs = 0;

    public void clear() {
	keyByCs = new Key[INITIAL_SIZE];
	csByKey = new HashMap();
	maxcs = 0;
    }

    class Key {
        Object key; int parent;
	int interpolateTo;
        Key(Object k, int p) { key=k; parent=p; }
	public int hashCode() { return key.hashCode() ^ parent; }
	public boolean equals(Object o) {
	    if(!(o instanceof Key)) return false;
	    Key k = (Key)o;
	    return parent == k.parent && key.equals(k.key);
	}
	public String toString() {
	    return "[DefaultVobMatcher key: "+key+" parent: "+parent+"]";
	}
    }
    void ensure(int n) {
        if(n+1 > keyByCs.length) {
            Key[] nu = new Key[2*n+1];
	    System.arraycopy(keyByCs, 0, nu, 0, maxcs+1);
	    keyByCs = nu;
	}
        if(n > maxcs) maxcs = n;
    }

    public DefaultVobMatcher() {
        add(-1, 0, null);
    }

    /** Get the keys of all children of the given cs. 
     */
    public Set getKeys(int cs) {
	Set result = new HashSet();
	for(int i=0; i<=maxcs; i++)
	    if(keyByCs[i] != null && keyByCs[i].parent == cs)
		result.add(keyByCs[i].key);
	return result;
    }


    // --- implement VobMatcher
    public int add(int cs, Object key) {
        add(0, cs, key);
	return cs;
    }
    public int add(int into, int cs, Object key) {
        ensure(cs);
	if(cs < 0)
	    throw new Error("Trying to add negative parent!");
        if(key == null) key = NULL;

	if (dbg) checkIfKeyAlreadyExist(new Key(key, into));

        keyByCs[cs] = new Key(key, into);
	csByKey.put(keyByCs[cs], new Integer(cs));
	return cs;
    }

    protected int getCSByKeyObject(Key key) {
	Integer i = (Integer)csByKey.get(key);
	if(i==null) return -1;
	return i.intValue();
    }

    public int getCS(Object key) {
        if(key == null) key = NULL;
        return getCSByKeyObject(new Key(key, 0));
    }

    public int getCS(int parent, Object key) {
        if(key == null) key = NULL;
        return getCSByKeyObject(new Key(key, parent));
    }

    public int getParent(int cs) {
	return keyByCs[cs].parent;
    }

    public boolean isAncestor(int cs, int ancestor) {
	while(cs != 0) {
	    int cur = getParent(cs);
	    if(cur == ancestor) return true;
	    cs = cur;
	}
	return false;
    }

    public Object getKey(int cs) {
	Object k = keyByCs[cs].key;
	return k == NULL ? null : k;
    }

    public void dump() {
	for(int i=1; i<=maxcs; i++) {
	    if(keyByCs[i] != null) {
		pa("CS "+i+": key: "+keyByCs[i]);
	    } else {
		pa("CS "+i+" ----------");
	    }
	}
    }

    public void keymapSingleCoordsys(int mine, int other) {
	keyByCs[mine].interpolateTo = other;
    }

    public int[] interpList(VobMatcher other0, boolean towardsOther) {
        DefaultVobMatcher other = (DefaultVobMatcher)other0;
	if(dbg) dump();
	if(dbg) other.dump();
	int[] interpList = new int[maxcs+1];
	if(dbg) pa("interplist: "+interpList[0]);
	int[] toOther = null;
	if(towardsOther) { // Construct inverse forced matches
	    toOther = new int[maxcs+1];
	    for(int i=0; i<other.maxcs+1; i++) {
		if(other.keyByCs[i] != null &&
		   other.keyByCs[i].interpolateTo > 0) {
		    toOther[other.keyByCs[i].interpolateTo] = i;
		}
	    }
	}
	for(int i=1; i<maxcs+1; i++) {
	    if(keyByCs[i] == null)
	        interpList[i] = DONT_INTERP;
	    else {
	        Key k = keyByCs[i];
		if(towardsOther) {
		    if(toOther[i] > 0)  {
			if(dbg) pa("Inverse overridden: "+i+" "+
				    toOther[i]);
			interpList[i] = toOther[i];
			continue;
		    }
		} else {
		    if(k.interpolateTo > 0) {
			int csTo = k.interpolateTo;
			Key kOther = other.keyByCs[csTo];
			// Need to check; otherwise can get core dumps
			if(kOther == null) {
			    pa("??!!! interp wrong");
			} else {
			    if(dbg) pa(i+" overridden into "+csTo);
			    interpList[i] = csTo;
			}
			continue;
		    }
		}

		if(k.parent < 0 || k.parent >= i) {
		    dumpSimply();
		    dumpByParent();
		    throw new Error("Got an invalid parent for "+i+" "+k.parent);
		}

		// XXX assumes that parent is set already
		int other_parent = interpList[k.parent];

		Object mappedKey = k.key; 
		Key other_key = new Key(mappedKey, other_parent);
		interpList[i] = other.getCSByKeyObject(other_key);

		if(dbg) pa(i+" "+k.parent+" "+other_parent+" "+
		           interpList[i]+" "+k.key);
	    }
	    if(dbg) pa(": "+interpList[i]);
	}
	return interpList;
    }

    private void checkIfKeyAlreadyExist(Key key) {
	for (int i=0; i<keyByCs.length; i++) {
	    if (keyByCs[i] == null) continue;
	    if (key.equals((Key)keyByCs[i])) {
		pa("KEY ALREADY EXIST!!! : " + key.key);
	    }
	}
    }


    public void dumpByEquals() {
	Vector keys = new Vector();

	for (int i=0; i<keyByCs.length; i++) {

	    // take key
	    Key key = keyByCs[i];
	    if (key == null) continue;

	    boolean already_in = false;
	    int found=-1;
	    // chek if it exit in any of the vectors.
	    for (int j=0; j<keys.size(); j++) {
		//pa(j+" dsaf ");

		Vector obj = (Vector)keys.get(j);
		if ( ((Key)obj.get(0)).equals(key)) {
		    already_in = true;
		    found = j;
		}
	    }
	    if (!already_in) { 
		keys.add(new Vector());
		((Vector)keys.get((keys.size()-1)) ).add(key);
	    } else {
		if (found >= 0)
		    ((Vector)keys.get(found)).add(key);
	    }
	}

	for (int i=0; i<keys.size()-1; i++) {
	    for (int j=0; j<((Vector)keys.get(i)).size(); j++) {
		if (keys.get(i) != null) 
		    pa(i+ " : " +((Key)((Vector)keys.get(i)).get(j)).toString() );
	    }
	}
    }	

    private void dumpParentRecursive(int cs, Vector shown) {
	shown.add(new Integer(cs));

	if (cs < 0) return;

	if (cs == 0) {
	    pa(cs + " :   BOTTOM  \n");
	    return;
	}

	if(keyByCs[cs] == null || keyByCs[cs] == NULL)
	    pa(cs + " :   NULL  ");

	pa(cs + " : " +((Key)keyByCs[cs]).toString() + " --->");
	//pa("       \\|/    ");
	if (((Key)keyByCs[cs]).parent != cs)
	    dumpParentRecursive(((Key)keyByCs[cs]).parent, shown);
	else pa("ARRRGH! wrong parent!");
	return;
    }


    public void dumpByParent(int cs) {
	if (keyByCs[cs] == null) {
	    pa(cs + " : is null");
	    return;
	}

	Vector shown = new Vector();

	pa("");
	dumpParentRecursive(cs, shown);
    }

    public void dumpByParent() {

	Vector shown = new Vector();

	for (int i=keyByCs.length-1; i>=0; i--) {
	    if (keyByCs[i] == null) {
		pa(i + " : is null");
		continue;
	    }

	    boolean already=false;
	    for(int j=0; j<shown.size(); j++) {
		if ( ((Integer)shown.get(j)).intValue() == i) already = true;
	    }

	    if (!already) {
		pa("");
		dumpParentRecursive(i, shown);
	    }
	}

	for (int i=0; i<keyByCs.length; i++) {
	    if (keyByCs[i] == NULL)
		pa(i + " : Real NULL");	    
	}
    }

    
    public void treeDump() { pa("dump as tree"); treeDump(0, 0); }
    private void treeDump(int cs, int indent) {
	for (int i=cs + 1; i < keyByCs.length; i++) {
	    if (keyByCs[i] != null && ((Key)keyByCs[i]).parent == cs) {
		// indent
		for(int j=0; j<indent; j++) 
		    System.out.print("  ");
		System.out.println("["+i+"] "+keyByCs[i]);
		treeDump(i, indent+1);
	    }
	}
    }

    public void dumpSimply() {
	for(int i=1; i<maxcs+1; i++) {
	    if(keyByCs[i] != null)
		pa(i+" "+keyByCs[i]);
	}
    }
}
