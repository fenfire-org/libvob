/*
Generator.hxx
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

#include <iostream>
#include <sstream>
#include <fstream>

#define VOB_JAVAPREFIX "Java_org_nongnu_libvob_gl_GLRen_"

class Generator;

extern void registerGenerator(Generator *g);

extern std::ostringstream g_JAVASTREAM;
extern std::ostringstream g_TRANSJAVASTREAM;

#define S3(i) #i
#define S2(i) S3(i)
#define S(i) S2(i)

class Generator {
public:
    std::ostringstream JNISTREAM;
    std::ostringstream JNICXXSTREAM;

    std::ostringstream JAVASTREAM;
    std::ostringstream TRANSJAVASTREAM;

    std::string outfile, headerfile;

    Generator(std::string outfile, std::string headerfile) 
	    : outfile(outfile), headerfile(headerfile) {
	registerGenerator(this);
    }

    void generate() {
	g_JAVASTREAM << "// "<< headerfile<<"\n "<<JAVASTREAM.str()<<"\n";
	g_TRANSJAVASTREAM << "// "<<headerfile<<"\n "<<TRANSJAVASTREAM.str()<<"\n";

	std::cout << outfile<<"\n";
	std::string outfilereal = outfile+"new";
	std::ofstream jni(outfilereal.c_str());

	jni << "//COMPUTER GENERATED DO NOT EDIT\n";
	jni << "#include <vob/jni/Types.hxx>\n";
	jni << "#include <vob/jni/Define.hxx>\n";
	jni << "#include "<<headerfile<<"\n";
// Should include this but can't - it'll be modified too often
//	jni << "#include \"org_nongnu_libvob_gl_GLRen.h\"\n";
	jni << "using namespace Vob::JNI;\n";
	jni << "using namespace Vob::Primitives;\n";
	jni << "using namespace Vob; using namespace Vob::Vobs; \n";
	jni << "extern HierarchicalTransform *((*transFactories[])());\n";

	jni << JNICXXSTREAM.str()<<"\n\n";

	jni << "extern \"C\" { \n";
	jni << JNISTREAM.str()<<"\n\n";
	jni << "}\n";
    }


};
