# 
# Copyright (c) 2003, Tuomas J. Lukka
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


# NOT A MODULE - A SOURCE FILE TO BE INCLUDED INTO TEST SCRIPTS

# Assumes global name "modelClass" which contains
# the constructor of the class that implements BoundedFloatModel

def test_boundedfloatmodel_Stuff():

    model = modelClass()

    model.minimum = 3
    model.maximum = 4

    model.value = 3.5
    assert model.value == 3.5

    model.value = 4.5
    assert model.value == 4

    model.value = 2.5
    assert model.value == 3

    model.value = 3.5
    model.maximum = 3.25
    assert model.value == 3.25

    model.minimum = 3.5
    assert model.value == 3.5
    assert model.maximum == 3.5

    model.maximum = 3.25
    assert model.minimum == 3.25
    assert model.value == 3.25

    class C:
	def __init__(self):
	    self.foo = 0
	def __call__(self, *foo):
	    self.foo += 1

    c = C()

    model = modelClass( actionPerformed = c )

    model.minimum = 0
    model.maximum = 1
    model.value = .5

    assert c.foo == 1

