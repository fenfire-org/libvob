==========================================================================
PEG cursors--humppake: Changing mouse cursor
==========================================================================

:Authors:  Asko Soukka
:Date-Created: 2003-05-09
:Last-Modified: $Date: 2003/05/16 11:04:57 $
:Revision: $Revision: 1.12 $
:Status:   Implemented
:Scope:    Trivial
:Type:     Feature, Interface, Implementation

This peg describes, how changing of the mouse cursor to any of the
default system cursors could be easily implemented on LibVob.

Issues
======

- Do we want to change mouse cursor?

	RESOLVED: Yes, we do. Of course, different effective areas on
        GUI should be noticeable also without changing mouse cursor on
        top of them, but different mouse cursors would work as
        additional visual glues - except for the effective ares, also
        for the overall application state.

- How the mouse cursor should be changed?

	RESOLVED: Calling ``org.nongnu.libvob.GraphicsAPI.Window.setCursor()``
	with ID of wanted cursor as a parameter. Of course setCursor() method
        should be implemented separately for both AWT and GL.

	RE-RESOLVED: Calling ``org.nongnu.libvob.GraphicsAPI.Window.setCursor()``
	with java.awt.Cursor as parameter. This will simplify the interface and
        make it consistent with ``java.awt.Component.setCursor()``.

	RE-RESOLVED: Calling ``org.nongnu.libvob.GraphicsAPI.Window.setCursor()``
	with proper cursor name string as parameter. This is to avoid unwanted
        initializing of AWT, when using GL.
   
	AWTAPI can also have setCursor() overloaded with java.awt.Cursor 
        as parameter. This allows using directly java.awt.Cursor on AWT.

- What is the available set of mouse cursors?
	
	RESOLVED: The set of available mouse cursors is the intersection of
        Xlib and AWT mouse cursors sets:

	- http://java.sun.com/products/jdk/1.2/docs/api/java/awt/Cursor.html
        - http://tronche.com/gui/x/xlib/appendix/b/

	RE-RESOLVED: To be more specific, all Java AWT cursors except 
        custom cursor are available.

	RE-RESOLVED: A stable sub set of available cursor should be 
        defined in Graphics API. At start, it will be the same as Java AWT
        cursors except the custom cursor. AWT and GL cursor APIs can
        then be extended (also separately).

        Names of the default cursors are similar to ones in
        ``java.awt.Cursor`` without the suffix "_CURSOR". In
        awt.Cursor there's a *reason* to have the
        suffix: they are data members of the class and it makes sense
        to separate the namespace. Here, that doesn't make sense.

- Should we use our own custom cursors?

	RESOLVED: Not yet. Probably we would like to use also our own
        custom cursors in the future, but at first it is more relevant
        to get at least changing of default system cursors work.

	NOTE: In Java, Toolkit.createCustomCursor is available since
        JDK 1.2. How custom cursor could be used efficiently in GL?

	RE-RESOLVED: Since ustom cursors should be also possible 
        outside the AWT, using custom cursor is allowed. Althought, left
        yet unimplemented in GL.

	RE-RESOLVED: General GraphicsAPI won't contain custom cursor
        choice, but custom cursor can be added into GL and AWT APIs 
	later on. If some cursor is implemented to them both, it can
        be included into general GraphicsAPI.

- How java.awt.Cursor is mapped to Xlib mouse cursor values?

	RESOLVED: Mapping is done in setCursor() method
        in ``org.nongnu.libvob.impl.gl.GLScreen`` using switch structure.

	RE-RESOLVED: String value corresponding to cursor is 
        passed to on in ``org.nongnu.libvob.impl.gl.GLScreen`` and
        low level implementation like usin Xlib is determined later on.
	At first in /src/os/Os-GLX.

	RE-RESOLVED: Irrelevant. Sending string values independent of 
        low level implementation.

- What are the mouse cursor IDs?
	
	RESOLVED: IDs are our own constants mapped to integer values that
        correspond the mouse cursors in current environment. Integers 
	for specific mouse cursors are different in AWT and Xlib and that's
	why we need our own mapping for them.

	RE-RESOLVED: Irrelevant. No more IDs. IDs were originally
        values of different mouse cursor constants of Xlib, but they
        are not used anymore.

- Where are the mouse cursor ID mappings located?
	
	RESOLVED: Mouse cursor constants are described with AWT values as default
        in ``org.nongnu.libvob.GraphicsAPI``. For GL implementation those
        mappings must be overwritten into
	``org.nongnu.libvob.impl.gl.GLAPI``.

	RE-RESOLVED: Irrelevant. No more IDs.

- Since it's possible to call setCursor() with pure integer values, is
  it allowed to use AWT or Xlib specific cursors?

	RESOLVED: Yes, but with care. If the application is runnable under
        both AWT and GL, there should be checking for proper GraphicsAPI..

	RESOLVED: Irrelevant. No more IDs.

Changes
=======

Interfaces
----------

Into ``org.nongnu.libvob.GraphicsAPI.Window``::

    /** Set the mouse cursor for the window.
     * Available cursor types. These are similar to ones
     * in java.awt.Cursor, tough the "_CURSOR" suffix
     * is dropped.
     *
     * "CROSSHAIR"  The crosshair cursor type.
     * "DEFAULT" The default cursor type (gets set if no cursor is defined).
     * "E_RESIZE" The east-resize cursor type.
     * "HAND" The hand cursor type.
     * "MOVE" The move cursor type.
     * "N_RESIZE" The north-resize cursor type.
     * "NE_RESIZE" The north-east-resize cursor type.
     * "NW_RESIZE" The north-west-resize cursor type.
     * "S_RESIZE" The south-resize cursor type.
     * "SE_RESIZE" The south-east-resize cursor type.
     * "SW_RESIZE" The south-west-resize cursor type.
     * "TEXT" The text cursor type.
     * "W_RESIZE" The west-resize cursor type.
     * "WAIT" The wait cursor type.
     */
    public void setCursor(String name);

