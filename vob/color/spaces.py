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


# Some color space conversions

from __future__ import nested_scopes

import math

from matrix import *

# CIE1931 x,y-chromaticity coordinates of the
# red, green and blue phosphors of the monitor
# used in RGB <-> XYZ conversions

R = .64,.33 # these are the HDTV/sRGB/EBU/ITU primaries
G = .30,.60
B = .15,.06

# Chromaticity coordinates of the white point (i.e., R = G = B = 1) --
# this determines the relative luminances of the primaries
# W = .312713,.329016 # = D65 standard illuminant
W = .3127268660,.3290235126 # = D65 standard illuminant [CIE 15.2, p.55]

# The gamma and offset used for converting linear RGB to monitor RGB
gamma = 2.4 # sRGB standard
offset = .055 

# The uncorrected display gamma of PC's is typically 2.2, i.e.,
# RGB values map to physical intensities with an exponent of 2.2.
# So, gamma correction of 2.2 (or 2.4 with .055 offset)
# here should result in linear intensity.

def linear_to_monitor(rgb):
    def f(x):
        if x < 0: return -f(-x)
        if offset == 0: return x**(1.0/gamma)

        # Use a linear segment near zero
        t = offset / (gamma - 1)
        t2 = (t * gamma / (1 + offset))**gamma
        c = t / (t * gamma / (1 + offset))**gamma
        if x < t2:
            return x * c
        
        return x**(1.0/gamma) * (1 + offset) - offset

    return (f(rgb[0]), f(rgb[1]), f(rgb[2]))

def monitor_to_linear(rgb):
    def f(x):
        if x < 0: return -f(-x)
        if offset == 0: return x**gamma
        
        # Use a linear segment near zero
        t = offset / (gamma - 1)
        c = t / (t * gamma / (1 + offset))**gamma 
        if x < t:
            return x / c
        
        return ((x + offset) / (1 + offset))**gamma
    
    return (f(rgb[0]), f(rgb[1]), f(rgb[2]))


def normalize(col):
    sum = 0.0
    for c in col:
        sum += c
    return [ c / sum for c in col ]
        

def D(T):
    """Return x,y coordinates of the CIE D illuminant specified by T

    4000 <= T <= 25000 is the correlated color temperature
    """
    if T <= 7000:
        x = -4.6070E9 * T**-3. + 2.9678E6 * T**-2. + 0.09911E3 * T**-1. + 0.244063
    else:
        x = -2.0064E9 * T**-3. + 1.9018E6 * T**-2. + 0.24748E3 * T**-1. + 0.237040
    y = -3.000 * x**2. + 2.870 * x - 0.275

    return x,y

def init():
    global R,G,B,W,Wr,Wg,Wb,RGBtoXYZmat,XYZtoRGBmat
    # Compute z-coordinates
    R = (R[0], R[1], 1 - R[0] - R[1])
    G = (G[0], G[1], 1 - G[0] - G[1])
    B = (B[0], B[1], 1 - B[0] - B[1])
    W = (W[0], W[1], 1 - W[0] - W[1])

    # Compute luminance weights for the primaries using the white point
    Wr = (R[1] * det([W,G,B])) / (W[1] * det([R,G,B]))
    Wg = (G[1] * det([R,W,B])) / (W[1] * det([R,G,B]))
    Wb = (B[1] * det([R,G,W])) / (W[1] * det([R,G,B]))

    RGBtoXYZmat = transpose((mul(Wr / R[1], R),
                             mul(Wg / G[1], G),
                             mul(Wb / B[1], B)))

    XYZtoRGBmat = inverse(RGBtoXYZmat)

init()

def set_temp(T):
    global W
    W = D(T)
    init()

#print RGBtoXYZmat[0]
#print RGBtoXYZmat[1]
#print RGBtoXYZmat[2]
#print
#print XYZtoRGBmat[0]
#print XYZtoRGBmat[1]
#print XYZtoRGBmat[2]

# RGB <-> CIE XYZ conversions

def RGBtoXYZ(v):
    #mat = [[ 0.412453, 0.35758 , 0.180423],
    #       [ 0.212671, 0.71516 , 0.072169],
    #       [ 0.019334, 0.119193, 0.950227]];

    return matvecmul(RGBtoXYZmat, v)

def XYZtoRGB(v):
    #mat =  [[ 3.240479,-1.53715 ,-0.498535],
    #        [-0.969256, 1.875991, 0.041556],
    #        [ 0.055648,-0.204043, 1.057311]];

    return matvecmul(XYZtoRGBmat, v)

XYZtoLMSmat = [[ 0.15514, 0.54312, -.03286 ],
               [ -.15514, 0.45684, 0.03286 ],
               [       0,       0, 0.01608 ]]
