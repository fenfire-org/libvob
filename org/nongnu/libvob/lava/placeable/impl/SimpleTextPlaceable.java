// (c): Matti J. Katila


package org.nongnu.libvob.lava.placeable.impl;
import org.nongnu.libvob.vobs.*;
import org.nongnu.libvob.*;

/** Very simple text placeable
 */
public class SimpleTextPlaceable 
    implements org.nongnu.libvob.lava.placeable.Placeable {

    private final TextVob vob; 

    public SimpleTextPlaceable(String text, TextStyle style) {
	this(text, style, java.awt.Color.black);
    }
    public SimpleTextPlaceable(String text, TextStyle style, java.awt.Color color) {
	vob = new TextVob(style, text, false, color);
    }

    public void place(VobScene vs, int cs) {
	float [] size = new float[3];
	vs.coords.getSqSize(cs, size);
	int textCS = vs.scaleCS(cs, "CS", getHeight()+vob.getDepth(1), getHeight()+vob.getDepth(1));
	vs.put(vob, textCS);
    }

    public float getHeight() { return vob.getHeight(1); }
    public float getWidth() { return vob.getWidth(1); }
    public String toString() { return vob.text; }
    
}
