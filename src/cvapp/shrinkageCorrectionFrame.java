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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


class shrinkageCorrectionFrame extends JFrame implements ActionListener {
   neuronEditorCanvas cellC;
   headerFrame headerF;
   neulucData cell;
   TextField tfx, tfy, tfz;

   shrinkageCorrectionFrame (neuronEditorCanvas c, headerFrame f) {
      super ();
      headerF = f;
      cellC = c;
      setLayout (new BorderLayout());
      
      JPanel tfs = new JPanel(); 
      tfs.setLayout (new GridLayout (3, 1, 3, 5));
      
      tfx = new TextField ("1.0       ");
      tfy = new TextField ("1.0       ");
      tfz = new TextField ("1.0       ");

      tfs.add (new labelTF ("X correction factor: ", tfx));
      tfs.add (new labelTF ("Y correction factor: ", tfy));
      tfs.add (new labelTF ("Z correction factor: ", tfz));
      
      JPanel buts = new JPanel();
      buts.setLayout (new GridLayout (1, 3, 2, 2));
      JButton bcancel = new JButton ("cancel");
      JButton bapply = new JButton ("apply");
      JButton bdone = new JButton ("done");
      buts.add (bcancel);
      buts.add(bapply);
      buts.add(bdone);

      bcancel.addActionListener (this);
      bapply.addActionListener (this);
      bdone.addActionListener (this);
      
      add ("Center", tfs);
      add ("South", buts);

      setSize (300, 300);
   }


   public void setCell (neulucData nld) {
      cell = nld;
      double[] scl = cell.getScale();
      tfx.setText (mytrim(scl[0]));
      tfy.setText (mytrim(scl[1]));
      tfz.setText (mytrim(scl[2]));
   }


   private String mytrim (double d) {
      return (" " + d + "       ").substring (1, 6);
   }
   

   public void applyCorrection () {
      double[] scl = new double[3];
      scl[0] = (new Double(tfx.getText())).doubleValue();
      scl[1] = (new Double(tfy.getText())).doubleValue();
      scl[2] = (new Double(tfz.getText())).doubleValue();
      if (cell == null) {
	 System.out.println ("error - no cell to apply correction to");
      } else {
	 cell.setScale(scl);
	 headerF.setCell (cell);
      }
      cellC.forceRepaint();
   }


   public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source instanceof JButton) {
         String sarg = ((JButton)source).getLabel();
	 if (sarg.equals("cancel")) {
	    setVisible(false);

	 } else if (sarg.equals("done")) {
	    applyCorrection();
	    setVisible(false);

	 } else if (sarg.equals("apply")) {
	    applyCorrection();
	    
	 }


      }
   }
}



class labelTF extends JPanel {
   labelTF (String s, TextField tf) {
      setLayout (new FlowLayout(FlowLayout.CENTER));
      add (new Label (s));
      add (tf);
   }
}




