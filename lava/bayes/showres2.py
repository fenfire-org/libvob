import sys

lines = sys.stdin.readlines()

prevline = ""
for line in lines:
    x = line.split()
    if len(x) < 14: continue
    if x[2] == x[8] and x[5] == x[11]:
        sys.stdout.write(prevline)
        sys.stdout.write(line)
        sys.stdout.write("\n")
    prevline = line
    
