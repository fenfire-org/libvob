=============================================
Libvob input framework for custom controllers
=============================================

How to read joysticks and custom
controllers built from LEGO(r) bricks.


Introduction
============

Among other things, we are building custom controllers out of LEGO(r) bricks
by connecting them to optomechanical USB computer mice. This allows the easy conversion
of mechanical motion to digital input. Inside the computer, we need to make
it easy to access the axes as well.


Requirements
============

Input types
-----------

We want to know the current positions of different controllers.
There are up to three axes per controller attached to x, y and mouse wheel.

To make things difficult, there are different types of axes:

- limited, with friction (i.e. remaining where the user set it).

- limited, with tension (i.e. returning to center, like a joystick).

- unlimited, with friction (i.e. a mouse wheel: infinite length)

There are also different uses for the axes inside:

- to control the value of some parameter absolutely, so the position
  should be converted to an absolute number between 0 and 1.
  An event should be sent whenever the number changes, but noise should
  be filtered.

- to control a first derivative, so there should be a null zone and 
  outside it, events should be sent at some reasonable interval containing
  the integral (?) of the value.

- to control the value of some parameter relatively, with 
  infinite variability (i.e. angle of rotation, which wraps at 360
  degrees). Should be scaled appropriately. May be useful to offer
  acceleration support.

Now, the *usual* (but not only) way to map the axes to meanings is to use
"limited, with friction" for absolute values, "limited with tension"
for first derivatives and "unlimited, with friction" for relative control.

There are, of course, also connected 2D axes but as those are dealt with
by existing mice &c, we will concentrate on single, independent axes.

Input devices
-------------

Need to support both axes received from mice and actual joysticks.

The system has to be extensible to several input devices.

Calibration
-----------

The calibration process needs to be trivial: press a key, wriggle
the device to extreme positions, and press another key.

When several input devices are used, they should detect among themselves,
*which* ones of them the user is trying to calibrate.

Getting information
-------------------

Each input device should define names and characteristics for the axes,
for programs to show the user what can be connected to what.


Design of the interfaces
========================

Models
------

The ``*Model`` classes are a central piece of the API.
This is a design similar
to swing: for each controlled "thing" there is a ``Model`` object
which implements a relevant interface. This allows us to, e.g., use special
models in Jython code so that the variable controlled is actually 
directly an object attribute - the intent is to use this code
in ``vob.putil.demokeys``.

..  UML:: custominput_models

    jlinkpackage org.nongnu.libvob.input

    class Model "interface"
	jlink
	methods
	    addActionListener(...)

    class BoundedFloatModel "interface"
	jlink
	inherit Model
	fields
	    minimum
	    maximum
	    value

    class ModFloatModel "interface"
	jlink
	inherit Model
	fields
	    modulus
	    value
	methods
	    addActionListener(...)

    ---
    horizontally(50, xx, BoundedFloatModel, ModFloatModel);
    vertically(50, yy, Model, xx);


Axes
----

The models are connected to the axes through absolute and relative
axis listeners:

..  UML:: custominput_adapters

    jlinkpackage org.nongnu.libvob.input

    class Axis "interface"
	jlink
	assoc multi(1) - multi(0..1) role(mainListener) AxisListener
	methods
	    setMainListener(AxisListener l)

    class AxisListener "interface"
	jlink


    class AbsoluteAxisListener "interface"
	jlink
	inherit AxisListener
	methods
	    void changedAbsolute(float newvalue)

    class RelativeAxisListener "interface"
	jlink
	inherit AxisListener
	methods
	    void changedRelative(float delta)

    class BoundedFloatLinearAbsoluteAdapter
	jlink
	realize AbsoluteAxisListener
	assoc multi(0..1) - multi(1) BoundedFloatModel

    class BoundedFloatLogAbsoluteAdapter
	jlink
	realize AbsoluteAxisListener
	assoc multi(0..1) - multi(1) BoundedFloatModel

    class ModFloatRelativeAdapter
	jlink
	realize RelativeAxisListener
	assoc multi(0..1) - multi(1) ModFloatModel

    class BoundedFloatModel "interface"
	jlink

    class ModFloatModel "interface"
	jlink

    ---
    horizontally(150, xx, Axis, AxisListener);
    horizontally(100, yy, AbsoluteAxisListener, RelativeAxisListener);
    horizontally(40, zz, BoundedFloatLinearAbsoluteAdapter, BoundedFloatLogAbsoluteAdapter, ModFloatRelativeAdapter);
    horizontally(130, qq, BoundedFloatModel, ModFloatModel);

    vertically(60, ww, AxisListener, yy, zz, qq);
    

There can only be one main model per axis - to not confuse the user.
If desired, we may later add the capability to put in "non-main"
listeners, but this is not a priority.

Input devices
-------------

The input devices, on the other hand, are represented by a central class
which allows programs to access the currently configured input devices:
``InputDeviceManager``. From this class, users may get instances of
the ``InputDevice`` class. This class also contains the methods for
beginning and ending calibration and choosing axes by moving them.
The ``InputDevice`` classes again allow users to get the axes.

..  UML:: custominput_inputs

    jlinkpackage org.nongnu.libvob.input

    class InputDeviceManager "interface"
	jlink
	fields
	    STATE_CALIBRATING
	    STATE_CHOOSING
	    STATE_NORMAL
	methods
	    void setState(int state)
	    Axis getCurrentChoice()
	assoc multi(1) - multi(*) InputDevice

    class InputDevice "interface"
	jlink
	assoc multi(1) - multi(*) Axis
	    

    class Axis "interface"
	jlink
	methods
	    getName()
	    void setState(int state)
	    float getChoiceProbability()

    ---
    horizontally(60, xx, InputDeviceManager, InputDevice, Axis);


Implementation
==============

Axes
----


Inside, we have classes to adapt the delta input from the mouse
to the axis interface.

..  UML:: custominputs_ps2mouse

    jlinkpackage org.nongnu.libvob.input.impl

    class (PSMD) PS2MouseDevice
	jlink
	realize InputDevice
	fields
	    RandomAccessFile dev
	assoc multi(0..1)  - multi(3) role(xyz) RelativeAxis
    
    class RelativeAxis
	jlink
	realize Axis
	methods
	    int changedRelative(int delta)

    jlinkpackage org.nongnu.libvob.input

    class InputDevice "interface"
	jlink

    class Axis "interface"
	jlink

    ---
    horizontally(40, xx, InputDevice, Axis);
    horizontally(70, yy, PSMD, RelativeAxis);
    vertically(50, zz, xx, yy);






.. vim: set syntax=text
