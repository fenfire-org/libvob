package org.nongnu.libvob.gl.impl.lwjgl.texture;

import java.nio.FloatBuffer;

public abstract class NamedTexture {

    public abstract void render(TextureParam params, int width, int height, int depth, int components, FloatBuffer data);
    

    
    static public NamedTexture getTexture(String name) {
	
	
//	if (strcmp(type, "TubeConnector") == 0) return new TubeConnector;
//	if (strcmp(type, "TubeFrame") == 0) return new TubeFrame;
//	if (strcmp(type, "bgnoise") == 0) return new bgnoise;
//	if (strcmp(type, "circle") == 0) return new circle;
//	if (strcmp(type, "coordinates") == 0) return new coordinates;
//	if (strcmp(type, "filereader") == 0) return new filereader;
//	if (strcmp(type, "fnoise") == 0) return new fnoise;
	if (name.equals("geometric")) return new GeometricTex(); //param, w,h,d,comps, data);
//	if (strcmp(type, "irregu") == 0) return new irregu;
//	if (strcmp(type, "irreguedge") == 0) return new irreguedge;
//	if (strcmp(type, "line") == 0) return new line;
//	if (strcmp(type, "lines1") == 0) return new lines1;
//	if (strcmp(type, "noise") == 0) return new noise;
//	if (strcmp(type, "palette1") == 0) return new palette1;
//	if (strcmp(type, "ppbg") == 0) return new ppbg;
//	if (strcmp(type, "ppbg1") == 0) return new ppbg1;
//	if (strcmp(type, "rd1") == 0) return new rd1;
//	if (strcmp(type, "sawnoise") == 0) return new sawnoise;
//	if (strcmp(type, "shape1") == 0) return new shape1;
//	if (strcmp(type, "shape2") == 0) return new shape2;
//	if (strcmp(type, "waves") == 0) return new waves;
	throw new Error("no texture implemented!");
    }
    
    
}
