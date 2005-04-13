/*   
LobFont.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein.
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
 * Written by Benja Fallenstein
 */

package org.nongnu.libvob.lob;
import javolution.realtime.*;
import javolution.lang.Text;
import java.util.*;

public interface LobFont extends Realtime {
    // XXX use Functional system...

    /** 
     *  XXX In the future, we need to transform a list of characters into
     *  a list of glyphs -- characters don't really map 1:1 to glyphs,
     *  that's an oversimplifying hack. (Read about Unicode combining marks
     *  if you don't know about this, or just google for something like
     *  'unicode characters glyphs'.)
     */
    Lob getLob(char c);

    Lob getTextEndLob();


    List text(String s);
    List text(Text t);

    List textLn(String s);
    List textLn(Text t);
}
