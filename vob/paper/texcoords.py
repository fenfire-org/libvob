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


# Texture coordinates
from __future__ import nested_scopes
import math
from math import sqrt

def smallIntegerG0(rnd):
    """Return a small integer greater than 0.
    """
    return abs(int(0.5*rnd.nextGaussian()))+1

def smallInteger(rnd):
    """Return a small integer.
    """
    return abs(int(1.5*rnd.nextGaussian()))

class TexGenData:
    def __init__(self, vec):
	self.vec = vec
    def getVec(self):
	return self.vec
    def createCorrelated_xyspace(self, rnd):
	"""Create a TexGenData correlated to this so that
	the repetitive unit in the (x,y) coordinates is similar;
	the (s,t,r) coordinates will not be similar.
	
	This is achieved by taking small-rational linear combinations
	of the vectors and adding random offsets.
	
	This operation is useful for creating new shapes, especially
	when 3D textures are not supported."""

	# Initialize with just shift.
	nv = [0, 0, 0, rnd.nextDouble(),
	      0, 0, 0, rnd.nextDouble(),
	      0, 0, 0, rnd.nextDouble()]

	# Add a random rational of each.
	for vold in range(0,3):
	    for vnew in range(0,3):
		numer = smallIntegerG0(rnd)
		denom = smallIntegerG0(rnd)
		if rnd.nextBoolean():
		    sign = 1
		else:
		    sign = -1
		for i in range(0,3):
		    nv[4*vnew + i] += sign * numer * self.vec[4*vold + i] / denom

	# return the new texgendata
	return TexGenData(nv)

    def createCorrelated_strspace(self,rnd):
	"""Create a TexGenData correlated to this in every way:
	this represents a slight shift in (s,t,r) coordinates.
	"""

class TexGenXYRepeatUnit:
    """ A class representing a parallelogram repeating unit in (x,y)
    coordinates.

    The size of the repeating unit is designed to be suitable
    for 1 to be the height of an A4 paper.
    """
    def __init__(self, rnd=None, 
	    vecs = None,
	    scale = .3,
	    scale_log_stddev = 0.4,
	    angle_stddev = .065,
	    lendiff_mean = 0,
	    lendiff_stddev = .1):

	if vecs != None:
	    self.vecs = vecs
	    return

	# The angle between the basis vectors
	angle = (.25 + angle_stddev*rnd.nextGaussian()) * 2 * math.pi
	angle *= 1 - 2 * rnd.nextBoolean()

	# The angle of the first basis vector
	as = rnd.nextDouble() * 2 * math.pi
	# And the angle of the second basis vector
	at = as + angle

	# Logarightm of the random scale factor
        m0 = scale_log_stddev * rnd.nextGaussian()

	# The difference between basis vector lengths
	m = lendiff_mean + lendiff_stddev * rnd.nextGaussian()

	# The basis vector lengths
	rs = scale * math.exp(m0 + m)
	rt = scale * math.exp(m0 - m)

	# The vectors that give x and y when dotted with (s, t, 0, 1)
	self.vecs = [[ rs * math.cos(as), rt * math.cos(at)],
                     [ rs * math.sin(as), rt * math.sin(at)]]

    def _getSTVectors(self, rnd=None):
	"""Get the 2 4-component vectors that (x,y,0,1) should
	be multiplied by to get (s,t).
	"""
	mat = self.vecs
	# 1 / determinant
	f = 1.0 / (mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0])

	# Start return value
	r = [[ f * mat[1][1], -f * mat[0][1], 0, "?"],
	     [ -f * mat[1][0], f * mat[0][0], 0, "?"]]

	if rnd != None:
	    # Random offsets
	    r[0][3] = rnd.nextDouble()
	    r[1][3] = rnd.nextDouble()

	return r

    def texCoords2D(self, rnd):
	"""Get an appropriate texgen vector to use for 2D texture coordinates.
	The randomness is used only for the shift inside the (s,t) coordinate
	system.
	"""
	stv = self._getSTVectors(rnd)
	return TexGenData([
	    stv[0][0],
	    stv[0][1],
	    stv[0][2],
	    stv[0][3],
	    stv[1][0],
	    stv[1][1],
	    stv[1][2],
	    stv[1][3],
	    0, 0, 0, 0
	    ])



    def getRelated(self, rnd):
	"""Create another TexGenXYRepeatUnit so that that unit
	also repeats with this unit but may repeat more often
	or be skewed.

`	The equation between the vectors is:
	a vn1 + b vn2 = vo1
	c vn1 + d vn2 = vo2
	Where a,b,c,d are integers.
	"""

	def chooseInts(rnd):
            while 1:
		a,b,c,d = [int(2*rnd.nextGaussian()**3) for i in range(0,4)]
                l1 = sqrt(a*a + b*b)
                l2 = sqrt(c*c + d*d)
                det = a*d-b*c
                if det == 0: continue
                if l1 * l2 / det > 2: continue
                if abs(math.log(l1 / l2)) > .7: continue
                break
            
	    return (a,b,c,d)
        
	a,b,c,d = chooseInts(rnd)
	# 1 / determinant
	f = 1.0 / (a * d - b * c)
        vecs = [ [ f * d * self.vecs[0][0] - f * b * self.vecs[0][1], -f * c * self.vecs[0][0] + f * a * self.vecs[0][1] ],
                 [ f * d * self.vecs[1][0] - f * b * self.vecs[1][1], -f * c * self.vecs[1][0] + f * a * self.vecs[1][1] ] ]

        # Note that the 'vecs' matrix stores the parallelogram side vectors as its columns
        # The debug prints below show that 'vecs' correctly solves the equation
        if 0:
            print "S: ", [self.vecs[0][0], self.vecs[1][0]]
            print "T: ", [self.vecs[0][1], self.vecs[1][1]]
            print "S': ", [vecs[0][0], vecs[1][0]]
            print "T': ", [vecs[0][1], vecs[1][1]]
            print "a S' + b T':", [a*vecs[0][0]+b*vecs[0][1], a*vecs[1][0]+b*vecs[1][1]]
            print "c S' + d T':", [c*vecs[0][0]+d*vecs[0][1], c*vecs[1][0]+d*vecs[1][1]]
            print "a,b,c,d:", a,b,c,d

	return TexGenXYRepeatUnit(vecs=vecs)

