/*
DepthChangeLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
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

public class DepthChangeLob extends AbstractMonoLob {

    protected float depthChange;

    public DepthChangeLob(Lob content, float depthChange) {
	super(content);
	this.depthChange = depthChange;
    }

    public float getDepthChange() { return depthChange; }
    public void setDepthChange(float dc) { depthChange = dc; chg(); }

    protected Object clone(Object[] params) {
	return new DepthChangeLob((Lob)params[0], depthChange);
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float x, float y, float w, float h, float d,
		       boolean visible) {
	int cs = scene.coords.translate(into, x, y, depthChange);
	content.render(scene, cs, matchingParent, 0, 0, w, h, d, visible);
    }
}
