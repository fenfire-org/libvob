
========================================================================
PEG gl_mouse_modifiers--mudyc: Fixing native mouse event binder
========================================================================

:Authors:  Matti Katila, Tuomas J. Lukka
:Date-Created: 2003-08-01
:Last-Modified: $Date: 2003/08/01 14:54:52 $
:Revision: $Revision: 1.6 $
:Status:   Current
:Stakeholders: tjl
:Scope:    Trivial
:Type:     Implementation


InputEvent, which is inherited by MouseEvent, knows the 
states of modifier keys. The current OpenGL native event binder gives
wrong modifiers to InputEvent class. To avoid difference between
OpenGL and awt clients this must be fixed.


Intro
=====

Input event model between awt and OpenGL differs. In awt JVM handles all 
events and in OpenGL all events are binded from X (currently there 
are no other ports) to JVM through JNI. The current mouse binder 
doesn't look for modifier keys('Control', 'Alt', 'Shift' or 'Meta').


Background
==========

The current OpenGL event code sends modifier keys on mouse button 
events in a really strange way:

1. If button 1 was pressed, then the event sent is "Button1"

2. If button 2 was pressed, then the event sent is "Alt+Button2"

3. If button 3 was pressed, then the event sent is "Meta+Button3"

There are *no change* in the event if any of the modifiers 
is pressed or not. So modifier keys *don't affect* any kind 
to the event. This is a bug.

The code that handles the X's OpenGL events to java must be 
fixed to correspond the correct mature of awt client 
where 'Control', 'Shift', 'Alt' and 'Meta' are 
noticed only when the modifier is pressed.

Changes
=======

The problem is, it appears, in JDK: the constants
InputEvent.BUTTON2_MASK and InputEvent.BUTTON3_MASK
are aliased to Event.ALT_MASK and META_MASK, respectively!!!

JDK 1.4 has some sort of workarounds but they make things
even more difficult - the workings of the MouseEvent
constructor are complicated, to say the least..

In view of this and the fact that we shouldn't have to pull
in any AWT code for GL-using vob code, we propose that 
``org.nongnu.libvob.Binder`` should stop using the MouseEvent class.

The Libvob AWT code should translate the AWT mouse events 
to the protocol we decide on.

I suggest an event structure of our own::

    public class VobMouseEvent {
	public final static int MOUSE_PRESSED;
	public final static int MOUSE_RELEASED;
	public final static int MOUSE_CLICKED;
	public final static int MOUSE_DRAGGED;
	public final static int MOUSE_WHEEL;

	public final static int SHIFT_MASK;
	public final static int CONTROL_MASK;
	public final static int ALT_MASK;

	/** Corresponds to getID in MouseEvent.
	 */
	public int getType();
	public int getX();
	public int getY();
	public int getWheelDelta();
	public int getModifiers();
	public int getButton();
    }

Note that this class does **not** inherit java.awt.event.MouseEvent.
It is its own class that just looks like java.awt.event.MouseEvent.

The AWT code will need to translate mouse events to this structure as well.
