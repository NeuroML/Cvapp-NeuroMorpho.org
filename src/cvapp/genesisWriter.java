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
    StringBuffer header;
    StringBuffer compsInfo;
    NumberFormat form;
    boolean declaredSoma = false;
    boolean declaredDend = false;
    boolean declaredAxon = false;
    int isoma = -1;
    int iaxon = -1;
    int ndend = 0;
    int nsoma = 0;
    nlpoint psoma = null;
    nlpoint paxon = null;
    nlpoint ppaxon = null;
    //boolean followAxon = false;
    //boolean flat = false;
    int cwi = 0;
    //boolean verbose = true;
    boolean verbose = false;
    private String morphologyOrigin;

    boolean onlyOneSomaPoint = true;

    genesisWriter(Vector pts, String[] st, String morphologyOrigin) {
        points = pts;
        sectionTypes = st;
        form = NumberFormat.getInstance();
        // NMO Developer : Added to remove insertion of comma in saved file if number exceeds 999.
        form.setGroupingUsed(false);
        int nt = st.length;
        for (int i = 0; i < nt; i++) {
            if (st[i].startsWith("soma")) {
                isoma = i;
            }
            if (st[i].startsWith("axon")) {
                iaxon = i;
            }
        }
        if (isoma < 0 || iaxon < 0) {
            System.out.println("WARNING - unknown segment types");
        }
        this.morphologyOrigin = morphologyOrigin;
    }

    //public void setFlatStyle(boolean b) {
    //    flat = b;
    //}

    private void ptappend(nlpoint p, StringBuilder sb) {
        sb.append("  ");
        sb.append(form.format(p.x));
        sb.append("  ");
        sb.append(form.format(p.y));
        sb.append("  ");
        sb.append(form.format(p.z));
        sb.append("  ");
        sb.append(form.format(2 * p.r));
        sb.append(" \n");
    }

    private void ptappend(nlpoint p, nlpoint ppar, StringBuilder sb) {
        sb.append("  ");
        sb.append(form.format(ppar.x));
        sb.append("  ");
        sb.append(form.format(ppar.y));
        sb.append("  ");
        sb.append(form.format(ppar.z));
        sb.append("  ");
        sb.append(form.format(p.x));
        sb.append("  ");
        sb.append(form.format(p.y));
        sb.append("  ");
        sb.append(form.format(p.z));
        sb.append("  ");
        sb.append(form.format(2 * p.r));
        sb.append(" \n");
    }
    /*
    private void relativeAppend(nlpoint p, nlpoint ppar) {
        if (ppar == null) {
            ppar = p;
        }
        compsInfo.append("  ");
        compsInfo.append(form.format(p.x - ppar.x));
        compsInfo.append("  ");
        compsInfo.append(form.format(p.y - ppar.y));
        compsInfo.append("  ");
        compsInfo.append(form.format(p.z - ppar.z));
        compsInfo.append("  ");
        compsInfo.append(form.format(2 * p.r));
        compsInfo.append(" \n");
    }*/

    public void recGenesisTrace(nlpoint ppar, nlpoint p, int daughterIndex) {

        StringBuilder line = new StringBuilder();
        StringBuilder lineN = new StringBuilder();

        if (verbose) {
            line.append("\n//-------------------------------\n//  daughterIndex: " + daughterIndex + "...\n");
            line.append("\n//  Adding point: " + p.toString().replaceAll("\n", "\n//  ") + "\n");
            if (ppar!=null)
                line.append("\n//  Its parent:   " + ppar.toString().replaceAll("\n", "\n//  ") + "\n");
        }

        /* OLD WARNING from Robert: the following in _not_ type-blind: it assumes correct
        segment labelling, with one soma, one axon, and posibly
        multiple dendrites from the soma. Anything else may give
        unpredictable results
         */

        //boolean absolute = true; //-----;


        boolean ignoreAttachmentToSoma = false;

        if (p.identPoint == null) {
            boolean setname = false;
            String dectxt = "";

            if (p.nlcode == isoma) {

                if (ppar==null){
                    for (nlpoint np: p.pnbr){
                        if (np!=null){
                            onlyOneSomaPoint = onlyOneSomaPoint && (np.nlcode != isoma);
                            System.out.println("onlyOneSomaPoint: "+ onlyOneSomaPoint);
                        }
                    }
                }
                if (onlyOneSomaPoint) {
                    if (!declaredSoma) {
                        declaredSoma = true;
                        //System.out.println("ggg"+p);
                        dectxt = "*compt /library/compartment_sphere\n";

                        p.name = "soma";
                        nsoma = 1;
                        psoma = p;
                        p.iseg = 0;
                        setname = true;
                    }
                } else {
                    if (ppar!=null){  // second soma point
                        if (!declaredSoma) {
                            declaredSoma = true;

                            p.name = "soma";
                            nsoma = 1;
                            psoma = p;
                            p.iseg = 0;
                            setname = true;
                        }
                    } else {
                        dectxt = "*compt /library/compartment\n*double_endpoint\n";
                    }
                }

            } /*else if (p.nlcode == iaxon) {
                if (!declaredAxon) {
                    p.name = "a";
                    p.iseg = 0;
                    declaredAxon = true;
                    dectxt = "*compt /library/axon\n";
                    setname = true;
                }
            }*/ else {
                if (!declaredDend) {
                    declaredDend = true;
                    dectxt = "\n*compt /library/compartment\n*double_endpoint\n";
                    ndend = 1;
                    p.name = "d" + ndend;
                    p.iseg = 0;
                    setname = true;
                }
            }

            /*
            if (p.nlcode == isoma && psoma == null) {
                
                if (!declaredSoma) {
                    declaredSoma = true;
                    for (nlpoint np: p.pnbr){
                        if (np!=null){
                            onlyOneSomaPoint = onlyOneSomaPoint && (np.nlcode != isoma);
                            System.out.println("onlyOneSomaPoint: "+ onlyOneSomaPoint);
                        }
                    }

                    if (onlyOneSomaPoint)
                        dectxt = "*compt /library/compartment_sphere\n";
                    
                    p.name = "soma";
                    nsoma = 1;
                    psoma = p;
                    p.iseg = 0;
                    setname = true;
                }
                 
            } else if (p.nlcode == iaxon) {
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
                    dectxt = "*compt /library/compartment\n*double_endpoint\n";
                    ndend = 1;
                    p.name = "d" + ndend;
                    p.iseg = 0;
                    setname = true;
                }
            }*/

            if (p.nlcode!=isoma && ppar!=null && ppar.nlcode==isoma)
            {
                ignoreAttachmentToSoma = true;
                //declaredDend = false;
            }

            //if (!flat) {
            compsInfo.append(dectxt);
            //}


            String nametxt = "";
            String nametxt2 = "";

            ////////////////////////////////////////
            if (ppar == null) {
                nametxt2 = p.name + "[" + p.iseg + "] ";
                nametxt2 += "none";

            } else if (setname) {
                nametxt2 = p.name + "[" + p.iseg + "] ";
                if (p.equals(psoma)) {
                    nametxt2 += " none ";
                } else {
                    nametxt2 += ppar.name + "[" + ppar.iseg + "] ";
                }

            }else {
                if (daughterIndex == 0) {
                    if (ppar == psoma) {
                        ndend++;
                        p.name = "d" + ndend;
                    } else {
                        p.name = ppar.name;
                        p.iseg = ppar.iseg + 1;
                    }
                    nametxt2 = p.name + "[" + p.iseg + "] ";
                    nametxt2 += (" . ");
                    //nametxt2 += (ppar.name + "[" + ppar.iseg + "]b ");
                } else {
                    if (ppar == psoma) {
                        ndend++;
                        p.name = "d" + ndend;
                    } else {
                        p.name = ppar.name + "b" + daughterIndex;
                    }
                    p.iseg = 0;
                    nametxt2 = p.name + "[" + p.iseg + "] ";
                    nametxt2 += ppar.name + "[" + ppar.iseg + "] ";
                }
            }
            /////////////////////////////////////////


            //if (!flat) {
            line.append(nametxt2);
            //}

            /*if (flat) {
                cwi++;
                p.writeIndex = cwi;
                line.append(p.writeIndex + "_" + p.nlcode + " ");
                if (ppar != null) {
                    line.append(ppar.writeIndex + "_" + ppar.nlcode + " ");
                } else {
                    line.append(" none ");
                }
            }*/

            if (ppar!=null) {
                    ptappend(p, ppar, line);
            } else {
                ptappend(p, line);
            }
           
        }

        if (!ignoreAttachmentToSoma) {
            if (! (ppar==null && !onlyOneSomaPoint) )
                compsInfo.append(line);
        }


        p.imark = 1;
        // if multiple daughters, reorder them to do somatic segments first;
        // and axons last;

        int numNeighbsNotDone = 0;
        for (int i = 0; i < p.nnbr; i++) {
            if (p.pnbr[i].imark == 0) {
                numNeighbsNotDone++;
            }
        }

        int newDaughterIndex = 0;
        for (int i = 0; i < p.nnbr; i++) {
            if (p.pnbr[i].imark == 0) {
                newDaughterIndex++;
                nlpoint pd = p.pnbr[i];

                ///if (pd.nlcode != iaxon || followAxon) {
                    recGenesisTrace(p, pd, numNeighbsNotDone == 1 ? 0 : newDaughterIndex);
                ///} else if (paxon == null && pd.nlcode == iaxon) {
                ///    ppaxon = p;
                ///    paxon = pd;
                ///}
            }
        }
    }

    public String hierarchicalString() {
        cwi = 0;

        if (points.size() < 2 || sectionTypes.length < 2
            || !sectionTypes[1].equals("soma")) {
            System.out.println("error: null data or section types "
                + "in genesisWriter");
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
            if (p0 == null || (p.nlcode == isoma && p.r > p0.r)) {
                p0 = p;
            }
            p.imark = 0;
        }

        if (p0 == null) {
            System.out.println("warning  - no soma segment found");
            p0 = ithPoint(0);
        }
        p0.name = "soma";
        p0.iseg = 0;

        for (int i = 0; i < p0.nnbr; i++) {
            if (p0.pnbr[i].nlcode == isoma) {
                System.out.println("Warning - multiple points marked as soma");
                System.out.println("Point of maximal radius taken as soma, "
                    + " rest converted to dendrites");

            }
        }


        header = new StringBuffer();
        compsInfo = new StringBuffer();

        // write the points to compsInfo;
        ///followAxon = false;

        recGenesisTrace(null, p0, 0);

        /*
        if (paxon != null) {
            followAxon = true;
            recGenesisTrace(ppaxon, paxon, 0);
        }*/

        header.append("//  GENESIS hierarchical morphology written by CVapp (NeuroMorpho.org version)\n");
        header.append("//  Original file: " + morphologyOrigin + " \n\n");
        header.append("//  Generated ");
        header.append((new Date()).toString());
        header.append("\n");

        String[] sah = {" ",
            "*absolute",
            " ",
            "*set_compt_param RM 0.33333   //   ohm m², a typical value, needs to be replaced by the real value",
            "*set_compt_param RA 0.3       // ohm m, a typical value, needs to be replaced by the real value",
            "*set_compt_param CM 0.01      // F m-2, a typical value, needs to be replaced by the real value",
            /*"*set_global ELEAK -0.060      // V, a typical value, needs to be replaced by the real value",*/
            "*set_global EREST_ACT -0.060  // V, a typical value, needs to be replaced by the real value",
            " ",
            " "};

        for (int i = 0; i < sah.length; i++) {
            header.append(sah[i]);
            header.append("\n");
        }

        StringBuilder sbf = new StringBuilder();
        sbf.append(header.toString());
        sbf.append(compsInfo.toString());
        return sbf.toString();
    }

    private String segmentName(int ityp) {
        String styp = "";
        if (ityp >= 5) {
            styp = "user" + form.format(ityp).trim();
        } else {
            if (ityp < 0) {
                System.out.println("warning: negative segment type converted "
                    + "to \"unknown\" ");
                ityp = 0;
            }
            styp = sectionTypes[ityp];
        }
        return styp;
    }

    private nlpoint ithPoint(int i) {
        return (nlpoint) (points.elementAt(i));
    }
}
