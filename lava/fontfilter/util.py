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


from __future__ import nested_scopes

import Image
import Numeric
from Numeric import *
from FFT import *
import os, popen2
from stats import erfcc

gamma = 2.2

# image2array and array2image functions adapted from Image-SIG mailing
# list postings
def image2array(im, gamma = gamma):
    if im.mode in ('1', 'L', 'RGB', 'RGBA', 'RGBA', 'CMYK', 'YCbCr'):
        a = Numeric.fromstring(im.tostring(), Numeric.UnsignedInt8)
    elif im.mode == 'F':
        a = Numeric.fromstring(im.tostring(), Numeric.Float32)
    elif im.mode == "I":
        a = Numeric.fromstring(im.tostring(), Numeric.Int32)
    else:
        raise ValueError, im.mode + " mode not supported"

    a.shape = (im.size[1], im.size[0], len(im.getbands()))

    a = a / 255.0
    if gamma != 1.0: a **= gamma
    return a

def array2image(a, gamma = gamma):
    if gamma != 1.0:
        a = a.astype(Numeric.Float64)
        a **= 1.0 / gamma
    a *= 255

    a = a.astype(Numeric.UnsignedInt8)
    if len(a.shape) == 2:
        bands = [ a ]
    else:
        bands = [ a[:,:,i] for i in range(0, a.shape[2]) ]

    bands = [ Image.fromstring("L", (b.shape[1], b.shape[0]), b.tostring())
              for b in bands ]
    
    mode = (None, 'L', None, 'RGB', 'RGBA')[len(bands)]

    return Image.merge(mode, bands)

def tileImage(im, w, h):
    x = im.size[0]
    y = im.size[1]
    dx = (w / 2 - x / 2) % x
    dy = (h / 2 - y / 2) % y
    nx = (w+dx+x-1)/x
    ny = (h+dy+y-1)/y

    print x,y,dx,dy,nx,ny

    tile = Image.new(im.mode, size = (w,h))
    for j in range(0,ny):
        for i in range(0,nx):
            tile.paste(im, (i*x-dx,j*y-dy))

    return tile

def load(file, gamma = gamma):
    im = Image.open(file)
    a = image2array(im, gamma)
    return a

def save(a, file, gamma = gamma):
    array2image(a, gamma).save(file)

def safe_exp(x):
    if x < -745: return 0
    return exp(x)


def genfilt1d_point(n, w):
    if w == 0:
        return array([1] + [0] * (n - 1))
    
    f = [ safe_exp(-i**2 / (2.0 * w**2)) for i in range(0, n/2+1) ]
    f = array(f + f[(n%2)-2:0:-1])
    f /= sum(f)

    return f

def genfilt1d_block(n, w):
    if w == 0:
        return array([1] + [0] * (n - 1))

    def g(i): return 1 - .5 * erfcc(-i / (sqrt(2) * w))

    f = [ g(i+.5) - g(i-.5) for i in range(0, n/2+1) ]
    f = array(f + f[(n%2)-2:0:-1])
    f /= sum(f)

    return f

def genfilt1d_ideal(n, w):
    g = genfilt1d_point(n, n / (2 * pi * w))
    f = inverse_fft(g).astype('d')
    return (f / sum(f))

def genfilt1d_alt(n, w):
    f = genfilt1d_point(n, .5)
    return filt_exp(f, (w/.4635)**2)

genfilt1d = genfilt1d_point
    
def filt_exp(f, x):
    g = real_fft(f)
    f = inverse_real_fft(g**x)
    return f / sum(f)

def rotate_filt(a):
    n = len(a)
    return concatenate((a[n/2:], a[:n/2]))

def pascal(n):
    f = zeros(n) * 1.0
    f[0] = 1
    for i in range(0,n-1):
        g = f[:-1].copy()
        f[1:] += g
    return f

def fft_blur1d(a, w, axis = 0):
    N = shape(a)[axis]

    f = genfilt1d(N, w)

    b = real_fft(a, axis = axis)
    g = real_fft(f)
    s = ones(len(shape(a)))
    s[axis] = shape(b)[axis]
    g = reshape(g, s)

    b *= g
    return inverse_real_fft(b, axis = axis)


def fft_blur(a, w, axes):
    b = a
    for axis in axes:
        b = fft_blur1d(b, w, axis)
    return b


def blur2d(a, w):
    x,y,c = shape(a)

    bin = "./blur2d"

    if os.stat(bin + ".c").st_mtime > os.stat(bin).st_mtime:
        os.system("make " + bin)
   
    g,f = popen2.popen2("%s %s %s %s" % (bin, x,y,c))

    f.write(w[:,:,0].astype('f').tostring())
    f.write(a.astype('f').tostring())

    tmp = fromstring(g.read(x * y * c * 4), "f")
    return reshape(tmp, shape(a))

def flat(a):
    return reshape(a, (product(shape(a)),))

def plot(data, axis = -1, **args):
    cmd = "graph -TX -a"
    for arg in args.keys():
        cmd += " -%s %s" % (arg, args[arg])

    p = os.popen(cmd, "w")

    n = size(data)
    m = shape(data)[axis]

    data = reshape(swapaxes(data, -1, axis), (n / m, m))
    
    for line in data:
        for x in line:
            p.write(str(x) + "\n")
        p.write("\n");
    p.close()

