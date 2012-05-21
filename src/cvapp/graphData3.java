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


class graphData3 extends Object {
   int           width;
   int           height;
   int           n_tickx, n_ticky;
   boolean       xVisible, yVisible;
   double        xLeft, xRight, yBottom, yTop, dpdx, dpdy;
   int           leftMargin, rightMargin, bottomMargin, topMargin;
   boolean       xRescalable, yRescalable;
   int           tickGridx, tickGridy;  
   Color         bgColor, fgColor, gridColor, dataColor;
   boolean       xLowDef, yLowDef, xUpDef, yUpDef;
   double        xLowLim, yLowLim, xUpLim, yUpLim;
   boolean       grey = false;
   boolean       initialisedRotation = false;
   int           x3c, y3c, z3c, zcen;
   double[]      cen3;
   double[][]    m3, m3t, m3sav;
   double        xScale = 1.0;
   double        yScale = 1.0;
   double        zScale = 1.0;
   Font          axisFont;
   edgeCube      edgeC;
   int           xcen, ycen;
   double        thax0 = 0.0, r0 = 0.0;
   double        thax1 = 0.0, r1 = 0.0;
   Color[]       lineColors;


   graphData3 () {
      width        = 100;
      height       = 100;
      n_tickx      = 5;
      n_ticky      = 4;
      xLeft        = -100.0;
      xRight       = 50.0;
      yBottom      = -0.01;
      yTop         = 1.05;
      leftMargin   = 10;
      rightMargin  = 20;
      bottomMargin = 20;
      topMargin    = 20;
      xRescalable  = true;
      yRescalable  = true;
      xVisible     = true;
      yVisible     = true;
      tickGridx    = 2;
      tickGridy    = 2;    
      cen3 = new double[3];
      m3 = new double[3][3];
      m3t = new double[3][3];
      m3sav = new double[3][3];
      z3c = 0;
      zcen = 0;
      edgeC = new edgeCube();
      for (int i = 0; i < 3; i++) m3[i][i] = 1.0;
      axisFont = new Font("8x13", Font.PLAIN, 12);

      lineColors = new Color[8];
      lineColors[0] = Color.white;
      lineColors[1] = Color.red;
      lineColors[2] = Color.gray;
      lineColors[3] = Color.green;
      lineColors[4] = Color.magenta;
      lineColors[5] = Color.cyan;
      lineColors[6] = Color.pink;
      lineColors[7] = Color.blue;


      setBackground (Color.black);
      setForeground (Color.white);

      pixelSet ();

   }
   
   public void setBackground (Color c) {
      bgColor = c;
      if (bgColor.getGreen() + bgColor.getRed() + bgColor.getBlue() > 150) {
	 gridColor = new Color (bgColor.getRGB() - 0x00080820);
      } else {
	 gridColor = new Color (bgColor.getRGB() + 0x001f2f1f);
      }
   }
   
   public void setGrey () {
      grey = true;
   }


  public void setFont (Font f) {
    axisFont = f;
  }

   public void setNonGrey () {
      grey = false;
   }




   public void setForeground (Color c) {
      fgColor = c;
   }
   
   public void setDataColor (Color c) {
      dataColor = c;
   }
   
   
   public void clip (Graphics g) {
      g.clipRect (leftMargin, topMargin, width - leftMargin - rightMargin,
		  height - topMargin - bottomMargin);
   }

   
   public void setRangeConstraints(Double x0, Double y0, Double x1, Double y1){
      if (xLowDef = (x0 != null)) xLowLim = x0.doubleValue();
      if (yLowDef = (y0 != null)) yLowLim = y0.doubleValue();
      if (xUpDef =  (x1 != null)) xUpLim  = x1.doubleValue();
      if (yUpDef =  (y1 != null)) yUpLim  = y1.doubleValue();
      
      enforceRangeConstraints();
      
   }
   
   
   public void enforceRangeConstraints () {
      if (xLowDef) {
	 if (xLeft  < xLowLim) xLeft  = xLowLim;
	 if (xRight < xLowLim) xRight = xLowLim;
      }
      if (yLowDef) {
	 if (yBottom < yLowLim) yBottom = yLowLim;
	 if (yTop    < yLowLim) yTop    = yLowLim;
      }
      if (xUpDef) {
	 if (xLeft  > xUpLim) xLeft  = xUpLim;
	 if (xRight > xUpLim) xRight = xUpLim;
      }
      if (yUpDef) {
	 if (yBottom > yUpLim) yBottom = yUpLim;
	 if (yTop    > yUpLim) yTop    = yUpLim;
      }
   }
   
   
   
   
   public double[] getRange () {
      double[] range = new double[4];
      range[0] = xLeft;
      range[1] = xRight;
      range[2] = yBottom;
      range[3] = yTop;
      return (range);
   }  
   
   
   public void setSize (int w, int h) {
      width = w;
      height = h;
      pixelSet();
   }
   
   public void setMargins (int l, int r, int b, int t) {
      leftMargin = l;
      rightMargin = r;
      topMargin = t;
      bottomMargin = b;
      pixelSet();
   }
   

   public void setCenter (int x, int y) {
      x3c = x;
      y3c = y;
   }

   
  public void setRescalable (boolean x, boolean y) {
     xRescalable = x;
     yRescalable = y;
  }
   
  public void setVisible (boolean x, boolean y) {
    xVisible = x;
    yVisible = y;
  }

  public void setTicks (int nx, int ny) {
    n_tickx = nx;
    n_ticky = ny;
  }

  



  public void fillBackground (Graphics g, Color bgColor) {

    g.setColor(bgColor);
    g.fillRect(0,0, width, height);
  }




