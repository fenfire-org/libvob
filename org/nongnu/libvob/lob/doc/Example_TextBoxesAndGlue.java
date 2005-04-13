// (c): Matti J. Katila

package org.nongnu.libvob.lob.doc;
import org.nongnu.libvob.lob.*;
import java.util.*;

// class is run from printter util to 
// generate visual screenshot.
public class Example_TextBoxesAndGlue {

    public Lob getLob() {
	
	LobFont font = Components.font();
	
	// vertical box or list
	Lob vbox = Lobs.vbox();
	
	vbox.add(Lobs.hbox(font.textLn("Hello world!")));
	vbox.add(Lobs.hbox(font.text("Hello world!")));

	List l1 = font.text("Hello");
	List l2 = Lists.list(Lobs.glue(Axis.X, 1));
	List l3 = font.text("world!");
	List l4 = Lists.list(Lobs.hglue());
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3,l4)));
	
	l1 = Lists.list(Lobs.hglue());
	l2 = font.text("Hello");
	l3 = font.text("world!");
	l4 = Lists.list(Lobs.hglue());
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3,l4)));

	l1 = Lists.list(Lobs.hglue());
	l2 = font.text("Hello world!");
	l3 = Lists.list(Lobs.hglue());
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3)));
		 
	l1 = Lists.list(Lobs.hglue());
	l2 = font.text("Hello");
	l3 = font.text(" ");
	l4 = font.text("world!");
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3,l4)));

	return vbox;
    }
}
