// (c): Matti J. Katila

package org.nongnu.libvob.layout.unit;
import org.nongnu.libvob.*;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.layout.component.*;
import org.nongnu.libvob.mouse.*;

import java.util.*;

/** XXX.
 */
public class OverlappingWindowManager extends LobLob implements WindowManager {
    static public boolean dbg = true;
    private void p(String s) { System.out.println("WindowManager:: "+s); }
    

    class AppLob extends AbstractDelegateLob {

	protected MapModel app;
	protected Model desk;

	protected Lob delegate;

	public AppLob(MapModel app, Model desk) {
	    this.app = app;
	    this.desk = desk;
	    
	    app.addObs(this); desk.addObs(this);
	    chg();
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { app, desk };
	}
	protected Object clone(Object[] params) {
	    return new AppLob((MapModel)params[0], (Model)params[1]);
	}

	protected Lob getDelegate() {
	    return delegate;
	}

	public void chg() {
	    MapModel m = app;
	    if (m.get(VIRTUAL_DESK) != null) {
		Lob content = (Lob) m.get(CONTENT);
		Box b = new Box(Lob.Y);
		b.add(new Frame(new Label("Title: "+m.get(TITLE).toString()),
				Theme.darkColor, Theme.darkColor, 0, 1,
				true, true, false));
		b.add(content);
		
		content = b;
		content = new Frame(content, Theme.lightColor, 
				    Theme.darkColor, 5, 3, 
				    true, true, false);

		Model nan = new FloatModel(Float.NaN);

		final Lob sizelob = content;

		final FloatModel x = (FloatModel) m.get(WindowManager.X);
		final FloatModel y = (FloatModel) m.get(WindowManager.Y);
		final FloatModel w = (FloatModel) m.get(W);
		final FloatModel h = (FloatModel) m.get(H);

		if(Float.isNaN(w.getFloat())) {
		    w.setFloat(sizelob.getNatSize(Lob.X));
		    h.setFloat(sizelob.getNatSize(Lob.Y));
		}

		content = new RequestChangeLob(content, nan, w, nan, nan, h, nan);
		content = new DragController(content, 3, VobMouseEvent.CONTROL_MASK,
		      new RelativeAdapter(){
			  public void changedRelative(float dx, float dy) {
			      float nw = w.getFloat() + dx;
			      float nh = h.getFloat() + dy;
			      if(nw < sizelob.getMinSize(Lob.X))
				  nw = sizelob.getMinSize(Lob.X);
			      if(nw > sizelob.getMaxSize(Lob.X))
				  nw = sizelob.getMaxSize(Lob.X);
			      if(nh < sizelob.getMinSize(Lob.Y))
				  nh = sizelob.getMinSize(Lob.Y);
			      if(nh > sizelob.getMaxSize(Lob.Y))
				  nh = sizelob.getMaxSize(Lob.Y);

			      w.setFloat(nw); h.setFloat(nh);
			      anim.switchVS();
			  }
		      });
		content = new RelativeDragController(content, 1, VobMouseEvent.CONTROL_MASK,
		      new RelativeDragListener(){
			  public void change(float dx, float dy) {
			      x.setFloat(x.getFloat() + dx);
			      y.setFloat(y.getFloat() + dy);
			      anim.rerender();
			  }
		      });
		content = new TranslationLob(content, 
			       (Model)m.get(WindowManager.X), 
			       (Model)m.get(WindowManager.Y));
		this.delegate = content;
	    }
	    else
		this.delegate = NullLob.instance;

	    super.chg();
	}
    }

    
    protected WindowAnimation anim;
    public OverlappingWindowManager(WindowAnimation anim) {
	this(anim, new ListModel.Simple());
    }
    public OverlappingWindowManager(WindowAnimation anim, 
				    ListModel windows) {
	this.anim = anim;
	this.windows = windows;

	apps = new Tray(false);

	apps.setModel(new SequenceModel.ListSequenceModel(new ListModel.Transform(windows, new AppLob(Parameter.mapModel(ListModel.PARAM), virtualDesk))));

	Tray bg = new Tray(false);
	try {
	    ;//bg.add(new Image(new java.io.File("testdata/libvob.png")));
	    throw new java.io.IOException();
	} catch(java.io.IOException e) {}
	content = new Between(bg, apps, new NullLob());
    }
    private Lob content;
    public Lob getLob() { return content; }
    private Tray apps;
    protected ListModel windows;
    protected ObjectModel virtualDesk = new ObjectModel("first");

    public void add(Lob app, String title) {
	MapModel m = new MapModel.Simple();
	
	m.put(TITLE, new ObjectModel(title));
	Lob application = app;
	m.put(W, new FloatModel(Float.NaN));
	m.put(H, new FloatModel(Float.NaN));
	m.put(WindowManager.X, new FloatModel(100));
	m.put(WindowManager.Y, new FloatModel(150));
	
	m.put(CONTENT, application);
	m.put(VIRTUAL_DESK, "first");
	
	windows.add(m);
    }

    public void remove(Lob window) {
	Object remove = null;
	for (Iterator i = windows.iterator(); i.hasNext();) {
	    MapModel m = (MapModel)i.next();
	    if (m.get(CONTENT) == window) remove = m;
	}
	windows.remove(remove);
    }
}
