/*
LobMonoLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

/** A LobLob that is a monolob.
 */
public class LobMonoLob extends LobLob implements MonoLob {

    public static final Object CONTENT =
	"http://fenfire.org/2004/07/layout/monoLobContent";

    protected LobMonoLob() {
    }

    public LobMonoLob(Lob delegate) {
	super(delegate);
    }

    protected Lob clone(Lob delegate, Map map) {
	return new LobMonoLob(delegate);
    }

    public void setContent(Lob newContent) {
	delegate.setTemplateParameter(CONTENT, newContent);
    }

    public Lob getContent() {
	return (Lob)delegate.getTemplateParameter(CONTENT);
    }
}
