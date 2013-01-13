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
import java.awt.List;
import java.net.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

class neuronEditorPanel extends JPanel implements ActionListener, ItemListener {
    
	graphData3 neugd;
	neuronEditorCanvas neucan;
	rescalableFloatSlider rsfsxscale;
	rescalableFloatSlider rsfsyscale;
	rescalableFloatSlider rsfszscale;
	rescalableFloatSlider rsfszcursor;
	neulucData cell;
	doublePointer xscale;
	doublePointer yscale;
	doublePointer zscale;
	doublePointer zcursor;
	ModifiedOptionBar ob; // NMO Developer : optionbar modified to accomodate new changes.
	String fdir = "";
	String frfile;
	String fwfile;
	boolean canReadFiles = true;
	boolean canWriteFiles = true;
	webCellBar wcb;
	popLabel markLabel;
	TextField markTF;
	PopupMenu typeMenu;
	String[] sectionTypes = { "undefined", "soma", "axon", "dendrite",
			"apical dendrite", "custom-1", "custom-2", "custom-n" };
	Checkbox cb1, cb2;
	shrinkageCorrectionFrame shrinkageCorrectionF;
	headerFrame headerF;
	JPanel pneucan; // neucan container - for switching with header;
	int markType = 0;
    
	JDialog messageD;
	//JPanel messageP;
    
	JFrame topF;
    
    protected final static String SKEL_PROJ = "Skeleton Proj.";
    protected final static String AREA_PROJ = "Area Proj.";
    
    protected final static String SAVE_OPTION = "Please select...";
    protected final static String SAVE_NEURON = "NEURON";
    protected final static String SAVE_GENESIS = "GENESIS";
    protected final static String SAVE_NEUROML_v1 = "NeuroML v1.8.1";
    protected final static String SAVE_NEUROML_v2alpha = "NeuroML v2alpha";
    protected final static String SAVE_NEUROML_v2beta = "NeuroML v2beta";
    protected final static String CENTER = "Center";
            


