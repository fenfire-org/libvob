/*
Axis.java
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

package org.nongnu.libvob.input;
import java.util.*;

/** A single input axis. 
 */
public interface Axis {
    /** Get the (user-readable) name of this axis.
     */
    String getName();

    /** Set the main listener.
     * There is only one main listener at a time - the previous one will be 
     * forgotten.
     */
    void setMainListener(AxisListener listener); 

    /** Set the state of this object; normal, calibrating or choosing.
     * @param state One of InputDeviceManager.STATE_*
     */
    void setState(int state);

    /** In the choosing state, returns the probability that this
     * axis has been chosen; some function of how much this
     * axis has been moved, between 0 and 1.
     */
    float getChoiceProbability();
}