  public void drawAxes (Graphics g, Color bgColor) {
    int xx, yy, powten, ii, off;
    double dx, dy, log;
    String lab;
    int[] intervals = {1, 2, 5};
 
    g.setColor (bgColor.brighter());
    g.drawRect (0, 0, width-1, height-1);
    g.drawRect (1, 1, width-3, height-3);
    g.setColor (bgColor.darker());
    g.drawRect (0, 0, width-2, height-2);

    //    g.clipRect ( 5, 0,  width-11, height);
    //    g.clipRect ( 5, 0,  width, height);

    Color axisColor = (grey ? bgColor.brighter() : fgColor);
    g.setColor (axisColor); 
    
    g.setFont(axisFont);

    if (xVisible) {
      dx = 1.5 / n_tickx * Math.abs (xRight - xLeft);
      log = Math.log (dx) / Math.log(10.); 
      powten = (int) Math.floor(log);
      ii = intervals[ (int) (2.999 * (log - powten))];
      dx = Math.pow(10.0, (double) powten) * ii;
      if (xRight < xLeft) dx *= -1;
      g.setColor (axisColor);      
      for (int i = (int) (0.5 + xLeft / dx); i <= (int) (xRight / dx); i++) 
	{  
	  lab = String.valueOf(i * dx);
	  off = lab.length();
	  
	  off = 3 - 4 * off;
	  if (i*dx < 0.0) off -= 4;
          
	  
	  xx = leftMargin + (int) (0.5 + dpdx * (i*dx - xLeft));
	  g.drawString(lab, xx+off, height-bottomMargin + 15);
	  g.drawLine (xx, height - bottomMargin, xx, height - bottomMargin +4);
	  
	}
      // add some vertical grid lines 
      dx = dx  / tickGridx;
      g.setColor(gridColor);
      for (int i = (int) (xLeft / dx); i <= (int) (xRight / dx); i++) 
	{  
	  xx = leftMargin + (int) (0.5 + dpdx * (i*dx - xLeft));
	  g.drawLine (xx, topMargin, xx, height - bottomMargin);
	}
    }



    if (yVisible) {
      dy = 1.5 / n_ticky * Math.abs (yTop - yBottom);
      log = Math.log (dy) / Math.log(10.); 
      powten = (int) Math.floor(log);
      ii = intervals[ (int) (2.999 * (log - powten))];
      dy = Math.pow(10.0, (double) powten) * ii;
      g.setColor (axisColor);      
      if (yTop < yBottom) dy *= -1;
      for (int i = (int) (0.5 + yBottom / dy); i <= (int) (yTop / dy); i++) 
	{  
	  lab = String.valueOf(i * dy);
	  off = -4 * lab.length();
	  
	  xx = leftMargin + off;
          yy = height - bottomMargin -  (int) (0.5 + dpdy * (i*dy - yBottom));
	  g.drawString(lab, xx+off, yy+4);
	  g.drawLine (leftMargin - 4, yy, leftMargin, yy);
	  
	}

      // add some vertical grid lines 
      dy = dy  / tickGridy;
      g.setColor(gridColor);
      for (int i = (int) (yBottom / dy); i <= (int) (yTop / dy); i++) 
	{  
	  yy = bottomMargin + (int) (0.5 + dpdy * (i*dy - yBottom));
	  g.drawLine (leftMargin, height-yy, width-rightMargin, height-yy);
	}

    }
    g.setColor (fgColor);      

  } 


  public void drawText (Graphics g, double x, double y, String s) {
    int ix, iy;
    ix = leftMargin + (int) (dpdx * (x - xLeft));
    iy = height - (bottomMargin + (int) (dpdy * (y - yBottom)));

    iy += 4;
    ix -= 6 * s.length();
    g.drawString (s, ix, iy); 

  }

  public void drawCenteredText (Graphics g, double x, double y, String s) {
    int ix, iy;
    ix = leftMargin + (int) (dpdx * (x - xLeft));
    iy = height - (bottomMargin + (int) (dpdy * (y - yBottom)));

    iy += 4;
    ix -= 3 * s.length();
    g.drawString (s, ix, iy); 

  }




  public void drawOffsetText (Graphics g, double x, double y, 
			      int offx, int offy, String s) {
    int ix, iy;
    ix = leftMargin + (int) (dpdx * (x - xLeft));
    iy = height - (bottomMargin + (int) (dpdy * (y - yBottom)));

    iy += 4;
    ix -= 6 * s.length();
    g.drawString (s, ix + offx, iy+offy); 

  }


   public int xToPix (double xr) {
      return  (int) (leftMargin + (int) (dpdx * (xr - xLeft)));
   }

   public int yToPix (double yr) {
      return  (int) (height - (bottomMargin + (int) (dpdy * (yr - yBottom))));
   }

  
  public double[] toWorld (int x, int y) {
    double rxy[] = {xLeft + ((x - leftMargin) / dpdx), 
		    yBottom + (height - y - bottomMargin) / dpdy};
    return (rxy);   
  } 




  public void drawData (Graphics g, double[] x, double[] y, int np) {
    int[] ix = new int[np];
    int[] iy = new int[np];
    
    for (int i = 0; i < np; i++){
      
      ix[i] = leftMargin + (int) (dpdx * (x[i] - xLeft));
      ix[i] = (ix[i] < 0      ? 0     : ix[i]); 
      ix[i] = (ix[i] > width  ? width : ix[i]); 
      
      iy[i] = height - (bottomMargin + (int) (dpdy * (y[i] - yBottom)));
      iy[i] = (iy[i] < 0       ? 0        : iy[i]); 
      iy[i] = (iy[i] > height  ? height   : iy[i]); 
      
    }
    g.drawPolygon (ix, iy, np);
  }




  public void fillPolygon (Graphics g, double[] x, double[] y, int np) {
    int[] ix = new int[np];
    int[] iy = new int[np];
    
    for (int i = 0; i < np; i++){
      
      ix[i] = leftMargin + (int) (dpdx * (x[i] - xLeft));
      ix[i] = (ix[i] < 0      ? 0     : ix[i]); 
      ix[i] = (ix[i] > width  ? width : ix[i]); 
      
      iy[i] = height - (bottomMargin + (int) (dpdy * (y[i] - yBottom)));
      iy[i] = (iy[i] < 0       ? 0        : iy[i]); 
      iy[i] = (iy[i] > height  ? height   : iy[i]); 
      
    }
    g.fillPolygon (ix, iy, np);
  }






  public void drawData (Graphics g, float[] x, float[] y, int np) {
    int[] ix = new int[np];
    int[] iy = new int[np];
    
    for (int i = 0; i < np; i++){
      
      ix[i] = leftMargin + (int) (dpdx * (x[i] - xLeft));
      ix[i] = (ix[i] < 0      ? 0     : ix[i]); 
      ix[i] = (ix[i] > width  ? width : ix[i]); 
      
      iy[i] = height - (bottomMargin + (int) (dpdy * (y[i] - yBottom)));
      iy[i] = (iy[i] < 0       ? 0        : iy[i]); 
      iy[i] = (iy[i] > height  ? height   : iy[i]); 
      
    }
    g.drawPolygon (ix, iy, np);
  }



  public void drawData (Graphics g, float[] x, float[] y, int[] rn) {
    int np = rn[1]-rn[0];
    int[] ix = new int[np];
    int[] iy = new int[np];
    
    for (int i = 0; i < np; i++){
      
      ix[i] = leftMargin + (int) (dpdx * (x[rn[0]+i] - xLeft));
      ix[i] = (ix[i] < 0      ? 0     : ix[i]); 
      ix[i] = (ix[i] > width  ? width : ix[i]); 
      
      iy[i] = height - (bottomMargin + (int) (dpdy * (y[rn[0]+i] - yBottom)));
      iy[i] = (iy[i] < 0       ? 0        : iy[i]); 
      iy[i] = (iy[i] > height  ? height   : iy[i]); 
      
    }
    g.drawPolygon (ix, iy, np);
  }




  public void fillRect (Graphics g, double x0,double y0,double x1,double y1) {
     int ix, iy, w, h;
     ix = leftMargin + (int) (dpdx * (x0 - xLeft));
     iy = topMargin + (int) (dpdy * (yTop - y1));
     w = (int) ( dpdx * (x1 - x0)) + 1; 
     h = (int) ( dpdy * (y1 - y0)) + 1; 
		
     g.fillRect (ix, iy, w, h);
  }



