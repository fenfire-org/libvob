import sys

lines = sys.stdin.readlines()

for line in lines:
    x = line.split()
    if len(x) < 12: continue

    x = map(float, x)

    col0 = x[1]
    col1 = x[4]
    col2 = x[7]
    col3 = x[10]

    res = x[13]

    if col0 == col2 and col1 == col3:
        print >> sys.stderr, line,

    col02 = 0.5 * (col0 + col2)
    col13 = 0.5 * (col1 + col3)

    #if abs(col02 - col13) < 1E-5:
    #    continue

    m = 1
    #m = .25
    col0 = col02 + (col0 - col02) * m
    col2 = col02 + (col2 - col02) * m
    col1 = col13 + (col1 - col13) * m
    col3 = col13 + (col3 - col13) * m
    


    if res:
        print "#m=2,S=4"
    else:
        print "#m=1,S=16"

    print col0,col1,col2,col3

    
    
