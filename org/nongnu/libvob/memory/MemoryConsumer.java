/*
MemoryConsumer.java
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

package org.nongnu.libvob.memory;

/** An object representing an entity which would like
 * to consume a significant amount of memory (large image or such).
 * Memoryconsumers <b>must</b> work as hash keys; a new consumer with
 * the same meaning as a previous one must be found in a hash.
 * <p>
 * Quality is an important variable whose semantics are not defined in this interface
 * beyond that
 * <ul>
 * <li> It is a float
 * <li> Larger quality = more bytes
 * <li> May be interpolated: a quality between any two qualities that have been
 *      given to the MemoryPartitioner API may be given.
 * </ul>
 * an example of quality would be DPI (dots per inch) for images, or a mipmap level index
 * starting from 0 = (1x1).
 * (i.e. log(DPI)) .
 */
public interface MemoryConsumer {
    /** The maximum number of bytes this object would like to consume.
     * @param quality The quality at which the maximum bytes are requested.
     */
    int getMaxBytes(float quality);
    /** Set the amount of memory this object is allowed to consume.
     * <p>
     * <b>This method may only be called by {@link MemoryPartitioner}.</b>
     * @param priority The priority to use for scheduling
     * 			this operation.
     * @param bytes The maximum amount of bytes this object should reserve.
     * @param quality Maximum quality that should be loaded (if quality is
     * 		adjusted in discrete steps, the class may round upwards). 
     * @return The number of bytes this consumer will actually use: e.g. if
     * 		sizes come in certain increments, less memory will be used
     * 		than needed..
     */
    int setReservation(float priority,
			int bytes, float quality);
    /** Get the number of bytes currently used.
     */
    int getReservation();

    /** Get the quality currently used.
     */
    float getQuality();

}
