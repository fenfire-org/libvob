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

import ImageFont
import Image
import ImageDraw
import util
from Numeric import *
from MLab import rand
from drawtext import *
import random 

txt =  """Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam
nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,
sed diam voluptua. At vero eos et accusam et justo duo dolores et ea
rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem
ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur
sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et
dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam
et justo duo dolores et ea rebum.

f i l ff fi fl ffi ffl
"""

w, h = 256, 256
mode = "L"
bgcolor = 255

def noise(c0 = .25, c1 = 1):
    c0 = reshape(array([c0, c0, c0]), (1, 1, 3))
    c1 = reshape(array([c1, c1, c1]), (1, 1, 3))
    return c0 + (c1 - c0) * rand(w, h, 1)

def colorNoise(c0 = .25, c1 = 1):
    return c0 + (c1 - c0) * rand(w, h, 3)

def checkerboard(size = 4, c0 = .5, c1 = 1):
    c0 = reshape(array([c0, c0, c0]), (1, 1, 3))
    c1 = reshape(array([c1, c1, c1]), (1, 1, 3))
    s2 = size * 2
    return c0 + (c1 - c0) * reshape(
        fromfunction(lambda x,y: (x % s2 < size) ^ (y % s2 < size), (w, h)),
        (w, h, 1))


def textImg(font, text = txt):
    im = Image.new(mode = mode, size = (w,h), color = bgcolor)
    drawText(im, (20,20), w - 40, font, txt)

    return im

targets = "hevonen hirvi kala kana kissa koira kukko lehm\xe4 siili".split()

def getpairs(word):
    l = []
    for i in range(0, len(word)-1):
        l.append(word[i:i+2])
    return l


def pseudoword(r = random):
    starts = [ w[:2] for w in targets ]
    pairs = reduce(lambda x,y:x+y, map(getpairs, targets))

    bad = 1
    while bad:
        bad = 0
        w = r.choice(starts)
        n = len(r.choice(targets))
    
        while len(w) < n:
            c = filter(lambda pair: pair[0] == w[-1], pairs)
            if not c: break
            w += r.choice(c)[1]
            if w[-1] == w[-2] == w[-3]:
                bad = 1
                break

        if bad: continue

        for t in targets:
            if w.find(t) != -1 or t.find(w) != -1:
                bad = 1
                break

    return w
    

def wordMatrixImg(font, type, seed = None):
    r = random.Random(seed)
    
    matw = 4
    math = 4
    n = matw * math
    l = [ pseudoword(r) for i in range(0, n - 1) ]
    if type == 1:
        target = r.choice(targets)
    else:
        target = pseudoword(r)
    i = r.choice(range(0, n))
    l = l[:i] + [target] + l[i:]

    im = Image.new(mode = mode, size = (w,h), color = bgcolor)

    i = 0
    for y in range(0, math):
        for x in range(0, matw):
            dx = 50
            dy = 20
            
            x0 = 0.5 * w + (x - 0.5 * (matw - 1)) * dx
            y0 = 0.5 * h + (y - 0.5 * (math - 1)) * dy
            
            drawText(im, (x0-.5*dx,y0), dx, font, l[i], justify = "C")

            i += 1

    return im

def wordMatrix(font, type, seed = None):
    im = wordMatrixImg(font, type, seed)
    return util.image2array(im)


def text(font, text = txt):
    im = textImg(font, text)
    return util.image2array(im)


def circleImg():
    
    im = Image.new(mode = mode, size = (w,h), color = bgcolor)
    draw = ImageDraw.Draw(im)

    for i in range(0,10000):
        x = random.randint(0, w)
        y = random.randint(0, h)
        s = random.randint(0, 5) + 2
        c = random.randint(150, 255)

        xy = (x, y, x+s, y+s)
        draw.ellipse(xy, fill = c, outline = c)

        if x+s > w:
            xy2 = (xy[0]-w,xy[1],xy[2]-w,xy[3])
            draw.ellipse(xy2, fill = c, outline = c)

            if y+s > h:
                xy2 = (xy[0]-w,xy[1]-h,xy[2]-w,xy[3]-h)
                draw.ellipse(xy2, fill = c, outline = c)

        if y+s > h:
            xy2 = (xy[0],xy[1]-h,xy[2],xy[3]-h)
            draw.ellipse(xy2, fill = c, outline = c)

        

    return im
        
def lineImg():

        
    im = Image.new(mode = mode, size = (w,h), color = bgcolor)
    draw = ImageDraw.Draw(im)

    for i in range(0,w*h/12):
        x = random.randint(0, w)
        y = random.randint(0, h)
        s = random.randint(0, 10) + 2
        c = random.randint(0, 255)

        a = random.random() * 2 * pi
        xy = (x+s,y+s,x+s+s*cos(a),y+s+s*sin(a))

        draw.line(xy, fill = c)

        if x+s >= w:
            xy2 = (xy[0]-w,xy[1],xy[2]-w,xy[3])
            draw.line(xy2, fill = c)

            if y+s >= h:
                xy2 = (xy[0]-w,xy[1]-h,xy[2]-w,xy[3]-h)
                draw.line(xy2, fill = c)

        if y+s >= h:
            xy2 = (xy[0],xy[1]-h,xy[2],xy[3]-h)
            draw.line(xy2, fill = c)

        

    return im
