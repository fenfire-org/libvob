# 
# Copyright (c) 2003, Tuomas J. Lukka and Janne Kujala
# 
# This file is part of Gzz.
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

from __future__ import nested_scopes
import vob
from util import *

class Scene:
    """ A scene showing multiple papers.
    """
    has_dual_papers = 0

    def __init__(self):
	self.bgcolor = (0.7, 0.8, 0.6)
        self.w = 3
        self.h = 2
        self.seed = 0
        self.initpaper()

	self.key = KeyPresses(self, 
Toggle("showOptimized", 0, "Use optimized papers", "o"),
Action("Choose random seed", "v", self.randomize, noAnimation=1),
Action("Edit seed number", re.compile("[0-9]|Back[sS]pace"), 
				self.__class__.editSeed, noAnimation=1),
Action("More/less papers shown", re.compile("[+-]"), self.__class__.npapersadj) ,
	    *keys
	    )

    def npapersadj(self, k):
        dw,dh = 0,0
        if k == "+": dw,dh = [ (0,1), (1,0) ][self.w * 3 < self.h * 4]
        elif k == "-": dw,dh = [ (-1,0), (0,-1) ][self.w * 3 < self.h * 4]
	else: print "NPapersadj called wrongly"
        self.w += dw
        self.h += dh

    def randomize(self, *args):
	self.seed = rng.nextInt(2000000000)
	self.initpaper()

    def editSeed(self,k):
        if k >= "0" and k <= "9": self.seed = self.seed * 10 + int(k)
        if k == "BackSpace" or k == "Backspace": self.seed = self.seed / 10
	self.initpaper()

    def initpaper(self):
        self.pq = range(0, self.w*self.h)
        self.opq = range(0, self.w*self.h)
        paperopt = vob.gl.PaperOptions.instance()
        tmp_paperopt = paperopt.use_opengl_1_1
        
        if not self.has_dual_papers:
            for i in range(0,self.w*self.h):
                self.pq[i] = getpaper(self.seed + i)
                self.opq[i] = getpaper(self.seed + i, optimized=1)
        else:
            for i in range(0, self.w*self.h):
                paperopt.use_opengl_1_1 = i & 1
                self.pq[i] = getpaper(self.seed + i / 2)
                self.opq[i] = getpaper(self.seed + i / 2, optimized=1)

        paperopt.use_opengl_1_1 = tmp_paperopt
            

    def mouse(self, ev):
	if ev.getID() == ev.MOUSE_CLICKED:
	    x,y = ev.getX(), ev.getY()

            print x,y
            index = 0
            for box in self.boxes:
                if (box[0] <= x <= box[2] and
                    box[1] <= y <= box[3]):
                    setCurrentScene("F1")
                    global currentScene
                    currentScene.seed = self.seed + index
                    currentScene.initpaper()
                index += 1
            

    def OLDkey(self, k):
	if k == "v":
            self.seed = rng.nextInt(200000000)
        if k == "f":  # as fork
            self.has_dual_papers = not self.has_dual_papers
            print 'Dual papers:', self.has_dual_papers
        if k in ("v", "f", "Return"):
	    self.initpaper()
	    AbstractUpdateManager.setNoAnimation()
	    AbstractUpdateManager.chg()

    def scene(self, vs):
        size = vs.getSize()
	vs.put( background(self.bgcolor))


        cs1w = .47*size.width/self.w
        cs1h = .47*size.height/self.h

	print self.w, self.h

	scale = float(self.zoom)
	cs2 = vs.coords.affine(0, 1, 0, 0, scale/cs1w, 0, 0, scale/cs1h)
        vs.matcher.add(cs2, "tex")

        self.boxes = []
        cs1 = range(0,self.w*self.h)
        for i in range(0,self.w*self.h):
            if i >= len(self.pq): self.initpaper()

            x = (i%self.w+.5)*size.width/self.w
            y = (i/self.w+.5)*size.height/self.h

            self.boxes.append( (x - cs1w, y - cs1h,
                                x + cs1w, y + cs1h ) )
            
            cs1[i] = vs.coords.affine(0, 100-i, x, y, cs1w, 0, 0, cs1h)

            
            vs.matcher.add(cs1[i], str(i))
            
	    if self.showOptimized: pap = self.pq[i]
	    else: pap = self.opq[i]
            vs.map.put(pap, cs1[i], cs2)
            putText(vs, cs1[i],
                    str(self.seed + i / (self.has_dual_papers + 1)),
                    color=(0,0,0), x=-1,y=1,z=-1, h=.5, key=str(i))


