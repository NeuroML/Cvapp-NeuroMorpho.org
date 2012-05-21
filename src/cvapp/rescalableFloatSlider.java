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
import java.text.*;

class rescalableFloatSlider  extends Canvas implements MouseListener,
                                                       MouseMotionListener {
 
  static int    LOG = 1;
  static int    LIN = 2;   
  static int    INT = 1;
  static int    FLOAT = 2;
  
  String        title;
  boolean       log;
  boolean       row;

  int[]         arrxl, arrxr, arryl, arryr;
  int           nparr;
  
  int           pressTime;
  int           width, height;
  int           xdown, ydown, xslider;
  long          downTime;
  Image         im;
  Color         bgC, bgCb, bgCd, barC, arrowC;
  Color[]         zoomC;
  
  double        v0, v1, vdown, linValue, v0orig, v1orig;
  double        linValueDown;
  doublePointer dPointer;
  integerPointer iPointer;
  Graphics      gdb;  
  boolean       draggingSlider = false;
  boolean       changedSize = true;
  boolean       rightButton;
  boolean       CR0 = false, CR1 = false;
  boolean       showScale = false;
  boolean       integ;
  boolean[]     havelimits = {false, false};
  double[]      limits = {0.0, 0.0};       
  Font          myFont;
  boolean       zeroable = false;
  boolean       iszero = false;
  int           wzb = 0;
  int           wpb = 0;
  boolean       active = true;
  boolean       precisionButtons = false;
  int           iprec = 3;

  Canvas        watcher;


 rescalableFloatSlider (int loglin,  
		         String name, doublePointer c, double a, double b) {
    integ = false;
    dPointer = c;
    v0 = a; v0orig = v0;
    v1 = b; v1orig = v1;

    log = (loglin == LOG);
    if (log && c.value() < Math.pow (10.0, v0)) {
       setZeroable (true);
       iszero = true;
       linValue = v0;
    } else {
       linValue = (log ?   Math.log(c.value()) / Math.log(10.0)  : c.value());
    }

    title = name;
    setFont (new Font ("8x13", Font.PLAIN, 12));
    repaint ();

    addMouseListener(this);    
    addMouseMotionListener(this);

  }


  rescalableFloatSlider () {
    integ = false;
    linValue = 1.;
    dPointer = new doublePointer(linValue);
    log = true;
    v0 = 0.; v0orig = v0;
    v1 = 3.; v1orig = v1;
    title = "fred";
    repaint ();
    addMouseListener(this);    
    addMouseMotionListener(this);

  }
  
 rescalableFloatSlider (int loglin, 
		         String name, integerPointer c, int a, int b) {  
    integ = true;
    iPointer = c;
    log = (loglin == LOG);
    linValue = (log ?   Math.log(c.value()) / Math.log(10.0)  : c.value());
    v0 = (double)a; v0orig = v0;
    v1 = (double)b; v1orig = v1;
    title = name;
    setFont (new Font ("8x13", Font.PLAIN, 12));
    repaint ();
    addMouseListener(this);    
    addMouseMotionListener(this);

  }


  public void setInactive () {
     if (dPointer != null) setDP (new doublePointer (dPointer.value()));
     if (iPointer != null) setIP (new integerPointer (iPointer.value()));
     setTitle("[   ]");
     active = false;
     repaint();
  }


  public void setLog () {
     log = true;
  }


  public void setZeroable (boolean b) {
     if (integ) {
	zeroable = (log ? b : false);
	iszero = (zeroable ? iPointer.value() <(int)( Math.pow (10.0, v0)) :  false);
	wzb = (zeroable ? 26 : 0);
	if (!zeroable) iszero = false;
	if (iszero && iPointer != null) iPointer.set (0);
     } else {
	zeroable = (log ? b : false);
	iszero = (zeroable ? dPointer.value() < Math.pow (10.0, v0) :  false);
	wzb = (zeroable ? 26 : 0);
	if (!zeroable) iszero = false;
	if (iszero && dPointer != null) dPointer.set (0.0);
     }
     changedSize = true;
  }


  public void setPrecisionButtons (boolean b) {
     precisionButtons = b;
  }

  public void setPrecision (int p) {
     iprec = p;
     precisionButtons = true;
  }




  public void setLin () {
     log = false;
  }

  public void setRange (double a, double b) {
    v0 = (double)a; v0orig = v0;
    v1 = (double)b; v1orig = v1;
  }


  public void setWatcher (Canvas w) {
     watcher = w;
     if (w != null) watcher.repaint();
  }

  public void setTitle (String s) {
     title = s;
     repaint();
  }

  public void setDP (doublePointer dp) {
     dPointer = dp;
     linValue = (log ?   Math.log(dp.value() <= 0.0 ? 1.0 : dp.value())
		        / Math.log(10.0)  : dp.value());
     integ = false;
     v0 = linValue-1; v0orig = v0;
     v1 = linValue+1; v1orig = v1;
     active = true;
     repaint();
  }


  public void setIP (integerPointer ip) {
     iPointer = ip;
     linValue = (log ?   Math.log(ip.value() <= 0 ? 1.0 : ip.value())
		        / Math.log(10.0)  : ip.value());
     integ = false;
     v0 = linValue-1; v0orig = v0;
     v1 = linValue+1; v1orig = v1;
     active = true;
     repaint();
  }


  public void unsetDP () {
     setDP (new doublePointer (dPointer.value()));
     active = false;
  }

  public void unsetIP () {
     setIP (new integerPointer (iPointer.value()));
     active = false;
  }

  public void setFont (Font f) {
    myFont = f;
  }


  public void setLimit (int lim, double value) {
    havelimits[lim] = true;
    limits[lim] = value;
  }

  public void setLimit (int lim, int value) {
    havelimits[lim] = true;
    limits[lim] = (double)value;
  }

  public void unsetLimit (int lim) {
    havelimits[lim] = false;
  }

  public Dimension getMinimumSize () {
    return new Dimension (120, 25);
  }

  public Dimension getPreferredSize () {
    return new Dimension (180, 30);
  }
  

  public String oldPrettyString (double a, int ispace) {
    String d = " ";
    if (integ || a == 0) {
       int id = (int) a;
       d = " " + id + "                      ";
       d = d.substring(1, ispace);
    } else { 
       String t = " " + a;
       int i = t.indexOf("E", 1);
       if (i < 0) i = t.indexOf("e", 1);
       if (i > 0) {
//	  int el = t.length() - i;
//          d = t.substring (1, ispace - el) + t.substring (i, el); 	  
	  t = t + "            ";
	  d = t.substring (1, ispace);
       } else {
	  t = t + "            ";
	  d = t.substring (1, ispace);
       }

    }
    return (d);
  }
  
  
  public void paint (Graphics g) {
    changedSize = false;
    Dimension d = getSize();
    if (im == null || width != d.width || height != d.height) {
      width = d.width;
      height = d.height; 
      im = createImage(width, width);
      changedSize = true;
    }
    gdb = im.getGraphics();
    realPaint(gdb);
    g.drawImage(im, 0, 0, this);


  }


  public void setArrows(int w, int h) {
    arrxl = new int[5];
    arrxr = new int[5];
    arryl = new int[5];
    arryr = new int[5];
    nparr = 4;

    arrxl[0] = 3;   arryl[0] = h/2;
    arrxl[1] = 19;  arryl[1] = 3;
    arrxl[2] = 16;  arryl[2] = h/2;
    arrxl[3] = 19;  arryl[3] = h-3;
    arrxl[4] = 3;   arryl[4] = h/2;

    for (int i = 0; i < 5; i++) {
      arrxr[i] = width-wpb - arrxl[(i+1) % 5];
      arryr[i] = height - arryl[(i+1) % 5];
    }
    
    for (int i = 0; i < 5; i++) arrxl[i] += wzb;

    zoomC = new Color[6];
    zoomC[0] = Color.red;
    for (int i = 1; i <6; i++) {
      zoomC[i] = new Color (zoomC[i-1].getRGB() + 0x00000f0f);
    }

  }
  
  public  void drawUpButton (Graphics g, Color c, 
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

  public void drawDownButton (Graphics g, Color c, 
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


  public void realPaint (Graphics g) {
    int iv, iv0, iv1, wr, hr, d, h0, h1;
    String s0, s1;

    if (bgC == null) {
      bgC = getBackground();       
      bgCb = bgC.brighter();
      bgCd = bgC.darker();
      barC =  new Color (bgC.getRGB() + 0x006f5f0f);
      arrowC =  new Color (bgC.getRGB() - 0x001f0f1f);
    }


    g.setColor (bgC);
    g.fillRect(0,0, width, height);
    WindowDressing.SunkenBorder (g, getSize(), bgC); 

    if (precisionButtons) {
       wpb = 30;
       drawUpButton (g, bgC,  width-wpb+4, 4, wpb-8, height - 8);
       g.setColor (Color.black);
       g.drawString (""+iprec, width-wpb+10, height/2+6);
    } else {
       wpb = 0;
    }
    


    if (zeroable) {
       if (iszero) {
	  drawDownButton (g, bgC, 3, 3, wzb-6, height-6);
       } else {
	  drawUpButton (g, bgC, 3, 3, wzb-6, height-6);
       }
       g.setColor (Color.black);
       g.drawString ("0", 7, height/2+6);
    } else {
       wzb = 0;
    }


    if (changedSize) {
      setArrows(width, height);
    }

    if (myFont != null) g.setFont (myFont);

    xslider = (int) (wzb + 15 + (width-wzb-wpb-30) * (linValue - v0) / (v1  - v0));

    g.setColor (arrowC);
    g.fillPolygon (arrxl, arryl, nparr);
    g.fillPolygon (arrxr, arryr, nparr);
    g.setColor (arrowC.darker());
    g.drawPolygon (arrxl, arryl, 5);
    g.drawPolygon (arrxr, arryr, 5);
    g.setColor (arrowC.brighter()); 
    g.drawPolyline (arrxl, arryl, 2);
    g.drawPolyline (arrxr, arryr, 3);

    g.setColor (bgCd);
    g.fillRect (width-wpb-9, 3, 6, 6);
    g.fillRect (width-wpb-9, height-8, 5, 5);
    g.setColor (bgCb);
    g.drawRect (width-wpb-9, height-8, 5, 5);
    g.fillRect (width-wpb-7, 5, 2, 2);

    g.setColor (bgC);
    g.fillRect (wzb+4, height-8, 5, 5);
    g.setColor (bgCd);
    g.drawRect (wzb+4, height-8, 5, 5);
    g.fillRect (wzb+6, height-6, 2, 2);

    g.setColor (bgCb);
    g.drawLine (xslider-6, height/2-4, xslider+6, height/2-4);
    g.drawLine (xslider-6, height/2-3, xslider+5, height/2-3);
    g.drawLine (xslider-6, height/2-4, xslider-6, height/2+4);
    g.drawLine (xslider-5, height/2-3, xslider-5, height/2+3);
    g.setColor (bgCd);
    g.drawLine (xslider-5, height/2+3, xslider+6, height/2+3);
    g.drawLine (xslider-6, height/2+4, xslider+6, height/2+4);
    g.drawLine (xslider+6, height/2-4, xslider+6, height/2+4);
    g.drawLine (xslider+5, height/2-3, xslider+5, height/2+3);



    if (CR0 || CR1 || showScale) {
       s0 = numForm.prettyTrim (log ? Math.pow(10.0, v0) : v0, 3, 8);
       s1 = numForm.prettyTrim (log ? Math.pow(10.0, v1) : v1, 3, 8);
    } else {
       s0 = title;
       if (integ) {
          s1 = numForm.prettyTrim (iPointer.value(), iprec, 8);
       } else {
          s1 = numForm.prettyTrim (dPointer.value(), iprec, 8);
       }
    }


    if ((width-wpb-40-wzb) > 10 * (s0.length() + s1.length())) {
      h0 = height/2 + 6; 
      h1 = height/2 + 6; 
    } else { 
      h0 = height/2 - 2;
      h1 = height/2 + 10;
      if (h0 < 10) h0 = 10;
      if (h1 > height-2) h1 = height-2;
    }
    g.setColor ((iszero || !active) ? bgCd : Color.black);
    g.drawString (s0, 22+wzb, h0);
    g.drawString (s1, width-wpb-wzb-22 - 6*s1.length(), h1);

    if (watcher != null) watcher.repaint();
  }
  


  public void impose () {
    double v = (integ ? (double)iPointer.value() : dPointer.value());
    linValue = (log ?   Math.log(iszero ? 1.0 : v) / Math.log(10.0)  : v);
    repaint ();
  }


  public void update(Graphics g) {
     paint(g); 
  }
  
  
  
  public Insets getInsets() {
     return (new Insets (1,1,1,1));
  }





  public void  mouseDragged (MouseEvent e) {
     int x = e.getX();
     int y = e.getY();
     rsfMouseDrag (x, y);
  }
  
  public void mouseMoved(MouseEvent e) {
  }
  
  public void mousePressed(MouseEvent e) {
     int x = e.getX();
     int y = e.getY();
     long when = e.getWhen();
     int modif = e.getModifiers();
     int button = 0;
     if (modif == e.BUTTON1_MASK) {
	button = 1;
     } else if (modif == e.BUTTON2_MASK) {
	button = 2;
     } else if (modif == e.BUTTON3_MASK) {
	button = 3;
     }
     rsfMouseDown (x, y, when, button);
  }
  
  public void mouseReleased(MouseEvent e) {
     int x = e.getX();
     int y = e.getY();
     rsfMouseUp (x, y);  
  }
  
  public void mouseEntered(MouseEvent e) {
  }
  
  public void mouseExited(MouseEvent e) {
  }
  
  public void mouseClicked(MouseEvent e) {
  }
  
  


  public void rsfMouseDown (int x, int y, long when, int button) {
     xdown = x;
     ydown = y;
     linValueDown = linValue;
     downTime = when;
     rightButton = (button == 3);
     
     if (zeroable && x < 20) {
	iszero = !iszero;
	setOutval();
	repaint ();  
      
     } else if (precisionButtons && x > width - wpb) {
	if (rightButton) {
	   if (iprec < 6) iprec++; else iprec = 1;
	} else {
	   if (iprec > 1) iprec--;
	}
	setOutval();
	repaint ();  
	
     } else if (Math.abs (x - xslider) < 9) {
	draggingSlider = true;
	linValueDown = linValue;
     } else if (width-wpb-x + height-y < 18) {
	zoomScale (log ? Math.sqrt(2.) : 2.0);
     } else if (width-wpb-x + y < 18) {
	zoomScale (log ? Math.sqrt(0.5) : 0.5);
     } else if (x-wzb + height-y < 18) {
        resetScale ();
     } else if (x > width-wpb - 17 && y < height - 5) {
	nudgeScale (-0.01);
     } else if (x-wzb < 17 && y < height-5) {
	nudgeScale (0.01);
     } else if (x-wzb > 20 && x < (width-wpb)/2) {
        CR0 = true;
        vdown = v0;
     } else if (x-wzb > (width-wpb)/2 && x < (width-wpb)-20) {
        CR1 = true;
        vdown = v1;
     }
  }
  
  public void nudgeScale (double f) {
     linValue -= f * (v1 - v0);
     checkRange(true, true);    
     repaint();
  }
  
  
  public void checkRange (boolean lim, boolean val) {
     if (lim) {
	if (havelimits[0] && v0 < limits[0]) v0 = limits[0];
	if (havelimits[1] && v1 > limits[1]) v1 = limits[1];
     }
     if (val) {
	if (linValue < v0) linValue = v0;
	if (linValue > v1) linValue = v1;
     }
  }
  
  
  public void zoomScale (double f) {
     v0 = linValue + f * (v0 - linValue);
     v1 = linValue + f * (v1 - linValue);
     checkRange(true, false);    
     showScale = true;
     repaint ();
  }
  
  public void resetScale () {
     v0 = v0orig;
     v1 = v1orig;
     showScale = true;
     checkRange(true, true);    
     repaint();
  }
  
  
  public void rsfMouseUp (int x, int y) {
     draggingSlider = false;
     CR0 = false;
     CR1 = false;
     showScale = false;
     checkRange (true, true);
     repaint();
  }
  

  public void setOutval () {
     if (iszero) {
	if (integ) iPointer.set(0); else dPointer.set(0.0);
     } else {
	if (integ) {
	   iPointer.set ((int) (numForm.trim (log ? Math.pow(10.0, linValue) :
				      linValue, iprec)));
	} else {
	   dPointer.set ( numForm.trim(log ? Math.pow(10.0, linValue) :
			              linValue, iprec));
	}
     }
  }
  

  public void rsfMouseDrag (int x, int y) {
     double dr, f, fff;
     f = 0.0;
     if (y > height) f = 0.01 * (height - y);
     if (y < 0) f = 0.01 * y;
     
     if (draggingSlider) {
        dr = (Math.pow (10.0, f) * (x-xdown)) / (width-wpb - 30);
        linValue = linValueDown + dr * (v1 - v0);
        checkRange (false, true);
	setOutval();
	repaint ();  
	
     } else if (CR0) {
	fff = 0.01 * (-x+xdown);
        fff = ( fff < 0 ? Math.exp(fff) : 1.0 + fff);
        v0 = v1 + fff * (vdown - v1);
        checkRange (true, false);
        repaint ();
     } else if (CR1) {
	fff = (0.01 * (x-xdown));
        fff = ( fff < 0 ? Math.exp(fff) : 1.0 + fff);
        v1 = v0 + fff * (vdown - v0);
	checkRange (true, false);
	repaint ();
     }
     
     ownScrollbarDrag ();
  }
  
  public void ownScrollbarDrag () {
  }	
  
}

  
  




