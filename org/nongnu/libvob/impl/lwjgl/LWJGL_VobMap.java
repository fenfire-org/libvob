/*   
GLVobMap.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
 *                  2003, Tuomas J. Lukka
 *                  2004, Matti J. Katila
 *
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Tuomas Lukka
 */
package org.nongnu.libvob.impl.lwjgl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.gl.*;
import javolution.realtime.Realtime;
import java.awt.*;

public class LWJGL_VobMap implements VobMap {
    public static boolean dbg = false;
    private static void p(String s) { System.out.println("GLVobMap:: "+s); }

    LWJGL_Screen screen;
    VobScene vs;

    public LWJGL_VobMap(LWJGL_Screen screen) { 
	this.screen = screen; 
	if(dbg) p("New GLVobMap for "+screen+": "+this);
	list = new int[20000];
	cs = new int[20000];
	vobs = new Vob[20000];
	clear();
    }

    public void setVS(VobScene vs) { this.vs = vs; }

    int[] list;
    int curs;
    int[] cs;
    Object[] vobs;
    int nvobs;

    public void clear() {
	for(int i=0; i<curs; i++)
	    list[i] = 0;

	for(int i=0; i<nvobs; i++) {
	    cs[i] = 0;

	    if(vobs[i] == null) continue;

	    // decrement Javolution reference counter
	    ((Vob)vobs[i]).move(Realtime.ObjectSpace.LOCAL);

	    // XXX does this still cause gc and take too much time
	    // even when using Realtime?
	    vobs[i] = null; 
	}
	 
	curs = 0;
	nvobs = 0;
    }

    public void put(Vob vob, int[] scs) {
	if(dbg) p("Add to GLVobMap "+this+":  "+vob+" "+cs+" "+cs.length);
	vob.move(Realtime.ObjectSpace.HOLD);
	int ind = vob.putGL(vs, cs);
	if(ind == 0) return;
	// Now, stash it away.
	cs[nvobs] = scs[0];
	vobs[nvobs++] = vob;
	list[curs++] = (GL.RENDERABLEN | ind);
	list[curs++] = scs.length;
	for(int i=0; i<scs.length; i++) 
	    list[curs++] = scs[i];
    }
    public void put(Vob vob, int coordsys1, int coordsys2, int coordsys3) {
	if(dbg) p("Add to GLVobMap "+this+":  "+vob+" "+coordsys1+" "
		   +coordsys2+" "+coordsys3+" curs: "+curs);
	vob.move(Realtime.ObjectSpace.HOLD);
	int ind = vob.putGL(vs, coordsys1, coordsys2, coordsys3);
	if(ind == 0) return;
	// Now, stash it away.
	cs[nvobs] = coordsys1;
	vobs[nvobs++] = vob;
	list[curs++] = (GL.RENDERABLE3 | ind);
	list[curs++] = coordsys1;
	list[curs++] = coordsys2;
	list[curs++] = coordsys3;
    }
    public void put(Vob vob, int coordsys1, int coordsys2) {
	if(dbg) p("Add to GLVobMap "+this+":  "+vob+" "+coordsys1+" "
		   +coordsys2+" curs: "+curs);
	vob.move(Realtime.ObjectSpace.HOLD);
	int ind = vob.putGL(vs, coordsys1, coordsys2);
	if(ind == 0) return;
	// Now, stash it away.
	cs[nvobs] = coordsys1;
	vobs[nvobs++] = vob;
	list[curs++] = (GL.RENDERABLE2 | ind);
	list[curs++] = coordsys1;
	list[curs++] = coordsys2;
    }
    public void put(Vob vob, int coordsys1) {
	if(dbg) p("Add "+this+":  "+vob+" "+coordsys1+
		   " curs: "+curs);
	vob.move(Realtime.ObjectSpace.HOLD);
	int ind = vob.putGL(vs, coordsys1);
	if(ind == 0) return;
	// Now, stash it away.
	cs[nvobs] = coordsys1;
	vobs[nvobs++] = vob;
	list[curs++] = (GL.RENDERABLE1 | ind);
	list[curs++] = coordsys1;
    }
    public void put(Vob vob) {
	if(dbg) p("Add "+this+":  "+vob+ " curs: "+curs);
	vob.move(Realtime.ObjectSpace.HOLD);
	int ind = vob.putGL(vs);
	if(ind == 0) return;
	// Now, stash it away.
	vobs[nvobs++] = vob;
	list[curs++] = (GL.RENDERABLE0 | ind);
    }
    public void dump() {
	p("GLVobMap");
	for(int i=0; i<nvobs; i++) {
	    p(cs[i] + " "+vobs[i]);
	}
	String s = "";
	for(int i=0; i<curs; i++) {
	    s = s + " " + list[i];
	}
	p(s);
    }
    public Vob getVobByCS(int csind) {
	for(int i=nvobs-1; i>=0; i--) {
	    if(cs[i] == csind)
		return (Vob)vobs[i];
	}
	return null;
    }

    public int _putChildVobScene(ChildVobScene child, int coorderResult,
					int[] cs) { 
	if(dbg) p("put child: "+child+", "+this+":  "+cs);
	if(coorderResult == 0) return 0;

	// Now, stash it away.
	if (true) throw new Error("unimpl.");
	/*
	list[curs++] = (GL.RENDERABLE_VS |
			((GLChildVobScene)child).childVS.getChildVSId());
	list[curs++] = coorderResult;
	*/
	/*
	list[curs++] = cs.length;
	for (int i=0; i<cs.length; i++)
	    list[curs++] = cs[i];
	*/
	return coorderResult;
    }

}

