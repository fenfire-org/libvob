/*
test.cxx
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

#include <boost/test/unit_test.hpp>
#include <boost/test/floating_point_comparison.hpp>

#include "vob/Transform.hxx"
#include "vob/trans/Primitives.hxx"
#include "vob/trans/LinearPrimitives.hxx"

using namespace Vob::Primitives;
using namespace Vob;

RootCoords root;
Transform *rootPtr = &root;

void testTranslateXYZ() {
    typedef PrimitiveHierarchicalTransform<TranslateXYZ_Explicit> T;
    T t;
    float pars[3] = { 1.5, 2.5, 3.5 };
    BOOST_CHECK_EQUAL(t.getNParams(), 3);
    t.setParams((const Transform**)&rootPtr, pars);
    ZPt p = t.transform(ZPt(100,200,300));
    BOOST_CHECK_CLOSE(p.x+.0, 101.5, .001);
    BOOST_CHECK_CLOSE(p.y+.0, 202.5, .001);
    BOOST_CHECK_CLOSE(p.z+.0, 303.5, .001);

    BOOST_CHECK_EQUAL(t.nonlinearity(ZPt(1,2,3), 10), 0);

    Pt sq = t.getSqSize();
    BOOST_CHECK_EQUAL(sq.x, 1);
    BOOST_CHECK_EQUAL(sq.y, 1);

    const Transform &it = t.getInverse();
    p = it.transform(ZPt(100,200,300));
    BOOST_CHECK_CLOSE(p.x+.0, 98.5, .001);
    BOOST_CHECK_CLOSE(p.y+.0, 197.5, .001);
    BOOST_CHECK_CLOSE(p.z+.0, 296.5, .001);

}

void testDependent_UnitSqBox() {
    typedef PrimitiveHierarchicalTransform<Box_Explicit> T1;
    typedef PrimitiveHierarchicalTransform<UnitSqBox> T2;
    T1 t1;
    T1 *t1p = &t1;
    T2 t2;
    float pars[2] = {100, 200};
    t1.setParams((const Transform**)&rootPtr, pars);
    t2.setParams((const Transform**)&t1p, pars);
    ZPt p = t2.transform(ZPt(0,0,0));
    BOOST_CHECK_CLOSE(p.x+.0, 0., .001);
    BOOST_CHECK_CLOSE(p.y+.0, 0., .001);
    BOOST_CHECK_CLOSE(p.z+.0, 0., .001);
    p = t2.transform(ZPt(1,1,1));
    BOOST_CHECK_CLOSE(p.x+.0, 100., .001);
    BOOST_CHECK_CLOSE(p.y+.0, 200., .001);
    BOOST_CHECK_CLOSE(p.z+.0, 1., .001);

}

float params[100];

template<class T> void code(T t__) {
    typedef PrimitiveHierarchicalTransform<T> H;
    H *t = new H();
    t->setParams((const Transform**)&rootPtr, params);
    delete t;
}

void testCodeGeneration() {
    code(TranslateXYZ_Explicit());
    code(ScaleXYZ_Explicit());
    code(Unit());
    code(Box_Explicit());
    code(AffineXY_Explicit());
    code(Ortho_Explicit());
    code(UnitSqBox());
}

using boost::unit_test_framework::test_suite;

test_suite*
init_unit_test_suite( int argc, char * argv[] ) {
    test_suite* test= BOOST_TEST_SUITE( "Test linear primitives" );

    test->add( BOOST_TEST_CASE( &testTranslateXYZ ));
    test->add( BOOST_TEST_CASE( &testDependent_UnitSqBox ));
    test->add( BOOST_TEST_CASE( &testCodeGeneration ));

    return test;
}

