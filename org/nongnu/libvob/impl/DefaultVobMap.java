/*
DefaultVobMap.java
 *
 *    Copyright (c) 2000-2002, Ted Nelson and Tuomas Lukka
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
package org.nongnu.libvob.impl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.awt.AWTTextStyle;
import java.awt.*;
import java.util.*;

/** An implementation of VobMap.
 */
public class DefaultVobMap implements VobMap {
    public static boolean dbg = false;
    private static void pa(String s) { System.err.println(s); }

    public void setVS(VobScene vs) { }

    int[] moreInts(int[] orig, int n) {
	int[] arr = new int[n];
	try {
	    System.arraycopy(orig, 0, arr, 0, orig.length);
	} catch(ArrayIndexOutOfBoundsException e) {
	    System.out.println("orig "+orig.length+" arr "+arr.length);
	    throw e;
	}
	return arr;
    }

    Vob[] moreVobs(Vob[] orig, int n) {
	Vob[] arr = new Vob[n];
	System.arraycopy(orig, 0, arr, 0, orig.length);
	return arr;
    }

    /** For each coordinate system, the index of the first vob
     * stored there.
     */
    int[] vstart = new int[256];

    /** For each vob, the index of the next vob in the same
     * coordinate system.
     */
    int[] vnext = new int[256];

    Vob[] vobs = new Vob[256];
    /** Indexed like vobs, the second coordinate system for the vob.
     */
    int[] secondcs = new int[256];

    int nvobs = 1;

    int nclips = 0;
    int[] clipcs = new int[64];
    int[] clipparent = new int[64];

    int nclipranges = 1;
    int[] clip = new int[64];
    int[] clippos = new int[64];

    char[] textChar = new char[64];
    int[] textRange = new int[64];
    int[] textClipRange = new int[64];

    int currange = -1;
    TextStyle[] textRangeStyle = new TextStyle[64];
    Color[] textRangeColor = new Color[64];

    int[] nextInChain = new int[64];
    int[] prevInChain = new int[64];


    public DefaultVobMap() {
        for(int i=0; i<vstart.length; i++) vstart[i] = -1;
	clip[0] = -1;
	clippos[0] = 0;
    }

    protected void ensure_clip() {
	if(nclips+1 > clipcs.length) {
	    clipcs = moreInts(clipcs, clipcs.length*2);
	    clipparent = moreInts(clipparent, clipparent.length*2);
	}
    }
    protected void ensure_clip_ranges() {
	if(nclipranges+1 > clippos.length) {
	    clip = moreInts(clip, clip.length*2);
	    clippos = moreInts(clippos, clippos.length*2); 
	}
    }

    public void clip(int cs) {
	ensure_clip();
	ensure_clip_ranges();

	clipcs[nclips] = cs;
	clipparent[nclips] = clip[nclipranges-1];
	
	clip[nclipranges] = nclips;
	clippos[nclipranges] = nvobs;

	nclips++;
	nclipranges++;
    }

    public void unclip() {
	int old = clip[nclipranges-1];
	if(old < 0)
	    throw new Error("no clip to unclip");

	ensure_clip_ranges();
	clip[nclipranges] = clipparent[old];
	clippos[nclipranges] = nvobs;

	nclipranges++;
    }



    public Vob getVobByCS(int i) {
	i = vstart[i];
	if(i<0) return null;
	while(vnext[i] >= 0) i = vnext[i];
	return vobs[i];
    }

    public void put(Vob vob) {
	put(vob, 0, -1);
    }
    public void put(Vob vob, int coordsys) {
	put(vob, coordsys, -1);
    }
    public void put(Vob vob, int coordsys, int coordsys2) {
	try {
	    vobs[nvobs] = vob;
	    secondcs[nvobs] = coordsys2;
	    vnext[nvobs] = -1;
	    nvobs++;
	} catch(ArrayIndexOutOfBoundsException e) {
	    int n = vobs.length * 2;
	    vnext = moreInts(vnext, n);
	    vobs = moreVobs(vobs, n);
	    secondcs = moreInts(secondcs, n);
	    put(vob, coordsys);
	    return;
	}
	if(vstart.length <= coordsys) {
            int n = vstart.length;
	    vstart = moreInts(vstart, coordsys*2);
            for(int i=n; i<vstart.length; i++)
                vstart[i] = -1;
        }
	if(vstart[coordsys] < 0) {
	    vstart[coordsys] = nvobs-1;
	} else {
	    // new one goes last;
	   int i = vstart[coordsys];
	   while(vnext[i] >= 0) i = vnext[i];
	   vnext[i] = nvobs-1;

	   /*
	    // New one goes first in chain;
	    vnext[nvobs-1] = vstart[coordsys];
	    vstart[coordsys] = nvobs-1;
	    */
	}
    }
    public void put(Vob vob, int [] cs) {
	throw new Error("Unimplemented in AWT");
    }



