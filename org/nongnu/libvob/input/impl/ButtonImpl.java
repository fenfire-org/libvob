// (c): Matti J. Katila

package org.nongnu.libvob.input.impl;
import org.nongnu.libvob.input.*;
import java.util.*;

/** A single input button event typically from a mouse.
 */
public class ButtonImpl /*implements Button*/ {
    static public boolean dbg = false;
    static private void p(String s) { System.out.println("ButtonImpl:: "+s); }

    private boolean buttonPressed;


    public synchronized void status(boolean b) {
	if (dbg) p(name+": "+b);

	// if the status has changed, inform listner..

	if (clickL != null) {
	    if ((b == true) && (buttonPressed == false))
		clickL.clicked();
	} else if (switchL != null) {
	    if (b != buttonPressed) 
		switchL.switched();
	}
	buttonPressed = b;
    }



// --- Interfacing with the outer world
    
    private String name;
    public String getName() {
	return name;
    }

    public ButtonImpl(String name) { this(name, false); }
    public ButtonImpl(String name, boolean pressed) {
	this.name = name; 
	this.buttonPressed = pressed;
    }

    private ClickButtonListener clickL;
    private SwitchButtonListener switchL;
    synchronized public void setMainListener(ButtonListener listener) {
	this.clickL = null;
	this.switchL = null;
	if(listener instanceof ClickButtonListener)
	    this.clickL = (ClickButtonListener)listener;
	else {
	    this.switchL = (SwitchButtonListener)listener;
	}
    }

}
