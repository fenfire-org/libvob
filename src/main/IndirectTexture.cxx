/*
IndirectTexture.cxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
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
 * Written by Tuomas J. Lukka
 */

#include <vob/IndirectTexture.hxx>
#include <iostream>
#include <vob/Debug.hxx>

namespace Vob {

    DBGVAR(dbg_indirectbind, "IndirectBind");

    IndirectTextureBind::IndirectTextureBind() :
		activeTexture(0),
		textureTarget(0),
		indirectTexture(0) { }
    IndirectTextureBind::IndirectTextureBind(
		GLenum activeTexture, 
		GLenum textureTarget,
		IndirectTexture *indirectTexture) :
		    activeTexture(activeTexture),
		    textureTarget(textureTarget),
		    indirectTexture(indirectTexture) { }

    void IndirectTextureBind::bind() {
	DBG(dbg_indirectbind) << "bind "<<activeTexture<<" "<<textureTarget
		    <<" "<<indirectTexture<<" "<<indirectTexture->texId<<"\n";
	if(indirectTexture == 0) return;
	glActiveTextureARB(activeTexture);
	glBindTexture(textureTarget, indirectTexture->texId);
	glActiveTextureARB(GL_TEXTURE0_ARB);
    }
    void IndirectTextureBind::unbind() {
	DBG(dbg_indirectbind) << "unbind "<<activeTexture<<" "<<textureTarget
		    <<" "<<indirectTexture<<" "<<indirectTexture->texId<<"\n";
	if(indirectTexture == 0) return;
	glActiveTextureARB(activeTexture);
	glBindTexture(textureTarget, 0);
	glActiveTextureARB(GL_TEXTURE0_ARB);
    }

}
