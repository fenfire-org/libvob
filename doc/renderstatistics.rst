=========================================
Collecting rendering statistics in Libvob
=========================================

There are several reasons to want rendering statistics: to adjust
complexity parameters at run-time, or (currently most importantly)
to load high-resolution mipmap levels of the currently most prominent
textures.

The requirements for the latter application are more stringent;
specifically, the statistics have to be *pushed* from the activated
collectors on the C++ side to the Java side to avoid having to explicitly
poll each collector.

Requirements
============

When statistics get collected, a way to 1) ignore (zero) them, and 2) call callbacks for them.

We only need one central (static) handling point for now, but
implementing more in the future should be trivial..

General
=======

Referencing
-----------

The main difficulty in implementing the push of data is to ensure that 
the Java objects will be garbage corrected. Because the Java garbage
collector cannot know that a global reference inside the C++ arena depends
only on a given Java object (i.e. when the Java proxy of a C++ renderable is deleted,
the C++ renderable gets deleted). 

Thus, there must be a weak reference in the chain from the C++ object to the callback.
It's probably easiest to do this on the java side.

Collection point
----------------

In order to make the pushing of triggered statistics possible
*after* the rendering has finished, there needs to be a C++ queue
containing pointers to all activated statistics.

When statistics are inserted into a statistics-collecting object, 
the object should
check if it has been inserted to the activated statistics queue. If not, 
it should insert itself.

Implementation details
======================

On the C++ side, the implementation is according to the following UML diagram:

..  UML:: renderstatistics_cxx

    class Vob.Stats.Collector "abstract"
	clink
	fields
	    bool onList
	methods
	    gotStatistics()
	    virtual void clear()
	    virtual void call()

    class Vob.Stats.Statistics
	assoc multi(0..1) - multi(*) Vob.Stats.Collector

    class TexAccum "abstract"
	inherit Vob.Stats.Collector
	methods
	    clear()

    class TexAccum_JNI
	inherit TexAccum
	methods
	    call()

    ---

    horizontally(50, xx, Vob.Stats.Statistics, Vob.Stats.Collector);
    vertically(50, yy, Vob.Stats.Collector, TexAccum, TexAccum_JNI);

On the Java side, the class ``GL.TexAccum`` is the center:
creating it with an object implementing the ``GL.StatsCallback``
interface allows pushing, using weak references.