	neuronEditorPanel(int w, int h, Font f) {

		neugd = new graphData3();

		neugd.setCenter((w - 60) / 2, (h - 60) / 2);
		neugd.setMargins(40, 25, 25, 20);
		neugd.setBackground(Color.black);
		neugd.setForeground(Color.white);
		neugd.setMargins(40, 20, 30, 20);
		neugd.setFont(f);

		typeMenu = new PopupMenu();
		for (int i = 0; i < sectionTypes.length; i++) {
			typeMenu.add(new MenuItem(sectionTypes[i]));
		}
		add(typeMenu);

		neucan = new neuronEditorCanvas(w - 40, h - 120, neugd);
		neucan.setFont(f);

		zcursor = new doublePointer(0.02);

		/*
		 * rsfsxscale = new rescalableFloatSlider (rescalableFloatSlider.LOG,
		 * "x rescale", neucan.xScale, -0.3, 0.3); rsfsyscale = new
		 * rescalableFloatSlider (rescalableFloatSlider.LOG, "y rescale",
		 * neucan.yScale, -0.3, 0.3); rsfszscale = new rescalableFloatSlider
		 * (rescalableFloatSlider.LOG, "z rescale", neucan.zScale, -0.3, 0.3);
		 */
		rsfszcursor = new rescalableFloatSlider(rescalableFloatSlider.LOG,
				"red-green dz", zcursor, -3., 0);

		neucan.setzCursor(zcursor);

		rsfszcursor.setWatcher((graphCanvas3) neucan);

		JPanel toppan = new JPanel();
		toppan.setLayout(new GridLayout(1, 9, 1, 1));
		toppan.add(new JButton("trace"));
		toppan.add(new JButton("find"));
		toppan.add(new JButton("clear"));
		toppan.add(new JButton("join"));
		toppan.add(new JButton("ident."));
		toppan.add(new JButton("merge"));
		toppan.add(new JButton("nodes"));
		toppan.add(new JButton("outlines"));
		toppan.add(new JButton("clean"));

		markLabel = new popLabel("as: unknown", typeMenu);
		markTF = new TextField("-1");

		JPanel butpan = new JPanel();
		butpan.setLayout(new GridLayout(4, 1, 1, 2));
		sbPanel butpan0 = new sbPanel();
		sbPanel butpan1 = new sbPanel();
		sbPanel butpan2 = new sbPanel();
		sbPanel butpan3 = new sbPanel();

		CheckboxGroup cbg = new CheckboxGroup();
		butpan0.setLayout(new GridLayout(3, 1, 1, 1));
		butpan0.add(cb1 = new Checkbox("normal", true, cbg));
		butpan0.add(cb2 = new Checkbox("grow", false, cbg));

		butpan0.add(new JButton("add floating"));

		butpan1.setLayout(new GridLayout(4, 1, 1, 1));
		butpan1.add(new JButton("cut"));
		butpan1.add(new JButton("add between"));
		butpan1.add(new JButton("remove"));
		butpan1.add(new JButton("loops"));

		butpan2.setLayout(new GridLayout(4, 1, 1, 1));
		butpan2.add(new JLabel("select:"));
		butpan2.add(new JButton("section"));
		butpan2.add(new JButton("tree"));
		butpan2.add(new JButton("points"));

		butpan3.add(new JButton("delete"));
		butpan3.add(new JButton("mark"));
		butpan3.setLayout(new GridLayout(4, 1, 1, 1));
		butpan3.add(markLabel);
		butpan3.add(markTF);

		butpan.add(butpan0);
		butpan.add(butpan1);
		butpan.add(butpan2);
		butpan.add(butpan3);

		cb1.addItemListener(this);
		cb2.addItemListener(this);

		markTF.addActionListener(this);

		for (int i = typeMenu.getItemCount() - 1; i >= 0; i--) {
			MenuItem c = typeMenu.getItem(i);
			if (c instanceof MenuItem) {
				((MenuItem) c).addActionListener(this);
			}
		}

		for (int i = butpan.getComponentCount() - 1; i >= 0; i--) {
			Component c = butpan.getComponent(i);
			if (c instanceof sbPanel) {
				sbPanel cs = (sbPanel) c;
				for (int j = cs.getComponentCount() - 1; j >= 0; j--) {
					Component d = cs.getComponent(j);
					if (d instanceof JButton) {
						((JButton) d).addActionListener(this);
					}
				}
			}
		}

		for (int i = toppan.getComponentCount() - 1; i >= 0; i--) {
			Component c = toppan.getComponent(i);
			if (c instanceof JButton) {
				((JButton) c).addActionListener(this);
			}
		}

		webCellBar wcb = new webCellBar(this);
		ob = new ModifiedOptionBar(this); // NMO Developer : optionbar modified to accomodate new changes.

		// Following panel is added to provide information to user. owl
		JPanel infoPanel = new JPanel();

		infoPanel.setLayout(new FlowLayout());
        JLabel jl1 = new JLabel("Left/Right Click: Zoom In/Out");
		infoPanel.add(jl1);
        JLabel jl2 = new JLabel("Left/Right Drag: Move/Rotate");
		infoPanel.add(jl2);

		JPanel bars = new JPanel();
		bars.setLayout(new GridLayout(2, 1));
		//  NMO Developer : Functionality not to be included. Hence commented out. 
		// bars.add (wcb);
		bars.add(infoPanel);
		bars.add(ob);

		pneucan = new JPanel();
		pneucan.setLayout(new BorderLayout());
        pneucan.setBorder(BorderFactory.createLoweredBevelBorder());

		//  NMO Developer : Functionality not to be included. Hence commented out.
		// pneucan.add("North", toppan);
		pneucan.add("Center", neucan);

		setLayout(new BorderLayout());
		add("North", bars);

		// NMO Developer : Functionality not to be included. Hence commented out.
		// add ("East", butpan);

		add("Center", pneucan);

		//  NMO Developer : Functionality not to be included. Hence commented out.
		// add ("South", rsfszcursor);

		headerF = new headerFrame();
		shrinkageCorrectionF = new shrinkageCorrectionFrame(neucan, headerF);

		String[] sdat = new String[6];
		sdat[0] = "1 0  0 0 0     4  -1";
		sdat[1] = "2 0  0 10 0    4  1";
		sdat[2] = "3 0  10 20 0   2  2";
		sdat[3] = "4 0 -10 20 0   2  2";
		sdat[4] = "5 0  -15 30 0  1  4";
		sdat[5] = "6 0  -5 30 0   1  4";

		setCell(sdat, "", "default.swc");
	}

