/*
TableLob.java
 *    
 *    Copyright (c) 2003-2005, Benja Fallenstein
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lob;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.DefaultVobMap; // for chaining coordinate systems
import org.nongnu.navidoc.util.Obs;
import javolution.realtime.*;
import java.util.*;

/** A lob that renders a table of lobs, one in each cell.
 */
public class TableLob extends AbstractLob {
    private static void p(String s) { System.out.println("TableLob:: "+s); }

    public static int MAXSIZE = (1 << 14);

    public interface Table extends Realtime { 
	// XXX make use of new functional system

	int getRowCount();
	int getColumnCount();

	Lob getLob(int row, int column);
    }

    protected Table table;

    protected float[] rowMinH = new float[MAXSIZE];
    protected float[] rowNatH = new float[MAXSIZE];
    protected float[] rowMaxH = new float[MAXSIZE];

    protected float[] colMinW = new float[MAXSIZE];
    protected float[] colNatW = new float[MAXSIZE];
    protected float[] colMaxW = new float[MAXSIZE];

    protected SizeRequest size = new SizeRequest();

    private TableLob() {}

    public static TableLob newInstance(Table table) {
	TableLob tl = (TableLob)LOB_FACTORY.object();
	tl.init(table);
	return tl;
    }

    private void init(Table table) {
	this.table = table;

	int rows = table.getRowCount();
	int cols = table.getColumnCount();

	if(rows > MAXSIZE-1 || cols > MAXSIZE-1)
	    throw new IllegalArgumentException("Table too large: "+rows+" "+
					       cols+" (max size "+(MAXSIZE-1)+
					       " on each axis)");

	rowMinH = FloatArray.newInstance(rows); 
	rowNatH = FloatArray.newInstance(rows); 
	rowMaxH = FloatArray.newInstance(rows); 

	colMinW = FloatArray.newInstance(cols); 
	colNatW = FloatArray.newInstance(cols); 
	colMaxW = FloatArray.newInstance(cols); 
	
	for(int r=0; r<rows; r++) {
	    rowMinH[r] = rowNatH[r] = 0;
	    rowMaxH[r] = SizeRequest.INF;
	}
	for(int c=0; c<cols; c++) {
	    colMinW[c] = colNatW[c] = 0;
	    colMaxW[c] = SizeRequest.INF;
	}

	for(int r=0; r<rows; r++) {
	    for(int c=0; c<cols; c++) {
		PoolContext.enter();
		try {
		    Lob l = table.getLob(r, c);
		    SizeRequest s = l.getSizeRequest();
		    
		    if(s.minH > rowMinH[r]) rowMinH[r] = s.minH;
		    if(s.natH > rowNatH[r]) rowNatH[r] = s.natH;
		    if(s.maxH < rowMaxH[r]) rowMaxH[r] = s.maxH;
		    
		    if(s.minW > colMinW[c]) colMinW[c] = s.minW;
		    if(s.natW > colNatW[c]) colNatW[c] = s.natW;
		    if(s.maxW < colMaxW[c]) colMaxW[c] = s.maxW;

		    if(rowNatH[r] > rowMaxH[r]) rowNatH[r] = rowMaxH[r];
		    if(colNatW[c] > colMaxW[c]) colNatW[c] = colMaxW[c];
		} finally {
		    PoolContext.exit();
		}
	    }
	}

	size.minW = size.natW = size.maxW = 0;
	size.minH = size.natH = size.maxH = 0;

	for(int r=0; r<rows; r++) {
	    size.minH += rowMinH[r];
	    size.natH += rowNatH[r];
	    size.maxH += rowMaxH[r];
	}
	for(int c=0; c<cols; c++) {
	    size.minW += colMinW[c];
	    size.natW += colNatW[c];
	    size.maxW += colMaxW[c];
	}
    }

    public SizeRequest getSizeRequest() {
	return size;
    }

    public Layout layout(float width, float height) {
	TableLayout tl = (TableLayout)LAYOUT_FACTORY.object();
	tl.init(table);

	int rows = table.getRowCount();
	int cols = table.getColumnCount();

	// positions of the rows and columns
	float[] posX = tl.posX, posY = tl.posY;

	doLayout(posY, rowMinH, rowNatH, rowMaxH, 
		 size.minH, size.natH, size.maxH, height, rows);

	doLayout(posX, colMinW, colNatW, colMaxW, 
		 size.minW, size.natW, size.maxW, width, cols);
	       
	return tl;
    }

    private void doLayout(float[] pos, float[] min, float[] nat, float[] max, 
			  float totalMin, float totalNat, float totalMax,
			  float totalSize, int nitems) {

	float totalStretch = totalMax - totalNat;
	float totalShrink  = totalNat - totalMin;
	
	// the amount by which we need to stretch/shrink
	float totalDiff = totalSize - totalNat;

	float cur = 0;

	for(int i=0; i<nitems; i++) {
	    pos[i] = cur;

	    float diff = 0;

	    if(totalDiff > 0) {
		if(totalStretch != 0) {
		    float stretch = max[i] - nat[i];
		    diff = totalDiff * (stretch / totalStretch);
		}
	    } else if(totalDiff < 0) {
		if(totalShrink != 0) {
		    float shrink = nat[i] - min[i];
		    diff = totalDiff * (shrink / totalShrink);
		}
	    }

	    cur += nat[i] + diff;
	}

	pos[nitems] = totalSize;
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    table.move(os);
	    return true;
	}
	return false;
    }

    private static final class TableLayout extends AbstractLayout {
	private Table table;
	private float[] posX = new float[MAXSIZE], posY = new float[MAXSIZE];

	private TableLayout() {}

	private void init(Table table) {
	    this.table = table;
	}

	public Size getSize() {
	    int rows = table.getRowCount(), cols = table.getColumnCount();
	    return Size.newInstance(posX[rows], posY[cols]);
	}

	public void render(VobScene scene, int into, int matchingParent,
			   float d, boolean visible) {

	    int rows = table.getRowCount();
	    int cols = table.getColumnCount();

	    for(int r=0; r<rows; r++) {

		int lastCS = -1; // in DefaultVobMap chain
		
		for(int c=0; c<cols; c++) {

		    float x = posX[c],     y = posY[r];
		    float w = posX[c+1]-x, h = posY[r]-y;

		    Layout layout;
		    float lobW;

		    PoolContext.enter();
		    try {
			Lob lob = table.getLob(r, c);
			layout = lob.layout(w, h);
			lobW = lob.getSizeRequest().natW;
		    } finally {
			PoolContext.exit();
		    }

		    int cs = scene.coords.translate(into, x, y);
		    layout.render(scene, cs, matchingParent, d, visible);
	
		    if(w == lobW) {
			if(lastCS >= 0 && scene.map instanceof DefaultVobMap)
			    ((DefaultVobMap)scene.map).chain(lastCS, cs);
		
			lastCS = cs;
		    } else {
			// do not chain lobs that were stretched or shrunken:
			// chaining is for rendering contiguous characters as a
			// single AWT call, and of course that always renders
			// spaces etc. the same width, so when a space between
			// two words is stretched, we need to render the words
			// separately.
			lastCS = -1;
		    }
		}
	    }
	}

	public boolean move(ObjectSpace os) {
	    if(super.move(os)) {
		table.move(os);
		return true;
	    }
	    return false;
	}
    }

    private static final Factory LAYOUT_FACTORY = new Factory() {
	    public Object create() {
		return new TableLayout();
	    }
	};

    private static final Factory LOB_FACTORY = new Factory() {
	    public Object create() {
		return new TableLob();
	    }
	};
}
