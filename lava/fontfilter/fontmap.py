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

fontmap = {
    "Helvetica": "/usr/lib/X11/fonts/Type1/n019003l.pfb",
    "Palatino": "/usr/lib/X11/fonts/Type1/p052003l.pfb",
    "Schoolbook": "/usr/lib/X11/fonts/Type1/c059013l.pfb",
    "Times": "/usr/lib/X11/fonts/Type1/n021003l.pfb",
    "Courier": "/usr/lib/X11/fonts/Type1/n022003l.pfb",
    "cmr": "/usr/share/texmf/fonts/type1/bluesky/cm/cmr10.pfb",
    }

fontnames = fontmap.keys()
fontnames.sort()

def getFont(fontname, size):
    return ImageFont.truetype(fontmap[fontname], size)

