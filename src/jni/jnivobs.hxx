/*
jnivobs.hxx
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

// This is the include file
// which determines what Vobs and 
// transformations get compiled
// into GLRen


// These are the include files that contain
// the actual vobs and transforms
//
#include <vob/vobs/Trivial.hxx>
#include <vob/vobs/Pixel.hxx>
#include <vob/vobs/Text.hxx>
#include <vob/vobs/Paper.hxx>
#include <vob/vobs/Irregu.hxx>
#include <vob/vobs/Lines.hxx>

#include <vob/trans/LinearPrimitives.hxx>
#include <vob/trans/FunctionalPrimitives.hxx>
#include <vob/trans/DisablablePrimitives.hxx>
#include <vob/trans/FisheyePrimitives.hxx>
