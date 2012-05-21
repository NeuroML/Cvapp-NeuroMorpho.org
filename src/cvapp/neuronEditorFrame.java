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
import java.awt.event.*;

class neuronEditorFrame extends Frame implements WindowListener  {
   neuronEditorPanel  neupan;
   
  neuronEditorFrame (int w, int h) {   
     //super ("cvapp 1.2  98-09-22");

     Font f = getFont();
     setFont (f);

     neupan = new  neuronEditorPanel (w, h, f); 
     setLayout (new BorderLayout());
     add ("Center", neupan);
     setSize (w, h);
     neupan.setParentFrame (this);
     setReadWrite (true, true);
     addWindowListener (this);
     
  }

	
	
   public void setReadWrite (boolean a, boolean b) {
      neupan.setReadWrite (a, b);
   }

  public void refresh () {
     neupan.refresh ();
  }


  public void loadFile(String s[], String fdir, String frfile)
  {
      neupan.setCell(s, fdir, frfile);
  }
  
   public void windowActivated(WindowEvent e) {}
   public void windowClosed(WindowEvent e) {}
   public void windowClosing(WindowEvent e) {
      setVisible( false);
   } 
   public void windowDeactivated(WindowEvent e)  {}
   public void windowDeiconified(WindowEvent e)  {} 
   public void windowIconified(WindowEvent e)  {}
   public void windowOpened(WindowEvent e)  {}


}
  




