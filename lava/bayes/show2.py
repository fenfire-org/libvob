from Numeric import *
import Tkinter
import ImageTk
import Image
from psi_model2 import npoints,getPoint,getPointIndex

N = 500

indices = fromfunction(lambda y,x: getPointIndex((x - N/2) * (2.0 / N),
                                                 -(y - N/2) * (2.0 / N)), (N, N))

points = reduce(lambda x,y:x+y, [ [
    (int(x * N/2 + N/2)) * N + int(-y * N/2 + N/2),
    (int(-x * N/2 + N/2)) * N + int(y * N/2 + N/2)
    ] for (x,y) in map(getPoint, range(0, npoints)) ])

points = filter(lambda p: p >= 0 and p < N * N, points)

#put(indices, points, -1)

pim = None
def showPointData(data):
    data = reshape(data, (size(data),))
    a = take(data, indices)

    if 1:
        M = max(a.flat)
        a = where(a == 0, M, a)
        m = min(a.flat)

        if M > m:
            a = (a - m) / (M - m)
        else:
            a = 0 * a

    a = (a * 255).astype(UnsignedInt8)

    im = Image.fromstring("L", (N,N), a.tostring())

    global pim, root
    if pim == None:
        root = Tkinter.Tk()
        pim = ImageTk.PhotoImage(im)
        label = Tkinter.Label(root, image = pim)
        label.pack()
    else:
        pim.paste(im)

    root.update()

