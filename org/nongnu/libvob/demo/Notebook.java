/*   
Notebook.java
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
package org.nongnu.libvob.demo;
import org.nongnu.libvob.*;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.layout.Observable;
import org.nongnu.libvob.layout.component.*;
import org.nongnu.libvob.impl.LobMain;
import org.nongnu.navidoc.util.Obs;
import java.awt.Color;
import java.util.*;

public class Notebook extends LobLob {

    protected class Note extends ObsSet.AbstractObservable {
	protected String title = "New note "+new Date();
	protected String body = "";

	public String getTitle() { return title; }
	public String getBody() { return body; }
	public void setTitle(String title) { 
	    this.title = title; obses.trigger(); 
	}
	public void setBody(String body) { 
	    this.body = body; obses.trigger(); 
	}
    }

    protected ListModel notes = new ListModel.Simple();
    protected Model noteModel = new ObjectModel();

    public Notebook() {
	noteModel.set(new Note());
	notes.add(new Note());
	notes.add(noteModel.get());
	notes.add(new Note());

	ComponentFactory comps = new ComponentFactory(Theme.getTextFont());

	Action quit = new AbstractAction() { public void run() {
	    System.exit(0);
	}};
	Action newNote = new AbstractAction() { public void run() {
	    Note n = new Note();
	    notes.add(n);
	    noteModel.set(n);
	}};
	Action deleteNote = new AbstractAction() { public void run() {
	    notes.startUpdate();
	    notes.remove(noteModel.get());
	    if(notes.isEmpty())
		notes.add(new Note());
	    noteModel.set(notes.get(0));
	    notes.endUpdate();
	}};


	Box outerVBox = new Box(Y);

	Menu menubar = new Menu(X);
	outerVBox.add(new KeyLob(menubar, "MENU"));

	Menu filemenu = menubar.addMenu("File");
	filemenu.add("Quit", quit);

	Menu notemenu = menubar.addMenu("Note");
	notemenu.add("New note", newNote);
	notemenu.add("Delete note", deleteNote);

	outerVBox.glue(5, 5, 5);

	Box hbox = new Box(X);
	outerVBox.add(hbox);

	hbox.glue(5, 5, 5);

	ListBox noteList = comps.listBox(notes, Note.class, "title",
					 "NOTES");
	
	noteList.setSelection(noteModel);

	hbox.addRequest(noteList, 100, 250, 250);
	hbox.glue(5, 5, 5);

	Box vbox = new Box(Y);
	hbox.add(vbox);

	TextField titleField = comps.textField(noteModel, Note.class, "title",
					       "TITLE");
	vbox.add(titleField);

	vbox.glue(5, 5, 5);

	TextArea bodyArea = comps.textArea(noteModel, Note.class, "body",
					   "BODY");
	// XXX -- should just be vbox.add()...
	vbox.addRequest(bodyArea, 
			30, 100, Float.POSITIVE_INFINITY);

	hbox.glue(5, 5, 5);

	outerVBox.glue(5, 5, 5);

	KeyController k = new KeyController(new FocusLob(outerVBox));
	k.add("Ctrl-N", newNote);

	setDelegate(k);
    }

    public static void main(String[] argv) {
	LobMain m = new LobMain(new Color(1, 1, .8f)) {
		protected Lob createLob() { return new Notebook(); }
	    };
	m.start();
    }
}
