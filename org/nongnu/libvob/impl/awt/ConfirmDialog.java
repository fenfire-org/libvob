/*
ConfirmDialog.java
 *    This file is part of Gzz.
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
 * Written by Tero Maeyraenen (ae = a umlaut! Fixed for gcj)
 */

package org.nongnu.libvob.impl.awt;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

/**
 *	The ConfirmDialog is a simple awt-dialog that displays a question and
 *	two possible choices, "Yes" and "No".  This is something of a breakaway
 *	from the ZigZag-paradigm into "PUI", so it is not recommended to use this
 *	anywhere.  The reason it is here is to make it possible to have another
 *	nasty feature, which is the "system" primitive in clasm.  This dialog
 *	is used to confirm all "system" actions with the user first.
 */

public class ConfirmDialog extends Dialog {

    private Button yes = new Button("Yes");
    private Button no = new Button("No");
    private boolean result = false;
    private Panel textPanel = new Panel();
    private ConfirmDialog dlg = this;

    /**
     *	Construct the dialog, with a textpanel and the 2 buttons.
     */

    public ConfirmDialog() {

	super(new Frame(), "GZigZag Confirm Dialog", true);
	textPanel.setLayout(new FlowLayout());
	this.add("Center", textPanel);
	Panel p = new Panel();
	p.setLayout(new FlowLayout());
	yes.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		result = true;
		dlg.hide();
	    }
	});
	p.add(yes);
	no.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		result = false;
		dlg.hide();
	    }
	});
	p.add(no);
	this.add("South", p);
	this.setSize(400, 200);
    }

    /**
     *	Show the question and the dialog window, and return true if the
     *	user replied "Yes", and false if it was "No".
     *
     *	@param question is what you want to confirm with the user.
     */

    public boolean confirm(String question) {
	result = false;
	textPanel.removeAll();

	Vector strings = new Vector();
	int i1 = 0;
	int i2 = question.indexOf("\n");
	while (i2 > 0) {
	    if (i1 == i2) strings.add(""); else strings.add(question.substring(i1, i2));
	    i1 = i2 + 1;
	    i2 = question.indexOf("\n", i1);
	}
	strings.add(question.substring(i1));

	for (Enumeration e = strings.elements(); e.hasMoreElements();) {
	    textPanel.add("North", new Label((String)e.nextElement()));
	}
	this.show();
	return result;
    }
}
