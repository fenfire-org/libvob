/*
Os-GLX.cxx
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

/** GLX means either the GL X11 extension of OpenGL
 * or GNU/LinuX, whichever you prefer.
 */

#include <sys/time.h>
#include <time.h>
#include <unistd.h>
#include <fcntl.h>

#include <vector>
#include <map>
#include <iostream>

#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/keysym.h>

// For setCursor()
#include <X11/Xlib.h>
#include <X11/cursorfont.h>
//

#include <vob/os/Os.hxx>
#include <vob/Debug.hxx>





#define BARF(m) { cerr << m << "\n"; exit(18); }

int xerrorhandler(Display *dpy, XErrorEvent *xee) {
    char buf[1024];
    XGetErrorText(dpy, xee->error_code, buf, sizeof(buf));
    std::cerr << "X Error: " << buf << "\n";
    exit(10);
}
int xioerrorhandler(Display *dpy) {
    std::cerr << "X IO Error on display: " << XDisplayName(NULL) << "\n";
    exit(10);
}

namespace Vob {
namespace Os {

    DBGVAR(dbg, "Os");
    DBGVAR(dbg_ctrl, "Os.controlEvents");

    using std::cerr;
    using std::cout;
    using std::string;
    using std::vector;

    /** Attributes for db visual.
     */
    static int doubleBufferAttributes[] = {
	GLX_RENDER_TYPE, GLX_RGBA_BIT,
	GLX_DOUBLEBUFFER, 1,
	GLX_RED_SIZE, 1,
	GLX_GREEN_SIZE, 1,
	GLX_BLUE_SIZE, 1,

// No alpha by default - should have option to get it
// but too many cards / configs don't have it
//	GLX_ALPHA_SIZE, 1, 
//
	GLX_DEPTH_SIZE, 1, 
	GLX_STENCIL_SIZE, 1, 
	GLX_DRAWABLE_TYPE, GLX_WINDOW_BIT,
	None
    };

    /** Attributes for db visual.
     */
    static int pbufferAttributes[] = {
	GLX_RENDER_TYPE, GLX_RGBA_BIT,
	GLX_DOUBLEBUFFER, 0,
	GLX_RED_SIZE, 1,
	GLX_GREEN_SIZE, 1,
	GLX_BLUE_SIZE, 1,
	GLX_DEPTH_SIZE, 1, 
	GLX_STENCIL_SIZE, 1, 
	GLX_DRAWABLE_TYPE, GLX_PBUFFER_BIT,
	None
    };

    /** Attributes for db visual for ChooseVisual.
     */
    static int old_dbattrs[] = {
	GLX_RGBA,
	GLX_DOUBLEBUFFER, 
	GLX_RED_SIZE, 1,
	GLX_GREEN_SIZE, 1,
	GLX_BLUE_SIZE, 1,

// No alpha by default - should have option to get it
// but too many cards / configs don't have it
//	GLX_ALPHA_SIZE, 1,

	GLX_DEPTH_SIZE, 1, 
	GLX_STENCIL_SIZE, 1, 
	None
    };

    /** Attributes for sb pixmap visual for ChooseVisual.
     */
    static int old_pxattrs[] = {
	GLX_RGBA,
	GLX_RED_SIZE, 1,
	GLX_GREEN_SIZE, 1,
	GLX_BLUE_SIZE, 1,
	GLX_DEPTH_SIZE, 1, 
	GLX_STENCIL_SIZE, 1, 
	None
    };

    struct LXWindow;

    typedef ::Window Win; // work around a syntax error.

    /** Return the current time as milliseconds after some unit.
     */
    long curTime() {
	struct timeval t;
	gettimeofday(&t, 0);
	return t.tv_usec/1000 + t.tv_sec * 1000;
    }
    struct Timeout {
	long time;
	Win window;
	int id;
	Timeout(int msOffs, Win w, int id) : window(w), id(id) {
	    time = curTime() + msOffs;
	}
    };

    int g(Display *dpy, GLXFBConfig conf, int arg) {
	int par;
	if(glXGetFBConfigAttrib(dpy,conf,arg,&par)!=Success) 
	    { printf("ERROR for %d\n", arg);
		throw Problem();
	    }
	return par;
    }

    struct LXWindowSystem : public Os::WindowSystem {

