/*
VisibilityLob.java
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

/** A lob that renders or doesn't render its content depending 
 *  on the value of a boolean model.
 */
public class VisibilityLob extends AbstractMonoLob {

    protected Model visibility;

    public VisibilityLob(Lob content, Model visibility) {
	super(content);
	this.visibility = visibility;
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content, visibility };
    }
    protected Object clone(Object[] params) {
	return new VisibilityLob((Lob)params[0], (Model)params[1]);
    }

    public boolean mouse(VobMouseEvent e, float x, float y) {
	if(visibility.getBool()) 
	    return super.mouse(e, x, y);
	else
	    return false;
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {
	if(visibility.getBool())
	    content.render(scene, into, matchingParent, w, h, d,
			   visible);
    }
}
