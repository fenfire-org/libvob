from util import *


a = load("txt3.png")

x = [[], [], [], [], [], []]


for w in arange(.1, 4, .1):
    b = 1 - fft_blur(a, w, (0,1)) / 255.

    f = genfilt1d(512, w)
    g = genfilt1d_ideal(512, w)

    y = ( max(flat(b)),
          1.2 / (w + 1),
          f[0] + f[1],
          g[0] + g[1],
          f[1] * 2.3,
          g[1] * 2.3,
          )

    for i in range(0, len(x)):
        x[i].append(y[i])
    
    print w, y


plot(x)
