# 
# Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
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


from string import join
from string import split

class RegScalar:
    pass

regfloat = RegScalar()

class RegTuple:
    def __init__(self, n):
        pass

regcolor = RegTuple(3)

class RegInt:
    pass

regseed = RegInt()

class RegSub:
    pass

regsub = RegSub()

class Registry:
    def __init__(self):
        self.vars = {} 
        self.keys = [] # The access order of keys for printing

    def sub(self, name):
        return self.get(regsub, name, Registry())

    def set(self, type, name, val):
        key = (name, type)
        if not self.vars.has_key(key):
            self.keys.append(key)
        self.vars[key] = val
        return val

    def get(self, type, name, default = None):
        key = (name, type)
        if not self.vars.has_key(key):
            self.vars[key] = default
            self.keys.append(key)
            
        return self.vars[key]

    def dump(self, prefix = ""):
        for key in self.keys:
            if self.vars.has_key(key):
                print prefix + key[0], key[1], self.vars[key]
                if key[1] == regsub:
                    self.vars[key].dump(prefix + key[0] + ".")


"""

from java.util import Random

def getcolor(foo):
    rng = Random(foo.get(regseed, "seed"))
    r = foo.get(regfloat, "R", rng.nextFloat())
    g = foo.get(regfloat, "G", rng.nextFloat())
    b = foo.get(regfloat, "B", rng.nextFloat())

    return (r,g,b)


foo = Registry()

rng = Random()

bar = foo.sub("Color")
bar.get(regseed, "seed", rng.nextInt())
c = foo.get(regcolor, "Color", getcolor(bar))

foo.dump()

"""
