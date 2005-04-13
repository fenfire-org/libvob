// (c): Matti J. Katila

package org.nongnu.libvob.lob.doc;
import org.nongnu.libvob.lob.*;

// class is run from printter util to 
// generate visual printout.
public class Example_TextWithGlue {

    public Lob getLob() {
	return Lobs.hbox(Components.font().textLn("Hello world!"));
    }

}
