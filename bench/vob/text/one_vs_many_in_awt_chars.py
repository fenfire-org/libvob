# 
# Copyright (c) 2004, Tuomas J. Lukka and Matti Katila
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


import java
from org.nongnu import libvob as vob

def benchScene(vs,
               nletters = 5,
               nwords = 15,
               nlines = 40,
               onePtime = 0):
    if isinstance(vob.GraphicsAPI.getInstance(), vob.impl.gl.GLAPI):
        return

    vs.map.put(vob.vobs.SolidBackdropVob(java.awt.Color.green))
    alph = 'abcdefghijklmnopqrstuvwxyz'
    alph = 100 * alph
    
    class ONE(vob.AbstractVob):
        def __init__(self, x,y, letter):
            self.x, self.y, self.t = x,y,letter
        def render(self, g, f, info1, info2):
            #print self.t
            g.drawString(self.t, self.x, self.y)
    
    class Whole(vob.AbstractVob):
        def __init__(self, x,y, word):
            self.x, self.y, self.t = x,y,word
        def render(self, g, f, info1, info2):
            g.drawString(self.t, self.x, self.y)

    x, y = 0,0
    G  = 12
    for i in range(nlines):
        x = 0
        for j in range(nwords):
            if (onePtime):
                for k in range(nletters):
                    vs.put(vob.vobs.TextTestVob(x,y,alph[x]))
                    x += G
            else:
                vs.put(vob.vobs.TextTestVob(x,y, alph[x: x+nletters]))
                x += G*nletters
            x += G
        y += G
       

args = { 
    "nletters": (3, 5, 7, 9),
    "nwords"  : (5, 13),
    "nlines"  : (1, 20, 40, 80),
    "onePtime": (0, 1),
}
