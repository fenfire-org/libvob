/*   
RDFLobFactory.java
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
import org.nongnu.navidoc.util.Obs;
import org.nongnu.libvob.layout.*;
import java.util.Comparator;

public class ComponentFactory {

    protected Model font;

    public ComponentFactory(Model font) {
	this.font = font;
    }

    public TextModel textModel(Model m) {
	return new TextModel.StringTextModel(m, font);
    }
    public TextModel textModel(Model m, Model key) {
	return new TextModel.StringTextModel(m, font, key);
    }
    public TextModel textModel(Object o, String slot) {
	return textModel(Models.adaptSlot(o, slot), new ObjectModel(o));
    }
    public TextModel textModel(Model m, Class c, String slot) {
	return textModel(Models.adaptSlot(m, c, slot), m);
    }

    public Label label(Model m) {
	return new Label(textModel(m));
    }
    public Label label(Object o, String slot) {
	return new Label(textModel(o, slot));
    }
    public Label label(Model m, Class c, String slot) {
	return new Label(textModel(m, c, slot));
    }

    public Label label(Model m, boolean linebreaking) {
	return new Label(textModel(m), linebreaking);
    }
    public Label label(Object o, String slot, boolean linebreaking) {
	return new Label(textModel(o, slot), linebreaking);
    }
    public Label label(Model m, Class c, String slot, boolean linebreaking) {
	return new Label(textModel(m, c, slot), linebreaking);
    }

    public TextField textField(Model m, Object key) {
	return new TextField(textModel(m), new ObjectModel(key));
    }
    public TextField textField(Object o, String slot, Object key) {
	return new TextField(textModel(o, slot), new ObjectModel(key));
    }
    public TextField textField(Model m, Class c, String slot, Object key) {
	return new TextField(textModel(m, c, slot), new ObjectModel(key));
    }

    public TextArea textArea(Model m, Object key) {
	return new TextArea(textModel(m), new ObjectModel(key));
    }
    public TextArea textArea(Object o, String slot, Object key) {
	return new TextArea(textModel(o, slot), new ObjectModel(key));
    }
    public TextArea textArea(Model m, Class c, String slot, Object key) {
	return new TextArea(textModel(m, c, slot), new ObjectModel(key));
    }

    public ListBox listBox(ListModel elements, Class c, String slot, 
			   Object key) {
	// XXX need this so that instantiating Label for the template
	// doesn't throw a NullPointerException... argl
	Model m0 = new ObjectModel(elements.get(0)); 

	Label template = 
	    label(Parameter.model(ListModel.PARAM, m0), c, slot, false);
	return new ListBox(elements, "template", template, 
			   "key", new ObjectModel(key));
    }
}
