/*
Theme.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein and Matti J. Katila
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
 * Written by Benja Fallenstein and Matti J. Katila
 */
package org.nongnu.libvob.layout.component;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.vobs.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;

public class Theme {

    protected Map templates;

    public Theme(Map templates) {
	this.templates = templates;
    }
    
    public Lob getLob(String uri, Map parameters) {
	Lob template = (Lob)templates.get(uri);
	
	return (Lob)template.instantiateTemplate(parameters);
    }


    private static Theme defaultTheme;
    private static Model font, whiteFont, textFont;

    public static final Model 
	lightColor = new ObjectModel(new java.awt.Color(1, 1, .9f)),
	darkColor = new ObjectModel(new java.awt.Color(.7f, .5f, .5f));

    public static Model getFont() {
	if(font == null) 
	    font = new ObjectModel(new LobFont("SansSerif", 0, 17, 
					       java.awt.Color.black));
	return font;
    }

    public static Model getWhiteFont() {
	if(whiteFont == null) 
	    whiteFont = new ObjectModel(new LobFont("SansSerif", 0, 17, 
						    java.awt.Color.white));
	return whiteFont;
    }

    public static Model getTextFont() {
	if(textFont == null) 
	    textFont = new ObjectModel(new LobFont("Serif", 0, 20, 
						   java.awt.Color.black));
	return textFont;
    }

