/*
Debug.cxx
 *    
 *    Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
 *    
 *    This file is part of Gzz.
 *    
 *    Gzz is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Gzz is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Gzz; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *    
 */
/*
 * Written by Janne Kujala and Tuomas J. Lukka
 */

#include <map>
#include <iostream>
#include <string>
#include "vob/Debug.hxx"
#include <fstream>
#include <cstdlib>

namespace Vob {

namespace Debug {
    
    std::ostream *debugStream() {
	static const char *str = std::getenv("VOBLOG");
	static std::ostream *dbgstream = 
	    (str ? new std::ofstream(str) : &std::cout);
	static bool setbufyet = false;
	if(!setbufyet && str) {
	    dbgstream->rdbuf()->pubsetbuf(0,0);
	    setbufyet = true;
	}
	return dbgstream;
    }

  struct IntPtr {
    int *ptr;
    IntPtr() : ptr(new int) { *ptr = 0; }
    IntPtr(int *p) : ptr(p) { }
    IntPtr(const IntPtr &p) : ptr(p.ptr) {}
    IntPtr& operator=(const IntPtr &p) {
      delete ptr;
      ptr = p.ptr;
      return *this;
    }
  };

  typedef std::map<std::string, IntPtr> VarMap;

  static VarMap &vars() {
      static VarMap v;
      return v;
  }

  int& var(const char *name) { 
    return *(vars()[name].ptr); 
  }
  
  int& extVar(const char *name, int *var) { 
    if (vars().count(name) > 0) {
      std::cerr << "registerInt: " << name << " already defined: previous definition replaced, but value preserved\n";
      IntPtr &prev = vars()[name];
      *var = *prev.ptr;
      return *(prev.ptr = var);
    }
    return *((vars()[name] = IntPtr(var)).ptr);
  }

  std::vector<const char *> getVarNames() {
    std::vector<const char *> v(vars().size());
    int i = 0;
    for (std::map<std::string, IntPtr>::iterator p = vars().begin(); 
	 p != vars().end(); ++p) {
      v[i++] = p->first.c_str();
    }
    return v;
  }


}

}
