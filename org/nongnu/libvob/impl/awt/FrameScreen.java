/*
FrameScreen.java
 *    
 *    This file is part of Libvob.
 *    
 *    Libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Libvob; if not, write to the Free
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
import java.awt.*;
import java.awt.event.*;

/** A single output window in a Java AWT Frame.
 */

public class FrameScreen extends AWTScreen {
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println(s); }

    protected Frame zzFrame;

    /** Get the AWT Frame this screen is rendered in.
     */
    public Frame getFrame() { return zzFrame; }

    public FrameScreen(GraphicsAPI api) {
	super(api);
	zzFrame = new Frame("Fenfire");
	zzFrame.addComponentListener(new ComponentAdapter() {
		public void componentMoved(ComponentEvent e) {
		}
		public void componentResized(ComponentEvent e) {
		    Dimension d = zzFrame.getSize();
		    Insets i = zzFrame.getInsets();
		    canvas.setSize(d.width - i.left - i.right,
				   d.height - i.top - i.bottom);
		    canvas.requestFocus();
		    AbstractUpdateManager.setNoAnimation();
		    AbstractUpdateManager.chg();
		}
	    });

	zzFrame.addWindowListener(new WindowListener() {
	    public void windowClosing(WindowEvent _) {
	        zzFrame.dispose();
	    }
	    public void windowClosed(WindowEvent _) {
	        binder.windowClosed();
	    }
	    public void windowOpened(WindowEvent _) {}
	    public void windowIconified(WindowEvent _) {}
	    public void windowDeiconified(WindowEvent _) {}
	    public void windowActivated(WindowEvent _) {}
	    public void windowDeactivated(WindowEvent _) {}
	});

	zzFrame.add(canvas);
    }

    public void die() {
	zzFrame.dispose();
    }
    public void chg() {
	// zzFrame.setTitle(screenCell.t()+" - GZigZag");
	//
	if(dbg) pa("FrameScreen chg");

	/*if (zzFrame.getComponentCount() != 1) {
	    p("FrameScreen chg: add stuff and show");
	    zzFrame.removeAll();
	    zzFrame.add(this.canvas);
	    zzFrame.pack();
	    zzFrame.show();
	    // super.chg(); // Try if doing it again would help...
	}*/
    }

    public void setLocation(int x, int y, int w, int h) {
	/* Probably not needed
	   Rectangle r = zzFrame.getBounds();
	   if (r.x == x && r.y == y && r.width == w && r.height == h)
	       return;
	   p("SetBounds: "+r+" to "+x+" "+y+" "+w+" "+h);
	*/

	if(dbg) pa("FrameScreen setloc "+x+" "+y+" "+w+" "+h);

	zzFrame.setSize(w, h);
	zzFrame.setLocation(x, y);
	zzFrame.show();
	canvas.requestFocus();
    }

    public void printScreen() {
	PrintJob pjob = canvas.getToolkit().getPrintJob(
					    zzFrame,
					    "Printing Test",
					    null);
	//	    pjob.printDialog();
	if (pjob != null) {
	    Graphics pg = pjob.getGraphics();

	    if (pg != null) {
		canvas.printAll(pg);
		pg.dispose(); // flush page
	    } else {
		pa("Failed to get abstract surface for printing!");
	    }
	    pjob.end();
	    pa("Printed. If you don't see any paper, look for a ps file in");
	    pa("your current directory or home directory.");
	} else {
	    pa("Failed to start printing!");
	}
    }
}