	/** A pipe, used to interrupt waiting of eventloop.
	 */
	int interruptPipe[2];

	fd_set readFds;
	fd_set tmpReadFds;
	int fdsMax;

	Display              *dpy;
	XSetWindowAttributes swa;
	int swaMask;
	int numReturned;

	// window
	GLXFBConfig *dbFbConfig;
	XVisualInfo *dbVinfo;
	GLXContext dbContext;

	// pbuffer
	GLXFBConfig *pbFbConfig;
	GLXContext pbContext;

	// pixmap
	XVisualInfo *pxVinfo;
	GLXContext pxContext;

	vector<Timeout> timeouts;

	vector<LXWindow *> windows;
	// vector<IdleTasks *> idletasks;
	
	std::map<Win, LXWindow *> windowsByX;

	/** The drawable to use to create pixmaps.
	 */
	Drawable pixmapDrawable;

	LXWindowSystem() {
	    if(pipe(interruptPipe) < 0) {
		perror("Making interrupt pipe");
		exit(1);
	    }
	    fcntl(interruptPipe[0], F_SETFL, O_NONBLOCK);
	    fcntl(interruptPipe[1], F_SETFL, O_NONBLOCK);

	    if(!(dpy = XOpenDisplay( NULL ))) BARF("Couldn't open display");
            XSetErrorHandler(xerrorhandler);
            XSetIOErrorHandler(xioerrorhandler);


	    FD_ZERO(&readFds);
	    FD_SET(ConnectionNumber(dpy), &readFds);
	    FD_SET(interruptPipe[0], &readFds);
	    fdsMax = (ConnectionNumber(dpy) >? interruptPipe[0]) + 5;


	    int nel;

	    pbFbConfig = 0;

	    dbFbConfig = glXChooseFBConfig(dpy, DefaultScreen(dpy), 
			doubleBufferAttributes, &nel);

	    if(dbFbConfig) {
		// Got it -- use the new way
		dbContext = glXCreateNewContext(dpy, dbFbConfig[0], GLX_RGBA_TYPE, 
				0, GL_TRUE);


		dbVinfo = glXGetVisualFromFBConfig(dpy, dbFbConfig[0]);

		pbFbConfig = glXChooseFBConfig(dpy, DefaultScreen(dpy),
			    pbufferAttributes, &nel);
		if(!pbFbConfig) BARF("Can't get pbuffer config");

		pbContext = glXCreateNewContext(dpy, pbFbConfig[0], 
			GLX_RGBA_TYPE, 
			dbContext, GL_TRUE);
	    } else {
		// Didn't -- use pixmap for off-screen and
		// glXChooseVisual

		dbVinfo = glXChooseVisual(dpy, DefaultScreen(dpy), 
				    old_dbattrs);

		if(!dbVinfo) BARF("Can't get dblbuf visual");

		dbContext = glXCreateContext(dpy, dbVinfo, 0, GL_TRUE);

 
	    }

	    // Get this in any case -- ATI doesn't
	    // let us use pbuffers on radeons...
	    pxVinfo = glXChooseVisual(dpy, DefaultScreen(dpy), 
				old_pxattrs);
	    if(!pxVinfo) BARF("Can't get dblbuf visual");
	    pxContext = glXCreateContext(dpy, pxVinfo, dbContext, GL_TRUE);

	    swa.border_pixel = 0;

	    swa.colormap = XCreateColormap(dpy, DefaultRootWindow(dpy), 
			    dbVinfo->visual,
			    AllocNone);

	    swa.background_pixmap = None;
	    swa.background_pixel = 0;
	    swa.event_mask = (StructureNotifyMask | 
		    ButtonPressMask |
		    ButtonReleaseMask | 
		    ButtonMotionMask |
		    KeyPressMask | 
		    KeyReleaseMask |
		    ExposureMask |
		    VisibilityChangeMask |
		    StructureNotifyMask |
		    SubstructureNotifyMask |
		    FocusChangeMask |
		    PropertyChangeMask |
		    PointerMotionMask ); // motion makes a lot of events :)


	    // swaMask = (CWBorderPixel | CWEventMask);
	    swaMask = (CWColormap|CWEventMask
			| CWBorderPixel | 
			CWBackPixmap 
			);

	    pixmapDrawable = XCreateWindow(dpy, 
			RootWindow(dpy, dbVinfo->screen),
			0, 0, 1, 1,
			0, dbVinfo->depth, InputOutput, 
			dbVinfo->visual, 
			swaMask, &swa
		    );

	}


