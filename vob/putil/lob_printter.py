# (c): Matti J. Katila

"""
Util to print out layout object visual output for documentation.
"""

import sys, org, java

print 'asdf'

file = sys.argv[2]

class Scene:
    print 'foo', sys.argv[2:]

    def lob(self):

        c = java.lang.Class.forName(file)
        constructor = c.getConstructors()[0]
        instance = constructor.newInstance([])
        return instance.getLob()
        
        #return vob.lob.Components.label("foo")

    def scene(self, scene):
        _ = scene
        matcher = vob.impl.IndexedVobMatcher()
        scene = vob.VobScene(_.map, _.coords, matcher,
                             _.gfxapi, _.window, _.size)
        scene.put(background((1,1,1)))

        
        #vob.lob.PoolContext.enter()
        try:
            l = self.lob()
            l = l.layout(scene.size.width, scene.size.height)
            l.render(scene, 0, 0, 1, 1)
        finally: pass
        #vob.lob.PoolContext.exit()

        global s
        s = scene.size

        class T(java.lang.Thread):
            def run(self):
                import time, string
                time.sleep(1)
                global file
                file = string.replace(file, '.', '/')
                vob.putil.saveimage.save(file+'.png',
                                         w.readPixels(0, 0, s.width, s.height),
                                         s.width, s.height
                                         )
                sys.exit(0)
        T().start()
        return scene
