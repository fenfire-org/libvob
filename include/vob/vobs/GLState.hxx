/*
GLState.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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

#ifndef VOB_VOBS_GLSTATE
#define VOB_VOBS_GLSTATE

#include <math.h>

#include <GL/gl.h>
#include <vob/Types.hxx>

#include <vob/Vec23.hxx>
#include <vob/VecGL.hxx>

#include <sstream>


#ifndef VOB_DEFINED
#define VOB_DEFINED(t)
#endif

namespace Vob {

//using namespace VecGL;

namespace Vobs {

//using namespace Vob::VecGL;

/** Cause the given GL matrix with to contain the matrix
 * for the given transformation.
 * Obviously the transformation has to be gl-performable, which
 * requires that the transformation be linear in homogeneous coords.
 */
struct TransMatrix {
    enum { NTrans = 1 };
    Token matrix;
    template<class F> void params(F &f) {
	f(matrix);
    }
    template<class T> void render(const T &t) const {
	glPushAttrib(GL_TRANSFORM_BIT);
	glMatrixMode(matrix);
	glLoadIdentity();
	t.performGL();
	glPopAttrib();
    }
};

VOB_DEFINED(TransMatrix);

extern std::string testStateRetainCorrect;

/** Test that a given set of Vobs pushes and pops the OpenGL state appropriately.
 * This Vob sets up the test.
 */
struct TestStateRetainSetup {
    enum { NTrans = 0 };
    template<class F> void params(F &f) {
	f();
    }
    void render() const {
	glPushAttrib(GL_CURRENT_BIT);
	glColor4d(.25, .50, .75, .125);
    }

};

VOB_DEFINED(TestStateRetainSetup);

/** Test that a given set of Vobs pushes and pops the OpenGL state appropriately.
 * This Vob performs the test and sets the test result.
 */
struct TestStateRetainTest {
    enum { NTrans = 0 };
    template<class F> void params(F &f) {
	f();
    }
    void render() const {
	glBegin(GL_TRIANGLES);
	glVertex3f(-100,-100,100);
	glVertex3f(-99,-100,100);
	glVertex3f(-100,-99,100);
	glEnd();
	GLdouble color[4];
	glGetDoublev(GL_CURRENT_COLOR, color);
#define TC(a, b) if(a != b) fail(#a, a, b); 
	TC(color[0], .25);
	TC(color[1], .50);
	TC(color[2], .75);
	TC(color[3], .125);

	glPopAttrib();
    }
private:
    void fail(char *s, double v0, double v1) const {
	std::ostringstream o;
	o << s << ": "<<v0<<" "<<v1<<"\n";
	testStateRetainCorrect += o.str();
    }

};

VOB_DEFINED(TestStateRetainTest);


}
}

#endif
