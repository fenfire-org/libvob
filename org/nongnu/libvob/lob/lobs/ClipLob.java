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
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.vobs.RectBgVob;
import java.awt.*;

public class ClipLob extends AbstractDelegateLob {

    protected static final RectBgVob rectvob = new RectBgVob();
    protected static final Object UNCLIP = new Object();

    protected Object key;

    private ClipLob() {};

    public ClipLob newInstance(Lob content) {
	return newInstance(content, null);
    }

    public ClipLob newInstance(Lob content, Object key) {
	ClipLob l = (ClipLob)FACTORY.object();
	l.delegate = content;
	l.key = key;
	return l;
    }

    public void render(Layout delegateLayout,
		       VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {

	this.delegateLayout = delegateLayout;
	this.scene=scene; this.d=d;
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

    protected Layout delegateLayout;
    protected VobScene scene;
    protected int cs, matchingParent;
    protected float d;
    protected boolean visible;

    private Runnable run_put_stencil = new Runnable() { public void run() {
	scene.map.put(rectvob, cs);
    }};

    private Runnable run_render = new Runnable() { public void run() {
	delegateLayout.render(scene, cs, matchingParent, d, visible);
    }};

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new ClipLob();
	    }
	};
}
