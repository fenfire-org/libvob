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

import Tkinter
from util import *
from showimage import *
from imagegen import *
from fontmap import *

root = Tkinter.Tk()
root.title("Font test")

size = 12
fonts = fontmap.keys()
fonts.sort()

for size in 10, 12, 14:
    x = Tkinter.Frame(root)
    x.pack()
    for fontname in fontnames:
        font = getFont(fontname, size)
        im = textImg(font)

        y = Tkinter.Frame(x)
        y.pack(side = Tkinter.LEFT)
        y = showImage(im, root = y)
        Tkinter.Label(y, text = "%s %s" % (fontname, font.getname())).pack()
    
root.mainloop()
