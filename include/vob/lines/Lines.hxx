/*
Lines.hxx
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
 * Written by Matti Katila
 */

#ifndef GZZ_LINES_HXX
#define GZZ_LINES_HXX

#include <vob/Vec23.hxx>
#include <vob/Debug.hxx>
#include <GL/gl.h>

#include <iostream>

//using namespace Vob::Debug;
//PREDBGVAR(dbg_lines);


/** Antialised OpenGL lines.
 */
namespace Lines {
    using std::vector;
    using std::ostream;
    using std::cout;
    using std::cerr;
    using std::endl;
    using Vob::ZPt;


    /** Very simple line from point a to b.
     */
    class SimpleLine {
        GLuint textureId;
        float linewidth;
        vector<ZPt> points;
    public:
        SimpleLine(GLuint textId, float lineWidth, vector<ZPt> points);
        ~SimpleLine() { }
        void draw();
    };


    /** Bevel shape model of the bending with ContinuousLine
     */
    const int BEVEL = 100; 

    /** Miter shape model of the bending with ContinuousLine
     */
    const int MITER = 200;

    /** Round shape model of the bending with ContinuousLine
     */
    const int ROUND = 300;
    

    /** ContinuousLine is line constructed from various points. 
     *  It bends on points between the fisrt and the last point.
     */
    class ContinuousLine {
    private:
        GLuint textureId;
        float lineWidth;
        vector<ZPt> points;
        int joinStyle;
        bool chain;

    public:
        /** ContinuousLine is line constructed from various points. 
         *  It bends in given points.
         * http://www.xaraxone.com/webxealot/xealot19/page_4.htm
         *
         * @param textId texture's id - must be > 0.
         * @param joinStyle "Bevel", "Miter" or "Round"
         * @param lineWidth line's width
         * @param chain join first and last point
         * @param points ZPt points a,b,c etc. 
         *          { ax,ay,az, bx,by,bz, cx,cy,cz, etc.. }
         */
        ContinuousLine(GLuint textId, float lineWidth,
                       vector<ZPt> points, int joinStyle, bool chain);
        ~ContinuousLine() { }

        /** Draw the continuous line on the screen.
         */ 
        void draw();
    };
}

#endif
