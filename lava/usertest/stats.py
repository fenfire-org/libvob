# 
# Copyright (c) 2003, Janne V. Kujala
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

# usage: stats.py <group> <sum>
# where <group> is comma-separated list of 1-based field indices for
# fields to group by and <sum> gives similarly the list of fields
# to compute statistics for

from sys import stdin, stdout, argv
from math import sqrt

group = [ int(i)-1 for i in argv[1].split(",") ]
sum = [ int(i)-1 for i in argv[2].split(",") ]


# split into groups
set = {}
while 1:
    line = stdin.readline()
    if not line: break

    fields = line.split()

    key = tuple([ int(fields[g]) for g in group ])
    if not set.has_key(key):
        set[key] = []

    set[key].append( fields )


def stats(data):
    n = 0
    sum = 0
    sum2 = 0
    for x in data:
        sum += float(x)
        sum2 += float(x) * float(x)
        n += 1

    return n, sum / n, sqrt(sum2 / n - (sum / n)**2)


keys = set.keys()
keys.sort()

l = 0
for key in keys:
    m = len(str(key))
    if m > l: l = m

for key in keys:
    line = "%-*s%4s:" % (l, key, len(set[key]))

    for s in sum:
        n, avg, sd = stats([ f[s] for f in set[key] ])
        line += "\t%8.4G%8.4G" % (avg, sd / sqrt(n))

    print line
