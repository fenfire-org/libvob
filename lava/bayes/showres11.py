import sys
from color.spaces import *

lines = sys.stdin.readlines()

for line in lines:
    x = line.split()
    if len(x) < 12: continue

    x = map(float, x)

    col0 = x[1:4]
    col1 = x[4:7]
    col2 = x[7:10]

    res = x[10]

    if col0 == col1:
        print >> sys.stderr, line,

    yst0,yst1,yst2 = map(RGBtoYST, [col0,col1,col2])

    if res:
        print "#m=2,S=4"
    else:
        print "#m=1,S=16"


    m = .5

    print yst0[1]+m*yst2[1],yst0[2]+m*yst2[2]
    print yst1[1]+m*yst2[1],yst1[2]+m*yst2[2]

    
    
