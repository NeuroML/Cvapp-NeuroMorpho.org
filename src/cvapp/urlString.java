package cvapp;
/*
    cvapp - neuronal morphology viewer, editor and file converter
    Copyright (C) 1998  Robert Cannon

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

    please send comments, bugs, and feature requests to rcc1@soton.ac.uk
    or see http://www.neuro.soton.ac.uk/cells/

*/
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;


public abstract class urlString {

  /*
  public static String readStringFromURL () {
     Frame fr = new Frame();

     File f;
     String sdat = "null";
     FileDialog fd = new FileDialog (fr, "arg string", FileDialog.LOAD );
     fd.pack();
     fd.setVisible(true);
     String fnm = fd.getDirectory() + fd.getFile();

     if (fnm != null) {
	try {
	   f = new File (fnm);
	   int size = (int)f.length();
	   FileInputStream in = new FileInputStream (f);
	   int bytes_read = 0;
	   byte[] contents = new byte[size];
	   while (bytes_read < size) bytes_read += 
		  in.read (contents, bytes_read, size-bytes_read);
	   
	   sdat = new String(contents, 0);
	} catch (IOException ex) {
	   System.out.println ("file read error ");
	}
     }
     return sdat;
  }


  public static URL getURLToRead() {
     Frame fr = new Frame();
     URL u = null;
     System.out.println ("shold be starting file dialog now...");
     String fnm;
     FileDialog fd = new FileDialog (fr, "arg string", FileDialog.LOAD );
     fd.pack();
     fd.setVisible(true);

     System.out.println ("shold be visible...");
     fnm = fd.getDirectory() + fd.getFile();
     System.out.println ("got " + fnm);
//     fnm = "/users/rcc/morph/3d/000l51.asc";
     return u;
  }

  */



  public static String[] readStringArrayFromURL (URL u) {
     Vector vs = new Vector();

     System.out.println ("opening " + u);
     String[] sdat = null;
     if (u != null) {
	try {
	   InputStream in = u.openStream ();
	   System.out.println ("got input stream");
	   BufferedReader bis = new BufferedReader(new InputStreamReader(in));
	   System.out.println ("got data input stream");



	   while (bis.ready()) {
	      vs.addElement (bis.readLine());
	   }

	} catch (IOException ex) {
	   System.out.println ("URL read error ");
	}

	System.out.println ("read " + vs.size() + " lines");

	if (vs.size() > 0) {
	   sdat = new String[vs.size()];
	   for (int i = 0; i < vs.size(); i++) {
	      sdat[i] = (String)(vs.elementAt(i));
	   }
	}

     }
     return sdat;

  }

}

