/*
Actions.java
 *    
 *    Copyright (c) 2005, Matti J. Katila
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
 * Written by Matti J. Katila
 */

package org.nongnu.libvob;
import org.nongnu.libvob.mouse.*;

/** Interface for actions that are performed on a mouse event on 
 *  visual objects found from the scene.
 */
public interface Actions {
    
    /** Try to found a proper action for the 
     *  event and perform it.
     */ 
    Action justPerform(VobMouseEvent event);

    /** 
     */
    Action execAction(int cs, VobMouseEvent event);

    
    /** Register an action for a mouse event 
     *  operated on specific coordinate system.
     */
    void put(int cs, Action action);


    /** Get registered action by coordinate system.
     */
    Action get(int cs);
}
