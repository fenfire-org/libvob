/*   
Frame.java
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.vobs.*;
import org.nongnu.navidoc.util.Obs;
import java.awt.Color;

public class Frame extends LobMonoLob {

    public static final String
	BG = "http://fenfire.org/2004/07/layout/frameBackground",
	FG = "http://fenfire.org/2004/07/layout/frameForeground";

    public Frame(Color bg, Color fg, float lineWidth, 
		 float margin, boolean is3d, boolean raised, boolean clip) {
	this(new NullLob(), bg, fg, lineWidth, margin, is3d, raised, clip);
    }

    public Frame(Lob content, Color bg, Color fg, float lineWidth, 
		 float margin, boolean is3d, boolean raised, boolean clip) {
	this(content, 
	     bg != null ? new ObjectModel(bg) : null,
	     fg != null ? new ObjectModel(fg) : null,
	     lineWidth, margin, is3d, raised, clip);
    }

    public Frame(Model bg, Model fg, float lineWidth, 
		 float margin, boolean is3d, boolean raised, boolean clip) {
	this(new NullLob(), bg, fg, lineWidth, margin, is3d, raised, clip);
    }

    public Frame(Lob content, Model bg, Model fg, float lineWidth, 
		 float margin, boolean is3d, boolean raised, boolean clip) {

	if(bg != null) bg = Parameter.model(BG, bg);
	if(fg != null) fg = Parameter.model(FG, fg);

	Lob l = Parameter.lob(CONTENT, content);
	l = new Margin(l, lineWidth+margin);
	if(clip) l = new ClipLob(l);
	Lob bgLob = (bg != null) ? 
	    (Lob)new VobLob(new FilledRectVob(bg), "BG") : NullLob.instance;
	Lob borderLob = (fg != null) && (lineWidth > 0) ? 
	    (Lob)new VobLob(new RectVob(fg, lineWidth, is3d, raised), "FG") 
	    : NullLob.instance;
	
	setDelegate(new Between(bgLob, l, borderLob));
    }

    public Model getBgModel() {
	return (Model)getParameter(BG);
    }

    public Model getFgModel() {
	return (Model)getParameter(FG);
    }

    public void setBgModel(Model bg) {
	setParameter(BG, bg);
    }

    public void setFgModel(Model fg) {
	setParameter(FG, fg);
    }

    public void chg() {
	super.chg();
    }
}
