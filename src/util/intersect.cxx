/*
intersect.cxx
 *    
 *    Copyright (c) 2003, Asko Soukka
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
 *    
 */
/*
 * Written by Asko Soukka
 */

/* Trivial intersection heuristics for culling coordinate transformation. */

#include <vob/Transform.hxx>
#include <vob/Vec23.hxx>
#include <vob/Debug.hxx>
#include "vob/intersect.hxx"

using Vob::Transform;
using Vob::Vec23::ZPt;
using Vob::Vec23::Pt;

namespace Vob {
namespace Util {
DBGVAR(dbg_intersect, "Intersect");
  bool parallelRectIntersect(ZPt &p1, ZPt &p2, ZPt &p3, ZPt &p4) {
    return (p2.x > p3.x) && (p4.x > p1.x) && (p2.y > p3.y) && (p4.y > p1.y);
  }
  bool parallelRectWithin(ZPt &p1, ZPt &p2, ZPt &p3, ZPt &p4) {
    return (p1.x <= p3.x) && (p1.y <= p3.y) && (p4.x <= p2.x) && (p4.y <= p2.y);
  }
  void findBoundingBox(const Transform *t, ZPt &p1, ZPt &p2) {
    int i0, j0;
    Pt box = t->getSqSize();
    float x1=0, y1=0, x2=0, y2=0;

    DBG(dbg_intersect) << "Find bb "<<t<<" "<<box<<"\n";
    for (i0=0; i0<=1; i0+=1) {
      for (j0=0; j0<=1; j0+=1) {
	float i = i0 * box.x;
	float j = j0 * box.y;
	if (i==0 && j==0) {
	  /** Initializing. */
	  ZPt tmpPt = t->transform(ZPt(i, j, 0));
	  x1 = tmpPt.x; y1 = tmpPt.y;
	  x2 = tmpPt.x; y2 = tmpPt.y;
	} else {
	  /** Comparing. */
	  ZPt tmpPt = t->transform(ZPt(i, j, 0));
	  if (tmpPt.x < x1) x1 = tmpPt.x;
	  else if (tmpPt.x > x2) x2 = tmpPt.x;
	  if (tmpPt.y < y1) y1 = tmpPt.y;
	  else if (tmpPt.y > y2) y2 = tmpPt.y;
	}
      }
    }
    p1 = ZPt(x1, y1, 0);
    p2 = ZPt(x2, y2, 0);
    DBG(dbg_intersect) << "Return "<<p1<<" "<<p2<<"\n";
  }

  void findDistortedBoundingBox(const Transform *t, ZPt &p1, ZPt &p2) {
    double i, step_x, step_y;
    Pt box = t->getSqSize();
    float x1=0, y1=0, x2=0, y2=0;
    int check_dist;
    DBG(dbg_intersect) << "Find distortedbb "<<t<<" "<<box<<"\n";
    
    if (box.x>0 && box.y>0) { // box area must be positive
      /** Initializing. */
      check_dist = 100; // XXX Adjusts the scanning frequency.
	
      ZPt o = t->transform(ZPt(0, 0, 0));
      ZPt u = t->transform(ZPt(box.x, box.y, 0));
    
      if (fabs(o.x - u.x) / check_dist > box.x)
        step_x = box.x/(fabs(o.x - u.x) / check_dist);
      else step_x = box.x;
      if (fabs(o.y - u.y) / check_dist > box.y)
        step_y = box.y/(fabs(o.y - u.y) / check_dist);
      else step_y = box.y;
    
      x1 = u.x; y1 = u.y;
      x2 = u.x; y2 = u.y;
    
      /** Sweeps the box's vertices. */
      /** Vertice (0,0) -> (w,0). */
      for (i=0; i < box.x; i+=step_x) {
        ZPt tmpPt = t->transform(ZPt(i, 0, 0));
        if (tmpPt.x < x1) x1 = tmpPt.x;
        else if (tmpPt.x > x2) x2 = tmpPt.x;
        if (tmpPt.y < y1) y1 = tmpPt.y;
        else if (tmpPt.y > y2) y2 = tmpPt.y;
      }
    
      /** Vertice (0,0) -> (0,h). */
      for (i=step_y; i < box.y; i+=step_y) {
        ZPt tmpPt = t->transform(ZPt(0, i, 0));
        if (tmpPt.x < x1) x1 = tmpPt.x;
        else if (tmpPt.x > x2) x2 = tmpPt.x;
        if (tmpPt.y < y1) y1 = tmpPt.y;
        else if (tmpPt.y > y2) y2 = tmpPt.y;
      }
    
      /** Vertice (0,h) -> (w,h). */
      for (i=0; i < box.x; i+=step_x) {
        ZPt tmpPt = t->transform(ZPt(i, box.y, 0));
        if (tmpPt.x < x1) x1 = tmpPt.x;
        else if (tmpPt.x > x2) x2 = tmpPt.x;
        if (tmpPt.y < y1) y1 = tmpPt.y;
        else if (tmpPt.y > y2) y2 = tmpPt.y;
      }
    
      /** Vertice (w,0) -> (w,h). */
      for (i=0; i < box.y; i+=step_y) {
        ZPt tmpPt = t->transform(ZPt(box.x, i, 0));
        if (tmpPt.x < x1) x1 = tmpPt.x;
        else if (tmpPt.x > x2) x2 = tmpPt.x;
        if (tmpPt.y < y1) y1 = tmpPt.y;
        else if (tmpPt.y > y2) y2 = tmpPt.y;
      }
    }
    p1 = ZPt(x1, y1, 0);
    p2 = ZPt(x2, y2, 0);
    DBG(dbg_intersect) << "Return "<<p1<<" "<<p2<<"\n";
  }
}
}
