/*
GeneratorMain.cxx
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

#include <boost/lambda/bind.hpp>
#include <vector>
#include <algorithm>

#include "Generator.hxx"

std::ostringstream g_JAVASTREAM;
std::ostringstream g_TRANSJAVASTREAM;

/** The canonical static initialization order trick.
 */
static std::vector<Generator*> &generators() {
    static std::vector<Generator*> gens;
    return gens;
}

void registerGenerator(Generator *g) {
    generators().push_back(g);
}

int getTransId() {
    static int id = 0;
    return id++;
}

int main() {

    std::ifstream templ("src/jni/GLRen.template.java");
    std::ifstream templtrans("src/jni/GLVobCoorder_Gen.template.java");
    std::ifstream templtrans2("src/jni/AWTVobCoorder_Gen.template.java");

    std::ofstream java("org/nongnu/libvob/gl/GLRen.java");
    java << "//COMPUTER GENERATED DO NOT EDIT\n";

    std::ofstream javatrans(
	    "org/nongnu/libvob/impl/gl/GLVobCoorder_Gen.java");
    std::ofstream javatrans2(
	    "org/nongnu/libvob/impl/awt/AWTVobCoorder_Gen.java");
    javatrans << "//COMPUTER GENERATED DO NOT EDIT\n";
    javatrans2 << "//COMPUTER GENERATED DO NOT EDIT\n";

    using namespace boost;
    using namespace boost::lambda;

    std::for_each(generators().begin(), generators().end(),
		bind(&Generator::generate, _1));

    std::istreambuf_iterator<char> in(templ);
    std::istreambuf_iterator<char> eos;
    std::ostreambuf_iterator<char> out(java);
    std::copy(in, eos, out);

    // gl coorder
    std::istreambuf_iterator<char> tin(templtrans);
    std::ostreambuf_iterator<char> tout(javatrans);
    std::copy(tin, eos, tout);

    // awt coorder
    std::istreambuf_iterator<char> tin2(templtrans2);
    std::ostreambuf_iterator<char> tout2(javatrans2);
    std::copy(tin2, eos, tout2);

    java << g_JAVASTREAM.str()<<"\n\n";
    java << "}\n";

    javatrans << g_TRANSJAVASTREAM.str()<<"\n\n";
    javatrans << "}\n";

    javatrans2 << g_TRANSJAVASTREAM.str()<<"\n\n";
    javatrans2 << "}\n";

    std::ofstream jni("src/jni/TransFactory.gen.hxx");


    jni << "//COMPUTER GENERATED DO NOT EDIT\n";
    jni << "#include <vob/jni/Types.hxx>\n";
    jni << "#include <vob/jni/Define.hxx>\n";
    jni << "#define NTRANSFORMTYPES "<<getTransId()<<"\n";
    jni << "namespace Vob { using JNI::HierarchicalTransformFactory;\n";
    jni << "extern HierarchicalTransform *((*transFactories[NTRANSFORMTYPES])());\n";
//    jni << "HierarchicalTransform *defaultTransformFactory(int id);\n";
    jni << "}\n";
    return 0;


}

