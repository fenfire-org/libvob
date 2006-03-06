package org.nongnu.libvob.impl.lwjgl;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.nongnu.libvob.AbstractUpdateManager;
import org.nongnu.libvob.Binder;
import org.nongnu.libvob.VobMouseEvent;

public class LWJGLUpdateManager extends AbstractUpdateManager {

    public static void startUpdateManager(Runnable r) {
	LWJGLUpdateManager mgr = new LWJGLUpdateManager(r);
	new Thread(mgr).start();
    }

    protected LWJGLUpdateManager(Runnable r) {
	super(r);
    }

    /* There can be only one screen in lwjgl so we can put the binder here.
     */
    public Binder binder;

    protected boolean handleEvents(boolean waitForEvent) {
	// check wheher the window is minimized or under others..
	if (!Display.isVisible())
	{
	    // sleep sometime perhaps
	    // return false;
	}

	boolean eventsWereReceived = false;
	Keyboard.poll();
	while (Keyboard.next())
	{
	    // stupid impl.
	    eventsWereReceived = true;
	    int key = Keyboard.getEventKey();
	    System.out.println("key event:: name: "+Keyboard.getKeyName(key)+
		    ", char: "+ Keyboard.getEventCharacter()+
		    ", state: "+Keyboard.getEventKeyState());
	    binder.keystroke(Keyboard.getKeyName(key));
	}
	Mouse.poll();
	while (Mouse.next())
	{
	    // stupid impl.
	    eventsWereReceived = true;
	    int x = Mouse.getX(), y = Mouse.getY(), z = Mouse.getDWheel();
	    int button = Mouse.getEventButton();
	    int state = VobMouseEvent.MOUSE_PRESSED;
	    int modifiers = 0;
	    int type = VobMouseEvent.MOUSE_MOVED;
	    VobMouseEvent ev = new VobMouseEvent(type, x,y,z, modifiers , button);
	    System.out.println(ev);
	    binder.mouse(ev);
	}
	return eventsWereReceived;
    }

    protected void interruptEventloop() { 
    }

    protected void synchronizeToolkit() { }

}