    /** Put a single character to be rendered in a particular cs,
     *  without using vobs. Together with chaining contiguous 
     *  coordinate systems (see chain() below), this allows for much
     *  faster rendering of text that can also be interpolated
     *  character-by-character (i.e., each character can potentially
     *  be interpolated to a different place).
     */
    public void putChar(TextStyle style, char c, Color color, int cs) {

	if(style == null || color == null)
	    throw new NullPointerException();

	if(cs >= textChar.length) {
	    int nlen = cs*2;

	    char[] nchar = new char[nlen];
	    int[] nrange = new int[nlen];
	    int[] nclip = new int[nlen];

	    System.arraycopy(textChar, 0, nchar, 0, textChar.length);
	    System.arraycopy(textRange, 0, nrange, 0, textRange.length);
	    System.arraycopy(textClipRange, 0, nclip, 0, textClipRange.length);

	    textChar = nchar; textRange = nrange; textClipRange = nclip;
	}

	if(currange < 0 || !textRangeStyle[currange].equals(style) ||
	   !textRangeColor[currange].equals(color)) {

	    int olen = textRangeStyle.length;

	    currange++;
	    if(currange >= olen) {
		int nlen = olen * 2;

		TextStyle[] nstyle = new TextStyle[nlen];
		Color[] ncolor = new Color[nlen];

		System.arraycopy(textRangeStyle, 0, nstyle, 0, olen);
		System.arraycopy(textRangeColor, 0, ncolor, 0, olen);

		textRangeStyle = nstyle; textRangeColor = ncolor;
	    }

	    textRangeStyle[currange] = style;
	    textRangeColor[currange] = color;
	}

	textChar[cs] = c;
	textRange[cs] = currange;

	textClipRange[cs] = nclipranges-1;
    }

    /** Chain two coordinate systems in a linked list of coordinate systems
     *  each containing one character of text.
     *  If a sequence of characters in a chain have the same text style
     *  and color, they are rendered in one call to AWT's Graphics object,
     *  resulting in much better performance. The position and scale are
     *  determined by the first coordinate system in the chain.
     *  During interpolation, the biggest sequences that are chained
     *  in both scenes are rendered as one piece. This means that
     *  when the linebreaking changes, for example, interpolation will
     *  still work correctly character-by-character.
     */
    public void chain(int cs1, int cs2) {
	if(nextInChain.length <= cs1) 
	    nextInChain = moreInts(nextInChain, cs1*2);

	if(prevInChain.length <= cs2) 
	    prevInChain = moreInts(prevInChain, cs2*2);

	nextInChain[cs1] = cs2;
	prevInChain[cs2] = cs1;

	//System.out.println("Chained "+prevInChain[cs2]+" "+cs2+" ('"+textChar[cs1]+"' -> '"+textChar[cs2]+"')");
    }



    class ChildVS { ChildVobScene child; int result; int[] cs;
	ChildVS(ChildVobScene c, int r, int[] cs_) {child=c; result=r; cs=cs_;}
    }
    Map childScenes = new HashMap();
    public int _putChildVobScene(ChildVobScene child, int coorderResult,
					int[] cs) {
	childScenes.put(""+coorderResult, new ChildVS(child, coorderResult, cs));
	put(null, coorderResult);
	return coorderResult;
    }

    public void clear() {
	nvobs = 1;
	nclips = 0;
	nclipranges = 1;
        for(int i=0; i<vstart.length; i++) vstart[i] = -1;

	currange = -1;

	Arrays.fill(textChar, (char)0);
	Arrays.fill(nextInChain, 0);
	Arrays.fill(prevInChain, 0);
    }

    /** An interface for setting the vob render info for the second coordinate system
     * of selected vobs.
     */
    public static interface RenderInfoSetter {
	/**
	 * @return true, if the system was set; false if interpolation
	 * can't be done so the vob should not be rendered.
	 */
	boolean set(Vob.RenderInfo info, int cs);
    }
    
    protected final int cmpClipRange(int vob, int range) {
	int clipMin = clippos[range];
	int clipMax = range+1<nclipranges ? clippos[range+1] : nvobs;
	if(vob < clipMin) return -1;
	if(vob < clipMax) return 0;
	else return 1;
    }

    protected final int findClipRange(int vob) {
	int lower = 0, upper = nclipranges;
	while(true) {
	    int range = lower + (upper-lower)/2;
	    int cmp = cmpClipRange(vob, range);
	    if(cmp == 0)
		return range;
	    if(upper-lower < 2)
		return range-cmp;
	    if(cmp < 0)
		upper = range;
	    else
		lower = range;
	}

	/** old, simpler but slower version
	for(int range=0; range<nclipranges; range++)
	    if(cmpClipRange(vob, range) == 0)
	        return range;
	throw new Error("No clip range found for vob (XXX shouldn't happen): "+vob);
	**/
    }

