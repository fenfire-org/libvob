# 
# Copyright (c) 2003, Tuomas J. Lukka
# 
# This file is part of Libvob.
# 
# Libvob is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Libvob is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Libvob; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 

"""Read standard libvob debugging commandline options + debug utils.

The standard debug options are:

    -d class / --dbg class

	Turn on debugging for the given Java class
	by setting its member
	    
	    public static boolean dbg = false;

	to true.

	The actual debugging output then depends on the printing
	statements in the class.

    -G glvar / --gldbg glvar

	Turn on the OpenGL debug variable with the given variable name.
	These are declared in the C++ code using the declarations
	in Debug.hxx

    -D property=value
	
	Set the given Java system property to the given value.
"""

# Jython:
# Provide variables for and read commandline debug options.

# python imports
import re

# java imports 
from java.lang import System

# gzz imports
import org.nongnu.libvob as vob
from org.nongnu.libvob.gl import GL, GLRen

debugger = vob.util.Dbg()

short = "d:G:D:"
long = ["--dbg=", "--gldbg="]

all = ["-d", "-G", "-D"] + long


def option(o,a):
    if o in ("-d", "--dbg"):
        debugger.debugClass(a, 1)
    elif o in ("-G", "--gldbg"):
        GL.loadLib()
        print "Setting GL debug ",a
        GL.setDebugVar(a, 1)
    elif o in ("-D",):
        m = re.match('^(.*)=(.*)$', a)
        assert m
	prop = System.getProperties()
        prop.setProperty(m.group(1), m.group(2))
	System.setProperties(prop)

import jarray



def showCS(vs, cs):
    """Print data about the given coordinate system.
    """
    arr = jarray.array([0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0], 'f')
    vs.coords.transformPoints3(cs, arr, arr)

    print "(0,0,0) -> (%f,%f,%f)" % (arr[0], arr[1], arr[2])
    print "(1,0,0) -> (%f,%f,%f)" % (arr[3], arr[4], arr[5])
    print "(0,1,0) -> (%f,%f,%f)" % (arr[6], arr[7], arr[8])
    print "(1,1,0) -> (%f,%f,%f)" % (arr[9], arr[10], arr[11])

    vs.coords.getSqSize(cs, arr)

    print "Unit box: (%s, %s)" % (arr[0], arr[1])


