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

class neuronEditorCanvas extends graphCanvas3 implements ActionListener {
   neulucData cell;
   static int TRACE = 1;
   static int CUT = 2;
   static int JOIN = 3;
   static int REMOVENODE = 4;
   static int ADDNODE = 5;
   static int MARKSECTION = 6;
   static int HIGHLIGHTSECTION = 7;
   static int HIGHLIGHTTREE = 8;
   static int HIGHLIGHTPOINTS = 9;
   static int MERGE = 10;
   static int IDENT = 11;

  String[] gotPointActionStrings = {" ",
                                    "click the point to trace from",
				    "click ends of segment to cut",
				    "click the two points to be joined",
				    "click node(s) to be deleted",
		          "click ends of segment in which to add node", 
		          "select ends of section",
		          "select ends of section",
			  "select two points on base of tree, lower first",
			  "click desired points",
                        "click point to be removed and then its replacement",
                   "click dummy point and then the point to identify it with"};

   String[] growModeInstructions = 
      {"press mouse on a node and drag (holding button down)",
       "left button - new point",
       "middle or left + shift key - move point",
       "right button - change radius"};

   int gotPointAction;
   boolean showPoints = false;
   boolean showOutlines = false;
   static int   SREDGREEN = 1;
   static int SPROJECTION = 2;
   static int   AREDGREEN = 3;
   static int APROJECTION = 4;
   static int SOLIDBLUE = 5;
   int view = 2;
   int lastView = 2;
   static int BRANCHDRAG = 20;
   static int POINTDRAG = 21;
   int dragPoint = 0;
   double[] dragOrig;
   double[] cdp;
   int[] pmark;
   boolean gotMarks = false;
   Image imred;
   Image imgreen;
   int imrgw = 0;
   int imrgh = 0;
   Graphics gr, gg;
   int[] bufred;
   int[] bufgreen;
   Image imageRG;
   boolean revvid = true;
   boolean growMode = false;
   boolean freeAdd = false;
   double[][]     prevLines;
   double[][]     newLines;
   double[]     growpt;








  neuronEditorCanvas (int w, int h, graphData3 gd) {
     super (w, h, gd);
     setSize (w, h);
     cdp = new double[3];
     pmark = new int[2];
     
//     icm = new IndexColorModel(3, 6, icmr, icmg, icmb, icma);
  }


   
   public void actionPerformed(ActionEvent e) {
      System.out.println ("neucan action event?");
      /*
      Object source = e.getSource();
      if (source instanceof MenuItem) {
         String sarg = ((MenuItem)source).getLabel();
	 sarg = e.getActionCommand();
	 if (gotMarks) {
	    if (cell != null) {
	       cell.markPointTypes (pmark, sarg);
	       repaint();
	    }
	    gotMarks = false;
	 }
	 
      }
      */
   }



   public void reverseVideo () {
      revvid = !revvid;
      repaint();
   }

  public void setData (neulucData dat) {
     cell = dat;
     repaint();
  }


   public void setView (int v) {
      view = v;
      lastView = v;
      repaint();
   }

   
   public void find () {
      double xl, xh;
      double yl, yh;
      xl = yl = xh = yh = 0.0;
      if (lineList != null) {
	 int nl = lineList.length;
	 for (int ii = 0; ii < nl; ii++) {
	   double[] p = lineList[ii];
	   if (xl > p[0]) xl = p[0];
	   if (xh < p[0]) xh = p[0];
	   if (yl > p[1]) yl = p[1];
	   if (yh < p[1]) yh = p[1];
	   
	 }
      }
      gd.setRange (xl-20, xh+20, yl-20, yh+20);
      repaint();
   }
   


