package org.nongnu.libvob.impl.lwjgl;

import java.awt.Dimension;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.nongnu.libvob.AbstractUpdateManager;
import org.nongnu.libvob.Binder;
import org.nongnu.libvob.ChildVobScene;
import org.nongnu.libvob.GraphicsAPI;
import org.nongnu.libvob.VobMap;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.GraphicsAPI;
import org.nongnu.libvob.gl.GL;
import org.nongnu.libvob.gl.GLVobCoorder;
import org.nongnu.libvob.gl.impl.lwjgl.LwjglRenderer;
import org.nongnu.libvob.impl.DefaultVobMatcher;
import org.nongnu.libvob.impl.gl.GLVobMap;

public class LWJGL_Screen extends GraphicsAPI.AbstractRenderingSurface
	implements GraphicsAPI.Window {

    static public boolean dbg = true;
    
    static private void p(String s) {
	System.out.println("LwjglScreen:: " + s);
    }

    private Object SCREENSIZEKEY = new Object();

    public LWJGL_Screen(GraphicsAPI api) {
	super(api);
	try {
	    GL.init();
	    Display.setDisplayMode(new DisplayMode(1, 1));
	    Display.setFullscreen(false);
	    Display.create();
	    Display.setTitle("Grazy Little Window");
	} catch (LWJGLException e) {
	    e.printStackTrace();
	}
    }

    public void finalize() throws Throwable {
	super.finalize();
	Display.destroy();
    }

    public Dimension getSize() {
	DisplayMode mode = Display.getDisplayMode();
	return new Dimension(mode.getWidth(), mode.getHeight());
    }

    public void renderStill(VobScene vs, float lod) {
	p("render still");

	LwjglRenderer.getInstance().render((LWJGL_VobMap)vs.map, null, (GLVobCoorder)vs.coords, null, 0, vs.getSize());
    }

    public void renderAnim(VobScene prev, VobScene next, float fract, float lod,
	    boolean showFinal) {
	p("render anim");

	VobScene sc = prev;
	VobScene osc = next;
	boolean towardsOther = true;
	if (fract > AbstractUpdateManager.jumpFract) {
	    sc = next;
	    osc = prev;
	    fract = 1-fract;
	    towardsOther = false;
	}
	if(osc == null) osc = sc;
	if(dbg) {
	    p("Going to render: "+sc+" "+osc+" "+fract);
	    sc.dump();
	}

	createInterpList(sc, osc, towardsOther);

	LwjglRenderer.getInstance().render((LWJGL_VobMap)sc.map, interplist, 
			(GLVobCoorder) sc.coords, (GLVobCoorder)osc.coords, 
			fract, prev.getSize());

    }

    VobScene listprev, listnext;
    int[] interplist;

    protected void createInterpList(VobScene sc, VobScene osc,
	    boolean towardsOther) {
	if (sc != listprev || osc != listnext) {
	    listprev = sc;
	    listnext = osc;
	    interplist = sc.matcher.interpList(osc.matcher, towardsOther);
	    interplist[0] = interplist.length;
	}
    }

    public boolean needInterp(VobScene prev, VobScene next) {
	createInterpList(prev, next, true);
	if (interplist == null)
	    return false;
	return prev.coords.needInterp(next.coords, interplist);
    }

    public VobScene createVobScene() {
	return createVobScene(getSize());
    }

    public VobScene createVobScene(Dimension size) {
	VobScene vs = new VobScene(new LWJGL_VobMap(this), new GLVobCoorder(),
		new DefaultVobMatcher(), this.getGraphicsAPI(), this, size);

	// Put the cs no 1, i.e. the screen size
	vs.boxCS(0, SCREENSIZEKEY, size.width, size.height);
	return vs;
    }

    public ChildVobScene createChildVobScene(Dimension size,
	    int numberOfParameterCS) {
	return null;
    }

    public int[] readPixels(int x, int y, int w, int h) {
	return null;
    }

    public void setLocation(int x, int y, int w, int h) {
	Display.setLocation(x, y);
	try {
	    Display.setDisplayMode(new DisplayMode(w, h));
	} catch (LWJGLException e) {
	    e.printStackTrace();
	}
    }

    public void setCursor(String name) {
    }

    Binder binder;

    public void registerBinder(Binder s) {
	binder = s;
    }

    public void addTimeout(int ms, Object o) {
    }

}
