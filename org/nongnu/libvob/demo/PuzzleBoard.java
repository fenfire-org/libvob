/*
PuzzleBoard.java
 *    
 *    Copyright (c) 2005, Benja Fallenstein.
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
package org.nongnu.libvob.demo;
import org.nongnu.libvob.*;
import org.nongnu.libvob.fn.*;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.lob.lobs.TableLob;
import org.nongnu.libvob.impl.NewLobMain;
import javolution.realtime.*;
import java.awt.Color;
import java.util.*;

public class PuzzleBoard {

    private int[][] pieces = new int[4][4];

    public PuzzleBoard() {
	for(int row=0; row<4; row++)
	    for(int col=0; col<4; col++)
		pieces[row][col] = 4*row + col + 1;

	pieces[3][3] = -1; // the gap
    }
    
    public int getPiece(int r, int c) {
	return pieces[r][c];
    }

    public int getGapRow() {
	int r=0, c=0;
	
	LOOP:
	for(r=0; r<4; r++)
	    for(c=0; c<4; c++)
		if(pieces[r][c] < 0)
		    break LOOP;
	
	return r;
    }

    public int getGapColumn() {
	int r=0, c=0;
	
	LOOP:
	for(r=0; r<4; r++)
	    for(c=0; c<4; c++)
		if(pieces[r][c] < 0)
		    break LOOP;
	
	return c;
    }

    public void movePiece(int row, int col) {
	if(row < 0 || row > 3 || col < 0 || col > 3)
	    return; // no piece there

	if(!isNextTo(row, col, getGapRow(), getGapColumn()))
	    return; // cannot move this piece

	pieces[getGapRow()][getGapColumn()] = pieces[row][col];
	pieces[row][col] = -1;
    }

    private boolean isNextTo(int r1, int c1, int r2, int c2) {
	if(r1+1 == r2 && c1 == c2) return true;
	if(r1 == r2+1 && c1 == c2) return true;
	if(r1 == r2 && c1+1 == c2) return true;
	if(r1 == r2 && c1 == c2+1) return true;

	return false;
    }
}
