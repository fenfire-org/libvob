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

from Tkinter import *
from showimage import *
from fontmap import *
from MLab import rand
from util import *
import imagegen
import fontmap

root = Tk()
root.title("Image test")

def getFilt(s, f0, f1):

    def dim(i):
        d = [1] * len(s)
        d[i] = s[i]
        return tuple(d)

    r2 = zeros(s) * 0.0

    for i in range(0, len(s)):
        x = reshape(concatenate((arange(0, s[i]/2),
                                 arange(s[i]/2, 0, -1))),
                    dim(i)) / float(s[i])
        r2 += x**2
    
    f = (r2 > f0**2) * (r2 <= f1**2)

    return f

w, h = 256, 256

font = fontmap.getFont("Helvetica", 11)
txt = imagegen.text(font)[:,:,0]

def cpl(f):
    return f * 6
    str = "Lorem ipsum dolor"
    lw = font.getsize(str)[0] / float(len(str))
    return lw * f

a = rand(w, h) - .5

#a = imagegen.checkerboard(8)[:,:,0]

b = fft2d(a, axes = (0,1))

freqs = [ .03125, .0625, .125, .25, .5, 1 ]
data = []
bandnames = []

f0 = 0
for f1 in freqs:
    data.append(inverse_fft2d(b * getFilt( (w, h), f0, f1),
                              axes = (0,1)).astype(Float64))
    print "Filtering band", f0, "...", f1
        
    bandnames.append("%.2g - %.2g cpl" % (cpl(f0), cpl(f1)))
    f0 = f1

n = len(data)
rms = [ sqrt(sum(flat(a**2))) for a in data ]

print rms

for i in range(0, n):
    if rms[i]: data[i] /= rms[i]

tot_rms = 0

oldvals = None
def update(val = None):
    global oldvals
    newvals = (dc.get(), tuple([ band.get() for band in bands ]),
               showtxt.get(), txt_col.get(), addtxt.get())
    if newvals == oldvals: return
    oldvals = newvals

    x = reshape(array(newvals[1]), (n, 1, 1))
    a = sum(x * data, axis = 0) + newvals[0]

    a = where(a < 0, 0, a)
    a = where(a > 1, 1, a)

    global tot_rms
    tot_rms = sqrt(sum(flat((a - dc.get())**2)))
    tot.set(tot_rms)

    if newvals[2]:
        if newvals[4]:
            a += (1 - txt) * (newvals[3] - .5) 
            a = where(a < 0, 0, a)
            a = where(a > 1, 1, a)
        else:
            a = a * txt + (1 - txt) * newvals[3]
    
    im = array2image(a)
    pasteImage(im, root)

def update_tot(val):
    val = float(val)
    if abs(val - tot_rms) > .05:
        if tot_rms > 0:
            m = val / tot_rms
            for band in bands:
                band.set( band.get() * m )
        else:
            for band in bands:
                band.set( val / sqrt(n) )
                
txt_col =  Scale(root, label = "Text color", orient=HORIZONTAL,
                 from_=0, to=1, resolution=.1, length = 256,
            command = update)
txt_col.pack()
dc =  Scale(root, label = "dc", orient=HORIZONTAL,
            from_=0, to=1, resolution=.1, length = 256,
            command = update) 
dc.set(0.5)
dc.pack()

bandmax = w * h / 1024.0

bands = [ Scale(root, label = "band " + str(i) + ": " + bandnames[i],
                orient=HORIZONTAL,
                from_=0, to=bandmax, resolution=.1, length = 256,
                command = update) for i in range(0, n)]

for band in bands:
    band.pack()

tot = Scale(root, label = "Total RMS energy", orient=HORIZONTAL,
            from_=0, to=bandmax * sqrt(n), resolution = .1, length = 256,
            command = update_tot)
tot.pack()

im = Image.new(mode = "RGB", size = (w,h), color = (255,255,255))
showImage(im, root)

showtxt = IntVar()
c = Checkbutton(root, text="Show text", variable = showtxt,
                command = update)
c.pack()
addtxt = IntVar()
c = Checkbutton(root, text="Additive text", variable = addtxt,
                command = update)
c.pack()

root.mainloop()

