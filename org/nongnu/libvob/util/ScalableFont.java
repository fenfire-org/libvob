/*
ScalableFont.java
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
 * Written by Rauli Ruohonen, Benja Fallenstein and Asko Soukka
 */

package org.nongnu.libvob.util;
import org.nongnu.libvob.*;
import java.awt.*;
import java.util.*;

/** A font object from which it is easy to obtain
 * instances and metrics scaled to requested multiplications.
 * This exists because our target, the Java API 1.1.8 does not
 * contain scaling functionality.
 * <p>
 * Scale is used in units of one thousand, i.e. 1000 = "normal" size.
 */

public final class ScalableFont {
    public static boolean dbg=false;
    private static final void pa(String s) { System.out.println(s); }

    /** If set, font metrics will not be cached.
     */
    public static boolean dontCache=false;

    /** Used to get FontMetrics for a Font. 
     * Should be set in e.g. main(). 
     */
    public static Component fmComp;
    {
	// just to get this initialized always... is it bad to do it like this?
	fmComp = new Panel();
    }

    /** A cache for Fonts and FontMetrics of same family and style.
     */
    static class Cache {
	String family;
	int style;
	Font[] fonts = new Font[20];
	FontMetrics[] metrics = dontCache ? new FontMetrics[0] 
	                                  : new FontMetrics[20];
	
	Cache(String family, int style) {
	    this.family = family;
	    this.style = style;
	}

	Font getFont(int pt) {
	    if(fonts.length <= pt) {
		Font[] nfonts = new Font[pt+1];
		System.arraycopy(fonts, 0, nfonts, 0, fonts.length);
		fonts = nfonts;
	    }
	    Font f = fonts[pt];
	    if(f == null) {
		f = new Font(family, style, pt);
		fonts[pt] = f;
	    }
	    return f;
	}

	FontMetrics getFontMetrics(int pt) {
	    if(pt < 0) 
		throw new IllegalArgumentException("Negative point size: "+pt);

	    if(dontCache) return fmComp.getFontMetrics(getFont(pt));

            if(metrics.length <= pt) {
                FontMetrics[] nmetrics = new FontMetrics[pt+1];
                System.arraycopy(metrics, 0, nmetrics, 0, metrics.length);
                metrics = nmetrics;
            }
            FontMetrics m = metrics[pt];
            if(m == null) {
                m = fmComp.getFontMetrics(getFont(pt));
                metrics[pt] = m;
            }
	    return m;
	}
    }

    /** The font and font metrics caches.
     */
    private static Map caches = new HashMap();

    /** The cache for this scalable font.
     */
    private Cache myCache;

    /** Cache for the scale-by-height function.
     */
    private float[] scaleByHeight = new float[256];

    /** The point size when scaling factor is 1.
     */
    private int defPt;

    /** Constructor similar to Java's Font(f,s,p).
     * @param pointsize Point size when scaling factor is 1.
     */
    public ScalableFont(String family, int style, int pointsize) {
	defPt = pointsize;

	String id = family + "/" + style; // XXX I dislike the ID -tjl
	myCache = (Cache)caches.get(id);
	if(myCache == null) {
	    myCache = new Cache(family, style);
	    caches.put(id, myCache);
	}
    }

    /** Get a scaled instance of the font.
     * @param scale        Scaling factor is (scale/1000).
     * @return May not return null.
     */
    public Font getFont(float scale) {
	int pt=scale2pt(scale);
	return myCache.getFont(pt);
    }

    FontMetrics lastFM;
    float lastFMScale = -1;

    /** Get font metrics for a scaled instance of the font.
     * @param scale        Scaling factor is (scale/1000).
     * @return May not return null.
     */
    public FontMetrics getFontMetrics(float scale) {
	if(scale < 0) 
	    throw new IllegalArgumentException("Negative scale: "+scale);

	if(scale == lastFMScale) return lastFM;
	int pt=scale2pt(scale);
	FontMetrics fm = myCache.getFontMetrics(pt);
	lastFM = fm; lastFMScale = scale;
	return fm;
    }

    private int scale2pt(float scale) {
	return (int)((defPt*scale+0.5));
    }

    /** Gets the scale for the point size which is nearest the given height.
     */
    public float getScale(float height) {
	int index = (int)height;
	if(index < 0) index = 0;
	if(index < scaleByHeight.length && scaleByHeight[index] > 0)
	    return scaleByHeight[index];

	float start = 1f;
	if (dbg) pa("Start with start: "+start+" height: "+getFontMetrics(start).getHeight());
	while (getFontMetrics(start).getHeight() < height) {
	    if (dbg) pa("Increment start, was: "+start);
	    start += 1f;
	}
	if (dbg) pa("Intended height: " + height);
	if (dbg) pa("Start: "+start);
	if (dbg) pa("Seeking area from " + (start-1f) + " (height: " +
		   getFontMetrics(start-1f).getHeight() + ") to " + start + " (height: " +
		   getFontMetrics(start).getHeight() + ")");

	if ((int)getFontMetrics(start-1f).getHeight() == (int)height) {
	    if (dbg) pa("Best: " + (start-1f) + " (height: " +
		   getFontMetrics(start-1f).getHeight() + ")");
	    return start-1f;
	} else if ((int)getFontMetrics(start).getHeight() == (int)height) {
	    if (dbg) pa("Best: " + (start) + " (height: " +
		   getFontMetrics(start).getHeight() + ")");
	    return start;
	}

	if (dbg) pa("Enters a recursive search for the best font scale for given height.");
	float result = seekBestScale(start-1f, start, 0, height);

	if(index < scaleByHeight.length)
	    scaleByHeight[index] = result;

	return result;
	
	/*
	float seek = seekBestScale(start-1f, start, 0, height);

	float start_diff = Math.abs(getFontMetrics(start-1f).getHeight() - height);
	float seek_diff = Math.abs(getFontMetrics(seek).getHeight() - height);
	float end_diff = Math.abs(getFontMetrics(start).getHeight() - height);	

	if (start_diff < end_diff) {
	    if (start_diff < seek_diff) return start-1f;
	    else return seek;
	} else if (seek_diff < end_diff) return seek;
	else return start;
	*/
    }

    /** Seeks recursively the best font scale. Seeks area ]start, end[.
     * @param start begin of the scale area to seek
     * @param end end of the scale area to seek
     * @param best currently the best scale found
     * @param height the intended height of drawn text
     */
    private float seekBestScale(float start, float end, float best, float height) {
	if (getFontMetrics(start).getHeight() > height) return 1; // for fail-safety
	if (dbg) pa("Start: " + start + " End: " + end + " Best: " + best + " Height: " + height);

	float current = start + (end-start)/2;
	float current_height = getFontMetrics(current).getHeight();
	float best_height = getFontMetrics(best).getHeight();

	if ((int)current_height == (int)height && (int)current_height != (int)best_height) {
	    if (dbg) pa("Best: " + current_height + " (height: " + current_height + ")");
	    return current;
	} else if ((int)current_height == (int)best_height) {
	    if (dbg) pa("Best: " + best_height + " (height: " + best_height + ")");
	    return best;
	} else best = current;

	if (current_height > height) return seekBestScale(start, current, best, height);
	else return seekBestScale(current, end, best, height);
    }
}
