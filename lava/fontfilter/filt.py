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

from Numeric import *
from util import *
from fontfilter import FontFilter
import imagegen
import fontmap
import sys
import os


def msg(str):
    sys.stderr.write(str)
    sys.stderr.flush()

#txt = load("txt3.png")
font = fontmap.getFont("Helvetica", 12)
txt = imagegen.text(font)
#txt = imagegen.wordMatrix(font, 0)
msg("Txt image shape: %s\n" % (shape(txt),))

c0 = .2
bg = load("bg2.png")
bg = where(bg == 1, 1.0, 0.0)
b = (1 - sum(flat(bg)) / size(bg))
bg = where(bg == 1, 1.0, ((c0 + 1) / 2 - (1 - b) ) / b)

print ((c0 + 1) / 2 - (1 - b) ) / b

bgs = [#imagegen.checkerboard(4, c0 = .25),
       #imagegen.checkerboard(8, c0 = .25),
       bg,
       #load("bg3.png")
       ]
msg("Bg image shape: %s\n" % map(shape, bgs))

filt = [ FontFilter(bg, txt) for bg in bgs ]

#dists = (.5, 1, 2, 4, 999)
#blurs = (0, 1, 2, 4, 8)
#bleaches = (0, .1, .2, .4, .8)
dists = (1, 3, 6, 40, 999)
blurs = (0, 1, 2, 4, 8)
bleaches = (0, )

def getname(ind, dist, blur, dist2, bleach):
    def f(x):
        if x == None: return "*"
        return "%04.1f" % (x,)
    
    return ",,out%s_%s_%s_%s_%s" % (ind, f(dist), f(blur), f(dist2), f(bleach))

def gen(dist, blur, dist2, bleach):
    msg("Generating: %s %s %s %s\n" % (dist, blur, dist2, bleach))
    for i in range(0, len(filt)):
        f = getname(i, dist, blur, dist2, bleach)
        filt[i].update(dist, blur, dist2, bleach)
        #msg("Saving %s\n" % (f,))
        save(filt[i].get(), f + "_img.png")
        save(filt[i].get(showtxt = 0), f + "_bg.png")

def montage(args, name):

    args1 = reduce(lambda a,b: a+b, args)

    for i in range(0, len(filt)):
        imgs = " ".join(map(lambda x: apply(getname, (i,) + x) + "_img.png", args1))
        bgs = " ".join(map(lambda x: apply(getname, (i,) + x) + "_bg.png", args1))

        tile = "-tile %sx%s" % (len(args[0]), len(args))
    
        msg("Building %s img montage... " % (name,));
        os.system("montage -geometry +1+1 %s %s ,,foo%s_%s_img.png" %
                  (tile, imgs, i, name))
        msg("\n")
        msg("Building %s bg montage... " % (name,));
        os.system("montage -geometry +1+1 %s %s ,,foo%s_%s_bg.png" %
                  (tile, bgs, i, name))
        msg("\n")


for dist in dists:
    for blur in blurs:
        for bleach in bleaches:
            gen(dist, blur, dist, bleach)

montage([[(dist, 0, dist, bleach)
          for bleach in bleaches]
         for dist in dists], "bleach")

montage([[(dist, blur, dist, 0)
          for blur in blurs]
         for dist in dists], "blur")

for dist in dists:
    montage([[(dist, blur, dist, bleach)
              for blur in blurs]
             for bleach in bleaches], "dist" + str(dist))