	public void setParentFrame(JFrame fr) {
		if (fr != null) {
			topF = fr;
			messageD = new JDialog();
            messageD.setTitle("Please wait...");
            messageD.getContentPane().add(new JLabel("Doing something 1..."));
            messageD.getContentPane().add(new JLabel("Doing something 2..."));
		}
	}

    public neulucData getCell() {
        return cell;
    }
    
    

	public void exit() {
		if (topF != null) {
			topF.setVisible(false);
			topF.dispose();
		}
	}

	public void reverseVideo() {
		neucan.reverseVideo();
	}

	public void setCellWindow() {
		setNormal();
		if (!(neucan.isShowing())) {
			pneucan.add("Center", neucan);
			doLayout();
			validate();
		}
	}

	public void setNormal() {
		cb1.setState(true);
		cb2.setState(false);
		neucan.setNormal();
	}

	public void setData(neulucData dat) {
		cell = dat;
		neucan.setData(cell);
	}

	public void setReadWrite(boolean a, boolean b) {
		canReadFiles = a;
		canWriteFiles = b;
	}

	public void shrinkageCorrect() {
		shrinkageCorrectionF.setCell(cell);
		shrinkageCorrectionF.setVisible(true);
	}

	public void editHeader() {
		headerF.setCell(cell);
		headerF.setVisible(true);
	}

	public void refresh() {
		neucan.repaint();
	}

	public String[] readStringArrayFromURL(URL u) {
		blockingMessageOn("Reading URL", u.toString());
		String[] sa = urlString.readStringArrayFromURL(u);
		blockingMessageOff();
		return sa;
	}

	public void writeStringToFile(String s, String flongwrite) {
		blockingMessageOn("Writing " + flongwrite);
		fileString.writeStringToFile(s, flongwrite);
		blockingMessageOff();
	}

	public String[] readStringArrayFromFile(String flongread) {
		blockingMessageOn("Reading " + flongread);
		String[] sa = fileString.readStringArrayFromFile(flongread);
		blockingMessageOff();
		return sa;
	}

