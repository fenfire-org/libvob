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


# Utilities for using NVIDIA's Cg compiler.

# While Cg is not part of our defined platform, it
# is still a useful tool for development.

import tempfile
import os

#_cgc = "/BIG/Cg/usr/bin/cgc"
_cgc = "cgc"

def compile(prog, profile):
    """Compile a Cg program and return an ARB vertex program.
    """
    source = tempfile.mktemp()
    dest = tempfile.mktemp()

    f = open(source, "w")
    f.write(prog)
    f.close()

    os.system("%s -longprogs -profile %s <%s -o %s " % 
	(_cgc, profile, source, dest))

    code = open(dest).read()
    print "Got code: ",code

    # Then, do the horrible...

    os.remove(source)
    os.remove(dest)
    return code
