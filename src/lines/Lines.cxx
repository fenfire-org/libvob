/*
Lines.cxx
 *    
 *    Copyright (c) 2003, Matti Katila
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
 * Written by Matti J. Katila
 */


#include <vob/lines/Lines.hxx>
#include <vob/glerr.hxx>
#include <cmath>

static bool dbg = false;

namespace Lines {
//    DBGVAR(dbg_lines, "Lines.general"); // XXX not defined in this scope... Makefiles?

    SimpleLine::SimpleLine(GLuint textId, float lineWidth, vector<ZPt> points): 
      textureId(textId), linewidth(lineWidth)
    { 
        if (textId <= 0) {
	  cout << "Error in SimpleLine - TextId under or zero!"<< textId <<"\n";
	  return;
	}
	if (points.size() != 2) {
	  cout << "Errorr in SimpleLine - not enough points!"<< points.size() <<"\n";
	  return;
	}
	this->points = points;
    }



    /* How texture clipping is count?
     * ------------------------------
     *   line... 
     *   +-----+---+-------+---+-----+
     *   |     |whi|       |whi|     |
     *   |clamp|te | black |te | clamp
     *   |     |   |       |   |     |
     *   +-----+---+-------+---+-----+
     *   0     t0  4/8    5/8  t1    1
     *          
     *         |< >|       <A6>< >|
     *           2 pixels   2 pixels
     *
     *             |<width>|
     *               
     *-----------------------------------
     *
     *     t0 = 1 - t1
     *
     *
     *  5/8 - 4/8      t1 - 10
     *  ---------  =  ---------
     *      w         2 + w + 2
     *
     *
     *   t0 = 0.5 - (4+w)/16w
     */
    void SimpleLine::draw() {
        ZPt a = points[0];
	ZPt b = points[1];

        if (dbg) cout << "lineWidth: " <<linewidth
		      <<" x: "<< a.x << ", "<< b.x 
                      <<" y: "<< a.y << ", "<< b.y <<"\n";
	
        glPushAttrib(GL_ENABLE_BIT);
          glDisable(GL_TEXTURE_1D);
          glEnable(GL_TEXTURE_2D);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        // lp2 > 1
        float total_width = 2 + linewidth/2;
        
        ZPt l = (a-b).cw90().normalized();
        l.x *= total_width; 
        l.y *= total_width;

        float t0 = 0.5 - ( (4+linewidth) / (16*linewidth) );
        float t1 = 1 - t0;

        if (dbg) cout <<"t1: " << t1 <<" t0 :" << t0 << endl;

        glBegin(GL_QUADS);
          
        glTexCoord2f(t0, 1);
        glVertex3f(a.x + l.x, a.y + l.y, a.z);

        glTexCoord2f(t1, 1);
        glVertex3f(a.x - l.x, a.y - l.y, a.z);

        glTexCoord2f(t1, 0);
        glVertex3f(b.x - l.x, b.y - l.y, b.z);

        glTexCoord2f(t0, 0);
        glVertex3f(b.x + l.x, b.y + l.y, b.z);

        glEnd();
        glBindTexture(GL_TEXTURE_2D, 0);

        glPopAttrib();
    }


    /* -------------------------------------------------------
     *
     * Continuous Line
     * ===============  
     */
    ContinuousLine::ContinuousLine(GLuint textId, float lineWidth,
				   vector<ZPt> points, int joinStyle,  bool chain):
      chain(chain) 
    {
        this->lineWidth = fabs(lineWidth);

        if (dbg) cout << "ContinuousLine constructor\n";

        if (textId <= 0) {
            cerr << "Lines error:: Texture Id <= 0 ! \n";
            return;
        }

        if (joinStyle == BEVEL ||
            joinStyle == MITER ||
            joinStyle == ROUND)
            this->textureId = textId;
        else {
            cerr << "Lines error:: Not a good joinStyle: " 
                 << joinStyle <<"\n";
            return;
        }

	// XXX dumb version!
	for(unsigned int i=0; i+1<points.size(); i++) {
	  vector<ZPt> tmp_pts;
	  tmp_pts.push_back(points[i]);
	  tmp_pts.push_back(points[i+1]);
	  SimpleLine l(textId, lineWidth, tmp_pts);
	  l.draw();
	}
	if (chain) {
	  vector<ZPt> tmp_pts;
	  tmp_pts.push_back(points[0]);
	  tmp_pts.push_back(points[points.size()-1]);
	  SimpleLine l(textId, lineWidth, tmp_pts);
	  l.draw();
	}
	draw();
    }

    void ContinuousLine::draw() {
      if (dbg) cout << "Now, if implemented - draw continuousline.\n";
    }




} // end of Lines namespace

