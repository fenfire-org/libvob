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


# Module gzz.control.commander

from __future__ import nested_scopes

import java.awt
#from java.awt import *
from pawt import GridBag
import code
import codeop
import sys
import traceback

class Commander:
    def __init__(self, gl, phys):
        self.gl = gl
        self.phys = phys
        self.interp = code.InteractiveInterpreter()

    def go(self, text):
        try:
            try:
                fun = codeop.compile_command("___result = "+text,
                   "<Gzz commander input>", "single")
            except SyntaxError:
                fun = codeop.compile_command(text,
                    "<Gzz commander input>", "single")
            exec fun in self.gl
            res = self.gl.get("___result", "")
            self.phys.output(repr(res))
        except:
            typ, val, tra = sys.exc_info()
            self.phys.output(repr((typ, val, tra)))
            l = traceback.format_list(traceback.extract_tb(tra))
            apply(self.phys.output, l)

class AwtCommander:
    def __init__(self, gl):
        self.commander=Commander(gl, self)
        self.frame = java.awt.Frame("LibVob commander")
        #self.frame = Frame("Gzz commander",
        #    windowClosing=lambda ev: self.frame.dispose())
        g = GridBag(self.frame)
        self.outputArea = java.awt.TextArea("Welcome to LibVob commander\n"+
            "Using Jython "+sys.version+"\n>>>", 80, 50)
        self.outputArea.editable = 0
        self.inputArea = java.awt.TextField(80,
            actionPerformed = self.execute)
        self.execButton = java.awt.Button("Go",
            actionPerformed = self.execute)
        self.toggle = java.awt.Checkbox("Updateloop enabled",
            itemStateChanged = self.toggleUpd)

        g.add(self.outputArea,
            gridx=0, gridy=0, 
            gridwidth=1, gridheight=10,
            fill="BOTH",
            weightx=100, weighty=100)

        g.add(self.inputArea, 
            gridx=0, gridy="RELATIVE", 
            gridwidth=1, gridheight=1,
            fill="HORIZONTAL",
            weightx=100, weighty=0)

        g.add(self.toggle,
            gridx=1, gridy=0,
            gridwidth=1, gridheight=1,
            fill="BOTH",
            weightx=0, weighty=0)

        g.add(self.execButton,
            gridx=1, gridy=10,
            gridwidth=1, gridheight=1,
            fill="BOTH",
            weightx=0, weighty=0)

        self.frame.setSize(600,600)
        self.frame.setLocation(200,200)
        self.frame.show()

    def execute(self, ev):
        self.output(self.inputArea.text)
        self.commander.go(self.inputArea.text)
        self.outputArea.append(">>>")

    def toggleUpd(self, ev):
        print "TOGGLE UPD", ev

    def output(self, *args):
        for a in args:
            self.outputArea.append(a)
        self.outputArea.append("\n")

# AwtCommander(globals())
