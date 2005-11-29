/*
Screen.java
 *    
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
*/
/*
 * Written by Rauli Ruohonen, Antti-Juhani Kaijanaho and Tuomas Lukka
 */
package org.nongnu.libvob.impl.awt;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.impl.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.PixelGrabber;

/** A single output window.
 */
public abstract class AWTScreen
	extends GraphicsAPI.AbstractWindow
	implements MouseListener, MouseMotionListener, JUpdateManager.EventProcessor {

    public static boolean dbg = false;
    private static void p(String s) { System.out.println(s); }

    public Color fg = Color.black;

    Component canvas = (Component) new ScreenCanvas() ;

    Binder binder;



    /** Applet needs access to screen cache and for key 
     *  events it needs this reference.
     */
    public interface Applet { 
	void setAWTScreen(AWTScreen a); 
    }
    public Image getCached() { return cache; }
    

    // XXX Should use VolatileImage in new JDKs?
    Image cache;
    Graphics cacheGraphics;

    /** Set the mouse cursor for the window.
     */	
    public void setCursor(String name) {
	try {
	    Cursor cursor = new Cursor(Cursor.class.getField(name.toUpperCase()+"_CURSOR").getInt(null));
	    canvas.setCursor(cursor);
	} catch (Exception e) {
	    throw new IllegalArgumentException("Unknown cursor: "+name);
	}
    }
    public void setCursor(Cursor cursor) {
        canvas.setCursor(cursor);
    }

    public VobScene createVobScene(Dimension size) {
	return new VobScene(
		new DefaultVobMap(),
		new AWTVobCoorder_Gen(size.width, size.height),
		new DefaultVobMatcher(),
		this.getGraphicsAPI(),
		this,
		size
		);
    }
    public ChildVobScene createChildVobScene(Dimension size, 
					     int numberOfParameterCS) {

	AWTVobCoorder_Gen coorder = new AWTVobCoorder_Gen(size.width, size.height);
	coorder.setNumberOfParameterCS(numberOfParameterCS);
	return new ChildVobScene(
	    new DefaultVobMap(),
	    coorder,
	    new DefaultVobMatcher(),
	    this.getGraphicsAPI(),
	    this,
	    size
	    );
    }

    public void registerBinder(Binder s) {
	binder = s;
    }


    /** Sets window size and location.
     *  @param x x-coordinate for window upper left-hand corner
     *  @param y y-coordinate for window upper left-hand corner
     *  @param w width of window in pixels
     *  @param h height of window in pixels
     */
    public void setLocation(int x, int y, int w, int h) {
	canvas.setSize(w, h);
    }

    public Dimension getSize() { return canvas.getSize(); }


    public AWTScreen(GraphicsAPI api) {
	super(api);
	setCanvas(canvas);
    }
    public AWTScreen(GraphicsAPI api, Component canvas) {
	super(api);
	setCanvas(canvas);
    }
    private void setCanvas(Component canvas) {
	if (canvas instanceof Applet)
	    ((Applet)canvas).setAWTScreen(this);
	this.canvas = canvas;
	canvas.addMouseListener(this);
	canvas.addMouseMotionListener(this);
	canvas.setVisible(true);
	JavaIncompat.callSetFocusTraversalKeysEnabled(canvas, false);
    }

    public void renderStill(VobScene prev, float lod) {
	renderAnim(prev, null, 0, lod, true);
    }

    /** Renders and display a frame of the view animation on screen
     */
    public void renderAnim(VobScene prev, VobScene next, float fract, float lod, boolean showFinal) {
	renderAnimImage(prev, next, fract, lod);
	if(dbg) p("renderan: "+prev+" "+next+" "+cache);
	Graphics gr = canvas.getGraphics();
	if (gr == null) return;
	canvas.paint(gr);
	gr.dispose();
    }


    /** Whether to use the global cache image or a separate cache for each
     * screen.
     * Currently default is false because it seems to be more efficient that
     * way.
     */
    static boolean useGlobalCache = false;
    static Image globalCache;
    VobScene listprev, listnext;
    int[] interplist;

    public int[] readPixels(int x, int y, int w, int h) {
	int[] pix = new int[w*h];
	if(dbg) p("Readpixels: "+x+" "+y+" "+w+" "+h+" "+cache);
	PixelGrabber pg = new PixelGrabber(cache.getSource(),
			x, y, w, h, pix, 0, w);
	try {
	    pg.grabPixels();
	} catch(InterruptedException e) {
	    throw new Error("Interrupted readpixels");
	}
	return pix;
    }

    void createInterpList(VobScene sc, VobScene osc, boolean towardsOther) {
	if(osc != null && (sc != listprev || osc != listnext)) {
	    listprev = sc;
	    listnext = osc;
	    interplist = sc.matcher.interpList(osc.matcher,
					       towardsOther);
	}
    }

    public boolean needInterp(VobScene prev, VobScene next) {
	createInterpList(prev, next, true);
	if(interplist == null) return false;
	return prev.coords.needInterp(next.coords, interplist);
    }

    private void renderMessage(Graphics gr, Dimension d, String message, 
			       int height) {
	ScalableFont f = new ScalableFont("SansSerif", Font.PLAIN, 36);
	FontMetrics fm = f.getFontMetrics(1);
	int sw = fm.stringWidth(message);
		
	gr.setFont(f.getFont(1));
	gr.drawString(message,
		      (d.width-sw-10)/2,
		      (d.height-fm.getHeight())/2);
    }

    /** Renders a frame of the view animation onto Graphics buffer
     */
    void renderAnimImage(VobScene prev, VobScene next, float fract, float lod) {
	Dimension d = canvas.getSize();
	if(useGlobalCache) {
	    if(cache != globalCache) cache = globalCache;
	    if (cache == null || cache.getWidth(null) < d.width ||
		cache.getHeight(null) < d.height) {
		int width = (cache == null ? 0 : cache.getWidth(null));
		if(d.width > width) width = d.width;
		int height = (cache == null ? 0 : cache.getHeight(null));
		if(d.height > height) height = d.height;
		globalCache = canvas.createImage(d.width, d.height);
		cache = globalCache;
	    }
	} else {
	    if (cache == null || cache.getWidth(null) != d.width ||
		cache.getHeight(null) != d.height) {
		cache = canvas.createImage(d.width, d.height);
		if(cache != null)
		    cacheGraphics = cache.getGraphics();
	    }
	}
	if(cache == null) return;

	Graphics gr = cacheGraphics; //cache.getGraphics();
	Shape oldClip = gr.getClip();

	try {
	    /*
	    if(gr instanceof Graphics2D) {
		Graphics2D gr2d = (Graphics2D)gr;
		gr2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
				      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    }
	    */
	    
	    gr.setColor(Color.white); // XXX
	    gr.fillRect(0, 0, d.width, d.height);
	    gr.setColor(Color.black);
	
	    if (next == null && fract != 0) {
		// No VobScene to draw
		gr.setColor(Color.white);
		gr.fillRect(0, 0, d.width, d.height);
		gr.setColor(Color.red);
		renderMessage(gr, d, "No vobscene to interpolate to", 18);
	    } else {
		VobScene sc = prev;
		VobScene osc = next;
		boolean towardsOther = true;
		if (fract > AbstractUpdateManager.jumpFract) {
		    sc = next;
		    osc = prev;
		    fract = 1-fract;
		    towardsOther = false;
		}
		if(dbg) p("Going to render: "+sc+" "+osc+" "+fract);
		if(dbg) sc.dump();
		if(dbg && osc!=null) osc.dump();
		
		createInterpList(sc, osc, towardsOther);
		AWTRenderer.render(sc, osc, towardsOther,
				   fract, gr, fg);
	    }
	} finally {
	    gr.setClip(oldClip);
	}
	//gr.dispose();
    }
    // ALL THESE MUST DO synchronized(UpdateManager.getSynchronizer())
    // OR PROBLEMS WILL RESULT!

    int dragButton = 0;
    public void zzProcessEvent(AWTEvent e) {
	if(dbg) p("ZZProcessEvent "+e);
	if(e instanceof KeyEvent) {
	    KeyEvent ke = (KeyEvent)e;
	    String name = InputEventUtil.getKeyEventName(ke);
	    if(dbg) p("ZZProcessEvent keyname: "+name);
	    if(name != null && !name.equals("")) {
		binder.keystroke(name);
	    }
	} else if(e instanceof MouseEvent) {
	    MouseEvent me = (MouseEvent) e;
	    if(dbg) p("MouseEvent: "+me);

	    int vet = 0;
	    int veb = 0;
	    // XXX MOUSE WHEEL NOT HANDLED
	    switch(me.getID()) {
 	        case MouseEvent.MOUSE_PRESSED: {
		    if (dragButton == 0) dragButton = me.getButton();
		    vet = VobMouseEvent.MOUSE_PRESSED; break;
		}
	        case MouseEvent.MOUSE_RELEASED: {
		    if (dragButton != 0) {
			veb = dragButton;
			dragButton = 0;
		    }
		    vet = VobMouseEvent.MOUSE_RELEASED; break;
		}
		case MouseEvent.MOUSE_CLICKED: 
		    vet = VobMouseEvent.MOUSE_CLICKED; break;
		case MouseEvent.MOUSE_DRAGGED: 
		    veb = dragButton;
		    vet = VobMouseEvent.MOUSE_DRAGGED; break;
	        case MouseEvent.MOUSE_MOVED:
		    vet = VobMouseEvent.MOUSE_MOVED; break;
	    }
	    switch(me.getButton()) {
	        case 0:
		    // argh -- that's a bug in awt -- this's a work around
		    veb = 1; break;
		case MouseEvent.BUTTON1: veb = 1; break;
		case MouseEvent.BUTTON2: veb = 2; break;
		case MouseEvent.BUTTON3: veb = 3; break;
	    }
	    /* mudyc: This makes no sense at all!
	    if(vet == 0 || veb == 0)
		return; // ignore
		//throw new Error("Event Type!");
	    */

	    int modifiers = 0;
	    modifiers += (me.isAltDown()? VobMouseEvent.ALT_MASK:0);
	    modifiers += (me.isControlDown()? VobMouseEvent.CONTROL_MASK:0);
	    modifiers += (me.isShiftDown()? VobMouseEvent.SHIFT_MASK:0);

	    if (dbg) p("modifiers: "+modifiers+", type: "+vet+", button: "+veb);
	    binder.mouse(
		    new VobMouseEvent(
			vet, me.getX(), me.getY(), 0, modifiers, veb
		    ));

	}
    }

    // EventQueue systemEventQueue = getToolkit().getSystemEventQueue();

    public void mouseEntered(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
	JUpdateManager.addEvent(this, e);
    }
    public void mouseClicked(MouseEvent e)  {
	JUpdateManager.addEvent(this, e);
    }
    public void mouseMoved(MouseEvent e)  {
	JUpdateManager.addEvent(this, e);
    }
    public void mouseDragged(MouseEvent e)  {
	JUpdateManager.addEvent(this, e);
    }
    public void mouseReleased(MouseEvent e) {
	JUpdateManager.addEvent(this, e);
    }
    public void mouseExited(MouseEvent e) {
    }

    class ScreenCanvas extends Canvas {
	ScreenCanvas() {
	    super();
	    enableEvents(AWTEvent.KEY_EVENT_MASK);
	}
	private KeyEvent lastEvent;
	public void processKeyEvent(KeyEvent e) {
	    if(dbg) p("AWTScreen received: " + e);
	    if(e == lastEvent) throw new Error("Re-used event object: "+e);
	    lastEvent = e;
	    JUpdateManager.addEvent(AWTScreen.this, e);
	}
	public boolean isFocusTraversable() { return true; }

        /** Draws cached image of the view onto screen */
        public void paint(Graphics gr) {
            Dimension d = this.getSize();
            if (cache == null) {
                gr.setColor(Color.white);
                gr.fillRect(0, 0, d.width, d.height);

		/* "a splash screen" for the impatient user */
		gr.setColor(Color.black);
		renderMessage(gr, d, "Loading...", 36);

                return;
            }
            if(useGlobalCache)
                gr.drawImage(cache, 0, 0, d.width-1, d.height-1,
                                    0, 0, d.width-1, d.height-1, null);
	    else {
		try {
		    gr.drawImage(cache, 0, 0, null);
		} catch(NullPointerException e) {
		    System.out.println("exc "+gr+" "+cache+" "+cache.getGraphics());
		    throw e;
		}
	    }
        }
        public void update(Graphics gr) {
            // Default behaviour overridden because we clear the canvas ourselves
            paint(gr);
        }

    }


    public double timeRender(VobScene vs, boolean swapbuf, int iters) {
	long t0 = System.currentTimeMillis();
	for(int i=0; i<iters; i++) {
	    renderAnim(vs, null, 0, 0, true);
	}
	long t1 = System.currentTimeMillis();
	return (t1-t0)/1e6;
    }

}


