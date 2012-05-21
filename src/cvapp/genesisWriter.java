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

class genesisWriter extends Object {
   
   Vector points;
   String[] sectionTypes;
   int[] segPerTyp;
   StringBuffer sb1;
   StringBuffer sb2;
   NumberFormat form;
   boolean declaredSoma = false;
   boolean declaredDend = false;
   boolean declaredAxon = false;
   int isoma = -1;
   int iaxon = -1;
   int ndend = 0;
   nlpoint psoma = null;
   nlpoint paxon = null;
   nlpoint ppaxon = null;
   boolean followAxon = false;
   boolean flat = false;
   int cwi = 0;

   genesisWriter (Vector pts, String[] st) {
      points = pts;
      sectionTypes = st;
      form = NumberFormat.getInstance();
      int nt = st.length;
      for (int i = 0; i < nt; i++) {
	 if (st[i].startsWith ("soma")) isoma = i;
	 if (st[i].startsWith ("axon")) iaxon = i;
      }
      if (isoma < 0 || iaxon < 0) {
	 System.out.println ("warning - unknown segment types");
      }
   }

   public void setFlatStyle (boolean b) {
      flat = b;
   }



   private void ptappend (nlpoint p) {
      sb2.append("  ");
      sb2.append (form.format(p.x));
      sb2.append("  ");
      sb2.append (form.format(p.y));
      sb2.append("  ");
      sb2.append (form.format(p.z));
      sb2.append("  ");
      sb2.append (form.format(2 * p.r));
      sb2.append(" \n");
   }




   private void relativeAppend (nlpoint p, nlpoint ppar) {
      if (ppar == null) ppar = p;
      sb2.append("  ");
      sb2.append (form.format(p.x - ppar.x));
      sb2.append("  ");
      sb2.append (form.format(p.y - ppar.y));
      sb2.append("  ");
      sb2.append (form.format(p.z - ppar.z));
      sb2.append("  ");
      sb2.append (form.format(2 * p.r));
      sb2.append(" \n");
   }




   public void recGenesisTrace (nlpoint ppar, nlpoint p, int idaughter) {

      /* WARNING - the following in _not_ type-blind: it assumes correct
       segment labelling, with one soma, one axon, and posibly 
       multiple dendrites from the soma. Anything else may give 
       unpredictable results
       */
      
     boolean absolute = false; //-----;

     if (p.identPoint == null) {
	boolean setname = false;
	String dectxt = "";
	if (p.nlcode == isoma && psoma == null) {
	   if (!declaredSoma) {
	      declaredSoma = true;
	       dectxt = "*compt /library/compartment_sphere\n";
	      p.name = "soma";
		psoma = p;
	      p.iseg = 0;
	      setname = true;
	   }
	} else if ( p.nlcode == iaxon) {
	   if (!declaredAxon) {
	      p.name = "a";
		p.iseg = 0;
	      declaredAxon = true;
	      dectxt = "*compt /library/axon\n";
	      setname = true;
	   }
	} else {
	   if (!declaredDend) {
		declaredDend = true;
		dectxt = "*compt /library/compartment\n";
		ndend = 1;
		p.name = "d" + ndend;
		p.iseg = 0;
		setname = true;
	     }
	}
	
	if (!flat) sb2.append (dectxt);


	String nametxt = "";
	nlpoint pp0 = ppar;
	if (ppar != null && ppar.identPoint != null) ppar = ppar.identPoint;
	
	  if (ppar == null) {
	     if (p.nlcode == isoma) {
		nametxt = p.name + " ";
	     } else {
	        nametxt = p.name + "[" + p.iseg + "] ";
	     }
	     nametxt += "none";
	     
	  } else if (setname) {
	     nametxt = p.name + "[" + p.iseg + "] ";
	     nametxt += (ppar == psoma ? " soma " : 
			 ppar.name + "[" + ppar.iseg + "] ");
	     
	  } else {
	     if (idaughter == 0 ){
		if (ppar == psoma) {
		   ndend++;
		   p.name = "d" + ndend;
		} else {
		   p.name = ppar.name;
		   p.iseg = ppar.iseg+1;
		}
		nametxt = p.name + "[" + p.iseg + "] ";
		nametxt +=  (" . ");
	     } else {
		if (ppar == psoma) {
		   ndend++;
		   p.name = "d" + ndend;
		} else {
		   p.name = ppar.name + "b" + idaughter;
		}
		p.iseg = 0;
		nametxt = p.name + "[" + p.iseg + "] ";
		nametxt +=  (ppar == psoma ? " soma " : 
			     ppar.name + "[" + ppar.iseg + "] ");
	     }
	  }
	
        if (!flat) sb2.append (nametxt);
	
	if (flat) {
	   cwi++;
	   p.writeIndex = cwi;
	   sb2.append (p.writeIndex + "_" + p.nlcode + " ");
	   if (ppar != null) {
	      sb2.append (ppar.writeIndex + "_" + ppar.nlcode + " " );
	   }  else {
	      sb2.append (" none ");
	   }
	}
	
	
	if (absolute) {
	   ptappend (p);
	} else {
	   relativeAppend (p, pp0);
	}
     }      


      p.imark = 1;
     // if multiple daughters, reorder them to do somatic segments first;
     // and axons last;

      int numd = 0;
      for (int i = 0; i < p.nnbr; i++) {
	 if (p.pnbr[i].imark == 0) numd++;
      }

      int ida = 0;
      for (int i = 0; i < p.nnbr; i++) {
	 if (p.pnbr[i].imark == 0) {
	    ida++;
	    nlpoint pd = p.pnbr[i];
	    if (pd.nlcode != iaxon || followAxon) {
	       recGenesisTrace (p, pd, numd == 1 ? 0 : ida);
	    } else if (paxon == null && pd.nlcode == iaxon) {
	       ppaxon = p;
	       paxon = pd;
	    }
	 }
      }
   }
   




