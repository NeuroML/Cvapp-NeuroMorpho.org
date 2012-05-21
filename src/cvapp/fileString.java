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


public abstract class fileString {


  public static String readStringFromFile (String fnm) {
     File f;
     String sdat = null;
     char[] ca = new char[1];
     int nmax, nread;

     if (fnm != null && !fnm.equals("null")) {
	try {
	   FileReader fr = new FileReader (fnm);
	   nread = 0;
	   for (nmax = 10000; fr.ready(); nmax *= 2) {
	      ca = new char[nmax];
	      nread = fr.read (ca, 0, ca.length);
	   }
	   fr.close();
	   sdat = new String (ca, 0, nread); 
	} catch (IOException ex) {
	   System.out.println ("file read error ");
	}
     }
     return sdat;
  }




  public static String readStringFromFile () {
     String fnm = getFileNameToRead();
     return (readStringFromFile (fnm));
  }


  public static String getFileNameToRead() {
     Frame fr = new Frame();
     String fnm;
     FileDialog fd = new FileDialog (fr, "arg string", FileDialog.LOAD );
     fd.pack();
     fd.setVisible(true);

     fnm = fd.getDirectory() + fd.getFile();
     return fnm;
  }

  public static String getFileNameToWrite() {
     Frame fr = new Frame();
     String fnm;
     FileDialog fd = new FileDialog (fr, "arg string", FileDialog.SAVE);
     fd.pack();
     fd.setVisible(true);
     fnm = fd.getDirectory() + fd.getFile();
     return fnm;
  }



  public static String getFileName (String mode, String root) {
     boolean read = (mode != null && mode.indexOf ("r") >= 0);
     Frame fr = new Frame();
     String fnm;
     FileDialog fd = new FileDialog (fr, "arg string",
				  (read ? FileDialog.LOAD : FileDialog.SAVE));
     if (root != null) fd.setDirectory (root);
     fd.pack();
     fd.setVisible(true);
     fnm = fd.getDirectory() + fd.getFile();
     return fnm;
  }


  public static String[] getFileName2 (String mode, String root) {
     boolean read = (mode != null && mode.indexOf ("r") >= 0);
     Frame fr = new Frame();
     FileDialog fd = new FileDialog (fr, "arg string",
				  (read ? FileDialog.LOAD : FileDialog.SAVE));
     if (root != null) fd.setDirectory (root);
     fd.pack();
     fd.setVisible(true);
     String[] sa = new String[2];
     sa[0] = fd.getDirectory();
     sa[1] = fd.getFile();
     return sa;
  }



  public static String[] getFileAndRoot (String mode, String root) {
     return getFileName2(mode, root);
  }

  public static String[] getFileAndRoot (String mode) {
     String root = null;
     return getFileName2(mode, root);
  }





  public static String getFileNameToRead(FilenameFilter fnf) {
     Frame fr = new Frame();
     String fnm;
     FileDialog fd = new FileDialog (fr, "arg string", FileDialog.LOAD );
     fd.setFilenameFilter (fnf);
     fd.pack();
     fd.setVisible(true);

     fnm = fd.getDirectory() + fd.getFile();
     return fnm;
  }

  public static String getFileNameToWrite(FilenameFilter fnf) {
     Frame fr = new Frame();
     String fnm;
     FileDialog fd = new FileDialog (fr, "arg string", FileDialog.SAVE);
     fd.setFilenameFilter (fnf);
     fd.pack();
     fd.setVisible(true);
     fnm = fd.getDirectory() + fd.getFile();
     return fnm;
  }






  public static String[] readStringArrayFromFile (String fnm) {
     Vector vs = new Vector();
     File f;

     String[] sdat = null;
     if (fnm != null && !(fnm.equals("null"))) {
	try {
	   f = new File (fnm);
	   FileInputStream in = new FileInputStream (f);
	   BufferedReader dis = new BufferedReader(new InputStreamReader(in));

	   while (dis.ready()) {
	      vs.addElement (dis.readLine());
	   }
	   dis.close();
	} catch (IOException ex) {
	   System.out.println ("file read error ");
	}

	//	System.out.println ("read " + vs.size() + " lines");

	if (vs.size() > 0) {
	   sdat = new String[vs.size()];
	   for (int i = 0; i < vs.size(); i++) {
	      sdat[i] = (String)(vs.elementAt(i));
	   }
	}

     }
     return sdat;

  }


  public static void writeStringToFile (String sdat, String fnm) {
     File f;

     if (fnm != null) {
	try {
	   FileWriter fw = new FileWriter (fnm);
	   fw.write (sdat, 0, sdat.length());
	   fw.close();
	} catch (IOException ex) {
	   System.out.println ("file write error ");
	}
     }
  }


  public static void writeStringArrayToFile (String[] sa, String fnm) {
     StringBuffer sb = new StringBuffer();
     for (int i = 0; i < sa.length; i++) sb.append (sa[i]);
     String sdat = sb.toString();
     writeStringToFile (sdat, fnm);
  }


  public static void writeStringToFile (String sdat) {
     String fnm = getFileNameToWrite();
     writeStringToFile(sdat, fnm);
  }



  public static void trimStringArray (String[] sa) {
     int ns = sa.length;
     for (int i = 0; i < ns; i++) sa[i] = (sa[i]).trim();

  }

  
}



