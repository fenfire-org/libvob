/*
Coorder.cxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka and Asko Soukka
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
 * Written by Tuomas J. Lukka and Asko Soukka
 */

#include <vob/Coorder.hxx>
#include <vob/VecGL.hxx>
#include <vob/Debug.hxx>



namespace Vob {
DBGVAR(dbg_coorder, "Coorder");
DBGVAR(dbg_coorder2, "CoorderInterp");


using namespace VecGL;

/** A transform that interpolates between
 * two transforms pointwise.
 */
struct PointInterpTransform : public Transform {
    const Transform &cs1, &cs2;
    float fract;
    bool didGetMat;
    float mat[16];
    Transform *inv;

    PointInterpTransform(const Transform &cs1, const Transform &cs2, 
		float fract)
	    : cs1(cs1), cs2(cs2), fract(fract), inv(0) { }
    virtual void vertex(const ZPt &p) const {
	glVertex(transform(p));
    }
    virtual ZPt transform(const ZPt &p) const {
	ZPt res = lerp(cs1.transform(p), 
		    cs2.transform(p),
		    fract);
	DBG(dbg_coorder) << "PointInterp: "<<p<<": "<<fract<<" "<<
		    cs1.transform(p) << " " << cs2.transform(p) << " " 
		    << res<<"\n";
	return res;
    }
    virtual bool isNonlinear() const {
	return cs1.isNonlinear() || cs2.isNonlinear();
    }
    virtual float nonlinearity(const ZPt &p, float radius) const { 
	return lerp(cs1.nonlinearity(p, radius), 
		cs2.nonlinearity(p, radius), fract);
    }
    virtual void dump(std::ostream &out) const {
	out << "Pointinterp\n";
    };

    virtual bool shouldBeDrawn() const {
	return cs1.shouldBeDrawn() && cs2.shouldBeDrawn();
    }
    virtual Pt getSqSize() const {
	return lerp(cs1.getSqSize(), cs2.getSqSize(), fract);
    }
    
    bool canPerformGL() const { 
	return cs1.canPerformGL() && cs2.canPerformGL(); 
    }

    void getMat() const {
	if(didGetMat) {
	    DBG(dbg_coorder2) << "did get mat";

	    /* After 4 hours of debugging mudyc found that 
	     * this was the one and only thing that just
	     * weirdly broke interpolation.
	     */
	    //return;
	}

	GLfloat mat1[16], mat2[16];
	glPushAttrib(GL_TRANSFORM_BIT);
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();

	glLoadIdentity();
	cs1.performGL();
	glGetFloatv(GL_MODELVIEW_MATRIX, mat1);

	glLoadIdentity();
	cs2.performGL();
	glGetFloatv(GL_MODELVIEW_MATRIX, mat2);

	DBG(dbg_coorder) << "PointInterp PerformGL!\n";
	if(dbg_coorder) {
	    for(int i=0; i<16; i++) DBG(dbg_coorder) << mat1[i] << " ";
	    DBG(dbg_coorder) << "\n";
	    for(int i=0; i<16; i++) DBG(dbg_coorder) << mat2[i] << " ";
	    DBG(dbg_coorder) << "\n";
	}
	for(int i=0; i<16; i++) 
	    ((float *)mat)[i] = lerp(mat1[i], mat2[i], fract);

	if(dbg_coorder2) {
	    for(int i=0; i<16; i++) DBG(dbg_coorder2) << mat[i] << " ";
	    DBG(dbg_coorder2) << "\n";
	}


	glPopMatrix();
	glPopAttrib();


    }

    
    bool performGL() const { 
	// XXX Allow using of vertex weighting or something
	if(!canPerformGL()) return false;
	getMat();
	glMultMatrixf(mat);
	return true; 
    }
    const Transform &getInverse() const {
	/*
	if(canPerformGL()) {
	    getMat();
	    return new MatrixCoordSys(invertMat(mat));
	} else {
	*/
	    // Badly wrong
	if(inv == 0)
	    ((PointInterpTransform *)this)->inv = new PointInterpTransform(cs1.getInverse(), cs2.getInverse(), fract);
	return *inv;
    }
};


/** A transform that delegates all to another tranform.
 * Used to put transforms from a child vobscene into a parent,
 * so that the parent's all transforms can be destroyed easily.
 */
struct DelegatedTransform : public Transform {
    const Transform &other;

