package org.nongnu.libvob.gl.impl.lwjgl.trans.linear;

import java.io.OutputStream;
import java.io.PrintStream;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.nongnu.libvob.gl.impl.lwjgl.Coorder;
import org.nongnu.libvob.gl.impl.lwjgl.Transform;

public class OrthoBoxTransform implements Transform {

    private Transform parent;
    private float z,x,y,xs,ys,w,h;
    public void setYourself(Coorder base, int index, int[] inds, float[] floats) {
//	System.out.println("orthobox");

	int floatInd = inds[index+2];
	int parentCS = inds[index+1];
	parent = base.getTransform(parentCS);
	z = floats[floatInd];
	x = floats[floatInd+1];
	y = floats[floatInd+2];
	xs = floats[floatInd+3];
	ys = floats[floatInd+4];
	w = floats[floatInd+5];
	h = floats[floatInd+6];
    }

    public boolean shouldBeDrawn() {
	return true;
    }

    public Vector3f transform(Vector3f p) {
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
//	System.out.println("trans "+x+", "+y+", "+z);
//	System.out.println("scale "+xs+", "+ys);
	GL11.glTranslatef(x,y,z);
	GL11.glScalef(xs,ys,0);
	return true;
    }

    public Transform getInverse() {
	return null;
    }

    public void dump(PrintStream out) {
    }

    Vector2f sq = new Vector2f();
    public Vector2f getSqSize() {
	sq.set(w,h);
	return sq;
    }

}
