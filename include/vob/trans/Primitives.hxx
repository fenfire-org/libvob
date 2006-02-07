/*
Primitives.hxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of LibVob.
 *    
 *    LibVob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    LibVob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with LibVob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#ifndef VOB_PRIMITIVES_HXX
#define VOB_PRIMITIVES_HXX

#include <vob/Vec23.hxx>
#include <vob/VecGL.hxx>
#include <vob/Templates.hxx>
#include <vob/Transform.hxx>
#include <boost/type_traits.hpp>

namespace Vob {

/** Primitive transformations and templates to allow their use hierarchically.
 * All classes whose name ends in PrimitiveTransform are empty tag classes
 * which allow primitive transformations to declare that they provide
 * certain features.
 */
namespace Primitives {


    /** A primitive transform, implying an interface
     * used by the templates for building up 
     * transforms.
     * Deriving from this class implies the following method:
     * <pre>
	void tr(const ZPt &from, ZPt &to) const 
	typedef InverseType ???;
	void inverse(InverseType &into) ;
      </pre>
     */
    class PrimitiveTransform {
    };

    /** A tag interface, implying that the primitive transform
     * may switch off rendering of the vobs in it.
     * This interface implies for a primitive
     * transform that there is shouldBeDrawn() method:
     * <pre>
      	bool shouldBeDrawn() const;
      </pre>
     */
    class DisablablePrimitiveTransform { };

    /** A tag interface, implying that the transform requires
     * floating-point parameters.
     * Implies the following interface in the inheriting class:
     * <pre>
	enum { NParams = ??? };
	template<class Ptr> void setParams(Ptr p)
	</pre>
	Note that combining this with DependentPrimitiveTransform
	adds more arguments to the setParams() call.
     */
    class ParametrizedPrimitiveTransform { };


    /** A tag interface, for a transform which 
     * depends on some transform(s).
     * This class unfortunately shows some abstraction through,
     * because sometimes you want the transformation to depend
     * on the real parent (UnitSqBox, Nadir), and sometimes not (cull).
     * Thus, this class will know about the "first parent".
     * Implies the following interface:
     * <pre>
	enum { NDepends = ??? }; // Number of parent coordsyses
	template<class SPtr> void setParams(SPtr depends) ;
	</pre>
	Note that combining this with ParametrizedPrimitiveTransform
	adds more arguments to the setParams() call.
     */
    class DependentPrimitiveTransform { };

    /** A tag interface for transformations which can be performed
     * by manipulating the OpenGL fixed-function vertex pipeline.
     * For instance rotations and transformations can be performed faster by
     * just letting OpenGL combine the transformations into a matrix.
     * Implies the following method:
       <pre>
	void performGL() { }
       </pre>
     *
     */
    class GLPerformablePrimitiveTransform { };

    /** A tag interface for transformations which may <em>sometimes</em>
     * be performed using OpenGL but sometimes not.
     * Implies the following methods:
      <pre>
	bool canPerformGL() ;
	bool performGL() ;
      </pre>
      with the same semantics as in {@link Vob::Transform}.
     *
     * @link Vob::Transform
     */
    class PotentiallyGLPerformablePrimitiveTransform {
    public:
    };

    /** A tag interface for transformations which may be nonlinear.
     * Implies
     * <pre>
	float nonlinearity(ZPt p, float radius) ;
	</pre>
     */
    class NonlinearPrimitiveTransform { };

    /** A tag for a primitive transform that sets a box size.
     * Implies
     * <pre>
	Pt getSqSize() { return Pt(1,1); }
	</pre>
     */
    class BoxPrimitiveTransform { };


    /** A tag for a transform that is not invertible.
     * This is done this way because the non-invertible 
     * transforms are rightly in a minority.
     */
    class NonInvertiblePrimitiveTransform { };

    /** A tag for a primitive transform that can print out
     * stuff.
     * Implies
     * <pre>
        void dump(std::ostream &out) const { }
     * </pre>
     */
    class DumpingPrimitiveTransform { };

    template<class Primitive> class PrimitiveHierarchicalTransform ;

