/*
AbstractHashtable.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *    This file is part of Fenfire.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Benja Fallenstein
 */

package org.nongnu.libvob.util;
import java.util.*;

/** An abstract superclass for custom, array-based hashtables.
 *  <p>
 *  Note: This class isn't very convenient to implement, and it's
 *  relatively easy to make mistakes. Read the documentation below closely.
 *  The point of this class is to allow the implementation of hashtables
 *  that don't require an object creation overhead per entry.
 *  This makes the internal interface of this class somewhat inconvenient.
 *  <p>
 *  To implement a hashtable based on this, in your subclass you need to:
 *  <ul>
 *  <li>For each field that you want to store per hashtable entry, define
 *      an array of that type. For example, to implement Map,
 *      define an <code>Object[] key</code> and an 
 *      <code>Object[] value</code> array.</li>
 *  <li>Define a constructor that initializes these arrays, all with
 *      the same size, and calls AbstractHashtable's constructor
 *      with that size.</li>
 *  <li>Implement a <code>void expandArrays(int size)</code> method that will
 *      create new arrays of the given size, <code>System.arraycopy</code>
 *      the contents of the current arrays into the new arrays, and
 *      then replace the current arrays by the new ones.</li>
 *  <li>Implement a <code>void hashCode(int entryIndex)</li> method that
 *      computes the hash code of an entry. For example, to implement Map,
 *      this would return <code>key[entryIndex].hashCode()</code>.</li>
 *  <li>Implement functions to add entries, search for entries, 
 *      remove entries, etc.</li>
 *  </ul>
 *  <p>
 *  To search for an entry, use the <code>first(int hashCode)</code> method
 *  to find the first entry with a particular hash code, and the
 *  <code>next(int entry)</code> method to find the next entry with the same
 *  hash code. Both methods return a value < 0 if there are no more entries
 *  with this hash. (Note: actually it's not necessarily the same hash code;
 *  this iterates through all entries whose hash code is mapped to the same
 *  index in the hashtable.) It's your responsibility to filter out those
 *  entries that you actually want (i.e., in the Map example, those whose key
 *  is equal to the key you're looking for, rather than those whose key
 *  only has the same hash code).
 *  <p>
 *  When adding a new entry, first of all, check whether it is already
 *  in the hashtable. If not, call <code>int newEntry()</code> to allocate
 *  an index in your arrays. Initialize the fields of the entry
 *  (i.e., set <code>arr[entryIndex]</code> for each of your arrays, where
 *  <code>entryIndex</code> is the index returned by <code>newEntry</code>).
 *  Then, call <code>put(entryIndex)</code>, which will put the new entry
 *  into the hashtable. You must not call <code>put()</code> 
 *  before initializing the entry, because it calls your 
 *  <code>hashCode()</code> function, which (presumably) can only return
 *  the correct value if the entry has been initialized.
 *  <p>
 *  To remove an entry, call <code>removeEntry(int index)</code>.
 *  Note that this makes the value of <code>next(int index)</code> undefined.
 *  Therefore, if you want to remove an entry while iterating through
 *  a chain of entries, you need to call <code>next()</code>
 *  <em>before</em> calling <code>removeEntry()</code>.
 */
public abstract class AbstractHashtable {
    private void p(String s) { System.out.println("AbstractHashtable:: "+s); }

    private int[] hashtable;

    private int firstUnusedEntry;
    private int[] nextEntry;

    private int size;


    // *** methods that subclasses need to override ***
    
    protected abstract int hashCode(int entryIndex);
    protected abstract void expandArrays(int newSize);
    


    // *** methods that subclasses need to call ***

    // constructor

    protected AbstractHashtable(int size) {
	nextEntry = new int[size];
	hashtable = new int[2*size];

	removeAllEntries(); // initialize to zero elements
    }


    // iterating through entries with a particular hash

    protected final int first(int hashCode) {
	return hashtable[hashToIndex(hashCode)];
    }
    protected final int next(int entry) {
	return nextEntry[entry];
    }


    // getting the number of entries

    protected final int getSize() {
	return size;
    }


    // memory management

    protected final int newEntry() {
	size++;

	if(firstUnusedEntry < 0)
	    expand(2*nextEntry.length);

	int result = firstUnusedEntry;
	firstUnusedEntry = nextEntry[result];
	//nextEntry[result] = USED;
	return result;
    }
    protected final void removeEntry(int entry) {
	size--;

	int hashIndex = hashToIndex(hashCode(entry));

	int i = hashtable[hashIndex];

	if(i == entry) {
	    hashtable[hashIndex] = nextEntry[entry];
	} else {
	    while(nextEntry[i] != entry)
		i = nextEntry[i];

	    nextEntry[i] = nextEntry[entry];
	}

	nextEntry[entry] = firstUnusedEntry;
	firstUnusedEntry = entry;
    }
    protected void removeAllEntries() {
	size = 0;

	Arrays.fill(hashtable, -1);
	for(int i=0; i<nextEntry.length-1; i++) nextEntry[i] = i+1;
	nextEntry[nextEntry.length-1] = -1;

	firstUnusedEntry = 0;
    }


    // putting entries into the hashtable

    /** Put an entry -- which must not be in the hashtable yet! --
     *  into the hashtable.
     */
    protected final void put(int entry) {
	int idx = hashToIndex(hashCode(entry));
	nextEntry[entry] = hashtable[idx];
	hashtable[idx] = entry;
    }



    // *** private methods ***

    // getting the hashtable index corresponding to a hash code

    private int hashToIndex(int hashCode) {
	int k = hashCode % hashtable.length;
	if(k < 0) k += hashtable.length;
	return k;
    }

    // expanding the arrays

    private void expand(int newSize) {
	expandArrays(newSize);

	int[] oldtable = hashtable;
	int[] oldnext = nextEntry;

	int[] nnext = new int[newSize];
	System.arraycopy(nextEntry, 0, nnext, 0, nextEntry.length);
	for(int i=nextEntry.length; i<newSize-1; i++) nnext[i] = i+1;
	nnext[newSize-1] = -1;

	firstUnusedEntry = nextEntry.length;
	nextEntry = nnext;

	hashtable = new int[2*newSize];
	Arrays.fill(hashtable, -1);

	for(int i=0; i<oldtable.length; i++)
	    for(int j=oldtable[i]; j>=0; j=oldnext[j])
		nextEntry[j] = -1;

	for(int i=0; i<oldtable.length; i++)
	    for(int j=oldtable[i]; j>=0; j=oldnext[j])
		put(j);
    }
}
