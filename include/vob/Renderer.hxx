/*
Renderer.hxx
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

#include <vob/Vob.hxx>
#include <vob/Coorder.hxx>

#include <vob/Debug.hxx>
#include <vob/util/ObjectStorer.hxx>

namespace Vob {

PREDBGVAR(dbg_renderer);
PREDBGVAR(dbg_fps);
PREDBGVAR(dbg_fps_cout);

namespace CurrentFPS {
    // bool showFPS;
    extern double current_fps;
    extern double current_fps5; // average of last 5 frames
    extern int count;
    enum { NCounts = 5 };
    extern double last_time[NCounts];
}

class Renderer {
    Coorder coordset;
    ObjectStorer<ChildVS> *childVsStorer;
public:

    Renderer(TransformFactory fac, ObjectStorer<ChildVS> *cvs) : 
	coordset(fac, cvs), childVsStorer(cvs) { }

    static bool sceneMoving;

    static const int RENDERABLE0 = 0x1000000;
    static const int RENDERABLE1 = 0x2000000;
    static const int RENDERABLE2 = 0x3000000;
    static const int RENDERABLE3 = 0x4000000;
    static const int RENDERABLEN = 0x5000000;
    static const int RENDERABLE_VS = 0x6000000;
    static const int RMASK = 0x7000000;

    static void setStandardCoordinates(Vec wh);
    static void fpsTick();

    /** Set the coordinate systems.
     */
    void setPoints( int ninitCS, Transform **initCS,
		    int ninds, 
			     int *inds1, float *points1, 
			     int *interpinds, 
			     int *inds2, float *points2, 
			     float fract, bool show1) {
	coordset.clean();
	sceneMoving = (interpinds != 0);
	coordset.setPoints(ninitCS, initCS,
			ninds, inds1, points1, interpinds, inds2, points2, fract, show1);
    }

    void setPoints( int ninitCS, Transform **initCS,
		    int ninds, int *inds1, float *points1) {
	coordset.clean();
	sceneMoving = false;
	coordset.setPoints(ninitCS, initCS,
			ninds, inds1, points1, 0, 0, 0, 0, true);
    }

    /** Render a scene.
     * The codes array is in a "language" which is interpreted
     * through the vob arrays and the coordinate systems set
     * through setPoints.
     * <p>
     * The array contains the following types of variable-length
     * entries: 
     *
     * <pre>
     * 0 - end
     *
     * vob index | RENDERABLE0 - render a 0-parameter vob
     *
     * vob index | RENDERABLE1 - render a 1-parameter vob
     * coordsys0
     *
     * vob index | RENDERABLE2 - render a 2-parameter vob
     * coordsys0
     * coordsys1
     *
     * vob index | RENDERABLE3 - render a 3-parameter vob
     * coordsys0
     * coordsys1
     * coordsys2
     *
     * vob index | RENDERABLEN - render a N-parameter vob
     * ncoordsyses
     * coordsys0
     * ...
     * coordsysn
     *
     * ChildVoScene id | RENDERABLE_VS - render a child vob scene
     * coordsys reserved for child
     * ncoordsyses
     * coordsys0
     * ...
     * coordsysn
     * </pre>
     *
     *
     *
     * @param codes The vobs and the coordinate systems they use,
     *              encoded as explained above
     * @param r0s The 0-parameter renderables
     * @param r1s The 1-parameter renderables
     * @param r2s The 2-parameter renderables
     * @param r3s The 3-parameter renderables
     * @param rNs The N-parameter renderables
     */
    void renderScene(int *codes, 
			    ObjectStorer<Vob0> &r0s,
			    ObjectStorer<Vob1> &r1s,
			    ObjectStorer<Vob2> &r2s,
			    ObjectStorer<Vob3> &r3s,
			    ObjectStorer<Vob> &rNs
			    ) ;
    void renderSceneRecursively(Coorder *coords, int *codes, 
			    ObjectStorer<Vob0> &r0s,
			    ObjectStorer<Vob1> &r1s,
			    ObjectStorer<Vob2> &r2s,
			    ObjectStorer<Vob3> &r3s,
			    ObjectStorer<Vob> &rNs
			    ) ;
};


}
