/*
TranslationLob.java
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

public class TranslationLob extends AbstractMonoLob {
    static private void p(String s) { System.out.println("TranslationLob:: "+s); }
    protected Model x, y;

    private VobScene scene = null;
    private int cs = -1;

    public TranslationLob(Lob content, Model x, Model y) {
	super(content);
	this.x = x; this.y = y;
	x.addObs(obs); y.addObs(obs);

	setChildSize();
    }

    protected Replaceable[] getParams() {
	return new Replaceable[] { content, x, y };
    }
    protected Object clone(Object[] params) {
	return new TranslationLob((Lob)params[0], (Model)params[1], 
				  (Model)params[2]);
    }

    public boolean mouse(VobMouseEvent e, float x, float y) {
	float myx = this.x.getFloat(), myy = this.y.getFloat();
	return content.mouse(e, x-myx, y-myy);
    }

    public void setSize(float w, float h) {
    }

    protected float childW, childH;

    public void chg() {
	super.chg();

	setChildSize();
    }

    protected void setChildSize() {
	childW = content.getNatSize(X);
	childH = content.getNatSize(Y);

	content.setSize(childW, childH);
    }

    protected Obs obs = new Obs() {
	    public void chg() {
		//p("cs:"+cs+", vs: "+scene);
		if (scene != null)
		    scene.coords.setTranslateParams(cs, 
						    x.getFloat(),
						    y.getFloat());
	    }
	};

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {

	this.scene = scene;
	this.cs = scene.coords.translate(into, 
					 this.x.getFloat(),
					 this.y.getFloat());

	content.render(scene, this.cs, matchingParent,
		       content.getNatSize(X), 
		       content.getNatSize(Y), d, visible);
    }
}