  public void fill3DRect (Graphics g, double x0,double y0,
			  double x1,double y1, int idepth) {
     int ix, iy, w, h;
     ix = leftMargin + (int) (dpdx * (x0 - xLeft));
     iy = topMargin + (int) (dpdy * (yTop - y1));
     w = (int) ( dpdx * (x1 - x0)); 
     h = (int) ( dpdy * (y1 - y0)); 
		
     g.fillRect (ix, iy, w, h);
     if (h > idepth) {
	g.setColor (g.getColor().darker());
	for (int i = 0; i < idepth; i++) {
	   g.drawLine (ix+w+i, iy+i, ix+w+i, iy+h-1);
	}
     }

  }



  public void fillCenteredOval (Graphics g, double xc, double yc,
				int w, int h) {
     int ix, iy;
     ix = leftMargin + (int) (dpdx * (xc - xLeft));
     iy = topMargin + (int) (dpdy * (yTop - yc));
     g.fillOval (ix-w/2, iy-h/2, w, h);
  }


  public void fillCenteredRect (Graphics g, double xc, double yc,
				int w, int h) {
     int ix, iy;
     ix = leftMargin + (int) (dpdx * (xc - xLeft));
     iy = topMargin + (int) (dpdy * (yTop - yc));
     g.fillRect (ix-w/2, iy-h/2, w, h);
  }


  public void fillOffsetCenteredOval (Graphics g, double xc, double yc,
				int ox, int oy, int w, int h) {
     int ix, iy;
     ix = leftMargin + (int) (dpdx * (xc - xLeft));
     iy = topMargin + (int) (dpdy * (yTop - yc));
     g.fillOval (ix-w/2 + ox, iy-h/2 + oy, w, h);
  }



  public void drawCenteredOval (Graphics g, double xc, double yc,
				int w, int h) {
     int ix, iy;
     ix = leftMargin + (int) (dpdx * (xc - xLeft));
     iy = topMargin + (int) (dpdy * (yTop - yc));
     g.drawOval (ix-w/2, iy-h/2, w, h);
  }

  public void drawCenteredRect (Graphics g, double xc, double yc,
				int w, int h) {
     int ix, iy;
     ix = leftMargin + (int) (dpdx * (xc - xLeft));
     iy = topMargin + (int) (dpdy * (yTop - yc));
     g.drawRect (ix-w/2, iy-h/2, w, h);
  }




  public void drawThickData (Graphics g, double[] x, double[] y, int
                             np, int thickness) {
    int[] ix = new int[np];
    int[] iy = new int[np];
    int[] ixt = new int[np];
    int[] iyt = new int[np];

    
    for (int i = 0; i < np; i++){
      
      ix[i] = leftMargin + (int) (dpdx * (x[i] - xLeft));
      ix[i] = (ix[i] < 0      ? 0     : ix[i]); 
      ix[i] = (ix[i] > width  ? width : ix[i]); 
      
      iy[i] = height - (bottomMargin + (int) (dpdy * (y[i] - yBottom)));
      iy[i] = (iy[i] < 0       ? 0        : iy[i]); 
      iy[i] = (iy[i] > height  ? height   : iy[i]); 
      
    }

    g.drawPolygon (ix, iy, np);

    for (int ith = 1; ith < thickness; ith++) {
      for (int i = 0; i < np; i++){
	ixt[i] = ix[i] + ith;
	iyt[i] = iy[i] + ith;
      }
      g.drawPolygon (ix,  iyt, np);
      g.drawPolygon (ixt, iy,  np);
    }
  }






  public void drawLine (Graphics g, double x0, double y0, 
                                    double x1, double y1) {
    int   px0, py0, px1, py1;
      
    px0 = leftMargin + (int) (dpdx * (x0 - xLeft));
    px0 = (px0 < 0      ? 0     : px0); 
    px0 = (px0 > width  ? width : px0); 
    
    px1 = leftMargin + (int) (dpdx * (x1 - xLeft));
    px1 = (px1 < 0      ? 0     : px1); 
    px1 = (px1 > width  ? width : px1); 

      
    py0 = height - (bottomMargin + (int) (dpdy * (y0 - yBottom)));
    py0 = (py0 < 0       ? 0        : py0); 
    py0 = (py0 > height  ? height   : py0); 

    py1 = height - (bottomMargin + (int) (dpdy * (y1 - yBottom)));
    py1 = (py1 < 0       ? 0        : py1); 
    py1 = (py1 > height  ? height   : py1); 
      
    g.drawLine (px0, py0, px1, py1);  
  }
    
  public void drawLine (Graphics g, int x0,  int y0, 
                                    int x1, int y1) {
     drawLine (g, (double) x0, (double) y0, (double) x1, (double) y1);
  }



   public final int xOfP (double[] p) {
      int x = x3c + (int) ( m3[0][0] * (xScale * p[0] - cen3[0])  +  
		            m3[0][1] * (yScale * p[1] - cen3[1])  +  
                            m3[0][2] * (zScale * p[2] - cen3[2]) );  
      return x;
   }

   public final int yOfP (double[] p) {
      int y = y3c + (int) ( m3[1][0] * (xScale * p[0] - cen3[0])  +  
	                    m3[1][1] * (yScale * p[1] - cen3[1])  +  
                            m3[1][2] * (zScale * p[2] - cen3[2]) );  
      return y;
   }

   public final int zOfP (double[] p) {
      int z = z3c + (int) ( m3[2][0] * (xScale * p[0] - cen3[0])  +  
	                    m3[2][1] * (yScale * p[1] - cen3[1])  +  
                            m3[2][2] * (zScale * p[2] - cen3[2]) );  
      return z;
   }


   public void drawLineList (Graphics g, double[][][] linelist) {
      int ii, i, j, k, nl;
      double c0, c1;
      double[] p0, p1;
      
      nl = 12; //linelist.size;
      for (ii = 0; ii < nl; ii++) {
	 p0 = linelist[ii][0];
	 p1 = linelist[ii][1];
	 
	 // get pixels from 3d transform....
	 int x0 = xOfP(p0);
         int y0 = yOfP(p0);
	 int x1 = xOfP(p1);
         int y1 = yOfP(p1);

	 // only draw line if it intersects visible region
	 // ie, if either end is inside, or its intersection with top
	 // or bottom axis is within region
	 boolean isvis = ((x0 > 0 && x0 < width && y0 > 0 && y0 < height) ||
			  (x1 > 0 && x1 < width && y1 > 0 && y1 < height));
	 if (!isvis) {
	    c0 = y0 - (x0 *  (y1 - y0)) / ((double) (x1 - x0));
	    c1 = y0 + ((width - x0) *  (y1 - y0)) / ((double) (x1 - x0));
	    isvis = ((c0 > 0 && c0 < height) || (c1 > 0 && c1 < height));
	 }
	 
	 isvis = true; //=====================

	 if (isvis) g.drawLine (x0, height - y0, x1, height - y1);
      }
      
   }




