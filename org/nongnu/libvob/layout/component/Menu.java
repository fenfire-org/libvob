/*
Menu.java
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
package org.nongnu.libvob.layout.component;
import org.nongnu.libvob.*;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.util.*;
import java.awt.Color;
import java.util.Map;

public class Menu extends LobLob {

    protected static final Object KEY = new Object();

    protected Axis axis;
    protected Sequence sequence;

    protected Lob glue;
    protected int n;
    protected Model selected = new IntModel(-1);
    protected Menu parentMenu = null;

    protected static LobFont font;

    public Menu(Axis axis) {
	this(axis, new Box(axis));
    }

    public Menu(Axis axis, Sequence sequence) {
	if(font == null) font = new LobFont("SansSerif", 0, 12, 
					    java.awt.Color.black);

	this.axis = axis;
	this.sequence = sequence;

	if(axis==X)
	    this.glue = new Glue(X, 8, 8, 8);
	else
	    this.glue = new Glue(Y, 3, 3, 3);

	Lob l = sequence;
	l = new AlignLob(l, 0, 0, 0, 0);
	l = new Frame(l, Theme.lightColor, Theme.darkColor, 1, 0,
		      true, true, false);
	l = new Frame(l, null, Theme.darkColor, 1, 0, false, false, false);

	setDelegate(l);
    }

    protected Lob clone(Lob delegate, Map map) {
	throw new UnsupportedOperationException("not implemented");
    }

    public void close() {
	sequence.add(new Glue(axis, 0, 0, Float.POSITIVE_INFINITY));
	sequence.setSize(sequence.getNatSize(X), sequence.getNatSize(Y));
    }

    public void closeMenu() {
	selected.setInt(-1);
	if(parentMenu != null)
	    parentMenu.closeMenu();
	AbstractUpdateManager.chg();
    }

    public void add(String s, Action action) {
	add(font.getLabel(s), action);
    }

    public void add(Lob lob, final Action action) {
	if(n > 0) sequence.add(glue);
	Action _action = new AbstractAction() { public void run() {
	    closeMenu();
	    action.run();
	}};
	sequence.add(new ClickController(new Margin(lob, 2), 1, _action), 
		     KEY, n);
	n++;
    }


    public Menu addMenu(String s) {
	return addMenu(Y, s);
    }

    public Menu addMenu(Axis axis, String s) {
	Menu menu = new Menu(axis);
	add(s, menu);
	return menu;
    }

    public void add(String s, Menu submenu) {
	add(font.getLabel(s), submenu);
    }

    public void add(Lob lob, Menu submenu) {
	Model visible = selected.equalsInt(n);

	Lob l;
	l = new DepthChangeLob(submenu, -1000);
	l = new VisibilityLob(l, visible);
	l = new NextToLob(axis.other(), new Margin(lob, 2), l);

	if(n > 0) sequence.add(glue);

	final int index = n;
	sequence.add(new ClickController(l, 1, VobMouseEvent.MOUSE_PRESSED,
					 new AbstractAction() {
		public void run() {
		    if(selected.getInt() != index) 
			selected.setInt(index);
		    else
			selected.setInt(-1);

		    AbstractUpdateManager.chg();
		}
	    }), KEY, n);

	n++;

	submenu.parentMenu = this;
    }
}
