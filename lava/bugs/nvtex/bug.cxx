/*
bug.cxx
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

#include <stdio.h>
#include<GL/glut.h>

#define TEXSIZE 1024
#define NLEVELS 11

unsigned char blue[TEXSIZE*TEXSIZE*4];
unsigned char red[TEXSIZE*TEXSIZE*4];

#define NTEX 6

GLuint texId[NTEX];

void reshape(int w, int h) {
    glViewport(0, 0, w, h);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(0, w, h, 0, 10000, -10000);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
}

#define GLERR() GLERR_impl(__LINE__)

void GLERR_impl(int line) {
    int err = glGetError();
    if(err != GL_NO_ERROR) {
	printf("===== OpenGL error at line %d! %s\n", line, gluErrorString(err));
    }
}

void init()
{
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glClearColor(0.2, 0.3, 0.4, 0.0);

    for(int i=0; i<TEXSIZE*TEXSIZE; i++) {
	blue[i*4 + 0] = 50 * (i%3 == 0);
	blue[i*4 + 1] = 50 * (i%4 == 0);
	blue[i*4 + 2] = 255;
	blue[i*4 + 3] = 255;

	red[i*4 + 0] = 255;
	red[i*4 + 1] = 50 * (i%5 == 0);
	red[i*4 + 2] = 50 * (i%6 == 0);
	red[i*4 + 3] = 255;
    }

    glGenTextures(NTEX, texId);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    /* To simplify things, take only from one mipmap.
     */
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);

    for(int t=0; t<NTEX; t++) {
	glBindTexture(GL_TEXTURE_2D, texId[t]);

	GLERR();
	for(int i=0; i<NLEVELS; i++) {
	    /* Load null teximages.
	     */
	    printf("Load: %d %d\n", i, TEXSIZE>>i);
	    glTexImage2D(GL_TEXTURE_2D, i, GL_RGBA, TEXSIZE >> i, TEXSIZE >> i,
		    0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
	    GLERR();
	}
	glBindTexture(GL_TEXTURE_2D, 0);

	GLERR();
    }

}

void load(int t, unsigned char *tex) {
    printf("Loading texture\n");
    glBindTexture(GL_TEXTURE_2D, texId[t]);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 8);
    for(int i=0; i<NLEVELS; i++) {
	glTexSubImage2D(GL_TEXTURE_2D, i, 0, 0, TEXSIZE >> i, TEXSIZE >> i, 
		    GL_RGBA, GL_UNSIGNED_BYTE, tex);
	GLERR();
    }
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
    glBindTexture(GL_TEXTURE_2D, 0);
}

void draw() {
    printf("Draw\n");
    glClear(GL_COLOR_BUFFER_BIT);
    glEnable(GL_TEXTURE_2D);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

    for(int t=0; t<NTEX; t++) {
	double y = 50 + 20 * t;
	glBindTexture(GL_TEXTURE_2D, texId[t]);

	/* Draw a series of quads of size 16x16 pixels,
	 * taking always more and more of the texture 
	 */
	int qs = 16;
	double tc = 1 / 1024.0;
	glBegin(GL_QUADS);
	for(int i=0; i<20; i++) {
	    double x = 20 * i;
	    glTexCoord2d(0, 0);
	    glVertex2f(x, y);
	    glTexCoord2d(0, tc);
	    glVertex2f(x, y + qs);
	    glTexCoord2d(tc, tc);
	    glVertex2f(x + qs, y + qs);
	    glTexCoord2d(tc, 0);
	    glVertex2f(x + qs, y);

	    tc *= 2;
	}
	glEnd();

	glBindTexture(GL_TEXTURE_2D, 0);
    }

    glDisable(GL_TEXTURE_2D);

    GLERR();

    glutSwapBuffers();

}

int curTex = 0;

void key(unsigned char key, int x, int y) {
    printf("Key: %d %d %d\n",key,x,y);
    switch(key) {
	case 'r': load(curTex, red);
		  break;
	case 'b': load(curTex, blue);
		  break;
	case '+': curTex ++; if(curTex >= NTEX) curTex = NTEX-1; break;
	case '-': curTex --; if(curTex < 0) curTex = 0; break;
    }
    glutPostRedisplay();
}

int main(int argc, char **argv) {
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DOUBLE);
    if(glutCreateWindow("Texture loading") == GL_FALSE)
	return 1;
    init();
    glutReshapeFunc(reshape);
    glutKeyboardFunc(key);
    glutDisplayFunc(draw);
    glutMainLoop();
    return 0;
}
