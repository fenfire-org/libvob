/*   
FileDialog.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
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
package org.nongnu.libvob.layout.component;
import org.nongnu.libvob.layout.*;
import org.nongnu.libvob.layout.component.*;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.libvob.vobs.*;
import org.nongnu.navidoc.util.Obs;
import java.io.File;
import java.util.*;

public class FileDialog extends LobLob {

    public interface Listener {
	void fileChosen(File file);
    }

    protected Listener listener;
    protected WindowManager windowManager;

    protected class ParentDirectory {
	protected File file;

	protected ParentDirectory(File file) { this.file = file; }
    }

    protected java.io.FileFilter fileFilter = new java.io.FileFilter() {
	    public boolean accept(java.io.File f) {
		if(f.isDirectory()) return true;
		return f.getName().endsWith(".timl");
	    }
	};

    protected Model dirModel, selectionModel;
    protected ListModel filesModel;
    
    protected Obs dirModelObs = new Obs() { public void chg() {
	java.io.File dir = (java.io.File)dirModel.get();
	
	java.io.File[] files = dir.listFiles(fileFilter);
	java.util.Arrays.sort(files, new java.util.Comparator() {
		public int compare(Object o1, Object o2) {
		    java.io.File f1 = (java.io.File)o1;
		    java.io.File f2 = (java.io.File)o2;
		    if(f1 == null || f2 == null)
			throw new NullPointerException(f1+" "+f2);
		    
		    if(f1.isDirectory() && !f2.isDirectory())
			return -1;
		    else if(!f1.isDirectory() && f2.isDirectory())
			return 1;
		    else
			return f1.compareTo(f2);
		}
	    });
	
	List fileList = new ArrayList(files.length + 1);
	    
	for(int i=-1; i<files.length; i++) {
	    java.io.File file;
	    
	    if(i>=0) {
		file = files[i].getAbsoluteFile();
		fileList.add(file);
	    } else if(dir.getParent() != null) {
		String parentName = dir.getParent();
		if(parentName.equals(""))
		    parentName = "/"; // work around Classpath bug
		file = new java.io.File(parentName);
		file = file.getAbsoluteFile();
		fileList.add(new ParentDirectory(file));
	    } else {
		continue;
	    }
	    
	}		    
	
	filesModel.clear();
	filesModel.addAll(fileList);

	selectionModel.set(fileList.get(0));
    }};

    protected class FileLob extends AbstractDelegateLob {

	protected Lob delegate;
	protected Model file;

	protected FileLob(Model fileModel) {
	    this.file = fileModel;

	    String name;
	    Object element = fileModel.get();
	    
	    if(element instanceof File) {
		File file = (File)element;
		name = file.getName();
		
		if(file.isDirectory()) name = "[DIR] "+name;
	    } else {
		name = "..";
	    }
	    
	    delegate = new Label(name);
	}

	protected Lob getDelegate() { return delegate; }

	protected Replaceable[] getParams() {
	    return new Replaceable[] { file };
	}
	protected Object clone(Object[] params) {
	    return new FileLob((Model)params[0]);
	}
    }

    public FileDialog(Listener listener, WindowManager windowManager) {

	this.listener = listener;
	this.windowManager = windowManager;

	final float inf = Float.POSITIVE_INFINITY;
	final float nan = Float.NaN;

	Box box = new Box(Lob.Y);

	dirModel = new ObjectModel(null);
	filesModel = new ListModel.Simple();

	dirModel.set(new java.io.File("/tmp").getAbsoluteFile());

	ListBox listBox = new ListBox(filesModel); {
	    listBox.setTemplate(new FileLob(Parameter.model(ListModel.PARAM)));
	}

	selectionModel = listBox.getSelectionModel();
	if(selectionModel == null) throw new NullPointerException();

	dirModel.addObs(dirModelObs);
	dirModelObs.chg();

	Lob l = new RequestChangeLob(listBox, 300, nan, 300, 200, nan, 200);
	box.add(l);

	box.glue(5, 5, 5);

	Box buttons = new Box(Lob.X);

	l = new Button("Open", new AbstractAction() { public void run() {
	    Object selected = selectionModel.get();
	    File file;
	    
	    if(selected instanceof File)
		file = (File)selected;
	    else if(selected instanceof ParentDirectory)
		file = ((ParentDirectory)selected).file;
	    else
		throw new ClassCastException(""+selected);

	    if(file.isDirectory()) {
		if(file.listFiles(fileFilter) == null) {
		    new MsgBox("Cannot open directory '"+file.getName()+"'",
			       "Error", FileDialog.this.windowManager);
		    
		    return;
		}

		dirModel.set(file);
		AbstractUpdateManager.setNoAnimation();
	    } else {
		FileDialog.this.windowManager.remove(FileDialog.this);
		FileDialog.this.listener.fileChosen(file.getAbsoluteFile());
	    }
	}});
	buttons.add(new KeyLob(l, "Open button"));

	buttons.glue(5, 5, 5);

	l = new Button("Cancel", new AbstractAction() { public void run() {
	    FileDialog.this.windowManager.remove(FileDialog.this);
	}});
	buttons.add(new KeyLob(l, "Cancel button"));

	box.add(new AlignLob(buttons, 1f, .5f, 1f, .5f));

	l = box;

	//l = new Window(l, "Open");

	setDelegate(l);

	windowManager.add(this, "Open");
    }
}