   public void drawCube (Graphics g) {
      double dy = (yTop - yBottom) / 2.;
      if (dy < 0.0) dy = -1 * dy;
      edgeC.offsetScaledDraw (g, this, dy, cen3); 
   }




   public void drawLineList9 (Graphics g, double[][] lines) { 
      int ii, i, j, k, nl;
      double c0, c1;
      int ncolx = lineColors.length;
      double[] p0, p1;
      p0 = new double[3];
      p1 = new double[3];

      nl = lines.length;
      for (ii = 0; ii < nl; ii++) {
	 p0[0] = lines[ii][0];
	 p0[1] = lines[ii][1];
	 p0[2] = lines[ii][2] ;

	 p1[0] = lines[ii][4];
	 p1[1] = lines[ii][5];
	 p1[2] = lines[ii][6];
	 

	 if (lines[ii][8] >= 0.0) {
	    g.setColor (Color.yellow);

	 } else if (lines[ii][9] > 1.0) {
	    int icol = (int)(lines[ii][9]) ;
	    if (icol >= ncolx) icol = ncolx-1;
	    g.setColor (lineColors[icol]);
	 } else { 
	    g.setColor (fgColor);
	 }



	 // get pixels from 3d transform....
	 int x0 = xOfP(p0);
         int y0 = yOfP(p0);
	 int x1 = xOfP(p1);
         int y1 = yOfP(p1);

	 // only draw line if it intersects visible region
	 // ie, if either end is inside, or its intersection with top
	 // or bottom axis is within region
	 boolean isvis = ((x0 > 0 && x0 < width && y0 > 0 && y0 < height) ||
			  (x1 > 0 && x1 < width && y1 > 0 && y1 < height));
//	 if (!isvis) {
//	    c0 = y0 - (x0 *  (y1 - y0)) / ((double) (x1 - x0));
//	    c1 = y0 + ((width - x0) *  (y1 - y0)) / ((double) (x1 - x0));
//	    isvis = ((c0 > 0 && c0 < height) || (c1 > 0 && c1 < height));
//	 }
	 
//	 isvis = true; //=====================
	 if (isvis) g.drawLine (x0, height - y0, x1, height - y1);
      }
      
   }


   public void drawAreaLineList9 (Graphics g, double[][] lines, 
				  Color ccc) {
      int ii, i, j, k, nl;
      double c0, c1;
      double[] p0, p1;
      int[] xpol = new int[4];
      int[] ypol = new int[4];
      p0 = new double[3];
      p1 = new double[3];
      double dpix = (xRight - xLeft) / (width - rightMargin - leftMargin);

      if (ccc != null) g.setColor (ccc);
      nl = lines.length;
      int ncolx = lineColors.length;
      for (ii = 0; ii < nl; ii++) {
	 p0[0] = lines[ii][0];
	 p0[1] = lines[ii][1];
	 p0[2] = lines[ii][2] ;

	 p1[0] = lines[ii][4];
	 p1[1] = lines[ii][5];
	 p1[2] = lines[ii][6];
	 


	 if (ccc == null) {
	    if (lines[ii][8] >= 0.0) {
	       g.setColor (Color.yellow);
	       
	    } else if (lines[ii][9] > 1.0) {
	       int icol = (int)(lines[ii][9]) ;
	       if (icol >= ncolx) icol = ncolx-1;
	       g.setColor (lineColors[icol]);
	    } else { 
	       g.setColor (fgColor);
	    }
	 }

	 int x0 = xOfP(p0);
         int y0 = yOfP(p0);
	 int x1 = xOfP(p1);
         int y1 = yOfP(p1);

	 boolean isvis = ((x0 > 0 && x0 < width && y0 > 0 && y0 < height) ||
			  (x1 > 0 && x1 < width && y1 > 0 && y1 < height));

	 double r0 = lines[ii][3];
	 double r1 = lines[ii][7];
	 double r = 0.5 * (lines[ii][3] + lines[ii][7]);
	 if (isvis) {	
	    if (r < 0.75 * dpix) {
	       g.drawLine (x0, height - y0, x1, height - y1);
	    } else {

	       int npw = (int)(r/dpix + 0.5);
	       double dl = Math.sqrt ((y1-y0)*(y1-y0) + (x1-x0)*(x1-x0));
	       double vnx0 =  (-r0/dpix * (y1 - y0) / dl);
	       double vny0 =  (r0/dpix * (x1 - x0) / dl);
	       
	       double vnx1 =  (-r1/dpix * (y1 - y0) / dl);
	       double vny1 =  (r1/dpix * (x1 - x0) / dl);


	       /*	       
	       int npw = (int)(r/dpix + 0.5);
	       double dl = Math.sqrt ((y1-y0)*(y1-y0) + (x1-x0)*(x1-x0));
	       double vnx = -r/dpix * (y1 - y0) / dl;
	       double vny =  r/dpix * (x1 - x0) / dl;
	       
	       xpol[0] = (int)(x0 - vnx);
	       xpol[1] = (int)(x0 + vnx);
	       xpol[2] = (int)(x1 + vnx);
	       xpol[3] = (int)(x1 - vnx);
	       
	       ypol[0] = height - (int)(y0 - vny);
	       ypol[1] = height - (int)(y0 + vny);
	       ypol[2] = height - (int)(y1 + vny);
	       ypol[3] = height - (int)(y1 - vny);
	       */


	       xpol[0] = (int)(x0 - vnx0);
	       xpol[1] = (int)(x0 + vnx0);
	       xpol[2] = (int)(x1 + vnx1);
	       xpol[3] = (int)(x1 - vnx1);
	       
	       ypol[0] = height - (int)(y0 - vny0);
	       ypol[1] = height - (int)(y0 + vny0);
	       ypol[2] = height - (int)(y1 + vny1);
	       ypol[3] = height - (int)(y1 - vny1);
	       

	       
	       g.fillPolygon (xpol, ypol, 4);
	    }
	 }
      }
   }





