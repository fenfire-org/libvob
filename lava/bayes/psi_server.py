#(C): Janne V. Kujala

# Classes implementing a trial placement computation server
# using the Psi method 

from Numeric import *
from bayes import *
from psi_model import models, get_psi, prior, getPoint, getModel
from select import *
import sys
from random import shuffle, random, seed, choice

psi_tbl = get_psi()


class Psi:
    def __init__(self, psi_tbl, prior):
        self.psi_tbl = psi_tbl
        self.prior = prior
        self.posterior = None
        self.i = None

    def compute_placement(self):
        assert self.has_prior()

        min_H = 1E300
        i = 0
        for psi in self.psi_tbl:
            (p0, p1, H0, H1), (posterior0, posterior1) = posterior2(self.prior, psi)
            H = p0 * H0 + p1 * H1
            if H < min_H:
                min_H = H
                min_i = i
                self.posterior = (posterior0, posterior1)
            i += 1

        self.prior = None
        self.i = min_i

    def get_placement(self):
        assert self.has_placement()
        p = getPoint(self.i)
        self.i = None
        return p

    def has_prior(self):
        return not self.prior is None

    def has_posterior(self):
        return not self.posterior is None

    def has_placement(self):
        return not self.i is None

    def update_prior(self,res):
        assert self.i is None
        self.prior = self.posterior[res]
        self.posterior = None

class DummyPsi:
    def get_placement(self): return (0,0)
    def has_prior(self): return 0
    def has_placement(self): return 1
    def update_prior(self,res):
        self.prior = 1
        self.posterior = None
    

def blockRandomize(values, trials, sep):
    l = [ v for v in values ]
    shuffle(l)
    while len(l) < trials:
        t = [ v for v in values ]
        shuffle(t)
        while t:
            while t[-1] in l[len(l)-sep:]:
                shuffle(t)
            l.append(t.pop())

    return l[:trials]

def indices(a, v):
    l = []
    for i in range(0, len(a)):
        if a[i] == v: l.append(i)
    return l
                
class PsiServer:
    def __init__(self, instream, outstream, out_mappings, trials, dummytrials, sep = 3):
        self.f_in = instream
        self.f_out = outstream
        self.po = poll()
        self.po.register(instream, POLLIN)
        #self.po.register(outstream, POLLOUT)

        self.psi = [ Psi(psi_tbl, prior) for x in out_mappings ]
        self.psi += [ DummyPsi() for x in out_mappings ]
        self.trials = blockRandomize( range(0, len(out_mappings)), trials, sep)

        # Add dummy trials (with 0 intensity)
        for i in range(0, dummytrials):
            c = i % len(out_mappings)
            j = choice(indices(self.trials, c))
            self.trials[j] += len(out_mappings)

        self.out_mappings = out_mappings + out_mappings

        
        print >> sys.stderr, "Trials:", self.trials
        #print >> sys.stderr, "Outmaps:", self.out_mappings

        self.ind0 = 0
        self.ind1 = 0
        
    def canread(self):
        if (self.f_in.fileno(), POLLIN) in self.po.poll(0):
            return 1
        return 0

    def canwrite(self):
        if (self.f_out.fileno(), POLLOUT) in self.po.poll(0):
            return 1
        return 0
        
    def read(self):
        line = self.f_in.readline()
        if not line:
            print >> sys.stderr, "EOF"
            return 0
        res = int(line[0])
        print >> sys.stderr, "Trial", self.ind0, "result:", line
        
        i = self.trials[ self.ind0 ]
        self.psi[i].update_prior(res)
        self.ind0 += 1

        return self.ind0 < len(self.trials)

    def write(self):
        if self.ind1 >= len(self.trials):
            return 0
        i = self.trials[self.ind1]
        psi = self.psi[i]
        if psi.has_placement():
            p = psi.get_placement()
            self.f_out.write(str(self.out_mappings[i](p)) + "\n")
            self.f_out.flush()
            self.ind1 += 1
            return 1
        else:
            return 0

    def mainloop(self):

        idle = 0
        while 1:
            if idle:
                print >> sys.stderr, "Trials in/out/total:", self.ind0, self.ind1, len(self.trials)
            while self.canread() or idle:
                idle = 0
                if self.read() == 0:
                    return

            if self.write():
                continue

            idle = 1
            for j in range(self.ind1, len(self.trials)):
                i = self.trials[ j ]
                if self.psi[i].has_prior():
                    print >> sys.stderr, "Computing placement for condition", i, "...",
                    sys.stderr.flush()
                    self.psi[i].compute_placement()
                    print >> sys.stderr, self.psi[i].i

                    idle = 0
                    break



#PsiServer(sys.stdin, sys.stdout, [ lambda x,i=i: (i,x) for i in range(0,10) ], 200).mainloop()

        
                
            
