/*   
ArrayVobMatcher.java
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
package org.nongnu.libvob.impl;
import org.nongnu.libvob.*;

/** An implementation of VobMatcher that does not create objects
 *  for every coordsys added, using arrays instead.
 */
public class ArrayVobMatcher implements VobMatcher {
    public static boolean dbg = false;
    private static void p(String s) { System.out.println(s); }

    public static final int MAX_MILLIS = 5;

    /** List of all coordinate systems that have been added */
    protected int ncs = 0;
    protected int[] csList = new int[16];

    /** The coordinate systems */
    protected int maxCS = -1;
    protected Object[] csKey = new Object[16];
    protected int[] csParent = new int[16];
    protected int[] csNextInHashtable = new int[16];

    /** The hashtable */
    protected int[] hashtable = new int[128];

    protected int hash(int parent, Object key) {
	int keyHash = key==null ? 24890 : key.hashCode();
	return 98247*parent ^ 314523*keyHash;
    }

    protected int csHash(int cs) {
	return hash(csParent[cs], csKey[cs]);
    }

    protected final int hashIndex(int hash) {
	if(hash < 0) hash = -hash;
	return hash % hashtable.length;
    }

    protected void addToHashtable(int cs) {
	int i = hashIndex(csHash(cs));
	for(int cs2=hashtable[i]; cs2>0; cs2=csNextInHashtable[cs2])
	    if(cs2==cs)
		throw new Error("XXX cycle in hashtable @ "+i+": "+cs+" "+csParent[cs]+" "+csKey[cs]);
	csNextInHashtable[cs] = hashtable[i];
	hashtable[i] = cs;
	ensureHashtable();
    }
    
    public int add(int cs, Object key) {
	return add(0, cs, key);
    }

    public int add(int parent, int cs, Object key) {
	ensureCSList(ncs+1);
	csList[ncs] = cs;
	ncs++;

	ensureMaxCS(cs);
	csParent[cs] = parent;
	csKey[cs] = key;
	addToHashtable(cs);
	return cs;
    }

    public int getCS(Object key) {
	return getCS(0, key);
    }

    public int getCS(int parent, Object key) {
	int i = hashIndex(hash(parent, key));
	for(int cs=hashtable[i]; cs>0; cs=csNextInHashtable[cs]) {
	    if(parent==csParent[cs] && equals(key, csKey[cs]))
		return cs;
	}
	return -1;
    }

    public int getParent(int cs) {
	return csParent[cs];
    }

    public boolean isAncestor(int cs, int ancestor) {
	while(cs != 0) {
	    cs = getParent(cs);
	    if(cs == ancestor) return true;
	}
	return false;
    }

    public Object getKey(int cs) {
	return csKey[cs];
    }

    public void clear() {
	ncs = 0;
	maxCS = -1;
	java.util.Arrays.fill(hashtable, 0);
	java.util.Arrays.fill(csKey, null);
    }

    protected int getOtherCS(VobMatcher other, int oparent, int mycs) {
	return other.getCS(oparent, csKey[mycs]);
    }

    public int[] interpList(VobMatcher other, boolean towardsOther) {
	long start = System.currentTimeMillis();

	int[] list = new int[maxCS+1];
	list[0] = 0; // interpolate 0 cs to 0 cs by default
	for(int i=1; i<maxCS+1; i++)
	    list[i] = SHOW_IN_INTERP;

	for(int i=0; i<ncs; i++) {
	    int cs = csList[i];
	    if(list[csParent[cs]] == SHOW_IN_INTERP) {
		System.out.println("parent showininterp");
		list[cs] = SHOW_IN_INTERP;
		continue;
	    }

	    // XXX assumes that parent is set already
	    int oparent = list[csParent[cs]];
	    if(oparent < 0) 
		list[cs] = DONT_INTERP;
	    else
		list[cs] = getOtherCS(other, oparent, cs);
	}

	long end = System.currentTimeMillis();
	if(dbg || end-start > MAX_MILLIS)
	    p("Time for interpList generation: "+(end-start)+" millis");

	//System.out.print("interpList: < ");
	//for(int i=0; i<maxCS+1; i++)
	//    System.out.print(i+":"+list[i]+" ");
	//System.out.println(" >.");

	return list;
    }

    protected static boolean equals(Object a, Object b) {
	if(a == null)
	    return b == null;
	else
	    return a.equals(b);
    }

    protected final void ensureCSList(int want) {
	if(want > csList.length)
	    expandCSList(2*want);
    }

    protected void expandCSList(int nlen) {
	int[] n = new int[nlen];
	System.arraycopy(csList, 0, n, 0, csList.length);
	csList = n;
    }

    protected final void ensureMaxCS(int want) {
	if(want > maxCS) {
	    maxCS = want;

	    int n = 16;
	    while(n < want+1) n *= 2;
	    
	    if(n > csKey.length) {
		expandMaxCS(n);
	    }
	}
    }

    protected void expandMaxCS(int n) {
	Object[] nkey = new Object[n];
	int[] nparent = new int[n];
	int[] nnext = new int[n];
	
	System.arraycopy(csKey, 0, nkey, 0, csKey.length);
	System.arraycopy(csParent, 0, nparent, 0, csKey.length);
	System.arraycopy(csNextInHashtable, 0, nnext, 0, csKey.length);
	
	csKey = nkey; csParent = nparent; csNextInHashtable = nnext;
    }

    protected void ensureHashtable() {
	if(hashtable.length < 2*ncs)
	    expandHashtable(4*ncs);
    }

    protected void expandHashtable(int n) {
	hashtable = new int[n];
	for(int i=0; i<ncs; i++)
	    addToHashtable(csList[i]);
    }
}
				      