   public void drawRadiusLineList9 (Graphics g, double[][] lines, 
				    Color cca) {
      int ii, i, j, k, nl;
      double c0, c1;
      double[] p0, p1;
      int[] xpol = new int[4];
      int[] ypol = new int[4];
      p0 = new double[3];
      p1 = new double[3];
      double dpix = (xRight - xLeft) / (width - rightMargin - leftMargin);

      nl = lines.length;
      for (ii = 0; ii < nl; ii++) {
	 p0[0] = lines[ii][0];
	 p0[1] = lines[ii][1];
	 p0[2] = lines[ii][2] ;

	 p1[0] = lines[ii][4];
	 p1[1] = lines[ii][5];
	 p1[2] = lines[ii][6];
	 

	 if (cca == null) {
	    if ( lines[ii][8] >= 0.0) {
	       g.setColor (Color.yellow);
	       
	    } else if (lines[ii][9] > 0.0) {
	       if ((int)(lines[ii][9]) == 1) {
		  g.setColor (Color.magenta);
	       } else if ((int)(lines[ii][9]) == 2) {
		  g.setColor (Color.cyan);
	       }
	    } else { 
	       g.setColor (fgColor);
	    }
	 } else {
	    g.setColor (cca);
	 }

	 int x0 = xOfP(p0);
         int y0 = yOfP(p0);
	 int x1 = xOfP(p1);
         int y1 = yOfP(p1);

	 boolean isvis = ((x0 > 0 && x0 < width && y0 > 0 && y0 < height) ||
			  (x1 > 0 && x1 < width && y1 > 0 && y1 < height));

	 double r0 = lines[ii][3];
	 double r1 = lines[ii][7];
	 double r = 0.5 * (r0 + r1);
	 if (isvis) {
	    int dy2 = (y1 - y0) / 6;
	    int dx2 = (x1 - x0) / 6;
	    
	    //	    g.drawLine (x0, height - y0, x1, height - y1);

	    g.drawLine (x0, height - y0, x0+dx2, height - (y0 + dy2));
	    g.drawLine (x1, height - y1, x1-dx2, height - (y1 - dy2));

	    
	    int npw = (int)(r/dpix + 0.5);
	    double dl = Math.sqrt ((y1-y0)*(y1-y0) + (x1-x0)*(x1-x0));
	    int vnx0 = (int) (-r0/dpix * (y1 - y0) / dl);
	    int vny0 = (int) (r0/dpix * (x1 - x0) / dl);
	    
	    int vnx1 = (int) (-r1/dpix * (y1 - y0) / dl);
	    int vny1 = (int) (r1/dpix * (x1 - x0) / dl);

	    g.drawLine (x0-vnx0, height - y0+vny0, 
			x0+vnx0, height - y0-vny0);
	    
	    g.drawLine (x1-vnx1, height - y1+vny1, 
			x1+vnx1, height - y1-vny1);
	    

	    g.drawLine (x0-vnx0, height - y0+vny0, x1-vnx1, height - y1+vny1);

	    g.drawLine (x0+vnx0, height - y0-vny0, x1+vnx1, height - y1-vny1);
	    

	 }
      }
   }







   public void drawBlockLineList (Graphics g, double[][] lines) {
      int ii, i, j, k, nl;
      double c0, c1;
      double[] p0, p1;
      int[] xpol = new int[4];
      int[] ypol = new int[4];
      p0 = new double[3];
      p1 = new double[3];
      double dpix = (xRight - xLeft) / (width - rightMargin - leftMargin);

      nl = lines.length;
      for (ii = 0; ii < nl; ii++) {
	 p0[0] = lines[ii][0];
	 p0[1] = lines[ii][1];
	 p0[2] = lines[ii][2] ;

	 p1[0] = lines[ii][4];
	 p1[1] = lines[ii][5];
	 p1[2] = lines[ii][6];

	 int x0 = xOfP(p0);
         int y0 = yOfP(p0);
	 int x1 = xOfP(p1);
         int y1 = yOfP(p1);

	 double r0 = lines[ii][3];
	 double r1 = lines[ii][7];
	 double r = 0.5 * (r0 + r1);
	 g.drawLine (x0, height - y0, x1, height - y1);
	 
	 int npw = (int)(r/dpix + 0.5);
	 double dl = Math.sqrt ((y1-y0)*(y1-y0) + (x1-x0)*(x1-x0));
	 int vnx0 = (int) (-r0/dpix * (y1 - y0) / dl);
	 int vny0 = (int) (r0/dpix * (x1 - x0) / dl);
	 
	 int vnx1 = (int) (-r1/dpix * (y1 - y0) / dl);
	 int vny1 = (int) (r1/dpix * (x1 - x0) / dl);
	 
	 g.drawLine (x0-vnx0, height - y0+vny0, 
		     x0+vnx0, height - y0-vny0);
	 
	 g.drawLine (x1-vnx1, height - y1+vny1, 
		     x1+vnx1, height - y1-vny1);
	 
	 
	 g.drawLine (x0-vnx0, height - y0+vny0, 
		     x1-vnx1, height - y1+vny1);
	 
	 
	 g.drawLine (x0+vnx0, height - y0-vny0, 
		     x1+vnx1, height - y1-vny1);
	 

	 int rx = (int)(r0 / dpix);
	 int ry = rx;
	 g.drawOval (x0-rx, height - y0-ry, 2 * rx, 2 * ry);

	 rx = (int)(r1 / dpix);
	 ry = rx;
	 g.drawOval (x1-rx, height - y1-ry, 2 * rx, 2 * ry);

      }
   }



















  public void setScale (double xs, double ys, double zs) {
     xScale = xs;
     yScale = ys;
     zScale = zs;
  }


   public void drawRedGreenLineList9 (Graphics g, double[][] lines, 
				      double zrg) {
      int ii, i, j, k, nl, x0, y0, z0, x1, y1, z1, dz0, dz1;
      double c0, c1;
      double[] p0, p1;
      p0 = new double[3];
      p1 = new double[3];

      nl = lines.length;
      for (ii = 0; ii < nl; ii++) {
	 p0[0] = lines[ii][0];
	 p0[1] = lines[ii][1];
	 p0[2] = lines[ii][2] ;

	 p1[0] = lines[ii][4];
	 p1[1] = lines[ii][5];
	 p1[2] = lines[ii][6];
	 
	 g.setColor (lines[ii][8] >= 0.0 ? Color.yellow : fgColor);
	 
	 // get pixels from 3d transform....
	 x0 = xOfP(p0);
         y0 = yOfP(p0);
         z0 = zOfP(p0);

	 x1 = xOfP(p1);
         y1 = yOfP(p1);
         z1 = zOfP(p1);

	 dz0 = (int) (zrg * z0);  
	 dz1 = (int) (zrg * z1);  

	 boolean isvis = ((x0 > 0 && x0 < width && y0 > 0 && y0 < height) ||
			  (x1 > 0 && x1 < width && y1 > 0 && y1 < height));
	 if (isvis) {
	    g.setColor (Color.red);
	    g.drawLine (x0+dz0, height - y0, x1+dz1, height - y1);
	    g.setColor (Color.green);
	    g.drawLine (x0, height - y0, x1, height - y1);
	 }
      }
      
   }




