/*
Demo.java
 *    
 *    Copyright (c) 2003, Benja Fallenstein
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lava;
import org.nongnu.libvob.util.ScalableFont;
import java.awt.*;

/** A demo of the capabilities of the
 *  structured graphics vob impl.
 */
public class Demo {

    public static void main(String[] args) {
	Group g = new Group();
	int v = g.add(new Translate(new RectVob(70, 40), 10, 30));
	int w = g.add(new Translate(new RectVob(44, 15), 25, 80));
	RenderTraversal s = g.transform(new RenderTraversal(), v+1);
	RenderTraversal t = g.transform(new RenderTraversal(), w+1);
	g.add(new Colorize(new LineVob(s, t), Color.red));

	Sequence seq = new SimpleBreaker(new Box(Vob.Y),
					 300, Vob.X);

	ScalableFont font = new ScalableFont("serif", 0, 12);
	String string = "A little experiment 'ad hominem.' "
	    + "(What 'ad hominem' means? I wouldn't know. "
	    + "I just use it to sound impressive. This is "
	    + "really about linebreaking :-) ) -- UPDATE: "
	    + "it means using an attack against a person "
	    + "as an 'argument' against what the person said. "
	    + "The word I knew for this so far is, "
	    + "'argumentum ad personam.'";

	for(int i=0; i<string.length(); i++) {
	    if((i+1) % 30 == 0)
		seq.add(new RectVob(20, 12));
	    seq.add(new TextVob(string.substring(i, i+1), font));
	}

	g.add(new Translate(new Colorize(seq.close(), Color.blue),
			    300, 70));
	
	Vob rect = new RectVob(300, (int)seq.getSize(Vob.Y));
	g.add(new Translate(new Colorize(rect, Color.red), 
			    300, 70));

			    

	Vob vob = new Colorize(g.close(), Color.blue);
	
	Frame f = new Frame();
	f.add(new VobCanvas(vob, new Color(255, 128, 155)));
	f.setVisible(true);
    }
}
