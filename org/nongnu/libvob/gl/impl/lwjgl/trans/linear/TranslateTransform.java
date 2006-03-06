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

public class TranslateTransform implements Transform {

    private Transform parent;
    private float x=0, y=0, z=0;
    public void setYourself(Coorder base, int index, int[] inds, float[] floats) {
//	System.out.println("translate");
	int floatInd = inds[index+2];
	int parentCS = inds[index+1];
	parent = base.getTransform(parentCS);
	x = floats[floatInd];
	y = floats[floatInd+1];
	z = floats[floatInd+2];
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
//	System.out.println("trans: "+x+", "+y+", "+z);
	GL11.glTranslatef(x,y,z);
	return true;
    }

    public Transform getInverse() {
	return null;
    }

    public void dump(PrintStream out) {
	
    }

    public Vector2f getSqSize() {
	return parent.getSqSize();
    }

}
