#(C): Janne V. Kujala
# SWIG wrapper for fftw3

CFLAGS = -I/usr/include/python2.3 -O3 -ffast-math -fomit-frame-pointer

lib: _fftw3.so fftw3.py

fftw3.i: /usr/include/fftw3.h fftw3_head.i
	{ \
	echo "// GENERATED FILE - DO NOT EDIT"; \
	cat fftw3_head.i; \
	gcc -E /usr/include/fftw3.h | \
	sed -e 's/; /;\n/g' | \
	grep fftw | \
	egrep -v '^#|guru|do_not_use|threads'; \
	grep '#define' /usr/include/fftw3.h; \
	} > fftw3.i

fftw3.py fftw3_wrap.c: fftw3.i
	swig -python fftw3.i

_fftw3.so: fftw3_wrap.o
	$(CC) -shared fftw3_wrap.o -o _fftw3.so -lfftw3 -lfftw3f -lfftw3l

clean:
	rm -f fftw3.py fftw3.i _fftw3.so fftw3_wrap.c fftw3_wrap.o

test: lib
	python test.py