#               [       0,       0, 0.00801 ]]
LMStoXYZmat = inverse(XYZtoLMSmat)

def XYZtoLMS(v):
    return matvecmul(XYZtoLMSmat, v)

def LMStoXYZ(v):
    return matvecmul(LMStoXYZmat, v)


# CIE L*a*b* conversions

def f(X):
    """The perceptual nonlinearity used in the CIE L*a*b* color space"""
    if X <= (216./24389):
        return (841./108) * X + (16./116)
    else:
        return X**(1./3)
    
def inv_f(X):
    """Inverse of the the perceptual nonlinearity"""
    if X <= (6./29):
        return (X - 16./116) * (108./841)
    else:
        return X**3


def RGBtoLAB(RGB):
    XYZ = RGBtoXYZ(RGB)
    XYZn = RGBtoXYZ([1,1,1])

    X3 = f(XYZ[0] / XYZn[0])
    Y3 = f(XYZ[1] / XYZn[1])
    Z3 = f(XYZ[2] / XYZn[2])

    return [ 116 * Y3 - 16,
             500 * (X3 - Y3),
             200 * (Y3 - Z3) ]

def LABtoRGB(LAB):
    Y3 = (LAB[0] + 16.) / 116
    X3 = LAB[1] / 500. + Y3
    Z3 = Y3 - LAB[2] / 200.

    XYZn = RGBtoXYZ([1,1,1])
    XYZ = [ XYZn[0] * inv_f(X3),
            XYZn[1] * inv_f(Y3),
            XYZn[2] * inv_f(Z3) ]
    
    return XYZtoRGB(XYZ);


# CIE L*u*v* conversions

def XYZtoUCS(XYZ):
    X,Y,Z = XYZ
    u_ = 4. * X / (X + 15 * Y + 3 * Z)
    v_ = 9. * Y / (X + 15 * Y + 3 * Z)
    return Y, u_, v_

def UCStoXYZ(Yu_v_):
    Y,u_,v_ = Yu_v_
    x, y = UCStoCIE1931(u_, v_)
    z = 1 - x - y
    s = Y / y
    return s * x, s * y, s * z
    

def CIE1931toUCS(x, y):
    u_ = 4. * x / (-2 * x + 12 * y + 3)
    v_ = 9. * y / (-2 * x + 12 * y + 3)
    return u_, v_

def UCStoCIE1931(u_, v_):
    x = 6.75 * u_ / (4.5 * u_ - 12 * v_ + 9)
    y = 3.00 * v_ / (4.5 * u_ - 12 * v_ + 9)
    return x, y

def RGBtoLUV(RGB):
    Y, u_, v_  = XYZtoUCS(RGBtoXYZ(RGB))
    Yn,un_,vn_ = XYZtoUCS(RGBtoXYZ([1,1,1]))

    L = YtoL(Y / Yn)
    U = 13 * L * (u_ - un_)
    V = 13 * L * (v_ - vn_)
    
    return L, U, V

def LUVtoRGB(LUV):
    Yn,un_,vn_ = XYZtoUCS(RGBtoXYZ([1,1,1]))

    Y = LtoY(LUV[0]) * Yn
    u_ = LUV[1] / (13 * LUV[0]) + un_
    v_ = LUV[2] / (13 * LUV[0]) + vn_
    
    return XYZtoRGB(UCStoXYZ((Y, u_, v_)))


def LABhue(RGB):
    LAB = RGBtoLAB(RGB)
    return math.atan2(LAB[2],LAB[1])

def abdiff(a, b):
    u = RGBtoLAB(a)
    v = RGBtoLAB(b)
    return math.sqrt((u[1] - v[1])**2 + (u[2] - v[2])**2)

def Ldiff(a, b):
    u = RGBtoLAB(a)
    v = RGBtoLAB(b)
    return abs(u[0] - v[0])

def getL(rgb): return RGBtoLAB(rgb)[0]

def inUnit(vec):
    """Tests whether the vector is inside the unit cube [0,1]^n"""
    return len(filter((lambda x: x<0 or x>1), vec)) == 0
    

def LABclamp(rgb):
    """Clamp a RGB color into [0,1]^3 towards the CIELAB L-axis 

    rgb: the color in RGB709
    returns: the clamped color in RGB709
    """
    if inUnit(rgb):
        return rgb

    (L,a,b) = RGBtoLAB(rgb)

    r = 0
    bit = .5
    for iter in range(0,10):
        rgb = LABtoRGB([L,(r+bit)*a,(r+bit)*b])
        if inUnit(rgb):
            r = r + bit
        bit = bit * .5
    return LABtoRGB([L,r*a,r*b])



