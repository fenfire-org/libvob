# 
# Copyright (c) 2003, Janne V. Kujala
# 
# This file is part of Libvob.
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

def det(mat):
    return (+ mat[0][0] * (mat[1][1] * mat[2][2] - mat[1][2] * mat[2][1])
            - mat[0][1] * (mat[1][0] * mat[2][2] - mat[1][2] * mat[2][0])
            + mat[0][2] * (mat[1][0] * mat[2][1] - mat[1][1] * mat[2][0]))

def transpose(mat):
    return ((mat[0][0],mat[1][0],mat[2][0]),
            (mat[0][1],mat[1][1],mat[2][1]),
            (mat[0][2],mat[1][2],mat[2][2]))

def inverse(mat):
    s = 1.0 / det(mat)

    return (
        (+ (mat[1][1]*mat[2][2] - mat[2][1]*mat[1][2]) * s,
         - (mat[0][1]*mat[2][2] - mat[2][1]*mat[0][2]) * s,
         + (mat[0][1]*mat[1][2] - mat[1][1]*mat[0][2]) * s),
        (- (mat[1][0]*mat[2][2] - mat[2][0]*mat[1][2]) * s,
         + (mat[0][0]*mat[2][2] - mat[2][0]*mat[0][2]) * s,
         - (mat[0][0]*mat[1][2] - mat[1][0]*mat[0][2]) * s),
        (+ (mat[1][0]*mat[2][1] - mat[2][0]*mat[1][1]) * s,
         - (mat[0][0]*mat[2][1] - mat[2][0]*mat[0][1]) * s,
         + (mat[0][0]*mat[1][1] - mat[1][0]*mat[0][1]) * s)
        )

    
def mul(m, vec):
    return [ m * x for x in vec ]

def matvecmul(mat, v):
    return ( mat[0][0] * v[0] + mat[0][1] * v[1] + mat[0][2] * v[2],
             mat[1][0] * v[0] + mat[1][1] * v[1] + mat[1][2] * v[2],
             mat[2][0] * v[0] + mat[2][1] * v[1] + mat[2][2] * v[2] )