class TexGen3DSlice:
    def __init__(self, rnd):
	# (s,t,r) of origin
	self.origin = [rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()]

	def chooseInts(rnd):
	    a,b,c,d,e,f = [int(rnd.nextGaussian()) for i in range(0,6)]
	    if a*e-b*d == 0 or a*f-c*e == 0: return chooseInts(rnd)
	    return (a,b,c,d,e,f)

	ints = chooseInts(rnd)
	self.vecs = [
	    [a,b,c],
	    [d,e,f]
	]
    def mapTexGenData(self, td):
	"""Given a TexGenData object, return a new one which maps to
	this slice instead of the 2D (s0,t0) coordinate system.
	"""
	v = [
	    td.vec[0][0]*self.vecs[0][0] + td.vec[1][0]*self.vecs[1][0],
	    td.vec[0][1]*self.vecs[0][0] + td.vec[1][1]*self.vecs[1][0],
	    td.vec[0][2]*self.vecs[0][0] + td.vec[1][2]*self.vecs[1][0],
	    td.vec[0][3]*self.vecs[0][0] + td.vec[1][3]*self.vecs[1][0]
		    + self.origin[0],
	    td.vec[0][0]*self.vecs[0][1] + td.vec[1][0]*self.vecs[1][1],
	    td.vec[0][1]*self.vecs[0][1] + td.vec[1][1]*self.vecs[1][1],
	    td.vec[0][2]*self.vecs[0][1] + td.vec[1][2]*self.vecs[1][1],
	    td.vec[0][3]*self.vecs[0][1] + td.vec[1][3]*self.vecs[1][1]
		    + self.origin[1],
	    td.vec[0][0]*self.vecs[0][2] + td.vec[1][0]*self.vecs[1][2],
	    td.vec[0][1]*self.vecs[0][2] + td.vec[1][1]*self.vecs[1][2],
	    td.vec[0][2]*self.vecs[0][2] + td.vec[1][2]*self.vecs[1][2],
	    td.vec[0][3]*self.vecs[0][2] + td.vec[1][3]*self.vecs[1][2]
		    + self.origin[2],
	]
	return TexGenData(v)


