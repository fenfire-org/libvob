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
from combinerutil import *

class Scene(PaperTemplate):
    """Show a "flowchart" of texture combiner operations to create shapes.
    """
    def __init__(self):
	self.bgcolor = (0.7, 0.8, 0.6)
	self.pt = PaperTemplate()
    def key(self, k):
	if k == "s":
	    shootImages()
	else:
	    self.pt.key(k)

    def scene(self, vs):
	vs.put( background(self.bgcolor))
	
	self.pt.place(vs,
	    vs.orthoCS(0, "S1", 0, 10, 10, 100, 100),
	    "RGB0", "RGB0", "NONE")
	
	self.pt.place(vs,
	    vs.orthoCS(0, "S2", 0, 300, 100, 100, 100),
	    "NONE", "RGB1", "RGB1")
	
	self.pt.place(vs,
	    vs.orthoCS(0, "S3", 0, 200, 250, 100, 100),
	    "RGB0", "BAND0", "RGB1")

	self.pt.place(vs,
	    vs.orthoCS(0, "S4", 0, 200, 400, 200, 200),
	    "RGB0", "BAND1", "RGB1")
	
	
	if 0:
	    self.pt.place(vs,
		vs.orthoCS(0, "S5", 0, 400, 400, 200, 200),
		"RGB0", "BAND1_X", "RGB1")

	    self.pt.place(vs,
		vs.orthoCS(0, "S5", 0, 400, 550, 200, 200),
		"RGB0", "BAND2", "RGB1")
	    
	    self.pt.place(vs,
		vs.orthoCS(0, "S6", 0, 500, 550, 200, 200),
		"RGB0", "BAND3", "RGB1")
	    
	    self.pt.place(vs,
		vs.orthoCS(0, "S8", 0, 500, 150, 400, 400),
		"RGB0", "SHAPEX_2", "RGB1")
	    
	    self.pt.place(vs,
		vs.orthoCS(0, "S9", 0, 700, 200, 400, 400),
		"RGB0", "SHAPEX_3", "RGB1")

	    self.pt.place(vs,
		vs.orthoCS(0, "S9S", 0, 700, 450, 400, 400),
		"RGB0", "SHAPEX_3B", "RGB1")

	# print texcodes["SHAPEX_3"]