   public void drawRedGreenLineList2 (Graphics g1,
				      Graphics g2, 
				      double[][] lines, 
				      double zrg) {
      int ii, i, j, k, nl, x0, y0, z0, x1, y1, z1, dz0, dz1;
      double c0, c1;
      double[] p0, p1;
      p0 = new double[3];
      p1 = new double[3];

      nl = lines.length;
      for (ii = 0; ii < nl; ii++) {
	 p0[0] = lines[ii][0];
	 p0[1] = lines[ii][1];
	 p0[2] = lines[ii][2] ;

	 p1[0] = lines[ii][4];
	 p1[1] = lines[ii][5];
	 p1[2] = lines[ii][6];
	 
	 // get pixels from 3d transform....
	 x0 = xOfP(p0);
         y0 = yOfP(p0);
         z0 = zOfP(p0);

	 x1 = xOfP(p1);
         y1 = yOfP(p1);
         z1 = zOfP(p1);

	 dz0 = (int) (zrg * z0);  
	 dz1 = (int) (zrg * z1);  

	 boolean isvis = ((x0 > 0 && x0 < width && y0 > 0 && y0 < height) ||
			  (x1 > 0 && x1 < width && y1 > 0 && y1 < height));
	 if (isvis) {
	    g1.drawLine (x0+dz0, height - y0, x1+dz1, height - y1);
	    g2.drawLine (x0, height - y0, x1, height - y1);
	 }
      }
      
   }




   public void drawAreaRedGreenLineList92 (Graphics g1,
					   Graphics g2, double[][] lines, 
					    double zrg) {
      int ii, i, j, k, nl, x0, y0, z0, x1, y1, z1, dz0, dz1;
      double c0, c1;
      double[] p0, p1;
      p0 = new double[3];
      p1 = new double[3];
      int[] xpol  = new int[4];
      int[] ypol  = new int[4];
      double dpix = (xRight - xLeft) / (width - rightMargin - leftMargin);

      nl = lines.length;
      for (ii = 0; ii < nl; ii++) {
	 p0[0] = lines[ii][0];
	 p0[1] = lines[ii][1];
	 p0[2] = lines[ii][2] ;

	 p1[0] = lines[ii][4];
	 p1[1] = lines[ii][5];
	 p1[2] = lines[ii][6];
	 
	 // get pixels from 3d transform....
	 x0 = xOfP(p0);
         y0 = yOfP(p0);
         z0 = zOfP(p0);

	 x1 = xOfP(p1);
         y1 = yOfP(p1);
         z1 = zOfP(p1);

	 dz0 = (int) (zrg * z0);  
	 dz1 = (int) (zrg * z1);  

	 boolean isvis = ((x0 > 0 && x0 < width && y0 > 0 && y0 < height) ||
			  (x1 > 0 && x1 < width && y1 > 0 && y1 < height));
	 if (isvis) {

	    double r = 0.5 * (lines[ii][3] + lines[ii][7]);
	    if (r < 0.75 * dpix) {
	       g1.drawLine (x0+dz0, height - y0, x1+dz1, height - y1);
	       g2.drawLine (x0, height - y0, x1, height - y1);

	    } else {
	       int npw = (int)(r/dpix + 0.5);
	       double dl = Math.sqrt ((y1-y0)*(y1-y0) + (x1-x0)*(x1-x0));
	       double vnx = -r/dpix * (y1 - y0) / dl;
	       double vny =  r/dpix * (x1 - x0) / dl;
	       
	       xpol[0] = (int)(x0+dz0 - vnx);
	       xpol[1] = (int)(x0+dz0 + vnx);
	       xpol[2] = (int)(x1+dz1 + vnx);
	       xpol[3] = (int)(x1+dz1 - vnx);
	       
	       ypol[0] = height - (int)(y0 - vny);
	       ypol[1] = height - (int)(y0 + vny);
	       ypol[2] = height - (int)(y1 + vny);
	       ypol[3] = height - (int)(y1 - vny);
	       
	       g1.fillPolygon (xpol, ypol, 4);


	       xpol[0] = (int)(x0 - vnx);
	       xpol[1] = (int)(x0 + vnx);
	       xpol[2] = (int)(x1 + vnx);
	       xpol[3] = (int)(x1 - vnx);
	       
	       g2.fillPolygon (xpol, ypol, 4);
	    }



	 }
      }
      
   }


   public void drawAreaRedGreenLineList9 (Graphics g, double[][] lines, 
					  double zrg) {
      int ii, i, j, k, nl, x0, y0, z0, x1, y1, z1, dz0, dz1;
      double c0, c1;
      double[] p0, p1;
      p0 = new double[3];
      p1 = new double[3];

      nl = lines.length;
      for (ii = 0; ii < nl; ii++) {
	 p0[0] = lines[ii][0];
	 p0[1] = lines[ii][1];
	 p0[2] = lines[ii][2] ;

	 p1[0] = lines[ii][4];
	 p1[1] = lines[ii][5];
	 p1[2] = lines[ii][6];
	 
	 g.setColor (lines[ii][8] >= 0.0 ? Color.yellow : fgColor);
	 
	 // get pixels from 3d transform....
	 x0 = xOfP(p0);
         y0 = yOfP(p0);
         z0 = zOfP(p0);

	 x1 = xOfP(p1);
         y1 = yOfP(p1);
         z1 = zOfP(p1);

	 dz0 = (int) (zrg * z0);  
	 dz1 = (int) (zrg * z1);  

	 boolean isvis = ((x0 > 0 && x0 < width && y0 > 0 && y0 < height) ||
			  (x1 > 0 && x1 < width && y1 > 0 && y1 < height));
	 if (isvis) {
	    g.setColor (Color.red);
	    g.drawLine (x0+dz0, height - y0, x1+dz1, height - y1);
	    g.setColor (Color.green);
	    g.drawLine (x0, height - y0, x1, height - y1);
	 }
      }
      
   }




   public void drawPointList (Graphics g, double[][] points) {
      int ii, i, j, k, nl;
      double c0, c1;
      double[] p0, p1;
      p0 = new double[3];

      nl = points.length;
      for (ii = 0; ii < nl; ii++) {
//	 if (points[ii][3] > 0.5) {
	    int x0 = xOfP(points[ii]);
	    int y0 = yOfP(points[ii]);

	    g.setColor (points[ii][4] <= 0 ? Color.blue : 
			(points[ii][4] == 2 ? Color.green : Color.cyan));
	    
	    if (x0 > 0 && x0 < width && y0 > 0 && y0 < height) {
	       g.fillOval (x0-3, height-y0-3, 7, 7); 
	    }
//	 }
      }
   }




   public void rotate3init (int x, int y) { 
      double[] p = new double[3];
      p[0] = x - x3c;
      p[1] = y - y3c;
      p[2] = zcen - z3c;
      double[] r = rotInv (p);
      cen3[0] += r[0];
      cen3[1] += r[1];
      cen3[2] += r[2];
      x3c = x;
      y3c = y;
      z3c = zcen;
      for (int i= 0; i < 3; i++) {
	 for (int j = 0; j < 3; j++) {
	    m3sav[i][j] = m3[i][j];
	 }
      }
      initialisedRotation = true;
   }
   
   public void unsetRotInit () {
      initialisedRotation = false;
   }


