//COMPUTER GENERATED DO NOT EDIT
/*
GLVobCoorder_Gen.template.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of Libvob.
 *    
 *    Libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */
package org.nongnu.libvob.impl.gl;

public class GLVobCoorder_Gen extends GLVobCoorderBase {
// <vob/trans/ScalarFuncs.hxx>
 public int rational1D22(int d0 , float p0 , float p1 , float p2 , float p3 , float p4 , float p5 ) {
int i = nfloats; 
addFloats(6);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
floats[i+5] = p5;
int j = ninds; addInds(3); inds[j+0] = 0;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setRational1D22Params(int ind, float p0, float p1, float p2, float p3, float p4, float p5) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
floats[i+5] = p5;
}
public int power1D(int d0 , float p0 , float p1 ) {
int i = nfloats; 
addFloats(2);
floats[i+0] = p0;
floats[i+1] = p1;
int j = ninds; addInds(3); inds[j+0] = 1;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setPower1DParams(int ind, float p0, float p1) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
}
public int power1D2(int d0 , float p0 , float p1 , float p2 , float p3 ) {
int i = nfloats; 
addFloats(4);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
int j = ninds; addInds(3); inds[j+0] = 2;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setPower1D2Params(int ind, float p0, float p1, float p2, float p3) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
}

// <vob/trans/FisheyePrimitives.hxx>
 public int distort(int d0 , float p0 , float p1 , float p2 , float p3 , float p4 , float p5 ) {
int i = nfloats; 
addFloats(6);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
floats[i+5] = p5;
int j = ninds; addInds(3); inds[j+0] = 3;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setDistortParams(int ind, float p0, float p1, float p2, float p3, float p4, float p5) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
floats[i+5] = p5;
}

// <vob/trans/DisablablePrimitives.hxx>
 public int cull(int d0 , int d1 , int d2 ) {
int i = nfloats; 
addFloats(0);
int j = ninds; addInds(5); inds[j+0] = 4;
inds[j+1] = d0;
inds[j+2] = d1;
inds[j+3] = d2;
inds[j+4] = i;
 return j;}

// <vob/trans/FunctionalPrimitives.hxx>
 public int concat(int d0 , int d1 ) {
int i = nfloats; 
addFloats(0);
int j = ninds; addInds(4); inds[j+0] = 5;
inds[j+1] = d0;
inds[j+2] = d1;
inds[j+3] = i;
 return j;}
public int concatInverse(int d0 , int d1 ) {
int i = nfloats; 
addFloats(0);
int j = ninds; addInds(4); inds[j+0] = 6;
inds[j+1] = d0;
inds[j+2] = d1;
inds[j+3] = i;
 return j;}

// <vob/trans/LinearPrimitives.hxx>
 public int translate(int d0 , float p0 , float p1 , float p2 ) {
int i = nfloats; 
addFloats(3);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
int j = ninds; addInds(3); inds[j+0] = 7;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setTranslateParams(int ind, float p0, float p1, float p2) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
}
public int scale(int d0 , float p0 , float p1 , float p2 ) {
int i = nfloats; 
addFloats(3);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
int j = ninds; addInds(3); inds[j+0] = 8;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setScaleParams(int ind, float p0, float p1, float p2) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
}
public int rotate(int d0 , float p0 ) {
int i = nfloats; 
addFloats(1);
floats[i+0] = p0;
int j = ninds; addInds(3); inds[j+0] = 9;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setRotateParams(int ind, float p0) {
int i = inds[ind+2];floats[i+0] = p0;
}
public int nadirUnitSq(int d0 , int d1 ) {
int i = nfloats; 
addFloats(0);
int j = ninds; addInds(4); inds[j+0] = 10;
inds[j+1] = d0;
inds[j+2] = d1;
inds[j+3] = i;
 return j;}
public int unit(int d0 ) {
int i = nfloats; 
addFloats(0);
int j = ninds; addInds(3); inds[j+0] = 11;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public int box(int d0 , float p0 , float p1 ) {
int i = nfloats; 
addFloats(2);
floats[i+0] = p0;
floats[i+1] = p1;
int j = ninds; addInds(3); inds[j+0] = 12;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setBoxParams(int ind, float p0, float p1) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
}
public int rotateXYZ(int d0 , float p0 , float p1 , float p2 , float p3 ) {
int i = nfloats; 
addFloats(4);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
int j = ninds; addInds(3); inds[j+0] = 13;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setRotateXYZParams(int ind, float p0, float p1, float p2, float p3) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
}
public int rotateQuaternion(int d0 , float p0 , float p1 , float p2 , float p3 ) {
int i = nfloats; 
addFloats(4);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
int j = ninds; addInds(3); inds[j+0] = 14;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setRotateQuaternionParams(int ind, float p0, float p1, float p2, float p3) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
}
public int affine(int d0 , float p0 , float p1 , float p2 , float p3 , float p4 , float p5 , float p6 ) {
int i = nfloats; 
addFloats(7);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
floats[i+5] = p5;
floats[i+6] = p6;
int j = ninds; addInds(3); inds[j+0] = 15;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setAffineParams(int ind, float p0, float p1, float p2, float p3, float p4, float p5, float p6) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
floats[i+5] = p5;
floats[i+6] = p6;
}
public int ortho(int d0 , float p0 , float p1 , float p2 , float p3 , float p4 ) {
int i = nfloats; 
addFloats(5);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
int j = ninds; addInds(3); inds[j+0] = 16;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setOrthoParams(int ind, float p0, float p1, float p2, float p3, float p4) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
}
public int buoyOnCircle1(int d0 , int d1 , float p0 , float p1 , float p2 , float p3 , float p4 , float p5 ) {
int i = nfloats; 
addFloats(6);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
floats[i+5] = p5;
int j = ninds; addInds(4); inds[j+0] = 17;
inds[j+1] = d0;
inds[j+2] = d1;
inds[j+3] = i;
 return j;}
public void setBuoyOnCircle1Params(int ind, float p0, float p1, float p2, float p3, float p4, float p5) {
int i = inds[ind+3];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
floats[i+5] = p5;
}
public int buoyOnCircle2(int d0 , int d1 , float p0 , float p1 ) {
int i = nfloats; 
addFloats(2);
floats[i+0] = p0;
floats[i+1] = p1;
int j = ninds; addInds(4); inds[j+0] = 18;
inds[j+1] = d0;
inds[j+2] = d1;
inds[j+3] = i;
 return j;}
public void setBuoyOnCircle2Params(int ind, float p0, float p1) {
int i = inds[ind+3];floats[i+0] = p0;
floats[i+1] = p1;
}
public int orthoBox(int d0 , float p0 , float p1 , float p2 , float p3 , float p4 , float p5 , float p6 ) {
int i = nfloats; 
addFloats(7);
floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
floats[i+5] = p5;
floats[i+6] = p6;
int j = ninds; addInds(3); inds[j+0] = 19;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setOrthoBoxParams(int ind, float p0, float p1, float p2, float p3, float p4, float p5, float p6) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
floats[i+2] = p2;
floats[i+3] = p3;
floats[i+4] = p4;
floats[i+5] = p5;
floats[i+6] = p6;
}
public int unitSq(int d0 ) {
int i = nfloats; 
addFloats(0);
int j = ninds; addInds(3); inds[j+0] = 20;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public int between(int d0 , int d1 ) {
int i = nfloats; 
addFloats(0);
int j = ninds; addInds(4); inds[j+0] = 21;
inds[j+1] = d0;
inds[j+2] = d1;
inds[j+3] = i;
 return j;}
public int translatePolar(int d0 , float p0 , float p1 ) {
int i = nfloats; 
addFloats(2);
floats[i+0] = p0;
floats[i+1] = p1;
int j = ninds; addInds(3); inds[j+0] = 22;
inds[j+1] = d0;
inds[j+2] = i;
 return j;}
public void setTranslatePolarParams(int ind, float p0, float p1) {
int i = inds[ind+2];floats[i+0] = p0;
floats[i+1] = p1;
}

// <vob/vobs/Texture.hxx>
 
// <vob/vobs/Debug.hxx>
 
// <vob/vobs/Fillet.hxx>
 
// <vob/vobs/Lines.hxx>
 
// <vob/vobs/GLState.hxx>
 
// <vob/vobs/Program.hxx>
 
// <vob/vobs/Irregu.hxx>
 
// <vob/vobs/Paper.hxx>
 
// <vob/vobs/Text.hxx>
 
// <vob/vobs/Pixel.hxx>
 
// <vob/vobs/Trivial.hxx>
 


}
