=============================================================
PEG ``vob_clipping--benja``: Add clipping state to vob scenes
=============================================================

:Author:   	Benja Fallenstein
:Last-Modified: $Date: 2003/04/08 07:59:41 $
:Revision: 	$Revision: 1.2 $
:Status:   	Current


Currently there is no way to clip vobs independent of the
``GraphicsAPI`` used. In OpenGL, clipping is handled through
the ``gzz.gfx.gl.Stencil`` class, which uses OpenGL state:
Being passed a ``Runnable`` object, the class sets up OpenGL
state so that a given area is clipped, calls the ``Runnable``
which places vobs into the vobscene that are rendered with
the given clip, then removes the clipping state and returns.
In AWT, clipping is currently unhandled.


Changes
-------

Add the following methods to ``VobScene``::

    /** Intersect the clipping area with the given cs's "unit square".
     *  This changes the current clipping area to the intersection
     *  of the old clipping area with the "unit square" of the given
     *  coordinate system, and pushes the old clipping area onto 
     *  a stack of clipping areas.
     *  All vobs placed into this VobScene will be rendered
     *  with the new clipping area, until ``unclip()`` is called,
     *  restoring the old area.
     *  <p>
     *  The clipping area is the area where graphics are drawn.
     *  Note that the stack of clipping areas is a way to
     *  specify the semantics of this method, and is not required
     *  to be implemented through a stack data structure.
     */
    void clip(int cs);
    
    /** Pop the topmost clipping area off the stack.
     *  The last pushed clipping area is popped off the stack and
     *  made current.
     *  @throws IndexOutOfBoundsException if the stack is empty.
     */
    void unclip();
    
Non-rectangular clips need to be handled in a ``GraphicsAPI``-specific manner,
because our least common denominator is rectangluarly shaped clipping.
(Of course, rectangularity is defined by the coordinate system: if the
coordinate system is not orthogonal, the clipped area may not be a rectangle.)
The ``unclip()`` method is needed so that several clips can be applied
hierarchically without difficulty.

In OpenGL, these methods can be implemented using OpenGL state, as in
the ``Stencil`` class. (The two methods here are less powerful than what the
``Stencil`` class provides, because they are least-common-denominator.
OpenGL specific methods can provide more powerful clipping functionality,
should it be needed.)

In AWT, these methods can be implement using ``java.awt.Graphics`` state 
combined with a stack of ``java.awt.Shape`` objects representing the different
clipping areas (``java.util.ArrayList`` contains convenience methods
for being used as a stack).


Issues
------

- How are the new ``VobScene`` methods implemented? Will there be
  ``GraphicsAPI``-specific subclasses, or will the calls be passed
  to ``VobCoorder``, which has ``GraphicsAPI``-dependent
  implementations currently?

   RESOLVED: They will be directed to ``VobCoorder``, since this
   results in less changes to the architecture. There will be
   ``clipRect()`` and ``unclip()`` methods with the same signature
   in ``VobCoorder``.

- VobCoorder doesn't know which VobScene it is in, or which VobMap
  it's using. This makes it impossible to implement this functionality
  for OpenGL changing only GLVobCoorder, since the OpenGL state is altered
  by placing a new Vob in the VobMap.

   RESOLVED: The ``VobCoorder`` methods will be passed the ``VobMap``.
   (This is slightly ugly, but having API-specific ``VobScenes``
   seems uglier.)

- How deep a stack is allowed / required? This makes a lot of difference
  for the OpenGL implementation which has native stacks of fixed depth.

   RESOLVED: Ultimately, stacks should be arbitrarily deep, with GL
   switching to a different implementation method when the native
   stack depth is exceeded. For now, it's acceptable to throw an
   exception if the native stack depth is exceeded. If we run into
   real-life problems with this, that'll be a good time to switch to
   a more advanced system.

- Should clipping with this method work with nonlinear coordinate systems?
  This is a very fundamental issue for OpenGL, since if the answer is no,
  we can use clip planes and get rid of much unwanted geometry, getting
  better performance; otherwise, we need to use stencil and draw everything.

   RESOLVED: This method should work with nonlinear coordinate systems.
   This ensures that we can use nonlinear coordinate systems for all
   things we draw in OpenGL. For example, we can put a window into
   a fisheye view without getting problems with clipping used on
   that window.

   This method should be safe to use as the general case; it is
   optimizations that should be the special case, used if necessary.

- What about the interaction of stenciling in OpenGL: We do want
  to use stencils for e.g. irregular edges, but
  1) there are only 8 bits in most implementations, and each clip 
  takes one bit
  2) only one stencil operation can be specified.

    RESOLVED: We can agree that we shall support four levels of clipping
    in OpenGL and Vobs are free to use the four lower-end bits.
    Also, the bits are always set to 1 for pass, so
    the stencil functions NEVER, ALWAYS, EQUAL, GEQUAL, GREATER 
    shall always work.

- Can we implement clipping along with Vob Batching correctly?

    RESOLVED: Surprisingly, yes, with little problems. When clip()
    is called, all active batches need to be prepared to clip,
    **if** a vob is placed in them after that.

- If we can't clip to a nonlinear CS in a particular case, for example
  because there is no stencil buffer, what should we do when asked
  to do that? Exception or silent failure with artifacts?

- Should the clipping exception be specified more accurately to allow
  graceful recovery from the preceding condition?




