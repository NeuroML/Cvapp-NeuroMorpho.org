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
import java.text.*;

class hocWriter extends Object {
   
   Vector points;
   String[] sectionTypes;
   int[] segPerTyp;
   StringBuffer sb1;
   StringBuffer sb2;
   NumberFormat form;

   hocWriter (Vector pts, String[] st) {
      points = pts;
      sectionTypes = st;
      form = NumberFormat.getInstance();
   }





   private void pt3dappend (nlpoint p, double dz) {
      sb2.append ("{pt3dadd(");
      sb2.append (form.format(p.x));
      sb2.append(",");
      sb2.append (form.format(p.y));
      sb2.append(",");
      sb2.append (form.format(p.z + dz));
      sb2.append(",");
      sb2.append (form.format(2 * p.r));
      sb2.append(")}\n");
   }



   public void recHOCTraceNS (nlpoint ppar, nlpoint p, 
			    boolean newseg, int mysegind) {

      if (newseg) {
	 int ityp = p.nlcode;
	 if (ityp < 0) ityp = 0;
	 mysegind = segPerTyp[ityp];
	 sb2.append ("{access ");
	 sb2.append (segmentName (ityp));
	 sb2.append ("[");
	 sb2.append (form.format(segPerTyp[ityp]));
	 sb2.append ("]}\n");
	 sb2.append ("{pt3dclear()}\n");
	 segPerTyp[ityp]++;

	 
	 if (ppar != p) {
	    pt3dappend (ppar, 0.0);
	 } else {
	    // first segment of whole structure - add a fictitious;
	    // point to make it at least two points long;
	    pt3dappend (p, 0.01);
	 }
      }


      pt3dappend (p, 0.0);
      p.imark = 1;

      int numd = 0;
      boolean diftype = false;
      for (int i = 0; i < p.nnbr; i++) {
	 if (p.pnbr[i].imark == 0) {
	    numd++;
	    if (p.pnbr[i].nlcode != p.nlcode) diftype = true;
	 }
      }

      boolean nextnew = false;
      if (diftype || numd > 1) {
	 nextnew = true;
      }

      for (int i = 0; i < p.nnbr; i++) {
	 if (p.pnbr[i].imark == 0) {
	    nlpoint pd = p.pnbr[i];
	    if (nextnew) {
	       sb2.append ("\n{");
	       sb2.append (segmentName (p.nlcode));
	       sb2.append ("[");
	       sb2.append (form.format(mysegind));
	       sb2.append ("] connect ");
	       
	       sb2.append (segmentName (pd.nlcode));
	       sb2.append ("[");
	       sb2.append (form.format(
                          segPerTyp[pd.nlcode < 0 ? 0 : pd.nlcode]));
	       sb2.append ("](0), 1}\n");
	    }
	    recHOCTraceNS (p, pd, nextnew, mysegind);
	 }
      }
   }
   




   public String hocString() {
      if (points.size() < 2 || sectionTypes.length < 2 ||
	  !sectionTypes[1].equals("soma")) {
	 System.out.println ("error: null data or section types in hocWrite");
	 return "";
      }
      
      //default starting point;
      nlpoint p0 = null;

      int np = points.size();
      segPerTyp = new int[np];
      for (int i = 0; i < np; i++) {
	 nlpoint p = ithPoint(i);
	 p.imark = 0;
	 if (p0 == null && p.nlcode == 1) {
	    // check for precicely one somatic neighbour;
	    int nsn = 0;
	    int jsom = -1;
	    for (int j = 0; j < p.nnbr; j++) {
	       if (p.pnbr[j].nlcode == 1) {
		  nsn++;
		  jsom = j;
	       }
	    }
	    if (nsn == 1) {
	       // ok got it - if > 1 neighbours, put the somatic one first;
	      if (jsom > 0) {
		 nlpoint dum = p.pnbr[jsom];
		 p.pnbr[jsom] = p.pnbr[0];
		 p.pnbr[0] = dum;
	      }
	      p0 = p;
	    }
	 }
      }
      if (p0 == null) p0 = ithPoint (0);

      sb1 = new StringBuffer();
      sb2 = new StringBuffer();

      // write the points to sb2;
      recHOCTraceNS (p0, p0, true, 0);

      
      // declare segment arrays - bit of overkill, but negligible in time;
      String styp = " ";
      for (int i = 0; i < np; i++) {
	 if ((segPerTyp[i]) > 0) {
	    styp = segmentName (i);
	    sb1.append ("{create ");
	    sb1.append (styp);
	    sb1.append("[");
	    sb1.append (form.format(segPerTyp[i]).trim());
	    sb1.append ("]}\n");
	 }
      }
      sb1.append ("\n");

      
      StringBuffer sbf = new StringBuffer();
      sbf.append (sb1.toString());
      sbf.append (sb2.toString());
      return sbf.toString();
   }



   private String segmentName (int ityp) {
      String styp = "";
      if (ityp >= 5) {
	 styp = "user"+ form.format(ityp).trim();
      } else { 
	 if (ityp < 0) {
	    System.out.println ("warning: negative segment type converted " + 
				"to \"unknown\" ");
	    ityp = 0;
	 }
	 styp = sectionTypes[ityp]; 
      }
      return styp;
   }


   private nlpoint ithPoint (int i) {
      return (nlpoint)(points.elementAt(i));
   }


}


