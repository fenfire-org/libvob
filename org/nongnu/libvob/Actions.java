// (c): Matti J. Katila

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
