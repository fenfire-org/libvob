import sys
import Tkinter
import Image
from showimage import *
from time import time

images = map(Image.open, sys.argv[1:])

root = Tkinter.Tk()

for im in images:
    im.load()

i = 0
showImage(images[i], root)

def ev(e):
    global i
    if e.keysym == "Next":
        i = (i + 1) % len(images)
    elif e.keysym == "Prior":
        i = (i - 1) % len(images)
    elif e.keysym == "q":
        root.quit()
    #root.iconify()
    t0 = time()
    pasteImage(images[i], root)
    t = time() - t0
    #root.deiconify()
    print t

root.bind('<KeyPress>', ev)

root.mainloop()
