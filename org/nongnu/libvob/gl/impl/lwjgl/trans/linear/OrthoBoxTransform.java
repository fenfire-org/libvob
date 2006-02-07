package org.nongnu.libvob.gl.impl.lwjgl.trans.linear;

import java.io.OutputStream;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.nongnu.libvob.gl.impl.lwjgl.Coorder;
import org.nongnu.libvob.gl.impl.lwjgl.Transform;

public class OrthoBoxTransform implements Transform {

    public void setYourself(Coorder base, int index, int[] inds, float[] floats) {
	System.out.println("orthobox");
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
	// TODO Auto-generated method stub
	return false;
    }

    public boolean performGL() {
	// TODO Auto-generated method stub
	return false;
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
