/*   
SingleCheckBox.java
 *    
 *    Copyright (c) 2004, Matti J. Katila
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
 * Written by Matti J. Katila
 */
package org.nongnu.libvob.layout.component;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.vobs.*;
import org.nongnu.navidoc.util.Obs;
import java.awt.Color;

public class SingleCheckBox extends LobLob {

    private static LobFont font;
    private static LobFont getFont() {
	if(font == null) font = new LobFont("SansSerif", 0, 12, 
					    java.awt.Color.black);

	return font;
    }

    public SingleCheckBox() {
	this(new BoolModel(false));
    }
    public SingleCheckBox(Model activated) {
	this(new Frame(Theme.lightColor, Theme.darkColor,
		       1,0,true, true, false), 
	     getFont().getLabel("X"), activated);
    }
    public SingleCheckBox(MonoLob bg, Lob fill, Model activated) {
	activated = Parameter.model(CheckBox.CHECKED, activated);

	fill = new AlignLob(fill, .5f, .5f, .5f, .5f);
	bg.setContent(new VisibilityLob(fill, activated));
	Action chg = new Model.Change(activated,
				      activated.select(new BoolModel(false), 
						       new BoolModel(true)));
	bg = new ClickController(bg, 1, chg);
	setDelegate(bg);
    }
    
}
