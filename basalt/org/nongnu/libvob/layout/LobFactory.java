// (c): Benja Fallenstain and Matti J. Katila

package org.nongnu.libvob.layout;
import org.nongnu.libvob.layout.component.*;
import java.util.*;
import java.lang.reflect.*;

public class LobFactory {
    static private void p(String s) { System.out.println("LobFactory:: "+s); }

    Class lobs[] = {
	Label.class,
	Box.class,
	Button.class
    };

    Map uri2class = new HashMap();
    Map uri2params = new HashMap();

    public LobFactory() {
	for (int i=0; i<lobs.length; i++) {
	    try {
		Object uri = lobs[i].getDeclaredField("URI").get(null);
		uri2class.put(uri, lobs[i]);
		uri2params.put(uri, lobs[i].getField("PARAMS").get(null));
	    } catch(NoSuchFieldException e) {
		String err = 
		    "There be squirrels. No lucky. " +
		    "(I.e., one of the classes listed in " +
		    "LobFactory.lobs, " + lobs[i] + ", doesn't have " +
		    "a URI nand/nor a PARAMS field, as it " +
		    "should have. That's a bug in that class.)";
		throw new Error(err);
	    } catch(IllegalAccessException e) {
		String err = 
		    "There be squirrels. No lucky. " +
		    "(I.e., the URI and/or PARAMS field in one " +
		    "of the classes listed in LobFactory.lobs, " + 
		    lobs[i] + ", isn't public. That's a bug " +
		    "in that class.)";
		throw new Error(err);
	    }
	}
	p(""+uri2class);
	p(""+uri2params);

    }

    public Replaceable get(String uri, Map paramMap) {
	Class c = (Class)uri2class.get(uri);
	Object[] paramKeys = (Object[])uri2params.get(uri);

	if(paramKeys == null)
	    throw new NoSuchElementException("Replaceable object "+
					     "with URI "+uri);

	Object[] params = new Object[paramKeys.length];
	Class[] paramTypes = new Class[paramKeys.length];

	for(int i=0; i<params.length; i++) {
	    params[i] = paramMap.get(paramKeys[i]);
	    if(params[i] == null) {
		throw new IllegalArgumentException("Argument missing " +
						   "from params map: " +
						   paramKeys[i]);
	    }
	    paramTypes[i] = params[i].getClass();
	}

	try {
	    Constructor con = c.getConstructor(paramTypes);
	    return (Replaceable)con.newInstance(params);
	} catch(Exception e) {
	    throw new Error(e);
	}
    }

    public static void main(String[] dontdothat) {
	LobFactory f = new LobFactory();
	p("New lob: "+f.get("XXX_Box",
			    Collections.singletonMap(Box.AXIS,Lob.X)));
    }
}
