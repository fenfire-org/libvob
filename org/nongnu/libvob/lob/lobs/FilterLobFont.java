/*   
FilterLobFont.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
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

package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.lob.*;
import javolution.realtime.*;
import javolution.util.*;
import java.util.*;

public class FilterLobFont extends RealtimeObject implements LobFont {

    protected LobFont delegate;
    protected FastMap glyphs;

    private FilterLobFont() {}

    public static FilterLobFont newInstance(LobFont delegate, FastMap glyphs) {
	FilterLobFont f = (FilterLobFont)FACTORY.object();
	f.delegate = delegate;
	f.glyphs = glyphs;
	return f;
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    glyphs.move(os);
	    return true;
	}
	return false;
    }

    public static FilterLobFont newInstance(LobFont delegate) {
	FastMap glyphs = FastMap.newInstance();

	Lob space = delegate.getLob(' ');

	SizeRequest r = space.getSizeRequest();

	float w = r.natW, h = r.natH;

	space = RequestChangeLob.newInstance(space, w, w, 2*w, 0, h, h);

	Lob strut = Glue.newInstance(0, 0, 0, 0, h, h);

	space = BreakPoint.newInstance(Axis.X, space, 0, strut, null, null);

	Lob lineEnd = Glue.newInstance(0, 0, SizeRequest.INF, h, h, h);

	Lob glue = Glue.newInstance(Axis.X, 25, 25, 25);
	Lob newline = // forced break followed by indent
	    BreakPoint.newInstance(Axis.X, lineEnd, Breakable.INF, 
				   lineEnd, null, glue);

	FastInt ch = FastInt.newInstance((int)' ');
	glyphs.put(ch, space);

	ch = FastInt.newInstance((int)'\n');
	glyphs.put(ch, newline);

	return newInstance(delegate, glyphs);
    }

    public Lob getLob(char c) {
	FastInt ch = FastInt.newInstance(c);
	Lob l = (Lob)glyphs.get(ch);

	if(l != null) 
	    return l;
	else
	    return delegate.getLob(c);
    }

    private static Factory FACTORY = new Factory() {
	    protected Object create() {
		return new FilterLobFont();
	    }
	};
}
