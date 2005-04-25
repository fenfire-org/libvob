/*
TextKeyController.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
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
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.fn.*;
import org.nongnu.libvob.*;
import java.util.*;

/** A key controller implementing simple text editing.
 */
public class TextKeyController extends AbstractDelegateLob {

    protected Model textModel, cursorModel;

    private TextKeyController() {}

    public static TextKeyController newInstance(Lob content, Model textModel,
						Model cursorModel) {
	TextKeyController c = (TextKeyController)FACTORY.object();
	c.delegate = content;
	c.textModel = textModel;
	c.cursorModel = cursorModel;
	return c;
    }

    public Lob wrap(Lob l) {
	return newInstance(l, textModel, cursorModel);
    }

    public boolean key(String key) {
	String text = (String)textModel.get();
	int tc = cursorModel.getInt();

	if(tc > text.length()) 
	    tc = text.length();

	WindowAnimation winAnim = Lobs.getWindowAnimation();

	if(key.length() == 1) {
	    if(tc < 0) tc = text.length();
	    textModel.set(text.substring(0, tc) + key + text.substring(tc));
	    cursorModel.set(tc+1);
	    winAnim.switchVS();
	    return true;
	} else if(key.equals("Enter")) {
	    if(tc < 0) tc = text.length();
	    textModel.set(text.substring(0, tc) + '\n' + text.substring(tc));
	    cursorModel.set(tc+1);
	    winAnim.switchVS();
	    return true;
	} else if(key.toLowerCase().equals("backspace")) {
	    if(tc < 0) tc = text.length();
	    if(tc == 0) return true;
	    textModel.set(text.substring(0, tc-1) + text.substring(tc));
	    cursorModel.set(tc-1);
	    winAnim.switchVS();
	    return true;
	} else if(key.equals("Left") || key.equals("Alt-Left")) {
	    if(tc < 0)
		tc = text.length();

	    if(tc == 0) return true;
	    cursorModel.set(tc-1);
	    winAnim.animate();
	    return true;
	} else if(key.equals("Right") || key.equals("Alt-Right")) {
	    if(tc < 0)
		tc = 0;

	    if(tc == text.length()) return true;
	    cursorModel.set(tc+1);
	    winAnim.animate();
	    return true;
	} else if(key.equals("Ctrl-C")) {
	    // PUI-copy the whole contents -- we don't have selecting, yet
	    org.nongnu.libvob.util.PUIClipboard.puiCopy(text);
	    return true;
	} else if(key.equals("Ctrl-V")) {
	    String str = org.nongnu.libvob.util.PUIClipboard.getText();

	    if(tc < 0) tc = text.length();
	    textModel.set(text.substring(0, tc) + str + text.substring(tc));
	    cursorModel.set(tc+str.length());
	    winAnim.switchVS();
	    return true;
	} else {
	    return delegate.key(key);
	}
    }

    public List getFocusableLobs() {
	return Lists.list(this);
    }

    private static final Factory FACTORY = new Factory() {
	    public Object create() {
		return new TextKeyController();
	    }
	};
}
