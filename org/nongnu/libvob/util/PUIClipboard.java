/*   
PUIClipboard.java
 *    
 *    Copyright (c) 2001-2003, Tuomas Lukka
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
 * Written by Tuukka Hastrup by cutting and pasting from old code 
 * by Tuomas Lukka
 */
package org.nongnu.libvob.util;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.awt.datatransfer.*;

/** Handle copy and paste of PUI clipboard
 */
public class PUIClipboard {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    static private Clipboard getClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /** Get the text content in the PUI clipboard.
     */
    static public String getText()
    {
        Clipboard clipboard = getClipboard();
        Transferable content = clipboard.getContents(new Object());
        String s = "";
        if(content != null) {
	    try {
                s = (String) content.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
		if(!dbg)
		    pa("Paste failed: "+e.toString());
		else
		    e.printStackTrace();
		return null;
	    }
        }
        return s;
    }

    /** Copy the string into the PUI clipboard (overwrites the
     * existing content).
     */
    static public void puiCopy(String str) {
        if(dbg) pa("PuiCOPY '"+str+"'");
        Clipboard clipboard = getClipboard();
        StringSelection contents = new StringSelection(str);
        clipboard.setContents(contents, new ClipboardOwner() {
            public void lostOwnership(Clipboard cb, Transferable t) {}
        });
    }
}
