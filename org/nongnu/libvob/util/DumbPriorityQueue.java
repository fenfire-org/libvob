/*
DumbPriorityQueue.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 */
/*
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.util;
import java.util.*;

/** A simple implementation of a priority queue.
 * This current implementation is not the most efficient 
 * possible one,
 * by far.
 */
public class DumbPriorityQueue implements PriorityQueue {
    /** Compare two objects using the jobPriority value
     * as primary and hash code as secondary code.
     */
    private class Comp implements Comparator {
	public int compare(Object o1, Object o2) {
	    Float p1 = (Float)jobPriority.get(o1);
	    Float p2 = (Float)jobPriority.get(o2);
	    float f1 = p1.floatValue();
	    float f2 = p2.floatValue();
	    if(f1 < f2) return -1;
	    if(f1 > f2) return 1;
	    int i1 = o1.hashCode();
	    int i2 = o2.hashCode();
	    if(i1 < i2) return -1;
	    if(i1 > i2) return 1;
	    return 0;
	}
    }

    /** Job object to Float (the priority).
     */
    private Map jobPriority = new HashMap();

    /** Set of jobs, to be compared by their priorities.
     */
    private TreeSet jobs = new TreeSet(new Comp());

    public void add(Object job, float priority) {
	Float prevprio = (Float)jobPriority.get(job);
	if(prevprio != null) {
	    if(priority >= prevprio.intValue()) return;
	    // Now, in order not to corrupt the TreeSet, 
	    // we must FIRST remove the element, then change
	    // the priority and then re-add it after the priority
	    // has been changed.
	    jobs.remove(job);
	}
	jobPriority.put(job, new Float(priority));
	jobs.add(job);
    }

    public void remove(Object job) {
	// Can't just remove as it wouldn't have an entry
	// there.
	if(jobPriority.containsKey(job)) {
	    jobs.remove(job);
	    jobPriority.remove(job);
	}
    }

    public Object getAndRemoveLowest() {
	if(jobs.isEmpty()) return null;
	Object j = jobs.first();
	jobs.remove(j);
	jobPriority.remove(j);
	return j;
    }
}
