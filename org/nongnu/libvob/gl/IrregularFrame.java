/*
IrregularFrame.java
 *    
 *    Copyright (c) 2002, Tuomas J. Lukka
 *    
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
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.gl;
import org.nongnu.libvob.*;
import org.nongnu.libvob.GraphicsAPI;
import org.python.util.*;
import org.python.core.*;

/** Encapsulate the python class for creating
 * irregularframe renderables.
 * <p>
 * The irregularframe renderables should be placed in 
 * two different coordinate systems, the first being
 * the coordinate system of the rectangle of which the
 * irregularframe is a cutout. The second coordinate
 * system gives the edges of the cutout as its unit square.
 */

public abstract class IrregularFrame {
    /** Return the renderable which will touch
     * the pixels that should be inside the frame.
     */
    abstract public Vob getContent();

    /** Return the renderable which will draw
     * the frame. May draw pixels inside the getContent()
     * area as well.
     */
    abstract public Vob getFrame();

    /** Get a renderable which will draw over ALL 
     * pixels drawn by the renderables returned by
     * getContent() and getFrame(), and possibly others,
     * but probably faster. Useful for clearing the stencil
     * buffer.
     */
    abstract public Vob getBlank();

    static private PyObject constr;

    static public void init() {
	if(constr == null) {
	    PythonInterpreter jython = new PythonInterpreter();
	    jython.exec("from vob.putil.effects import IrreguFrame\n");
	    constr = jython.get("IrreguFrame");
	    if(constr == null)
		throw new Error("Couldn't initialize IrregularFrame");
	}
    }


    static public IrregularFrame create(float x0, float y0, float x1, float y1, 
			    float border, float ripple) {
	return create(x0,y0,x1,y1,border,ripple,0);
    }
    static public IrregularFrame create(float x0, float y0, float x1, float y1, 
			    float border, float ripple, int type) {
	init();
	PyObject[] params = new PyObject[] {
	    new PyFloat(x0),
	    new PyFloat(y0),
	    new PyFloat(x1),
	    new PyFloat(y1),
	    new PyFloat(border),
	    new PyFloat(ripple),
	    new PyInteger(type)
	};
	PyInstance ret = ((PyInstance)constr.__call__(params));
	if(ret == null)
	    throw new Error("Couldn't create IrregularFrame");
	return (IrregularFrame)ret.__tojava__ (IrregularFrame.class);
    }
}
