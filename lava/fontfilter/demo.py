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
from Tkinter import *
import ImageTk
import util
from fontfilter import FontFilter
from Numeric import *
import showimage
from FFT import *

class App(Frame):
    def __init__(self, master, bgs, txts):
        Frame.__init__(self, master)
        self.pack()

        self.filt = [ [ FontFilter(bg, txt, blurtype) for bg in bgs for txt in txts] for blurtype in (0,1) ]

        self.bgind = Scale(self, label = "Background image", orient=HORIZONTAL,
                           from_=0, to=len(bgs)-1, resolution=1, length = 256,
                           command=self.update)
        self.bgind.pack()
        self.txtind = Scale(self, label = "Text image", orient=HORIZONTAL,
                            from_=0, to=len(txts)-1, resolution=1, length = 256,
                            command=self.update)
        self.txtind.pack()

        self.brightness = Scale(self, label = "Background brightness", orient=HORIZONTAL,
                              from_=0, to=2, resolution=.1, length = 256,
                              command=self.update)
        self.brightness.set(1.0)
        self.brightness.pack()

        self.image = util.array2image(bgs[0].astype(UnsignedInt8))
        
        self.tkim = ImageTk.PhotoImage(self.image.mode, self.image.size)
        Label(self, image=self.tkim).pack()

        self.showtxt = IntVar()
        c = Checkbutton(master, text="Show text", variable = self.showtxt,
                        command = self.update)
        c.pack()
        self.showenh = IntVar()
        c = Checkbutton(master, text="Show enhancement", variable = self.showenh,
                        command = self.update)
        c.pack()
        self.blurtype = IntVar()
        c = Checkbutton(master, text="Alternative blurring", variable = self.blurtype,
                        command = self.update)
        c.pack()


        self.dist = Scale(self, label = "blur dist", orient=HORIZONTAL,
                          from_=0, to=10, resolution=0.2, length = 256,
                          command=self.update)
        self.dist.pack()

        self.blur = Scale(self, label = "blur", orient=HORIZONTAL,
                          from_=0, to=20, resolution=0.2, length = 256,
                          command=self.update)
        self.blur.pack()

        self.dist2 = Scale(self, label = "bleach dist", orient=HORIZONTAL,
                           from_=0, to=10, resolution=0.2, length = 256,
                           command=self.update)
        self.dist2.pack()

        self.bleach = Scale(self, label = "bleach", orient=HORIZONTAL,
                            from_=0, to=10, resolution=0.1, length = 256,
                            command=self.update)
        self.bleach.pack()

        f = Frame(self)
        Button(f, text = "QUIT", command = self.quit).pack(side = LEFT)
        Button(f, text = "Toggle", command = self.toggle).pack(side = RIGHT)
        Button(f, text = "Spectrum", command = self.spect).pack(side = RIGHT)
        f.pack()
        
    def toggle(self):
        self.showenh.set(not self.showenh.get())
        self.showtxt.set(not self.showtxt.get())
        self.update()
        
    def update(self, val = None):
        i = int(self.bgind.get() * (1 + self.txtind.cget("to")) +
                self.txtind.get())
        j = int(self.blurtype.get())
        filt = self.filt[j][i]
        
        filt.update(self.dist.get(),
                    self.blur.get(),
                    self.dist2.get(),
                    self.bleach.get())
        t = filt.get( self.showenh.get(),
                      self.showtxt.get(),
                      self.brightness.get() )
        self.tkim.paste(util.array2image(t))

    def spect(self):
        i = int(self.bgind.get() * (1 + self.txtind.cget("to")) +
                self.txtind.get())
        j = int(self.blurtype.get())
        filt = self.filt[j][i]
        t = filt.get( self.showenh.get(),
                      self.showtxt.get(),
                      self.brightness.get() )
        u = abs(fft2d(sum(t, 2))).astype('d')
        x,y = shape(u)
        u *= 1.0 / max(util.flat(u)[1:])

        showimage.showImage(util.array2image(u))
        
        

import imagegen
import fontmap

bgs = [ util.load("bg.png"),
        util.load("bg2.png"),
        util.load("bg3.png"), ]

bgs.append( imagegen.noise() )
bgs.append( imagegen.colorNoise() )
bgs.append( imagegen.checkerboard(8) )
bgs.append( imagegen.checkerboard(4) )

txts = [ util.load("txt3.png") ]

for size in 12, 14:
    for font in "Helvetica", "Schoolbook":
        txts.append( imagegen.text(fontmap.getFont(font, size)) )
        



root = Tk()

print map(shape, bgs)
print map(shape, txts)

App(root, bgs, txts, )
root.mainloop()
