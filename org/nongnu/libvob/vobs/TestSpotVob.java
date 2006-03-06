/*
TestSpotVob.java
 *    
 *    Copyright (c) 2002, Tuomas J. Lukka
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
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.vobs;
import org.nongnu.libvob.*;
import java.awt.*;
import org.nongnu.libvob.gl.GLCache;
import org.nongnu.libvob.gl.GLUtil;
import org.nongnu.libvob.util.ColorUtil;

/** A vob which produces a small (about 3x3 pixel) rectangle at a specified place
 * with a specified color;
 * useful for testing VobCoorders.
 * For tests, an important feature is that the size of the rectangle is
 * constant in screen coordinates; scaling does not affect it.
 * This is most likely not useful for anything else. 
 */
public class TestSpotVob extends AbstractVob {

    float x, y, z;

    Color color = Color.white;

    Vob glrend;

    /** Create a white testspot.
     */
    public TestSpotVob(float x, float y, float z) {
	this(x,y,z, Color.WHITE);
    }
    public TestSpotVob(float x, float y, float z, Color c) {
	this.x = x;
	this.y = y;
	this.z = z;
	this.color = c;
    }

    public void render(Graphics g,
				boolean fast,
				Vob.RenderInfo info1,
				Vob.RenderInfo info2) {
	g.setColor(color);
	g.fillRect(info1.box_x(x), info1.box_y(y), 3, 3);
    }

    public int putGL(VobScene vs, int coordsys1) {
	if(glrend == null) {
	    glrend = GLCache.getCallListCoorded(
"		PushAttrib CURRENT_BIT ENABLE_BIT     \n" +
"		Disable TEXTURE_2D     \n" +
"		Disable BLEND     \n" +
"		PointSize 3     \n" +
"		Color "+ColorUtil.colorGLString(color)+"\n" +
"		Begin POINTS     \n" +
"		Vertex "+x+" "+y+" "+z+" \n" +
"		End     \n" +
"		PopAttrib     \n"
	    );
	}
	vs.map.put(glrend, coordsys1);
	return 0;
    }
}
