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
import util

class FontFilter:
    def __init__(self, bg, txt, blurtype = 0):
        self.dist = None
        self.blur = None
        self.dist2 = None
        self.bleach = None
        self.bg = bg
        self.txt = txt
        self.blurtype = blurtype
        self.update(0,0,0,0)
        
    def update(self, dist, blur, dist2, bleach):

        mask = (dist != self.dist,
                blur != self.blur,
                dist2 != self.dist2,
                bleach != self.bleach)


        def blurText(txt, dist):
            if dist == 0:
                return 1 - txt
            
            blurred_txt = 1 - util.fft_blur(txt, dist, (0,1))

            # Normalize:
            m = max(util.flat(blurred_txt))
            print "Max:", m
            blurred_txt = blurred_txt * (1 / m)

            # Try to keep the maximum closer to constant
            #blurred_txt *= 1 + dist

            return blurred_txt
        
        if mask >= (1,0,0,0):
            self.dist = dist
            self.blurred_txt = blurText(self.txt, self.dist)

        if mask >= (0,1,0,0):
            self.blur = blur

            if self.blur > 0:
                if self.blurtype == 0:
                    # modulate blur radius
                    tmp = self.blurred_txt * self.blur
                    if self.dist < 999:
                        self.blurred_bg = util.blur2d(self.bg, tmp)
                    else:
                        self.blurred_bg = util.fft_blur(self.bg, self.blur, axes = (0,1))
                else:
                    # lerp to constant-radius-blurred
                    tmp = arctan(self.blurred_txt * 6) * (2 / pi)
                    self.blurred_bg = util.fft_blur(self.bg, self.blur, axes = (0,1))
                    self.blurred_bg = (1 - tmp) * self.bg + tmp * self.blurred_bg 
            else:
                self.blurred_bg = self.bg

        if mask >= (0,0,1,0):
            self.dist2 = dist2
            self.blurred_txt2 = blurText(self.txt, self.dist2)
                
        if mask >= (0,0,0,1):
            self.bleach = bleach
            if self.bleach == 0:
                self.bleached_bg = self.blurred_bg
            else:
                if 0:
                    # add white
                    self.bleached_bg = self.blurred_bg + self.blurred_txt2 * self.bleach
                else:
                    # lerp to white
                    t = arctan(self.blurred_txt2 * self.bleach) * (2 / pi)
                    self.bleached_bg = (1 - t) * self.blurred_bg + t

    def get(self, showenh = 1, showtxt = 1, brightness = 1.0):
        if showenh:
            t = self.bleached_bg
        else:
            t = self.bg
        if brightness != 1.0:
            t = (2 - brightness) * t + (brightness - 1)
            if brightness < 1.0:
                t = where(t < 0, 0, t)
        if showtxt:
            t = t * self.txt
        return t
