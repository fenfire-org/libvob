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
import ImageDraw
import Image

def drawText(im, pos, width, font, text, spacing = None,
             justify = "L"):
    draw = ImageDraw.Draw(im)

    m = font.getmetrics()
    height = m[0] + m[1]

    space = font.getsize(" ")[0]
    if spacing:
        height = int(spacing * height + .5)
    
    words = text.split()
    sizes = [font.getsize(word)[0] for word in words]

    dx_sum = 0
    dx_count = 0

    i0 = 0
    while i0 < len(sizes):
        w = sizes[i0]
        i = i0 + 1
        while i < len(sizes):
            t = w + space + sizes[i]
            if t > width: break
            w = t
            i += 1

        dw = width - w
        if justify == "B":
            dx = space
            if i == i0 + 1:
                pass
            elif i < len(sizes):
                dx += dw / (i - i0 - 1.0)
                dx_sum += dx
                dx_count += 1
            else:
                dx = dx_sum / dx_count
            x, y = pos
            for j in range(i0, i):
                draw.text( (x, y), words[j], font = font, fill = 0)
                x += sizes[j] + dx
        else:
            t = " ".join(words[i0:i])
            x = pos[0]
            if justify == "R": x += dw
            if justify == "C": x += .5 * dw
            draw.text( (x, pos[1]), t, font = font, fill = 0)


        pos = (pos[0], pos[1] + height)
        i0 = i

    return pos


