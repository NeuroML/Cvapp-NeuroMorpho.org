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

import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;


public class graphCanvas3 extends Canvas  implements MouseListener,
                                                   MouseMotionListener,
                                                   KeyListener {

// to be subclassed for each graph
						      
  graphData3     gd;
  int            prefWidth, prefHeight;
  int            imw, imh;
  Image          im;
  Image          imbuf;
  
  Graphics       gdb, sbgdb;
  int            xdown, ydown;
  long           pressTime;
  boolean[]        Qvisited;
  boolean        turnZoom = false;
  boolean        rightButton = false;
  boolean        middleButton = false;
  int            tzx0, tzx1, tzy0, tzy1;
  Font           defaultFont;
  boolean        leftButton = false;
  boolean        rotate3 = false;

  double[][]     lineList;
  double[][]     pointList;
  int[][]        echoArray;
  int            pointsNeeded;
  int            pointsGot;
  int[]          chosenPoints;
  boolean        echoMode = false;
  boolean        pointGetting = false;
  int            echoPoint = -1;
  int            previousEchoPoint = -1;
  int            downPoint = -1;
  boolean        ignoreUp = false;
  doublePointer  zCursor, xScale, yScale, zScale;
  boolean        controt = true;
  int            dragMode = 0;
  boolean        wantDrag = false;
  boolean        geomChange = false;
  boolean        xorUpdate = false;
  boolean        mdown = false;
  int            lstx = 0;
  int            lsty = 0;


  public graphCanvas3 (int w, int h, graphData3 dat) {
     super();
     defaultFont = getFont();
     setFont (defaultFont);
     prefWidth = w;
     prefHeight = h;
     imw = w;
     imh = h;
     gd = dat;
     Qvisited = new boolean[4];
     
     gd.setSize (w, h);
     createImage(imw, imh);
     setScales();

     addMouseListener(this);    
     addMouseMotionListener(this);
  } 



  public void setContinuousRotate(boolean b) {
     controt = b;
  }


  public void setScales () {
     xScale = new doublePointer (1.0);
     yScale = new doublePointer (1.0);
     zScale = new doublePointer (1.0);

  }

  public void setzCursor (doublePointer dp) {
     zCursor = dp;
  }
  
  public void setzScale (doublePointer dp) {
     zScale = dp;
  }
  



  public void paint (Graphics g) {
     Dimension d = getSize();
     if (im == null || imw != d.width || imh != d.height) {
	imw = d.width;
	imh = d.height; 
	if (imw > 0 && imh > 0) {
	   im = createImage(imw, imh);
	   gd.setSize (imw, imh);
	}
     }
     if (im != null) {
	gdb = im.getGraphics();
	realPaint(gdb);
	g.drawImage(im, 0, 0, this);
     }
  }
  

  
  public void realPaint (Graphics g) {

    if (gd.bgColor == null) {
      Color col = getBackground();      
      gd.setBackground (col);
    } 
    g.setFont (defaultFont);

    gd.fillBackground (g, gd.bgColor);
    myBackgroundPaint (g);
    gd.drawAxes (g, gd.bgColor);
    if (rotate3 && !controt) {
       if (imbuf != null) g.drawImage(imbuf, 0, 0, this);
       gd.drawCube (g);
    } else {
       myPaint (g);
       if (echoMode && geomChange && !mdown) makeEchoArray();

    }
  }



  public void makeEchoArray () {
     double[] p;
     int i, j;
     int nx = imw  / 3 + 1;
     int ny = imh  / 3 + 1;
     if (echoArray == null || echoArray.length < nx || 
	 echoArray[0].length < ny) echoArray = new int[nx][ny];
     for (i=0; i < nx; i++) {
	for (j = 0; j < ny; j++) echoArray[i][j] = -1;
     }
     int np = pointList.length;
     for (int ip = 0; ip < np; ip++) {
	int x0 = gd.xOfP(pointList[ip]) / 3;
	int y0 = gd.yOfP(pointList[ip]) / 3;
	if (x0 > 0 && x0 < nx-2 && y0 > 0 && y0 < ny-2) {
	   echoArray[x0-1][y0-1] = ip;
	   echoArray[x0-1][y0  ] = ip;
	   echoArray[x0-1][y0+1] = ip;
	   echoArray[x0  ][y0-1] = ip;
	   echoArray[x0  ][y0  ] = ip;
	   echoArray[x0  ][y0+1] = ip;
	   echoArray[x0+1][y0-1] = ip;
	   echoArray[x0+1][y0  ] = ip;
	   echoArray[x0+1][y0+1] = ip;
	}
     }
     geomChange = false;
     echoPoint = echoArray[lstx / 3][lsty / 3];
  }




  public void update(Graphics g) {
     if (xorUpdate) {
	g.drawImage (im, 0, 0, this); 
	myXorUpdate(g);

     } else {

	int x0, y0;
	if (geomChange || !echoMode)  paint(g);

	if (echoMode && echoPoint != previousEchoPoint) {
	   g.drawImage (im, 0, 0, this); 
	   g.setColor (Color.red);
	   if (echoPoint >= 0) {
	      x0 = gd.xOfP(pointList[echoPoint]);
	      y0 = gd.yOfP(pointList[echoPoint]);
	      g.drawOval (x0 - 4, (imh-y0-1)-4, 9, 9);
	      g.drawOval (x0 - 3, (imh-y0-1)-3, 7, 7);
	      double[] ple = pointList[echoPoint];
	      String snum = " " + 
                            mytrim(xScale.value() * ple[0]) + " " + 
			    mytrim(yScale.value() * ple[1]) + " " + 
			    mytrim(zScale.value() * ple[2]);
	      g.setColor (Color.white);
	      g.setFont (getFont());
	      g.drawString (snum, 10, 20);
	   }
	   
	} 
     }
  }
  

  private String mytrim (double d) {
     return (" " + d + "       ").substring (1, 7);
  }


  public void getOnePoint () { getNPoints(1); }     

  public void getTwoPoints () { getNPoints(2); }     


  public void getNPoints (int np) {
     pointGetting = true;
     if (pointList == null) {
	System.out.println ("need points list first - getOnePoint");
	/*
     } else if (echoMode) {
	echoMode = false;
	pointsGot = 0;
	pointsNeeded = 0;

     } else {
	echoMode = true;
	makeEchoArray ();
	pointsNeeded = np;
	pointsGot = 0;
	
     }
     */
     } else {
	pointsNeeded = np;
	pointsGot = 0;
	echoMode = true;
	geomChange = true;
	repaint();
     }
  }


  public void unsetPointGetting () {
     pointGetting = false;
     pointsNeeded = 0;
  }


  public void gotPoint (int[] cp) {
     // to be overridden
  }






   public void  mouseDragged (MouseEvent e) {
     int x = e.getX();
     int y = e.getY();
     gcMouseDrag (x, imh - y);
  }
  
  public void mouseMoved(MouseEvent e) {
     int x = e.getX();
     int y = e.getY();
     gcMouseMove (x, imh - y);
  }
  
  public void mousePressed(MouseEvent e) {

     int x = e.getX();
     int y = e.getY();
     long when = e.getWhen();
     int modif = e.getModifiers();
     int button = 1;
     /*
   System.out.println ("funny modif: " + modif + " " + e.BUTTON1_MASK + 
		    " " + e.BUTTON2_MASK + " " + e.BUTTON3_MASK + " " + 
		       e.ALT_MASK + " " + e.CTRL_MASK);
		       */


     if (modif == e.BUTTON1_MASK) {
	button = 1;
     } else if (modif == e.BUTTON2_MASK) {
	button = 2;
     } else if (modif == e.BUTTON3_MASK) {
	button = 3;
     } else if (modif == e.BUTTON3_MASK + e.CTRL_MASK || 
		modif == e.BUTTON1_MASK + e.CTRL_MASK || 
		modif == e.BUTTON3_MASK + e.SHIFT_MASK || 
		modif == e.BUTTON1_MASK + e.SHIFT_MASK) {
	button = 2;
     } 

     mdown = true;
     gcMouseDown (x, imh - y, when, button);
  }
  
  public void mouseReleased(MouseEvent e) {
     long when = e.getWhen();
     int x = e.getX();
     int y = e.getY();
     mdown = false;
     gcMouseUp (x, imh - y, when);  
  }
  
  public void mouseEntered(MouseEvent e) {
     requestFocus();
  }
  
  public void mouseExited(MouseEvent e) {
  }
  
  public void mouseClicked(MouseEvent e) {
  }

  public void keyPressed(KeyEvent e) {
  }

  public void keyReleased(KeyEvent e) {
  }

  public void keyTyped(KeyEvent e) {
     char c = e.getKeyChar();
     //     gcKeyDown(c);

  }
  


  public void gcMouseMove(int x, int y) {
     if (echoMode) {
	previousEchoPoint = echoPoint;
	echoPoint = -1;
	if (echoArray != null && x/3 < echoArray.length && 
	    echoArray[x/3] != null && y/3 < echoArray[x/3].length) {
	   echoPoint =  echoArray[x/3][y/3];
	}	
	repaint();
    }
  }



  public void gcMouseDown(int x, int y, long when, int button) {
    xdown = x;
    ydown = y;
    rightButton = (button == 3);
    middleButton = (button == 2);
    leftButton = (button == 1);

    pressTime = when;
    downPoint = echoPoint;

    boolean status = true;
    if (echoMode && echoPoint >= 0 && pointGetting) { 
       int[] ncp = new int [pointsGot+1];
       for (int i = 0; i < pointsGot; i++) ncp[i] = chosenPoints[i];
       chosenPoints = ncp;
       chosenPoints[pointsGot] = echoPoint;
       
       pointsGot++;
       if (pointsGot == pointsNeeded) {
	  echoMode = false;
	  pointsNeeded = 0;
	  pointsGot = 0;
	  pointGetting = false;
	  gotPoint(chosenPoints);
       }
    } else {
       rotate3 = false;
    }
    status = myMouseDown (x,  y);

 }



  public void gcMouseUp(int x, int y, long when) {
    boolean longClick;    
    boolean status = true;
    xorUpdate = false;
    

    if (ignoreUp || downPoint >= 0){
       status = myMouseUp (x, y);
       
    } else if (turnZoom) {
       turnZoom = false;
       gd.pixelSet();
       
    } else if (rotate3) {
       if (!controt) {
	  gd.dragRotate (xdown, ydown, x,  y, rightButton);
	  geomChange = true;
       }
       rotate3 = false;
       gd.pixelSet();
       
    } else  {
       longClick =  ((when - pressTime > 150) || rightButton);
       gd.panZoom (xdown, ydown, x, y, longClick);
       geomChange = true;
    }
    
    gd.unsetRotInit();
    dragMode = 0;
    downPoint = -1;
    echoPoint = -1;
    lstx = x;
    lsty = y;
    
    ignoreUp = false;
    repaint();
  }



  public void gcMouseDrag (int x, int y) {
    boolean status = true;

    if (downPoint >= 0) {
       status = myMouseDrag (x, y);
    } else if (rotate3) {
       gd.dragRotate (xdown, ydown, x,  y, rightButton);
       geomChange = true;
       repaint();

    } else if (turnZoom) {
      if ( (x - tzx1) * (x-tzx1) + (y-tzy1)*(y-tzy1) > 64) {
         double a = (tzx1 - tzx0) * (y - tzy0) - (tzy1 - tzy0) * (x - tzx0);
         a = a / ((tzx1-tzx0)*(tzx1-tzx0) + (tzy1-tzy0)*(tzy1-tzy0));
         double f = Math.exp (0.16 * a);         
         gd.fixZoom (xdown, ydown, f);
	 repaint ();         
         tzx0 = tzx1; tzx1 = x;
         tzy0 = tzy1; tzy1 = y;
	 geomChange = true;
      }

    } else if (leftButton && x > imw || x < 0 || y > imh || y < 0) {
      tzx0 = xdown;
      tzy0 = ydown;
      tzx1 = x;
      tzy1 = y;
      turnZoom = true;
    } else {
        if ((rightButton || middleButton) && !rotate3) {
	  imbuf = createImage(imw, imh);
	  gdb = imbuf.getGraphics();  
	  gdb.drawImage(im, 0, 0, this);
	  rotate3 = true;
       }
    }
  }



  /*
  public boolean myKeyDown (char key) {
     return true;
  }
  */

  public void myXorUpdate (Graphics g) {
  }


  public void myPaint (Graphics g) {
  }

  public void myBackgroundPaint (Graphics g) {
  }

  public boolean myMouseDown (int x, int y) {
    return true;
  }


  public boolean myMouseUp (int x, int y) {
    return true; 
  }

  public boolean myMouseDrag (int x, int y) {
    return true;
  }
}








