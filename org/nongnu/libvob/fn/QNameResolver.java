/*
QNameResolver.java
 *    
 *    Copyright (c) 2005 Benja Fallenstein
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
package org.nongnu.libvob.fn;
import java.util.*;

/** Static method for resolving qnames used in Java to URIs.
 *  A qname, coming from XML Namespaces, is something like "xhtml:em",
 *  where the prefix -- "xhtml" -- is associated with a namespace URI.
 *  In the Semantic Web community, qnames are used to denote URIs,
 *  constructed of the concatenation of the namespace URI
 *  with the part after the ":" in the qname (the "local name").
 *  <p>
 *  For example, if the prefix "ex" is associated with the namespace URI
 *  "http://example.org/", then the qname "ex:foo" is short for the URI
 *  "http://example.org/foo".
 *  <p>
 *  As URIs are used as keys in Libvob and as parameter names
 *  in the functional system (among other things), this class provides
 *  a simple functionality for interpreting QNames as URIs inside Java.
 *  A class using this normally looks something like,
 *  <pre>
 *      public class Foo {
 *          private static String u(String qname) { return QNameResolver.resolve(Foo.class, qname); }
 *
 *          public static final String[] NAMESPACES = {
 *              "ex",  "http://example.org/",
 *              "foo", "http://example.net/foo/",
 *          };
 *
 *          // ...
 *      }
 *  </pre>
 *  QNameResolver.resolve() will first look in 'class.NAMESPACES', where
 *  'class' is the class it is passed, and, if the prefix isn't found there,
 *  in PackageNamespaces.NAMESPACES, where PackageNamespaces is in
 *  the same package as 'class' (XXX this isn't implemented yet), and then
 *  in QNameResolver.NAMESPACES (XXX is this good?).
 */
public class QNameResolver {

    public static final String[] NAMESPACES = {
	"fn",  "http://fenfire.org/2005/03/functional/",
	"lob", "http://fenfire.org/2005/03/layout/",
    };

    private static final Map namespaceCache = new HashMap();
    private static final Map uriCache = new HashMap();

    public static String resolve(Class klass, String qname) {
	Map classUriCache = (Map)uriCache.get(klass);
	if(classUriCache == null) {
	    classUriCache = new HashMap();
	    uriCache.put(klass, classUriCache);
	}

	String uri = (String)classUriCache.get(qname);
	if(uri == null) {
	    int colon = uri.indexOf(':');
	    if(colon < 0) throw new IllegalArgumentException("qname doesn't contain colon: '"+qname+"'");

	    String prefix = uri.substring(0, colon);
	    String localName = uri.substring(colon+1);

	    Map m = (Map)namespaceCache.get(klass);
	    if(m == null) {
		m = new HashMap();
		
		for(int i=0; i<NAMESPACES.length; i++,i++)
		    m.put(NAMESPACES[i], NAMESPACES[i+1]);

		try {
		    String[] namespaces = 
			(String[])klass.getDeclaredField("NAMESPACES").get(null);

		    for(int i=0; i<namespaces.length; i++,i++)
			m.put(namespaces[i], namespaces[i+1]);
		} catch(NoSuchFieldException e) {
		    // no NAMESPACES field -- no problem, we only search
		    // the namespaces in QNameResolver (and in the future,
		    // in PackageNamespaces)
		} catch(IllegalAccessException e) {
		    // the field is private or something -- this is probably
		    // a bug on the part of the programmer
		    throw new Error(e);
		}

		namespaceCache.put(klass, m);
	    }

	    String namespaceURI = (String)m.get(prefix);
	    
	    if(namespaceURI == null) throw new IllegalArgumentException("Namespace prefix "+prefix+" in qname "+qname+" not declared in class "+klass);

	    uri = namespaceURI + localName;

	    classUriCache.put(qname, uri);
	}
	return uri;
    }
}