   public void rotateXY (double rx, double ry) {
      double cx = Math.cos (rx);
      double sx = Math.sin (rx);

      double cy = Math.cos (ry);
      double sy = Math.sin (ry);
      
      m3[0][0] = cy * m3sav[0][0] -sx*sy* m3sav[1][0] + cx*sy* m3sav[2][0];
      m3[0][1] = cy * m3sav[0][1] -sx*sy* m3sav[1][1] + cx*sy* m3sav[2][1];
      m3[0][2] = cy * m3sav[0][2] -sx*sy* m3sav[1][2] + cx*sy* m3sav[2][2];

      m3[1][0] = 0. * m3sav[0][0]  + cx * m3sav[1][0] + sx * m3sav[2][0];
      m3[1][1] = 0. * m3sav[0][1]  + cx * m3sav[1][1] + sx * m3sav[2][1];
      m3[1][2] = 0. * m3sav[0][2]  + cx * m3sav[1][2] + sx * m3sav[2][2];

      m3[2][0] = -sy * m3sav[0][0] -sx*cy* m3sav[1][0] + cx*cy* m3sav[2][0];
      m3[2][1] = -sy * m3sav[0][1] -sx*cy* m3sav[1][1] + cx*cy* m3sav[2][1];
      m3[2][2] = -sy * m3sav[0][2] -sx*cy* m3sav[1][2] + cx*cy* m3sav[2][2];

   }




   public void axisRotate (double thax, double thr, 
			   double[][] m3a, double[][] m3b) {
      // rotate through angle thr about the line in the x-y plane making
      // angle thax with the line y = 0;

      double cf, sf, cr, sr;

      cf = Math.cos (thax);
      sf = Math.sin (thax);

      cr = Math.cos (thr);
      sr = Math.sin (thr);
      
      for (int i = 0; i < 3; i++) {
	 m3b[0][i] = (cf * cf + sf * cr * sf) * m3a[0][i] +
		    (cf * sf - sf * cr * cf) * m3a[1][i] + 
                    (-sr * sf) * m3a[2][i];

	 m3b[1][i] = (sf * cf - cf * cr * sf)  * m3a[0][i] +
                    (sf * sf + cf * cr * cf) * m3a[1][i] +
                    (cf * sr) * m3a[2][i];

	 m3b[2][i] = (sr * sf)  * m3a[0][i] +
                    (-sr * cf) * m3a[1][i] + 
                    (cr) *  m3a[2][i];
      }
   }



   public void zRotate (double theta, double[][] m3a, double[][] m3b) {
      // rotate through angle thr about the line in the x-y plane making
      // angle thax with the line y = 0;

      double cf, sf;

      cf = Math.cos (theta);
      sf = Math.sin (theta);

      for (int i = 0; i < 3; i++) {
	 m3b[0][i] = (cf ) * m3a[0][i] +
		    (sf) * m3a[1][i]; 

	 m3b[1][i] = (-sf)  * m3a[0][i] +
                    (cf) * m3a[1][i];

	 m3b[2][i] = m3a[2][i];
      }
   }






   public void dragRotate (int xdown, int ydown, int x, int y, boolean mode1) {
     /*
     if (!initialisedRotation) rotate3init (xdown, ydown);
     double ry = (3.0 *  (x - xdown)) / width;
     double rx = (3.0 *  (y - ydown)) / height;
     */
      double dx, dy;
      double dd = 1. / width;
      double pi = 3.14159;

      if (!initialisedRotation) {
	 xcen = leftMargin + (width - leftMargin - rightMargin)/2;
	 ycen = bottomMargin + (height - topMargin - bottomMargin)/2;
	 rotate3init (xcen, ycen);

	 dx = x - xcen;
	 dy = y - ycen;
	 thax0 = Math.atan2(dx, -dy);// + 0.5 * pi;
	 r0 = Math.sqrt (dx * dx + dy * dy) / width;
	 r0 =  pi * (1. / (1. + Math.exp (6. *  r0)) - 0.5);
      }

      dx = x - xcen;
      dy = y - ycen;
      thax1 = Math.atan2(dx, -dy);// + 0.5 * pi;
      r1 = Math.sqrt (dx * dx + dy * dy) / width;
      r1 =  pi * (1. / (1. + Math.exp (6. *  r1)) - 0.5);

      if (mode1) {
	 axisRotate (thax0+pi, r0, m3sav, m3t);
	 axisRotate (thax1, r1, m3t, m3);
      } else {
	 zRotate (-Math.atan2((double)(xdown - xcen), (double)(ydown - ycen)),
		  m3sav, m3t);
	 zRotate (Math.atan2(dx, dy), m3t, m3);
		       
      }
   }

   
   
   public void panZoom (int xdown, int ydown, 
			int x, int y, 
			boolean longClick) {
      if ((Math.abs (x - xdown) + Math.abs (y - ydown)) > 5) {
	 x3c += x - xdown;
	 y3c += y - ydown;
      } else { 
	 fixZoom (x, y, (longClick ? 2./3. : 3./2.) );
      }
      pixelSet ();
   }

  
  
  public void fixZoom (int x, int y, double f) {
     if (y < height - topMargin && x < width - rightMargin) {
	double[] p = new double[3];
	p[0] = x - x3c;
	p[1] = y - y3c;
	p[2] = zcen - z3c;
	double[] r = rotInv (p);
	cen3[0] += r[0];
	cen3[1] += r[1];
	cen3[2] += r[2];
	x3c = x;
	y3c = y;
	z3c = zcen;
	
	if (x < leftMargin) {
	   // expand only along current y

	} else if (y < bottomMargin) {
	   // expand only along current x direction	   

	} else {
	   // expand in all three
	   for (int i=0; i < 3; i++) {
	      for (int j = 0; j < 3; j++) {
		 m3[i][j] *= f;
	      }
	   }
	}
     }  
     if (y > height - topMargin) tickGridx = tickGridx + (f < 1. ? -1 : 1);
     if (x > width - rightMargin) tickGridy = tickGridy + (f < 1. ? -1 : 1);
  }


   public void setRange (double xl, double xh, double yl, double yh) {
      cen3[0] = 0.5 * (xl + xh);
      cen3[1] = 0.5 * (yl + yh);
      cen3[2] = 0.0;
      
      x3c = width/2;
      y3c = height / 2;
      z3c = 0;

      double[] pp = new double[3];
      pp[0] = xh;
      pp[1] = yh;
      pp[2] = 0.0;

      for (int i=0; i < 3; i++) {
	 for (int j = 0; j < 3; j++) {
	    m3[i][j] = 0.0;
	 }
	 m3[i][i] = 1.0;
      } 

      int x = xOfP(pp);
      int y = yOfP(pp);
      


      double d1 = 1 + Math.sqrt ((x - width / 2) * (x - width/2) + 
				 (y - height / 2) * (y - height / 2));
      double d2 =  Math.sqrt (width * width / 4 + height * height / 4);
      
      double f = d2 / d1;

      for (int i=0; i < 3; i++) {
	 for (int j = 0; j < 3; j++) {
	    m3[i][j] *= f;
	 }
      } 
      pixelSet();
   }



