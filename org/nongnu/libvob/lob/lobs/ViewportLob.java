/*
ViewportLob.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein
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
import org.nongnu.libvob.impl.IndexedVobMatcher;
import javolution.realtime.*;
import java.awt.Color;
import java.util.*;

public class ViewportLob extends AbstractDelegateLob {

    protected Axis scrollAxis;
    protected Object key;
    protected int intKey;

    protected float width, height;

    private ViewportLob() {}

    public static ViewportLob newInstance(Axis scrollAxis, Lob delegate,
					  Object key, int intKey) {
	ViewportLob l = (ViewportLob)LOB_FACTORY.object();
	l.delegate = delegate;
	l.scrollAxis = scrollAxis;
	l.key = key; l.intKey = intKey;
	l.width = -1; l.height = -1;
	return l;
    }

    public Lob wrap(Lob l) {
	return newInstance(scrollAxis, l, key, intKey);
    }

    public Lob layout(float w, float h) {
	Lob lob = delegate;

	if(lob.getLayoutableAxis() == scrollAxis.other())
	    lob = lob.layoutOneAxis(scrollAxis.other().coord(w, h));

	SizeRequest r = lob.getSizeRequest();
	if(scrollAxis == Axis.X)
	    lob = lob.layout(r.natW, h);
	else
	    lob = lob.layout(w, r.natH);

	ViewportLob vp = newInstance(scrollAxis, lob, key, intKey);
	vp.width = w; vp.height = h;
	return vp;
    }

    public SizeRequest getSizeRequest() {
	if(width < 0)
	    return delegate.getSizeRequest();
       
	return SizeRequest.newInstance(width, height);
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    if(key instanceof Realtime) ((Realtime)key).move(os);
	    return true;
	}
	return false;
    }

    float[] point = new float[3];
    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {
	
	if(width < 0 || height < 0)
	    throw new IllegalStateException("not layouted yet");
	
	SizeRequest r = delegate.getSizeRequest();

	int cs = scene.coords.translate(into, 0, 0);

	delegate.render(scene, cs, matchingParent, d, visible);

	if(r.width() <= width && r.height() <= height)
	    return;

	int focus;

	if(scene.matcher instanceof IndexedVobMatcher) {
	    IndexedVobMatcher m = (IndexedVobMatcher)scene.matcher;
	    focus = m.getCS(matchingParent, key, intKey);
	} else {
	    focus = scene.matcher.getCS(matchingParent, key);
	}

	if(focus >= 0) {
	    scene.coords.getSqSize(focus, point);

	    point[0] /= 2; 
	    point[1] /= 2;
	    point[2] = 0;

	    scene.coords.transformPoints3(focus, point, point);
	    scene.coords.inverseTransformPoints3(cs, point, point);

	    float x = point[0];
	    float y = point[1];

	    float pos = scrollAxis.coord(x, y);
	    float lobSize = scrollAxis.coord(r.width(), r.height());
	    float viewportSize = scrollAxis.coord(width, height);

	    float scroll = getScroll(pos, lobSize, viewportSize);

	    if(scrollAxis == Axis.X)
		scene.coords.setTranslateParams(cs, scroll, 0);
	    else
		scene.coords.setTranslateParams(cs, 0, scroll);
	}
    }

    protected float getScroll(float pos, float lobSize, float viewportSize) {
	float scroll;
	if(lobSize < viewportSize) {
	    float align = 0;
	    scroll = align*viewportSize - align*lobSize;
	} else if(pos < viewportSize/2)
	    scroll = 0;
	else if(pos > lobSize-(viewportSize/2))
	    scroll = viewportSize - lobSize;
	else
	    scroll = viewportSize/2 - pos;

	return scroll;
    }

    private static final Factory LOB_FACTORY = new Factory() {
	    public Object create() {
		return new ViewportLob();
	    }
	};
}