  public void myPaint (Graphics g) {
     double xs, ys, zs;

     if (cell != null) {
	lineList = cell.getLineList ();
	pointList = cell.getPointList ();
     }
     if (lineList != null) {
	gd.setScale (xScale.value(),
		     yScale.value(),
		     zScale.value());


       if (view == SPROJECTION) {
	  gd.drawLineList9 (g, lineList);
       } else if (view == SREDGREEN) {

	  Dimension d = getSize();
	  if (imred == null || imrgw != d.width || imrgh != d.height) {
	     imrgw = d.width;
	     imrgh = d.height; 
	     if (imrgw > 0 && imrgh > 0) {
		imred = createImage(imrgw, imrgh);
		imgreen = createImage(imrgw, imrgh);
	     }
	  }
	  if (imred != null && imgreen != null) {
	     gr = imred.getGraphics();
	     gg = imgreen.getGraphics();
	     gr.setColor (revvid ? Color.white : Color.black);
	     gg.setColor (revvid ? Color.white : Color.black);
	     gr.fillRect (0, 0, imrgw, imrgh);
	     gg.fillRect (0, 0, imrgw, imrgh);
	     gr.setColor (revvid ? Color.magenta : Color.red);
	     gg.setColor (revvid ? Color.cyan : Color.green);

	     gd.drawRedGreenLineList2 (gr, gg, lineList, 
				       zCursor.value());
		
	     g.setPaintMode();
	     g.drawImage (imred, 0, 0, this);
	     g.setColor (revvid ? Color.white : Color.white);
	     g.setXORMode(revvid ? Color.white : Color.black);
	     g.drawImage (imgreen, 0, 0, this);
	  }


	  
       } else if (view == AREDGREEN) {
	  Dimension d = getSize();
	  if (imred == null || imrgw != d.width || imrgh != d.height) {
	     imrgw = d.width;
	     imrgh = d.height; 
	     if (imrgw > 0 && imrgh > 0) {
		imred = createImage(imrgw, imrgh);
		imgreen = createImage(imrgw, imrgh);
	     }
	  }
	  if (imred != null && imgreen != null) {
	     gr = imred.getGraphics();
	     gg = imgreen.getGraphics();
	     gr.setColor (revvid ? Color.white : Color.black);
	     gg.setColor (revvid ? Color.white : Color.black);
	     gr.fillRect (0, 0, imrgw, imrgh);
	     gg.fillRect (0, 0, imrgw, imrgh);
	     gr.setColor (revvid ? Color.magenta : Color.red);
	     gg.setColor (revvid ? Color.cyan : Color.green);

	     gd.drawAreaRedGreenLineList92 (gr, gg, lineList, 
				        zCursor.value());


	     g.setPaintMode();
	     g.drawImage (imred, 0, 0, this);
	     g.setColor (revvid ? Color.white : Color.white);
	     g.setXORMode(revvid ? Color.white : Color.black);
	     g.drawImage (imgreen, 0, 0, this);
	  }

       } else if (view == APROJECTION) {
	  gd.drawAreaLineList9 (g, lineList, null);
       } else if (view == SOLIDBLUE) {
	  gd.drawAreaLineList9 (g, lineList, Color.blue);
       }
	
	if (showOutlines) {
	   gd.drawRadiusLineList9 (g, lineList, Color.yellow);
	}

     }

     if (( view == SPROJECTION  || view == APROJECTION) 
	 && pointList != null && showPoints) { 	 
	gd.drawPointList (g, pointList);
     }

     g.setColor (Color.cyan);
     if (gotPointAction >= 0) {
	g.drawString (gotPointActionStrings[gotPointAction], 10, 35);

     } else if (growMode) {
	for (int i = 0; i < growModeInstructions.length; i++) {
	   g.drawString (growModeInstructions[i], 10, 35 + 13 * i);

	   if (newLines != null) {
	      g.setColor (Color.cyan);
	      gd.drawBlockLineList (g, newLines);
	   }
	}
     }

  }







   public void trace () {
      pointList = cell.getPointList();
      getOnePoint ();
      gotPointAction = TRACE;
   }

   public void join () {
      pointList = cell.getPointList();
      getTwoPoints ();
      gotPointAction = JOIN;
   }

   public void merge () {
      pointList = cell.getPointList();
      getTwoPoints ();
      gotPointAction = MERGE;
   }


   public void ident () {
      pointList = cell.getPointList();
      getTwoPoints ();
      gotPointAction = IDENT;
   }


   public void highlightSection () {
      pointList = cell.getPointList();
      getTwoPoints ();
      gotPointAction = HIGHLIGHTSECTION;
   }

   public void highlightTree () {
      pointList = cell.getPointList();
      getTwoPoints ();
      gotPointAction = HIGHLIGHTTREE;
   }