   public void setZorigin (double[] p) {
      zcen = zOfP (p);
   }


   
   public void pixelSet ()  {
      // zcen is current world z of center = maps to 
      double ax, bx, ay, by, az, bz, det;
      double[] rbl, rbr, rtl;
      double[] p = new double[3];
      p[0] = leftMargin - x3c;
      p[1] = bottomMargin - y3c;
      p[2] = zcen - z3c;
      rbl = rotInv (p);

      p[0] = width - rightMargin - x3c;
      p[1] = bottomMargin - y3c;
      p[2] = zcen - z3c;
      rbr = rotInv (p);

      p[0] = leftMargin - x3c;
      p[1] = height - topMargin - y3c;
      p[2] = zcen - z3c;
      rtl = rotInv (p);

      double totx = 0., toty = 0.;
      for (int i=0; i < 3; i++) {
	 totx += (rbr[i] - rbl[i]) * (rbr[i] - rbl[i]);
	 toty += (rtl[i] - rbl[i]) * (rtl[i] - rbl[i]);
      }
      totx = Math.sqrt (totx);
      toty = Math.sqrt (toty);
      
      p[0] = 0; p[1] = 0; p[2] = 0;
      int x0 = xOfP(p);
      int y0 = yOfP(p);

      xRight = (totx * (width - rightMargin - x0)) / 
                       (width - rightMargin - leftMargin);
      xLeft = xRight - totx;
      yTop = (toty * (height - topMargin - y0)) / 
                       (height - topMargin - bottomMargin);
      yBottom = yTop - toty;

      dpdx = (width - leftMargin - rightMargin) / (xRight - xLeft);
      dpdy = (height - topMargin - bottomMargin) / (yTop - yBottom);
   }   

   
   public double[] rotInv (double[] p) {

      double[] r = new double[3];
      double det;

      det = m3[0][0] * (m3[1][1] * m3[2][2] - m3[2][1] * m3[1][2]) -
            m3[0][1] * (m3[1][0] * m3[2][2] - m3[2][0] * m3[1][2]) + 
            m3[0][2] * (m3[1][0] * m3[2][1] - m3[2][0] * m3[1][1]);


      r[0] =  (m3[0][1] * (m3[1][2] * p[2]     - m3[2][2] * p[1]    ) -
               m3[0][2] * (m3[1][1] * p[2]     - m3[2][1] * p[1]    ) +
               p[0]     * (m3[1][1] * m3[2][2] - m3[2][1] * m3[1][2]) ) / det;
 

      r[1] = -(m3[0][0] * (m3[1][2] * p[2]     - m3[2][2] * p[1]    ) -
               m3[0][2] * (m3[1][0] * p[2]     - m3[2][0] * p[1]    ) + 
               p[0]     * (m3[1][0] * m3[2][2] - m3[2][0] * m3[1][2]) ) / det;

      r[2] =  (m3[0][0] * (m3[1][1] * p[2]     - m3[2][1] * p[1]    ) -
               m3[0][1] * (m3[1][0] * p[2]     - m3[2][0] * p[1]    ) + 
               p[0]     * (m3[1][0] * m3[2][1] - m3[2][0] * m3[1][1]) ) / det;

      double[] rp = new double[3];
      for (int i = 0; i < 3; i++) {
	 for (int j=0; j < 3; j++) rp[i] += m3[i][j] * r[j];
      }
      double ddif = 0.0;
      for (int i = 0; i < 3; i++) ddif += (p[i] - rp[i]) * (p[i] - rp[i]);

      if (ddif > 0.0001) {
	 System.out.println ("Error inv arg: "+p[0]+" " + p[1] + " " + p[2]);
	 System.out.println ("inv res: " + r[0] + " " + r[1] + " " + r[2]);
	 System.out.println ("inv m*res: " + rp[0] + " " + rp[1] + " "+rp[2]);
      }
      return r;
   }


   public double[] worldToPixel3 (double[] world) {
      double[] p3 = new double[3];
      p3[0] = xOfP(world);
      p3[1] = yOfP(world);
      p3[2] = zOfP(world);
//      System.out.println (" wtp3 " + world[0] +" " +world[1] + " " +world[2]);
//      System.out.println (" wtp3 " + p3[0] + " " + p3[1] + " " + p3[2]);

      return p3;
   }
   
   
   public double[] pixel3ToWorld (double[] pix3) {
      double[] r = new double[3];
      r[0] = pix3[0] - x3c;
      r[1] = pix3[1] - y3c;
      r[2] = pix3[2] - z3c;
      double[] ri = rotInv (r);
      ri[0] = ri[0] / xScale + cen3[0];
      ri[1] = ri[1] / yScale + cen3[1];
      ri[2] = ri[2] / zScale + cen3[2];

//      System.out.println (" p3tw " + pix3[0] + " " +pix3[1] + " " +pix3[2]);
//      System.out.println (" p3tw " + ri[0] + " " + ri[1] + " " + ri[2]);
      return ri;
   }

}



class edgeCube extends Object {

   double[][][] linelist;
   double[][] ll9;
   double[] tl;

   // linelist [no of lines] [2 = no of points] [3 = no of dims]

   edgeCube () {
      int i, j, k, l, m, n;
      linelist = new double[12][2][3];
      ll9 = new double[12][10];

      double[] tl = new double[3];
      n = 0;
      for (i = 0; i <= 1; i++) {
	 for (j = 0; j <= 1; j++) {
            for (k = 0; k <= 1; k++) {
	      tl[0] = i; tl[1] = j; tl[2] = k;
	      for (l = 0; l <= 2; l++) {
		if (tl[l] < 0.5) {
		  for (m=0; m <=2; m++) {
		     linelist[n][0][m] = tl[m];
		     linelist[n][1][m] = tl[m];
		  }
		  linelist[n][1][l] = 1.0;
		  n++;
		}
	      }
	    }
	 }
      }
   }


   public void draw (Graphics g, graphData3 gd) {
      double[] p = new double[3];
      p[0] = 0.5; p[1] = 0.5; p[2] = 0.5;
      gd.setZorigin (p);

      gd.drawLineList (g, linelist);
   }
      


   public void offsetScaledDraw (Graphics g, graphData3 gd, 
				 double dy, double[] cen3) {

      for (int ii = 0; ii < 12; ii++) {
	 ll9[ii][0] = cen3[0] + dy * (linelist[ii][0][0] - 0.5);
	 ll9[ii][1] = cen3[1] + dy * (linelist[ii][0][1] - 0.5);
	 ll9[ii][2] = cen3[2] + dy * (linelist[ii][0][2] - 0.5);
	 ll9[ii][4] = cen3[0] + dy * (linelist[ii][1][0] - 0.5);
	 ll9[ii][5] = cen3[1] + dy * (linelist[ii][1][1] - 0.5);
	 ll9[ii][6] = cen3[2] + dy * (linelist[ii][1][2] - 0.5);
	 ll9[ii][8] = -1.;
      }
      gd.drawLineList9 (g, ll9);

   }
}










