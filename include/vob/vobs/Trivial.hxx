/*
Trivial.hxx
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

#ifndef VOB_VOBS_TRIVIAL
#define VOB_VOBS_TRIVIAL

#include <math.h>

#include <GL/gl.h>
#include <vob/Types.hxx>

#include <vob/Vec23.hxx>
#include <vob/VecGL.hxx>

#include <vob/glerr.hxx>


#ifndef VOB_DEFINED
#define VOB_DEFINED(t)
#endif

namespace Vob {
namespace Vobs {

using namespace Vob::VecGL;

/** A line (in screen space)
 * drawn between the centers of two coordinate systems.
 */
struct LineConnector {
    enum { NTrans = 2 };
  
  /** Mounting points for connection. */
    float px0, py0, px1, py1;

    template<class F> void params(F &f) {
	f(px0, py0, px1, py1);
    }

    template<class T> void render(const T &t0, const T &t1) const {
        Pt box0 = t0.getSqSize();
        Pt box1 = t1.getSqSize();

	glBegin(GL_LINES);
	    t0.vertex(ZPt(box0.x*px0,box0.y*py0,0));
	    t1.vertex(ZPt(box1.x*px1,box1.y*py1,0));
	glEnd();
    }
};

VOB_DEFINED(LineConnector);

/** A line drawn from the center of the first coordinate system
 *  towards the center of another coordinate system and the end
 *  decorated with a circle.
 */
struct PinStub {
    enum { NTrans = 2 };
  
  /** Mounting points for connection. */
    float px0, py0, px1, py1, factor, radius;

    template<class F> void params(F &f) {
	f(px0, py0, px1, py1, factor, radius);
    }

    template<class T> void render(const T &t0, const T &t1) const {
        Pt box0 = t0.getSqSize();
        Pt box1 = t1.getSqSize();

	/** Transforms points through given transformations. */
	ZPt pt0 = t0.transform(ZPt(box0.x*px0, box0.y*py0, 0));
	ZPt pt1 = t1.transform(ZPt(box1.x*px1, box1.y*py1, 0));

	/** The distance between transformed points. */
	float len = sqrt(pow(pt1.x-pt0.x, 2)
			 + pow(pt1.y-pt0.y, 2));

	/** The end coordinates of PinStub. The length of pin is
	 * the distance between transformed point in factor f.
	 */
	float dx = (int)(((1-factor)*len*pt0.x + (factor)*len*pt1.x)/len);
	float dy = (int)(((1-factor)*len*pt0.y + (factor)*len*pt1.y)/len);

	glBegin(GL_LINES);
	    glVertex3f(pt0.x, pt0.y, 0);
	    glVertex3f(dx, dy, 0);
	glEnd();

	/** Render small circle as pin's ball. */
	glBegin(GL_POLYGON);
            for (double i=0; i<360; i+=36) {
	        float a = (float)((M_PI / 180) * (360-i));
	        glVertex3f(dx+cos(a)*radius, dy+sin(a)*radius, 0);
	    }
	glEnd();
    }
};

VOB_DEFINED(PinStub);


/** A non-filled rectangle.
 */
struct NonFilledRectangle {
    enum { NTrans = 1 };

    float lineWidth, r1, g1, b1, a1, r2, g2, b2, a2;

    template<class F> void params(F &f) {
	f(lineWidth, r1, g1, b1, a1, r2, g2, b2, a2);
    }