    /** Render the vobs in the given coordinate system.
     * The Vobs whose *first* coordinate system is the given one
     * are also rendered (crude hack for now).
     * @return the new lastClip
     */
    public int renderCS(int cs, Vob.RenderInfo info, Graphics g, 
			RenderInfoSetter setter, Vob.RenderInfo _info2, 
			int lastClip, Shape noClip, 
			VobScene other, int[] interpList) {
	OrthoRenderInfo info2 = (OrthoRenderInfo)_info2;

	if(cs < textChar.length && textChar[cs] > 0)
	    lastClip = renderText(cs, info, g, other, interpList, 
				  lastClip, noClip, info2, setter
);

	// It is possible that no vob has been put in 
	// any coordinate system >= cs.
	if(cs >= vstart.length) return lastClip;

	for(int i = vstart[cs]; i >= 0; i = vnext[i]) {
	    // pa("Render: "+vobs[i]);

	    if(cmpClipRange(i, lastClip) != 0) {
		
		lastClip = findClipRange(i);
		//assert(cmpClipRange(i, lastClip) == 0);
		setClip(g, lastClip, noClip, info2, setter);
	    }

	    //pa("vob info : "+info);
	    if(secondcs[i] >= 0 && setter != null) {
		if(setter.set(info2, secondcs[i]))
		    vobs[i].render(g, false, info, info2);
	    } else {
		vobs[i].render(g, false, info, info);
	    }
	}

	return lastClip;
    }

    void setClip(Graphics g, int clipRange, Shape noClip, Vob.RenderInfo info,
		 RenderInfoSetter setter) {
	g.setClip(noClip);
	
	for(int c=clip[clipRange]; c>=0; c=clipparent[c])
	    if(setter.set(info, clipcs[c]))
		g.clipRect((int)info.x, (int)info.y, 
			   (int)info.width, (int)info.height);
    }

    char[] chars = new char[256];

    int renderText(int cs, Vob.RenderInfo info, Graphics g,
		   VobScene other, int[] interpList, 
		   int lastClip, Shape noClip, Vob.RenderInfo clipInfo,
		   RenderInfoSetter setter) {

	if(!isFirstInFragment(cs, other, interpList))
	    return lastClip;

	int i = 0;
	int ics = cs;

	do {
	    if(chars.length <= i) {
		char[] nc = new char[2*chars.length];
		System.arraycopy(chars, 0, nc, 0, chars.length);
		chars = nc;
	    }

	    chars[i] = textChar[ics];

	    i++;
	    //System.out.println(i);

	    if(ics >= nextInChain.length)
		break;

	    ics = nextInChain[ics];
	} while(ics > 0 && ics < textChar.length && textChar[ics] > 0 &&
		!isFirstInFragment(ics, other, interpList));

	int len = i;

	int range = textRange[cs];

	TextStyle style = textRangeStyle[range];
	chars[0] = textChar[cs];
	g.setColor(textRangeColor[range]);

	int myClip = textClipRange[cs];
	if(myClip != lastClip) {
	    setClip(g, myClip, noClip, clipInfo, setter);
	    lastClip = myClip;
	}

        float x = info.x, y = info.y;
        float w = info.width, h = info.height;

	float scale = style.getScaleByHeight(h);
	float fasc = style.getAscent(scale);
	float fdsc = style.getDescent(scale);
	float fh = fasc + fdsc;
	float ty = y + h/2 + fasc/2;
        
	((AWTTextStyle)style).render(g, (int)x, (int)ty, chars, 0, len,
				     scale, info);

	return lastClip;
    }

    boolean isFirstInFragment(int cs, VobScene other, int[] interpList) {
	if(cs >= prevInChain.length) return true;

	int prev = prevInChain[cs];
	if(prev == 0 || textChar[prev] == 0) return true;
	if(textRange[prev] != textRange[cs]) return true;

	//System.out.println("prev != 0");

	if(interpList == null) 
	    return false;
	else {
	    DefaultVobMap o = (DefaultVobMap)other.map;

	    if(cs >= interpList.length) return true;
	    if(prev >= interpList.length) return true;

	    int ocs = interpList[cs];
	    if(ocs < 0) return true;
	    if(ocs >= o.prevInChain.length) return true;
	    int oprev = o.prevInChain[ocs];

	    /** I first thought the checks below would be good,
	     *  but in fact they're unnecessary and cost quite a lot
	     *  of performance. It is enough that the two cs are chained
	     *  in both vobscenes; it doesn't matter whether they would
	     *  be rendered in one call in both scenes (which is what
	     *  the stuff below checks).
	     *
	     *  The reason it impacts performance is the case where
	     *  a text string is rendered invisibly -- the cs are put in
	     *  but the actual text isn't, so that interpolation to a scene
	     *  where the text *is* visible will work right.
	     *
	     *  If we have the checks below, then in that case, when rendering
	     *  the vobscene that *has* the characters, during interpolation,
	     *  it will render every character in an own AWT call.
	     *
	    //if(o.textChar[oprev] == 0) return true;
	    //if(o.textRange[oprev] != o.textRange[ocs]) return true;
	    **/

	    return interpList[prev] != oprev;
	}
    }

    public void dump() {
	pa("VOBMAP");
	for(int i=0; i<vstart.length; i++) {
	    if(vstart[i] < 0) continue;
	    pa("  CS "+i);
	    for(int j = vstart[i]; j >= 0; j = vnext[j]) {
		pa("     "+j+" "+vobs[j]);
	    }

	}
    }
}
