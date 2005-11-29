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

    public static ClipLob newInstance(Lob content) {
	return newInstance(content, "clip-cs");
    }

    public static ClipLob newInstance(Lob content, Object key) {
	ClipLob l = (ClipLob)FACTORY.object();
	l.delegate = content;
	l.key = key;
	return l;
    }

    protected Lob wrap(Lob l) {
	return newInstance(l, key);
    }

    public Lob layout(float w, float h) {
	return newInstance(delegate.layout(w, h), key);
    }

    public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			 float x, float y) {
	
	/* // not currently, because the matching parent isn't changed
	 * // (see below)
	if(key != null)
	    cs = scene.matcher.getCS(cs, key);
	*/

	return delegate.mouse(e, scene, cs, x, y);
    }


    private static int clipparent = -1; // ARGH!!!
    private static int clipparent_matchingparent = -1;

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {

	SizeRequest s = delegate.getSizeRequest();
	float w = s.width(), h = s.height();
	
	this.scene=scene; this.d=d;
	this.visible = visible;
	this.matchingParent = matchingParent;

	int oldcp = clipparent, oldcpmp = clipparent_matchingparent;

	if(key != null) {
	    this.cs = scene.coords.box(into, w, h);

	    //this.matchingParent = cs; // don't change the matching parent
	    // instead, this ugly hack:
	    if(clipparent_matchingparent == matchingParent) {
		scene.matcher.add(clipparent, cs, key);
	    } else {
		scene.matcher.add(matchingParent, cs, key);
	    }

	    clipparent = cs;
	    clipparent_matchingparent = matchingParent;

	    //scene.matcher.add(matchingParent, cs, key);
	} else {
	    this.cs = into;
	}

	GraphicsAPI api = GraphicsAPI.getInstance();
	if(api instanceof org.nongnu.libvob.impl.awt.AWTAPI ||
	    api instanceof org.nongnu.libvob.impl.terminal.TERMINALAPI) {
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

	clipparent = oldcp;
	clipparent_matchingparent = oldcpmp;
    }

    protected VobScene scene;
    protected int cs, matchingParent;
    protected float d;
    protected boolean visible;

    private Runnable run_put_stencil = new Runnable() { public void run() {
	scene.map.put(rectvob, cs);
    }};

    private Runnable run_render = new Runnable() { public void run() {
	delegate.render(scene, cs, matchingParent, d, visible);
    }};

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new ClipLob();
	    }
	};
}
