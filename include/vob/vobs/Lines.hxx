/*
Lines.hxx
 *    
 *    Copyright (c) 2003, Matti J. Katila
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

#ifndef VOB_VOBS_LINES_HXX
#define VOB_VOBS_LINES_HXX

#ifndef VOB_DEFINED
#define VOB_DEFINED(x)
#endif

#include <vob/lines/Lines.hxx>
#include <vob/jni/Types.hxx>
#include <GL/gl.h>

namespace Vob {
namespace Vobs {

  //PREDBGVAR(dbg_lines);

class ContinuousLine {
public:
    enum { NTrans = 1 };

    int texId;
    float width;
    int joinStyle;
    bool chain;
    vector<float> points;

    template<class F> void params(F &f) {
      f(texId, width, joinStyle, chain, points);
    }

    template<class T> void render(const T &coords1) const {
      vector<ZPt> pts;
      for (unsigned int i=0; i+2<points.size(); i+=3) {
	ZPt p = coords1.transform( ZPt(points[i], points[i+1], points[i+2]) );
	pts.push_back(p);
      }

      // XXX constructor in params and call only render...
      if (pts.size() <= 2) {
	Lines::SimpleLine l = Lines::SimpleLine(texId, width, pts);
	l.draw();
      } else {
	Lines::ContinuousLine l = Lines::ContinuousLine(texId, width, pts, joinStyle, chain);
	l.draw();
      }
    };


};


VOB_DEFINED(ContinuousLine);

}
}

#endif
