===============================================================
``vob_considerations--benja``: Vobs from a 50k feet perspective
===============================================================

:Author:	Benja Fallenstein
:Date:		2002-12-07
:Revision:	$Revision: 1.2 $
:Last-Modified:	$Date: 2003/06/12 10:14:18 $
:Type:		Architecture
:Scope:		Major
:Status:	Incomplete


This PEG describes the intentions between the different stages
the vob system has passed through, trying to describe the
overall design of each. Implementation details are not of concern.
At the end, I try to draw conclusions for improving
the current state of the system (to make it easier to use,
by matching its intentions better).

This document is for readers familiar with the vob concept;
it tries to formalize things you already know.

.. contents::



-------------------------
History of the vob system
-------------------------


First stage: The 0.6 system
===========================

In the first-stage vob system [1]_, there was a single
*canvas* with a number of *vobs* on it. Each vob was a visual object
with coordinates on the canvas and an attached *identity*--
cell, in our case. In this simple system, all vobs play the same
kind of *role*: Each vob represented a cell. There were no vobs
representing, for example, the border of something; when a cell
had a border, it would be part of the vob representing that cell.

Formally, there was a set of identities (here: cells), and each vob
was a visual object completely representing a member of that set.
The same approach could have been used for a set of files,
where each vob would have represented one file completely
(if we wanted to show a file as an icon plus a file name, both
would have to be drawn by the same vob in this paradigm).

The system described so far didn't suffice for 0.6; two additions
were needed. Firstly, to draw connections, "renderables"
were introduced, visible objects without identity. These
were not shown during interpolation. Secondly, for the dimension roses,
"vob paths" were introduced: In addition to their identity (cell),
vobs could be marked with a string, the *vob path*; a vob
would only be interpolated to another vob with the same identity
and the same vob path. Vob paths designated *roles* in which a vob
could appear; the same cell could be shown as part of the structure,
and in the dimension rose, yet the two vobs were not supposed
to be interpolated to each other.

.. [1] In 0.6, vobs were called 'flobs.' I consider this a detail
   that would only be distracting to the discussion here.


Stage one-point-five: Keys are arbitrary objects
================================================

This stage is only a minor step from the first stage, above.
In order to remove the dependency of vobs on zzstructure,
I suggested employing arbitrary objects as the vobs' identities
("keys"), instead of the cells used so far.

This was meant to be a simple extension allowing to use other
sets of identities than cells, for example files in a file manager.
However, it had an unintended consequence that has been with us
ever since: The proliferation of vob keys being used to designate
a vob's *role*, as in "the menu bar" or "the dimension rose"
or "the border of this thing." I call this kind of object
a *role key*. The usual pattern is that an instance of class
``Object`` is created statically in some class and used
as the key whenever a vob is placed into a scene in that role.

This is somewhat ugly, but arguably less so than alternatives
(like the "vob path" hack, which bears some resemblance
to the role key phenomenon).


Second stage: Coordinate systems
================================

The second stage, introduced by Tuomas in Spring 2002, modified
the system by introducing *coordinate systems*. A coordinate system
is defined by a coordinate transformation (translation and scale
or possibly a full affine transformation) relative to the canvas.
It is coordinate systems that take identities (keys) in this system,
not vobs. 

Because coordinate systems have identities and are interpolated
between keyframes, vobs are now less overloaded: they can now be seen as
graphical objects to be drawn in a coordinate system.

The most important effect of this change is that it allows
a single Vob to span two coordinate systems. The earlier system
allowed animation of connections between vobs only in a kludgy
way. The new system allows the connection vob to know both
its start and end coordinate system and trivially draw itself
from point A in coordsys 1 to point B in coordsys 2.

However, while a vob is now just a graphical object without identity,
some vobs are still drawn to show an object with identity, while
others are drawn as 'decorations' showing additional information
about those objects. For example, a vob that shows a cell
is drawn to represent that cell to the user, while a vob showing
a little icon next to the cell (to indicate it is has changed recently,
say) does not represent the cell itself to the user, but
additional information about the cell (even though both
may be placed into the same coordinate system, keyed by the cell).

It is still basically assumed in this system that there
is a set of identities, and each coordsys represents one member
of that set. In practice, however, even if most coordsys represent cells,
it is common practice for some to hold other stuff (for example
the name of the currently shown view etc.).


Third stage: Hierarchical coordsys
==================================

The next step, getting us to the current vob system, was to
introduce a *hierarchy* of coordinate systems. In this paradigm,
the canvas is a coordinate system in itself, the *root coordinate system*.
Other coordinate systems can be placed inside it, and yet other
coordinate systems can be placed into *those* coordinate systems.

This provides a way to finally break up the monolithic cell vobs,
combining border, background, and cell content in a single vob.
Since the cell content can be in a coordinate system
inside the border's, it can be scrolled relatively to the border,
showing a line cursor in the middle of the visible area.
While hierarchical coordsys can also be used for
showing a set of cells inside another cell, for example,
this kind of assembling a bigger object from smaller parts
was an important reason for moving to hierarchical coordsys
in the first place.

