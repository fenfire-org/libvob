# 
# Copyright (c) 2003, Asko Soukka
# 
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
# Public License along with Fenfire; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 

"""
Quick tool to generate JPEG texture file. Actually, creates
at first PNGs, but is hacked to convert them into JPEGs to
fit better for WWW usage.
"""

__do_not_delete__ = 1

import sys
import os
import time

from java.lang import System,Runnable,Throwable
System.setProperty("vob.api", "gl")

sys.path.insert(0, ".")

import java

import getopt

import vob

from org.nongnu.libvob.gl import GL, GLRen, GLCache
from org.nongnu.libvob.vobs import SolidBackdropVob
from vob.putil import dbg, saveanim

# catching parameters froms the command line
try: sys.argv.remove('--loop'); loop = 1
except ValueError: loop = 0

try: sys.argv.remove('--jpeg'); jpeg = 1
except ValueError: jpeg = 0

notify = None

print "ARGV:",sys.argv
print "DBG:",dbg.short,dbg.long+['notify='],dbg.all
opts, args = getopt.getopt(sys.argv[1:], 
	dbg.short, 
	dbg.long+['notify='])
for o,a in opts:
    print "Opt: ",o,a
    if o == '--notify':
        notify = a
    if o in dbg.all:
	dbg.option(o,a)

passmask = [1,1,1,1]
basedir = './' # must be ended by backslash


def listdir(path, extensions):
    """
    Returns all files with specific
    extensions under path. Nonrecursive.
    """
    files = os.listdir(path)
    files = [f for f in files if extensions.count(f.split('.')[-1]) > 0]
    return files

def genBgFileFromSeed(w, vs, seed, jpeg=0, scale=1):
    """
    Generates a background texture into filename constructed
    from the seed. Seed is some 64bit integer value.
    ThePaperMill must loaded and be set to be global.

    Returns the filename with absolute path of the generated file.
    """
    scalePostfix = ''
    if scale != 1: scalePostfix = '-%sx' % (scale)

    size = vs.getSize()

    # for Java to accept Python Long
    if seed > 0x7FFFFFFFFFFFFFFFL:
        lseed = seed - 0x10000000000000000L
    else: lseed = seed

    pap = ThePaperMill().getPaper(lseed, passmask=passmask, vecs=[[.5,0],[0, .5]])
    pq = GLRen.createPaperQuad(pap, -0.5, -0.5, 0.5, 0.5, 0)
    cs = vs.coords.affine(0, 1, 0, 0, 768*scale, 0, 0, 768*scale)
    vs.matcher.add(cs, "tex")
    vs.put(SolidBackdropVob(java.awt.Color(0,0,0)))
    vs.map.put(pq, cs, 0)
    w.renderStill(vs, 0)

    if (jpeg):
        print 'Saving background texture into %s%s-paper.gen.jpg.' % (str(seed), scalePostfix)
        saveanim.saveframe('bgfile.tmp', w, 0, 0, 384*scale, 384*scale)   
        os.system("convert -quality 95 %s %s" \
                  % ('bgfile.tmp', basedir+str(seed)+'%s-paper.gen.jpg' % (scalePostfix)))
        os.system("rm bgfile.tmp")
        return os.path.abspath(basedir+str(seed)+'%s-paper.gen.jpg' % (scalePostfix))
    else:
        print 'Saving background texture into %s%s-paper.gen.png.' % (str(seed), scalePostfix)
        saveanim.saveframe(basedir+str(seed)+'%s-paper.gen.png' % (scalePostfix),
                           w, 0, 0, 384*scale, 384*scale)   
        return os.path.abspath(basedir+str(seed)+'%s-paper.gen.png' % (scalePostfix))

class Main(Runnable):
    def run(self):
        global ThePaperMill, w
        from vob.paper.papermill import ThePaperMill

        # w = gfxapi.createWindow()

        # pbuffer
        w = gfxapi.createStableOffscreen(1536, 1536)

        #w.setLocation(0,0,384,384)
        vs = w.createVobScene()
        print "Working directory:", basedir

        if not loop:
            for seed in sys.argv[1:]:
                genBgFileFromSeed(w, vs, long(seed), jpeg)
        else:
            print """
Entering into looping server mode. Requests will be read from foo.request
files within directory %s. Please, replace foo with some 64bit integer
value.
""" % (basedir)
            sleep = 0
            while 1:
                requests = listdir(basedir, 'request')
                tmp = open(basedir+'bgfilegen.watchdog', 'w')
                tmp.close()
                if len(requests) > 0:
                    sleep = 0
                    for seed in requests:
                        if seed.find('-') != -1:
                            id = seed.split('.')[0]
                            scale = id.split('-')[1].split('x')[0]
                            filepath =  genBgFileFromSeed(w,
                                                          vs,
                                                          long(id.split('-')[0]),
                                                          jpeg, int(scale))
                        else:
                            filepath =  genBgFileFromSeed(w,
                                                          vs,
                                                          long(seed.split('.')[0]),
                                                          jpeg)
                        if notify:
                            os.system(notify + ' ' + filepath \
                                      + ' ' + seed.split('.')[0])
                        os.system('rm '+basedir+seed)
                else:
                    if not sleep:
                        print time.strftime('%y/%m/%d %H:%M:%S'), \
                              "No more requests, sleeping..."
                        sleep = 1
                    time.sleep(1)
                if os.path.isfile(basedir+'stop_bgfilegen'):
                    print time.strftime('%y/%m/%d %H:%M:%S'), "Exiting..."
                    break

        System.exit(0)

gfxapi = vob.GraphicsAPI.getInstance()
gfxapi.startUpdateManager(Main())