    public static Theme getDefaultTheme() {
	if(defaultTheme != null) return defaultTheme;

	Map map = new HashMap();

	float nan = Float.NaN;
	float inf = Float.POSITIVE_INFINITY;

	getFont(); getWhiteFont(); // ensure they're initialized

	Model key = Parameter.model(Component.KEY);


	// ThemeFrame template

	Lob l = Parameter.lob(LobMonoLob.CONTENT);

	l = new Frame(l, lightColor, darkColor, 1, 3, false, false, true);

	map.put(ThemeFrame.URI, new KeyLob(l, key));


	// Window template

	Model title = Parameter.model(Window.TITLE, new ObjectModel(""));

	Lob rect = new Frame(null, darkColor, 1, 0, true, false, false);
	rect = new RequestChangeLob(rect, 3, 3, 3, 3, 3, 3);
	Lob stipple = new Margin(new RepeatLob(new Margin(rect, 1)), 0);

	Box titleBox = new Box(Lob.X);
	titleBox.addRequest(stipple, 20, 20, 20);
	titleBox.glue(5, 5, 5);
	titleBox.add(new NoGrowLob(new Label(title, whiteFont)));
	titleBox.glue(5, 5, 5);
	titleBox.addRequest(stipple, 0, 0, inf);

	MonoLob titleLob = new Frame(darkColor, null, 0, 1, 
				     false, false, true);
	titleLob.setContent(titleBox);

	l = Parameter.lob(LobMonoLob.CONTENT);
	
	l = new Margin(l, 5);

	Box vbox = new Box(Lob.Y);
	vbox.add(titleLob);
	vbox.add(l);

	l = new Frame(vbox, lightColor, darkColor, 1, 0, true, true, false);
	l = new Frame(l, null, darkColor, 1, 0, false, false, false);

	map.put(Window.URI, new KeyLob(l, key));


	// Button template

	Action action = Parameter.action(Button.ACTION, null);
	l = Parameter.lob(LobMonoLob.CONTENT);

	l = new AlignLob(l, .5f, .5f, .5f, .5f);
	l = new RequestChangeLob(l, 50f, nan, inf, 15f, nan, inf);
	l = new Frame(l, Theme.lightColor, Theme.darkColor, 1, 3, 
		      true, true, false);
	
	l = new ClickController(l, 1, action);

	map.put(Button.URI, new KeyLob(l, key));


	// CheckBox template

	Model checked = Parameter.model(CheckBox.CHECKED, new BoolModel());

	Box hbox = new Box(Lob.X);
	hbox.add(new SingleCheckBox(checked), "SingleCheckBox");
	hbox.glue(1, 10, 10);
	hbox.add(new AlignLob(Parameter.lob(LobMonoLob.CONTENT), 
			      0, .5f, 0, .5f),
		 LobMonoLob.CONTENT);
	
	map.put(CheckBox.URI, new KeyLob(hbox, key));


	// ListBox template

	ListModel elements = Parameter.listModel(ListBox.ELEMENTS);

	Box list = new Box(Lob.Y);

	Model selected = 
	    Parameter.model(ListBox.SELECTED);
	Lob template =
	    Parameter.lob(ListBox.TEMPLATE);


	Model elem = Parameter.model(ListModel.PARAM);
	Lob lob = template;
	
	Action select = new Model.Change(selected, elem);
	
	lob = new Margin(lob, 1);
	lob = new ClickController(lob, 1, VobMouseEvent.MOUSE_PRESSED, select);
	lob = new KeyLob(lob, elem);
	

	ListModel listModel = new ListModel.Transform(elements, lob);

	ListModel end =
	    new SequenceModel.Singleton(new Glue(Lob.Y, 0, 0, inf));

	listModel = new ListModel.Concat(listModel, end);

	list.setModel(new SequenceModel.ListSequenceModel(listModel));

	Lob selector = new VobLob(new FilledRectVob(Theme.darkColor),
				  "SELECTOR");

	Sequence seq = new SelectionLob(list, selector, selected,
					new IntModel(-1));

	Model lengthModel = new CollectionModel.SizeModel(elements);
	Model index = new ListModel.IndexModel(elements, selected);

	l = new ViewportLob(Lob.Y, seq, seq.positionModel(Lob.Y, index));
	l = new ClipLob(l, "VIEWPORT-CLIP");
	l = new RequestChangeLob(Lob.Y, l, nan, nan, inf);
	l = new Margin(l, 2);


	Box box = new Box(Lob.X);
	box.add(new RequestChangeLob(Lob.X, l, 0, 20, inf));
	box.add(new KeyLob(new Scrollbar(Lob.Y, index, lengthModel.minus(1)),
			   "SCROLLBAR"));

	l = new Frame(box, Theme.lightColor, Theme.darkColor,
		      1, 0, false, false, true);

	map.put(ListBox.URI, new KeyLob(l, key));


	// TreeBox template

	Lob treeNodeTemplate = Parameter.lob(TreeBox.TEMPLATE,
					     new Label("XXX no instance"));
	Model node = Parameter.model(TreeBox.ROOT);
	ListModel children = Parameter.listModel(TreeBox.CHILDREN);

	selected = Parameter.model(TreeBox.SELECTED);

	Box tree = new Box(Lob.Y);

	select = new Model.Change(selected, node);

	MapModel params = new MapModel.Simple();
	params.put(TreeBox.ROOT, node);
	l = Instance.lob(treeNodeTemplate, params);
	l = new Margin(l, 2);
	l = new KeyLob(l, node);
	hbox = new Box(Lob.X);
	hbox.add(new NoGrowLob(Lob.X, l));
	hbox.glue(0, 0, inf);
	l = new ClickController(hbox, 1, VobMouseEvent.MOUSE_PRESSED, select);
	tree.add(l);

	params = new MapModel.Simple();
	params.put(TreeBox.ROOT, node);
	ListModel myChildren = Instance.listModel(children, params);

	params = new MapModel.Simple();
	params.put(TreeBox.TEMPLATE, treeNodeTemplate);
	params.put(TreeBox.ROOT, new ObjectModel.StaticModel(Parameter.mapModel(ListModel.PARAM, new MapModel.Simple(Collections.singletonMap("argh", "ARGH")))));
	params.put(TreeBox.CHILDREN, children);
	params.put(TreeBox.SELECTED, selected);
	Lob subTree = 
	    new ModelLob(new Instance.RecursionInstance(tree, params));

	Box childrenBox = new Box(Lob.Y);
	childrenBox.setModel(new SequenceModel.ListSequenceModel(new ListModel.Transform(myChildren, subTree)));

	Box indented = new Box(Lob.X);
	indented.glue(20, 20, 20);
	indented.add(childrenBox);

	tree.add(indented);

	l = new SelectionLob(tree, selector, selected, new IntModel(-1));

	l = new Frame(l, lightColor, darkColor, 1, 0, true, true, false);
	l = new Frame(l, null, darkColor, 1, 0, false, false, false);

	map.put("XXX/TreeBox", new KeyLob(l, key));


	// XXX add other lob templates here


	return (defaultTheme = new Theme(map));
    }

    private static class DbgLob extends AbstractMonoLob {
	private DbgLob(Lob l) { super(l); }
	protected Replaceable[] getParams() {
	    System.out.println("dbglob getparms");
	    return super.getParams();
	}
	protected Object clone(Object[] params) {
	    System.out.println("Cloning DbgLob");
	    return new DbgLob((Lob)params[0]);
	}
    }


    public static void main(String[] argv) {
	System.out.println(getDefaultTheme().templates);
    }
}
