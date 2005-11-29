/**
 *	jline - Java console input library
 *	Copyright (c) 2002, 2003, 2004, 2005, Marc Prud'hommeaux <mwp1@cornell.edu>
 *                    2005 Matti J. Katila
 *	All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or
 *	without modification, are permitted provided that the following
 *	conditions are met:
 *
 *	Redistributions of source code must retain the above copyright
 *	notice, this list of conditions and the following disclaimer.
 *
 *	Redistributions in binary form must reproduce the above copyright
 *	notice, this list of conditions and the following disclaimer
 *	in the documentation and/or other materials provided with
 *	the distribution.
 *
 *	Neither the name of JLine nor the names of its contributors
 *	may be used to endorse or promote products derived from this
 *	software without specific prior written permission.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *	"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 *	BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *	AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 *	EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 *	FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *	OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *	PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 *	AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *	LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 *	IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 *	OF THE POSSIBILITY OF SUCH DAMAGE.
 */



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

    public TerminalWindow(GraphicsAPI api) {
	super(api);
	try {
	    terminal = new UnixTerminal();
	    terminal.initializeTerminal();
	    terminalSize = terminal.getTerminalSize();
	    g = new TerminalGraphics(terminalSize);
	    TerminalUpdateManager.getInstance().set(terminal);
	} catch (Exception e) { e.printStackTrace(); }

    }

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
