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

#ifndef VOB_INTERSECT_HXX
#define VOB_INTERSECT_HXX

#include <vob/Vec23.hxx>
#include <vob/Transform.hxx>

using Vob::Vec23::ZPt;
using Vob::Transform;

namespace Vob {
namespace Util {
  /** Checks if two parallel rectangle intersects.
   * "Two rectangles, represented by lower left and upper right points (p1, p2)
   *  and (p3, p4), intersects if and only if the conjunction
   *  (x2 >= x3) && (x4 >= x1) && (y2 >= y3) && (y4 >= y1)
   *  is true. (The rectangles must intersect in both dimensions.)"
   * @param p1 first rectangle, "lower left corner"
   * @param p2 first rectangle, "upper right corner"
   * @param p3 second rectangle, "lower left corner"
   * @param p4 second rectangle, "upper right corner"
   * @return true if the given rectangles intersect
   */
  bool parallelRectIntersect(ZPt &p1, ZPt &p2, ZPt &p3, ZPt &p4);
  
  /** Checks if two parallel rectangle are within each other.
   * @param p1 first rectangle, "lower left corner"
   * @param p2 first rectangle, "upper right corner"
   * @param p3 second rectangle, "lower left corner"
   * @param p4 second rectangle, "upper right corner"
   * @return true if the given rectangles are within each other.
   */
  bool parallelRectWithin(ZPt &p1, ZPt &p2, ZPt &p3, ZPt &p4);
  
  /** Finds the bounding box of coordsys' box after
   * transformation. Assumes linear transform and transforms only
   * corner's of the box.
   * @param t the coordinate system, whose box to use
   * @param p1 "lower left corner"
   * @param p2 "upper right corner"
   */
  void findBoundingBox(const Transform *t, ZPt &p1, ZPt &p2);
  
  /** Finds the bounding box of coordsys' box after
   * transformation. Because the transform may be distorted,
   * searches bounding box's coners also along vertices.
   * @param t the coordinate system, whose box to use
   * @param p1 "lower left corner"
   * @param p2 "upper right corner"
   */
  void findDistortedBoundingBox(const Transform *t, ZPt &p1, ZPt &p2);
}
}

#endif
