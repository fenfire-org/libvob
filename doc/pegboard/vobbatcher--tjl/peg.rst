=============================================================
PEG vobbatcher--tjl: 
=============================================================

:Author:   Tuomas J. Lukka
:Last-Modified: $Date: 2003/04/08 20:31:00 $
:Revision: $Revision: 1.1 $
:Status:   Current

OpenGL is much better at rendering AAAAAAAAAAAABBBBBBBBBBBB
than ABABABABABABABABABABAB.

The benchmark text.overhead shows that for 20-letter text vobs,
**OVER HALF** of the time can be spent in OpenGL state changes.
This is obviously unacceptable, especially as most text vobs 
are shorter than that.

The same benchmark already contains the most important part
of the solution, namely batching of the text vobs in the sense
of first doing init, then all the actual text vobs and finally
the deinit.

This problem gets worse with text vobs with backgrounds: the number of
state changes grows.

Issues
======

- Can we somehow batch them automatically, without explicit user control?

    RESOLVED: No.  This cannot be done without the explicit knowledge of
    at least some programmer outside LibVob: rearranging vobs can lead to
    several artifacts when the depth buffer is used. For example, text
    vobs use blending to obtain subpixel accuracy. If the background is
    green and a white rectangle is rendered behind the vob, artifacts
    would occur if the rectangle were rendered after the vob: on the
    blended pixels, the green color would show through because only the
    latest value is written into the depth buffer, when blending.

    Additionally, with OpenGL, state change vobs are commonly used.

- Can we at least make it easy for the user?

    RESOLVED: Definitely. The overlaid API should make it as simple
    as possible

- Should we have autodetection of order for the required states for each vob?
  Or simply exact batching?

    RESOLVED: At first, we'll go for exact batching. Orders are not changed - 
    vobplacer recursion is used to allow the user easily change the ordering.

- Do we need to use full vobscene recursion for this?

    RESOLVED: No. We can do everything with just vobplacer recursion: 
    the coordinates and matching need not be affected, since the coordinate
    systems are orthogonal to mapping.

- Do we need this system to work with AWT, too?

    RESOLVED: No. The performance tradeoffs in AWT are very different due
    to the different nature of the underlying pipes. Because there is so much
    less flexibility, state changes are also much cheaper.

    There is also another difficulty: AWT does not have depth buffering,
    so any overlapping objects **must** be rendered in depth order.

Design criteria
===============

One of the most common operations is vobs with backgrounds. If the backgrounds
are opaque, they can be rendered first in one batch and the content afterwards. 
If the backgrounds use blending at the edges, there may be small artifacts
but these are much less significant than ones with text; also, it's
more efficient to disable blending there unless it's absolutely vital.

Another important criterion is that this system must cause **no** complications
if it is not used. All vobs should be usable just as they used to be.

Low-level design
================

It is good to separate the system to the low-level API which is simple to implement
and the high-level API which is visible to the user.

There are two user-visible changes: Batchable Vobs and VobPlacer recursion.

Batchable Vobs
--------------

The interface BatchableVob is defined as

    public interface BatchableVob extends Vob {
	Vob getSetUp();
	Vob getRealVob();
	Vob getTearDown();
    }

In order to make use of these, we'll add to VobPlacer, VobMap and VobScene
the methods putBatching(BatchableVob vob, ...) analogical to put(Vob vob, ...).
This way, no time is used to check for batching unless requested. For AWT,
these methods shall just delegate to put().  

When using these methods, the code for the 1-cs version of putBatching is

    Vob lastSetUp;
    Vob lastTearDown;

    public void putBatching(BatchableVob v, int coordsys1) {
	Vob s = v.getSetUp();
	if(s != lastSetUp) {
	    if(lastSetUp != null)
		put(lastTearDown);
	    put(s);
	    lastSetUp = s;
	    lastTearDown = v.getTearDown();
	}
	realPut(v.getRealVob(), coordsys1);
    }

This way, if rendering 100 batchable vobs of type 1 and one of type 2 in the middle,
things will still work, with the slight performance hit.

Naturally, the put() methods must be modified also, by moving them to be
the new final private realPut() methods and creating the wrappers

    public void put(Vob vob, int coordsys1) {
	lastSetUp = null;
	realPut(vob, coordsys1);
    }

This causes a minimal performance hit for the normal code path.

VobPlacer recursion
-------------------

The batchable vobs alone do not quite suffice, as the same types of vobs
need to be placed right after another, for which there is no support. The
view system would need to contort itself quite badly for this.

The low-level feature which we can implement to help this is related to
recursive vobscenes, but is actually a far simpler subset. Quite simply,
we request from a VobScene a new recursed instance which shares both the
VobCoorder and VobMatcher but has a new VobPlacer. All the Vobs placed
into the new instance will be rendered at the point where the instance
was created.

For example::

    vs.put(v1)
    vs2 = vs.interstice()
    vs2.put(v4)
    vs.put(v2) 
    vs2.put(v5)
    vs.put(v3) 

puts the vobs in the order v1, v4, v5, v2, v3. Nothing else is changed.

This recursion interacts with the proposed clipping solution
(vob_clipping--benja); the clipping is global to all recursed instances
of the vobscene.


High-level design
=================

Given the low-level APIs which provide the fundamental ways to do things,
the view must be adjusted somewhat to get the maximum advantage.

For instance, if we have a factory which places Vobs into a scene,
the factory should have a method for starting batching where it would
internally create the interstices. In this way, the user of the factory
still has full control on at what point the vobs get rendered (i.e. the point
where the interstices were created), but does not need to be concerned
about the number of interstices &c.

The interaction with the structured graphics stuff has not as yet been
thought out.



