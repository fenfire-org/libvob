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

def safe_exp(x):
    if x < -745: return 0
    return exp(x)

def genfilt1d(n, w):
    if w == 0:
        return array([1] + [0] * (n-1))
    
    f = [ safe_exp(-i**2 / (2.0 * w**2)) for i in range(0, n) ]
    f = array(f)
    f /= sum(f[::-1])

    return f


print genfilt1d(100, .01)


for w in arange(0,10,.1):
    print w,

    n = 1000

    f = genfilt1d(n, w)

    g = cumsum(f[::-1])

    for i in range(0, n):
        if g[i] >= 1E-4:
            print n - i, 1-cumsum(f)[int(ceil(3*w))]
            break

    
    
