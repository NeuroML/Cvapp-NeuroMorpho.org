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


public class rsbPanel extends Panel {

   boolean raised;
   boolean border;
   int[] iset;

  rsbPanel() {
     super();
     iset = new int[4];

     setRaised (true);
     setBorder (true);
  }


  public void setRaised (boolean b) {
     raised = b;
     if (raised) {
	for (int i = 0; i < 4; i++) iset[i] = 8;
     } else {
	for (int i = 0; i < 4; i++) iset[i] = 4;
     }
     
  }


  public void setBorder (boolean b) {
     border = b;
  }


   public void setRB (boolean r, boolean b) {
      setRaised (r);
      setBorder (b);
   }



  public void setBorderDepth (int idep) {
	for (int i = 0; i < 4; i++) iset[i] = idep;
  }


  public Insets getInsets() {
    return (new Insets (iset[0], iset[1], iset[2], iset[3]));
  }


   public void paint (Graphics g) {
      Dimension d = getSize();
      Color c = getBackground();

      if (border) {
         WindowDressing.SunkenBorder (g, d, c); 
      } else {
         g.setColor(c);
         g.fillRect(0,0, d.width, d.height);
      }

      if (raised) WindowDressing.Button (g, d, c, 2, 3);
   }


  public void rfsEvent (String s) {
  }

}
