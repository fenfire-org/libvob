===================================
Design of LibVob C++ Vob templates
===================================

The Vob templates were not an easy design:
the previous approach in Gzz's ``gfx/librenderables/renderables.py`` was
not at all optimal, due to the syntactic complexities and the single point
of generation.

The ideal requirements for the template design were:

- Pure C++ templates (possibly some preprocessor macros)

- Author of Vob type should not have to type a list of assignments
  to the members - at most a single list of parameter names.

- Should be able to avoid the extra assignments and initialize
  vob members directly from the parameters passed from Java.

To my surprise, there was a solution. The basic Vob definition
would be ::

    struct SomeVob {
	float someParam;
	vector<float> otherParam;

	template<class T> void render(const T &t0, const T &t1) const {
	    ...
	}
    };

where the template is used to allow later optimizations
to the coordinate system types.

The way to obtain, for code generation, the types of the
parameters, is to add a single method to the class::
    
	template<class F> void params(F &f) {
	    f(
		someParam,
		otherParam
	    );
	}

Now, the caller simply gives as parameter an object
with a generic ``operator()``, and that operator gets as
parameters the types.

The same function can be used to get *references* to all 
members in the JNI code, allowing copyless assignments.
