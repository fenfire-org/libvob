include ../rules.mk

sources = test.cxx

all: test.o test

test: test.o
	$(CXX) $(CXXFLAGS) -o test test.o ../src/trans/*.o ../../libvob-depends/boost/test/*.o  -lGL

include $(sources:.cxx=.dep)
