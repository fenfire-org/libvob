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
    private float xs, ys, zs;
    public void setYourself(Coorder base, int index, int[] inds, float[] floats) {
//	System.out.println("scale");
	int floatInd = inds[index+2];
	int parentCS = inds[index+1];
	parent = base.getTransform(parentCS);
	xs = floats[floatInd];
	ys = floats[floatInd+1];
	zs = floats[floatInd+2];
    }

    public boolean shouldBeDrawn() {
	return true;
    }

    public Vector3f transform(Vector3f p) {
	// TODO Auto-generated method stub
	return null;
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
	return null;
    }

    public void dump(PrintStream out) {
	out.print("(scale "+xs+", "+ys+", "+zs+")");
    }

    public Vector2f getSqSize() {
	Vector2f ret = parent.getSqSize();
	ret.x *= xs;
	ret.y *= ys;
	return ret;
    }

}