In this stage, we finally truly depart from the notion
that there is a single set of identities and each coordsys
represents one of them. At some levels in the hierarchy
this is still true; a zzstructure view still places coordsys
representing cells into the vob scene. But a coordsys for
"the contents of a cell, scrolled to show the line cursor
in the middle" works in a different way. We use role
keys to represent this kind of thing.

[XXX Say something about interpolation hierarchies here?
Maybe leave as a detail; we don't need to say everything
in a 50k view...]



--------
Analysis
--------


Role keys
=========

Role keys are here to stay. This is because indeed they solve
an important problem-- in a PUI scrollbar, when we've hit
'PageDown,' how do we animate the box inside the scrollbar
appropriately without knowing that the two vobs placed
into the vob scenes before and after interpolation should be
interpolated to each other? The box has no 'identity' except
its role in the scrollbar.

Unlike currently used, though, as often as possible role keys
should be defined publicly in methodless Java interfaces
(so that they can be shared between all classes that use them).
In the PUI scrollbar example, we could have::

    interface ScrollbarKeys {
	Object BUTTON_UP_KEY = new Object(),
	       BUTTON_DOWN_KEY = new Object(),
	       DRAG_BOX_KEY = new Object();
    }

This would allow two independent scrollbar implementations,
maybe from two differrent widget toolkits, to be interpolated
to each other. This is what the vob system is all about:
Interpolation between views that were not explicitly programmed
to be interpolatable to each other.


What do we think of as a vob?
=============================

All the time since stage one, a vob has been *a graphical object
without accessible internal structure*. While in stage one,
a vob was always associated with an identity, since stage two,
you can think of it simply as an image transformed by
a coordinate system; in both cases, though, a vob is a primitive
you cannot 'look into.'

If a vob (graphical object) has internal structure, this structure
is not *reified*, that is, it is not accessible through the vob system.
Consider a vob representing a cell in stage one, above; there may be
a cell border, textual content (possibly broken over multiple lines),
and a line cursor. There definitely is internal structure, but it is
impossible to substitute, say, a different kind of line cursor
on the vob system level, since it treats the whole cell vob as
an indivisible entity.

Hierarchical coordinate systems (stage three) attempt to solve
this problem by modelling only the primitive graphical objects
as vobs. A cell would be drawn by using a border vob, one or more
text vobs, and a line vob to show the cursor. 

But consider what we'd consider to be a 'vob' or 'visual object'
as an application programmer. The paradigm is that we place
'visual objects' on the screen by putting them into coordinate
systems; how would we interpret this when programming, for example,
a zzstructural view? -- It seems to me that the earlier stages
match the expectations better: the 'visual objects' are the cells;
what's inside them is simply of no concern.

I think it is here that the current vob system errs. By only
providing for atomic objects as vobs, it forces the
application programmer into micromanagement. I believe that
a better definition of vob would be *a graphical object
with ignorable internal structure*: An object
which can be safely treated as a unit, ignoring its internals,
but can also be seen as a collection of things.

I propose to make ``Vob`` an interface with a ``place`` method,
putting the given vob into a coordinate system. Additionally,
I propose a ``Renderable`` subclass of ``Vob`` which takes on
the current meaning of ``Vob``: an indivisible graphical primitive.
Only ``Renderable`` objects can be placed into a ``VobMap``,
and this is what a default ``place`` implementation
in ``Renderable`` does. A more complex ``Vob`` implementation
could create new coordinate systems inside the coordsys
given to its ``place`` method, putting other vobs into
these sub-coordsys. A vob showing a cell could be implemented
that way.

.. uml:: vob_and_renderable

    class Vob "interface"
	methods
	    place(into)

    class Renderable
	inherit Vob
    ---
    vertically(50, ver_c, Vob, Renderable);

(The naming is a little bit ironic, since in the first stage
vob system, ``Flob``, the predecessor of ``Vob``, was
a subclass of ``Renderable``. Yet, it fits the roles really well.)

``Vob`` could become a superinterface of the current ``HBox``,
which adds methods to request the size and baseline at a given scale.
Indeed it could be useful to put requests for size information
into ``Vob``, leaving only baseline requests to ``HBox`` ("``HVob``"?).
This would bring the interface close to ``CellView``; it may even
be possible to unify ``Vob`` and ``CellView``. These are details
out of scope for this PEG, though.

``Vob.place()`` should return the coordinate system given to it;
this allows writing ::

    box = vob.place(vs, vs.boxCS(cs, key, 250, 250, 50, 50))

-- i.e., making the placement of a vob a one-liner,
which it should really be (since it should be an atomic action
in the application programmer's mind), without losing the coordsys
the vob was placed into (``vs.matcher.getCS(key)`` only works
for non-repeating views, i.e. views that never use
the same key twice under any circumstances).

