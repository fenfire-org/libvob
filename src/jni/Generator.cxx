/*
Generator.cxx
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


#include "Generator.hxx"

static Generator gen(S(C_OUTPUT_FILE), S(HEADER_FILE));

#define VOB_JNISTREAM ::gen.JNISTREAM
#define VOB_JNICXXSTREAM ::gen.JNICXXSTREAM
#define VOB_JAVASTREAM ::gen.JAVASTREAM
#define VOB_TRANSJAVASTREAM ::gen.TRANSJAVASTREAM

#include <vob/jni/Generate.hxx>
#include HEADER_FILE