   public String hierarchicalString() {
      cwi = 0;

      if (points.size() < 2 || sectionTypes.length < 2 ||
	  !sectionTypes[1].equals("soma")) {
	 System.out.println ("error: null data or section types " +
			     "in genesisWriter");
	 return "";
      }
      
      declaredSoma = false;
      declaredDend = false;
      declaredAxon = false;
      ndend = 0;
      psoma = null;

      //default starting point;
      nlpoint p0 = null;

      int np = points.size();
      segPerTyp = new int[np];
      for (int i = 0; i < np; i++) {
	 nlpoint p = ithPoint(i);
	 if (p0 == null || (p.nlcode == isoma && p.r > p0.r)) p0 = p;
	 p.imark = 0;	 
      }

      if (p0 == null) {
	 System.out.println ("warning  - no soma segment found");
	 p0 = ithPoint (0);
      }
      p0.name = "soma";
      p0.iseg = 0;

      for (int i = 0; i < p0.nnbr; i++) {
	if (p0.pnbr[i].nlcode == isoma) {
	   System.out.println ("warning - multiple points marked as soma");
	   System.out.println ("point of maximal radius taken as soma, " + 
			       " rest converted to dendrites");
	   
	}
      }


      sb1 = new StringBuffer();
      sb2 = new StringBuffer();

      // write the points to sb2;
      followAxon = false;
      recGenesisTrace (null, p0, 0);
      
      if (paxon != null) {
	 followAxon = true;
	 recGenesisTrace (ppaxon, paxon, 0);
      }

      sb1.append ("// Genesis hierarchical morphology written by cvapp  \n");
      sb1.append ("// ");
      sb1.append ((new Date()).toString());
      sb1.append ("\n");
      String[] sah = {" ",
		      "*relative",
		      " ",
		      "*set_compt_param RM {RM}",
		      "*set_compt_param RA {RA}",
		      "*set_compt_param CM {CM}",
		      "*set_global ELEAK {ELEAK}",
		      "*set_global EREST_ACT {EREST_ACT}", 
		      " ",
		      " "};
      for (int i = 0; i < sah.length; i++) {
	 sb1.append (sah[i]);
	 sb1.append ("\n");
      }
      
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


