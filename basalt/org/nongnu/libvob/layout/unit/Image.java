// (c): Matti J. Katila

package org.nongnu.libvob.layout.unit;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.*;

import java.awt.*;
import java.io.*;

public class Image extends VobLob {

    protected float width, height;
    protected java.awt.Image img;

    public float getNatSize(Axis ax) {
	if (ax == X) return width;
	return height;
    }
    public float getMinSize(Axis ax) { return 0; }
    public float getMaxSize(Axis ax) { return Float.POSITIVE_INFINITY; }


    static protected int ID = 0;
    static protected MediaTracker tracker;
    static public void setComponent(Component c) {
	tracker = new MediaTracker(c);
    }

    /** NOTE: this implementation do not work 
     *  with OpenGL API!!!
     */
    public Image(File f) throws IOException {
	this((InputStream)new FileInputStream(f));
    }
    public Image(InputStream in) throws IOException {
	super(null, new Object());

	com.sixlegs.image.png.PngImage png = 
		 new com.sixlegs.image.png.PngImage(in, true);
	width = (float) png.getWidth();
	height = (float) png.getHeight();

	//System.out.println("Image: "+png+", w: "+width+", h: "+height);
	img = Toolkit.getDefaultToolkit().createImage(png);
	tracker.addImage(img, ID++);

	vob = new AbstractVob() {
		public void render(Graphics g, boolean fast, 
				   RenderInfo info1, RenderInfo info2) {
		    if (width < 0 || height < 0) return;
		    int x = (int)info1.x, 
			y = (int)info1.y;
		    int w = (int)(info1.width), 
			h = (int)(info1.height);
		    
		    g.drawImage(img, x,y,w,h, null); 
		    /*
		      new java.awt.image.ImageObserver() {
		      public boolean imageUpdate(java.awt.Image img, 
		                                 int infoflags, 
		      int x, int y, int width, int height) {
		      return false;
		      )
		      );
		    */
		}
	    };
	png = null;
    }

}
