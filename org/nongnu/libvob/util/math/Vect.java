// (c): Matti J. Katila, Benja Fallenstein

package org.nongnu.libvob.util.math;

/** Non-Mathematical presentation of vector. 
 *  Contains small subset of methods 
 *  to handle them, e.g., scalar multiply and abs.
 * 
 *  Perhaps in future this also can handle 
 *  operations with matrixes.
 *
 *  There is a pool of cached vector objects, which can be used
 *  to avoid object creation overhead. The pool must only
 *  be accessed from one thread at a time. The pattern is this:
 *
 *  <pre>
 *      int vectorCount = Vect.currentVectorCount();
 *      try {
 *          // do something with vectors, probably allocating new ones
 *      } finally {
 *          Vect.releaseVectors(vectorCount);
 *      }
 *  </pre>
 *
 *  releaseVectors() releases all vectors allocated since vectorCount
 *  was called (this is why multi-threaded access would wreak havoc).
 */
public class Vect {

    private int size;
    private float[] vector = new float[3];
    private boolean useCache;

    public float x() { assertSize(1); return vector[0]; }
    public float y() { assertSize(2); return vector[1]; }
    public float z() { assertSize(3); return vector[2]; }

    public void x(float f) { assertSize(1); vector[0] = f; }
    public void y(float f) { assertSize(2); vector[1] = f; }
    public void z(float f) { assertSize(3); vector[2] = f; }

    public float get(int i) { assertSize(i); return vector[i]; }
    public void set(int i, float f) { assertSize(i); vector[i] = f; }
    public int set(int i, Vect f) { 
	assertSize(i+f.size());
	int j;
	for (j=0; j<f.size(); j++)
	    vector[i+j] = f.get(j); 
	return j;
    }

    private void assertSize(int s) {
	if(s > size) 
	    throw new IndexOutOfBoundsException(s+" (size is "+size+")");
    }



    private static Vect[] vectors = new Vect[128];
    private static int usedVectors = 0;

    public static int currentVectorCount() { return usedVectors; }
    public static void releaseVectors(int fromVectorCount) {
	usedVectors = fromVectorCount;
    }

    
    private static Vect make(int length) {
	Vect v = vectors[usedVectors];
	if(v == null) v = vectors[usedVectors] = new Vect(true);
	usedVectors++;
	v.size = length;
	for(int i=0; i<3; i++) v.vector[i] = 0;
	return v;
    }


    public static Vect get(float[] vec) {
	Vect v = make(vec.length); 
	System.arraycopy(vec, 0, v.vector, 0, vec.length);
	return v;
    }
    public static Vect get(float x) {
	Vect v = make(1); v.vector[0] = x;
	return v;
    }
    public static Vect get(float x, float y) {
	Vect v = make(2); v.vector[0] = x; v.vector[1] = y;
	return v;
    }
    public static Vect get(float x, float y, float z) {
	Vect v = make(3); v.vector[0] = x; v.vector[1] = y; v.vector[2] = z;
	return v;
    }


    private Vect(boolean useCache) {
	this.useCache = useCache;
    }

    public Vect(float[] vec) {
	System.arraycopy(vec, 0, vector, 0, vec.length);
	size = vec.length;
    }
    public Vect(float x) {
	vector[0] = x;
	size = 1;
    }
    public Vect(float x, float y) {
	vector[0] = x; vector[1] = y;
	size = 2;
    }
    public Vect(float x, float y, float z) {
	vector[0] = x; vector[1] = y; vector[2] = z;
	size = 3;
    }


    private Vect ret(int size) {
	// make a vector that can be returned by one of the functions
	if(useCache)
	    return make(size);

	Vect v = new Vect(false);
	v.size = size;
	return v;
    }


    public int size() { return size; }

    public float scalar(Vect a) {
	if (a.size() != size()) throw new Error("size matters!");

	float ret= 0.0f;
	for (int i=0; i<size; i++) {
	    ret += a.vector[i] * this.vector[i];
	}
	return ret;
    }

    public float abs() { 
	float sum = 0.0f;
	for (int i=0; i<size; i++) 
	    sum += vector[i] * vector[i];
	return (float) Math.sqrt(sum);
    }

    public Vect sum(Vect a) {
	if (a.size() != size()) throw new Error("size matters!");

	Vect ret = ret(size);
	for (int i=0; i<size; i++)
	    ret.vector[i] = a.vector[i] + this.vector[i];
	return ret;
    }

    /**
     * @param trunc Just truncate the old values with new.
     */
    public void sum(Vect a, boolean trunc) {
	if (a.size() != size()) throw new Error("size matters!");

	for (int i=0; i<size; i++)
	    vector[i] = a.vector[i] + this.vector[i];
    }

    public Vect neg(Vect a) {
	if (a.size() != size()) throw new Error("size matters!");

	Vect ret = ret(size);
	for (int i=0; i<size; i++)
	    ret.vector[i] = this.vector[i] - a.vector[i];
	return ret;
    }

    public Vect invert() {
	Vect ret = ret(size);
	for (int i=0; i<size; i++)
	    ret.vector[i] = - this.vector[i];
	return ret;
    }

    public Vect mul(float multiplyValue) { 
	Vect ret = ret(size);
	for (int i=0; i<size; i++) 
	    ret.vector[i] = vector[i] * multiplyValue;
	return ret;
    }

    // doesn catch problems...
    public Vect div(float value) { 
	Vect ret = ret(size);
	for (int i=0; i<size; i++) 
	    ret.vector[i] = vector[i] / value;
	return ret;
    }


    public String toString() {
	return "[ " + x() + ", "+y()+" ] ";
    }
}
