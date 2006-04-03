package org.nongnu.libvob.gl.impl.lwjgl.trans.linear;

import java.io.PrintStream;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.nongnu.libvob.gl.impl.lwjgl.Coorder;
import org.nongnu.libvob.gl.impl.lwjgl.Transform;

public class OrthoTransform implements Transform {

    private Coorder coorder;
    private int TYPE;
    private Transform parent;
    private float z,x,y,xs,ys;

    public void setYourself(Coorder base, int index, int[] inds, float[] floats) {
	this.coorder = base;
	this.TYPE = inds[index];

	int floatInd = inds[index+2];
	int parentCS = inds[index+1];
	parent = base.getTransform(parentCS);
	z = floats[floatInd];
	x = floats[floatInd+1];
	y = floats[floatInd+2];
	xs = floats[floatInd+3];
	ys = floats[floatInd+4];
    }

    public boolean shouldBeDrawn() {
	return true;
    }

    public Vector3f transform(Vector3f p) {
	p = parent.transform(p);
	p.x += x;
	p.y += y;
	p.z += z;
	p.x *= xs;
	p.y *= ys;
	return p;
    }

    public Vector2f transform(Vector2f p) {
	p = parent.transform(p);
	p.x += x;
	p.y += y;
	p.x *= xs;
	p.y *= ys;
	return p;
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
	return parent.canPerformGL();
    }

    public boolean performGL() {
	boolean parentPerformed = parent.performGL();
	GL11.glTranslatef(x,y,z);
	GL11.glScalef(xs,ys, 1f);
	return parentPerformed;
    }

    public Transform getInverse() {
	OrthoTransform inv = (OrthoTransform) coorder.createTransform(TYPE);
	inv.x = -x;
	inv.y = -y;
	inv.z = -z;
	inv.xs = 1f/xs;
	inv.ys = 1f/xs;
	inv.parent = parent.getInverse();
	return inv;
    }

    public void dump(PrintStream out) {
	// TODO Auto-generated method stub
	
    }

    public Vector2f getSqSize() {
	return parent.getSqSize();
    }

}
