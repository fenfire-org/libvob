/*   
Model.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein
 *
 *    This file is part of Libvob.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *
 */
package org.nongnu.libvob.layout;
import org.nongnu.navidoc.util.*;
import java.util.*;

public interface Model extends Observable, Replaceable {

    Object get();
    void set(Object value);

    int getInt();
    void setInt(int value);

    float getFloat();
    void setFloat(float value);

    boolean getBool();
    void setBool(boolean value);

    Model equalsModel(Model other);
    Model equalsObject(Object other);
    Model equalsInt(int other);
    Model equalsFloat(float other);
    Model equalsBool(boolean other);

    Model plus(Model m);
    Model plus(float f);

    Model minus(Model m);
    Model minus(float f);

    Model times(Model m);
    Model times(float f);

    Model divide(Model m);
    Model divide(float f);

    Model divide(Model m, float ifZero);
    Model divide(float f, float ifZero);

    Model not();

    Model select(Model ifTrue, Model ifFalse);


    class Change extends AbstractAction {
	protected Model m, to;
	public Change(Model m, Model to) {
	    this.m = m; this.to = to;
	}
	public void run() {
	    m.set(to.get());
	}

	protected Replaceable[] getParams() {
	    return new Replaceable[] { m, to };
	}
	protected Object clone(Object[] params) {
	    return new Change((Model)params[0], (Model)params[1]);
	}
    }
}
