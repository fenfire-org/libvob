/*
trycfillet.cxx
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

#include <GL/glut.h>
#include <math.h>
#include <iostream>

// #define DBG(x) std::cerr 
#define DBG(x) if(0) std::cerr 

#include <vob/geom/Fillets.hxx>

using namespace Vob;
using namespace Vob::Geom;
using std::cout;
using std::cerr;

int main0() {
    CircleFillet f(
	    Vec(100,200),
	    100,
	    .3,
	    1,
	    250,
	    10);
    CircleFillet f2(
	    Vec(100,200),
	    100,
	    .3,
	    -1,
	    250,
	    10);
    cerr << f.r << " " << f.ctr << " "<<f.filletcenter <<" "
	    << f.filletrad << " " <<
	    f.dirconn<<" "<<f.dirtang<<"\n";
    for(float a = 0; a < 2*M_PI; a+= .01) {
	Vec rv = dirVec(a) * f.rad(dirVec(a));
	cout << rv.x << " "<<rv.y<< "\n";
    }
    for(float a = 0; a < 2*M_PI; a+= .01) {
	Vec rv = dirVec(a) * f2.rad(dirVec(a));
	cout << rv.x << " "<<rv.y<< "\n";
    }
}

float a2 = .31;
float sd = .001;
float d = sd;

void vert(Vec r) {
    glVertex2f(r.x / 300 ,  r.y / 300);
}

void display() {
    a2 += d;
    if(a2 >= 1.4) d = -sd;
    if(a2 <= .31) d = +sd;

    FilletSpan sp(
		Vec(0,0),
		100,
		.3, 250, 1,
		a2, 200, 2);
    FilletSpan sp2(
		Vec(0,0),
		100,
		a2, 200, 2,
		.3 + 2*M_PI, 250, 1
		);
    cerr << "F: "<<sp.f<<" "<<sp.fangle << " " << sp.aang<<" "<<sp.bang<<"\n";
    glClear(GL_COLOR_BUFFER_BIT);
    glColor3f(1,1,1);
    glBegin(GL_LINE_STRIP);
    for(float a = 0; a < 1; a+= .001) {
	// Vec rv = dirVec(a) * sp.rad_blended(dirVec(a), BlendSimply());
	Vec rv = sp2.point(a, BlendSimply());
//	cout << rv.x << " "<<rv.y<< "\n";

	vert(rv);
   }
    for(float a = 0; a < 1; a+= .001) {
	// Vec rv = dirVec(a) * sp2.rad_blended(dirVec(a), BlendSimply());
	Vec rv = sp.point(a, BlendSimply());
//	cout << rv.x << " "<<rv.y<< "\n";
	vert(rv);
    }
    glEnd();
    glutSwapBuffers();
}

void idle() {
    glutPostRedisplay();
}

int main(int argc, char *argv[]) {
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_RGB | GLUT_DOUBLE);
    glutCreateWindow("Fillet test");
    glutDisplayFunc(display);
    glutIdleFunc(idle);
    glutMainLoop();
}
