/*
PriorityQueue.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.util;
import java.util.*;

/** A simple implementation of a priority queue, for background
 * jobs.
 * <p>
 * This interface and its implementations are not synchronized,
 * analogous to java.util Containers!
 */
public interface PriorityQueue {
    /** Add a job to run.
     * If the job has already been added, set the priority
     * to the lower value (more important) of the two.
     */
    void add(Object job, float priority) ;

    /** Remove a job from the queue.
     */
    void remove(Object job) ;

    /** Get the most important (with lowest numeric priority) job and remove
     * it from this queue.
     * If there are several jobs with the same importance,
     * no guarantee is made of which job is returned.
     * Subclasses *may* make their own guarantees about this.
     */
    Object getAndRemoveLowest() ;
}