   public void cut () {
      pointList = cell.getPointList();
      getTwoPoints ();
      gotPointAction = CUT;
   }

   public void grow () {
      setGrowMode();
      geomChange = true;
      repaint();
   }

   public void forceRepaint() {
      geomChange = true;
      repaint();

   }


   public void addFree () {
      setGrowMode();
      freeAdd = true;
      ignoreUp = true;
      repaint();
   }





   public void drag () {
      pointList = cell.getPointList();
      getOnePoint();
      gotPointAction = BRANCHDRAG;
   }


   public void addNode () {
      pointList = cell.getPointList();
      getTwoPoints ();
      gotPointAction = ADDNODE;
   }

   public void removeNode () {
      pointList = cell.getPointList();
      getOnePoint ();
      gotPointAction = REMOVENODE;
   }

   public void markPoint () {
      pointList = cell.getPointList();
      getOnePoint ();
      gotPointAction = HIGHLIGHTPOINTS;
   }

   public void setRemoveMode () {
      removeNode();
   }


   public void showPoints () {
      showPoints = !showPoints;
      geomChange = true;
      if (growMode) geomChange = true;
      repaint();
   }


   public void showOutlines () {
      showOutlines = !showOutlines;
      geomChange = true;
      if (growMode) geomChange = true;
      repaint();
   }



   public void reallyShowPoints () {
      showPoints = true;
      repaint();
   }


   public void showLoops () {
      pointList = cell.getPointList();
      cell.showLoops();
      showPoints = false;;
      repaint();
   }

   public void  clear () {
      gotPointAction = -1;
      if (pointList != null) {
	 echoMode = false;
	 cell.clearTrace();
	 repaint();
      }
   }


   
   public void cleanCell() {
      cell.clean();
      repaint();
   }

   public void setNormal () {
      restoreView();
//      geomChange = true;
      xorUpdate = false;
      clear();
      repaint();
   }


   
   public void restoreView () {
      view = lastView;
      growMode = false;
      echoMode = false;
   }


   public void setGrowMode() {
      dragMode = POINTDRAG;
      showOutlines = true;
      unsetPointGetting();
      gotPointAction = -1;
      echoMode = true;
      geomChange = true;
      makeEchoArray();
      growMode = true;
   }


   public void gotPoint (int[] ip) {
      
      if (gotPointAction == TRACE) {
	 cell.tracePoint (ip[0]);
	 trace();
	 repaint();

      } else if (gotPointAction == CUT) {
	 cell.separatePoints (ip[0], ip[1]);
	 repaint();

      } else if (gotPointAction == JOIN) {
	 cell.joinPoints (ip[0], ip[1]);
	 cell.tracePoint (ip[0]);
	 join();
	 repaint();


      } else if (gotPointAction == MERGE) {
	 cell.mergePoints (ip[0], ip[1]);
	 merge();
	 repaint();

      } else if (gotPointAction == IDENT) {
	 cell.identifyPoints (ip[0], ip[1]);
	 ident();
	 repaint();


      } else if (gotPointAction == MARKSECTION) {
	 pmark[0] = ip[0];
	 pmark[1] = ip[1];
	 gotMarks = true;
	 repaint();

      } else if (gotPointAction == HIGHLIGHTSECTION) {
	 pmark[0] = ip[0];
	 pmark[1] = ip[1];
	 gotMarks = true;
	 cell.highlightSection (ip[0], ip[1]);

	 repaint();

      } else if (gotPointAction == HIGHLIGHTTREE) {
	 pmark[0] = ip[0];
	 pmark[1] = ip[1];
	 gotMarks = true;
	 cell.highlightTree (ip[0], ip[1]);
	 repaint();

      } else if (gotPointAction == HIGHLIGHTPOINTS) {
	 cell.highlightPoint (ip[0]);
	 repaint();
	 markPoint();


      } else if (gotPointAction == ADDNODE) {
	 cell.addPoint (ip[0], ip[1]);
	 repaint();


      } else if (gotPointAction == REMOVENODE) {
	 cell.removePoint (ip[0]);
	 repaint();
	 removeNode();

      } else if (gotPointAction == BRANCHDRAG) {
	 dragPoint = ip[0];
	 cell.markShiftPoints (ip[0]);
	 dragMode = BRANCHDRAG;
	 wantDrag = true;
	 dragOrig = pointList[dragPoint];
	 for (int i=0; i< 3; i++) cdp[i] = 0.0;
	 cell.setShift (cdp);
	 repaint();
      }

   }


