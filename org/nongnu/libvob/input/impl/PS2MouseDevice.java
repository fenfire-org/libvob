/*
PS2Reader.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 */
/*
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.input.impl;
import org.nongnu.libvob.input.*;
import java.util.*;
import java.io.*;

/** A simple class to read IMPS/2 mouse events.
 *  <p>
 *  ref: Implementation information found from file 
 *  /usr/src/linux/drivers/usb/input/usbmouse.c for buttons.
 *  
 *  other ref:
 *  http://home.t-online.de/home/gunther.mayer/gm_psauxprint-0.01.c
 */

public class PS2MouseDevice implements InputDevice {
    public static boolean dbg = false;
    private static void pa(String s) { System.out.println(s); }

    private String name;
    public String getName() { return name; }

    private RelativeAxis[] axes ;
    public List getAxes() {
	return Collections.unmodifiableList(
		    Arrays.asList(axes));
    }
    private ButtonImpl[] buttons;
    public List getButtons() {
	return Collections.unmodifiableList(
		    Arrays.asList(buttons));
    }

    private RandomAccessFile in;

    public boolean b1;
    public boolean b2;
    public boolean b3;

    Thread t;

    static final byte[] imps2 = new byte[] {
	(byte)0xf3, (byte)0xc8, (byte)0xf3, (byte)0x64, (byte)0xf3, (byte)0x50
    };

    private int proto;

    public final static int PS2_PROTO = 1;
    public final static int IMPS_PROTO = 2;
    public final static int IMEX_PROTO = 2;

    public PS2MouseDevice(String file, String name)  
		throws IOException {
	this(file, name, PS2_PROTO, 2);
    }
    public PS2MouseDevice(String file, String name, int proto, int buttonCount) 
		throws IOException {

	this.name = name;
	in = new RandomAccessFile(file, "rw");

	this.proto = proto;
	if(proto == IMPS_PROTO) {
	    in.write(imps2);
	    axes = new RelativeAxis[3]; 
	    buf = new byte[4];
	    buttons = new ButtonImpl[buttonCount];
	} else {
	    axes = new RelativeAxis[2];
	    buf = new byte[3];
	    buttons = new ButtonImpl[buttonCount];
	}

	this.axes[0] = new RelativeAxis("x");
	this.axes[1] = new RelativeAxis("y");
	if(axes.length > 2)
	    this.axes[2] = new RelativeAxis("z");

	for (int i=0; i<this.buttons.length; i++)
	    this.buttons[i] = new ButtonImpl(""+i);


	t = new Thread(new Runnable() {
	    public void run() {
		while(true) {
		    try {
			PS2MouseDevice.this.read();
		    } catch(Exception e) {
		    }
		}
	    }
	});
	t.setDaemon(true);
	t.setPriority(Thread.MAX_PRIORITY);
	t.start();
    }

    private byte[] buf;

    private void read() throws IOException {
	for(int i=0; i<buf.length; i++) {
	    int r = in.read();
	    if(r < 0) throw new Error("EOF? On file!");
	    buf[i] = (byte)r;
	    if(dbg) pa("Got "+buf[i]);
	}
	if(dbg) pa("Read");

	int dx, dy, dz;

	if((buf[0] & 8) == 0) {
	    // Resynch
	    if(dbg) pa("Resynch");
	    in.read();
	    return;
	}

	b1 = (buf[0] & 1) != 0;
	b2 = (buf[0] & 2) != 0;
	b3 = (buf[0] & 4) != 0;

	if(proto == IMPS_PROTO) {
	    dx = buf[1];
	    dy = buf[2];
	    dz = buf[3];
	} else {
	    dx = buf[1];
	    dy = buf[2];
	    dz = 0;
	}


	if(dbg) pa("Did: "+dx+" "+dy+" "+dz);

	if(dx != 0) axes[0].changedRelative(dx);
	if(dy != 0) axes[1].changedRelative(dy);
	if(dz != 0) axes[2].changedRelative(dz);

	for (int i=0; i<buttons.length; i++) {
	    if (i<4)
		buttons[i].status( (buf[0] & (1 << i)) != 0);
	    else if ( (buf[0] & 0x10) != 0)				  
		buttons[i].status( (buf[0] & (1 << (i-4))) != 0);
	}
    }

}

