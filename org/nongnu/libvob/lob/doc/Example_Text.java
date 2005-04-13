// (c): Matti J. Katila

package org.nongnu.libvob.lob.doc;
import org.nongnu.libvob.lob.*;

// class is run from printter util to 
// generate visual printout.
public class Example_Text {

    public Lob getLob() {
	return Lobs.hbox(Components.font().text("H e l l o !"));
    }

}