    template<class T> void render(const T &t) const {
	GLERR;
	glPushMatrix();
	if(t.performGL()) {
	    glPushAttrib(GL_CURRENT_BIT | GL_ENABLE_BIT);
	    glDisable(GL_TEXTURE_2D);
	    
	    Pt box = t.getSqSize();
	    
	    glColor4d(r1, g1, b1, a1);
	    
	    // top strip
	    glBegin(GL_QUAD_STRIP);
	    glVertex2f(0, 0);
	    glVertex2f(lineWidth, lineWidth);
	    glVertex2f(box.x, 0);
	    glVertex2f(box.x-lineWidth, lineWidth);
	    glEnd();
	    
	    // left strip
	    glBegin(GL_QUAD_STRIP);
	    glVertex2f(0, 0);
	    glVertex2f(0, box.y);
	    glVertex2f(lineWidth, lineWidth);
	    glVertex2f(lineWidth, box.y-lineWidth);
	    glEnd();
	    
	    glColor4d(r2, g2, b2, a2);

	    // right strip
	    glBegin(GL_QUAD_STRIP);
	    glVertex2f(box.x-lineWidth, lineWidth);
	    glVertex2f(box.x-lineWidth, box.y-lineWidth);
	    glVertex2f(box.x, 0);
	    glVertex2f(box.x, box.y);
	    glEnd();
	    
	    // bottom strip
	    glBegin(GL_QUAD_STRIP);
	    glVertex2f(lineWidth, box.y-lineWidth);
	    glVertex2f(0, box.y);
	    glVertex2f(box.x-lineWidth, box.y-lineWidth);
	    glVertex2f(box.x, box.y);
	    glEnd();
	    
	    glPopAttrib();
	} else {
	    std::cout << "Error: NonFilledRectangle with non-glperformable.\n";
	    t.dump(std::cout);
	}
	glPopMatrix();
	GLERR << "After NonFilledRectangle.\n";
    }
};
VOB_DEFINED(NonFilledRectangle);


/** Call the given display list.
 */
struct CallList {
    enum { NTrans = 0 };

    DisplayListID no;

    template<class F> void params(F &f) {
	f(no);
    }

    void render() const {
	GLERR;
	glCallList(no.get());
	GLERR << "After calling list "<<no.get()<<"\n";
    }
};

VOB_DEFINED(CallList);

/** Call the given display list, with the GL transformation
 * matrix set from the given transformation.
 * This Vob cannot be used in non-glperformable transformations,
 * so for example any nonlinear transformations cause an error.
 * This is because we cannot make OpenGL map the coordinates properly,
 * especially if the nonlinearity would require dicing.
 * <p>
 * There are solutions for this - see, e.g. liblines and paperquad.
 */
struct CallListCoorded : public CallList {
    enum { NTrans = 1 };
    template<class T> void render(const T &t) const {
	GLERR;
	glPushMatrix();
	if(t.performGL()) {
	    glCallList(no.get());
	} else {
	    std::cout << "Error: CallistCoorded with non-glperformable.\n";
	    t.dump(std::cout);
	}
	glPopMatrix();
	GLERR << "After coorded calling list "<<no.get()<<"\n";
    }
};
VOB_DEFINED(CallListCoorded);

/** Call the given display list, with the GL transformation
 * matrix set from the given transformation's box.
 * This has the same effect as using CallListCoorded with
 * the coordinate system unitSq() on top of the coordinate
 * system given to this vob..
 * <p>
 * This Vob cannot be used in non-glperformable transformations,
 * so for example any nonlinear transformations cause an error.
 * This is because we cannot make OpenGL map the coordinates properly,
 * especially if the nonlinearity would require dicing.
 * <p>
 * There are solutions for this - see, e.g. liblines and paperquad.
 *
 */
struct CallListBoxCoorded : public CallList {
    enum { NTrans = 1 };
    template<class T> void render(const T &t) const {
	GLERR;
	glPushMatrix();
	if(t.performGL()) {           
	    Pt boxwh = t.getSqSize();
            glScalef(boxwh.x, boxwh.y, 1.0);

	    glCallList(no.get());
	} else {
	    std::cout << "Error: CallisBoxtCoorded with non-glperformable.\n";
	    t.dump(std::cout);
	}
	glPopMatrix();
	GLERR << "After boxcoorded calling list "<<no.get()<<"\n";
    }
};
VOB_DEFINED(CallListBoxCoorded);

