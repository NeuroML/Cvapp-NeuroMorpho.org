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
import java.awt.event.*;
import java.awt.*;
import javax.swing.JLabel;


class popLabel extends JLabel implements MouseListener {
   PopupMenu mymenu;
   
   popLabel (String s, PopupMenu pum) {
      super (s);
      mymenu = pum;
      addMouseListener (this);
   }

   public void mouseClicked(MouseEvent e) {
      mymenu.show(this, 0, 10);
   }


   public void mousePressed (MouseEvent e) {
      //      mymenu.show(this, 0, 10);
   }

   public void mouseReleased (MouseEvent e) {}
   public void mouseEntered (MouseEvent e) {   }
   public void mouseExited (MouseEvent e) {   }

}




