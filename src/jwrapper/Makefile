include ../../rules.mk

sources = jwrapper.cxx 

OBJS= $(sources:%.cxx=%.o)

ifeq ($(JAVAHOME),)
    $(error JAVAHOME undefined in src/jni/Makefile (Please see README for info))
endif
EXTRAINCLUDE=-I${JAVAHOME}/include -I${JAVAHOME}/include/linux -I${JAVAHOME}/include/kaffe

all: build build/jwrapper

build:
	mkdir -p build

build/jwrapper: $(OBJS) build/register.o
	g++ -o build/jwrapper $(OBJS) build/register.o -L${JAVAHOME}/jre/lib/i386/client -ljvm -L../jni -lvobjni

build/register.cxx: methodreader.py
	mkdir -p build
	(cd ../..; python2.3 src/jwrapper/methodreader.py `find org -name "*.java"` >src/jwrapper/build/register.cxx)
	

include $(sources:.cxx=.dep)