/** For debugging differences between GL and real transformations,
 * and for just drawing a diced quad (inefficiently!).
 * The coordinates in the innermost coordinate system are the box
 * coordinates, as are the texture coordinates.
 */
struct Quad {
    enum { NTrans = 1 };
    int flags;
    int dicex, dicey;
    template<class F> void params(F &f) {
	f(dicex, dicey, flags);
    }
    template<class T> void render(const T &t) const {
	GLERR;
	Pt boxwh = t.getSqSize();
	if(flags & 1) {
	    glPushMatrix();
	    t.performGL();
	    
	    for(int i=0; i<dicex; i++) {
		float x0 = boxwh.x * i / (dicex+0.);
		float x1 = boxwh.x * (i+1) / (dicex+0.);
		glBegin(GL_QUAD_STRIP);
		for(int j=0; j<(dicey+1); j++) {
		    float y = boxwh.y * j / (dicey+0.);
		    glVertex(ZPt(x0,y,0));
		    glVertex(ZPt(x1,y,0));
		}
		glEnd();
	    }
	    glPopMatrix();
	} else {
	    for(int i=0; i<dicex; i++) {
		float x0 = boxwh.x * i / (dicex+0.);
		float x1 = boxwh.x * (i+1) / (dicex+0.);
		glBegin(GL_QUAD_STRIP);
		for(int j=0; j<(dicey+1); j++) {
		    float y = boxwh.y * j / (dicey+0.);
		    glVertex(t.transform(ZPt(x0,y,0)));
		    glVertex(t.transform(ZPt(x1,y,0)));
		}
		glEnd();
	    }
	}
	GLERR;
    }
};
VOB_DEFINED(Quad);


/** Timing transformations.
 */
struct TransTest {
    enum { NTrans = 1 };

    int npoints;
    int flags; // 1 == call transform(), 2 == call glVertex after transform

    ZPt *points;

    TransTest() : points(0) {
    }
    ~TransTest() {
	if(points) delete[] points;
    }

    template<class F> void params(F &f) {
	npoints = 0;
	if(points) delete[] points;
	f(npoints, flags);
	points = new ZPt[npoints];
	for(int i=0; i<npoints; i++) 
	    points[i] = ZPt(0,0,0);
    }

    template<class T> void render(const T &t) const {
	ZPt *p = points;
	if(flags & 1) {
	    for(int i=0; i<npoints; i++)
		p[i] = t.transform(p[i]);
	    if(flags & 2) {
		for(int i=0; i<npoints; i++)
		    VecGL::glVertex(p[i]);
	    }
	}
	else {
	    glBegin(GL_TRIANGLE_FAN);
	    for(int i=0; i<npoints; i++)
		t.vertex(p[i]);
	    glEnd();
	}
    }

};


VOB_DEFINED(TransTest);


/** A Vob to draw a selection with 3 'selection modes'. 
 * 2nd coordinate system is used to select the current mode.
 * The mode is represented by a vob. These 3 possibile 'selection mode'
 * vobs are set in parameters. 
 * <p>
 * Modes(2nd cs square size width): 
 *    <=1 normal, 
 *    <= 2 pre selection and 
 *    other is post selection.
 */
struct SelectVob {
    enum { NTrans = 2 };

    Vob1 * normal,
         * preSelect,
         * postSelect;

    template<class F> void params(F &f) {
      f(normal, preSelect, postSelect);
    }

    template<class T> void render(const T &t0, const T &t1) const {
        Pt box = t1.getSqSize();
	//std::cout << "Size of box: " << box.x << "\n";
	if (box.x <= 1)      normal->render1(t0);
	else if (box.x <= 2) preSelect->render1(t0);
	else                 postSelect->render1(t0);
    }
  
};
VOB_DEFINED(SelectVob);

}}

#endif
