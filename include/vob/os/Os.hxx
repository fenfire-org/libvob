/*
Os.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of LibVob.
 *    
 *    LibVob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    LibVob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with LibVob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#ifndef VOB_OS_OS_HXX
#define VOB_OS_OS_HXX

namespace Vob {
namespace Os {

    class Eventhandler {
    public:
	enum {
	    PRESS = 1,
	    MOTION = 2,
	    RELEASE = 3
	};
	virtual ~Eventhandler() { } // not called by Window..
	virtual void keystroke(const char *str) {}
	virtual void mouse(int x, int y, int button, int type, 
			    int modifiers) {}
	virtual void timeout(int id) {}
	virtual void windowClosed() {}
	virtual void resize(int w, int h) { repaint(); }
	virtual void repaint() { };
    };


    /** An abstract OpenGL rendering surface.
     */
    class RenderingSurface {
    public:
	virtual ~RenderingSurface() { };

	/** Enable this window for rendering.
	 */
	virtual bool setCurrent() = 0;
	virtual bool releaseCurrent() = 0;

	virtual void getSize(int *xywh) = 0;
	
	/** Swap buffers; may be no-op.
	 */
	virtual void swapBuffers() { };
    };


    /** An OpenGL rendering surface in a physical
     * window.
     */
    class Window : public RenderingSurface {
    protected:
    public:
	Window() { }
	virtual ~Window() { };
	virtual void setEventHandler(Eventhandler *h) = 0;
	/*
	virtual Eventhandler *getEventHandler() { 
	    return eventhandler;
	}
	*/

	// virtual pair<int, int> getSize() = 0;


	virtual void resize(int w, int h) = 0;
	virtual void move(int x, int y) = 0;
        virtual void setCursor(const std::string name) = 0;

	/** Call EventHandler->timeout at least X milliseconds 
	 * from now.
	 */
	virtual void addTimeout(int ms, int id) = 0;
    };


    /** The overall singleton wrapper class
     * for the current window system. OpenGL
     * is supported on different windowing systems
     * and those systems all have different ways
     * of creating the drawables and handling
     * events. This class is the basis of abstracting
     * those features that gzz needs.
     */
    class WindowSystem {
	static WindowSystem *instance;
    public:
	/** Get the instance of WindowSystem appropriate 
	 * for the current environment.
	 */
	static WindowSystem *getInstance();

	virtual ~WindowSystem() {}

	virtual Window *openWindow(int x, int y, int w, int h) = 0;
	/** Open a reliable off-screen rendering surface.
	 */
	virtual RenderingSurface *openStableOffScreen(int w, int h) = 0;

	/** Handle events.
	 * @param wait If true, this function will wait for the next event
	 *             and handle it before returning; if false, will return
	 *             immediately.
	 * @return Whether something happened
	 */
	virtual bool eventLoop(bool wait) = 0;

	/** Interrupt event loop waiting.
	 * Because there are things happening in multiple threads in Java,
	 * it is useful to be able to stop the event loop from waiting
	 * to perform idle tasks again.
	 * <p>
	 * This is the ONLY function here that may be called from another
	 * thread.
	 */
	virtual void interrupt() = 0;


    };


    class Problem { };


}
}

#endif
