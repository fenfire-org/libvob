// (c): Matti J. Katila

// bases directly to code by Tuomas J. Lukka on native side.

package org.nongnu.libvob.gl.impl.lwjgl;



import java.io.PrintStream;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public interface Transform {

    /** 
     * Set up this instance of transform.
     */
    void setYourself(Coorder base, int index, int[] inds, float[] floats);
    
    
    
    //void setActivated(boolean b);

    //boolean isActive();

    /**
     * Check whether this coordinate system should be drawn with the current
     * parameters. This method should not recurse; the parents will already
     * have been asked. It should only consider the parameters of the
     * current coordinate system.
     */
    boolean shouldBeDrawn();

    /**
     * Return the given Vector3f transformed into this coordinate system.
     * Note that some "coordinate systems" may overload this to return e.g.
     * a color in the Vector3f always, without regard to the parameter.
     */
    Vector3f transform(final Vector3f p);
    Vector2f transform(final Vector2f p);

    /**
     * Call glVertex with the given Vector3f transformed into this
     * coordinate system.
     */
    void vertex(final Vector3f p);

    /**
     * Whether the transformation is nonlinear.
     */
    boolean isNonlinear();

    /**
     * How nonlinear is the coordinate system at the given point. The return
     * value is 1/l where l would be a reasonable length for dicing. Returns
     * 0 if dicing is not required. XXX This needs more thought.
     */
    float nonlinearity(final Vector3f p, float radius);

    /**
     * Whether this transformation can be performed by OpenGL alone by using
     * the transformation matrix. If true, calling performGL allows the
     * caller to use plain glVertex calls to place vertices using this
     * transform.
     */
    boolean canPerformGL();

    /**
     * Try to perform the GL operations to set this coordinate system in the
     * current matrix. Only the topmost matrix on the matrix stack may be
     * altered by this routine, no other GL state. The matrix used is
     * determined by the GL current matrix state.
     * <p>
     * This method will NOT set up vertex programs or change any other
     * OpenGL state.
     * 
     * @return True if successful, but if false is returned, then the matrix
     *         is in an undefined state. If this is not acecptable, try
     *         canPerformGL() first.
     */
    boolean performGL();

    /**
     * Get the inverse of this coordinate system. Always returns non-null
     * but it is not guaranteed that this will work properly. (XXX
     * canInvert() ?) The returned inverse is owned by this object and
     * mustn't be deleted by the caller.
     */
    Transform getInverse();

    //void inverse(Transform toBeInversedTransform);
    
    /**
     * Print this coordinate system into the given ostream.
     */
    void dump(PrintStream out);

    /**
     * Get the size of the "unit square" of this coordinate system. For most
     * coordinate systems, this will be Vector2f(1,1) but there are some
     * which alter this, for the purpose of catching mouse clicks at a
     * larger area. A mouse click is "in" this coordinate system, if it is
     * in the area Vector2f(0,0) .. getSqSize()
     * 
     * NOTE: Must be implemented also at GLVobCoorder.java.
     */
    Vector2f getSqSize();

    
    
    
    // tag interfaces.
    
    
    
    
    
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
    static public interface PrimitiveTransform {
    };

    /** A tag interface, implying that the primitive transform
     * may switch off rendering of the vobs in it.
     * This interface implies for a primitive
     * transform that there is shouldBeDrawn() method:
     * <pre>
      	bool shouldBeDrawn() const;
      </pre>
     */
    static public interface DisablablePrimitiveTransform { };

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
    static public interface ParametrizedPrimitiveTransform { };


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
    static public interface DependentPrimitiveTransform { };

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
    static public interface GLPerformablePrimitiveTransform { };

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
    static public interface PotentiallyGLPerformablePrimitiveTransform { };

    /** A tag interface for transformations which may be nonlinear.
     * Implies
     * <pre>
	float nonlinearity(ZPt p, float radius) ;
	</pre>
     */
    static public interface NonlinearPrimitiveTransform { };

    /** A tag for a primitive transform that sets a box size.
     * Implies
     * <pre>
	Pt getSqSize() { return Pt(1,1); }
	</pre>
     */
    static public interface BoxPrimitiveTransform { };


    /** A tag for a transform that is not invertible.
     * This is done this way because the non-invertible 
     * transforms are rightly in a minority.
     */
    static public interface NonInvertiblePrimitiveTransform { };

    /** A tag for a primitive transform that can print out
     * stuff.
     * Implies
     * <pre>
        void dump(std::ostream &out) const { }
     * </pre>
     */
    static public interface DumpingPrimitiveTransform { };

    
}
