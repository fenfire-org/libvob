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


# Choosing colors and 3-dotproduct factors for papers.

from vob.color.spaces import YSTtoRGB,clampSat,LtoY,linear_to_monitor
from vob.color.spaces import RGBtoLAB,LABtoRGB,LABclamp

def toLAB(rgb):
    return RGBtoLAB(monitor_to_linear(rgb))

from math import sin,cos,atan2,pi,log,sqrt
from random import Random,shuffle

import java


dbg=0

class Colors:
    def _js(self, arg):
	return " ".join([str(a) for a in arg])
    def __init__(self, seed,
                 colors = 8,
                 minlum = 80,
                 blend = 0):
	rnd = self.rnd = java.util.Random(seed)

        huerange = rnd.nextGaussian() * 90

        # Note: This color sampling scheme only produces
        # palettes with similar colors.
        # It could be nice to have other schemes
        # with, e.g., complementary colors.

        # Add orange color to the color circle
        def getangle(f):
            # 0 = red, 120 = green, 240 = blue
            angles = [ 0, 30, 60, 120, 180, 240, 300, 360 ]
            n = len(angles) - 1
            f *= n / 360.0
            index = int(f) % n
            fract = f - int(f)
            return (1 - fract) * angles[index] + fract * angles[index + 1]

        # Sample hues uniformly from the range shifted to a random angle
        hue0 = rnd.nextDouble() * 360
        hues = ([hue0, hue0 + huerange] + 
                [hue0 + rnd.nextDouble() * huerange for i in range(2,colors)])
        hues = map(getangle, hues)
        shuffle(hues, rnd.nextDouble)

        # Take one half dark colors and one half light colors
        lumrange = 100 - minlum
        if colors == 1:
            # Use the full luminance range for solid color backgrounds
            x = rnd.nextDouble()
            # Weight lower luminances more
            x = (1 - sqrt(1-x))
            lums = [minlum + x * lumrange]
        else:
            lums = ([minlum + rnd.nextDouble() * lumrange/2
                     for i in range(0,(colors+1)/2)] +
                    [minlum + (1 + rnd.nextDouble()) * lumrange/2
                     for i in range((colors+1)/2,colors)]
                    )

        # Sample saturation:
        #  - take the most saturated color 2/3 of the time
        #    and a dull color 1/3 of the time
        sats = [(1 - (1 - (1 - rnd.nextDouble())**2) * (rnd.nextDouble() < .333))
                for i in range(0, colors)]

        # Construct colors and clamp to RGB cube keeping hue and luminance constant
        yst = [ ( LtoY(lums[i]),
                  sats[i] * cos(hues[i]*pi/180),
                  sats[i] * sin(hues[i]*pi/180) )
                for i in range(0,colors)]

        col = [linear_to_monitor(clampSat(YSTtoRGB(c))) for c in yst]
        shuffle(col, rnd.nextDouble)

        if blend > 0:
            col = [ [blend * 1 + (1 - blend) * c for c in cc] for cc in col]
                 
        
        if dbg:
            print "ANGLE=", self._AB_angle(col), "AREA=", self._AB_area(col)*100

	self.colors = [self._js(c) for c in col]
	self.colorarrs = col

	self.randvecs = [self._randvec2() for i in range(0,15)]

    def getColorStr(self, ind):
	return self.colors[ind%len(self.colors)]

    def _randvec2(self):
	#x = 2 * self.rnd.nextDouble() - 1
	#y = 2 * self.rnd.nextDouble() - 1
	#z = 2 * self.rnd.nextDouble() - 1
	x = self.rnd.nextDouble()
	y = self.rnd.nextDouble()
	z = self.rnd.nextDouble()
	return [x, y, z]

    def getNVDP3VecStr(self, ind):
	return self._js(self.randvecs[ind % len(self.randvecs)])+" 1"

    def _AB_angle(self, cols):
        #print cols
        getangle = lambda lab: 180 / pi * atan2(lab[2], lab[1])
        angles = [ getangle(toLAB(col)) for col in cols ] 
        #print angles
        angles.sort() 
        #print angles
        n = len(angles)
        maxd = 0
        for i in range(0, n):
            if i == n - 1:
                d = angles[0] + 360 - angles[i]
            else:
                d = angles[i + 1] - angles[i]
            maxd = max(d, maxd)
        return 360 - maxd

    def _AB_avg_dot(self, cols):
        print cols
        ab = [ (lab[1]/100.0,lab[2]/100.0) for lab in map(toLAB, cols) ]

        dot = lambda x,y: x[0] * y[0] + x[1] * y[1]
        dots = [ dot(x,y) for x in ab for y in ab ]

        return reduce(lambda x,y: x+y, dots) / len(dots)
    
    def _AB_area(self, cols):
        ab = [ (lab[1]/100.0,lab[2]/100.0) for lab in map(toLAB, cols) ]
        #print [ (int(100*a),int(100*b)) for (a,b) in ab ]
        ab = convex_hull(ab)
        #print [ (int(100*a),int(100*b)) for (a,b) in ab ]
        return polygon_area(ab)

def polygon_area(pts):
    A = 0
    for i in range(0, len(pts)):
        A += pts[i-1][0] * pts[i][1] - pts[i][0] * pts[i-1][1]
    return A

def convex_hull(pts):
    if len(pts) < 2: return pts
    pts.sort()
    
    #print "P=", [ (int(100*a),int(100*b)) for (a,b) in pts ]

    def dir(p,q,r):
        """Return positive if p,q,r turns cw, neg if ccw, zero if linear."""
        return (q[1]-p[1])*(r[0]-p[0]) - (q[0]-p[0])*(r[1]-p[1])


    U = pts[0:2]
    L = pts[0:2]

    for p in pts[2:]:
        while len(U) > 1 and dir(U[-2], U[-1], p) <= 0: U.pop()
        while len(L) > 1 and dir(L[-2], L[-1], p) >= 0: L.pop()
        U.append(p)
        L.append(p)

    #print "L=", [ (int(100*a),int(100*b)) for (a,b) in L ]
    #print "U=", [ (int(100*a),int(100*b)) for (a,b) in U ]

    U.reverse()
    return U + L[1:-1]

