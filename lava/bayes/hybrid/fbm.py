
from Numeric import *
import libplot
import os
import re

def psi_str(x, t): # NOTE NO PARENS!
    return """
-Log(Abs(%s - Exp(
  - Exp(Exp(v) * Log(0.69314718055994529 * %s / Exp(2*u))) )))
    """ % (t, x)

def psi(v, u, x, t):
    return -log(abs(t-exp(
	    -exp(exp(v) * log(0.69314718055994529 *x / exp(2*u))) )))

def streamtoarray(stream):
    arr = []
    while 1:
	str = stream.readline()
	if str == "":
	    return array(arr)
	arr.append([float(x) for x in str.split()])

try:
    os.unlink("psitest.log")
except:
    pass

def r(*args):
    args = [re.sub("\t"," ", a) for a in args]
    assert not os.spawnlp(os.P_WAIT,
	*args), args
	
def s(str):
    assert not os.system(str)
    
if 1:
#	u ~ ExpGamma(1, .2) + v ~ ExpGamma(1, .2) 
#	-Log(Abs( 1- (Tanh(u*i + v) * .5 + .5)

    r("dist-spec", "dist-spec", "psitest.log" , """
	u ~ Normal(0, 100) + v ~ Normal(0, 100) 
	""",
	psi_str("i", "t"))


s("data-spec psitest.log 1 1 / test.dat .")

pl= libplot.Plotter()
pl.openpl()
pl.pencolorname("red")
pl.fillcolorname("cyan")
pl.colorname("black")
pl.filltype(1)
pl.linewidth(8)
pl.bgcolorname("yellow")
pl.fspace(-.5, -.5, 299.5, 149.5)

def upto(it):
    s('dist-mc psitest.log %s' % it)
    if 0:
	s('dist-hist v -30 30 40  psitest.log  | graph -TX -X "v %s"' % it)
	s('dist-hist u -30 30 40  psitest.log  | graph -TX -X "u %s"' % it)
	s('dist-hist E 0 30 40  psitest.log  | graph -TX -X "e %s"' % it)
	s('dist-tbl uv psitest.log | graph -TX -X "%s"' % it)

    print "EST"
    s("""dist-est '%s' psitest.log""" % psi_str(.5, 1))
    s("""dist-est '%s' psitest.log""" % psi_str(1, 1))
    s("""dist-est '%s' psitest.log""" % psi_str(2, 1))
    s("""dist-est '%s' psitest.log""" % psi_str(3, 1))

    array = streamtoarray(os.popen("dist-tbl uv psitest.log", "r"))

    pl.erase()

    pl.circle(50,50,50)

    pl.flushpl()


    print array

s("mc-spec psitest.log heatbath hybrid 25 .01 ")
# s("mc-spec psitest.log heatbath metropolis ")

upto(100)
#upto(500)
#upto(1000)
