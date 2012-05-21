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
import javax.swing.JFrame;


class headerFrame extends JFrame implements ActionListener {
   neulucData cell;
   TextArea ta;
   TextField tf;
   Button readB;

   headerFrame () {   
      super ();
      setLayout (new BorderLayout(4, 4));

      rsbPanel top = new rsbPanel();
      top.setRaised (false);
      top.setBorderDepth (10);

      top.setLayout (new BorderLayout());
      tf = new TextField (30);
      readB = new Button ("read template");
      readB.addActionListener (this);
      top.add("Center", tf);
      top.add("East", readB);

      add ("North", top);

      ta = new TextArea (30, 60);
      add ("Center", ta);
      
      rsbPanel buts = new rsbPanel ();
      buts.setRaised(false);
      buts.setBorderDepth (10);

      buts.setLayout (new GridLayout (1, 2, 2, 2));
      Button bapply = new Button ("apply");
      Button bdone = new Button ("done");
      buts.add(bapply);
      buts.add(bdone);

      bapply.addActionListener (this);
      bdone.addActionListener (this);
      add ("South", buts);

      setSize (450, 450);
   }


   public void setCell (neulucData nld) {
      cell = nld;
      ta.setText (cell.getHeaderText());
   }

   
   public void apply () {
      if (cell != null) cell.setHeaderText (ta.getText());
   }
   

   public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source instanceof Button) {
         String sarg = ((Button)source).getLabel();


	 if (sarg.equals("done")) {
	    apply();
	    setVisible(false);

	 } else if (sarg.equals("apply")) {
	    apply();
	    
	 } else if (sarg.equals("read template")) {
	    String s = fileString.readStringFromFile (tf.getText());
	    if (cell != null) cell.updateHeaderText(s);
	    setCell (cell);

	 } else {
	    System.out.println("unknown event string: " + sarg);
	 }
      }
   }
}






