// (c) Tuomas J. Lukka

#include <jni.h>
#include <stdlib.h>
#include <vector>
#include <string>

extern void registerNativeMethods(JNIEnv *env);

#define dbg 1

#define p(...) if(dbg) {printf(__VA_ARGS__); printf("\n");}

/** A wrapper main, to workaround unsuccessful
 * dynamic loading of NVIDIA libGL in latest
 * Debian. *May* also speed up startup,
 * but not known yet.
 */

void usage() {
    fprintf(stderr, "Usage:\n"
"	jwrapper (-cp|-classpath) <path> -Dname=value class <params> \n"
    );
    exit(1);
}

int main(int argc, char **argv) {

    std::vector<std::string> args;
    std::vector<std::string> mainargs;

    int i;
    bool gotCP = false;
    // First loop: allow only direct options
    for(i=1; i<argc; i++) {
	if(argv[i][0] != '-') break;
	if(!strcmp(argv[i], "-classpath") ||
	   !strcmp(argv[i], "-cp")) {
	    if(i+1 >= argc)
		usage();
	    args.push_back(std::string("-Djava.class.path=") + 
		    std::string(argv[i+1]));
	    gotCP = true;
	    i++;
	} else {
	    args.push_back(std::string(argv[i]));
	}
    }

    if(!gotCP) {
	char *s = getenv("CLASSPATH");
	if(s) {
	    args.push_back(std::string("-Djava.class.path=") + 
		    std::string(s));
	}
    }

    // The next argument must be the class name to run
    if(i >= argc) usage();

    char *className = argv[i];
    i++;

    // Finally, the parameters for the class itself

    for(; i<argc; i++)
	mainargs.push_back(std::string(argv[i]));

    // Now, create the java param array in the correct format.
    JavaVMOption *options = new JavaVMOption[args.size()];
    for(unsigned i=0; i<args.size(); i++) {
	options[i].optionString = (char *)(args[i].c_str()); // XXX
	options[i].extraInfo = 0;
    }

    p("Params read");

    // Create the VM
    JavaVM* jvm;
    JNIEnv* env;
    JavaVMInitArgs initArgs;
    initArgs.version = JNI_VERSION_1_2;
    initArgs.nOptions = args.size();
    initArgs.options = options;
    initArgs.ignoreUnrecognized = JNI_FALSE;

    if(JNI_CreateJavaVM(&jvm, (void **)&env, &initArgs) < 0) {
	fprintf(stderr, "Couldn't create VM.\n");
	exit(42);
    }

    p("VM created");

    registerNativeMethods(env);
    p("Natives registered");

    for(char *c=className; *c!=0; c++)
	if(*c == '.') *c = '/';
    jclass mainClass = env->FindClass(className);

    if(!mainClass) {
	fprintf(stderr, "Couldn't find main class '%s'.\n", className);
	exit(42);
    }
    p("Main class found");

    jclass stringClass = env->FindClass("java/lang/String");
    if(!stringClass) {
	fprintf(stderr, "Couldn't find string class.\n");
	exit(42);
    }

    jobjectArray jargv = env->NewObjectArray(mainargs.size(), stringClass, 0);
    for(unsigned i=0; i<mainargs.size(); i++) {
	env->SetObjectArrayElement(jargv, i,
		env->NewStringUTF(mainargs[i].c_str()));
    }
    p("Params created");

    jmethodID mainMethod = env->GetStaticMethodID(mainClass, "main", "([Ljava/lang/String;)V");
    p("Calling");
    env->CallStaticVoidMethod(mainClass, mainMethod, jargv);
    p("Called");
    if(env->ExceptionOccurred()) {
	p("Exception occurred");
	env->ExceptionDescribe();
    }

    jvm->DestroyJavaVM();

    return 0;

}
