# 
# Copyright (c) 2003, Tuomas J. Lukka
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


"""Benchmarking for libvob.

"""


import sys
vob = sys.modules["vob"]

import sys, types, traceback

import java

def dict(d):
    n = {}
    for k in d.keys():
	n[k] = d[k]
    return n

def _flattenArgLists(args, key):
    res = []
    for v in args[key]:
	d = dict(args)
	d[key] = v
	res.extend(_argLists(d))
    return res

def _argLists(args):
    k = args.keys()
    k.sort()
    for key in k:
	if type(args[key]) is types.TupleType:
	    return _flattenArgLists(args, key)
    return [args]

def runVSBench(sceneFunc, args):
    """Run the given benchmark with the arguments.

    The sceneFunc is assumed to take parameters
    of the form (vs, param=value, param2=value2),
    i.e. first a vobscene and then parameters with
    default values.

    The args parameter is a mapping from parameter
    name to a set of values to use for that parameter.
    """

    win = vob.GraphicsAPI.getInstance().createWindow()
    vob.putil.demowindow.w = win
    win.setLocation(0, 0, 1024, 768)

    sx = 0
    while sx < 1024:
	vs = win.createVobScene()
	sx = vs.size.width

    outfile = open("benchdata","w")

    for ar in _argLists(args):
	vs = win.createVobScene()
	ou = sceneFunc(vs, **ar)
	print ar
	total = 0
	iters = 1
	while total < 1.7 and iters < 4:
	    iters *= 2
	    java.lang.System.gc()
	    java.lang.System.gc()
	    total = win.timeRender(vs, 1, iters) 
	    ms = total / iters * 1000
	    # print "Now: ",iters,total,ms
	print ou
	print ms, "ms   with ",iters," in ",total
	outfile.write("%s %s\n" % (ou, ms))
	print 1000.0 / ms,"per second\n"

    outfile.close()

def runPureBench(func, args):
    for ar in _argLists(args):
	# Precompile for JIT
	func(1, **ar)
	func(1, **ar)
	func(1, **ar)

	total = 0
	iters = 1
	while total < 3.0:
	    iters *= 2
	    (total, str) = func(iters, **ar)
	    ms = total * 1. / iters * 1000
	print str
	print ar
	print ms, "ms   with ",iters," in ",total
	print 1000.0 / ms,"per second\n"
	    

import getopt
opts, args = getopt.getopt(sys.argv[1:], 
	vob.putil.dbg.short, 
	vob.putil.dbg.long)
for o,a in opts:
    print "Opt: ",o,a
    vob.putil.dbg.option(o,a)

# testmod = "bench.vob.text.overhead"
# testmod = "bench.vob.paper.dice"
# testmod = "bench.vob.trans.trivial"
testmod = args[0]
print "TESTMOD: ",testmod



class Starter(java.lang.Runnable):
    def run(self):
	exec "import "+testmod+"\ntestmod = "+testmod+"\n"
	try:
	    if hasattr(testmod, "benchScene"):
		runVSBench(testmod.benchScene, testmod.args)
	    else:
		runPureBench(testmod.bench, testmod.args)
	except:
	    typ, val, tra = sys.exc_info()
	    print (repr((typ, val)))
	    print str(val)
	    l = traceback.format_list(traceback.extract_tb(tra))
	    print "\n".join(l)
	    val.printStackTrace()
	print "Calling system.exit"
	java.lang.System.exit(0)
vob.GraphicsAPI.getInstance().startUpdateManager(Starter())


# 4 cases:
# run(None, { "a": (1,2), "b": (3,4), "c": 5 })
