Date: Mon, 17 Mar 2003 21:37:19 +0200
From: Tuomas Lukka <lukka@mit.jyu.fi>
To: gzz developers list <gzz-dev@mail.freesoftware.fsf.org>
Subject: [Gzz] Benchmark experiments: what to do and what not



Ok, some things to point out to Matti and others about
how to design benchmark experiments to find out bottlenecks,
focused on OpenGL rendering.

1) Create a *SINGLE VOBSCENE* that shows the problem

2) Render it thousands of times and measure the time, in timeRender().

This is the only way to get really meaningful results.
The reasons are:

        - There's a LOT of noise in computers, due to other processes,
          other threads, cache, ...

                -> need to repeat a lot of times to average out

        - We don't want to measure vobscene generation + rendering
          Measuring some composite like that is ridiculous.
          You must find out *SEPARATELY* the time to generate it
          and the time to render it. NEVER measure both and assume
          that delays occur in one of them.

                -> generate the vobscene and then time the thousands
                   of renders

                -> measure the time to generate 1000 times the vobscene
                   (without any rendering)

        - We want a meaningful measure

                -> don't measure an animation from one place to another

                   The scene changes inbetween and this means that the
                   noise mentioned above will play a greater role.

                   Also, any change to the rendering code will change
                   the actual frames rendered and invalidate comparisons
                   to the earlier times.

                   Define *ONE* *SINGLE* *SCENE* that is slower to
                   either generate or render than it should be.

After this, vary parameters in the one scene and little by little,
isolate what the bottleneck in it is.

Then, make a simple benchmark that has just the bottleneck in one
scene. Again, using timeRender().

Once we have this, we can really start working on speeding up the
code. Anything less and we'll be shooting around blindly.

        Tuomas




Small example by Matti(not tested, though)
::

    from org.nongnu.libvob import AbstractUpdateManager 

    # Draw n letter vobs and benchmark it by pressing Ctrl-b
    class SmallScene:
    	def __init__(self):
            self.count = 0
    	def scene(self, vs):
	    bgcolor = (0.6, 0.7, 0.8)
            putnoc(vs, background(bgcolor))

	    alph = 'abcdefghijklmnopqrstuvwxyz'
	    r = java.util.Random()
	    size = vs.getSize()

	    for i in range(0, self.count):
	        cs = vs.translateCS(0, 'TEXT'+str(count),
     	                            r.nextFloat()*size.width, 
                                    r.nextFloat()*size.height)
                putText(vs, cs, alph[(i%len(alph)], color=(0,0,0), h=14, y=10)
        def benchmark(self):
            for self.count in range(0,801,100):
                vs = w.createVobScene()
                self.scene(vs)

	 	# if need to see what happens
                AbstractUpdateManager.chg()

		# render time with 1000 iterations
                print self.count,' letter vobs, ',\
                      'render time: ', w.timeRender(vs, 1000)

		# render time to create 1000 vobscenes
		time = System.currentTimeMillis()
		for i in range(0,1000):
	            vs = w.createVobScene()
        	    self.scene(vs)
		print self.count,' letter vobs, ',\
		      'vobscene creation time: ', \
		      System.currentTimeMillis() - time 
        def key(self, k):
            if k == "Ctrl-B":
                self.benchmark()

    currentScene = SmallScene()




