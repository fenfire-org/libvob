/*
Paper.cxx
 *    
 *    Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
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
 * Written by Janne Kujala and Tuomas J. Lukka
 */

#include "vob/paper/Paper.hxx"


/** The matrix that maps v[TEX0] into o[TEXi] is stored 
 * starting at c[tex_addr_base + 4*i].
 */
#define tex_addr_base 60


namespace Vob {
namespace Paper {


    TexGen::TexGen(const float *tex_mat) {
	this->tex_mat[0] = tex_mat[0];
	this->tex_mat[1] = tex_mat[1];
	this->tex_mat[2] = tex_mat[2];
	this->tex_mat[3] = tex_mat[3];

	this->tex_mat[4] = tex_mat[4];
	this->tex_mat[5] = tex_mat[5];
	this->tex_mat[6] = tex_mat[6];
	this->tex_mat[7] = tex_mat[7];

	this->tex_mat[8] = tex_mat[8];
	this->tex_mat[9] = tex_mat[9];
	this->tex_mat[10] = tex_mat[10];
	this->tex_mat[11] = tex_mat[11];

	this->tex_mat[12] = 0;
	this->tex_mat[13] = 0;
	this->tex_mat[14] = 0;
	this->tex_mat[15] = 1;
    }

    void TexGen::setUp_VP(int unit, LightParam * param) {
      // XXX: This could also be implemented as CallGL code
#ifdef GL_VERTEX_PROGRAM_NV
      GLuint base = unit * 4 + tex_addr_base;
      glProgramParameter4fvNV(GL_VERTEX_PROGRAM_NV, base + 0, tex_mat);
      glProgramParameter4fvNV(GL_VERTEX_PROGRAM_NV, base + 1, tex_mat + 4);
      glProgramParameter4fvNV(GL_VERTEX_PROGRAM_NV, base + 2, tex_mat + 8);
      glProgramParameter4fvNV(GL_VERTEX_PROGRAM_NV, base + 3, tex_mat + 12);
#endif
    }

    string TexGen::getVPCode(int unit) {
#if 0
      // XXX: this code  crashes when compiled with g++-3.1 -O3 -ffast-math
      std::ostringstream code;

      GLuint base = unit * 4 + tex_addr_base;
      code << "DP4 o[TEX" << unit << "].x, c[" << base + 0 << "], v[TEX0];\n" 
	   << "DP4 o[TEX" << unit << "].y, c[" << base + 1 << "], v[TEX0];\n" 
	   << "DP4 o[TEX" << unit << "].z, c[" << base + 2 << "], v[TEX0];\n"
	   << "DP4 o[TEX" << unit << "].w, c[" << base + 3 << "], v[TEX0];\n";

      return code.str();
#else
      GLuint base = unit * 4 + tex_addr_base;
      char str[1000];
      sprintf(str,
	      "DP4 o[TEX%d].x, c[%u], v[TEX0];\n" 
	      "DP4 o[TEX%d].y, c[%u], v[TEX0];\n" 
	      "DP4 o[TEX%d].z, c[%u], v[TEX0];\n"
	      "DP4 o[TEX%d].w, c[%u], v[TEX0];\n",
	      unit, base, unit, base + 1, unit, base + 2, unit, base + 3);
      return str;
#endif
    }

    void TexGen::setUp_explicit(LightParam * param) {
	explicit_mat = tex_mat;
    }

