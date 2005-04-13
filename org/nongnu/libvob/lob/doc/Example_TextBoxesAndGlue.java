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

	List l1 = font.text("Hello");
	List l2 = Lists.list(Lobs.glue(Axis.X, 10,10,10));
	List l3 = font.text("world!");
	List l4 = Lists.list(Lobs.glue(Axis.X, Lob.INF, Lob.INF ,Lob.INF));
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3,l4)));
	
	l1 = Lists.list(Lobs.glue(Axis.X, Lob.INF, Lob.INF, Lob.INF));
	l2 = font.text("Hello");
	l3 = font.text("world!");
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3)));
		 
	l1 = Lists.list(Lobs.glue(Axis.X, Lob.INF, Lob.INF, Lob.INF));
	l2 = font.text("Hello");
	l3 = font.text(" ");
	l4 = font.text("world!");
	vbox.add(Lobs.hbox(Lists.concat(l1,l2,l3,l4)));

	return vbox;
    }
}
