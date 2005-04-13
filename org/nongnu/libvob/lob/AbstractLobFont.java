/*
AbstractLobFont.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein
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
import org.nongnu.libvob.lob.lobs.TextLobList;
import javolution.realtime.*;
import javolution.lang.Text;
import java.util.*;

public abstract class AbstractLobFont extends RealtimeObject 
    implements LobFont {

    public List text(String s) {
	return text(Text.valueOf(s));
    }

    public List text(Text text) {
	return TextLobList.newInstance(this, text);
    }

    public List textLn(String s) {
	return textLn(Text.valueOf(s));
    }

    public List textLn(Text text) {
	return Lists.concat(text(text), Lists.list(getTextEndLob()));
    }
}
