// (c): Matti J. Katila

package org.nongnu.libvob.lob.doc;
import org.nongnu.libvob.lob.*;
import java.util.*;

// class is run from printter util to 
// generate visual screenshot.
public class Example_LineBreakedText {

    public Lob getLob() {
	LobFont[] fonts = new LobFont[]{
	    Components.font(java.awt.Color.black),
	    Components.font(java.awt.Color.gray),
	    Components.font(java.awt.Color.yellow),
	    Components.font(java.awt.Color.red),
	    Components.font(java.awt.Color.blue),
	    Components.font(java.awt.Color.green),
	    Components.font(java.awt.Color.cyan),
	    Components.font(java.awt.Color.white),
	    Components.font(java.awt.Color.orange)
	};

	String [] texts = {
	    "Abc", "cat", "walks", "to", "some", "tree", "which",
	    "has", "ugly", "green", "tea", "next", "to", "see.", 
	};

	List textList = Lists.list();
	for (int i=0; i<texts.length; i++) {
	    textList = Lists.concat(textList, 
				    fonts[i%fonts.length].text(texts[i]+" "));
	}
	return Lobs.linebreaker(textList);
    }
}
