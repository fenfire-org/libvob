
==============================================================================
PEG controllers_events--mudyc: Requirements for handling events of controllers
==============================================================================

:Authors:  Matti J. Katila
:Date-Created: 2003-07-15
:Last-Modified: $Date: 2003/08/07 14:15:06 $
:Revision: $Revision: 1.7 $
:Status:   Current
:Scope:    Minor
:Type:     Architecture requirements


.. :Stakeholders:

.. Affect-PEGs:


New user interfaces need custom controllers. Handling the events of these
controllers should be modular, easy and generic from the bottom layer up 
to top layer where the user is main character.


Issues
======

- How does this peg affect to Tjl's input framework?

    RESOLVED: This PEG uses some of that framework, by allowing mouse
    events to be adapted for relative axis listeners.

    On the whole, the two frameworks have relatively different purposes.

- Should normal mouse controller and custom LEGO controllers 
  fit in the same API at some level?

    RESOLVED: Yes and no. Mouse uses the buttons and modifiers
    while other controllers are not "modified" to such a degree.
    There is some commonality - the mouse should be usable to control
    things that controllers can but probably not vice versa.

- Should custom LEGO controllers be able to control many things
  in different contexts, like fiddling zoom pan in buoyoing view and
  in other view for example change the speed of interpolation?

    RESOLVED: This is minor issue since with requirements specified 
    it is doable. The question is: would it be sensible.
  
- How do we change bindings while the program is running?

    RESOLVED: By providing enough information to the control setup
    (action name, ...), we can make the program binding be just
    the default and load user bindings behind the scenes.

    Thus, if we just make sure that we have enough information in the
    loop, we can postpone this.

- Should these classes go to the vob.input package or a new vob.mouse package?
  On the one hand, they will use .input for RelativeAxisListener,
  but on the other, this is a separate piece that may be used or not,
  as per the user's choice.

    RESOLVED: Separate package. Clearly showing the different modules
    is important.


Requirements
============

For a user the controller interface should be easy, say
-------------------------------------------------------

- Inverting controller.

    - ``This mouse wheel works in wrong direction!``

- Automatic calibrating

    - ``This doesn't work!``

- Bind to any axe or button or modifier from any controller.

    - Bindings should be dynamically changeable while program is running.

        - ``Button 1, why?``
        - ``Why in vertical, why not in horizontal direction?``
        - ``Modifier with this would be nice..``  
        - ``I want that red looking LEGO controller, not that big blue :-)``
  
- (Scale)

    - ``Mouse is too slow!``


For programming, the control framework should support different models
----------------------------------------------------------------------

- Axis model

    - Absolutely 
    - Relative

- Normal mouse model

    - Point model

        - X and y in screen.

    - Dragging model

        - Close to relative axes model

- Modifiers

Design
======

Listeners: the interfaces the API user provides
-----------------------------------------------

The user provides listeners for different events.
The central binding class is used to multiplex e.g. mouse
button events with different modifiers to different 
listeners.

Mouse drag events may be multiplexed by the location of the original
press event through a vob.mouse.MousePressListener::

    public interface MousePressListener {
	/** The mouse was pressed down.
	 * @param x,y The coordinates.
	 * @return If non-null, the return value is the drag listener
	 *      to be used if the mouse is dragged from here.
	 */
	MouseDragListener pressed(int x, int y);
    }

The mouse drag interface is as follows::

    public interface MouseDragListener {
	/** Called when the drag is started. This method
	 * is called even if this dragListener was received
	 * from a MousePressListener to make these easier
	 * to program.
	 */
	void startDrag(int x, int y);
	/** Called when a drag event is received.
	 */
	void drag(int x, int y);
	/** Called when the drag is ended.
	 */
	void endDrag(int x, int y);
    }

Mouse clicks are handled separately:

    public interface MouseClickListener {
	/** The mouse was clicked.
	 * @param x,y The coordinates.
	 */
	MouseDragListener clicked(int x, int y);
    }

Separating these three classes allows the easiest assembling of 
event handling structures at run time.

Multiplexer
-----------

The multiplexer sends mouse events it is given to the appropriate
listeners.

    /** A class to send mouse events to the listeners that want them.
     */
    public class MouseMultiplexer {
	private static class Direction { private Direction() {} }
	public static final Direction HORIZONTAL = new Direction();
	public static final Direction VERTICAL = new Direction();

	public void setListener(int button, int modifiers, String description, MousePressListener l);
	public void setListener(int button, int modifiers, String description, MouseClickListener l);
	public void setListener(int button, int modifiers, Direction dir, String description, RelativeAxisListener l);

	public void setWheelListener(int modifiers, String description, RelativeAxisListener l);

	public boolean deliverEvent(VobMouseEvent e);
    }