def getRGBslice(Y, dupes = 0):
    """Return the intersection of the RGB cube and the given Y-plane
    as a list of RGB triplets defining the intersection polygon
    """
    poly = []

    w = (Wr, Wg, Wb)

    for i in range(0, 3):
        c = [0,0,0]
        r = Y / w[i]
        if r <= 1:
            c[i] = r
            poly.append(tuple(c))
            if dupes:
                poly.append(tuple(c))
        else:
            r_prev = (Y - w[i]) / w[(i-1)%3]
            r_next = (Y - w[i]) / w[(i+1)%3]

            c[i] = 1
            if r_prev <= 1:
                c[(i-1)%3] = r_prev
                poly.append(tuple(c))
            elif dupes:
                c[(i-1)%3] = 1
                c[(i+1)%3] = (Y - w[i] - w[(i-1)%3]) / w[(i+1)%3]
                poly.append(tuple(c))

            c = [0,0,0]
            c[i] = 1
            if r_next <= 1:
                c[(i+1)%3] = r_next
                poly.append(tuple(c))
            else:
                c[(i+1)%3] = 1
                c[(i-1)%3] = (Y - w[i] - w[(i+1)%3]) / w[(i-1)%3]
                poly.append(tuple(c))

    return poly

# The YST color space below is a linear color space with 
# a luminance component and a color plane vector whose angle and
# radius specify the hue and saturation, respectively.
#
# The Y component is the CIE Y luminance and
# the ST-plane has the RGB primaries 120 (R = 0, G = 120, B = 240)
# degrees apart at radius 1.
#    
# Luminance weights of the RGB primaries used in YST color space
# functions:
#Wr = 0.212671 
#Wg = 0.715160
#Wb = 0.072169
# Note: the weigths are computed from the values specified at the
#       beginning of this file
# Note: the YST color space is device dependent unless
#       a standardized RGB space is used

def YSTtoRGB(v):
    mat =  [ [1, Wg+Wb,    (Wb - Wg) / math.sqrt(3) ],
             [1,   -Wr,  (2*Wb + Wr) / math.sqrt(3) ],
             [1,   -Wr, -(2*Wg + Wr) / math.sqrt(3) ] ]
    
    return matvecmul(mat, v)

def RGBtoYST(v):
    mat = [[ Wr, Wg, Wb ],
           [ 1, -.5, -.5],
           [ 0, .5*math.sqrt(3), -.5*math.sqrt(3) ]]

    return matvecmul(mat, v)

def maxYSTsat(YST):
    """Return the maximum saturation factor in RGB cube of the given color"""

    # Split into "lightness" and "color" components
    Y = YSTtoRGB((YST[0],0,0))
    vec = YSTtoRGB((0,YST[1],YST[2]))

    assert 0 <= Y[0] == Y[1] == Y[2] <= 1

    return min( ((vec[0] > 0) - Y[0]) / vec[0],
                ((vec[1] > 0) - Y[1]) / vec[1],
                ((vec[2] > 0) - Y[2]) / vec[2] )

    
def clampSat(rgb):
    """Clamp an RGB color keeping hue and lightness constant"""

    if inUnit(rgb):
        return rgb

    (Y,S,T) = RGBtoYST(rgb)

    r = maxYSTsat((Y,S,T))

    return YSTtoRGB((Y,r*S,r*T))


def YSThue(RGB):
    YST = RGBtoYST(RGB)
    return math.atan2(YST[2],YST[1])

C0 = 1955 / 150.0
C1 = -5533 / 150.0

def RGBtoYRB(RGB):
    L,M,S = XYZtoLMS( RGBtoXYZ(RGB) )
    L0,M0,S0 = XYZtoLMS( RGBtoXYZ((1,1,1)) )
    return (L + M,
            C0 * (L / (L + M) - L0 / (L0 + M0)),
            C1 * (S / (L + M) - S0 / (L0 + M0)))

def YRBtoRGB(YRB):
    L0,M0,S0 = XYZtoLMS( RGBtoXYZ((1,1,1)) )
    L = ((YRB[1] / C0) + L0 / (L0 + M0)) * YRB[0]
    M = YRB[0] - L
    S = ((YRB[2] / C1) + S0 / (L0 + M0)) * YRB[0]
    return XYZtoRGB( LMStoXYZ((L,M,S)) )

def YtoL(Y):
    """
    Convert linear luminance into perceptual lightness (CIE L*)
    Y: luminance between 0 and 1
    returns: lightness between 0 and 100
    """
    if Y <= (216./24389):
        return Y * (24389./27)
    else:
        return 116 * pow(Y, 1./3) - 16

def LtoY(L):
    """
    Convert perceptual lightness (CIE L*) into linear luminance
    L: lightness between 0 and 100
    returns: luminance between 0 and 1
    """
    if L <= 8:
        return L * (27./24389)
    else:
        return pow((L + 16.0) / 116, 3.0)