	public void setCell(String[] sdat, String fdir, String frfile) {
		System.out.println("1 " + sdat.length);
		System.out.println("2 " + fdir);
		System.out.println("3 " + frfile);
		blockingMessageOn("parsing", fdir + frfile);
		cell = new neulucData();
		cell.fill(sdat, fdir + frfile);
		sdat = null;
		setData(cell);
		shrinkageCorrectionF.setCell(cell);
		headerF.setCell(cell);
		blockingMessageOff();
		cell.setSourceFileName(frfile);
		ob.setFileNameLabel(cell.getSourceFileName());

	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof JButton) {
			String sarg = ((JButton) source).getLabel();
			processNameEvent(sarg);
		} else if (source instanceof MenuItem) {
			String sarg = ((MenuItem) source).getLabel();
			sarg = e.getActionCommand();

			int newtyp = -1;
			for (int i = 0; i < sectionTypes.length; i++) {
				if (sectionTypes[i].equals(sarg))
					newtyp = i;
			}
			markTF.setText((new Integer(newtyp)).toString());
			if (newtyp >= 0) {
				markType = newtyp;
				int inwt = newtyp;
				if (inwt >= sectionTypes.length)
					inwt = sectionTypes.length - 1;
				markLabel.setText("as: " + sectionTypes[inwt]);
			}
		} else if (source instanceof TextField) {
			String s = markTF.getText();

			int newtyp = (Integer.valueOf(s)).intValue();
			markType = newtyp;
			int inwt = newtyp;
			if (inwt >= sectionTypes.length)
				inwt = sectionTypes.length - 1;
			markLabel.setText("as: " + sectionTypes[inwt]);
			System.out.println("tf text " + s + sectionTypes[inwt]);

		}
	}

	/*
	 * if (gotMarks) { if (cell != null) { cell.markPointTypes (pmark, sarg);
	 * repaint(); } gotMarks = false;
	 */

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if (e.getStateChange() == e.SELECTED) {
			String sarg = (String) (e.getItem());
			processNameEvent(sarg);
		}
	}

	public void processNameEvent(String sarg) {

		if (sarg.equals("trace")) {
			setNormal();
			neucan.trace();
		} else if (sarg.equals("find")) {
			neucan.find();
		} else if (sarg.equals("add between")) {
			setNormal();
			neucan.addNode();
		} else if (sarg.equals("remove")) {
			setNormal();
			neucan.setRemoveMode();
		} else if (sarg.equals("join")) {
			setNormal();
			neucan.join();
		} else if (sarg.equals("merge")) {
			setNormal();
			neucan.merge();
		} else if (sarg.equals("ident.")) {
			setNormal();
			neucan.ident();
		} else if (sarg.equals("clear")) {
			setNormal();
		} else if (sarg.equals("clean")) {
			setNormal();
			neucan.cleanCell();
		} else if (sarg.equals("cut")) {
			setNormal();
			neucan.cut();
		} else if (sarg.equals("drag")) {
			setNormal();
			neucan.drag();
		} else if (sarg.equals("grow")) {
			neucan.grow();
		} else if (sarg.equals("normal")) {
			setNormal();

		} else if (sarg.equals("add floating")) {
			neucan.addFree();
		} else if (sarg.equals("nodes")) {
			// setNormal();
			neucan.showPoints();
		} else if (sarg.equals("outlines")) {
			// setNormal();
			neucan.showOutlines();
		} else if (sarg.equals("loops")) {
			setNormal();
			neucan.showLoops();
		} else if (sarg.equals("section")) {
			setNormal();
			neucan.highlightSection();

		} else if (sarg.equals("points")) {
			setNormal();
			neucan.reallyShowPoints();
			neucan.markPoint();

		} else if (sarg.equals("tree")) {
			setNormal();
			neucan.highlightTree();
		} else if (sarg.equals("mark")) {
			if (cell != null) {
				cell.markHighlightedType(markType);
				neucan.clear();
			}
		} else if (sarg.equals("delete")) {
			if (cell != null) {
				cell.deleteHighlighted();
				neucan.clear();
			}

		} else if (sarg.equals("open")) {
			if (canReadFiles) {

				String[] sa = fileString.getFileName2("r", fdir);
				if (sa != null && sa[0] != null && sa[1] != null) {
					fdir = sa[0];
					frfile = sa[1];

					String[] sdat = readStringArrayFromFile(fdir + frfile);
					setCell(sdat, fdir, frfile);
				}
			}

		} else if (sarg.equals("shrinkage correction")) {
			shrinkageCorrect();

		} else if (sarg.equals("edit header")) {
			editHeader();

		} else if (sarg.equals("save as swc")) {
			if (canWriteFiles) {
				headerF.apply();
				String[] sa = fileString.getFileName2("w", fdir);
				if (sa != null && sa[0] != null) {
					fdir = sa[0];
					fwfile = sa[1];
					blockingMessageOn("formatting as SWC");
					writeStringToFile(cell.write(), fdir + fwfile);
				}

			} else {
				System.out.println("file writing not allowed ");
			}

		} /*else if (sarg.equals("save as Genesis - flat")) {
			if (canWriteFiles) {
				String[] sa = fileString.getFileName2("w", fdir);
				if (sa != null && sa[0] != null) {
					fdir = sa[0];
					fwfile = sa[1];
					blockingMessageOn("formatting as GENESIS");
					writeStringToFile(cell.GENESISwrite(), fdir + fwfile);
				}

			} else {
				System.out.println("file writing not allowed ");
			}

		}*/ else if (sarg.startsWith("save as Genesis - hierar")) {
			if (canWriteFiles) {
				File f = fileString.getFileToSave(this, fdir, "Select GENESIS file");
				if (f != null) {
					blockingMessageOn("formatting as GENESIS");
					writeStringToFile(cell.GENESISwriteHR(), f.getAbsolutePath());
				}

			} else {
				System.out.println("file writing not allowed ");
			}

		} else if (sarg.startsWith("save as hoc (Neuron) - stru")) {
			if (canWriteFiles) {
				String[] sa = fileString.getFileName2("w", fdir);
				if (sa != null && sa[0] != null) {
					fdir = sa[0];
					fwfile = sa[1];
					blockingMessageOn("formatting as HOC");
					writeStringToFile(cell.HOCwrite(), fdir + fwfile);
				}

			} else {
				System.out.println("file writing not allowed ");
			}

		} else if (sarg.startsWith("save as hoc (Neuron) - name")) {
			if (canWriteFiles) {
				File f = fileString.getFileToSave(this, fdir, "Select NEURON file");
				if (f != null) {
					blockingMessageOn("formatting as HOC (named segments)");
					writeStringToFile(cell.HOCwriteNS(), f.getAbsolutePath());
				}

			} else {
				System.out.println("file writing not allowed ");
			}


		} else if (sarg.startsWith(neuronEditorPanel.SAVE_NEUROML_v1)) {
			if (canWriteFiles) {
				File f = fileString.getFileToSave(this, fdir, "Select NeuroML file");
				if (f != null) {
					blockingMessageOn("formatting as NeuroML");
					writeStringToFile(cell.writeNeuroML_v1_8_1(), f.getAbsolutePath());
				}

			} else {
				System.out.println("file writing not allowed ");
			}
                } else if (sarg.startsWith(neuronEditorPanel.SAVE_NEUROML_v2beta)) {
			if (canWriteFiles) {
				File f = fileString.getFileToSave(this, fdir, "Select NeuroML file");
				if (f != null) {
					blockingMessageOn("formatting as NeuroML");
					writeStringToFile(cell.writeNeuroML_v2beta(), f.getAbsolutePath());
				}

			} else {
				System.out.println("file writing not allowed ");
			}

		} else if (sarg.equals("auto save as swc")) {
			if (canWriteFiles) {
				headerF.apply();
				fwfile = frfile.substring(0, frfile.lastIndexOf(".")) + ".swc";
				blockingMessageOn("formatting as SWC");
				writeStringToFile(cell.write(), fdir + fwfile);
			} else {
				System.out.println("file writing not allowed ");
			}

		} else {
			System.out.println("button ? " + sarg);
		}

	}

	public void blockingMessageOn(String s) {
        ((JLabel)messageD.getContentPane().getComponent(0)).setText(s);
        ((JLabel)messageD.getContentPane().getComponent(1)).setText("");
        messageD.pack();
		Point p = getLocationOnScreen();
		messageD.setLocation(p.x + 100, p.y + 20);
		messageD.setVisible(true);
	}

	public void blockingMessageOn(String s1, String s2) {
		if (messageD != null) {
			((JLabel)messageD.getContentPane().getComponent(0)).setText(s1);
			((JLabel)messageD.getContentPane().getComponent(1)).setText(s2);
            messageD.pack();
			Point p = getLocationOnScreen();
			messageD.setLocation(p.x + 100, p.y + 20);
            messageD.setVisible(true);
		}
	}

	public void blockingMessageOff() {
		if (messageD != null)
			messageD.dispose();
        else
            System.out.println("Blocking message dialog is null...");
	}

}

