/*   
Scrollbar.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
 *
 *    This file is part of Fenfire.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.layout.component;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.vobs.*;
import org.nongnu.navidoc.util.Obs;
import java.awt.Color;

public class Scrollbar extends LobLob {

    public static final String 
	POSITION_MODEL = "http://fenfire.org/2004/07/layout/scrollbarPosModel",
	MAXIMUM_MODEL = "http://fenfire.org/2004/07/layout/scrollbarMaxModel",
	KNOBFRACTION_MODEL = "http://fenfire.org/2004/07/layout/scrollbarKnobFractionModel",
	MIN_KNOB_SIZE_MODEL = "http://fenfire.org/2004/07/layout/scrollbarMinKnobSizeModel";

    protected static final float LARGE = 1000000000f;

    protected Axis axis;
    protected Model positionModel;
    protected Model maximumModel;
    protected Model knobFractionModel;

    protected Model fract1, fract2, knobFract;

    public void chg() {
	// do NOT propagate events! we know we never change our
	// requested size. (although this isn't really the right way
	// to prevent change events from being propagated; someone
	// should notice that our requested size hasn't actually changed,
	// and not propagate the event because of that, rather than
	// achieving that behavior by hand. XXX)
    }

    /**
     * @param value the value model giving the minimum size of the 
     *              scrolling knob in pixels
     */
    public void setMinKnobSize(Model value) {
	delegate.setTemplateParameter(MIN_KNOB_SIZE_MODEL, value);
    }

    public Scrollbar(Axis axis, Model positionModel, Model maximumModel) {
	this(axis, positionModel, maximumModel, 
	     new FloatModel(0.0f)); // constantly minimum size knob
	setMinKnobSize(new IntModel(15));
    }

    /**
     * @param axis
     * @param positionModel 0...max - otherwise outside of positions
     * @param maximumModel 0...
     * @param knobFractionModel 0... - limited below 1
     */
    public Scrollbar(Axis axis, Model positionModel, Model maximumModel, 
		     Model knobFractionModel) {
	float nan = Float.NaN, inf = Float.POSITIVE_INFINITY;

	positionModel = Parameter.model(POSITION_MODEL, positionModel);
	maximumModel = Parameter.model(MAXIMUM_MODEL, maximumModel);
	knobFractionModel = 
	    Parameter.model(KNOBFRACTION_MODEL, knobFractionModel);

	positionModel = Models.max(positionModel, new IntModel(0));
	knobFractionModel = Models.min(knobFractionModel, new IntModel(1));

	this.axis = axis;
	this.positionModel = positionModel;
	this.maximumModel = maximumModel;
	this.knobFractionModel = knobFractionModel;

	// knob and glue together fill the whole bar
	Model glueSizeModel = (new FloatModel(1)).minus(knobFractionModel);

	// XXX we assume knobFraction is the visible fraction
	// XXX we assume elements are of equal size
	Model visiblePositions = knobFractionModel.times(maximumModel);

	// scrollPosition is 0 until something above is not visible
	Model scrollPosition = positionModel.minus(visiblePositions
						   .divide(new IntModel(2)));
	scrollPosition = Models.max(scrollPosition, new IntModel(0));
	Model scrollMaximum = maximumModel.minus(visiblePositions);
	// scrollPosition is scrollMaximum after nothing below is invisible
	scrollPosition = Models.min(scrollPosition, scrollMaximum);

	this.fract1 = scrollPosition.divide(scrollMaximum, 0);
	this.fract2 = (new FloatModel(1)).minus(fract1);

	// glueSize is divided between fract1 and fract2
	this.fract1 = this.fract1.times(glueSizeModel);
	this.fract2 = this.fract2.times(glueSizeModel);

	this.knobFract = knobFractionModel;

	Box box = new Box(axis);
	Frame frame = new Frame(Theme.darkColor, Theme.darkColor, 1, 0, 
				true, false, false);

	Box outerBox = new Box(axis);
	outerBox.add(button(-1), "UP-BUTTON");
	outerBox.add(new Between(frame, 
				 new ClipLob(box), 
				 NullLob.instance));
	outerBox.add(button(+1), "DOWN-BUTTON");

	box.add(glue(-1));
	box.add(middle(), "KNOB");
	box.add(glue(+1));


	Lob delegate = outerBox;
	delegate = new RequestChangeLob(delegate, 15, 15, 15, nan, nan, inf);
	
	setDelegate(delegate);
    }

    private Lob glue(int dir) {
	Model zero = new FloatModel(0);
	Model fract = (dir<0) ? fract1 : fract2;

	Lob glue = new RequestChangeLob(axis, NullLob.instance, 
					zero, zero, fract.times(LARGE));
	return clickListener(glue, dir);
    }

    private Lob middle() {
	Model min = Parameter.model(MIN_KNOB_SIZE_MODEL, new FloatModel(10));
	Lob buttonRect = buttonRect(Float.NaN, Float.NaN, Float.NaN);
	return new RequestChangeLob(axis, buttonRect, min, min, 
				    knobFract.times(LARGE));
    }
	
    private Lob button(int dir) {
	Lob button = buttonRect(15, 15, 15);
	return clickListener(button, dir);
    }

    private Lob buttonRect(float min, float nat, float max) {
	Lob rect = rect(min, nat, max);
	rect = new KeyLob(rect, "inner");
	rect = new Frame(rect, null, Theme.darkColor, 1, 0, 
			 false, false, false);
	return rect;
    }

    private Lob rect(float min, float nat, float max) {
	Lob rect = new Frame(NullLob.instance, Theme.lightColor, 
			     Theme.darkColor, 1, 0, true, true, false);

	return new RequestChangeLob(axis, rect, min, nat, max);
    }

    private static class ClickAction extends AbstractAction {
	protected Model positionModel, maximumModel;
	protected int dir;
	protected ClickAction(Model positionModel, Model maximumModel,
			      int dir) {
	    this.positionModel = positionModel;
	    this.maximumModel = maximumModel;
	    this.dir = dir;
	}
	protected Replaceable[] getParams() { 
	    return new Replaceable[] { positionModel, maximumModel };
	}
	protected Object clone(Object[] params) { 
	    return new ClickAction((Model)params[0], (Model)params[1], dir);
	}
	public void run() {
	    float min = 0, max = maximumModel.getFloat();
	    float value = positionModel.getFloat() + dir;
		    
	    if(value > max) value = max;
	    if(value < min) value = min;
	    
	    positionModel.setFloat(value);
	}
    }

    private Lob clickListener(Lob content, int dir) {
	Action action = new ClickAction(positionModel, maximumModel, dir);
	return new ClickController(content, 1, action);
    }

    protected Scrollbar(Lob content) {
	super(content);
    }

    public Lob clone(Lob content) {
	return new Scrollbar(content);
    }
}
