/*
PaperMill.java
 *    
 *    Copyright (c) 2002-2003, Tuomas J. Lukka
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
import org.nongnu.libvob.GraphicsAPI;
import org.python.util.PythonInterpreter;

public abstract class PaperMill {

    /** Get the paper corresponding to the given seed.
     */
    public abstract Paper getPaper(int seed);

    /** Get an optimized (rendered-to-texture) paper.
     * This will usually return a paper with a single
     * pass and single 
     * texture, in which case you can set the texture environment
     * (or fragment program) and add other papers.
     * <p>
     * HOWEVER, this does not work on ATI drivers; so it is not
     * guaranteed that the paper will be like that. Check it first.
     * @see org.nongnu.libvob.GL#workaroundStupidBuggyAtiDrivers
     */
    public Paper getOptimizedPaper(int seed) {
	return getPaper(seed);
    }

    static private PaperMill instance;

    static public PaperMill getInstance() {
	if(instance == null) {
	    PythonInterpreter jython = new PythonInterpreter();
	    jython.exec("from vob.paper.papermill import ThePaperMill\n" +
			"papermillInstance = ThePaperMill()\n");
	    instance = (PaperMill)(jython.get("papermillInstance",
					PaperMill.class));

	    instance = new CachingPaperMill(instance, 200);
	}
	return instance;
    }
}
