/*
PinStub.java
 *
 *    Copyright 2003, Asko Soukka
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
 */
/*
 * Written by Asko Soukka
 */
package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;
import java.lang.Math;
import java.awt.*;

/**Pin like Stub vob for marking hidden connections. */

public class PinStub extends AbstractVob {

    /** Pin's start and end coordinates. */
    private float x0, y0, x1, y1;

    /** Pin's drawing color. */
    private Color color;

    /** Pin's stick's length factor. The length is relative to
     * the distance of vobs coordinates after coordinate system
     * transformations.
     */
    private float f;

    /** Pin's radius. */
    private float r;

    /** Pin like stub
     * @param x0 X-coordinate before first CS transformation.
     * @param y0 Y-cordinate before first CS transformation.
     * @param x1 X-coordinate before second CS transformation.
     * @param y1 Y-coordinate before second CS transformation.
     * @param color Drawing color.
     * @param f Pin's stick's length factor.
     * @param r Pin's radius.
     */
    public PinStub(float x0, float y0, float x1, float y1,
		   Color color, float f, float r) {
	super();
	this.x0 = x0;
	this.y0 = y0;
	this.x1 = x1;
	this.y1 = y1;
	this.color = color;
	this.r = r;
	this.f = f;
    }
    public PinStub(float x0, float y0, float x1, float y1) {
	this(x0, y0, x1, y1, null);
    }
    public PinStub(float x0, float y0, float x1, float y1,
		   Color color) {
	/** Default values for pin's lengt factor and radius. */
	this(x0, y0, x1, y1, color, 0.2f, 2f);
    }

    public void render(Graphics g,
		       boolean fast,
		       Vob.RenderInfo info1,
		       Vob.RenderInfo info2) {
	if(fast) return;
	if(color != null) g.setColor(color);

	/** Transforms points through given transformations. */
	int px0 = info1.box_x(x0),
	    py0 = info1.box_y(y0),
	    px1 = info2.box_x(x1),
	    py1 = info2.box_y(y1);

	/** The distance between transformed points. */
	double len = Math.sqrt(Math.pow(px1-px0, 2)
			       + Math.pow(py1-py0, 2));

	/** The end coordinates of PinStub. The length of pin is
	 * the distance between transformed point in factor f.
	 */
	int dx = (int)(((1-f)*len*px0 + (f)*len*px1)/len);
	int dy = (int)(((1-f)*len*py0 + (f)*len*py1)/len);

	g.drawLine(px0, py0, dx, dy);
	g.fillOval((int)(dx-r), (int)(dy-r), (int)(r*2), (int)(r*2));
    }

    Vob pin;

    public int putGL(VobScene vs, int coordsys1, int coordsys2) {
	if(pin == null) pin = GLRen.createPinStub(x0, y0, x1, y1, f, r);
	vs.map.put(pin, coordsys1, coordsys2);
	return 0;
    }
}
