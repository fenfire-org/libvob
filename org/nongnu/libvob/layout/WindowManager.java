/*
WindowManager.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.layout;
import org.nongnu.libvob.layout.component.Window;
import java.util.*;

/** A provisional interface for something that dialog boxes can register with
 *  in order to be drawn on the screen. This is just a first cut at the
 *  interface and will most probably change in the future.
 */
public interface WindowManager {
    
    void add(Lob window, String title);
    void remove(Lob window);


    Object
	TITLE = "http://fenfire.org/rdf-v/2004/10/20/Manager#Title",
	CONTENT = "http://fenfire.org/rdf-v/2004/10/20/Manager#Content",
	X = "http://fenfire.org/rdf-v/2004/10/20/Manager#X",
	Y = "http://fenfire.org/rdf-v/2004/10/20/Manager#Y",
	W = "http://fenfire.org/rdf-v/2004/10/20/Manager#W",
	H = "http://fenfire.org/rdf-v/2004/10/20/Manager#H",
	ZOOM = "http://fenfire.org/rdf-v/2004/10/20/Manager#Zoom",
	VIRTUAL_DESK = "http://fenfire.org/rdf-v/2004/10/20/Manager#VirtualDesk";


    class SimpleWindowManager extends LobLob implements WindowManager {
	protected Lob background;
	protected List windows;
	protected Map titles;
	protected SequenceModel.SimpleModel model;

	public SimpleWindowManager(Lob background) {
	    this.background = background;
	    this.windows = new ArrayList();
	    this.titles = new HashMap();
	    this.model = new SequenceModel.SimpleModel();
	    
	    updateModel();

	    setDelegate(new Tray(model, true));
	}

	public void add(Lob window, String title) {
	    windows.add(window);
	    titles.put(window, title);
	    updateModel();
	}

	public void remove(Lob window) {
	    windows.remove(window);
	    updateModel();
	}

	protected void updateModel() {
	    model.startUpdate();

	    try {
		model.add(background);
		for(Iterator i=windows.iterator(); i.hasNext();) {
		    Lob window = (Lob)i.next();
		    String title = (String)titles.get(window);
		    Lob decor = new Window(window, title);
		    model.add(new AlignLob(decor, .5f, .5f, .5f, .5f),
			      window);
		}
	    } finally {
		model.endUpdate();
	    }
	}
    }
}