	Os::Window *openWindow(int x, int y, int w, int h);
	Os::RenderingSurface *openStableOffScreen(int w, int h);


	void interrupt();
	bool eventLoop(bool wait);

	/*
	void addIdle(IdleTasks *task) {
	    idletasks.insert(idletasks.end(), task);
	}
	*/

    private:

    };

    // static char eventStringBuf[256];
    //
    struct LXPixmap : public Os::RenderingSurface {
	LXWindowSystem *ws;

	Pixmap pix;
	int w, h;

	LXPixmap(LXWindowSystem *ws, int w, int h) : ws(ws) {
	
	    pix = XCreatePixmap(ws->dpy, 
		    ws->pixmapDrawable,
		    w, 
		    h,
		    ws->pxVinfo->depth);

	    DBG(dbg) << "Created LXPixmap: "<<pix<<" "<<ws->pixmapDrawable<<" "<<
		ws->pxVinfo->depth<<" "<<w<<" "<<h<<"\n";
	    if(pix == 0)
		throw "Can't create pixmap";
	    this->w = w;
	    this->h = h;
	}

	bool setCurrent() {
	    DBG(dbg) << "setcurrent pixmap "<<pix<<" "<<ws->pxContext<<"\n";
	    bool ret = glXMakeCurrent(ws->dpy, pix, ws->pxContext);
	    DBG(dbg) << "setcurrent pixmap ret: "<<ret<<"\n";
	    if(!ret) throw "Can't draw into pixmap";
	    return ret;
	}
	bool releaseCurrent() {
	    DBG(dbg) << "Releasecurrent pixmap "<<pix<<" "<<ws->pxContext<<"\n";
	    bool ret = glXMakeCurrent(ws->dpy, None, NULL);
	    DBG(dbg) << "Releasecurrent pixmap ret: "<<ret<<"\n";
	    if(!ret)throw "Can't draw into pixmap";
	    return ret;
	}

	void getSize(int *xywh) {
	    xywh[0] = 0;
	    xywh[1] = 0;
	    xywh[2] = w;
	    xywh[3] = h;
	}
    };



    // static char eventStringBuf[256];
    //
    struct LXPBuffer : public Os::RenderingSurface {
	LXWindowSystem *ws;

	GLXPbuffer pbuf;
	int w, h;

	LXPBuffer(LXWindowSystem *ws, GLXPbuffer pbuf,
			    int w, int h) : ws(ws), pbuf(pbuf) {

	    unsigned val;
	    glXQueryDrawable(ws->dpy, pbuf, GLX_PBUFFER_WIDTH, &val);
	    this->w = val;

	    glXQueryDrawable(ws->dpy, pbuf, GLX_PBUFFER_HEIGHT, &val);
	    this->h = val;

	    DBG(dbg) << "PBuffer reserved "<<this->w<<" "<<this->h<<"\n";
	    // XXX getting weird results...
	    this->w = w;
	    this->h = h;
	}

	bool setCurrent() {
	    DBG(dbg) << "setcurrent pbuf "<<pbuf<<" "<<ws->pbContext<<"\n";
	    bool ret = glXMakeCurrent(ws->dpy, pbuf, ws->pbContext);
	    DBG(dbg) << "setcurrent pbuf ret: "<<ret<<"\n";
	    if(!ret) throw "Can't draw into pbuf";
	    return ret;
	}
	bool releaseCurrent() {
	    DBG(dbg) << "Releasecurrent pbuf "<<pbuf<<" "<<ws->pbContext<<"\n";
	    bool ret = glXMakeCurrent(ws->dpy, None, NULL);
	    DBG(dbg) << "Releasecurrent pbuf ret: "<<ret<<"\n";
	    if(!ret) throw "Can't draw into pbuf";
	    return ret;
	}

	void getSize(int *xywh) {
	    xywh[0] = 0;
	    xywh[1] = 0;
	    xywh[2] = w;
	    xywh[3] = h;
	}
    };

