include ../../rules.mk
include Modules


VOB_OBJS = $(VOBS:%=%.vobjniobj)
TRANS_OBJS = $(TRANS:%=%.transjniobj)

sources = TransFactory.cxx Main.cxx Strings.cxx Transform.cxx Render.cxx Paper.cxx Image.cxx TexAccum.cxx Arrays.cxx QuadFont.cxx FTFont.cxx IndirectTexture.cxx Texture.cxx DisplayList.cxx ChildVS.cxx



CALLGL=../../../callgl
CALLGLOBJS=$(CALLGL)/src/glwrapper/GL_wrapper.o $(CALLGL)/src/callgl.o

LINESOBJ=../lines/Lines.o
PAPEROBJS=../paper/Paper.o 
OSOBJS=../os/Os-GLX.o 
TEXTUREOBJS=../texture/Texture.o ../texture/Texture_pipetexture.o 
UTILOBJS=../util/Perlin.o ../util/buildmipmaps.o ../util/Debug.o ../util/ImageLoader.o ../util/intersect.o
GEOMOBJS=../geom/Quadrics.o
STATSOBJS=../stats/TexAccum.o ../stats/Stats.o

TRANSOBJS=../trans/Transform.o ../trans/Coorder.o

MAINOBJS=../main/Renderer.o ../main/IndirectTexture.o

OBJS= $(sources:%.cxx=%.o)

JNIOBJS=$(OBJS) $(VOB_OBJS) $(TRANS_OBJS) $(OSOBJS) $(TEXTUREOBJS) $(UTILOBJS) $(CALLGLOBJS) $(TRANSOBJS) $(MAINOBJS) $(PAPEROBJS) $(LINESOBJ) $(GEOMOBJS) $(STATSOBJS)

ifeq ($(JAVAHOME),)
    $(error JAVAHOME undefined in src/jni/Makefile (Please see README for info))
endif
EXTRAINCLUDE=-I${JAVAHOME}/include -I${JAVAHOME}/include/linux -I${JAVAHOME}/include/kaffe

LIBS=-lGL -lGLU -lstdc++ -lfreetype `gdk-pixbuf-config --libs`

all: $(sources:.cxx=.o) jnilib 

jnilib: libvobjni.so



GLRen.gen.o: org_nongnu_libvob_gl_GLRen.h
Main.o: org_nongnu_libvob_gl_GL.h

libvobjni.so: $(JNIOBJS) Makefile
	$(CXXLINK) -rdynamic -fpic -o libvobjni.so $(SHARED) $(CXXFLAGS) $(JNIOBJS) $(LIBS)

$(sources:.cxx=.dep) $(OBJS): org_nongnu_libvob_gl_GL.h org_nongnu_libvob_gl_GLRen.h org_nongnu_libvob_gl_Paper.h

include $(sources:.cxx=.dep)
include $(VOBS:%=%.vobjnidep)
include $(TRANS:%=%.transjnidep)

