# 
# Copyright (c) 2003, Matti J. Katila
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




import java, vob, string, sys

dbg = 0

class Logger:
    """
    This is a multiplexer for events used in scene.
    Multiplexing is done between user events and events
    read from file.
    
    ---------
    Interface for normal recording operations like start and stop:
    - setRecordFileName
    - playRecord
    - startRecording
    - stopRecording

    -----------
    Interface for tests: easy recording and play with parametring:
    - recorderFunction(filename, lock, scene,
              recordFunction='play'/'record', stopRecordEvent='F11')
    
    --------
    There's also interface for demo framework:
    - mouse(ev)
    - key(stroke)
    """
    def __init__(self):
        self.filename = None
        self.noUserEvents = 0
        self.logging = 0
        self.lock = None
        self.events = []
        self.recordStopEvent = None
    def setRecordFileName(self, filename):
        self.filename = filename

    def playRecord(self, filename, scene, lock=None):
        print "Event recording in file(%s): play" % filename
        self.noUserEvents = 1
        startTime = None
        self.events = []
        file = open(filename, 'r')
        str = file.readline()
        while str != '':
            timeAndEvent = string.split(str, ':')
            if len(timeAndEvent) != 2: return
            time, event = timeAndEvent[0], timeAndEvent[1]
            if startTime==None: startTime = long(time)
            ev = vob.VobMouseEvent.createObjectFromStateStr(event)
            if ev == None: ev = event
            self.events.append( (long(time),ev) )
            str = file.readline()
        file.close()
        record = self.RecordPlayThread(self, scene, self.events, startTime, lock)
        if lock!=None: lock.acquire()
	record.start()
    def startRecording(self, lock=None):
        if self.logging: raise 'Recording already started!'
        if lock: lock.acquire()
        print "Event recording in file(%s): start" % self.filename
        self.logging = 1
        self.startTime = java.lang.System.currentTimeMillis()
        self.events = []
    def stopRecording(self, popLastEvent=1):
        print "Event recording in file(%s): stop" % self.filename
        if popLastEvent:
            self.events.pop(len(self.events)-1)
        self.logging = 0

        file = open(self.filename, 'w')
        for s in self.events:
            time, ev = s
            if isinstance(ev, vob.VobMouseEvent):
                file.write(str(time)+':'+ev.getObjectStateStr()+'\n')
            else: file.write(str(time)+':'+ev+'\n')
        file.close()

    def _add(self, event):
        self.events.append( (java.lang.System.currentTimeMillis(),
                             event) )
        
    class RecordPlayThread(java.lang.Thread):
        def __init__(self, logger, scene, events, startTime, lock=None):
            java.lang.Thread.__init__(self)
            if dbg: print self, logger, scene, events, startTime
            self.logger = logger
            self.scene = scene
            self.events = events
            self.startTime = startTime
            self.lock = lock
        def run(self):
            prevTime = self.startTime
            for ev in self.events:
                time, event = ev
                dTime = time - prevTime
                self.sleep(dTime)
                self._exec(event)
                prevTime = time

            # clean
            self.logger.noUserEvents = 0
            self.events = None
            if self.lock!=None:
                self.lock.release()
        def _exec(self, ev):
            if dbg: print 'event:',ev
            vob.AbstractUpdateManager.freeze()
            if isinstance(ev, vob.VobMouseEvent):
                self.scene.mouse(ev)
            else: self.scene.key(ev)
            vob.AbstractUpdateManager.thaw()

    # -----------------------------------------

    def recorderFunction(self, filename, scene, lock, 
                         functionOperation, stopRecordEvent='F11'):
        if functionOperation == 'play':
            self.playRecord(filename, scene, lock)
            return
        elif functionOperation == 'record':
            print 'Recording will be stop with:',stopRecordEvent
            self.recordStopEvent = stopRecordEvent
            self.setRecordFileName(filename)
            self.startRecording(lock)
            self.lock = lock
        else: raise 'Operation not known(\'play\'/\'record\')!',functionOperation


    # -----------------------------------------

    def mouse(self, scene, ev):
        if self.noUserEvents: return
        if self.logging:
            self._add(ev)
        self.scene = scene
        scene.mouse(ev)
        
    def key(self, scene, s):
        if s == self.recordStopEvent:
            self.noUserEvents = 0
            self.stopRecording(popLastEvent=0)
            if self.lock: self.lock.release()
            return
        if self.noUserEvents: return
        if self.logging:
            self._add(s)
        self.scene = scene
        scene.key(s)
        