   public void myXorUpdate (Graphics g) {

      int nl = newLines.length;
      for (int k = 0; k < nl; k++) {
	 for (int i = 0; i < 4; i++) {
	    newLines[k][4+i] = growpt[i];
	 }
      }

      g.setColor (Color.green);
      if (newLines != null && newLines.length > 0) {
	 gd.drawBlockLineList (g, newLines);
      } else {
	 System.out.println ("drawing nothing...?");
      }
      prevLines = newLines;

   }



   public boolean myMouseDown (int x, int y) {
      if (growMode && downPoint >= 0) {
	 xorUpdate = true;
	 dragOrig = pointList[downPoint];
	 growpt = new double[4];
	 growpt[0] = dragOrig[0];
	 growpt[1] = dragOrig[1];
	 growpt[2] = dragOrig[2];
	 growpt[3] = dragOrig[3];
	 if (leftButton) {
	    newLines = new double[1][8];
	    for (int i = 0; i < 4; i++) {
	       newLines[0][i] = dragOrig[i];
	    }
	 } else {
	    newLines = cell.linesTo(downPoint);
	 }
	 repaint();
	 
      } else if (freeAdd) {
	 freeAdd = false;
	 
	 double[] po = new double[3];
	 po[0] = x;
	 po[1] = y;
	 po[2] = 0;
	 double[] tcdp1 = gd.pixel3ToWorld (po);
	 po[1] = y - 15;
	 double[] tcdp2 = gd.pixel3ToWorld (po);
	 
	 cell.addSegment (tcdp1, tcdp2);
	 
	 geomChange = true;
	 repaint();
      }
      return true;
   }
   

   


   public boolean myMouseUp (int x, int y) {
      freeAdd = false;
      if (growMode && downPoint >= 0 && growpt != null) {
	 if (leftButton) {
	    cell.addPoint (growpt, downPoint);
	 } else if (middleButton) {
	    cell.setPointPosition (growpt, downPoint);
	 } else if (rightButton) {
	    cell.setPointRadius(growpt, downPoint);
	 }
	 

      } else if (dragMode == BRANCHDRAG) {
	 cell.imposeShift (cdp);
	 wantDrag = false;
	 dragMode = -1;
      }
      geomChange = true;
      xorUpdate = false;
      newLines = null;
      repaint();
      return true; 
   }
   
   


  public boolean myMouseDrag (int x, int y) {
     double dx = x - xdown;
     double dy = y - ydown;

     if (growMode && downPoint >= 0) {
	if (growpt == null) {
	   System.out.println ("error - dragging null point in grow mode");
	   
	} else {
	   double[] pixOrig = gd.worldToPixel3 (dragOrig);
	   pixOrig[0] += dx;
	   pixOrig[1] += dy;
	   double tcdp[] = gd.pixel3ToWorld (pixOrig);
	   if (rightButton) {
	      growpt[3] =  
	     Math.sqrt ((dragOrig[0] - tcdp[0]) * (dragOrig[0] - tcdp[0]) + 
			(dragOrig[1] - tcdp[1]) * (dragOrig[1] - tcdp[1]) + 
			(dragOrig[2] - tcdp[2]) * (dragOrig[2] - tcdp[2]));
	      
	   } else  {
	      growpt[0] = tcdp[0];
	      growpt[1] = tcdp[1];
	      growpt[2] = tcdp[2];
	      
	   } 
	   repaint();
	}
     } else if (downPoint >= 0 && dragMode == BRANCHDRAG) {
	double[] pixOrig = gd.worldToPixel3 (dragOrig);
	pixOrig[0] += dx;
	pixOrig[1] += dy;
	double tcdp[] = gd.pixel3ToWorld (pixOrig);

	for (int i = 0; i < 3; i++) cdp[i] = tcdp[i] - dragOrig[i];
	cell.setShift (cdp);
	repaint();
     } else if (dragMode == POINTDRAG) {
	System.out.println ("point drag with no point???");

     }
     return true;
  }

}