    LXPBuffer *createPBuffer(LXWindowSystem *ws, int w, int h) {
	    int attrs[] = {
		GLX_PBUFFER_WIDTH, w,
		GLX_PBUFFER_HEIGHT, h,
		GLX_PRESERVED_CONTENTS, 1,
		GLX_LARGEST_PBUFFER, 0,
		0
	    };
	    GLXPbuffer pbuf = glXCreatePbuffer(ws->dpy, ws->pbFbConfig[0], attrs);
	    DBG(dbg) << "Pbuf alloc: "<<w<<" "<<h<<" "<<pbuf<<"\n";
	    if(pbuf == 0) 
		return 0;
            XVisualInfo *visinfo = glXGetVisualFromFBConfig(ws->dpy, ws->pbFbConfig[0]);
            if (!visinfo) {
                throw "No visinfo in pbuffer!";
            }
            XFree(visinfo);
	    return new LXPBuffer(ws, pbuf, w, h);
    }

    struct LXWindow : public Os::Window {
	LXWindowSystem *ws;
	::Window xw;
	bool needRepaint;
	Eventhandler *eventhandler;

	virtual void setEventHandler(Eventhandler *h) {
	    eventhandler = h;
	    DBG(dbg) << "Set window "<<xw<<" eventhandler to "<<((int)h)<<"\n";
	}

	LXWindow(LXWindowSystem *ws, int x, int y, int w, int h) : ws(ws), 
		    needRepaint(true), eventhandler(0) {
	    DBG(dbg) << "Create win: "<<ws->dbVinfo->depth<<" "<<ws->dbVinfo->visual << " ";
	    DBG(dbg) << ws->dbVinfo->visualid <<"\n";
	    DBG(dbg) << "Coords: "<<x<<" "<<y<<" "<<w<<" "<<h<<"\n";
	    xw = XCreateWindow( ws->dpy, 
			RootWindow(ws->dpy, ws->dbVinfo->screen),
			x, y, w, h,
			0, ws->dbVinfo->depth, InputOutput, 
			ws->dbVinfo->visual, 
			ws->swaMask, &ws->swa
		    );
	    DBG(dbg) << "Got window "<<xw<<"\n";

	}

	void mapThisWindow() {
	    DBG(dbg) << "mapwindow "<<xw<<"\n";
	    XMapWindow( ws->dpy, xw );
	    while(1) {
		XEvent ev;
		XWindowEvent( ws->dpy, xw, 
			ExposureMask                  |
			VisibilityChangeMask            |
			StructureNotifyMask,
			&ev);
		DBG(dbg) << "Windowevent: "<<ev.type<<"\n";
		if(ev.type == MapNotify) {
		    DBG(dbg) << "Got mapnotify... getting out\n";
		    break;
		}
	    }
	    /*
	     * glXWaitX();
	     * setCurrent();
	    */
	}


	virtual bool setCurrent() {
	    DBG(dbg) << "setcurrent "<<xw<<"\n";
	    // Can't check for GL errors; we might not have a current context!
	    // GLERR
	    bool ret = glXMakeCurrent(ws->dpy, xw, ws->dbContext);
	    // GLERR
	    DBG(dbg) << "setcurrent returned: "<<ret<<"\n";
	    return ret;
	}

	virtual bool releaseCurrent() {
	    DBG(dbg) << "release "<<xw<<"\n";
	    // Can't check for GL errors; we might not have a current context!
	    // GLERR
	    bool ret = glXMakeCurrent(ws->dpy, None, NULL);
	    // GLERR
	    DBG(dbg) << "release returned: "<<ret<<"\n";
	    return ret;
	}

	virtual void swapBuffers() {
	    glXSwapBuffers(ws->dpy, xw);
	}


	virtual void move(int x, int y) {
	    XMoveWindow(ws->dpy, xw, x, y);
	}
	virtual void resize(int w, int h) {
	    XResizeWindow(ws->dpy, xw, w, h);
	}
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

	void getSize(int *xywh) {
	    // cout << "GetGeometry "<<int(ws)<<" "<<int(ws->dpy)<<" "<<int(xw)<<"\n";
	    XWindowAttributes attrs;
	    XGetWindowAttributes(ws->dpy, xw, &attrs);
	    xywh[0] = attrs.x;
	    xywh[1] = attrs.y;
	    xywh[2] = attrs.width;
	    xywh[3] = attrs.height;
	}

