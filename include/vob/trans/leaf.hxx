/*
leaf.hxx
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

#ifndef VOB_NON_LEAF 
#define VOB_NON_LEAF 0
#endif

#if VOB_NON_LEAF
#include F
#else

#undef VOB_NON_LEAF
#define VOB_NON_LEAF 1

#ifdef VOB_PRIMITIVETRANS_DEFINED_NONLEAF
#undef VOB_PRIMITIVETRANS_DEFINED
#define VOB_PRIMITIVETRANS_DEFINED(a,b) VOB_PRIMITIVETRANS_DEFINED_NONLEAF(a,b)
#endif
#include F

#ifdef VOB_PRIMITIVETRANS_DEFINED_NONLEAF
#undef VOB_PRIMITIVETRANS_DEFINED
#define VOB_PRIMITIVETRANS_DEFINED(a,b) VOB_PRIMITIVETRANS_DEFINED_LEAF(a,b)
#endif

#undef VOB_NON_LEAF
#define VOB_NON_LEAF 0

#endif
