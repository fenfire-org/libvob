package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import java.awt.*;

public class TextTestVob extends AbstractVob {

    int x, y;
    String t;
    public TextTestVob(int x, int y, String t) {
	this.x = x;
	this.y = y;
	this.t = t;
    }

    public void render(Graphics g,
		       boolean fast,
		       Vob.RenderInfo info1,
		       Vob.RenderInfo info2) {
	g.drawString(t, x,y);
    }
}
