/*
OrthoCoorder.java
 *
 *    Copyright (c) 2000-2002, Tuomas Lukka
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
package org.nongnu.libvob.impl.awt;
import org.nongnu.libvob.impl.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.vobs.SolidBackdropVob;
import java.awt.Color;
import java.util.*;

/** A set of ortho coordinate systems for vobs.
 */
public class AWTRenderer {
    public static boolean dbg = false;
    private static void p(String s) { System.out.println("AWTRenderer:: "+s); }


    /** Render the vobs, fract towards the interpTo coordinates.
     */
    static public void render(
	final VobScene sc, final VobScene osc, final boolean towardsOther,
	final float fract, java.awt.Graphics g, Color fg)
    {

	if(dbg) sc.coords.dump();
        if(dbg) sc.map.dump();
        Color bg;
        Vob bgvob = sc.map.getVobByCS(0);
        if(bgvob instanceof SolidBackdropVob) {
            bg = ((SolidBackdropVob)bgvob).color;
            if(dbg) p("Background color: "+bg);
        } else {
            bg = Color.white;
            if(dbg) p("NO SOLIDBG VOB: Fall back on white bg color");
        }
        // what is maxdepth?
	OrthoRenderInfo info = new OrthoRenderInfo(bg, 10000); 
	OrthoRenderInfo info2 = new OrthoRenderInfo(bg, 10000);

	renderImpl(sc, osc, towardsOther, 
		   fract, g, fg, 
		   info, info2);
    }

    static void renderImpl(
	final VobScene sc, final VobScene osc, final boolean towardsOther,
	final float fract, java.awt.Graphics g, Color fg,
	OrthoRenderInfo info,
	OrthoRenderInfo info2)
    {
	long m1 = System.currentTimeMillis();
	
	int [] tmp = null;
	if (sc != null && osc != null)
	    tmp = sc.matcher.interpList(osc.matcher, towardsOther);
	final int [] interpList = tmp;

	DefaultVobMap.RenderInfoSetter setter = new DefaultVobMap.RenderInfoSetter() {
	    public boolean set(Vob.RenderInfo info, int my) {
		int other = -1;
		if(osc != null) {
		    if(dbg) p("...interpTo != null");
                    if(my == 0) {
                        if(dbg) p("...my == 0.");
			((AWTVobCoorderBase)sc.coords).setInterpInfo(
			    my, ((AWTVobCoorderBase)osc.coords), 0, 
			    fract, (OrthoRenderInfo)info);
                        return true;
                    }
                    
		    try {
		        other = interpList[my];
		        if(other < 0) {
			    return false;
			    /* // render anyway:
			    ((AWTVobCoorderBase)sc.coords).setInfo(
			        my, (OrthoRenderInfo)info);
			    return true;
			    */
			}
		    } catch(ArrayIndexOutOfBoundsException _) {
		        // XXX Not all coordsys must be in the matcher.
			// Therefore, it is legal for the matcher
			// to return an array that is too short.
			return false;
		    }
		    ((AWTVobCoorderBase)sc.coords).setInterpInfo(
			my, ((AWTVobCoorderBase)osc.coords), other, fract,
			(OrthoRenderInfo)info);
		} else {
		    if(dbg) p("...interpTo == null");
		    ((AWTVobCoorderBase)sc.coords).setInfo(
			my, (OrthoRenderInfo)info);
		}
		if(dbg) p("...set done.");
		return true;
	    }
	};



	
        if(dbg) sc.map.dump();

	if(dbg) p("Start rendering." + sc);
	long m2 = System.currentTimeMillis();
	AWTVobCoorderBase c = (AWTVobCoorderBase) sc.coords;
	c.sorter.sort();
	int[] sorted = c.sorter.sorted;
	int nsorted = c.sorter.nsorted;

	long m3 = System.currentTimeMillis();
	java.awt.Shape noClip = g.getClip();
	int lastClip = ((DefaultVobMap)sc.map).renderCS(
	    0, info, g, setter, info2, 0, noClip, osc, interpList);
	for(int i=0; i<nsorted; i++) {
	    if(sorted[i]< c.numberOfParameterCS) continue;
	    if(c.isChildVS(sorted[i])) {
		// XXX child vs shall be rendered here if rendered at all!

		if (dbg) p("render child! ----------------------------------");

		ChildVobScene ocsc = null;
		/*
		if (osc != null && interpList[sorted[i]] > 0) {
		    
		    ocsc = ((AWTVobCoorderBase)osc.coords).sys.children[
			interpList[sorted[i]]];
		}
		*/
		
		AWTVobCoorderBase childCoord = 
		    (AWTVobCoorderBase) c.children[sorted[i]].coords;
		if (dbg) childCoord.dump();
		if (dbg) p("sys: "+childCoord);
		childCoord.parentCS = sorted[i];
		childCoord.parentCoordsys = c;
		renderImpl(
			c.children[sorted[i]],
			ocsc, towardsOther,
			fract, 
			g, fg,
			info, info2
			);

		if (dbg) p("child DONE! --------------------------------------");
		continue;
	    }
	    if(dbg) p("...set: "+sorted[i]);
	    if(setter.set(info, sorted[i])) {
	        if(dbg) p("...render: "+sorted[i]);
	        lastClip = 
		    ((DefaultVobMap)sc.map).renderCS(
			sorted[i], info, g, setter,
			info2, lastClip, noClip, osc, interpList);
	    }
	}
	if(dbg) p("End rendervobs");


	long m4 = System.currentTimeMillis();
	//p("Rendered in "+(m2-m1)+"+"+(m3-m2)+"+"+(m4-m3)+" ms");
    }

}