    /** An inverse hierarchical transformation.
     */
    template<class Primitive, class OrigPrimitive> class InverseHierarchicalTransform : 
	    public Transform 
    {
	const Transform *super;
	const Transform *original;

	// Dispatching by the tag classes of t:
	// alternative versions for transforms with and without
	// given features.

	bool performGL(const GLPerformablePrimitiveTransform *_) const {
	    t.performGL();
	    return super->performGL();
	}
	bool performGL(const PotentiallyGLPerformablePrimitiveTransform *_) const {
	    if(!t.performGL()) return false;
	    return super->performGL();
	}
	bool performGL(const void *t) const {
	    return false;
	}

	bool canPerformGL(const GLPerformablePrimitiveTransform *_) const {
	    return super->canPerformGL();
	}
	bool canPerformGL(const PotentiallyGLPerformablePrimitiveTransform *_) const {
	    return t.canPerformGL() && super->canPerformGL() ;
	}
	bool canPerformGL(const void *t) const {
	    return false;
	}

	bool shouldBeDrawn(const DisablablePrimitiveTransform *_) const {
	    return t.shouldBeDrawn();
	}
	bool shouldBeDrawn(const void *_) const {
	    return true;
	}

	bool isNonlinear(const NonlinearPrimitiveTransform *_) const {
	    return true;
	}
	bool isNonlinear(const void *_) const {
	    return super->isNonlinear();
	}

	float selfNonlinearity(const NonlinearPrimitiveTransform *_,
		    const ZPt &p, float radius) const {
	    return t.nonlinearity(p, radius);
	}
	float selfNonlinearity(const void *t,
		    const ZPt &p, float radius) const {
	    return 0;
	}

	void dumpParams(const DumpingPrimitiveTransform *__, std::ostream &out) const {
	    t.dump(out);
	}
	void dumpParams(const void *__, std::ostream &out) const {
	}

    public:
	Primitive t;

	InverseHierarchicalTransform(const Transform *super, const Transform *original) :
	    super(super), original(original) {
	}

	template<class Ptr> void setParams(const Transform *super, Ptr p) {
	    this->super = super;
	    t.setParams(p);
	}


	virtual ZPt transform(const ZPt &p) const {
	    ZPt mp = super->transform(p); 
	    ZPt res;
	    t.tr(mp, res);
	    return res;
	}

	virtual void vertex(const ZPt &p) const {
	    ZPt mp = transform(p);
	    glVertex3f(mp.x, mp.y, mp.z);
	}

	virtual bool performGL() const { return performGL(&t); }

	virtual bool canPerformGL() const { return canPerformGL(&t); }

	virtual float nonlinearity(const ZPt &p, float radius) const {
	    float s = selfNonlinearity(&t, p, radius);
	    ZPt mp;
	    t.tr(p, mp);
	    float su = super->nonlinearity(mp, radius);
	    return s + su; // XXX !!!
	}

        virtual bool shouldBeDrawn() const { return shouldBeDrawn(&t); }

	virtual const Transform &getInverse() const {
	    return *original; // XXX
	}

	virtual void dump(std::ostream &out) const {
	    out << "[inversetrans "<<
		PrimitiveHierarchicalTransform<OrigPrimitive>::name
		    <<"("<<this->canPerformGL()
		    << ") ";
	    super->dump(out);
	    dumpParams(&t, out);
	    out <<"]";
	}

	virtual Pt getSqSize() const {
	    return Pt(1,1);
	}

	virtual bool isNonlinear() const { return isNonlinear(&t); }

    };

    /** A type of transform which exposes a vector of
     * float parameters and parent transformations.
     */
    class HierarchicalTransform : public Transform {
    public:
	virtual int getNParams() = 0;
	virtual int getNDepends() = 0;
	virtual void setParams(const Transform **depends, float *p) = 0;
    };


