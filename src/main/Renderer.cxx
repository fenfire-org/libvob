/*
Renderer.cxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *                  2004, Matti J. Katila
 * 
 *    This file is part of Libvob.
 *    
 *    Libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#include <sys/time.h>
#include <GL/gl.h>

#include <vob/Renderer.hxx>
#include <vob/glerr.hxx>



double getTime() {
  struct timeval t;
  gettimeofday(&t, 0);
  return t.tv_usec*1E-6 + t.tv_sec;
}

// this made a compile error of Vob::Vob::foo
// so the compiler was thinking that we are now in 
// the Vob namespace..
//
//using namespace Vob;

namespace Vob {

    // For glerr.hxx
    int vobglErrorVariable;

    namespace Vobs {
	DBGVAR(dbg_irregularquad, "IrregularQuad");
	DBGVAR(dbg_vfillets, "VFillets");
	DBGVAR(dbg_calllist, "Calllist");
	DBGVAR(dbg_text, "Text");

	std::string testStateRetainCorrect;
    }

    namespace Dicer {
	DBGVAR(dbg, "Dicer");
    }

    namespace Vobs {
	DBGVAR(dbg_paperquad, "PaperQuad");
    }

    namespace Irregu {
	DBGVAR(dbg_irregu, "Irregu");
    }

    namespace Geom {
	DBGVAR(dbg_fillets, "Fillets");
    }

    namespace Primitives {
	DBGVAR(dbg_buoyoncircle, "BuoyOnCircle");
    }


namespace CurrentFPS {
    // bool showFPS;
    double current_fps;
    double current_fps5; // average of last 5 frames
    int count;
    double last_time[NCounts];
}

bool Renderer::sceneMoving;

DBGVAR(dbg_renderer, "Renderer");
DBGVAR(dbg_fps, "Renderer.fps");
DBGVAR(dbg_fps_cout, "Renderer.fps_cout");

void Renderer::setStandardCoordinates(Vec wh) {
    int w = (int)wh.x;
    int h = (int)wh.y;
    glViewport(0, 0, w, h);
    GLERR;
    glMatrixMode(GL_PROJECTION);
    GLERR;
    glLoadIdentity();
    GLERR;
    glOrtho(0, w, h, 0, 10000, -10000);
    GLERR;
    glMatrixMode(GL_MODELVIEW);
    GLERR;
    glLoadIdentity();
    GLERR;
    DBG(dbg_renderer) << "stdcoords done\n";

}

void Renderer::renderScene(int * codes, 
			ObjectStorer<Vob0> &r0s,
			ObjectStorer<Vob1> &r1s,
			ObjectStorer<Vob2> &r2s,
			ObjectStorer<Vob3> &r3s,
			ObjectStorer<Vob> &rNs
			) {
    this->renderSceneRecursively((Coorder *) (&(this->coordset)), (int *)codes, r0s, r1s, r2s,r3s, rNs);
}
void Renderer::renderSceneRecursively(Coorder * coords, 
			   int * codes, 
			ObjectStorer<Vob0> &r0s,
			ObjectStorer<Vob1> &r1s,
			ObjectStorer<Vob2> &r2s,
			ObjectStorer<Vob3> &r3s,
			ObjectStorer<Vob> &rNs
			) {
    int i=0; 
    
    DBG(dbg_renderer) << "renderScene\n";
    while(codes[i] != 0) {
	DBG(dbg_renderer) << "Rendercode "<<i<<" "<<codes[i]<<"\n";
	int code = codes[i] & ~RMASK;
	if((codes[i] & RMASK) == RENDERABLE0) {
	    DBG(dbg_renderer) << "rend0 "<<r0s[code]->getVobName()<<" "<<r0s[code]<<"\n";
	    r0s[code]->render0();
	    i += 1;
	}
	else if((codes[i] & RMASK) ==  RENDERABLE1) {
	    Transform *cs1 = coords->get(codes[i+1]);

	    if (dbg_renderer)
		if (cs1) cs1->dump(std::cout);
		else DBG(dbg_renderer) <<"cs1 is NULL!  tried to get: "
				       <<codes[i+1]<<"\n";

	    DBG(dbg_renderer) << "rend1 "<<r1s[code]->getVobName()<<" "<<r1s[code]<<": "<<codes[i+1]<<" "<<cs1<<"\n";
	    if(cs1)
		r1s[code]->render1(*cs1);
	    i += 2;
	}
	else if((codes[i] & RMASK) == RENDERABLE2) {
	    Transform *cs1 = coords->get(codes[i+1]);
	    Transform *cs2 = coords->get(codes[i+2]);

	    if (dbg_renderer) {
		if (cs1) cs1->dump(std::cout);
		else DBG(dbg_renderer) <<"cs1 is NULL!\n";

		if (cs2) cs2->dump(std::cout);
		else DBG(dbg_renderer) <<"cs2 is NULL!\n";
	    }
	    DBG(dbg_renderer) << "rend2 "<<r2s[code]->getVobName()<<" "<<r2s[code]<<": "
		    <<codes[i+1]<<" "<<cs1<<" "<<codes[i+2]<<" "<<cs2<<"\n";
	    if(cs1 && cs2)
		r2s[code]->render2(*cs1, *cs2);
	    i += 3;
	}
	else if((codes[i] & RMASK) == RENDERABLE3) {
	    const Transform *t[3];
	    t[0] = coords->get(codes[i+1]);
	    t[1] = coords->get(codes[i+2]);
	    t[2] = coords->get(codes[i+3]);
	    DBG(dbg_renderer) << "rend3 "<<r3s[code]->getVobName()<<" "<<r3s[code]<<": "
			      <<codes[i+1]<<" "<<t[0]<<" "<<codes[i+2]<<" "<<t[1]<<" " 
			      <<t[2]<<" "<<codes[i+3]<<"\n";
	    if(t[0] && t[1] && t[2])
		r3s[code]->render(t, 3);
	    i += 4;
	}
	else if((codes[i] & RMASK) == RENDERABLEN) {
	    int ncs = codes[i+1];
	    const Transform *t[ncs];
	    for(int c = 0; c<ncs; c++) {
		t[c] = coords->get(codes[i+2+c]);
		if(t[c] == 0) goto SKIP;
	    }
	    rNs[code]->render(t, ncs);
	SKIP:
	    i += 2 + ncs;
	}
	else if((codes[i] & RMASK) == RENDERABLE_VS) {
	    int childVsId = codes[i++] & ~RMASK;
	    DBG(dbg_renderer) << "Push child ------- "<<childVsId<<"\n";

	    ChildVS * childVS = this->childVsStorer->get(childVsId);
	    int coorderResult = codes[i++];

	    Coorder * temp = coords;
	    coords = (coords->getChildCoorder(coorderResult));
	    this->renderSceneRecursively(coords, &(childVS->mapCodes[0]), r0s, r1s, r2s, r3s, rNs);
	    coords = temp;
	    DBG(dbg_renderer) << "Pop child ------- "<<childVsId<<"\n";
	}
	else {
	    // We have a problem
	    cout << "HELP!\n";
	    std::cerr << "We seem to have a problem!!! "<<codes[i]<<"\n";
	    cout << "HELP!\n";
	    exit(19);
	}
    }
    DBG(dbg_renderer) << "renderScene end\n";
}

void Renderer::fpsTick() {
  if(dbg_fps) {
    double new_time = getTime();
    CurrentFPS::current_fps = 1.0/(new_time - CurrentFPS::last_time[CurrentFPS::count]);
    DBG(dbg_fps_cout) << "CurrentFPS: " << CurrentFPS::current_fps << "\n";
    CurrentFPS::count ++;
    CurrentFPS::count %= CurrentFPS::NCounts;
    CurrentFPS::current_fps5 = CurrentFPS::NCounts *
				1.0/(new_time - CurrentFPS::last_time[CurrentFPS::count]);
    CurrentFPS::last_time[CurrentFPS::count] = new_time;

  }
}
   

}

