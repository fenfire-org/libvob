/*
Gen.cxx
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of LibVob.
 *    
 *    LibVob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    LibVob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with LibVob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

#define VOB_JAVAPREFIX "Java_org_nongnu_libvob_gl_GLRen_"

#include <vob/jni/Generate.hxx>
#include "jnivobs.hxx"

int main() {
    std::ifstream templ("src/jni/GLRen.template.java");
    std::ifstream templtrans("src/jni/GLVobCoorder_Gen.template.java");
    std::ifstream templtrans2("src/jni/AWTVobCoorder_Gen.template.java");

    std::ofstream java("org/nongnu/libvob/gl/GLRen.java");
    std::ofstream javatrans(
	    "org/nongnu/libvob/impl/gl/GLVobCoorder_Gen.java");
    std::ofstream javatrans2(
	    "org/nongnu/libvob/impl/awt/AWTVobCoorder_Gen.java");
    std::ofstream jni("src/jni/GLRen.gen.cxx");
    java << "//COMPUTER GENERATED DO NOT EDIT\n";

    // renderable templates
    std::istreambuf_iterator<char> in(templ);
    std::istreambuf_iterator<char> eos;
    std::ostreambuf_iterator<char> out(java);
    std::copy(in, eos, out);

    // gl vob coorder
    std::istreambuf_iterator<char> tin(templtrans);
    std::ostreambuf_iterator<char> tout(javatrans);
    std::copy(tin, eos, tout);

 std:cout << "\nASDFSADFADSFAS\n\n";
 std:cout << " this src is not used anymore?\n";

    // awt vob coorder
    std::istreambuf_iterator<char> t2in(templtrans2);
    std::ostreambuf_iterator<char> t2out(javatrans2);
    std::copy(t2in, eos, t2out);


    java << VOB_JAVASTREAM.str()<<"\n\n";
    java << "}\n";

    javatrans << VOB_TRANSJAVASTREAM.str()<<"\n\n";
    javatrans << "}\n";

    javatrans2 << VOB_TRANSJAVASTREAM.str()<<"\n\n";
    javatrans2 << "}\n";


    jni << "//COMPUTER GENERATED DO NOT EDIT\n";
    jni << "#include <vob/jni/Types.hxx>\n";
    jni << "#include <vob/jni/Define.hxx>\n";
    jni << "#include \"jnivobs.hxx\"\n";
    jni << "#include \"org_nongnu_libvob_gl_GLRen.h\"\n";
    jni << "using namespace Vob::JNI;\n";
    jni << "using namespace Vob::Primitives;\n";
    jni << "namespace Vob { \n";
    jni << "#define TRANSTYPE(x) return new PrimitiveHierarchicalTransform<x>()\n";
    jni << "HierarchicalTransform *defaultTransformFactory(int id) {\n";
    jni << "	switch(id) {\n" << VOB_TRANSCSTREAM.str()<<"\n";
    jni << "	default: return 0;\n";
    jni << "	}}\n";
    jni << "} \n";
    jni << "using namespace Vob; using namespace Vob::Vobs; \n";
    jni << "extern \"C\" { \n";
    jni << VOB_JNISTREAM.str()<<"\n\n";
    jni << "}\n";
    return 0;
}


