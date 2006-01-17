// (c): Matti J. Katila, and others worked on AWTScreen


package org.nongnu.libvob.impl.terminal;
import org.nongnu.libvob.impl.awt.*;
import org.nongnu.libvob.impl.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.*;
import java.io.*;
import java.util.*;
import java.awt.*;


/** A class that encapsulates all GraphicsAPI.Window methods.
 */
public class TerminalWindow extends GraphicsAPI.AbstractWindow 
    implements ConsoleOperations {

    static public boolean dbg = false;
    static private void p(String s) { System.out.println("TermWindow:: "+s); }
    
    private TerminalGraphics g;
    private UnixTerminal terminal;
    private Dimension terminalSize = null;

    Binder binder;
    VobScene listprev, listnext;
    int[] interplist;

    TerminalWindow(GraphicsAPI api) {
	super(api);
	try {
	    terminal = new UnixTerminal();
	    terminal.initializeTerminal();
	    terminalSize = terminal.getTerminalSize();
	    g = new TerminalGraphics(terminalSize);
	    ((TerminalUpdateManager)AbstractUpdateManager.getInstance()).set(terminal);
	} catch (Exception e) { e.printStackTrace(); }
	instance = this;
    }

    private TerminalWindow instance = null;
    public TerminalWindow getInstance() { return instance; }

    public void renderStill(VobScene prev, float lod) {
	renderAnim(prev, null, 0, lod, true);
    }

    /** Renders and display a frame of the view animation on screen
     */
    public void renderAnim(VobScene prev, VobScene next, float fract, float lod, boolean showFinal) {
	renderAnimImage(prev, next, fract, lod);
	if(dbg) p("renderanim: "+prev+" "+next);
	if (g == null) return;
	//canvas.paint(g);
	// terminal .flush...
	g.convert().flush();
	g.dispose();
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
	Dimension d = g.getSize();
	Graphics gr = g;
	Shape oldClip = gr.getClip();

	try {
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
				   fract, gr, gr.getColor());
	    }
	} finally {
	    gr.setClip(oldClip);
	}
	//gr.dispose();
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

    public void setCursor(String name) { }
    //public void setCursor(Cursor cursor) { }

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
 

    public void setLocation(int x, int y, int w, int h) { }

    public Dimension getSize() { return g.getSize(); }

    public int[] readPixels(int x, int y, int w, int h) { return null; }

}
