/*
FastInt.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein
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
import javolution.realtime.*;

/** A version of Integer that Javolution can re-use.
 */
public final class FastInt extends RealtimeObject implements Comparable {

    private FastInt() {}
    private int value;

    public byte byteValue() { return (byte)value; }
    public short shortValue() { return (short)value; }
    public int intValue() { return (int)value; }
    public long longValue() { return (long)value; }
    public float floatValue() { return (float)value; }
    public double doubleValue() { return (double)value; }


    public boolean equals(Object o) {
	if(!(o instanceof FastInt)) return false;
	return ((FastInt)o).value == value;
    }

    public int hashCode() {
	return value;
    }

    public int compareTo(FastInt anotherInt) {
	// only compare; doing math might overflow
	// thanks GNU Classpath for publishing debugged sources ;-)
	if(value < anotherInt.value)
	    return -1;
	else if(value > anotherInt.value)
	    return 1;
	else
	    return 0;
    }

    public int compareTo(Object o) {
	return compareTo((FastInt)o);
    }

    public static FastInt newInstance(int value) {
	FastInt i = (FastInt)FACTORY.object();
	i.value = value;
	return i;
    }

    private static Factory FACTORY = new Factory() {
	    protected Object create() {
		return new FastInt();
	    }
	};
}
