#(C): Janne V. Kujala

# This module defines the psychometric function and Bayesian model
# used by the Psi method trial placement computation (psi_placement.py)

import Numeric
from Numeric import *
from sys import stderr

# Fix underflow
min_exp = -745.13321910194116526
def safe_exp(x):
    x = where(less(x, min_exp), min_exp, x)
    return Numeric.exp(x)

RND_PRIOR = 0
RND_MODEL = (1.1, 1.1, 0)

def psi(point, model):
    a = point[1] - model[2]
    x = point[0] / model[0] * cos(a)
    y = point[0] / model[1] * sin(a)

    d = .04
    r2 = x*x + y*y
    p = ((1 - 0.5 * d) - (1 - d) * safe_exp( -r2 * r2 ));
    #p = ((1 - 0.5 * d) - (1 - d) * safe_exp( -r2  ));
    #p = ((1 - 0.5 * d) - (1 - d) * safe_exp( -r2 * r2 * r2 ));

    if RND_PRIOR:
        return where(model[0] == RND_MODEL[0], .5, p)

    return p

    
def getPoint(i):
    r,a = points[i]
    x = r * cos(a)
    y = r * sin(a)
    return x,y

def getPointIndex(x, y):
    """Return the index of a point nearest to (x,y)
    or -1 if the point is out of range
    """
    r = hypot(x, y)
    r = where(r == 0, 1E-100, r)
    r = log(r) / log(F) + nr - 1
    r = (r + .5).astype(Int32)

    x = where(x == 0, 1E-100, x)
    a = arctan(y / x) - .5 * (r & 1) * pi / na
    a = fmod(a, pi)
    a = where(less(a, 0), a + pi, a) / pi * na
    
    a = (a + .5).astype(Int32)
    a = where(a == na, 0, a)

    return where((r >= nr) | (r < 0), -1, r * na + a)


def getModel(i):
    r1,r2,a0 = models[i]

    mat = array([[cos(a0), -sin(a0)],
                 [sin(a0),  cos(a0)]])

    n = 32
    a = arange(0,n+1) * 2 * pi / n

    x = r1 * cos(a)
    y = r2 * sin(a)

    return transpose(dot(mat, array((x,y))))




# Sampling constants

F = 10**(1/20.)  # Intensity value resolution
nr = 40          # Number of intensity values
na = 12          # Number of intensity angles

F2 = 10**(1/15.) # Ellipse axis length resolution
nr2 = 32         # Number of ellipse axis lengths
na2 = 12         # Number of ellipse angles


points = array([ (F**(i+1-nr), (j+0.5*(i&1))*pi/na)
                 for i in range(0, nr)
                 for j in range(0, na)
                 ])

models = array([ (F2**(i+1-nr2), F2**(j+1-nr2), (k+0.5*(i&1))*pi/na2)
                 for i in range(0, nr2)
                 for j in range(0, i+1)
                 for k in range(0, na * (i > j) + (i == j))
                 ])


if RND_PRIOR:
    models = concatenate((models, array([RND_MODEL])))

prior = ones((len(models),), Float64) / float(len(models))


if RND_PRIOR: prior[-1] *= RND_PRIOR

prior = prior / sum(prior)
                                
#f = open(",,psi_points.dat", "w")
#f.write(points.tostring())
#f.close()

#f = open(",,psi_models.dat", "w")
#f.write(models.tostring())
#f.close()

def sample():
    f = open(",,psi.dat", "w")

    for i in range(0, len(points)):

        if (i & 15) == 0: print >> stderr, "%.1f%%" % (float(i) / len(points) * 100)

        p = psi( points[i], transpose( models ) )

        f.write(p.astype(Float32).tostring())

    f.close()


def get_psi():
    #print >> stderr, "Reading ,,psi.dat..."
    f = open(",,psi.dat", "r")
    psi_tbl = reshape(fromstring(f.read(len(points) * len(models) * 4), Float32),
                      (len(points), len(models)))
    f.close()
    #print >> stderr, "done"

    return psi_tbl
