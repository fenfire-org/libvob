/*   
Label.java
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

public class Label extends LobLob {
    static public final Object 
	URI = "http://org.nongnu.libvob.layout.component.Label:80/";

    public static final Object[] PARAMS = {};

    public Label(String text) {
	this(text, true);
    }
    public Label(String text, boolean linebreaking) {
	this(text, Theme.getFont(), linebreaking);
    }

    public Label(Model stringModel) {
	this(stringModel, true);
    }
    public Label(Model stringModel, boolean linebreaking) {
	this(stringModel, Theme.getFont(), linebreaking);
    }

    public Label(String text, Model font) {
	this(text, font, true);
    }
    public Label(String text, Model font, boolean linebreaking) {
	this(new ObjectModel(text), font, linebreaking);
    }

    public Label(Model stringModel, Model font) {
	this(stringModel, font, true);
    }
    public Label(Model stringModel, Model font, boolean linebreaking) {
	stringModel = Parameter.model("XXX_string", stringModel);
	font = Parameter.model("XXX_font", font);

	TextModel textModel = new TextModel.StringTextModel(stringModel, font);
	setDelegate(makeSequence(textModel, linebreaking));
    }

    public Label(SequenceModel textModel) {
	this(textModel, true);
    }
    public Label(SequenceModel textModel, boolean linebreaking) {
	super(makeSequence(Parameter.sequenceModel("XXX_seqmodel", textModel), 
			   linebreaking));
    }


    private static Lob makeSequence(SequenceModel textModel, 
				    boolean linebreaking) {
	if(!linebreaking)
	    return new Box(X, textModel);
	else
	    return new Breaker(X, textModel);
    }
}
