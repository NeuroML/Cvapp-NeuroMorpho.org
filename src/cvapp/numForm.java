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

abstract class numForm {


  public static String prettyTrim (double a, int ifig, int ilen) {
     StringBuffer sb = new StringBuffer();
     int imlt, ii, ifir, nf;

     if (a == 0.0) {
	String d = ("0                 ").substring (0, ilen);
	return d;
     }


     if (a < 0.0) {
	sb.append("-");
	a = -a;
     }
     a = Math.abs(a);
     double mlt = Math.log(a) / Math.log(10.0);
     imlt = (int)mlt + (mlt < 0 ? -1 : 0);

     if (imlt > -3 && imlt < 5) {
	ifir = (imlt > 0 ? imlt : 0);
	double pt = Math.pow(10.0, ifir);
	for (nf = 0; nf < ifir - imlt + ifig; nf++) {
	   ii = (int) (a / pt);
	   if ((int) (10. * pt+0.001) == 1) sb.append (".");
	   sb.append ((" " + ii).substring (1, 2));
	   a -= pt * ii;
	   pt /= 10.;
	}
	for (nf = 0; nf <= imlt - ifig; nf++) sb.append ("0");
  
     } else {
	String t = " " + a / Math.pow(10.0, imlt) + "         ";
	sb.append (t.substring (1, ifig+2) + "e"+ (imlt> 0 ? "+" : "")+imlt); 
     }

     return sb.toString();

  }






  public static String prettyTrim (int a, int ifig, int ilen) {
    String d = " " + a + "             ";
    return d.substring(1, 1+ilen);
  }
 


  public static double trim (double a, int itr) {
       double sig =  (a / Math.abs(a));
       a = Math.abs(a);
       double mlt = Math.log(a) / Math.log(10.0) - itr;
       int imlt = (int) (mlt + (mlt > 0.0 ? 0.99 : 0.0));
       int ia = (int) (a / Math.pow(10.0, imlt));
       a = ia * Math.pow(10.0, imlt);
       double d =  sig * a;
       return d;
  }
}
