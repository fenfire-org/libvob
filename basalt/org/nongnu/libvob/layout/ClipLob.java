/*
ClipLob.java
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
import org.nongnu.libvob.vobs.RectBgVob;
import java.awt.*;

public class ClipLob extends AbstractMonoLob {

    protected static final RectBgVob rectvob = new RectBgVob();
    protected static final Object UNCLIP = new Object();

    protected Object key;

    public ClipLob(Lob content) {
	this(content, null);
    }

    public ClipLob(Lob content, Object key) {
	super(content);
	this.key = key;
    }

    protected Object clone(Object[] params) {
	return new ClipLob((Lob)params[0], key);
    }
 
    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {

	this.scene=scene; this.w=w; this.h=h; this.d=d;
	this.visible = visible;
	this.matchingParent = matchingParent;

	if(key != null) {
	    this.cs = scene.coords.box(into, w, h);
	    scene.matcher.add(matchingParent, cs, key);
	} else {
	    this.cs = into;
	}

	GraphicsAPI api = GraphicsAPI.getInstance();
	if(api instanceof org.nongnu.libvob.impl.awt.AWTAPI) {
	    org.nongnu.libvob.impl.DefaultVobMap map = 
		(org.nongnu.libvob.impl.DefaultVobMap)scene.map;
	    map.clip(cs);
	    run_render.run();
	    map.unclip();
	} else if(api instanceof org.nongnu.libvob.impl.gl.GLAPI) {
	    org.nongnu.libvob.gl.Stencil.drawStenciled(
		scene,
		run_put_stencil,
		null,
		null,
		run_render,
		false
		);
	} else {
	    throw new Error("Unsupported API: "+api);
	}

    }

    protected VobScene scene;
    protected int cs, matchingParent;
    protected float w,h,d;
    protected boolean visible;

    private Runnable run_put_stencil = new Runnable() { public void run() {
	scene.map.put(rectvob, cs);
    }};

    private Runnable run_render = new Runnable() { public void run() {
	content.render(scene, cs, matchingParent, w, h, d, visible);
    }};
}