Into ``org.nongnu.libvob.impl.awt.AWTScreen``::
 
    /** Set the mouse cursor for the window.
     */	
    public void setCursor(Cursor cursor) {
        canvas.setCursor(cursor);
    }

Implementation
--------------

Description
"""""""""""

Java AWT client uses ``java.awt.Cursor``, which can be passed to any
``java.awt.Component`` - like ScreenCanvas in AWTScreen. GL client
needs a platform specific implementation. Currently we are supporting
X implementation. In X Windows, mouse cursor could be changed via
Xlib.

In GL implementation, the cursor name string is passed on in
``org.nongnu.libvob.impl.gl.GLScreen`` and the low level
implementation like using Xlib is determined later on. Most probably
in /src/os/Os-GLX.

In AWT the cursor, name string is converted to corresponding
``java.awt.Cursor``, which is passed to proper AWT component.

Java
""""

Into ``org.nongnu.libvob.impl.awt.AWTScreen``::
 
    public void setCursor(String name) {
	try {
	    Cursor cursor = new Cursor(Cursor.class.getField(name.toUpperCase()+"_CURSOR").getInt(null));
	    canvas.setCursor(cursor);
	} catch (Exception e) {
	    throw new IllegalArgumentException("Unknown cursor: "+name);
	}
    }

Into ``org.nongnu.libvob.gl.GL.Window``::

    /** Set the mouse cursor of the window.
     */
    public void setCursor(String name) { impl_Window_setCursor(getId(), name); }

Into ``org.nongnu.libvob.gl.GL``::

    static private native void impl_Window_setCursor(int id, String name);

Into ``org.nongnu.libvob.impl.GL.GLScreen``::

    public void setCursor(String name) {
      name = name.toUpperCase();
      if (name.equals("CROSSHAIR") ||
	  name.equals("DEFAULT") ||
	  name.equals("E_RESIZE") ||
	  name.equals("HAND") ||
	  name.equals("MOVE") ||
	  name.equals("N_RESIZE") ||
	  name.equals("NE_RESIZE") ||
	  name.equals("NW_RESIZE") ||
	  name.equals("S_RESIZE") ||
	  name.equals("SE_RESIZE") ||
	  name.equals("SW_RESIZE") ||
	  name.equals("TEXT") ||
	  name.equals("W_RESIZE") ||
	  name.equals("WAIT"))
            window.setCursor(name);
      else throw new IllegalArgumentException("Unknown cursor: "+name);
    }

C
"

Into ``include/vob/os/Os.cxx Vob.Os.Window``::

    virtual void setCursor(const std::string name) = 0;

Into ``src/jni/Main.cxx``::

    jf(void, impl_1Window_1setCursor)
    (JNIEnv *env, jclass, jint id, jstring name) {
          Os::Window *w = (Os::Window *)windows.get(id);
          DBG(dbg) << "Set window "<<id<<" Cursor name "<<name<<" at "<<(int)w<<"\n";
          std::string name_str = jstr2stdstr(env, name);
          w->setCursor(name_str);
    }

Into ``src/os/Os-GLX.cxx``::

    // For setCursor()
    #include <X11/Xlib.h>
    #include <X11/cursorfont.h>
    //

Into ``src/os/Os-GLX.cxx Vob.Os.LXWindow``::

    virtual void setCursor(const std::string name) {
        Cursor cursor = 0;
	if (name == "CROSSHAIR")
	  cursor = XCreateFontCursor(ws->dpy, XC_crosshair);
	else if (name == "DEFAULT")
	  cursor = XCreateFontCursor(ws->dpy, XC_left_ptr);
	else if (name == "E_RESIZE")
	  cursor = XCreateFontCursor(ws->dpy, XC_right_side);
	else if (name == "HAND")
	  cursor = XCreateFontCursor(ws->dpy, XC_hand2);
	else if (name == "MOVE")
	  cursor = XCreateFontCursor(ws->dpy, XC_fleur);
	else if (name == "N_RESIZE")
	  cursor = XCreateFontCursor(ws->dpy, XC_top_side);
	else if (name == "NE_RESIZE")
	  cursor = XCreateFontCursor(ws->dpy, XC_top_right_corner);
	else if (name == "NW_RESIZE")
	  cursor = XCreateFontCursor(ws->dpy, XC_top_left_corner);
	else if (name == "S_RESIZE")
	  cursor = XCreateFontCursor(ws->dpy, XC_bottom_side);
	else if (name == "SE_RESIZE")
	  cursor = XCreateFontCursor(ws->dpy, XC_bottom_right_corner);
	else if (name == "SW_RESIZE")
	  cursor = XCreateFontCursor(ws->dpy, XC_bottom_left_corner);
	else if (name == "TEXT")
	  cursor = XCreateFontCursor(ws->dpy, XC_xterm);
	else if (name == "W_RESIZE")
	  cursor = XCreateFontCursor(ws->dpy, XC_left_side);
	else if (name == "WAIT")
	  cursor = XCreateFontCursor(ws->dpy, XC_watch);
	if (cursor != 0) XDefineCursor(ws->dpy, xw, cursor);
    } 
 

