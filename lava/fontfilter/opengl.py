from OpenGL.GL import *
import Tkinter
import OpenGL.Tk
from OpenGL.Tk import _default_root
from time import time

def swapBuffers():
    gl.tk.call(gl._w, 'swapbuffers')

def ev(e):
    global event, t
    event = e
    t = time()
    gl.quit()

def redraw():
    pass


def pev(e):
    print e.type, e.serial

def quit(e = None):
    gl.quit()
 
def init(**args):
    global gl
    gl = OpenGL.Tk.RawOpengl(**args)
    _default_root.wm_maxsize(gl.winfo_screenwidth(), gl.winfo_screenheight())
    gl.tkRedraw = redraw
    gl.pack(expand = 1, fill = Tkinter.BOTH)
    gl.bind("<Enter>", lambda event: event.widget.focus() )

    gl.update() # Process pending events to set up the widget

    gl.bind("<KeyPress>", ev)

    gl.tk.call(gl._w, 'makecurrent')
    return gl

def getwinsize():
    return (int(gl.configure("width")[4]),
            int(gl.configure("height")[4]))

def flushevents():
    gl.update()

def waitkey(timeout = None, t0 = None):
    global event
    if not t0:
        t0 = time()
    if timeout:
        event = None
        while not event:
            gl.update()
            t = time() - t0
            if t >= timeout:
                gl.tk.call(gl._w, 'makecurrent')
                return None, t
    else:
        gl.mainloop()
    t = time()
    #print 1000 * (t-1.07105E9), 1000 * (t-1.07105E9) - event.time, event.time
    
    gl.tk.call(gl._w, 'makecurrent')
    return event.keysym, 1000 * (t - t0)

def wait(timeout, t0 = None):
    if not t0:
        t0 = time()
    while time() - t0 < .001 * timeout:
        gl.update()