    DelegatedTransform(const Transform &other0) : other(other0) { }

    virtual bool shouldBeDrawn() const { return other.shouldBeDrawn(); }
    virtual ZPt transform(const ZPt &p) const {
	return other.transform(p);
    }
    virtual void vertex(const ZPt &p) const {
	other.vertex(p);
    }
    virtual bool isNonlinear() const {
	return other.isNonlinear();
    }
    virtual float nonlinearity(const ZPt &p, float radius) const {
	return other.nonlinearity(p, radius);
    }
    virtual bool canPerformGL() const {
	return other.canPerformGL();
    }
    virtual bool performGL() const {
	return other.performGL();
    }
    virtual const Transform &getInverse() const {
	return other.getInverse();
    }
    virtual void dump(std::ostream &out) const {
	other.dump(out);
    }
    virtual Pt getSqSize() const {
	return other.getSqSize();
    }
};


//
// -------------------- Coorder implementation

Coorder::Coorder(TransformFactory fac, ObjectStorer<ChildVS> *cvs) : 
    transformFactory(fac), 
    childVsStorer(cvs),
    cs1_tmp(0), cs2_tmp(0) 
{
}
Coorder::~Coorder() {
    DBG(dbg_coorder) << "Deleting coordset "<<this<<" "<<cs1_tmp<<" "<<cs2_tmp<<"\n";
    clean();
    if(cs1_tmp) { delete cs1_tmp; cs1_tmp = 0; }
    if(cs2_tmp) { delete cs2_tmp; cs2_tmp = 0; }
}

void Coorder::clean() {
    for(unsigned i=ninitCS; i<cs.size(); i++) {
	if(cs[i]) {
	    delete cs[i];
	    cs[i] = 0;
	}
    }
    for(std::map<int, Coorder *>::iterator i=childCoorders.begin(); 
		i!= childCoorders.end(); i++) {
	delete i->second;
    }
    childCoorders.clear();
}

// Once types are checked, this method checks the parents.
bool Coorder::shouldInterpolate(int cs1, int cs2, int nprev) {
    DBG(dbg_coorder) << "Shouldinterp "<<cs1<<" "<<cs2<<" "<<nprev<<"\n";
    for(int i=0; i<nprev; i++) {
	int par1 = inds1[cs1+1+i];
	if(!cs[par1]) return false;
	if(par1 >= interpinds[0]) return false;
	int par2 = inds2[cs2+1+i];
	int par1interpsto = (par1 > 0 ? interpinds[par1] : 0);
	if(par1interpsto != par2) return false;
    }
    DBG(dbg_coorder) << "DO interpolate\n";
    return true;
}

void Coorder::setPoints( int ninitCS, Transform **initCS,
			    int ninds, 
			     int *inds1, float *points1, 
			     int *interpinds, 
			     int *inds2, float *points2, 
			     float fract, bool show1) {
    DBG(dbg_coorder) << "Set points "<<ninds<<" "
		<< inds1 << " " 
		<< points1 << " " 
		<< interpinds << " " 
		<< inds2 << " " 
		<< points2 << " " 
		<< fract << " " 
		<< show1 << "\n" ;
#define ASG(x) this->x = x;
    ASG(ninitCS)
    ASG(ninds)
    ASG(inds1)
    ASG(points1)
    ASG(interpinds)
    ASG(inds2)
    ASG(points2)
	
    params.resize(0);
    DBG(dbg_coorder)  << "Resized\n";
    params.reserve(3*ninds);
    DBG(dbg_coorder)  << "Reserved\n";
    cs.resize(ninds);
    DBG(dbg_coorder)  << "Resized2\n";

    for(int i=0; i<ninitCS; i++)
	cs[i] = initCS[i];

    DBG(dbg_coorder)  << "setroot\n";
    int lastIndSize = 1;

    // these have to be created only once.
    DBG(dbg_coorder) << "Removing tmps "<<this<<" "<<cs1_tmp<<" "<<cs2_tmp<<"\n";
    if(cs1_tmp) { delete cs1_tmp; cs1_tmp = 0; }
    if(cs2_tmp) { delete cs2_tmp; cs2_tmp = 0; }

    this->maxcs = ninds;

    for(int i=ninitCS; i<ninds; i+=lastIndSize) {
	DBG(dbg_coorder)  << "loop "<<i<<"\n";

	// Handle the child vobscene cases.
	if(inds1[i] == -1) {
	    // -1 = create a child coorder.

	    lastIndSize = 3 + inds1[i+2];

	    ChildVS *childVS = childVsStorer->get(inds1[i+1]);

	    // XXX This implementation is wasteful!
	    // Need to change to reuse the functions;
	    // we know there will be no self-recursion
	    Coorder *childCoorder = new Coorder(transformFactory, childVsStorer);

	    Transform *parents[inds1[i+2]];
	    for(int j=0; j<inds1[i+2]; j++) {
		parents[j] = get(inds1[i+3+j]);
		if(parents[j] == 0) {
		    delete childCoorder;
		    cout << "oh no!!!\n";
		    continue;
		}
	    }

	    childCoorder->setPoints(inds1[i+2], parents,
			    childVS->coorderInds.size(),
			    &(childVS->coorderInds[0]),
			    &(childVS->coorderFloats[0]),
			    0, 0, 0, 0, 1);

	    this->childCoorders[i] = childCoorder;

	    continue;
	} else if(inds1[i] == -2) {
	    lastIndSize = 3;
	    std::map<int, Coorder *>::iterator iter =
		childCoorders.find(inds1[i+1]);
	    if(iter == childCoorders.end()) continue;
	    Transform *base = (*iter).second->get(inds1[i+2]);
	    if(base != 0)
		cs[i] = new DelegatedTransform(*base);
	    continue;
	}

	int parind = params.size();

	// Point TYPE ála is it buoycs, transcs etc.
	int tp = inds1[i] & ~CSFLAGS;

	Transform *c = 0;
	// Used as a temp; set to null if moved to c.
	Primitives::HierarchicalTransform *tmp_c;
	tmp_c = transformFactory(tp);
	if(!tmp_c) {
	    std::cerr << "OUCH! Coorder factory failure. Aborting\n";
	    abort();
	}
	
	int npars = tmp_c->getNParams();
	int nprev = tmp_c->getNDepends();
	int ind1;
	int csind2;

	lastIndSize = nprev + 2; // typecode, prevs and paramind


	ind1 = inds1[i+1+nprev]; 
	csind2 = ((interpinds && i < interpinds[0]) ? interpinds[i] : -1);
	DBG(dbg_coorder) << "inds: "<<parind<<" typ:"<<tp<<" npars:"<<npars<<
			" ind1:"<<ind1<<" "<<csind2<<"\n";

	int t2 = 0;
	int ind2 = 0;
	if(csind2 > 0) {
	    t2 = inds2[csind2] & ~CSFLAGS;
	    if(t2 != tp) goto interpolatePointwise;
	    ind2 = inds2[csind2 + 1 + nprev]; // same as cs1
	}
	// We need to interpolate. Check the structural constraints:
	// If
	//   1) both are of same type
	//   2) all parents interpolate to each other exactly
	// then use parameter interpolation
	// Else, use pointwise interpolation
	if(csind2 <= 0 || (t2 == tp && shouldInterpolate(i, csind2, nprev))) {

	    if(csind2 <= 0) {

		if(show1) {
		    DBG(dbg_coorder) << "Interpolate but no to, show1\n";
		    for(int j = 0; j<npars; j++) {
			params.push_back(points1[ind1 + j]);
		    }
		} else {
		    DBG(dbg_coorder) << "Interpolate but no to, not showing, continuing\n";
		    continue;
		}
	    } else {
		DBG(dbg_coorder) << "Interpolate \n";
		for(int j = 0; j<npars; j++) {
		    DBG(dbg_coorder) << "Interpolating "
			<<(ind1+j)<<" " <<(ind2+j)<<"  = "
			<< points1[ind1+j]<<" "<< points2[ind2+j]<<"\n";
		    params.push_back(
			lerp(points1[ind1 + j], 
			     points2[ind2 + j], fract));
		}
	    }
	    const Transform *prev[nprev];
	    
	    DBG(dbg_coorder) << "Parents \n";
	    for(int j=0; j<nprev; j++) {
		int parent = inds1[i+1+j];
		Transform *parentcs = get(parent); // cs[parent]; -- check bounds using geT()
		DBG(dbg_coorder) << "Parent "<<j<<" "<<parent<<" "<<parentcs<<" \n";
		if(parentcs == 0) {
		    DBG(dbg_coorder) << "Parent "<<j<<" not interp\n";
		    goto nextInd; // If parent's not interpolating, neither are we.
		}
		prev[j] = parentcs;
	    }
	    DBG(dbg_coorder) << "CS assigned "<<tp<<"\n";

	    /** Finally, creates a new coordsys of demanded type and sets its parents and
	     * params. If the initialized coordsys (according to its initialized attributes) 
	     * decides not to be drawn, it will be deleted and replaced with the NULL pointer. 
	     */
	    tmp_c->setParams(prev, &(params[0]) + parind);
	    c = tmp_c; 
	    tmp_c = 0;
	} else {
	interpolatePointwise:
	    
	    // Now, the hairy case.
	    DBG(dbg_coorder) << "It got hairy now: "<<tp<<" "<<t2<<"\n";
	    if(!cs1_tmp) {
		cs1_tmp = new Coorder(transformFactory, childVsStorer);
		cs1_tmp->setPoints(ninitCS, initCS,
				   ninds, inds1, points1,
				   0,  0,  0,  0, true);
	    }
	    if(!cs2_tmp) {
		int maxind = 0;
		for(int k = 1; k<interpinds[0]; k++)
		    if(interpinds[k] > maxind) 
			maxind = interpinds[k];
		cs2_tmp = new Coorder(transformFactory, childVsStorer);
		cs2_tmp->setPoints(ninitCS, initCS,
				   maxind+1, inds2, points2, 0, 0, 0,
				   0, true);
	    }
	    Transform *cs1_non = cs1_tmp->get(i);
	    Transform *cs2_non = cs2_tmp->get(csind2);

	    if(!cs1_non || !cs2_non) goto nextInd;

	    c = new PointInterpTransform(*cs1_non, *cs2_non, fract);
	}
	{
	    cs[i] = c;
	    c->setActivated((inds1[i] & CSFLAG_ACTIVE) != 0);


	    if (!cs[i]->shouldBeDrawn()) {
	      DBG(dbg_coorder) << "CS should not be drawn... freeing it with delete.\n";
	      delete cs[i]; // Must be deleted, because created with "new" in factories.
	      cs[i] = NULL;
	    }
	}
    nextInd:
	if(tmp_c) delete tmp_c;
    }
    DBG(dbg_coorder)  << "end: "<<cs1_tmp<<" "<<cs2_tmp<<"\n";
}

Coorder::iterator Coorder::begin() {
    return iterator(this->ninitCS, this);
    //return iterator(1, this);
}
Coorder::iterator Coorder::end() {
    return iterator(maxcs, this);
}
    
    
}