/**
 * @author  NMO Developer
 * This class is modified to display only the selected functionality of cvapp.
 * */
class ModifiedOptionBar extends JPanel implements ItemListener, ActionListener {
	//JChoice cfile;
	neuronEditorPanel neupan;
	JLabel FileNameL;

	/**
	 * This constructor initializes all the buttons required on the panel. It
	 * also adds listeners to them.
	 * */
	ModifiedOptionBar(neuronEditorPanel p) {
		neupan = p;

		setLayout(new FlowLayout());
		FileNameL = new JLabel("            null             ");

		// Check box button created and listeners are added.
		ButtonGroup bg = new ButtonGroup();
		
		JRadioButton sklProj = new JRadioButton(neuronEditorPanel.SKEL_PROJ, true); 
        bg.add(sklProj);
		sklProj.addItemListener(this);

		JRadioButton areaProj = new JRadioButton(neuronEditorPanel.AREA_PROJ, false);
        bg.add(areaProj);
		areaProj.addItemListener(this);
        
		// button that centers neuron is creates and listener is added.
		JButton centerJButton = new JButton(neuronEditorPanel.CENTER);
		centerJButton.addActionListener(this);

        /**
		// Neuron Save buttons created and Listeners are added
		JButton saveInNeuron = new JButton(neuronEditorPanel.SAVE_NEURON);
		saveInNeuron.addActionListener(this);


		JButton saveInGenesis = new JButton(neuronEditorPanel.SAVE_GENESIS);
		saveInGenesis.addActionListener(this);
        
		// Neuron Save buttons created and Listeners are added
		JButton saveInNeuroML = new JButton(neuronEditorPanel.SAVE_NEUROML);
		saveInNeuroML.addActionListener(this);
		

		// ceated buttons are added to panel.
		add(saveInNeuron);
		add(saveInGenesis);
		add(saveInNeuroML);*/
        
        JLabel saveLabel = new JLabel("Save as:");
        
        //PopupMenu options = new PopupMenu("Save as...");
        JComboBox options = new JComboBox();
        options.setLightWeightPopupEnabled(false);
        options.addActionListener(this);
        options.addItem(neuronEditorPanel.SAVE_OPTION);
        options.addItem(neuronEditorPanel.SAVE_NEURON);
        options.addItem(neuronEditorPanel.SAVE_GENESIS);
        options.addItem(neuronEditorPanel.SAVE_NEUROML_v1);
        options.addItem(neuronEditorPanel.SAVE_NEUROML_v2beta);
        
		add(saveLabel);
		add(options);
        
		add(centerJButton);
		add(sklProj);
		add(areaProj);
        
        this.repaint();

	}

