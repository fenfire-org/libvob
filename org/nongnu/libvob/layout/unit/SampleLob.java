// (c): Matti J. Katila

package org.nongnu.libvob.layout.unit;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.layout.component.*;
import org.nongnu.libvob.impl.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.mouse.*;
import org.nongnu.navidoc.util.Obs;
import java.util.*;
public class SampleLob extends AbstractDelegateLob {
    private void p(String s) { System.out.println("Sample:: "+s); }

    protected Replaceable[] getParams() { return NO_PARAMS; }
    protected Object clone(Object[] params) { return new SampleLob(null); }
    
    Lob[] lobs; 
    Box y;
    public SampleLob(final WindowAnimation anim) {
	try {

	    final Tray tray = new Tray(false);
	    for (int i=1; i<7; i++) {
		final int ii = i;
		final Model x = new FloatModel(ii*30);
		final Model y = new FloatModel(ii*30);

		Lob l = new Label("Test translation - try dragging");
		tray.add(new TranslationLob(
			     new RelativeDragController(l , 1, 
						new RelativeDragListener(){
						    public void change(float dx, float dy) {
							x.setFloat(x.getFloat() + dx);
							y.setFloat(y.getFloat() + dy);
							anim.rerender();
						    }
						}
				 ), x,y)
		    );
	    }

	    final Model zoom = new FloatModel(1),
		panX = new FloatModel(0),
		panY = new FloatModel(0);
	    
	    Lob content = tray;

	    final OverlappingWindowManager mgr = 
		new OverlappingWindowManager(anim);
	    mgr.add(new Button("Stupid button", new org.nongnu.libvob.layout.AbstractAction() {
		    public void run() { new MsgBox("Clicked", "Clicked", mgr); }
		}), "stupid title");
	    
	    lobs = new Lob[]{
		/*
		new Button("This is a button Lob that does nothing", 
			   new AbstractAction() { public void run() {}}),
		new CheckBox("This is a check box."),

		new Between(new Image(new java.io.File("testdata/libvob.png"))
			    , content, new NullLob()),
		*/

		new Between(new Label("Try zooming with button 3 and panning with button 1."),
			    new DragController(
				new DragController(new PanZoomLob(new Image(new java.io.File("testdata/libvob.png")),
								  panX, panY, zoom),
						   3, new RelativeAdapter() {
							   public void changedRelative(float dx, float dy) {
							       zoom.setFloat(zoom.getFloat() + dy/100);
							       //p("panx: "+panX.getFloat()+", z: "+zoom.getFloat());
							       anim.rerender();
							   }
						       }),
				1, new RelativeAdapter() {
					public void changedRelative(float dx, float dy) {
					    panX.setFloat(panX.getFloat() - dx/zoom.getFloat());
					    panY.setFloat(panY.getFloat() - dy/zoom.getFloat());
					    anim.rerender();
					}
				    }), new NullLob())
		,
		//mgr.getLob()
	    };
	} catch (Exception e) { e.printStackTrace(); }

	int s = (int) Math.sqrt(lobs.length);
	if (s < Math.sqrt(lobs.length))
	    s += 1;
	
	y = new Box(Y);
	for (int i=0; i<s; i++) {
	    Box x = new Box(X);
	    y.add(x);
	    for(int j=0; j<s; j++) {
		try {
		    Lob l = lobs[i*s + j]; 
		    x.add(l);
		} catch (ArrayIndexOutOfBoundsException e) {}
	    }
	}
    }

    protected Lob getDelegate() { return y; }

    public static void main(String[] argv) {
	Main m = new LobMain(new java.awt.Color(1, 1, .8f)) {
		protected Lob createLob() {
		    return new SampleLob(windowAnim);
		}
	    };
	m.start();
    }

}
