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


#foo

# print "VOB INIT"

import vob.gl
import vob.util
import vob.putil

# print "Vob init: Imported vob.putil: ",vob.putil

import org.nongnu.libvob as _libvob
import java as _java

import vob as _vob

for i in dir(_libvob):
    if _java.lang.Character.isUpperCase(i[0]):
	setattr(_vob, i, getattr(_libvob, i))

view = _libvob.view
impl = _libvob.impl
input = _libvob.input
mouse = _libvob.mouse

del _vob