      void TexGen::setUp_texgen(LightParam *param) {
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	
	float mat[16] = { param->e0.x, param->e0.y, param->e0.z, 0,
			  param->e1.x, param->e1.y, param->e1.z, 0,
			  param->e2.x, param->e2.y, param->e2.z, 0,
			  param->orig.x, param->orig.y, param->orig.z, 1 };
	glMultMatrixf(mat);

	glEnable(GL_TEXTURE_2D);
	glTexGenf(GL_S, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	glTexGenfv(GL_S, GL_EYE_PLANE, tex_mat);
	glTexGenf(GL_T, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	glTexGenfv(GL_T, GL_EYE_PLANE, tex_mat + 4);
	glTexGenf(GL_R, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	glTexGenfv(GL_R, GL_EYE_PLANE, tex_mat + 8);
	glEnable(GL_TEXTURE_GEN_S);
	glEnable(GL_TEXTURE_GEN_T);
	glEnable(GL_TEXTURE_GEN_R);
	glPopMatrix();
      }

      void TexGenEmboss::setUp_VP(int unit, LightParam *param) {
	/** Suppose 
         *   x = vertex position
	 *   p = paper coordinates
	 *   s = texture coordinates
	 *   A = paper-to-vertex mapping
	 *   E = embossing mapping (translates x towards light)
	 *   M = modelview matrix
	 *   T = texture matrix
	 *
	 * The usual eye-linear embossing texgen computes
	 *   s' = Tp' = T((MEA)^-1 M x) = T(A^-1 E^-1 x)
	 * The usual simple texgen computes
	 *   s = Tp = T((MA)^-1 M x) = T(A^-1 x)  ==>  x = Ap
	 * Thus, the mapping from p to s' is
	 *   s' = Tp' = T(A^-1 E^-1 x) = T A^-1 E^-1 A p
	 * 
	 * The following code computes T A^-1 E^-1 A and stores it
	 * in place of T in the constant registers.
	 */

#ifdef GL_VERTEX_PROGRAM_NV
        GLuint base = unit * 4 + tex_addr_base;

        glMatrixMode(GL_MATRIX0_NV);
        glLoadIdentity();

	float eps = this->eps * param->e2.dot(param->e2) 
	  / (param->Light - param->Light_w * param->orig).dot(param->e2);
	
	glTranslatef(eps * param->Light.x, eps * param->Light.y, eps * param->Light.z);
	float s = 1 - param->Light_w * eps;
	glScalef(s, s, s);
	
	float mat[16] = { param->e0.x, param->e0.y, param->e0.z, 0,
			  param->e1.x, param->e1.y, param->e1.z, 0,
			  param->e2.x, param->e2.y, param->e2.z, 0,
			  param->orig.x, param->orig.y, param->orig.z, 1 };

	glMultMatrixf(mat);

	// Hack for easily inverting the matrix
	glTrackMatrixNV(GL_VERTEX_PROGRAM_NV, base, GL_MATRIX0_NV, GL_INVERSE_NV);
	glTrackMatrixNV(GL_VERTEX_PROGRAM_NV, base, GL_NONE, GL_INVERSE_NV);
	float foo[16];
	glGetProgramParameterfvNV(GL_VERTEX_PROGRAM_NV, base + 0, GL_PROGRAM_PARAMETER_NV, foo);
	glGetProgramParameterfvNV(GL_VERTEX_PROGRAM_NV, base + 1, GL_PROGRAM_PARAMETER_NV, foo + 4);
	glGetProgramParameterfvNV(GL_VERTEX_PROGRAM_NV, base + 2, GL_PROGRAM_PARAMETER_NV, foo + 8);
	glGetProgramParameterfvNV(GL_VERTEX_PROGRAM_NV, base + 3, GL_PROGRAM_PARAMETER_NV, foo + 12);

	glLoadMatrixf(tex_mat);

	glMultMatrixf(foo);

	glMultMatrixf(mat);


	// Load the current matrix in c[base:base+4]
	glTrackMatrixNV(GL_VERTEX_PROGRAM_NV, base, GL_MATRIX0_NV, GL_IDENTITY_NV);
	glTrackMatrixNV(GL_VERTEX_PROGRAM_NV, base, GL_NONE, GL_IDENTITY_NV);
	
	glMatrixMode(GL_MODELVIEW);
#endif
      }
  
      void TexGenEmboss::setUp_texgen(LightParam *param) {
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();

	float eps = this->eps * param->e2.dot(param->e2) 
	  / (param->Light - param->Light_w * param->orig).dot(param->e2);
	
	glTranslatef(eps * param->Light.x, eps * param->Light.y, eps * param->Light.z);
	float s = 1 - param->Light_w * eps;
	glScalef(s, s, s);
	
	float mat[16] = { param->e0.x, param->e0.y, param->e0.z, 0,
			  param->e1.x, param->e1.y, param->e1.z, 0,
			  param->e2.x, param->e2.y, param->e2.z, 0,
			  param->orig.x, param->orig.y, param->orig.z, 1 };
	glMultMatrixf(mat);

	glEnable(GL_TEXTURE_2D);
	glTexGenf(GL_S, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	glTexGenfv(GL_S, GL_EYE_PLANE, tex_mat);
	glTexGenf(GL_T, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	glTexGenfv(GL_T, GL_EYE_PLANE, tex_mat + 4);
	glEnable(GL_TEXTURE_GEN_S);
	glEnable(GL_TEXTURE_GEN_T);
	glPopMatrix();
      }

      void TexGenEmboss::setUp_explicit(LightParam * param) {
	/** Does some as TexGenEmboss::setUp_VP, but without
	 * using vertex programs. In other words, calculates
	 * translation matrix for translating paper position
	 * into embossed texture coordinates.
	 *
	 * Proper translation matrix is T A^-1 E^-1 A (see
	 * TexGenEmboss::setUp_VP for details) and it's stored
	 * into float TexGenEmboss::explicit_tmp_mat[16].
	 */

	/** Implementing under progress O:-)

	// Matrix E
 	float eps = this->eps * param->e2.dot(param->e2) 
 	  / (param->Light - param->Light_w * param->orig).dot(param->e2);

 	float transl_mat[16] = { 1, 0, 0, eps * param->Light.x,
			         0, 1, 0, eps * param->Light.y,
				 0, 0, 1, eps * param->Light.z,
				 0, 0, 0, 1};

 	float s = 1 - param->Light_w * eps;

 	float scale_mat[16] = { s, 0, 0, 0,
				0, s, 0, 0, 
				0, 0, s, 0,
				0, 0, 0, 1};

	// Matrix A
 	float p2v_mat[16] = { param->e0.x, param->e0.y, param->e0.z, 0,
			  param->e1.x, param->e1.y, param->e1.z, 0,
			  param->e2.x, param->e2.y, param->e2.z, 0,
			  param->orig.x, param->orig.y, param->orig.z, 1 };

	*/

	/** We don't have Matrix invertion or multiplications routines yet...
	
	// Matrix E
	float emb_map_mat[16] = matrix_multiplication(transl_mat, scale_mat);
	// Matrix E^-1
	invert_translation_matrix(emb_map_mat);
	
	// Matrix E^-1 A
	float tmp_mat[16] = matrix_multiplication(emb_map_mat, p2v_mat);
	// Matrix A^-1
	invert_translation_matrix(p2v_mat);
	// Matrix T A^-1
	float tmp_mat2[16] = matrix_multiplication(tex_mat, p2v_mat);
	// Matrix T A^-1 E^-1 A
	explicit_tmp_map = matrix_multiplication(tmp_mat2, tmp_mat);
	// explicit_mat shoulde be made to point explicit_tmp_map
	*/
	
	std::cerr << "Warning: TexGenEmboss::setUp_explicit() not yet implemented!\n";
	explicit_mat = tex_mat;
      }

      TexGenLightmap::TexGenLightmap(const float *tex_mat, 
		     const float *inv_mat) 
	: TexGen(tex_mat) {
	this->inv_mat[0] = inv_mat[0];
	this->inv_mat[1] = inv_mat[1];
	this->inv_mat[2] = inv_mat[4];
	this->inv_mat[3] = inv_mat[5];
      }

      void TexGenLightmap::setUp(LightParam *param) {
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();

	glTranslatef(param->Light.x, param->Light.y, param->Light.z);

	float s = (param->Light - param->orig).dot(param->e2) 
		   / param->e2.dot(param->e2);

	float det = inv_mat[0] * inv_mat[3] - inv_mat[1] * inv_mat[2];
	
	glScalef(s/det, s/det, s);
	
	float mat[16] = { 
	  inv_mat[3] * param->e0.x - inv_mat[1] * param->e0.y, 
	  -inv_mat[2] * param->e0.x + inv_mat[0] * param->e0.y, param->e0.z, 0,
	  inv_mat[3] * param->e1.x + inv_mat[1] * param->e1.y, 
	  -inv_mat[2] * param->e1.x + inv_mat[0] * param->e1.y, param->e1.z, 0,
	  param->e2.x, param->e2.y, param->e2.z, 0,
	  0, 0, 0, 1 };

	glMultMatrixf(mat);

	glEnable(GL_TEXTURE_2D);
	glTexGenf(GL_S, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	glTexGenfv(GL_S, GL_EYE_PLANE, tex_mat);
	glTexGenf(GL_T, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	glTexGenfv(GL_T, GL_EYE_PLANE, tex_mat + 4);
	glEnable(GL_TEXTURE_GEN_S);
	glEnable(GL_TEXTURE_GEN_T);
	glPopMatrix();
      }
            
      LightDirSetup::LightDirSetup(const float *tex_mat) {
	mat[0] = tex_mat[0];
	mat[1] = tex_mat[1];
	mat[2] = tex_mat[4];
	mat[3] = tex_mat[5];
      }

      void LightDirSetup::setUp(LightParam *param) {
	//ZVec Li = inverse_map(e0, e1, e2, Light).normalize();
	ZVec Li = ZVec(param->e0.dot(param->Light), param->e1.dot(param->Light), param->e2.dot(param->Light)).normalized();

	ZVec L(mat[0] * Li.x + mat[1] * Li.y,
	       mat[2] * Li.x + mat[3] * Li.y, Li.z);
	glColor3f(.5 * L.x + .5, .5 * L.y + .5, .5 * L.z + .5);
      }

  void PaperPass::independentSetup() {
      setupcode();
      for(unsigned i=0; i<indirectTextureBinds.size(); i++) {
	  indirectTextureBinds[i]->bind();
      }
  }
  void PaperPass::independentTeardown() {
      for(unsigned i=0; i<indirectTextureBinds.size(); i++) {
	  indirectTextureBinds[i]->unbind();
      }
      teardowncode();
  }
  
  void PaperPass::setUp_texgen(LightParam *param) {
    independentSetup();
    
    /* Set up TexGen for each texture unit */
    GLenum unit = GL_TEXTURE0_ARB;
    for (vector<shared_ptr<TexGen> >::iterator it = texgen.begin(); it != texgen.end(); ++it) {
      glActiveTextureARB(unit++);
      if (it->get()) (*it)->setUp_texgen(param);
      else std::cerr << "Warning: ignoring null TexGen\n";
    }
	
    /* Do general parametric setup */
    for (vector<shared_ptr<LightSetup> >::iterator it = setup.begin(); it != setup.end(); ++it) {
      if (it->get()) (*it)->setUp(param);
      else std::cerr << "Warning: ignoring null LightSetup\n";
    }
  }
  
  void PaperPass::loadVP() {
    string code = 
      "!!VP1.1 OPTION NV_position_invariant;\n"
      "MOV o[COL0], v[COL0];\n"
      "MOV o[COL1], v[COL1];\n";
    
    int unit = 0;
    for (vector<shared_ptr<TexGen> >::iterator it = texgen.begin(); it != texgen.end(); ++it) {
      if (it->get()) code += (*it)->getVPCode(unit);
      else std::cerr << "Warning: ignoring null TexGen\n";
      unit++;
    }
    
    code += "END\n";
    
    //std::cerr << "Creating VPCode with the source " << code << "\n";
    
    texgenvp = ARBVertexProgram(code.c_str());
  }
  
  void PaperPass::setUp_VP(LightParam *param) {
    if (texgenvp.getSource().length() == 0) loadVP();
    
    independentSetup();
    
    /* Set up VP TexGen parameters for each texture unit */
    int unit = 0;
    for (vector<shared_ptr<TexGen> >::iterator it = texgen.begin(); it != texgen.end(); ++it) {
      if (it->get()) (*it)->setUp_VP(unit, param);
      else std::cerr << "Warning: ignoring null TexGen\n";
      unit++;
    }
    
    /* Do general parametric setup */
    for (vector<shared_ptr<LightSetup> >::iterator it = setup.begin(); it != setup.end(); ++it) {
      if (it->get()) (*it)->setUp(param);
      else std::cerr << "Warning: ignoring null LightSetup\n";
    }
    
    texgenvp.bind(); // Bind vertex program
#ifdef GL_VERTEX_PROGRAM_NV
    glEnable(GL_VERTEX_PROGRAM_NV);
#endif
  }
  
  void PaperPass::tearDown_VP() {
    independentTeardown();
#ifdef GL_VERTEX_PROGRAM_NV
    glDisable(GL_VERTEX_PROGRAM_NV);
#endif
  }

  void PaperPass::setUp_explicit(LightParam *param) {
    independentSetup();
    
    /* Set up explicit TexGen parameters for each texture unit */
    for (vector<shared_ptr<TexGen> >::iterator it = texgen.begin(); it != texgen.end(); ++it) {
      if (it->get()) (*it)->setUp_explicit(param);
      else std::cerr << "Warning: ignoring null TexGen\n";
    }

    /* Do general parametric setup */
    for (vector<shared_ptr<LightSetup> >::iterator it = setup.begin(); it != setup.end(); ++it) {
      if (it->get()) (*it)->setUp(param);
      else std::cerr << "Warning: ignoring null LightSetup\n";
    }
  }

  void PaperPass::texcoords_explicit(float *ppos) {
    const float *mat;
    float texCoords[4];

    GLenum unit = GL_TEXTURE0_ARB;

    /** Transforms paper position into proper texture coordinates.
     * In other words, multiplicates explicit_mat transformation matrix 
     * by ppos col vector.
     */
    for (vector<shared_ptr<TexGen> >::iterator it = texgen.begin(); it != texgen.end(); ++it) {
      if (it->get()) {
	mat = (*it)->explicit_mat;
	texCoords[0] = mat[0] * ppos[0] + mat[1] * ppos[1] + mat[2] * ppos[2] + mat[3] * ppos[3];
	texCoords[1] = mat[4] * ppos[0] + mat[5] * ppos[1] + mat[6] * ppos[2] + mat[7] * ppos[3];
	texCoords[2] = mat[8] * ppos[0] + mat[9] * ppos[1] + mat[10] * ppos[2] + mat[11] * ppos[3];
	texCoords[3] = mat[12] * ppos[0] + mat[13] * ppos[1] + mat[14] * ppos[2] + mat[15] * ppos[3];

	glMultiTexCoord4fvARB(unit, texCoords);
      }
      else std::cerr << "Warning: ignoring null TexGen\n";
      unit++;
    }
  }



}
}







