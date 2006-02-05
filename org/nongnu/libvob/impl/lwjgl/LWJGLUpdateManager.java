package org.nongnu.libvob.impl.lwjgl;

import org.nongnu.libvob.AbstractUpdateManager;

public class LWJGLUpdateManager extends AbstractUpdateManager {

    public static void startUpdateManager(Runnable r) {
	LWJGLUpdateManager mgr = new LWJGLUpdateManager(r);
	new Thread(mgr).start();
    }

    protected LWJGLUpdateManager(Runnable r) {
	super(r);
    }

    protected boolean handleEvents(boolean waitForEvent) {
	
	
	return false;
    }

    protected void interruptEventloop() { }

    protected void synchronizeToolkit() { }

}
