# 
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


# Demo keystroke handling

from __future__ import nested_scopes
from org.nongnu.libvob import AbstractUpdateManager
import re

# For the compiled regexp class
somere = re.compile("[ab]")

dbg = 0

class KeyPresses:
    """An object that can function as the ``key`` method
    of a gl demo.

    """
    def __init__(self, scene, *args):
	self.scene = scene
	print args
	self.things = args
	self.keymap = {}
	self.res = []
	if dbg: print "KEYMAP: things = ",self.things
	for t in self.things:
	    for k in t.getKeys():
		if dbg: print "KEY: ",k
		self.keymap[k[0]] = k[1]
	    self.res.extend(t.getREs())
	    t.prepare(scene)

    def getdoc(self, scene):
        doc = ""
        for t in self.things:
            doc += "\n" + t.getdoc(scene)
        return doc
            
            
    def __call__(self, key):
	if self.keymap.has_key(key):
	    return self.keymap[key](self.scene,key)
	else:
	    for r in self.res:
		if r[0].match(key):
		    return r[1](self.scene, key)
	print "No such key known: ",key,"Here:",self.keymap,self.res
    
class _NoAnimation:
    def __init__(self, func): self.func = func
    def __call__(self, *args):
	print "NOANIMATION CALL"
	AbstractUpdateManager.setNoAnimation()
	self.func(*args)

class _Presses:
    def __init__(self, opts, *keys):
	if dbg: print "PRESSES: ",keys
	self.keys = keys
	if opts.get("noAnimation", 0):
    	    self.keys = [
		(k[0], _NoAnimation(k[1])) for k in self.keys]
    def getKeys(self):
	return [k for k in self.keys if k[0] != None and not isinstance(k[0], somere.__class__)]
	    
    def getREs(self):
	"""Get the regular expressions this object
	wants to catch.
	"""
	return [k for k in self.keys if k[0] != None and isinstance(k[0], somere.__class__)]
    def keyStrings(self):
	list = []
	for k in self.keys:
	    if k[0] == None: continue
	    if isinstance(k[0], somere.__class__):
		list.append(k[0].pattern)
	    else:
		list.append(k[0])
	return ", ".join(list)

    def getdoc(self, scene):
        return "XXX an undocumented _Presses object"


class Action(_Presses):
    """A key used to fire an action.
    """
    def __init__(self, description, key, func, **opts):
	_Presses.__init__(self, opts, 
	   (key, self.act),
	    )
	self.func = func
        self.description = description

    def getdoc(self, scene):
        return self.keyStrings() + ": " + self.description

    def prepare(self, scene):
	pass

    def act(self, scene, key):
	self.func(scene, key)


class Toggle(_Presses):
    """A key used to toggle a feature on or off.
    """
    def __init__(self, attr, default, description, key, **opts):
	_Presses.__init__(self, opts, 
	    (key, self.toggle)
	    )
	self.attr = attr
	self.default = default
        self.description = description

    def getdoc(self, scene):
	return "%s: Toggle %s (%s)" % (
            self.keyStrings(), self.description, getattr(scene, self.attr))

    def prepare(self, scene):
	setattr(scene, self.attr, self.default)

    def toggle(self, scene, key):
	setattr(scene, self.attr, 1 - getattr(scene, self.attr))

class ListIndex(_Presses):
    """A key or a pair of keys used to move on a list of alternatives.
    """
    def __init__(self, attr, listattr, default, description, keydown, keyup, **opts):
	_Presses.__init__(self, opts, 
	    (keydown , lambda *args: self.move(-1, *args)),
	    (keyup , lambda *args: self.move(1, *args)),
	    )
	self.attr = attr
	self.listattr = listattr
	self.default = default
        self.description = description

    def getdoc(self, scene):
	return "%s: Select %s (%s)" % (
            self.keyStrings(), self.description, getattr(scene, self.attr))

    def prepare(self, scene):
	setattr(scene, self.attr, self.default)

    def move(self, dir, scene, key):
	v = getattr(scene, self.attr)
	l = getattr(scene, self.listattr)
	v += dir
	v = v % len(l)
	setattr(scene, self.attr, v)


class _Slide(_Presses):
    """A pair of keys used to move a log slider up&down.
    """
    def __init__(self, attr, default, description, keydown, keyup, **opts):
	_Presses.__init__(self, opts, 
	    (keydown , lambda *args: self.move(-1, *args)),
	    (keyup , lambda *args: self.move(1, *args)),
	    )
	self.attr = attr
	self.default = default
        self.description = description

    def getdoc(self, scene):
	return "%s: Adjust %s (%.4G)" % (
            self.keyStrings(), self.description,
            getattr(scene, self.attr))

    def prepare(self, scene):
	setattr(scene, self.attr, self.default)

class SlideLog(_Slide):
    def __init__(*args):
	_Slide.__init__(*args)

    def move(self, dir, scene, key):
	v = getattr(scene, self.attr)
	if dir > 0:
	    v *= 1.03
	else:
	    v /= 1.03
	print "SlideLog: ",v
	setattr(scene, self.attr, v)


class SlideLin(_Slide):
    def __init__(self, attr, default, delta, description, keydown, keyup, **opts):
	_Slide.__init__(self, attr, default, description, keydown, keyup, **opts)
	self.delta = delta

    def move(self, dir, scene, key):
	v = getattr(scene, self.attr)
	if dir > 0:
	    v += self.delta
	else:
	    v -= self.delta
	print "SlideLin: ",v
	setattr(scene, self.attr, v)







