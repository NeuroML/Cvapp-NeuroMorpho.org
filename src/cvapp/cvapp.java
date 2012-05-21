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
import java.applet.*;
import javax.swing.JFrame;

public class cvapp extends Applet implements ActionListener {

    public cvapp() {
        super();
    }

    @Override
    public void init() {

        int w;
        int h;

        //      w = (new Integer(getParameter ("width"))).intValue();
        //      h = (new Integer(getParameter ("height"))).intValue();

        w = 500;
        h = 600;

        setLayout(new BorderLayout());
        Font f = new Font("8x13", Font.PLAIN, 13);

        neuronEditorPanel neupan = new neuronEditorPanel(w, h, f);

        neupan.setParentFrame(new JFrame());

        add("Center", neupan);

        Button bfloat = new Button("f l o a t");

        add("North", bfloat);
        bfloat.addActionListener(this);
        neupan.setReadWrite(true, true);
        setSize(w, h);

    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof Button) {
            String sarg = ((Button) source).getLabel();

            if (sarg.equals("f l o a t")) {
                Dimension d = getSize();
                int w = d.width;
                int h = d.height;
                neuronEditorFrame nef =
                    new neuronEditorFrame(w, h);
                nef.setReadWrite(true, true);
                nef.validate();
                nef.setVisible(true);
            }
        }
    }

//  public static void main (String argv[]) {
//    Font boldFont = new Font ("8x13", Font.PLAIN, 13);
//    Font plainFont = new Font ("8x13", Font.PLAIN, 13);
//
//    neuronEditorFrame nef = new neuronEditorFrame (600, 550);
//
//    nef.setReadWrite (true, true);
//
////    nef.setSize(600, 550);
//    nef.validate();
//    nef.setVisible(true);
//  }
    
}







