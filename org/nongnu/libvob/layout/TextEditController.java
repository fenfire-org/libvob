/*   
TextEditController.java
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
package org.nongnu.libvob.layout;
import org.nongnu.libvob.AbstractUpdateManager;
import org.nongnu.libvob.VobMouseEvent;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public class TextEditController extends LobSequence {

    protected TextModel textModel;
    protected Model textCursorModel;
    protected Model lineModel;

    protected Model focusModel;

    public TextEditController(Sequence delegate, TextModel textModel, 
			      Model textCursorModel, Model lineModel) {
	super(delegate);
	this.textModel = textModel;
	this.textCursorModel = textCursorModel;
	this.lineModel = lineModel;
    }

    protected Sequence clone(Sequence delegate) {
	return new TextEditController(delegate, textModel, textCursorModel,
				      lineModel);
    }

    public List getFocusableLobs() {
	return Collections.nCopies(1, this);
    }

    public Model isFocusedModel() {
	return new AbstractModel.AbstractBoolModel() {
		public boolean getBool() {
		    if(focusModel == null) return false;
		    return TextEditController.this.equals(focusModel.get());
		}
	    };
    }

    public void setFocusModel(Model focusModel) {
	this.focusModel = focusModel;
    }

    public boolean mouse(VobMouseEvent e, float x, float y) {
	if(e.getButton() == 1 && e.getType() == e.MOUSE_PRESSED) {
	    textCursorModel.setInt(delegate.getCursorIndexAt(x, y));
	    if(focusModel != null) focusModel.set(this);
	    AbstractUpdateManager.chg();
	    return true;
	} else {
	    return false;
	}
    }

    public boolean key(String key) {
	int length = textModel.size() - 1;
	int textCursor = textCursorModel.getInt();
	
	if(textCursor < 0 || textCursor > length) 
	    textCursor = length;
	
	if(key.length() == 1) {
	    if(key.charAt(0) == 65535) {
		System.out.println("'Key' 0xffff -- ignore");
		return true;
	    }	       

	    textModel.insert(textCursor, key);
	    textCursor++;
	    textCursorModel.setInt(textCursor);
	    AbstractUpdateManager.setNoAnimation();
	} else if(key.equals("Enter")) {
	    textModel.insert(textCursor, "\n");
	    textCursor++;
	    textCursorModel.setInt(textCursor);
	} else if(key.equals("Backspace") || key.equals("BackSpacE")) {
	    if(textCursor <= 0) return true;
	    textModel.delete(textCursor-1, textCursor);
	    textCursor--;
	    textCursorModel.setInt(textCursor);
	    AbstractUpdateManager.setNoAnimation();
	} else if(key.equals("Ctrl-V")) {
	    // XXX allow timl!
	    String clipboardText = 
		org.nongnu.libvob.util.PUIClipboard.getText();

	    textModel.insert(textCursor, clipboardText);
	    textCursor += clipboardText.length();
	    textCursorModel.setInt(textCursor);
	    //AbstractUpdateManager.setNoAnimation();
	} else if(key.equals("Left") || key.equals("Alt-Left")) {
	    textCursor--;
	    if(textCursor < 0) textCursor = 0;
	    textCursorModel.setInt(textCursor);
	} else if(key.equals("Right") || key.equals("Alt-Right")) {
	    textCursor++;
	    if(textCursor > length) textCursor = length;
	    textCursorModel.setInt(textCursor);
	} else if(key.equals("Down") || key.equals("Alt-Down")) {
	    if(lineModel != null)
		lineModel.setInt(lineModel.getInt() + 1);
	} else if(key.equals("Up") || key.equals("Alt-Up")) {
	    if(lineModel != null)
		lineModel.setInt(lineModel.getInt() - 1);
	} else if(key.equals("Shift-Left")) {
	    textCursor -= 100;
	    if(textCursor < 0) textCursor = 0;
	    textCursorModel.setInt(textCursor);
	} else if(key.equals("Shift-Right")) {
	    textCursor += 100;
	    if(textCursor > length) textCursor = length;
	    textCursorModel.setInt(textCursor);
	} else {
	    return delegate.key(key);
	}

	AbstractUpdateManager.chg();
	return true;
    }
}
