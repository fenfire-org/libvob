#! /bin/sh
grep -v -- --- | cut -c9- | tr LR 01 | awk '{print (1+(2*$1-1)*(2*$5-1))/2 " " $2 " " $3 " " $4 " " $5 }' | python stats.py 3,4 1,2
