# 
# Copyright (c) 2003, Janne V. Kujala
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


from Numeric import *
import util
from util import *

bg = load("txt3.png")

util.genfilt1d = util.genfilt1d_ideal

for w in exp(arange(-3, 3, .5)):

    tmp = ones(shape(bg)) * w

    a = fft_blur(bg, w, (0, 1))
    a = fft_blur(a, w, (0, 1))

    b = fft_blur(bg, w * sqrt(2), (0,1))

    d = flat(a - b)

    print "SD = ", w, "error range:", min(d), "-", max(d)

