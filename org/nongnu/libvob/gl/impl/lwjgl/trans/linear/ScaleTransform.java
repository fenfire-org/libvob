// (c): Matti J. Katila

// bases on code of Tuomas J. Lukka
package org.nongnu.libvob.gl.impl.lwjgl.trans.linear;

import java.io.OutputStream;
import java.io.PrintStream;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.nongnu.libvob.gl.impl.lwjgl.Coorder;
import org.nongnu.libvob.gl.impl.lwjgl.Transform;

public class ScaleTransform implements Transform {

    private Transform parent;
    private Coorder coorder;
    int TYPE;
    private float xs, ys, zs;
    public void setYourself(Coorder base, int index, int[] inds, float[] floats) {
//	System.out.println("scale");
	int floatInd = inds[index+2];
	int parentCS = inds[index+1];
	this.coorder = base;
	this.TYPE = inds[index];
	parent = base.getTransform(parentCS);
	xs = floats[floatInd];
	ys = floats[floatInd+1];
	zs = floats[floatInd+2];
    }

    public boolean shouldBeDrawn() {
	return true;
    }

    public Vector3f transform(Vector3f p) {
	p = parent.transform(p);
	p.x *= xs;
	p.y *= ys;
	p.z *= zs;
	return p;
    }
    public Vector2f transform(Vector2f p) {
	p = parent.transform(p);
	p.x *= xs;
	p.y *= ys;
	return p;
    }

    public void vertex(Vector3f p) {
    }

    public boolean isNonlinear() {
	return false;
    }

    public float nonlinearity(Vector3f p, float radius) {
	return 0;
    }

    public boolean canPerformGL() {
	return true;
    }

    public boolean performGL() {
	parent.performGL();
//	System.out.println("scale "+xs+", "+ys+", "+zs);
	GL11.glScalef(xs,ys,zs);
	return true;
    }

    public Transform getInverse() {
	ScaleTransform inverse = (ScaleTransform) coorder.createTransform(TYPE);
	inverse.xs = 1f/xs;
	inverse.ys = 1f/ys;
	inverse.zs = 1f/zs;
	inverse.parent = parent.getInverse();
	return inverse;
    }

    public void dump(PrintStream out) {
	out.print("(scale "+xs+", "+ys+", "+zs+")");
    }

    public Vector2f getSqSize() {
	Vector2f ret = parent.getSqSize();
	// haha..  scale shall not affect to square at all..
	// ret.x *= xs;
	// ret.y *= ys;
	return ret;
    }

}
