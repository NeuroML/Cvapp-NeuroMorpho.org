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

abstract class WindowDressing {
  
  public static void SunkenBorder (Graphics g, Dimension d, Color c) {
    g.setColor(c);
    g.fillRect(0,0, d.width, d.height);

    g.setColor (c.brighter());
    g.drawRect (0, 0, d.width-1, d.height-1);
    g.drawRect (1, 1, d.width-3, d.height-3);
    g.setColor (c.darker());
    g.drawRect (0, 0, d.width-2, d.height-2);
  }


  public static void SunkenBorderOnly (Graphics g, Dimension d, Color c) {
    g.setColor(c);
    g.setColor (c.brighter());
    g.drawRect (0, 0, d.width-1, d.height-1);
    g.drawRect (1, 1, d.width-3, d.height-3);
    g.setColor (c.darker());
    g.drawRect (0, 0, d.width-2, d.height-2);
  }




  public static void RaisedBorder (Graphics g, Color c, int w, int h) {
    g.setColor (c.darker());
    g.drawRect (2, 2, w-3, h-3);
    g.setColor (c.brighter());
    g.drawRect (0, 0, w-3, h-3);
    g.setColor(c);
    g.drawRect (1, 1, w-3, h-3);


  }


  public static void Button (Graphics g, Dimension d, Color c, int a, int b) {
    int xx, zz;
     
    g.setColor(c);
    g.fillRect(a, a, d.width-2*a, d.height-2*a);

    g.setColor(c.brighter());
    for (int i = a; i <= b; i++) {
       g.drawLine (i, i, i, d.height -  i - 1);
       g.drawLine (i, i, d.width - i - 1,  i);
     }

    g.setColor(c.darker());
    for (int i = a; i <= b; i++) {
       xx = d.width  - i - 1;
       zz = d.height - i - 1;     

       g.drawLine (xx, a+i, xx, zz);
       g.drawLine (a+i, zz, xx, zz);
     }
  }


  public static void drawUpButton (Graphics g, Color c, 
			      int x, int y, int w, int h) {
     g.setColor (c.darker());
     g.drawLine (x,     y+h,   x+w,   y+h);
     g.drawLine (x+1,   y+h-1, x+w,   y+h-1);
     g.drawLine (x+w,   y,     x+w,   y+h);
     g.drawLine (x+w-1, y+1,   x+w-1, y+h);


     g.setColor (c.brighter());
     g.drawLine (x,     y,     x+w,   y);
     g.drawLine (x,     y+1,   x+w-1,   y+1);
     g.drawLine (x,     y,      x,   y+h);
     g.drawLine (x+1,   y,     x+1, y+h-1);


     g.setColor (c);
     g.fillRect (x+2, y+2, w-3, h-3);

  }


  public static void drawDownButton (Graphics g, Color c, 
			      int x, int y, int w, int h) {

     g.setColor (c.darker());
     g.drawLine (x,     y,     x+w,   y);
     g.drawLine (x,     y+1,   x+w,   y+1);
     g.drawLine (x,     y,      x,   y+h);
     g.drawLine (x+1,   y,     x+1, y+h);


     g.setColor (c.brighter());
     g.drawLine (x+1,     y+h, x+w,   y+h);
     g.drawLine (x+2,   y+h-1, x+w,  y+h-1);
     g.drawLine (x+w,   y+1,     x+w,   y+h);
     g.drawLine (x+w-1, y+2,   x+w-1, y+h);

     g.setColor (c);
     g.fillRect (x+2, y+2, w-3, h-3);

  }
  
  public static void drawBoolButton (Graphics g, boolean b,  Color c, 
				     int x, int y, int w, int h) {
     if (b) {
	drawDownButton (g, c, x, y, w, h);
     } else {
	drawUpButton (g, c, x, y, w, h);
     }
  }

}
