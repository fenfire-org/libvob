/*   
TextCursorLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
 *
 *    This file is part of Fenfire.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
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
import org.nongnu.libvob.vobs.SimpleConnection;
import org.nongnu.navidoc.util.Obs;

public class TextCursorLob extends LobSequence {

    protected static final Object UPKEY = new Object(), LOWKEY = new Object();

    protected Model textCursorModel;
    protected Model showModel;

    public TextCursorLob(Sequence delegate, Model textCursorModel) {
	this(delegate, textCursorModel, new BoolModel(true));
    }

    public TextCursorLob(Sequence delegate, Model textCursorModel,
			 Model showModel) {
	super(delegate);
	this.textCursorModel = textCursorModel;
	this.showModel = showModel;
    }

    public Sequence clone(Sequence delegate) {
	return new TextCursorLob(delegate, textCursorModel, showModel);
    }

    public void setShowModel(Model showModel) {
	Model old = this.showModel;
	this.showModel = showModel;
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float w, float h, float d,
		       boolean visible) {
	super.render(scene, into, matchingParent, w, h, d, visible);

	if(!showModel.getBool()) return;

	int i = textCursorModel.getInt();
	if(i >= delegate.length()) i = delegate.length()-1;
	if(i < 0) i = 0;

	float x2 = delegate.getPosition(X, i), y2 = delegate.getPosition(Y, i);
	float h2 = delegate.getLob(i).getNatSize(Lob.Y);

	int upper = scene.coords.translate(into, x2, y2, -1);
	int lower = scene.coords.translate(into, x2, y2+h2, -1);

	scene.matcher.add(matchingParent, upper, UPKEY);
	scene.matcher.add(matchingParent, lower, LOWKEY);
	
	if(visible)
	    scene.put(new SimpleConnection(0,0,0,0), upper, lower);
    }
}
