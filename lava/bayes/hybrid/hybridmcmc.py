#(c) Tuomas J. Lukka 

"""A hybrid MCMC driver, using the Horowitz version
of the algorithm.
"""

from RandomArray import *
import math
from Numeric import *
import os

class HybridMCMC_Horowitz:
    def __init__(self, nparams, ninst, function):
	"""

	nparams -- number of parameters to function
	ninst -- number of instances to run in parallel
	function -- function that returns E = -log(p) and derivatives.
	            It is given an array (ninst, nparams)
		    and must return array (ninst) of values
		    and array (ninst, nparams) of derivatives.
	"""
	self.nparams = nparams
	self.ninst = ninst
	self.function = function

	self.epsilon = .005
	self.alpha = .99
    def randomize(self):
	"""Calculate new random initial values.
	Uses uniform (0,1) distribution -- bad.
	"""
	self.x = .1 * random((self.ninst, self.nparams)) 
	self.p = .1 * random((self.ninst, self.nparams))
	self.fv, self.fd = self.function(self.x)
	self.Hv = self.fv + .5 * sum(self.p**2, -1)

    def step(self):
	"""Do a single step of the algorithm.
	"""
	really = 1
	if really:
	    # print "STATE:",self.x,self.p,self.fv,self.fd,self.Hv
	    # Calculate proposal - leapfrog
	    phalf = self.p - self.epsilon/2 * self.fd
	    xprop = self.x + self.epsilon * phalf
	    nfv, nfd = self.function(xprop)
	    # print "XPF:",xprop,nfv,nfd
	    pprop = phalf - self.epsilon/2 * nfd
	    # print "X",xprop[:,0],pprop[:,0]
	    pprop = -pprop

	else:
	    pprop = 0 * self.p
	    xprop = self.x + .1 * standard_normal(shape(self.x))
	    nfv, nfd = self.function(xprop)

	# Calculate new H
	Hvprop = nfv + .5 * sum(pprop**2, -1)

	acceptp = exp(-(Hvprop - self.Hv))
	accept = (acceptp >= random(shape(acceptp)))

	# print "H:",nfv, self.Hv,Hvprop
	# print "HV", self.Hv, Hvprop
	# print "AC:", accept

	# Accept 
	def ac(old, new):
	    # print "ACNOW:",old,new
	    for i in range(0, shape(accept)[0]):
		# print old[i],new[i]
		if accept[i]:
		    old[i] = new[i]
	    
	ac(self.x, xprop)
	ac(self.p, pprop)
	ac(self.fv, nfv)
	ac(self.fd, nfd)
	ac(self.Hv, Hvprop)

	if really:
	    # Adjust pprop for new stochastically adjusted values
	    #print pprop[:,0]
	    delta = standard_normal(shape(self.p))
	    # print delta
	    self.p = self.alpha * pprop + \
		    sqrt(1-self.alpha**2) * delta
	    #print pprop[:,0]

	if really:
	    # Unconditionally reverse momenta
	    self.p = -self.p

	
if __name__ == '__main__':
    # Run in a unit gaussian.
    if 0:
	sigma = 1
	def f(x):
	    # return (((x[...,0]-.4) / sigma)**2/2,  ((x - .4)/sigma**2))
	    return ((x[...,0]**2)/2,  x)
	hybrid = HybridMCMC_Horowitz(1, 5, f)
	hybrid.randomize()
	csum = array(hybrid.x)
	csumsq = array(hybrid.x)
	csummed = 0
	nsum = array(hybrid.x)
	nsumsq = array(hybrid.x)
	graph = os.popen("graph -TX", "w")
	graph2 = os.popen("graph -TX", "w")
	for i in range(0,30000):
	    hybrid.step()
	    # print hybrid.x[:,0], hybrid.p[:,0],hybrid.Hv

	    graph.write("%s %s\n" % (
		hybrid.x[0][0] + .00 * random(), 
		hybrid.p[0][0] + .00 * random()))
	    graph2.write("%s %s\n" % (
		i, hybrid.x[0][0]))

	    if i > 10000:
		csum += hybrid.x
		csumsq += hybrid.x**2
		#nor = standard_normal(shape(nsum))
		#nsum += nor
		#nsumsq += nor ** 2
		csummed += 1

	csum /= csummed
	csumsq /= csummed
	#nsum /= csummed
	#nsumsq /= csummed
	print "RAW:",csumsq-csum*csum
	print csum, sqrt(csumsq - csum*csum) 
	#print nsum, sqrt(nsumsq - nsum*nsum) 
	graph.close()
	graph2.close()
    if 1:
	def f(x):
	    return (.1*x[...,0]**2 + 5 * x[...,0]**2 * x[...,1]**2 + .1*x[...,1]**2,
		    transpose(
			array([
			    2*x[...,0] * (.1 + 5 * x[...,1]**2),
			    2*x[...,1] * (.1 + 5 * x[...,0]**2),
			    ])))
	hybrid = HybridMCMC_Horowitz(2, 20, f)
	hybrid.randomize()

	graph = os.popen("graph -x -8 8 -y -8 8 -TX -S 1 -m 0 ", "w")
	for i in range(0,1000000):
	    hybrid.step()
	    for j in range(0,shape(hybrid.x)[0]):
		graph.write("%s %s\n" % (
		    hybrid.x[j][0] + .00 * random(), 
		    hybrid.x[j][1] + .00 * random()))
	graph.close()

