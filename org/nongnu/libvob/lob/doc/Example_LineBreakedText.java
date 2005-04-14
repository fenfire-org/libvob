// (c): Matti J. Katila

package org.nongnu.libvob.lob.doc;
import org.nongnu.libvob.lob.*;
import java.awt.Color;
import java.util.*;

// class is run from printter util to 
// generate visual screenshot.
public class Example_LineBreakedText {

    public Lob getLob() {
	String [] texts = {
	    "Abc", "cat", "walks", "to", "some", "tree", "which",
	    "has", "ugly", "green", "tea", "next", "to", "see.", 
	};


	Random r = new Random(0);
	LobFont[] fonts = new LobFont[texts.length];
	for (int i=0; i<texts.length; i++)
	    fonts[i] = Components.font(new Color(r.nextInt(255),
						 r.nextInt(255),
						 r.nextInt(255)));

	List textList = Lists.list();
	for (int i=0; i<texts.length; i++) {
	    textList = Lists.concat(textList, 
				    fonts[i].text(texts[i]+" "));
	}
	Lob l = Lobs.linebreaker(textList);
	l = Lobs.margin(l, 10);
	return l;
    }
}
