#
# Copyright (c) 2002, Tuomas Lukka and Benja Fallenstein
#
# This file is part of Gzz.
# 
# Gzz is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Gzz is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Gzz; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
#

"""Tools for Jython testing."""


from java.util import *

def mklist(collection):
    """Turn a Java collection into a Jython list."""

    list = []
    i = collection.iterator()
    while i.hasNext():
        list.append(i.next())

    return list

def assertSetEquals(set, list):
    l1 = list
    l2 = mklist(set)

    for x in l1: assert x in l2
    for x in l2: assert x in l1


def set(list):
    """Turn a Jython list into a Java set."""


def mkmap(dict):
    """Turn a Jython dictionary into a Java String -> String Map."""

    map = HashMap()

    for (k,v) in dict.items():
        # Jython is supposed to convert Jython strings to Java Strings
        # automatically when a Java method expects an object:

        map.put(str(k), str(v))

    return map

