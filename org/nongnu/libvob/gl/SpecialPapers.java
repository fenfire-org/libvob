/*
SpecialPapers.java
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

package org.nongnu.libvob.gl;
import java.awt.Color;
import org.nongnu.libvob.util.ColorUtil;

/** A class which provides some special types of papers for various
 * purposes.
 * XXX TODO: Graph paper, coordinate paper, ...
 */
public class SpecialPapers {
    static public Paper solidPaper(Color color) {
	Paper p = new Paper();
	p.setNPasses(1);
	Paper.Pass p0 = p.getPass(0);
	p0.setSetupcode(
		"PushAttrib CURRENT_BIT COLOR_BUFFER_BIT ENABLE_BIT\n"+
		"Disable BLEND\n"+
		"Disable TEXTURE_2D\n"+
		"Color "+ColorUtil.colorGLString(color)+"\n"
		);
	p0.setTeardowncode(
		"PopAttrib\n"
	);
	return p;
    }

    static private GL.Texture graphtex;
    /** A colored graph paper.
     */
    static public Paper cgraphPaper(float unitSize) {
	if(graphtex == null) {
	    graphtex = GL.createTexture();
	    graphtex.shade_all_levels(256,256,0,3,
		    "R3_G3_B2", "RGB", 
		    "coordinates", new String[] {
			"type", "0",
			"ticks", "4", 
			"thickness", "2"
		    });
	}
	Paper p = new Paper();
	p.setNPasses(1);
	Paper.Pass p0 = p.getPass(0);
	p0.setSetupcode(
	    "PushAttrib CURRENT_BIT COLOR_BUFFER_BIT TEXTURE_BIT ENABLE_BIT\n"+
	    "Disable BLEND\n"+
	    "Color 1 1 1\n"+
	    "Enable TEXTURE_2D\n"+
	    "BindTexture TEXTURE_2D "+graphtex.getTexId()+"\n"+
	    "TexParameter TEXTURE_2D TEXTURE_WRAP_S CLAMP\n"+
	    "TexParameter TEXTURE_2D TEXTURE_WRAP_T CLAMP\n"
	);
	p0.setTeardowncode(
		"PopAttrib\n"
	);
	p0.setNTexGens(1);
	p0.putNormalTexGen(0, new float[] {
	    1.0f/unitSize, 0, 0, 0,
	    0, 1.0f/unitSize, 0, 0,
	    0, 0, 1, 0,
	    0, 0, 0, 1
	});
	return p;
    }

    static public GLRen.FixedPaperQuad selectionFixedPaperQuad() {
	return selectionFixedPaperQuad(null);
    }
    static public GLRen.FixedPaperQuad selectionFixedPaperQuad(Color color) {
	return GLRen.createFixedPaperQuad(selectionPaper(color), 0, 0, 1, 1, 0, 1, 1, 10, null, 1);
    }

    static public Paper selectionPaper() {
	return selectionPaper(null);
    }
    /** Create a paper that is able to show a "selection".
     */
    static public Paper selectionPaper(Color color) {
	if(color == null) color = new Color(.6f, .5f, .7f);
	Paper selectPaper = new Paper();
	selectPaper.setNPasses(1);
	Paper.Pass pas = selectPaper.getPass(0);
	pas.setSetupcode(
	    "PushAttrib CURRENT_BIT ENABLE_BIT COLOR_BUFFER_BIT\n"+
	    "Disable TEXTURE_2D\n"+
	    "Disable DEPTH_TEST\n"+
	    "Color "+ColorUtil.colorGLString(color)+"\n"+
	    "Enable BLEND\n"+
	    "BlendFunc ONE  ONE_MINUS_SRC_COLOR\n"
	);
	pas.setTeardowncode(" PopAttrib\n");
	return selectPaper;
    }

}
