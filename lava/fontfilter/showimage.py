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
import os, sys

import Tkinter
import ImageTk

def showImage(im, root = None, title = None):
    if not root:
        root = Tkinter.Toplevel()
    if not hasattr(root, "xxx_i"):
        root.xxx_i = []
        root.xxx_l = []
    if title:
        root.title(title)
    
    root.xxx_i.append(ImageTk.PhotoImage(im))
    l = Tkinter.Label(root, image = root.xxx_i[-1])
    l.pack()
    root.xxx_l.append(l)
    return root

def replaceImage(im, root, i = 0):
    t = ImageTk.PhotoImage(im)
    root.xxx_l[i]["image"] = t
    root.xxx_i[i] = t
    return root

def pasteImage(im, root, i = 0, box = None):
    # XXX: ImageTk.paste method ignores box!!!
    root.xxx_i[i].paste(im)
    return root
    
