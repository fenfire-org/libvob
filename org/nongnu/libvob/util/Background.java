/*   
Background.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 * Written by Tuomas Lukka
 */


package org.nongnu.libvob.util;

/** Perform tasks in the background, in a queue.
 * E.g. loading images.
 * <p>
 * If a task needs to be run again and again, it <b>can</b>
 * add itself: while adding a task twice before it is run
 * a single time will not cause it
 * to run twice, a task adding itself while it is running
 * will run again.
 */

public interface Background {
    /** Add a new task to be run in the background.
     * If the task has already been added, and has not
     * been started, the priority will be set to the lower number
     * (higher priority) of the two, but the task will only
     * be run once.
     */
    void addTask(Runnable r, float priority) ;

    /** Remove the given task from the queue.
     * Due to synchronization issues, this does not guarantee that the given
     * task will not be run -- some implementations may have that property,
     * others may not.
     */
    void removeTask(Runnable r);
}
