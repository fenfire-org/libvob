# 
# Copyright (c) 2003, Tuomas J. Lukka
# 
# This file is part of Gzz.
# 
# Gzz is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Gzz is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Gzz; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 
# 

"""Abstract away the PriorityQueue interface invariants testing.
"""

import java, vob
from vob.util.faildefs import *

def setUp_PriorityQueue(queueMaker0):
    """Setup for priority queue testing.

    QueueMaker = a function that returns a new, empty priorityqueue.
    """
    global queueMaker
    queueMaker = queueMaker0

def test_PriorityQueue_BasicSort():
    q = queueMaker()
    q.add("A", 5)
    q.add("B", 3)
    q.add("C", 8)

    failUnlessEqual(q.getAndRemoveLowest(), "B")
    failUnlessEqual(q.getAndRemoveLowest(), "A")
    failUnlessEqual(q.getAndRemoveLowest(), "C")
    failUnlessEqual(q.getAndRemoveLowest(), None)
    failUnlessEqual(q.getAndRemoveLowest(), None)

def test_PriorityQueue_ResetPriority():
    """Test the changing of the priority of an existing job
    """

    q = queueMaker()

    A, B, C, D, E = "A", "B", "C", "D", "E"

    q.add(A, 5)
    q.add(B, 6)
    q.add(C, 7)
    q.add(D, 8)

    failUnlessEqual(q.getAndRemoveLowest(), "A")

    q.add(A, 3)

    failUnlessEqual(q.getAndRemoveLowest(), "A")

    q.add(A, 9)

    failUnlessEqual(q.getAndRemoveLowest(), "B")

    q.add(A, 5)
    q.add(C, 12)  # no effect!

    failUnlessEqual(q.getAndRemoveLowest(), "A")
    failUnlessEqual(q.getAndRemoveLowest(), "C")
    failUnlessEqual(q.getAndRemoveLowest(), "D")
    failUnlessEqual(q.getAndRemoveLowest(), None)

def test_PriorityQueue_remove():
    """Test that removing jobs works right
    """
    q = queueMaker()

    A, B, C, D, E = "A", "B", "C", "D", "E"


    q.add(B, 6)
    q.add(A, 5)
    q.add(D, 8)
    q.add(C, 7)
    q.add(E, 9)

    failUnlessEqual(q.getAndRemoveLowest(), "A")
    q.remove(A)
    q.remove(C)
    q.remove(E)
    failUnlessEqual(q.getAndRemoveLowest(), "B")
    failUnlessEqual(q.getAndRemoveLowest(), "D")
    failUnlessEqual(q.getAndRemoveLowest(), None)
    


