/*
Stats.cxx
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

#include <boost/format.hpp>

#include <vob/Debug.hxx>
#include <vob/stats/Stats.hxx>

namespace Vob {
namespace Stats {
    DBGVAR(dbg, "Stats");
    using boost::format;

    Statistics::Statistics() {
	first = 0;
    }
    Statistics::~Statistics() {
	for(Collector *i = first; i != 0; i = i->next) {
	    i->stats = 0;
	    i->onList = false;
	    i->next = 0;
	}
    }

    void Statistics::triggered(Collector *c) {
	DBG(dbg) << format("Triggered: %x %d %x %x\n") 
			% c % c->onList % first % c->next;
	if(!c->onList) {
	    // XXX Asserts
	    c->next = first;
	    first = c;
	    c->onList = true;
	}
    }
    
    void Statistics::toBeDeleted(Collector *c) {
	DBG(dbg) << format("ToBeDeleted: %x %x\n") % first % c;
	if(c->onList) {
	    if(first == c) 
		first = c->next;
	    else {
		for(Collector *i = first; i != 0; i = i->next) {
		    if(i->next == c) {
			i->next = c->next;
			return;
		    }
		}
		// XXX ???
	    }
	}
    }
    void Statistics::clear() {
	DBG(dbg) << format("Clear: %x\n") % first;
	Collector *next;
	for(Collector *i = first; i != 0; i = next) {
	    DBG(dbg) << format("Clear stats and onlist: %x\n") % i;
	    i->clear();
	    i->onList = false;
	    next = i->next;
	    i->next = 0;
	}
	first = 0;
    }
    void Statistics::call(void *u) {
	DBG(dbg) << format("Call: %x\n") % first;
	for(Collector *i = first; i != 0; i = i->next) {
	    DBG(dbg) << format("Calling: %x\n") % i;
	    i->call(u);
	}
    }


    Collector::Collector(Statistics *stats) : 
	stats(stats),
	onList(false),
	next(0)
	{
    }
    Collector::~Collector() {
	if(stats) 
	    stats->toBeDeleted(this);
    }

    void Collector::gotStatistics() {
	if(stats)
	    stats->triggered(this);
    }

}
}


