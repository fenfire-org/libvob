/*
TableLob.java
 *    
 *    Copyright (c) 2003-2005, Benja Fallenstein
 *
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
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.DefaultVobMap; // for chaining coordinate systems
import org.nongnu.navidoc.util.Obs;
import javolution.realtime.*;
import java.util.*;

/** A lob that renders a table of lobs, one in each cell.
 */
public class TableLob extends AbstractLob {
    private static void p(String s) { System.out.println("TableLob:: "+s); }

    public static int MAXSIZE = (1 << 10);

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

    public Lob layout(float width, float height) {
	if(width < 0 || height < 0)
	    throw new IllegalArgumentException("negative size: "+width+" "+height);

	if(width < size.minW || height < size.minH) {
	    // we have not been given enough space to layout ourselves;
	    // work around it

	    Lob l = Lobs.translate(layout(size.minW, size.minH), 0, 0);
	    l = l.layout(width, height);
	    return l;
	}

	TableLayout tl = (TableLayout)LAYOUT_FACTORY.object();
	tl.init(table);

	int rows = table.getRowCount();
	int cols = table.getColumnCount();

	doLayout(tl.posY, rowMinH, rowNatH, rowMaxH, 
		 size.minH, size.natH, size.maxH, height, rows);

	doLayout(tl.posX, colMinW, colNatW, colMaxW, 
		 size.minW, size.natW, size.maxW, width, cols);

	tl.setSize(tl.posX[cols], tl.posY[rows]);
	       
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
	    
	    if(diff < 0)
		throw new Error("XXX negative size diff");

	    cur += nat[i] + diff;

	    if(cur > totalSize) {
		if(cur > totalSize + 0.0005)
		    throw new Error("XXX cannot fit in total size: "+cur+" > "+totalSize);
		else
		    // seems to be round-off error
		    cur = totalSize;
	    }
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

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {
	throw new UnsupportedOperationException("not layouted yet");
    }

    public boolean key(String key) {
	return keyImpl(table, key);
    }
    
    public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			 float x, float y) {
	throw new UnsupportedOperationException("not layouted");
    }

    public List getFocusableLobs() {
	return getFocusableLobsImpl(table);
    }

    protected static boolean keyImpl(Table table, String key) {
	for(int r=0; r<table.getRowCount(); r++) {
	    for(int c=0; c<table.getColumnCount(); c++) {
		PoolContext.enter();
		try {
		    if(table.getLob(r, c).key(key)) 
			return true;
		} finally {
		    PoolContext.exit();
		}
	    }
	}

	return false;
    }

    protected static List getFocusableLobsImpl(Table table) {
	List result = Lists.list();
	for(int r=0; r<table.getRowCount(); r++) {
	    for(int c=0; c<table.getColumnCount(); c++) {
		PoolContext.enter();
		try {
		    result.addAll(table.getLob(r, c).getFocusableLobs());
		} finally {
		    PoolContext.exit();
		}
	    }
	}
	return result;
    }

    private static final class TableLayout extends AbstractLayout {
	private Table table;
	private float[] posX = new float[MAXSIZE], posY = new float[MAXSIZE];

	private TableLayout() {}

	private void init(Table table) {
	    this.table = table;
	}

	protected void setSize(float w, float h) { super.setSize(w, h); }

	public void render(VobScene scene, int into, int matchingParent,
			   float d, boolean visible) {

	    int rows = table.getRowCount();
	    int cols = table.getColumnCount();

	    for(int r=0; r<rows; r++) {

		int lastCS = -1; // in DefaultVobMap chain
		
		for(int c=0; c<cols; c++) {

		    float x = posX[c],     y = posY[r];
		    float w = posX[c+1]-x, h = posY[r+1]-y;

		    int cs = scene.coords.translate(into, x, y);

		    float lobW;

		    PoolContext.enter();
		    try {
			Lob lob = table.getLob(r, c);
			Lob layout = lob.layout(w, h);
			lobW = lob.getSizeRequest().natW;

			layout.render(scene, cs, matchingParent, d, visible);
		    } finally {
			PoolContext.exit();
		    }

	
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

	public boolean key(String key) {
	    return keyImpl(table, key);
	}

	public boolean mouse(VobMouseEvent e, VobScene scene, int cs, 
			     float x, float y) {

	    if(x < 0 || x > posX[table.getColumnCount()]) return false;
	    if(y < 0 || y > posY[table.getRowCount()])    return false;

	    int row;
	    for(row = 0; row<table.getRowCount()-1; row++)
		if(posY[row+1] > y) break;

	    int col;
	    for(col = 0; col<table.getColumnCount()-1; col++)
		if(posX[col+1] > x) break;

	    x -= posX[col];
	    y -= posY[row];

	    Lob lob = table.getLob(row, col);
	    lob = lob.layout(posX[col+1]-posX[col], posY[row+1]-posY[row]);

	    return lob.mouse(e, scene, cs, x, y);
	}

	public List getFocusableLobs() {
	    return getFocusableLobsImpl(table);
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
