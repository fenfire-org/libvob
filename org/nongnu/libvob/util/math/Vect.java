// (c): Matti J. Katila

package org.nongnu.libvob.util.math;

/** Non-Mathematical presentation of vector. 
 *  Contains small subset of methods 
 *  to handle them, e.g., scalar multiply and abs.
 * 
 *  Perhaps in future this also can handle 
 *  operations with matrixes.
 */
public class Vect {

    private float[] vector;
    public float x() {return vector[0]; }
    public float y() {return vector[1]; }
    public float z() {return vector[2]; }

    public void x(float f) { vector[0] = f; }
    public void y(float f) { vector[1] = f; }
    public void z(float f) { vector[2] = f; }

    public float get(int i) {return vector[i]; }
    public void set(int i, float f) { vector[i] = f; }
    public int set(int i, Vect f) { 
	int j;
	for (j=0; j<f.size(); j++)
	    vector[i+j] = f.get(j); 
	return j;
    }

    


    public Vect(float[] vec) { 
	this.vector = vec;
    }
    public Vect(float x) { 
	vector = new float[]{x}; 
    } 
    public Vect(float x, float y) {
	vector = new float[]{x,y}; 
    }
    public Vect(float x, float y, float z) {
	vector = new float[]{x,y,z}; 
    }

    public int size() { return vector.length; }

    public float scalar(Vect a) {
	if (a.size() != size()) throw new Error("size matters!");

	float ret= 0.0f;
	for (int i=0; i<vector.length; i++) {
	    ret += a.vector[i] * this.vector[i];
	}
	return ret;
    }

    public float abs() { 
	float sum = 0.0f;
	for (int i=0; i<vector.length; i++) 
	    sum += vector[i] * vector[i];
	return (float) Math.sqrt(sum);
    }

    public Vect sum(Vect a) {
	if (a.size() != size()) throw new Error("size matters!");

	Vect ret = new Vect(new float[vector.length]);
	for (int i=0; i<vector.length; i++)
	    ret.vector[i] = a.vector[i] + this.vector[i];
	return ret;
    }

    /**
     * @param trunc Just truncate the old values with new.
     */
    public void sum(Vect a, boolean trunc) {
	if (a.size() != size()) throw new Error("size matters!");

	for (int i=0; i<vector.length; i++)
	    vector[i] = a.vector[i] + this.vector[i];
    }

    public Vect neg(Vect a) {
	if (a.size() != size()) throw new Error("size matters!");

	Vect ret = new Vect(new float[vector.length]);
	for (int i=0; i<vector.length; i++)
	    ret.vector[i] = a.vector[i] - this.vector[i];
	return ret;
    }

    public Vect invert() {
	Vect ret = new Vect(new float[vector.length]);
	for (int i=0; i<vector.length; i++)
	    ret.vector[i] = - this.vector[i];
	return ret;
    }

    public Vect mul(float multiplyValue) { 
	Vect ret = new Vect(new float[vector.length]);
	for (int i=0; i<vector.length; i++) 
	    ret.vector[i] = vector[i] * multiplyValue;
	return ret;
    }

    // doesn catch problems...
    public Vect div(float value) { 
	Vect ret = new Vect(new float[vector.length]);
	for (int i=0; i<vector.length; i++) 
	    ret.vector[i] = vector[i] / value;
	return ret;
    }


    public String toString() {
	return "[ " + x() + ", "+y()+" ] ";
    }
}
