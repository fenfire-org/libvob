#
# Copyright (c) 2002, Benja Fallenstein and Tuomas J. Lukka
# 
# This file is part of Fenfire.
# 
# Fenfire is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Fenfire is distributed in the hope that it will be useful, but WITHOUT
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

def failUnlessRaises(excClass, callableObj, *args, **kwargs):
    try: callableObj(*args, **kwargs)
    except excClass: pass
    else: assert 0
def failUnlessApprox(delta, first, second, msg = None):
    failUnless(abs(first-second) <= delta, str((delta, first, second, msg)))
def failIfApprox(delta, first, second, msg = None):
    failIf(abs(first-second) <= delta, msg)
def failUnlessEqual(first, second, msg = None):
    assert first == second, (first, second, msg)
def failIfEqual(first, second, msg = None):
    assert first != second, (first, second, msg)
def failIf(expr, msg = None):
    assert not expr, msg
def failUnless(expr, msg = None):
    assert expr, msg
def fail(msg = None):
    assert 0, msg

