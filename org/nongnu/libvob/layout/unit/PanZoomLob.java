/*
PanZoomLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *                  2004, Matti J. Katila
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
 * Written by Benja Fallenstein and Matti J. Katila
 */
package org.nongnu.libvob.layout.unit;
import org.nongnu.navidoc.util.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.layout.*;

public class PanZoomLob extends AbstractMonoLob {
    static public boolean dbg = false;
    private void p(String s) { System.out.println("PanZoomLob:: "+s); }

    protected Model panX, panY, zoom;

    private VobScene scene = null;
    private int cs = -1;

    public PanZoomLob(Lob content, Model zoom) {
	this(content, new FloatModel(), new FloatModel(), zoom);
    }
    public PanZoomLob(Lob content, Model panX, Model panY, Model zoom) {
	super(content);
	this.panX = panX; this.panY = panY;
	this.zoom = zoom;

	panX.addObs(obs); panY.addObs(obs); zoom.addObs(obs);

	content.setSize(content.getNatSize(X),
			content.getNatSize(Y));
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content, panX, panY, zoom };
    }
    protected Object clone(Object[] params) {
	return new PanZoomLob((Lob)params[0], (Model)params[1], 
				  (Model)params[2], (Model)params[3]);
    }

    public boolean mouse(VobMouseEvent e, float x, float y) {
	x = x - w/2f;
	y = y - h/2f;

	x /= zoom.getFloat();
	y /= zoom.getFloat();

	x += panX.getFloat();
	y += panY.getFloat();


	if (dbg) p("mouse2: x: "+x+", y: "+y+
		   ", pan: ("+panX.getFloat()+
		   ", "+panY.getFloat()+
		   "), zoom: "+zoom.getFloat());
	return content.mouse(e, x,y);
    }

    public void setSize(float w, float h) {
    }

    public void chg() {
	super.chg();
	content.setSize(content.getNatSize(X),
			content.getNatSize(Y));
    }

    protected Obs obs = new Obs() {
	    public void chg() {
		if (scene != null)
		    setZoomPan();
	    }
	};

    protected void setZoomPan() {
	scene.coords.setOrthoBoxParams(box2plane, 0, 
		panX.getFloat() - w/zoom.getFloat()/2f, 
		panY.getFloat() - h/zoom.getFloat()/2f, 
		1/zoom.getFloat(),
		1/zoom.getFloat(),
	        w,h);
    }

    protected int box2plane = -1;
    protected float x=0,y=0, w=0,h=0;
    protected final Object KEY = new Object();
    protected final Object KEY_INV = new Object();
    protected final Object KEY_CONCAT = new Object();

    public void render(VobScene scene, int into, int matchingParent,
		       float x, float y, float w, float h, float d,
		       boolean visible) {
	this.scene = scene;
	this.w = w;
	this.h = h;
	into = scene.coords.box(into, x, y, w, h);

	int box2screen = into;
	box2plane = scene.coords.orthoBox(0,0,0,0,0,0,0,0);
	scene.matcher.add(box2screen, box2plane, KEY);
	setZoomPan();
	int plane2box = scene.invertCS(box2plane, KEY_INV);
	int plane2screen = scene.concatCS(box2screen, KEY_CONCAT,
				       plane2box);

	content.render(scene, plane2screen, matchingParent, 0, 0,
		       content.getNatSize(X), 
		       content.getNatSize(Y), d, visible);
    }
}