	public void setFileNameLabel(String s) {
		if (s != null) {
			FileNameL.setText(s);
			FileNameL.setSize(s.length() * 8, 16);

			doLayout();
			repaint();
			// FileNameL.repaint();
		}
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if (e.getStateChange() == e.SELECTED) {
			String sarg = ((JRadioButton) (e.getItem())).getText();
			if (sarg.equalsIgnoreCase(neuronEditorPanel.SKEL_PROJ)) {
				neupan.setCellWindow();
				neupan.neucan.setView(neupan.neucan.SPROJECTION);
			} else if (sarg.equalsIgnoreCase(neuronEditorPanel.AREA_PROJ)) {
				neupan.setCellWindow();
				neupan.neucan.setView(neupan.neucan.APROJECTION);
			} else {
				neupan.processNameEvent(sarg);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
            
        //System.out.println("Handling event: "+e);
        
		String sarg = null;
        if (source instanceof JButton) 
            sarg = ((JButton) source).getLabel();
        if (source instanceof JComboBox) 
            sarg = ((JComboBox) source).getSelectedItem().toString();
        
		if (sarg.equals(neuronEditorPanel.SAVE_NEURON)) {
			neupan.processNameEvent("save as hoc (Neuron) - named segments");
		} else if (sarg.equals(neuronEditorPanel.SAVE_OPTION)) {
			// do nothing...
		} else if (sarg.equals(neuronEditorPanel.SAVE_GENESIS)) {
			neupan.processNameEvent("save as Genesis - hierarchical");
		} else if (sarg.equals(neuronEditorPanel.SAVE_NEUROML_v1)) {
			neupan.processNameEvent(neuronEditorPanel.SAVE_NEUROML_v1);
		} else if (sarg.equals(neuronEditorPanel.SAVE_NEUROML_v2beta)) {
			neupan.processNameEvent(neuronEditorPanel.SAVE_NEUROML_v2beta);
		} else if (sarg.equals("Center")) {
			neupan.neucan.find();
		} else {
            System.out.println("Unable to process "+sarg+" for event: "+e);
        }
        

	}


}

class optionBar extends sbPanel implements ItemListener, ActionListener {
	Choice cfile;
	neuronEditorPanel neupan;
	JLabel FileNameL;

	optionBar(neuronEditorPanel p) {
		neupan = p;

		setLayout(new FlowLayout());

		Choice view = new Choice();
		view.add(neuronEditorPanel.SKEL_PROJ);
		view.add(neuronEditorPanel.AREA_PROJ);
		view.add("skeleton red-green");
		view.add("area red-green");
		view.add("area solid blue");
		view.addItemListener(this);

		cfile = new Choice();

		PopupMenu pmfile = new PopupMenu();
		MenuItem[] mi = new MenuItem[10];
		mi[0] = new MenuItem("open");
		mi[1] = new MenuItem("shrinkage correction");
		mi[2] = new MenuItem("edit header");
		mi[3] = new MenuItem("save as swc");
		mi[4] = new MenuItem("save as hoc (Neuron) - structure only");
		mi[5] = new MenuItem("save as hoc (Neuron) - named segments");
		mi[6] = new MenuItem("save as Genesis - flat");
		mi[7] = new MenuItem("save as Genesis - hierarchical");
		mi[8] = new MenuItem("auto save as swc");
		mi[9] = new MenuItem("quit");

		for (int i = 0; i < mi.length; i++) {
			mi[i].addActionListener(this);
			pmfile.add(mi[i]);
		}

		add(pmfile);

		popLabel pl = new popLabel("file", pmfile);
		FileNameL = new JLabel("            null             ");

		Choice cturn = new Choice();
		cturn.add("continuous rotate");
		cturn.add("cube rotate");
		cturn.addItemListener(this);

		JButton brev = new JButton("reverse");
		brev.addActionListener(this);

		add(pl);
		add(FileNameL);
		add(cturn);
		add(view);
		add(brev);

	}

	public void setFileNameLabel(String s) {
		if (s != null) {
			FileNameL.setText(s);
			FileNameL.setSize(s.length() * 8, 16);

			doLayout();
			repaint();
			// FileNameL.repaint();
		}
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if (e.getStateChange() == e.SELECTED) {
			String sarg = (String) (e.getItem());
			processNameEvent(sarg);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof JButton) {
			String sarg = ((JButton) source).getLabel();
			if (sarg.equals("reverse")) {
				neupan.reverseVideo();
			}
		} else if (source instanceof MenuItem) {
			String sarg = ((MenuItem) source).getLabel();
			processNameEvent(sarg);
		}
	}

	private void processNameEvent(String sarg) {
		if (sarg.equals("cube rotate")) {
			neupan.neucan.setContinuousRotate(false);
		} else if (sarg.equals("continuous rotate")) {
			neupan.neucan.setContinuousRotate(true);
		} else if (sarg.equals(neuronEditorPanel.SKEL_PROJ)) {
			neupan.setCellWindow();
			neupan.neucan.setView(neupan.neucan.SPROJECTION);
		} else if (sarg.equals("skeleton red-green")) {
			neupan.setCellWindow();
			neupan.neucan.setView(neupan.neucan.SREDGREEN);
		} else if (sarg.equals("area red-green")) {
			neupan.setCellWindow();
			neupan.neucan.setView(neupan.neucan.AREDGREEN);
		} else if (sarg.equals(neuronEditorPanel.AREA_PROJ)) {
			neupan.setCellWindow();
			neupan.neucan.setView(neupan.neucan.APROJECTION);
		} else if (sarg.equals("area solid blue")) {
			neupan.setCellWindow();
			neupan.neucan.setView(neupan.neucan.SOLIDBLUE);

		} else if (sarg.equals("quit")) {
			neupan.exit();
			System.exit(0);
			neupan.processNameEvent(sarg);
		} else {
			neupan.processNameEvent(sarg);
		}
	}

}

class webCellBar extends sbPanel implements ItemListener, ActionListener,
		WindowListener {
	neuronEditorPanel neupan;
	String[] slist = null;
	TextField webAdd;
	String hostroot;
	Choice chlist;
	List list;
	Frame listFrame;

	webCellBar(neuronEditorPanel p) {
		neupan = p;
		setLayout(new FlowLayout());
		chlist = new Choice();
		list = new List();

		setList(null);
		webAdd = new TextField("http://www.neuro.soton.ac.uk/DigitisedCells/");
		JButton fetch = new JButton("fetch list");
		fetch.addActionListener(this);

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gbl);
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.ipadx = 1;
		gbc.ipady = 1;
		gbc.fill = gbc.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbl.setConstraints(webAdd, gbc);
		add(webAdd);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbl.setConstraints(fetch, gbc);
		add(fetch);
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbl.setConstraints(chlist, gbc);
		add(chlist);
		chlist.addItemListener(this);

		list.addItemListener(this);
		listFrame = new Frame();
		listFrame.setLayout(new BorderLayout());
		listFrame.add("Center", list);
		JButton b = new JButton("close");
		listFrame.add("South", b);
		b.addActionListener(this);
		listFrame.pack();
		listFrame.setSize(100, 600);
		listFrame.addWindowListener(this);

	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		listFrame.setVisible(false);
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void setList(String[] l) {
		slist = l;
		chlist.removeAll();
		list.removeAll();
		if (slist != null) {
			for (int i = 0; i < slist.length; i++) {
				chlist.add(slist[i]);
				list.add(slist[i]);
			}
		} else {
			chlist.add("list is empty");
			list.add("list is empty");
		}
		if (listFrame != null) {
			Dimension d = listFrame.getSize();
			listFrame.pack();
			listFrame.setSize(d);
			listFrame.setVisible(true);
		}
	}

	public void getIndexFromURL() {
		String[] slist = null;
		URL u1 = null;
		URL u2 = null;
		String ts = hostroot;
		ts = ts.trim();
		if (!ts.startsWith("http://"))
			ts = "http://" + ts;
		if (!ts.endsWith("/"))
			ts = ts + "/";

		try {
			u1 = new URL(ts);
		} catch (Exception e) {
			System.out.println("malformed URL " + ts);
		}

		if (u1 != null) {
			hostroot = u1.toString();
			webAdd.setText(hostroot);

			try {
				u2 = new URL(u1, "list.html");

				String[] sl = neupan.readStringArrayFromURL(u2);
				setList(sl);
			} catch (Exception e) {
				System.out.println("malformed URL " + u1 + "list.html");
			}
		}
	}

	public void setDataFromURL(String surl) {
		URL u = null;
		try {
			u = new URL(hostroot + surl);
		} catch (Exception e) {
			System.out.println("malformed URL " + hostroot + surl);
		}
		// listFrame.setVisible(false);
		String[] sdat = neupan.readStringArrayFromURL(u);

		neupan.setCell(sdat, hostroot, surl);
	}

	public void itemStateChanged(ItemEvent e) {
		String sarg = "none";
		Object source = e.getSource();
		if (e.getStateChange() == e.SELECTED) {
			if (source instanceof Choice) {
				sarg = (String) (e.getItem());
				processNameEvent(sarg);
			} else if (source instanceof List) {
				sarg = (String) (list.getSelectedItem());
				processNameEvent(sarg);
			}
		}
		System.out.println("item state change " + sarg);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof JButton) {
			String sarg = ((JButton) source).getLabel();
			if (sarg.equals("fetch list")) {
				hostroot = webAdd.getText();
				getIndexFromURL();
			} else if (sarg.equals("close")) {
				listFrame.setVisible(false);
			}
		}
	}

	private void processNameEvent(String sarg) {
		setDataFromURL(sarg);
	}
}