class TexCoords:

    def texCoords2D(self, rnd):
	"""Get the data to pass to texgen for a single, random
	2D texture coordinate system.
	"""




	vectors = self._invert2Dsimple(self._create2DXYVectors(rnd))
	return TexGenData([
	    vectors[0][0],
	    vectors[0][1],
	    vectors[0][2],
	    vectors[0][3],
	    vectors[1][0],
	    vectors[1][1],
	    vectors[1][2],
	    vectors[1][3],
	    0, 0, 0, 0
	    ])

    def _create3DPlane(self, rnd):
	"""Get a suitable vector for a plane inside a 3D texture.

	Returns a 4-component vector; the equation is
	v^T \cdot (s3,t3,r3,1) = 0
	"""

	# Generate 3 integers: we want to use integer coefficients
	# to obtain repetition soon to avoid spending all of the
	# texture always.
	stddev = 2.5
	i = int(stddev * rnd.nextGaussian())
	j = int(stddev * rnd.nextGaussian())
	k = int(stddev * rnd.nextGaussian())
	if i == j == k == 0: return self._create3DPlane(rnd)

	# The shift can be anything.
	shift = 10 * rnd.nextDouble()

	return (i, j, k, shift)

    def _3DPlaneOrthoVecs(self, v):
	"""From a 4-vector (a,b,c,d) describing a plane, create two orthonormal
	3-vectors plus one 3-offset vector which mean 
	(s3,t3,r3) = v_1*s2 + v_2*t2 + v_3 where
	s3,t3,r3 etc. are the 3-dimensional texture coordinates,
	s2,t2 are the virtual 2-dimensional texture coordinates.

	This method will loop infinitely if all of (a,b,c) are zero.
	"""
	# First, the offset vector. To make things easy, we'll always offset 
	# along one of the coordinate axes. Here we also get to check
	# whether all components are zero.
	if v[0] != 0:
	    vec3 = (- v[3] / v[0], 0, 0)
	elif v[1] != 0:
	    vec3 = (0, - v[3] / v[1], 0)
	elif v[2] != 0:
	    vec3 = (0, 0, - v[3] / v[2])
	else:
	    assert 0, "Vec was all zero!"

	# Create vec1. Two alternatives: if the plane is the xy plane, just
	# grab the first vector, else project v to xy plane and rotate 90deg.
	if v[1] == 0 and v[0] == 0:
	    vec1 = (1,0,0)
	else:
	    l = math.sqrt( v[0]**2 + v[1]**2 )
	    vec1 = (v[1]/l,-v[0]/l,0)

	# Then, vec2 is simply the cross product between v and vec1
	vec2_0 = (
	    v[1] * vec1[2] - v[2] * vec1[1],
	    v[2] * vec1[0] - v[0] * vec1[2],
	    v[0] * vec1[1] - v[1] * vec1[0],
	)
	l = math.sqrt( vec2_0[0]**2 + vec2_0[1]**2 + vec2_0[2]**2 )
	vec2 = [el / l for el in vec2_0]

	return (vec1, vec2, vec3)

    def texCoords3D(self, rnd):
	"""Get the data to pass to texgen for a single, random
	2D slice of a 3D texture coordinate system.
	"""
	# Get the transformation from (x,y,z) to 
	# the virtual 2D texture coordinates (s2, t2)
	vectors = self._invert2Dsimple(self._create2DXYVectors(rnd))

	# Then, create the transformation from (s2, t3)
	# to (s3, t3, r3)
	planevec =  self._3DPlaneOrthoVecs(self._create3DPlane(rnd))

	# And finally, concatenate the two by matrix
	# multiplication
	vecs = [
	    [
	     vectors[0][i4] * planevec[0][i3] +
	     vectors[1][i4] * planevec[1][i3] 
		for i4 in range(0,4)
	    ]
	    for i3 in range(0,3)
	]
	# And add the offsets
	for i in range(0,3):
	    vecs[i][3] += planevec[2][i]
	return TexGenData([
	    vecs[0][0],
	    vecs[0][1],
	    vecs[0][2],
	    vecs[0][3],
	    vecs[1][0],
	    vecs[1][1],
	    vecs[1][2],
	    vecs[1][3],
	    vecs[2][0],
	    vecs[2][1],
	    vecs[2][2],
	    vecs[2][3],
	    ])
