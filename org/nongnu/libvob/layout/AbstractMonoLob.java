/*
AbstractMonoLob.java
 *    
 *    Copyright (c) 2003-2004, Benja Fallenstein
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
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

/** A lob that contains one other lob.
 *  For example, something drawing a border around the contained lob.
 */
public abstract class AbstractMonoLob extends AbstractDelegateLob 
    implements MonoLob {

    protected Lob content;

    protected AbstractMonoLob() {
    }

    protected AbstractMonoLob(Lob content) {
	if(content != null)
	    setContent(content);
    }

    protected Lob getDelegate() {
	return content;
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content };
    }

    public void setContent(Lob content) {
	if(this.content != null) this.content.removeObs(this);
	this.content = content;
	content.addObs(this);
	chg();
    }
    
    public Lob getContent() {
	return content;
    }
}
