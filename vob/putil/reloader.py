# 
# Copyright (c) 2002, Janne Kujala and Tuomas J. Lukka
# Copyright (c) 2003, Tuomas J. Lukka
# 
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
# 


import sys

def reloadModules():
    """Force reload of all gfx.* modules.
    """
    
    mods = sys.modules.keys()
    mods.sort()

    # XXX: simply deleting the modules forces a reload,
    # but some modules may want to cache data in globals over reloads
    # so we check for a "__do_not_delete__" directive.
    # Note: such modules must reload dependencies themselves
    
#    for modname in mods:
#	mod = sys.modules[modname]
#	if hasattr(mod, "__do_not_delete__"):
#	    print "Reloading", modname
#	    try:
#		reload(mod)
#	    except:
#		print "Warning: reload failed"
	    
    for modname in mods:
	mod = sys.modules[modname]
	if not hasattr(mod, "__do_not_delete__"):
	    # if modname[:4] == "gfx." and hasattr(mod, "__file__"):
	    if hasattr(mod, "__file__"):
		# Special problem: putil only contains python classes
		# and deleting them makes them nonreloadable.
		if (modname == "org.nongnu.libvob.putil" or
		    modname == "org.nongnu.libvob" or
		    modname == "org.nongnu" or
		    modname == "org.fenfire" or
		    modname == "org.fenfire.view" or
		    modname == "org.fenfire.view.buoy" or
		    modname == "org"):
		    continue
		print "Deleting", modname
		del sys.modules[modname]


    print sys.modules

#    del sys.modules["org.nongnu.libvob.putil"]

    # XXX: need to reload twice so that "from foo import bar"
    # in a reloaded module can get the newer version even if
    # foo is later in the list and has not yet been reloaded
    #for iter in range(0,2):
    #    for mod in mods:
    #        if mod[:4] == "gfx." and hasattr(sys.modules[mod], "__file__"):
    #            print "Reloading", mod
    #            reload(sys.modules[mod])

