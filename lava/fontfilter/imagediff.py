import util
import sys

x = util.load(sys.argv[1], gamma = 1.0)
y = util.load(sys.argv[2], gamma = 1.0)

util.save(.5 + x - y, ",,diff.png", gamma = 1.0)

d = util.flat(x - y)

print "Diff: ", min(d), "...", max(d)
