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

package org.nongnu.libvob.util;
import java.io.*;

/** A simple class to read IMPS/2 mouse events.
 */

public class PS2Reader {
    public interface Listener {
	void chg(int x, int y, int z, boolean b1, boolean b2, boolean b3);
    };

    private RandomAccessFile in;
    private Listener chg;

    public void setListener(Listener chg) {
	this.chg = chg;
    }

    static final byte[] imps2 = new byte[] {
	(byte)0xf3, (byte)0xc8, (byte)0xf3, (byte)0x64, (byte)0xf3, (byte)0x50
    };
    public PS2Reader(String file) throws IOException {
	in = new RandomAccessFile(file, "rw");
	in.write(imps2);
    }

    public int x;
    public int y;
    public int z;

    public boolean b1;
    public boolean b2;
    public boolean b3;

    Thread t;

    public void start() {
	if(t != null) return;
	t = new Thread(new Runnable() {
	    public void run() {
		while(true) {
		    try {
			PS2Reader.this.read();
		    } catch(Exception e) {
		    }
		}
	    }
	});
	t.start();
    }

    private byte[] buf = new byte[4];
    private void read() throws IOException {
	int n = in.read(buf);
	if(n < 0) throw new Error("EOF? On file!");
	if(n < 4) return;
	if((buf[0] & 8) == 0) {
	    // Resynch
	    in.read();
	    return;
	}
	int dx = buf[1];
	int dy = buf[2];
	int dz = buf[3];

	x += dx;
	y += dy;
	z += dz;

	b1 = (buf[0] & 1) != 0;
	b2 = (buf[0] & 2) != 0;
	b3 = (buf[0] & 4) != 0;

	if(chg != null)
	    chg.chg(x, y, z, b1, b2, b3);
    }

    public void print() {
	System.out.println(""+x+"\t"+y+"\t"+z+"\t"+b1+
			"\t"+b2+"\t"+b3);
    }

    static public void main(String[] argv) throws Exception {
	PS2Reader r = new PS2Reader(argv[0]);
	while(true) {
	    r.read();
	    r.print();
	}
    }

}
