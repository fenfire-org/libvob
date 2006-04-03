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

public class BoxTransform implements Transform {

    private Coorder coorder;
    private int TYPE;
    private Transform parent;
    private float z,x,y,w,h;
    public void setYourself(Coorder base, int index, int[] inds, float[] floats) {
//	System.out.println("box");
	this.coorder = base;
	int floatInd = inds[index+2];
	int parentCS = inds[index+1];
	TYPE = inds[index];
	parent = coorder.getTransform(parentCS);
	z = floats[floatInd];
	x = floats[floatInd+1];
	y = floats[floatInd+2];
	w = floats[floatInd+3];
	h = floats[floatInd+4];

    }

    public boolean shouldBeDrawn() {
	return true;
    }

    public Vector3f transform(Vector3f p) {
	p = parent.transform(p);
	p.x += x;
	p.y += y;
	p.z += z;
	return p;
    }
    public Vector2f transform(Vector2f p) {
	p = parent.transform(p);
	p.x += x;
	p.y += y;
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
//	System.out.println("trans "+x+", "+y+", "+z);
	GL11.glTranslatef(x,y,z);
	return true;
    }

    public Transform getInverse() {
	BoxTransform inv = (BoxTransform) coorder.createTransform(TYPE);
	inv.x = -x;
	inv.y = -y;
	inv.z = -z;
	inv.w = -w;
	inv.h = -h;
	inv.parent = parent.getInverse();
	return inv;
    }

    public void dump(PrintStream out) {
    }

    public Vector2f getSqSize() {
	return parent.getSqSize();
    }

}
