/*   
TextComponent.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
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
 * Written by Benja Fallenstein
 */

package org.nongnu.libvob.layout.component;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.vobs.RectVob;
import org.nongnu.navidoc.util.Obs;
import java.awt.Color;

public abstract class TextComponent extends LobLob implements Component {

    public static final Object TEXT_MODEL =
	"http://fenfire.org/2004/07/layout/textModel-2004-12-09";

    public static final Object TEXT_CURSOR_MODEL =
	"http://fenfire.org/2004/07/layout/textCursorModel-2004-10-13";

    protected Axis scrollAxis;
    protected TextModel textModel;
    protected Model textCursorModel, lineModel, lineCountModel;

    private Breaker breaker;

    protected Model positionModel;

    protected TextComponent(Axis scrollAxis) {
	this.scrollAxis = scrollAxis;

	this.textCursorModel = Parameter.model(TEXT_CURSOR_MODEL,
					       new IntModel());
    }

    public Model getTextCursorModel() {
	return (Model)delegate.getTemplateParameter(TEXT_CURSOR_MODEL);
    }

    public void setTextCursorModel(Model model) {
	delegate.setTemplateParameter(TEXT_CURSOR_MODEL, model);
    }

    public TextModel getText() {
	return (TextModel)delegate.getTemplateParameter(TEXT_MODEL);
    }

    public void setText(TextModel value) {
	delegate.setTemplateParameter(TEXT_MODEL, value);
    }

    public Model getKey() {
	return (Model)delegate.getTemplateParameter(KEY);
    }

    public void setKey(Object value) {
	getKey().set(value);
    }

    public void setKey(Model value) {
	delegate.setTemplateParameter(KEY, value);
    }

    protected void init(Sequence sequence, TextModel textModel, 
			Model lineModel, Model lineCountModel, 
			boolean scrollbar, Model key) {

	textModel = Parameter.textModel(TEXT_MODEL, textModel);

	this.textModel = textModel;
	this.lineModel = lineModel;
	this.lineCountModel = lineCountModel;

	this.positionModel = sequence.positionModel(scrollAxis, 
						    textCursorModel);

	Lob lob;

	if(!scrollbar) {
	    lob = text(sequence);
	} else {
	    Box box = new Box(scrollAxis.other());
	    
	    box.addRequest(text(sequence), 0, 0, Float.POSITIVE_INFINITY);
	    box.add(scrollbar());

	    lob = box;
	}
	    
	lob = frame(lob);
	lob = new KeyLob(lob, Parameter.model(KEY, key));

	setDelegate(lob);
    }
 
    protected Lob frame(Lob content) {
	return new Frame(content, new ObjectModel(Color.white), 
			 Theme.darkColor, 1, 0, false, false, true);
    }

    protected Model visibleSize, natSize;

    protected Lob text(Sequence s) {
	s.setModel(textModel);

	TextEditController textEditController =
	    new TextEditController(s, textModel, textCursorModel, lineModel);

	TextCursorLob textCursorLob = 
	    new TextCursorLob(textEditController, textCursorModel,
			      textEditController.isFocusedModel());

	visibleSize = new FloatModel();
	natSize = new FloatModel();
	Lob lob = new SizeRequestWatcherLob(Lob.Y, textCursorLob, 
					    null, natSize, null);
	lob = new ViewportLob(scrollAxis, lob, positionModel);
	lob = new Margin(lob, 5);
	lob = new AssignedSizeWatcherLob(Lob.Y, lob, visibleSize);
	//lob = new ClipLob(lob);
	
	return lob;
    }

    protected Lob scrollbar() {
	Lob l = new Scrollbar(Lob.Y, lineModel, lineCountModel.minus(1),
			      visibleSize.divide(natSize));
	return new KeyLob(l, "SCROLLBAR");
    }

    protected TextComponent(Lob content) {
	super(content);
    }

    public Model getPositionModel() {
	return positionModel;
    }
}
