=============================================================
PEG gfxdemos_framework--tjl: 
=============================================================

:Authors:   Tuomas J. Lukka, Janne V. Kujala
:Last-Modified: $Date: 2003/03/31 10:00:03 $
:Revision: $Revision: 1.1 $
:Status:   Incomplete

The current gfx/demos directory is a mess. To see an effect,
you have to read the source for the key bindings and hope the demo
hasn't been broken by recent changes. It's time to organize.

Issues
======

- How should gldemo.py move between scenes if started with
  more than one?

    RESOLVED: For less than ten, function keys.
    For more, TBD.

- What module for ``Toggle`` &c?

    RESOLVED: ``gfx.util.demokeys``. 

- Is the declarative key handling powerful enough?

    RESOLVED: Yes, since with the ``Custom`` type it
    is possible to bind any pattern of keystrokes to 
    be called to a method.

    In a pinch, ::

	keys = [
	    Custom("key", re.compile(".*"))
	]

    will give the old behaviour, bu is **STRONGLY**
    discouraged.

Goals
=====

- Demos should be self-documenting.

- Demo files should be small and *graspable*.

- It should be possible / easy to make a "demo browser", 
  which loads all demos and shows a menu.

- It should be easy to write tests for demos

Changes
=======

Files, directories
------------------

Change the file/directory structure of the demos so that
one file shall contain at most **one** scene object,
and the name of that object will be ``Scene``.
The scene files should be importable as modules,
independent of the ``gldemo.py`` wrapper.
If several scenes move around a common theme,
they should be placed in their own directory (python
module).

    RATIONALE: easiest for a demo browser to read.
    Files will be smaller. Directory structure
    helps organization.

The ``gldemo.py`` wrapper should then be changed to
accept one or more file or directory names. 
The global window variable ``w`` should be moved
to its own module.

Scene objects
-------------

Each Scene object shall have a docstring briefly explaining
what the *point* of the demo is, and how the point is
demonstrated by the demo. For example::

    Demonstrate the ambiguity of box-line views.

would be a good way to start the docstring of a demo;
OTOH, ::

    Show a small graph filleted and unfilleted.

misses the point entirely. The reader, unfamiliar with
the idea, does **not** understand why this is something
she'd like to see. Demos not fulfilling this requirement
shall be relegated to the directory ``gfx/demo/pointless``.

Instead of the current ``key(self, k)`` mechanism where
key presses are decoded in a sequence of if statements,
each Scene class shall contain a table ``keys``::
    
    keys = KeyPresses(
	Toggle("fillets", 0, "filleting", "f"),
	SlideLog("zoom", 900.0, "Zoom factor", "<", ">"),
	Custom("setFlurbType", re.compile("[0-9]")),
	...
    )

where the ``Toggle`` and others come from ``gfx.util.demokeys``.

The scene mechanism ::
    
    def scene(self, vs):
	# ... put stuff into vs

shall remain the same.



