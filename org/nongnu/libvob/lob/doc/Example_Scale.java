// (c): Matti J. Katila

package org.nongnu.libvob.lob.doc;
import org.nongnu.libvob.lob.*;
import java.util.*;

// class is run from printter util to 
// generate visual screenshot.
public class Example_Scale {

    public Lob getLob() {
	LobFont font = Components.font();
	Lob vbox = Lobs.vbox();
	
	int N = 5;
	float scale = 1;
	for (int i=0; i<N; i++) {
	    Lob l = Lobs.hbox(font.textLn("Hello!"));
	    l = Lobs.scale(l, scale, scale); // scaleX, scaleY
	    scale *= 1.2;
	    vbox.add(l);
	    if ((i+1) != N)
		vbox.add(Lobs.vglue());
	}
	return vbox;
    }
}
