package org.nongnu.libvob.gl.impl.lwjgl.trans.linear;

import java.io.OutputStream;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.nongnu.libvob.gl.impl.lwjgl.Coorder;
import org.nongnu.libvob.gl.impl.lwjgl.Transform;

public class ScaleTransform implements Transform {

    private float xs, ys, zs;
    public void setYourself(Coorder base, int index, int[] inds, float[] floats) {
	System.out.println("scale");
	int floatInd = inds[index+2];
	int parent = inds[index+1];
	xs = floats[floatInd];
	ys = floats[floatInd+1];
	zs = floats[floatInd+2];
    }

    public boolean shouldBeDrawn() {
	// TODO Auto-generated method stub
	return false;
    }

    public Vector3f transform(Vector3f p) {
	// TODO Auto-generated method stub
	return null;
    }

    public void vertex(Vector3f p) {
	// TODO Auto-generated method stub

    }

    public boolean isNonlinear() {
	// TODO Auto-generated method stub
	return false;
    }

    public float nonlinearity(Vector3f p, float radius) {
	// TODO Auto-generated method stub
	return 0;
    }

    public boolean canPerformGL() {
	return true;
    }

    public boolean performGL() {
	GL11.glScalef(xs,ys,zs);
	return true;
    }

    public Transform getInverse() {
	// TODO Auto-generated method stub
	return null;
    }

    public void dump(OutputStream out) {
	// TODO Auto-generated method stub

    }

    public Vector2f getSqSize() {
	// TODO Auto-generated method stub
	return null;
    }

}