    /** A hierarchical transformation, which applies the 
     * template argument primitive transformation to the result
     * of the parent transform.
     */
    template<class Primitive> class PrimitiveHierarchicalTransform : 
	    public HierarchicalTransform
    {

	const Transform *super;
	Transform *inverse;

	Primitive t;

	// Dispatching by the tag classes of t:
	// alternative versions for transforms with and without
	// given features.

	bool performGL(const GLPerformablePrimitiveTransform *_) const {
	    if(!super->performGL()) return false;
	    t.performGL();
	    return true;
	}
	bool performGL(const PotentiallyGLPerformablePrimitiveTransform *_) const {
	    if(!super->performGL()) return false;
	    return t.performGL();
	}
	bool performGL(const void *_) const {
	    return false;
	}

	bool canPerformGL(const GLPerformablePrimitiveTransform *_) const {
	    return super->canPerformGL();
	}
	bool canPerformGL(const PotentiallyGLPerformablePrimitiveTransform *_) const {
	    return t.canPerformGL() && super->canPerformGL() ;
	}
	bool canPerformGL(const void *_) const {
	    return false;
	}

	bool shouldBeDrawn(const DisablablePrimitiveTransform *_) const {
	    return t.shouldBeDrawn();
	}
	bool shouldBeDrawn(const void *_) const {
	    return true;
	}

	bool isNonlinear(const NonlinearPrimitiveTransform *_) const {
	    return true;
	}
	bool isNonlinear(const void *_) const {
	    return super->isNonlinear();
	}

	float selfNonlinearity(const NonlinearPrimitiveTransform *_,
		    const ZPt &p, float radius) const {
	    return t.nonlinearity(p, radius);
	}
	float selfNonlinearity(const void *_,
		    const ZPt &p, float radius) const {
	    return 0;
	}

	int getNParams(const void *_) const {
	    return 0;
	}
	int getNParams(const ParametrizedPrimitiveTransform *_) const {
	    return Primitive::NParams;
	}

	int getNDepends(const DependentPrimitiveTransform *_) const {
	    return Primitive::NDepends;
	}
	int getNDepends(const void *_) const {
	    return 1;
	}

	Pt getSqSize(const BoxPrimitiveTransform *___) const {
	    return t.getSqSize();
	}
	Pt getSqSize(const void *_) const {
	    return Pt(1,1);
	}

	// Difficult: double switches: dependent, parametrized for setParams..
	void setParams(const DependentPrimitiveTransform *___, 
		const Transform **depends, float *p) {
	    setDependentParams(&t, depends, p);
	}
	void setParams(const void *__t, 
		const Transform **depends, float *p) {
	    setNondependentParams(&t, p);
	}

	void setDependentParams(const ParametrizedPrimitiveTransform *___,
		const Transform **depends, float *p) {
	    t.setParams(depends, p);
	}
	void setDependentParams(const void *___, 
		const Transform **depends, float *p) {
	    t.setParams(depends);
	}

	void setNondependentParams(const void *___, 
		 float *p) {
	    // noop
	}
	void setNondependentParams(const ParametrizedPrimitiveTransform *___,
		 float *p) {
	    t.setParams(p);
	}

	Transform *getInverse(const void *___) const {
	    InverseHierarchicalTransform<
		typename Primitive::InverseType, 
		Primitive> *inv 
			= new InverseHierarchicalTransform<
			    typename Primitive::InverseType,
			    Primitive >(
				    &(super->getInverse()), this);
	    t.inverse(inv->t);
	    return inv;
	}

	Transform *getInverse(const NonInvertiblePrimitiveTransform *t) const {
	    // XXX Warn?
	    return new RootCoords();
	}


	void dumpParams(const DumpingPrimitiveTransform *__, std::ostream &out) const {
	    t.dump(out);
	}
	void dumpParams(const void *__, std::ostream &out) const {
	}
    public:
	static const char *name;

	PrimitiveHierarchicalTransform() {
	    super = 0;
	    inverse = 0;
	}
	virtual ~PrimitiveHierarchicalTransform() {
	    if(inverse) delete inverse;
	}


	virtual int getNParams() {
	    return getNParams(&t);
	}
	virtual int getNDepends() {
	    return getNDepends(&t);
	}

	virtual void setParams(const Transform **depends, float *p) {
	    this->super = depends[0];
	    setParams(&t, depends, p);
	}


	virtual ZPt transform(const ZPt &p) const {
	    ZPt mp;
	    t.tr(p, mp);
	    return super->transform(mp);
	}

	virtual void vertex(const ZPt &p) const {
	    ZPt mp;
	    t.tr(p, mp);
	    super->vertex(mp);
	}


	virtual bool performGL() const { return performGL(&t); }

	virtual bool canPerformGL() const { return canPerformGL(&t); }

	virtual bool isNonlinear() const { return isNonlinear(&t); }

	virtual float nonlinearity(const ZPt &p, float radius) const {
	    float s = selfNonlinearity(&t, p, radius);
	    ZPt mp;
	    t.tr(p, mp);
	    float su = super->nonlinearity(mp, radius);
	    return s + su; // XXX !!!
	}

        virtual bool shouldBeDrawn() const { return shouldBeDrawn(&t); }
 
	virtual const Transform &getInverse() const {
	    if(!this->inverse)  {
		((PrimitiveHierarchicalTransform<Primitive> *)this)
			->inverse = getInverse(&t);
	    }
	    return *inverse;
	}

	virtual void dump(std::ostream &out) const {
	    out << "[trans "<<name<<"("<<this->canPerformGL()<<
		    ") ";
	    super->dump(out);
	    dumpParams(&t, out);
	    out <<"]";
	}

	virtual Pt getSqSize() const {
	    return getSqSize(&t);
	}

    };


}
}

#endif
