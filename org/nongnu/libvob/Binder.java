/*
Binder.java
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
package org.nongnu.libvob;
import java.util.*;
import java.io.*;

/** An interface for receiving keystrokes and mouse events.
 * One binder is usually associated with each window.
 */
public interface  Binder {
    /** A keystroke.
     */
    void keystroke(String s);
    /** A mouse event.
     */
    void mouse(VobMouseEvent e);

    /** Set the Screen this Binder is connected to. To be used
     * only in Screen.Screen().
     */
    void setScreen(Screen s) ;

    /** The timeout (set separately) with the given id
     * expired.
     */
    void timeout(Object id);

    /** A repaint event was received.
     * The window should be repainted at the earliest opportunity
     * (during this call is fine).
     */
    void repaint();

    /** A window has been closed by the user.
     */
    void windowClosed();
}