	void addTimeout(int ms, int id) {
	    ws->timeouts.insert(ws->timeouts.end(), Timeout(ms, xw, id));
	}

	int mapButton(int button) {
	    switch(button) {
		case Button1: return 1;
		case Button2: return 2;
		case Button3: return 3;
		case Button4: return 4;
		case Button5: return 5;
		default: return 0;
	    }
	}

	int mapButtonStateMask(int button) {
	    if(button & Button1Mask) return 1;
	    if(button & Button2Mask) return 2;
	    if(button & Button3Mask) return 3;
	    if(button & Button4Mask) return 4;
	    if(button & Button5Mask) return 5;
	    return 0;
	}

	void deliverTimeout(int id) {
	    if(!eventhandler) return;
	    eventhandler->timeout(id);
	}

	int modmask(int state) {
	    int mask = 0;
	    if(state & ShiftMask)
	        mask |= 1;
	    if(state & ControlMask)
	        mask |= 2;
	    return mask;
	}

	void deliverEvent(XEvent *e) {
	    DBG(dbg_ctrl) << "event "<<xw<<" "<<e->type<<"\n";
	    if(!eventhandler) {
		DBG(dbg_ctrl) << "No event handler for window\n";
		return;
	    }
	    switch(e->type) {
	    case KeyPress:  {
		char buf[256];
		const char *str = buf;
		KeySym keysym;
		int ret = XLookupString(&e->xkey, buf, 256, &keysym, NULL);
		if(ret >= 254) {
		    cout << "Odd number of keypress chars into buffer";
		    return;
		}
		if(ret >= 0)
		    buf[ret] = 0;

		if(!ret || buf[0] < 32 || buf[0] == 127) {
		    str = XKeysymToString(keysym);
		    if(str == 0) {
			cout << "Null keystroke\n";
			return;
		    }
		    if(dbg_ctrl) {
			for(const char *c = str; *c != 0; c++) {
			    cout << (int)*c << " ";
			}
			cout << "\n";
		    }
		}
		char buf2[256];
		if(e->xkey.state & ControlMask) {
		    sprintf(buf2, "Ctrl-%s", str);
		    str = buf2;
		}
		if(e->xkey.state & Mod1Mask) {
		    sprintf(buf2, "Alt-%s", str);
		    str = buf2;
		}
		DBG(dbg_ctrl) << "Sending keystroke '"<<str<<"'\n";
		eventhandler->keystroke(str);
		break;
	    }
            case KeyRelease: {
                DBG(dbg_ctrl) << "Key released";
		break;
            }
	    case ButtonPress: case ButtonRelease: {
	      int button = mapButton(e->xbutton.button);
	      DBG(dbg_ctrl) << "Button: " << e->xbutton.button << " " <<e->xbutton.x << " "
		    << e->xbutton.y<<"\n";
              DBG(dbg_ctrl) << "  Control: " << int(e->xbutton.state & ControlMask) 
                            << ", shift: " << int(e->xbutton.state & ShiftMask) <<"\n";
	      eventhandler->mouse(e->xbutton.x, e->xbutton.y, button, 
			(e->type == ButtonPress ? 
				eventhandler->PRESS : 
				eventhandler->RELEASE),
			modmask(e->xbutton.state)
			 );
	      break;
	    }
	    case MotionNotify: {
		int button = mapButtonStateMask(e->xmotion.state);
		DBG(dbg_ctrl) << "Motion: " << button << " " <<e->xmotion.x << " "
		    << e->xmotion.y<<"\n";
		eventhandler->mouse(e->xmotion.x, e->xmotion.y, button, 
				eventhandler->MOTION,
				modmask(e->xbutton.state)
				);

	       break;
	    }
	    case Expose:
		DBG(dbg_ctrl) << "Expose\n";
		eventhandler->repaint();
		break;
	    case ConfigureNotify:
		DBG(dbg_ctrl) << "Configurenotify\n";
		eventhandler->repaint();
		break;
	    case MapRequest:
		DBG(dbg_ctrl) << "MapRequest\n";
		eventhandler->repaint();
		break;
	    case MapNotify:
		DBG(dbg_ctrl) << "MapNotify\n";
		eventhandler->repaint();
		break;
	    default:
		    DBG(dbg_ctrl) << "Unknown event "<<e->type<<"\n";
	    }
	}
    };

 

