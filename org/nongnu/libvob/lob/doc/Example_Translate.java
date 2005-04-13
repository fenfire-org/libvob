// (c): Matti J. Katila

package org.nongnu.libvob.lob.doc;
import org.nongnu.libvob.lob.*;
import java.util.*;

// class is run from printter util to 
// generate visual screenshot.
public class Example_Translate {

    public Lob getLob() {
	
	Lob l = Lobs.filledRect(java.awt.Color.red);
	float 
	    minWidth = 20,
	    naturalWidth = 20,
	    maxWidth = 20;
	float 
	    minHeight = 50,
	    naturalHeight = 50,
	    maxHeight = 50;

	l = Lobs.request(l,
			 minWidth, naturalWidth, maxWidth,
			 minHeight, naturalHeight, maxHeight);

	l = Lobs.translate(l, 34, 56);

	return l;
    }
}