    Os::Window *LXWindowSystem::openWindow(int x, int y, int w, int h) {
	LXWindow *win = new LXWindow(this, x, y, w, h);
	windows.insert(windows.end(), win);
	windowsByX[win->xw] = win;
	win->mapThisWindow();
	return win;
    }

    Os::RenderingSurface *LXWindowSystem::openStableOffScreen(int w, int h)
    { 
	Os::RenderingSurface *ret = 0;
	if(pbFbConfig) {
	    ret = createPBuffer(this, w, h);
	    if(ret != 0) {
		DBG(dbg) << "Pixbuf successfully created\n";
		return ret;
	    }
	}
	return new LXPixmap(this, w, h);
    }

    WindowSystem *WindowSystem::instance = 0;
    WindowSystem *WindowSystem::getInstance() {
	if(!instance)
	    instance = new LXWindowSystem();
	return instance;
    }

    char intr = 'S';
    void LXWindowSystem::interrupt() {
	write(interruptPipe[1], &intr, 1);
    }

    bool LXWindowSystem::eventLoop(bool wait) {
	DBG(dbg) << "In C++ eventloop : "<<wait<<"\n";
	bool ret = false;
	// We don't want to block;
	while(1) {
	    DBG(dbg) << "Start loop\n";

	    bool eventsWaiting = XEventsQueued(dpy, QueuedAfterFlush);

	    // If we should not wait and there are no events, return now
	    if(!wait && !eventsWaiting) return ret;
	    // We only wait once: on the next iteration of the loop,
	    // we'll return
	    wait = false;

	    DBG(dbg) << "Wait for next event, interrupt or timeout\n";

	    // If no events were waiting, now's the time to 
	    // wait for them.
	    if(!eventsWaiting) {
		// Visit Java at least every .5 seconds
		timeval timeout;
		timeout.tv_sec = 0;
		timeout.tv_usec = 500 * 1000;

		// If we're waiting for a timeout, sleep less.
		if(timeouts.size() && !eventsWaiting) {
		    long t = curTime();
		    for(unsigned i=0; i<timeouts.size(); i++) {
			if(timeouts[i].time < t) {
			    int id = timeouts[i].id;
			    LXWindow *w = windowsByX[timeouts[i].window];
			    timeouts.erase(timeouts.begin()+i);
			    w->deliverTimeout(id);
			    return ret;
			} else {
			    int ms = timeouts[i].time - t;
			    if(ms < timeout.tv_usec / 1000)
				timeout.tv_sec = 1000 * ms;
			}
		    }
		}
		// Call select to wait for something to happen.
		tmpReadFds = readFds;
		select(fdsMax, &tmpReadFds, 0, 0, &timeout);
		char b[4];
		DBG(dbg) << "Emptying interrupt pipe\n";
		while(read(interruptPipe[0], &b, 4) > 0) {
		    DBG(dbg) << "Got "<<b[0]<<" "<<b[1]<<" "<<b[2]<< " "<<b[3]<<"\n";
		    wait = false;
		}
		DBG(dbg) << "Empty\n";
		continue;
	    }


	    XEvent e;
	    XNextEvent(dpy, &e);
	    ret = true;
	    LXWindow *w = windowsByX[e.xany.window];
	    if(!w) {
		DBG(dbg) << "Event for unknown window\n";
		continue;
	    }
	    // Compress motion events to a single event.
	    // We may want to make this optional later on.
	    // Code inspired by window maker
	    if(e.type == MotionNotify) {
		// See if there's another event coming...
		while(XPending(dpy)) {
		    XEvent tmp;
		    XPeekEvent(dpy, &tmp);
		    // ...that's also a motion event for the same ..
		    if(tmp.type == MotionNotify &&
			tmp.xmotion.window == e.xmotion.window &&
			tmp.xmotion.subwindow == e.xmotion.subwindow) {
			// .. and replace the original event by it
			XNextEvent(dpy, &e);
		    } else 
			break;
		}
	    }
	    w->deliverEvent(&e);
	}
    }

}
}
